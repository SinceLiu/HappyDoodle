package com.readboy.Q.HappyDoodle.SelectCanvas;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.doodle.DoodleActivity;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;
import com.readboy.Q.HappyDoodle.util.ActivityManagerUtil;
import com.readboy.Q.HappyDoodle.util.BaseActivity;
import com.readboy.Q.HappyDoodle.util.UsbActionReceiver;
import com.readboy.Q.HappyDoodle.util.Utils;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SelectCanvasActivity extends BaseActivity implements PermissionListener {
	private static final String TAG = "lqn-SelectCanvasActivity";
	private int apMode;		//ap模式
	/** 需要保存的数据，以备系统重建activity时恢复状态，注：启动当前activity的intent里的数据并不需要保存，因为
	 *	系统会保存该intent，即重建时得到的intent是被杀掉前的intent
	 */
	private static final String SAVE_KEY_AGE = "Age";
	/** 需要保存的数据，以备系统重建activity时恢复状态 */
	private static final String SAVE_KEY_CURPAGE = "CurPage";
	private static final String SAVE_KEY_APMODE = "apMode";
	/** 数据库的Uri */
	public static final Uri dbUri = Uri.parse("content://com.readboy.Q.share/userdata");
	/** 根布局，用于拦截touch消息 */
	private MyLayout myMainLayout;
	/** 关闭按钮 */
	private Button mCloseBtn;
	/** 作品集按钮 */
	private Button mOpusBtn;
	/** viewpager控件 */
	private ViewPager mViewPager;
	/** 页码指示器 */
	private RadioGroup mRadioGroup;
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 当前icon索引 */
	private int mIconIndex;
	/** 当前页 */
	private int mCurPage;
	
	/** 播放声音时禁止再有操作 */
	public static boolean mIsForbidOp;
	/** 背景声音的播放对象 */
	private static MediaPlayer mMpBk;
	/** 背景声音是否被打断，比如按home键或弹出了休息界面（注：背景声音为全局播放的
	 * 所以由本app弹出的其他activity不打断此声音），用于返回该界面时恢复播放 */
	private static boolean mIsBkSndPause = true;
	/** 背景声音是否该停掉，进入本应用的下级activity不停止，按了home键或弹出休息界面时停止 */
	public static boolean mIsNeedPauseBkSnd = true;
	/** 播放声音对象 */
	private MediaPlayer mMediaPlayer;
	/** 声音是否被打断，比如按home键或进入其它界面了，用于返回该界面时恢复播放 */
	private static boolean mIsSndPause;
	/** 是否有焦点 */
	private boolean mHasFocus;
	/** activity是否已暂停 */
	private boolean mHasPause;
	
	/** 图片显示选项 */
	private static DisplayImageOptions options;
	private UsbActionReceiver mUsbActionReceiver;
	
	/** 是否要进入下一级界面的消息，之所以要此标志，是因为如果在声音刚好播放完但还没来onCompletion消息时，按下了home键等，
	 *  onCompletion还是会来
	 */
	private boolean isNeedEnterNextAct;
	PowerManager pm;
	
	
	@Override
	public boolean onInit() {
		Bundle savedInstanceState = getSavedInstanceState();
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "onCreate savedInstanceState="+savedInstanceState);
		setContentView(R.layout.select_canvas);
		ActivityManagerUtil.getInstance().addActivity(this);//将该activity加入activity管理类中
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		mIsSndPause = false;
		myMainLayout = (MyLayout) findViewById(R.id.myMainLayout);
		
		if(null != savedInstanceState)
		{
			mAge = savedInstanceState.getInt(SAVE_KEY_AGE,0);
			mCurPage = savedInstanceState.getInt(SAVE_KEY_CURPAGE,0);
			apMode = savedInstanceState.getInt(SAVE_KEY_APMODE,Constant.MODE_NORMAL);
		}
		else 
		{
			Intent intent = getIntent();
			mAge = intent.getIntExtra("Age", 0); 
			Log.i(TAG, "-----mage = "+mAge);
			apMode = intent.getIntExtra("apMode", Constant.MODE_NORMAL);
		}
		

		//mAge = getAgeFromDB();
		
		MyOnClickListenner onClickListenner = new MyOnClickListenner();
		mCloseBtn = (Button) findViewById(R.id.closeBtn);
		mOpusBtn = (Button) findViewById(R.id.opusBtn);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mRadioGroup = (RadioGroup) findViewById(R.id.page_indicator);
		
		mCloseBtn.setOnClickListener(onClickListenner);
		mOpusBtn.setOnClickListener(onClickListenner);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//Log.e(TAG, "checkId="+checkedId);
				if (group != null) {
					setCurrentPage(checkedId);
				}
			}
		});
		
		initViews();
		
		initImageOptions();
		
		mViewPager.setAdapter(new ViewPagerAdapter(this, mPageCount, Constant.TOTAL_CANVAS_EACH_AGE[mAge]));
		mViewPager.setOnPageChangeListener(new MyOnPagerChangeListenner());
		
		playBackSound();
		playHintSound();
		//Utils.printTaskInfo(this);
		
		mUsbActionReceiver = new UsbActionReceiver();
		mUsbActionReceiver.registerScreenActionReceiver(this);
		
		
		if(null != savedInstanceState)
		{
			setCurrentPage(mCurPage);
		}

		AndPermission.with(this)
				.requestCode(100)
				.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
				.send();
		return true;
	}
	
	private void initViews()
	{
		mPageCount = calculatePageCount();
		mRadioGroup.removeAllViews();
		for(int i=0;i<mPageCount;i++)
		{
			RadioButton rb = new RadioButton(this);
			rb.setId(i);//注意:很重要，此id要对应于页码的页数
			rb.setButtonDrawable(android.R.color.transparent);//去掉默认的背景
			rb.setBackgroundResource(R.drawable.radio_btn_checked);
			mRadioGroup.addView(rb);
		}
		setCheckedRadioButton(0);
	}
	
	private void initImageOptions()
	{
		//磁盘空间不足时不使用硬盘缓存
		if(Utils.getUsableSpace(Environment.getExternalStorageDirectory())<Constant.MIN_DISK_SPACE)
		{
			options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_stub_1)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			.cacheInMemory(true)
			//.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
		}
		else 
		{
			options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_stub_1)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.resetViewBeforeLoading(true)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();
		}
	}
	
	/**
	 * 从全局数据库中获取年龄
	 * @return 年龄
	 */
	private int getAgeFromDB()
	{
		int age;
		age = getDataFromDb("ChildAge",3);
		if(age <= 3)
			age = 0;
		else if(age <= 5)
			age = 1;
		else if(age <= 7)
			age = 2;
		return age;
	}
	
	/**
	 * 从数据库获取key对应的数据
	 * @param key 关键字
	 * @param defValue 默认值
	 * @return 年龄
	 */
	private int getDataFromDb(String key, int defValue){
		int value = 0;
		Cursor cursor = getContentResolver().query(dbUri, new String[] { "key", "value" },
				"key" + "=?", new String[] {key}, null);
		if (cursor != null && cursor.moveToFirst()) {
			value = cursor.getInt(cursor.getColumnIndexOrThrow("value"));
			Log.i(TAG, key + "-----------value="+value);  
		} else {  
		    Log.i(TAG, key + "----------query failure!");  
		    value = defValue;
		} 
		if(cursor != null){
			cursor.close();
		}
		
		return value;
	}
	
	private int calculatePageCount()
	{
		int pageCount;
		pageCount = Constant.TOTAL_CANVAS_EACH_AGE[mAge]/Constant.MAX_ITEM_PER_CANVAS;
		if(Constant.TOTAL_CANVAS_EACH_AGE[mAge]%Constant.MAX_ITEM_PER_CANVAS != 0)
			pageCount++;
		return pageCount;
	}
	
	private void setCheckedRadioButton(int checkedId)
	{
		//Log.e(TAG, "CheckedRadioButtonId="+mRadioGroup.getCheckedRadioButtonId());
		if(mRadioGroup.getChildCount() > 1)
		{
			RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(checkedId);
			if(!radioButton.isChecked())
				radioButton.setChecked(true);
		}
	}
	
	private void setCurrentPage(int pageIdx)
	{
		mViewPager.setCurrentItem(pageIdx,true);
		setCheckedRadioButton(pageIdx);
		mCurPage = pageIdx;
	}
	
	public static DisplayImageOptions getOptions() {
		return options;
	}
	
	public static void setIsSndPause(boolean mIsSndPause) {
		SelectCanvasActivity.mIsSndPause = mIsSndPause;
	}
	
	public static void playBackSound() 
	{
		mMpBk = DataManager.playSound("bk.ogg", true, null);
		mIsNeedPauseBkSnd = true;
	}
	
	public void playHintSound() 
	{
		mMediaPlayer = DataManager.playSound("nh_059.ogg", false, null);
	}
	
	public void playIconSoundByIndex(int index) 
	{
		if(mMediaPlayer!=null)
		{
			if(mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.reset();
	    	mMediaPlayer.release();
		}
		
		mIconIndex = index;
		String path = "icon/"+ ((mAge == 0) ? "0/":"1/") + index+".ogg";
		mMediaPlayer = DataManager.playSound(path, false, new MyOnCompleteListenner(2));
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "------playIconSoundByIndex------mIsSndPause="+mIsSndPause+",mHasPause="+mHasPause);
		if(mIsSndPause || !mHasFocus || mHasPause)
		{
			mMediaPlayer.pause();
			mIsSndPause = true;
		}
		mIsForbidOp = true;
	}
	
	public int getAge() {
		return mAge;
	}
	
	public ViewPager getViewPager() {
		return mViewPager;
	}
	
	public MyLayout getMyMainLayout() {
		return myMainLayout;
	}
	
	public static void resumeBkSnd()
	{
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "======resumeBkSnd mIsBkSndPause="+mIsBkSndPause);
		if(mMpBk == null)
			playBackSound();
		
		if(mIsBkSndPause)
    	{
    		mMpBk.start();
    		mIsBkSndPause = false;
    	}
	}
	
	public static void pauseBkSnd() 
	{
		if(mMpBk != null)
		{
			if(mIsNeedPauseBkSnd && mMpBk.isPlaying())
	    	{
	    		mMpBk.pause();
	    		mIsBkSndPause = true;
	    	}
	    	/*else
	    		mIsBkSndPause = false;*/
		}
	}
	
	
	/**
     * 从其他activity返回时会来
     */
    @Override
    protected void onContinue() {
    	super.onContinue();
    	if(HappyDoodleApp.DEBUG)
    		Log.i(TAG, "------onResume------mHasFocus="+mHasFocus);
    	mHasPause = false;
    	if(mHasFocus)
    	{
	    	mIsNeedPauseBkSnd = true;
	    	if(mIsSndPause)
	    	{
	    		mMediaPlayer.start();
	        	mIsSndPause = false;
	    	}
	    	
	    	resumeBkSnd();
	    	
	    	if(isNeedEnterNextAct)
	    		enterOpusSetAct();
    	}
    }
    
    /**
     * 按了home键后重新进入或先锁屏然后解锁时会来
     */
    @Override
    public void onReinit() {
    	super.onReinit();
    	if(HappyDoodleApp.DEBUG)
    		Log.i(TAG, "------onRestart------");
    	//bkAnim.start();
    	//launchBK.beginAnim();
    	/*if(mIsSndPause)
    	{
    		mMediaPlayer.start();
        	mIsSndPause = false;
    	}*/
    }
    
    /**
     * 切换界面时会来
     */
    @Override
    protected void onSuspend() {
    	super.onSuspend();
    	if(HappyDoodleApp.DEBUG)
    		Log.i(TAG, "------onPause------mIsNeedPauseBkSnd="+mIsNeedPauseBkSnd);
    	mHasPause = true;
    	if(mMediaPlayer!=null && mMediaPlayer.isPlaying())
    	{
    		mMediaPlayer.pause();
    		mIsSndPause = true;
    	}
    	else
    		mIsSndPause = false;
    	
    	if(mIsNeedPauseBkSnd && mMpBk.isPlaying())
    	{
    		mMpBk.pause();
    		mIsBkSndPause = true;
    	}
    	/*else
    		mIsBkSndPause = false;*/
    }
    
    /**
     * 按了home键后或锁屏或关闭该activity时会来
     */
    @Override
    public void onHalt() {
    	super.onHalt();
    	if(HappyDoodleApp.DEBUG)
    		Log.i(TAG, "------onStop------");
    	//bkAnim.stop();
    	if(mMediaPlayer!=null && mMediaPlayer.isPlaying())
    	{
    		mMediaPlayer.pause();
    		mIsSndPause = true;
    	}
    	
    }
    
    private void releaseRes() 
    {
    	if(HappyDoodleApp.DEBUG)
    		Log.i(TAG, "------releaseRes------");
    	if(mMediaPlayer!=null)
		{
			if(mMediaPlayer.isPlaying())
	    		mMediaPlayer.stop();
	    	mMediaPlayer.release();
	    	mMediaPlayer = null;
		}
    	
    	if(mMpBk != null)
    	{
	    	if(mMpBk.isPlaying())
	    		mMpBk.stop();
	    	mMpBk.release();
	    	mMpBk = null;
    	}
    	
    	if(mUsbActionReceiver != null)
    	{
    		mUsbActionReceiver.unRegisterScreenActionReceiver(this);
    		mUsbActionReceiver = null;
    	}
    	
    	//android.os.Process.sendSignal(android.os.Process.myPid(), android.os.Process.SIGNAL_KILL);
    	ActivityManagerUtil.getInstance().exit();//退出整个程序
	}
    
    @Override
    public void onExit() {
    	super.onExit();
		if(HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onDestroy------");
		releaseRes();
		/*if(mMediaPlayer!=null)
		{
			if(mMediaPlayer.isPlaying())
	    		mMediaPlayer.stop();
	    	mMediaPlayer.release();
		}
    	
    	if(mMpBk.isPlaying())
    		mMpBk.stop();
    	mMpBk.release();
    	if(mUsbActionReceiver != null)
    		mUsbActionReceiver.unRegisterScreenActionReceiver(this);
    	ActivityManagerUtil.getInstance().exit();//退出整个程序*/
	}
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if(HappyDoodleApp.DEBUG)
    		Log.e(TAG, "==========onSaveInstanceState===========");
    	outState.putInt(SAVE_KEY_AGE, mAge);
    	outState.putInt(SAVE_KEY_CURPAGE, mCurPage);
    	outState.putInt(SAVE_KEY_APMODE, apMode);
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if(HappyDoodleApp.DEBUG)
    		Log.e(TAG, "------onWindowFocusChanged------hasFocus="+hasFocus);
    	if(hasFocus)
    	{
    		myMainLayout.setIsIntercept(false);
    		mHasFocus = true;
    		{
    			mIsNeedPauseBkSnd = true;
    	    	if(mIsSndPause)
    	    	{
    	    		mMediaPlayer.start();
    	        	mIsSndPause = false;
    	    	}
    	    	if(!mHasPause)
    	    		resumeBkSnd();
    	    	
    	    	if(isNeedEnterNextAct)
    	    		enterOpusSetAct();
    		}
    	}
    	else
    	{
    		myMainLayout.setIsIntercept(true);
    		mHasFocus = false;
    	}
    	super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//return super.onKeyUp(keyCode, event);
    	if(HappyDoodleApp.DEBUG)
    		Log.e(TAG, "------onKeyUp------keyCode="+keyCode);
    	if(!mHasFocus || mIsForbidOp)//在还没获得焦点前或不允许有操作了，不响应按键操作
    	{
    		Log.e(TAG, "------onKeyUp------mHasFocus="+mHasFocus+",mIsForbidOp="+mIsForbidOp);
    		return true;
    	}
    	
		switch (keyCode) 
		{
		case KeyEvent.KEYCODE_BACK:
			finish();
			releaseRes();
			return true;//已经处理了，不再转发

		default:
			break;
		}
		
		return super.onKeyUp(keyCode, event);
	}
    
    /*@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	Log.e(TAG, "------dispatchTouchEvent------ev="+ev.getAction());
    	return super.dispatchTouchEvent(ev);
    }*/
    
    private void enterOpusSetAct()
    {
    	Intent intent = new Intent(SelectCanvasActivity.this,OpusSetActivity.class);
		
    	launchForResult(intent, -1);
		overridePendingTransition(R.anim.slide_in_top,
				R.anim.slide_out_bottom);
		isNeedEnterNextAct = false;
    }

	@Override
	public void onSucceed(int requestCode) {

	}

	@Override
	public void onFailed(int requestCode) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// 这个Activity中没有Fragment，这句话可以注释。
		// super.onRequestPermissionsResult(requestCode, permissions, grantResults);

		// 没有Listener，最后的PermissionListener参数不写。
//        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);

		// 有Listener，最后需要写PermissionListener参数。
		AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
	}


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
		 * @position 的值根据滑动方向而定，当向左滑动时（例如由第2页到第1页），position的值为当前页减1；
		 * 而当向右滑动时（例如由第1页到第2页），position的值为当前页；
		 */
		@Override
		public void onPageScrolled(int position, float positionOffset, int arg2) {
			//Log.e(TAG, "curPage="+mViewPager.getCurrentItem());
			//Log.e(TAG, "position,positionOffset,arg2="+position+","+positionOffset+","+arg2);
			//mViewPager.getChildAt(1).setAlpha(0.3f);
			float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;
			
			ViewPagerAdapter pagerAdapter = (ViewPagerAdapter) mViewPager.getAdapter();
			View leftView = pagerAdapter.findViewFromObject(position);
			View rightView = pagerAdapter.findViewFromObject(position+1);
			if(leftView != null)
			{
				float Rot = 30.0f * positionOffset;
				//leftView.setAlpha(1-effectOffset);
				//leftView.setAlpha(1f);//当前页不透明
				leftView.setRotationY(Rot);
			}
			if(rightView != null)
			{
				float Rot = -30.0f * (1-positionOffset);
				//rightView.setAlpha(effectOffset);
				rightView.setRotationY(Rot);
			}
			
			

		}

		@Override
		public void onPageSelected(int pageIdx) {
			setCheckedRadioButton(pageIdx);
			//Log.e(TAG, "onPageSelected curPage="+mViewPager.getCurrentItem());
			View curView = ((ViewPagerAdapter) mViewPager.getAdapter()).findViewFromObject(pageIdx);
			if(curView != null)
			{
				//curView.setAlpha(1f);
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
			if(mIsForbidOp)
			{
				if(HappyDoodleApp.DEBUG)
					Log.e(TAG, "------onClick ForbidOp");
				return ;
			}
			myMainLayout.setIsIntercept(true);
			
			if(v == mCloseBtn)
			{
				/*finish();
				ActivityManagerUtil.getInstance().exit();//退出整个程序
*/				
				if(mMediaPlayer!=null)
				{
					if(mMediaPlayer.isPlaying())
						mMediaPlayer.stop();
					mMediaPlayer.reset();
			    	mMediaPlayer.release();
				}
				mMediaPlayer = DataManager.playSound("nh_066.ogg", false, new MyOnCompleteListenner(0));
				mIsForbidOp = true;
			}
			else if(v == mOpusBtn)
			{
				/*Intent intent = new Intent(SelectCanvasActivity.this,OpusSetActivity.class);
				mIsSndPause = false;
				launchForResult(intent, -1);*/
				
				if(mMediaPlayer!=null)
				{
					if(mMediaPlayer.isPlaying())
						mMediaPlayer.stop();
					mMediaPlayer.reset();
			    	mMediaPlayer.release();
				}
				mMediaPlayer = DataManager.playSound("nh_065.ogg", false, new MyOnCompleteListenner(1));
				mIsForbidOp = true;
			}
		}
    	
    }
    
    /**
     * 声音播放结束监听器
     * @author css
     *
     */
    class MyOnCompleteListenner implements OnCompletionListener
    {
    	/** 0代表退出，1代表作品集，2代表icon声音 */
    	private int flag;
    	
    	/**
    	 * 构造函数
    	 * @param flag 0代表退出，1代表作品集，2代表icon声音
    	 */
    	public MyOnCompleteListenner(int flag) {
    		this.flag = flag;
		}
    	
		@Override
		public void onCompletion(MediaPlayer mp) {
			if(HappyDoodleApp.DEBUG)
				Log.e(TAG, "------onCompletion------flag="+flag);
			if(mp == mMediaPlayer)
			{
				Intent intent;
				//Log.e(TAG, "flag="+flag);
				switch (flag) 
				{
				case 0:
					//ImageLoader.getInstance().clearMemoryCache();
					//ImageLoader.getInstance().clearDiscCache();
					finish();
					releaseRes();
					//ActivityManagerUtil.getInstance().exit();//退出整个程序
					break;
				case 1:
					//mIsNeedPauseBkSnd = false;
					mIsSndPause = false;
					if(pm.isScreenOn())
					{
						enterOpusSetAct();
					}
					else 
					{
						isNeedEnterNextAct = true;
					}
					break;
				case 2:
					if(HappyDoodleApp.DEBUG)
						Log.e(TAG, "------onCompletion------");
					intent = new Intent(SelectCanvasActivity.this,DoodleActivity.class);
					intent.putExtra("AGE", mAge);
					intent.putExtra("CANVAS_INDEX", mIconIndex);
					intent.putExtra("apMode", apMode);
					//mIsNeedPauseBkSnd = false;
					mIsSndPause = false;
					launchForResult(intent, -1);
					overridePendingTransition(R.anim.slide_in_top,
							R.anim.slide_out_bottom);
					break;

				default:
					break;
				}
				mIsForbidOp = false;
			}
			
		}
    	
    }
    
    
	
}
