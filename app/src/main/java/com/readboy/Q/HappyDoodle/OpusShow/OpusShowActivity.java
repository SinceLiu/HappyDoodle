package com.readboy.Q.HappyDoodle.OpusShow;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.readboy.Q.HappyDoodle.HappyDoodleActivity;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.util.ActivityManagerUtil;
import com.readboy.Q.HappyDoodle.util.BaseActivity;
import com.readboy.Q.HappyDoodle.util.ToastUtils;
import com.readboy.Q.HappyDoodle.util.Utils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Toast;

public class OpusShowActivity extends BaseActivity {
	private static final String TAG = "lqn-OpusShowActivity";
	/** 关闭按钮 */
	private Button mCloseBtn;
	/** 上一张按钮 */
	private Button mLastOpusBtn;
	/** 下一张按钮 */
	private Button mNextOpusBtn;
	/** viewpager控件 */
	private ViewPager mViewPager;

	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 当前显示页 */
	private int mCurPage;
	/** 所有作品的路径*/
	private ArrayList<String> mOpusFilesPath;
	
	/** 图片显示选项 */
	private DisplayImageOptions options;
	/** 是否有焦点 */
	private boolean mHasFocus;
	/** activity是否已暂停 */
	private boolean mHasPause;
	
	private int apMode;		//ap模式
	private String savePicPath;	//蚕宝宝成长记瓢虫的世界或者瓢虫的世界模式保存图片的路径
	private boolean isSilkwormFirst = true;		//第一次进入
	private boolean isLadybugFirst = true;		//第一次进入
	private boolean needStartTimer = false;	//是否需要开启定时器，默认不开启，只有从其他ap跳转进来才要
	private Timer timer;					//定时器
	private int timerCount;					//定时器计数
	private static final int COUNT_SUM = 100;//计时20秒
	private static final int MSG_TIMER = 0x103;
	/** 播放声音对象 */
	private MediaPlayer mMediaPlayer;
	/** 声音是否被打断，比如按home键或进入其它界面了，用于返回该界面时恢复播放 */
	private boolean mIsSndPause = false;
	@Override
	public boolean onInit() {
//		Log.e(TAG, "000000--------------onInit--"+getTaskId()+"------activity = "+this);
		setContentView(R.layout.opus_show);
		ActivityManagerUtil.getInstance().addActivity(this);//将该activity加入activity管理类中
		
		Intent intent = getIntent();
		mPageCount = intent.getIntExtra("TOTAL_PAGE", 0);
		mCurPage = intent.getIntExtra("CUR_PAGE", 0);
		apMode = intent.getIntExtra("apMode", Constant.MODE_NORMAL);
		savePicPath = intent.getStringExtra("savePicPath");
		if(apMode == Constant.MODE_SILKWORM || apMode == Constant.MODE_LADYBUG){
			mPageCount = 1;
			mOpusFilesPath = new ArrayList<String>();
			mOpusFilesPath.add(savePicPath);
			SharedPreferences sp = getSharedPreferences("ap_flag", MODE_PRIVATE);
			isSilkwormFirst = sp.getBoolean("isSilkwormFirst", true);
			isLadybugFirst = sp.getBoolean("isLadybugFirst", true);
		}else {
			mOpusFilesPath = (ArrayList<String>) intent.getSerializableExtra("OPUS_PATH");
		}
		
		
		
		MyOnClickListenner onClickListenner = new MyOnClickListenner();
		mCloseBtn = (Button) findViewById(R.id.closeBtn);
		mLastOpusBtn = (Button) findViewById(R.id.lastOpusBtn);
		mNextOpusBtn = (Button) findViewById(R.id.nextOpusBtn);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		
		mCloseBtn.setOnClickListener(onClickListenner);
		mLastOpusBtn.setOnClickListener(onClickListenner);
		mNextOpusBtn.setOnClickListener(onClickListenner);
		
		if(apMode == Constant.MODE_SILKWORM || apMode == Constant.MODE_LADYBUG){
			mLastOpusBtn.setVisibility(View.GONE);
			mNextOpusBtn.setVisibility(View.GONE);
			if((apMode == Constant.MODE_SILKWORM && isSilkwormFirst)||
					(apMode == Constant.MODE_LADYBUG && isLadybugFirst)){
				needStartTimer = true;
				Log.w(TAG, "------------onStartTimer");
				onStartTimer();
			}
//			playHintSnd();		//播放语音
		}
		
		//MyOnTouchListener onTouchListener = new MyOnTouchListener();
		//mViewPager.setOnTouchListener(onTouchListener); 
		
		initViews();
		
		initImageOptions();
		
		mViewPager.setAdapter(new OpusShowViewPagerAdapter(this, mPageCount, mPageCount));
		mViewPager.setOnPageChangeListener(new MyOnPagerChangeListenner());
		setCurrentPage(mCurPage, false);
		return true;
	}
	
	/**
	 * 播放提示音
	 */
	private void playHintSnd()
	{
		if(mMediaPlayer!=null)
		{
			if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
			mMediaPlayer.reset();
	    	mMediaPlayer.release();
		}
		String sound = "show_remark.ogg";
		int n = (int) (Math.random() * 2);
		if(n == 1){
			if(apMode == Constant.MODE_SILKWORM){
				sound = "silkworm_show0.ogg";
			}else if(apMode == Constant.MODE_LADYBUG){
				sound = "ladybug_show0.ogg";
			}
		}
		mMediaPlayer = DataManager.playSound(sound, false, new MyOnCompleteListenner(0));
		//Log.e(TAG, "------playHintSnd------mIsSndPause="+mIsSndPause);
		if(mIsSndPause || !mHasFocus || mHasPause)
		{
			Log.w(TAG, "------------play sound");
//			mMediaPlayer.pause();
//			mIsSndPause = true;
		}
	}
	
	//定时器
  	public void onStartTimer() {
  		if(timer != null){
  			timer.cancel();
  		}
  		timer = new Timer();
  		timer.schedule(new TimerTask() {
  			@Override
  			public void run() {
  				Message msg = new Message();
  				msg.what = MSG_TIMER;
  				mHandler.sendMessage(msg);
  			}
  		}, 0, 100);
  	}
	
  	@SuppressLint("HandlerLeak")
	final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_TIMER) {
				if(++timerCount >= COUNT_SUM){
					if(timer != null){
						timer.cancel();
						timer = null;
					}
					needStartTimer = false;
					finish();
				}
			}
		}
	};
	
	/**
     * 声音播放结束监听器
     * @author css
     *
     */
    class MyOnCompleteListenner implements OnCompletionListener
    {
    	/** 0代表提示语 */
    	private int flag;
    	
    	/**
    	 * 构造函数
    	 * @param flag 0代表提示语
    	 */
    	public MyOnCompleteListenner(int flag) {
    		this.flag = flag;
		}
    	
		@Override
		public void onCompletion(MediaPlayer mp) {
			if(mp == mMediaPlayer)
			{
				switch (flag) 
				{
				case 0:
					if((apMode == Constant.MODE_SILKWORM && isSilkwormFirst)||
							(apMode == Constant.MODE_LADYBUG && isLadybugFirst)){
						needStartTimer = true;
						Log.w(TAG, "------------onStartTimer");
						onStartTimer();
					}
					break;
				default:
					break;
				}
			}
			
		}
    }
    
    private void deletePic(String path){
    	File file = new File(path);
    	if(file.exists()){
    		file.delete();
    		file = null;
    	}
    }
  	
	private void initViews()
	{
		
	}
	
	private void initImageOptions()
	{
		//磁盘空间不足时不使用硬盘缓存
		if(Utils.getUsableSpace(Environment.getExternalStorageDirectory())<Constant.MIN_DISK_SPACE)
		{
			options = new DisplayImageOptions.Builder()
			//.showStubImage(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			//.cacheInMemory(true)//容易导致out of memory?
			//.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
		}
		else 
		{
			options = new DisplayImageOptions.Builder()
			//.showStubImage(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			//.cacheInMemory(true)//容易导致out of memory?
			.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
		}
	}
	
	
	private void setCurrentPage(int pageIdx,boolean isSmoothScroll)
	{
		mViewPager.setCurrentItem(pageIdx,isSmoothScroll);
	}
	
	public ViewPager getViewPager() {
		return mViewPager;
	}
	
	public DisplayImageOptions getOptions() {
		return options;
	}
	
	public ArrayList<String> getOpusFilesPath() {
		return mOpusFilesPath;
	}
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	@Override
	protected void onContinue() {
    	super.onContinue();
        //Log.i(TAG, "------onResume------");
        mHasPause = false;
        SelectCanvasActivity.mIsNeedPauseBkSnd = true;
        if(mHasFocus)
        {
        	SelectCanvasActivity.resumeBkSnd();
        }
        //mImageWorker.setExitTasksEarly(false);
    }

	@Override
	public void onReinit() {
    	super.onReinit();
//    	if(HappyDoodleApp.DEBUG)
//    		Log.i(TAG, "------onRestart------");
    	//bkAnim.start();
    	//launchBK.beginAnim();
    	if(mIsSndPause && mMediaPlayer != null)
    	{
    		mMediaPlayer.start();
        	mIsSndPause = false;
    	}
    }
	
    @Override
    protected void onSuspend() {
    	super.onSuspend();
        mHasPause = true;
        SelectCanvasActivity.pauseBkSnd();
        
    	if(mMediaPlayer != null && mMediaPlayer.isPlaying())
    	{
    		mMediaPlayer.pause();
    		mIsSndPause = true;
    	}
    	else {
            mIsSndPause = false;
        }
    	
    	if(timer != null){
			timer.cancel();
			timer = null;
		}
    }
    
    /**
     * 按了home键后或锁屏或关闭该activity时会来
     */
    @Override
    public void onHalt() {
    	super.onHalt();
    	if(HappyDoodleApp.DEBUG) {
            Log.i(TAG, "------onStop------");
        }
    	//bkAnim.stop();
    	if(mMediaPlayer != null && mMediaPlayer.isPlaying())
    	{
    		mMediaPlayer.pause();
    	}
    	
    }
	
	@Override
	public void onExit() {
    	super.onExit();
		//Log.e(TAG, "onDestroy");
		if(apMode == Constant.MODE_SILKWORM){
			SharedPreferences sp = getSharedPreferences("ap_flag", MODE_PRIVATE);
			sp.edit().putBoolean("isSilkwormFirst", false).commit();
			deletePic(savePicPath);
		}else if (apMode == Constant.MODE_LADYBUG) {
			SharedPreferences sp = getSharedPreferences("ap_flag", MODE_PRIVATE);
			sp.edit().putBoolean("isLadybugFirst", false).commit();
			deletePic(savePicPath);
		}
		
		if(mMediaPlayer != null)
		{
			if(mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
	    	mMediaPlayer.release();
	    	mMediaPlayer = null;
		}
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
    	//Log.e(TAG, "==========onSaveInstanceState===========");
    	super.onSaveInstanceState(outState);
    }
	
	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	//Log.e(TAG, "------onWindowFocusChanged------hasFocus="+hasFocus);
    	if(hasFocus)
    	{
    		mHasFocus = true;
    		SelectCanvasActivity.mIsNeedPauseBkSnd = true;
    		if(!mHasPause) {
                SelectCanvasActivity.resumeBkSnd();
            }
    		if(needStartTimer){
    			onStartTimer();
    		}
    		
    	}
    	else 
    	{
    		mHasFocus = false;
		}
    	super.onWindowFocusChanged(hasFocus);
    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//return super.onKeyUp(keyCode, event);
		//Log.e(TAG, "------onKeyUp------keyCode="+keyCode);
    	if(!mHasFocus)//在还没获得焦点前，不响应按键操作
    	{
    		//Log.e(TAG, "------onKeyUp------mHasFocus="+mHasFocus);
    		return true;
    	}
    	
		switch (keyCode) 
		{
		case KeyEvent.KEYCODE_BACK:
			finish();
			return true;//已经处理了，不再转发

		default:
			break;
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	/*@Override
	protected void onUserLeaveHint() {
		Log.e(TAG, "------onUserLeaveHint------");
		super.onUserLeaveHint();
	}*/
	
	/**
     * viewPager页面改变监听器
     * @author css
     *
     */
    class MyOnPagerChangeListenner implements OnPageChangeListener
    {
    	private boolean isSmall(float positionOffset) {
    		return Math.abs(positionOffset) < 0.0001;
    	}
    	
		@Override
		public void onPageScrollStateChanged(int arg0) {
			
		}

		/**
		 * position 的值根据滑动方向而定，当向左滑动时（例如由第2页到第1页），position的值为当前页减1；
		 * 而当向右滑动时（例如由第1页到第2页），position的值为当前页；
		 */
		@Override
		public void onPageScrolled(int position, float positionOffset, int arg2) {
			//Log.e(TAG, "curPage="+mViewPager.getCurrentItem());
			//Log.e(TAG, "position,positionOffset,arg2="+position+","+positionOffset+","+arg2);
			//mViewPager.getChildAt(1).setAlpha(0.3f);
			float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;
			
			OpusShowViewPagerAdapter pagerAdapter = (OpusShowViewPagerAdapter) mViewPager.getAdapter();
			View leftView = pagerAdapter.findViewFromObject(position);
			View rightView = pagerAdapter.findViewFromObject(position+1);
			if(leftView != null)
			{
				float Rot = 80.0f * positionOffset;
				//leftView.setAlpha(1-effectOffset);
				leftView.setAlpha(1f);//当前页不透明
				leftView.setRotationY(Rot);
			}
			if(rightView != null)
			{
				float Rot = -80.0f * (1-positionOffset);
				rightView.setAlpha(effectOffset);
				rightView.setRotationY(Rot);
			}
		}

		@Override
		public void onPageSelected(int pageIdx) {
			//Log.e(TAG, "onPageSelected pageIdx="+pageIdx);
			mCurPage = pageIdx;
			View curView = ((OpusShowViewPagerAdapter) mViewPager.getAdapter()).findViewFromObject(pageIdx);
			if(curView != null)
			{
				curView.setAlpha(1f);
				curView.setRotationY(0f);
			}
		}
    	
    }
	
	/**
     * 按钮等view的单击监听器
     * @author css
     *
     */
    class MyOnClickListenner implements OnClickListener
    {

		@Override
		public void onClick(View v) {
			if(v == mCloseBtn)
			{
				finish();
				//ActivityManagerUtil.getInstance().exit();//退出整个程序
				overridePendingTransition(0,R.anim.slide_out_up);
			}
			else if(v == mLastOpusBtn)
			{
				//int curPage,lastPage;
				//curPage = mViewPager.getCurrentItem();
				//Log.e(TAG, "curPage="+mCurPage);
				if(mOpusFilesPath.size() == 0){
					return;
				}
				if(mCurPage <= 0){
					ToastUtils.show(OpusShowActivity.this, "已经是第一页了！", Toast.LENGTH_LONG);
					return;
				}
				//Log.e(TAG, "mCurPage="+mCurPage);
				setCurrentPage(mCurPage - 1,true);
			}
			else if(v == mNextOpusBtn)
			{
				//int curPage,nextPage;
				//curPage = mViewPager.getCurrentItem();
				//Log.e(TAG, "mCurPage="+mCurPage);
				if(mOpusFilesPath.size() == 0){
					return;
				}
				if(mCurPage >= mPageCount - 1){
					ToastUtils.show(OpusShowActivity.this, "已经是最后一页了！", Toast.LENGTH_LONG);
					return;
				}
				//Log.e(TAG, "mCurPage="+mCurPage);
				setCurrentPage(mCurPage + 1,true);
			}
			
		}
    	
    }
    
    /**
     * 按钮等view的单击监听器
     * @author css
     *
     */
    class MyOnTouchListener implements OnTouchListener
    {

		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{
			switch (event.getAction()) 
			{
			case MotionEvent.ACTION_DOWN:
				if(mLastOpusBtn.getVisibility() == View.INVISIBLE || mNextOpusBtn.getVisibility() == View.INVISIBLE)
				{
					if(mCurPage != 0) {
                        mLastOpusBtn.setVisibility(View.VISIBLE);
                    }
					if(mCurPage != mPageCount-1) {
                        mNextOpusBtn.setVisibility(View.VISIBLE);
                    }
				}
				
				break;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				/*if(mLastOpusBtn.getVisibility() == View.VISIBLE || mNextOpusBtn.getVisibility() == View.VISIBLE)
				{
					mLastOpusBtn.setVisibility(View.INVISIBLE);
					mNextOpusBtn.setVisibility(View.INVISIBLE);
				}*/
				
				break;
			}
			
			return false;
		}
    	
    }
}
