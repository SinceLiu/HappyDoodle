package com.readboy.Q.HappyDoodle.opusSet;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog;
import com.readboy.Q.HappyDoodle.doodle.DoodleActivity;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.DialogStyle;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.OnBtnClickCallback;
import com.readboy.Q.HappyDoodle.util.ActivityManagerUtil;
import com.readboy.Q.HappyDoodle.util.BaseActivity;
import com.readboy.Q.HappyDoodle.util.ImageResizer;
import com.readboy.Q.HappyDoodle.util.Utils;
import com.readboy.reward.Reward;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
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
import android.widget.Toast;

public class OpusSetActivity extends BaseActivity {
	private static final String TAG = "lqn-OpusSetActivity";
	/** 图片缓存目录 */
	private static final String IMAGE_CACHE_DIR = "happyDoodlethumbs";
	/** 根布局，用于拦截touch消息 */
	private static MyLayout myMainLayout;
	/** 关闭按钮 */
	private Button mCloseBtn;
	/** 删除按钮 */
	private Button mDelOpusBtn;
	/** viewpager控件 */
	private ViewPager mViewPager;
	/** 页码指示器 */
	private static RadioGroup mRadioGroup;
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 所有作品的路径 */
	private static ArrayList<String> mOpusFilesPath;
	/** 异步加载图片类对象 */
	private ImageResizer mImageWorker;
	/** 播放声音对象 */
	private MediaPlayer mMediaPlayer;
	/** 声音是否被打断，比如按home键或进入其它界面了，用于返回该界面时恢复播放 */
	private boolean mIsSndPause;
	/** 是否有焦点 */
	private boolean mHasFocus;
	/** activity是否已暂停 */
	private boolean mHasPause;
	
	/** 图片显示选项 */
	private static DisplayImageOptions options;
	
	private CustomDialog customDialog;
	private OpusViewPagerAdapter opusViewPagerAdapter;
	
	@Override
	public boolean onInit() {
		setContentView(R.layout.opus_set);
		ActivityManagerUtil.getInstance().addActivity(this);//将该activity加入activity管理类中
		
		myMainLayout = (MyLayout) findViewById(R.id.myMainLayout);
		MyOnClickListenner onClickListenner = new MyOnClickListenner();
		mCloseBtn = (Button) findViewById(R.id.closeBtn);
		mDelOpusBtn = (Button) findViewById(R.id.delOpusBtn);
		mViewPager = (ViewPager) findViewById(R.id.viewpager);
		mRadioGroup = (RadioGroup) findViewById(R.id.page_indicator);
		
		mCloseBtn.setOnClickListener(onClickListenner);
		mDelOpusBtn.setOnClickListener(onClickListenner);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				//Log.e(TAG, "checkId="+checkedId);
				if (group != null) {
					setCurrentPage(checkedId);
				}
			}
		});
		
		if(!initViews())
			return false;
		
		initImageOptions();
		
		opusViewPagerAdapter = new OpusViewPagerAdapter(this, mViewPager, mPageCount, mOpusFilesPath.size(),mImageWorker);
		mViewPager.setAdapter(opusViewPagerAdapter);
		mViewPager.setOnPageChangeListener(new MyOnPagerChangeListenner());
		
		if(mOpusFilesPath.size() == 0)
		{
			mMediaPlayer = DataManager.playSound("no_opus_hint.ogg", false, new MyOnCompleteListenner(0));
		}
		else 
		{
			mMediaPlayer = DataManager.playSound("delete_opus_hint.ogg", false, new MyOnCompleteListenner(0));
		}
		return true;
	}
	
	private boolean initViews()
	{
		String saveDir = "";
		//如果手机插入了SD卡，而且应用程序具有访问SD的权限
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			File sdCardDir = Environment.getExternalStorageDirectory();
			try 
			{
				saveDir = sdCardDir.getCanonicalPath()+Constant.SAVE_PATH;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			//if(Utils.getUsableSpace(sdCardDir)<Constant.MIN_DISK_SPACE)
			{
				Log.e(TAG, "DISK SPACE not enough");
				//空间不足时需清空缓存，不然将不能解析图片
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().clearDiscCache();
			}
		}
		else
		{
			if(HappyDoodleApp.DEBUG)
				Log.e(TAG, "sdcard error");
			Toast toast = Toast.makeText(this, "SD卡不可用，请确保已经正确关闭了usb存储设备", -1);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		
		mOpusFilesPath = getFiles(saveDir);
		if(mOpusFilesPath == null)
			return false;
		if(mOpusFilesPath.size() == 0)
		{
			//Log.i(TAG, "no opus now");
			Toast toast = Toast.makeText(this, "小朋友，作品集中还没有作品哦！", -1);
			toast.setDuration(Toast.LENGTH_LONG);
			toast.show();
		}
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
		return true;
	}
	
	private void initImageOptions()
	{
		//磁盘空间不足时不使用硬盘缓存
		if(Utils.getUsableSpace(Environment.getExternalStorageDirectory())<Constant.MIN_DISK_SPACE)
		{
			options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_stub_2)
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
			.showStubImage(R.drawable.ic_stub_2)
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
	 * 计算有多少页
	 * @return 页数
	 */
	public static int calculatePageCount()
	{
		int pageCount,files;
		if(mOpusFilesPath == null)
			return 0;
		files = mOpusFilesPath.size();
		//最多显示120个
		if(files > Constant.MAX_OPUS)
			files = 120;
		pageCount = files/Constant.MAX_ITEM_PER_CANVAS;
		if(files%Constant.MAX_ITEM_PER_CANVAS != 0)
			pageCount++;
		//Log.e(TAG, "files="+files+",pagecount="+pageCount);
		return pageCount;
	}
	
	/**
	 * 设置选中的单选按钮
	 * @param checkedId 被选中的单选按钮索引
	 */
	public static void setCheckedRadioButton(int checkedId)
	{
		//Log.e(TAG, "CheckedRadioButtonId="+mRadioGroup.getCheckedRadioButtonId());
		if(mRadioGroup.getChildCount() >= 1)
		{
			RadioButton radioButton = (RadioButton) mRadioGroup.getChildAt(checkedId);
			if(!radioButton.isChecked())
				radioButton.setChecked(true);
		}
	}
	
	/**
	 * 设置当前显示页
	 * @param pageIdx 当前显示页索引
	 */
	private void setCurrentPage(int pageIdx)
	{
		mViewPager.setCurrentItem(pageIdx,true);
		setCheckedRadioButton(pageIdx);
	}
	
	/**
	 * 获取指定文件夹下的某种类型的文件名
	 */
    private ArrayList<String> getFiles(String path) {
		ArrayList<String> listImageFiles = new ArrayList<String>();
		File files = null;
		try 
		{
			files = new File(path);
			String[] strFiles = files.list(new ImageFileFilter());
			if (strFiles.length > 0) {
				listImageFiles = new ArrayList<String>();
				for (int i = 0; i < strFiles.length && i < Constant.MAX_OPUS; i++) {
					String strFile = strFiles[i];
					if (strFile.length() > 0) {
						listImageFiles.add(path + strFile);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		files = null;
		return listImageFiles;
	}
    
    public void setmIsSndPause(boolean mIsSndPause) {
		this.mIsSndPause = mIsSndPause;
	}
    
    public MediaPlayer getmMediaPlayer() {
		return mMediaPlayer;
	}
    
    public ViewPager getViewPager() {
		return mViewPager;
	}
    
    public static ArrayList<String> getOpusFilesPath() {
		return mOpusFilesPath;
	}
    
    public static DisplayImageOptions getOptions() {
		return options;
	}
    
    public static RadioGroup getRadioGroup() {
		return mRadioGroup;
	}
    
    public static MyLayout getMyMainLayout() {
		return myMainLayout;
	}
    
    public boolean getHasPause() {
		return mHasPause;
	}
    
    /**
	 * 显示对话框
	 */
	public void showCustomDialog(){
		if(customDialog == null){
			customDialog = CustomDialog.CreateDialog(this);
		}
		customDialog.show();
	}
	
	/**
	 * 关掉对话框
	 */
	public void dismissDialog() {
        if (customDialog != null) {
        	customDialog.dismiss();
        	customDialog = null;
        }
    }
    @Override
    protected void onContinue() {
    	super.onContinue();
        //Log.i(TAG, "------onResume------");
        mHasPause = false;
        if(opusViewPagerAdapter != null){
        	opusViewPagerAdapter.initDialog();
        }
        SelectCanvasActivity.mIsNeedPauseBkSnd = true;
        if(mHasFocus)
        {
	        if(mMediaPlayer != null && mIsSndPause)
	    	{
	    		mMediaPlayer.start();
	        	mIsSndPause = false;
	    	}
        
	        
	    	SelectCanvasActivity.resumeBkSnd();
	        //mImageWorker.setExitTasksEarly(false);
	        PagerAdapter adapter = mViewPager.getAdapter();
	        if(adapter != null)
	        	adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onSuspend() {
    	super.onSuspend();
    	//Log.i(TAG, "------onPause------");
        mHasPause = true;
        //mImageWorker.setExitTasksEarly(true);
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying())
    	{
    		mMediaPlayer.pause();
    		mIsSndPause = true;
    	}
    	else
    		mIsSndPause = false;
        
        SelectCanvasActivity.pauseBkSnd();
    }
    
    @Override
    public void onExit() {
    	super.onExit();
    	//Log.i(TAG, "------onDestroy------");
    	if(mMediaPlayer!=null)
		{
			if(mMediaPlayer.isPlaying())
	    		mMediaPlayer.stop();
	    	mMediaPlayer.release();
		}
    	
    	dismissDialog();//防止泄露对话框窗口
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
    		
    		if(mMediaPlayer != null && mIsSndPause)
	    	{
	    		mMediaPlayer.start();
	        	mIsSndPause = false;
	    	}
        
	        SelectCanvasActivity.mIsNeedPauseBkSnd = true;
	        if(!mHasPause)
	        	SelectCanvasActivity.resumeBkSnd();
	        //mImageWorker.setExitTasksEarly(false);
	        PagerAdapter adapter = mViewPager.getAdapter();
	        if(adapter != null)
	        	adapter.notifyDataSetChanged();
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
			
			OpusViewPagerAdapter pagerAdapter = (OpusViewPagerAdapter) mViewPager.getAdapter();
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
			//Log.e(TAG, "onPageSelected pageIdx="+pageIdx);
			setCheckedRadioButton(pageIdx);
			View curView = ((OpusViewPagerAdapter) mViewPager.getAdapter()).findViewFromObject(pageIdx);
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
			if(v == mCloseBtn)
			{
				//SelectCanvasActivity.mIsNeedPauseBkSnd = false;
				finish();
				//ActivityManagerUtil.getInstance().exit();//退出整个程序
				overridePendingTransition(0,R.anim.slide_out_up);
			}
			else if(v == mDelOpusBtn)
			{
				
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
					mIsSndPause = false;
					break;

				default:
					break;
				}
			}
			
		}
    	
    }
    
    /**
     * 过滤文件后缀
     * 
     * @author Administrator
     * 
     */
    class ImageFileFilter implements FilenameFilter {

    	@Override
    	public boolean accept(File dir, String name) {
    		boolean isImage = false;
    		if (name.indexOf(".png") != -1 && !name.contains(Constant.SILKWORM_NAME)
    				&& !name.contains(Constant.LADYBUG_NAME)) {
    			isImage = true;
    		}
    		return isImage;
    	}
    }
}
