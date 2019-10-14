package com.readboy.Q.HappyDoodle;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.loveplusplus.update.UpdateChecker;
import com.readboy.Q.HappyDoodle.GuideBK.GuideBKCallback;
import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.doodle.DoodleActivity;
import com.readboy.Q.HappyDoodle.util.ActivityManagerUtil;
import com.readboy.Q.HappyDoodle.util.BaseActivity;
import com.readboy.Q.HappyDoodle.util.HostInfo;


/**
 * 引导界面，由两个动画组成，提示语播完后直接跳到选择界面。
 * @author lqn
 * @version 1.1
 */
public class HappyDoodleActivity extends BaseActivity {
	private static final String TAG = "lqn-HappyDoodleActivity";
	private int apMode;		//ap模式
	/** 用于GuideBK的timer中通知播放声音 */
	public static final int MSG_PLAY_ENTERSELECTACT_SOUND = 0x1000;
	/** 背景动画 */
	private static GuideBK guideBK;
	/** 标题动画 */
	private static TitleView mTitleView;
	/** 资源管理对象 */
	private DataManager dataManager;
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	
	/** 播放声音对象 */
	private MediaPlayer mMediaPlayer;
	/** 声音是否被打断，比如按home键或进入其它界面了，用于返回该界面时恢复播放 */
	private boolean mIsSndPause;
	/** 是否有焦点 */
	private boolean mHasFocus;
	/** 是否暂停了，有这种情况，在launcher中点击图标后马上按home键，没进入app，但surfaceview会走surfaceCreated>>surfaceDestroyed>>surfaceCreated,
	 *	所以会导致surfaceview锁不到
	 */
	private boolean mHasPause;
	private Handler mMainHandler;
	
	/** 是否要进入下一级界面的消息，之所以要此标志，是因为如果在声音刚好播放完但还没来onCompletion消息时，按下了home键等，
	 *  onCompletion还是会来
	 */
	private boolean isNeedEnterNextAct;
	PowerManager pm;
	
	/**宝贝计划调用时，会传进来两张本地图片的路径*/
	private String babyPicPath0;
	private String babyPicPath1;
	
	private long lastClickTime = 0;
	//AnimationDrawable bkAnim;
    /** Called when the activity is first created. */
    @Override
    public boolean onInit() {
//    	Log.e(TAG, "000000--------------onInit--"+getTaskId()+"------activity = "+this);
        super.onInit();

//        autoUpdateInit();		//这个ap不用自动检测更新了，所以注释掉。
        //初始化资源管理对象，注意这里传的参数是应用程序的上下文
        dataManager = ((HappyDoodleApp)getApplication()).getDataManager();//DataManager.getDataManager(getApplicationContext());
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        
        if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onCreate-----------isScreenOn="+pm.isScreenOn());
		}
        
        Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		mAge = 0;
		if(bundle != null){
			/*int age = bundle.getInt("Age", 3);
			if(age == 5 || age == 6){
				mAge = 1;
			}else if (age == 7) {
				mAge = 1;
			}else {
				mAge = 0;
			}
			Log.i(TAG, "-----mage = "+age+"------mAge = "+mAge);*/
			apMode = bundle.getInt("apMode", 0);
			if(apMode == Constant.MODE_BABYPLAN){
				babyPicPath0 = bundle.getString("babyPicPath0", null);
				babyPicPath1 = bundle.getString("babyPicPath1", null);
			}
		}
		babyPicPath0 = "mnt/sdcard/baby0.png";
		babyPicPath1 = "mnt/sdcard/baby1.png";
		apMode = Constant.MODE_NORMAL;
		
        
        setContentView(R.layout.main);
        ActivityManagerUtil.getInstance().addActivity(this);//将该activity加入activity管理类中
        
        guideBK = (GuideBK) findViewById(R.id.bk);
        mTitleView = (TitleView) findViewById(R.id.title);
        mTitleView.init(this, apMode);
        guideBK.setCallback(new GuideBKCallback() {
			@Override
			public void nextScene() {
				long clickTime = System.currentTimeMillis();
				if(clickTime - lastClickTime > Constant.CLICKTIME_GAP){
					enterSelectCanvasAct();
				}
				lastClickTime = clickTime;
			}
		});
        
        Button closeBtn = (Button) findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				/*ContentValues values = new ContentValues();
				values.put("key", "antisystem_off");
				values.put("value", "0");
				Uri uri = Uri.parse("content://com.readboy.Q.share/antiaddiction");
				getContentResolver().insert(uri, values);
				
				
				values.clear();
				values.put("key", "useperiod");
				values.put("value", "3");
				uri = Uri.parse("content://com.readboy.Q.share/antiaddiction");
				getContentResolver().insert(uri, values);
				
				values.clear();
				values.put("key", "studyperiod");
				values.put("value", "2");
				uri = Uri.parse("content://com.readboy.Q.share/antiaddiction");
				getContentResolver().insert(uri, values);*/
				

			}
		});
        //printTask();
        mMainHandler = new MyHandler();
        return true;
    }



    private void autoUpdateInit(){
    	//设置下载完成后，弹出安装提示，默认false 
		UpdateChecker.setPopInstallActivity(true);   
		//设置检测间隔时间，以小时为单位，默认 0，表示次次都检测 
		UpdateChecker.setCheckTimeDistance(24); 
		//指定机型类型，默认程序自动读取机型 
		UpdateChecker.setCheckUrl(HostInfo.getHostQ5());
		//检测 
		UpdateChecker.checkForNotification(this);
	}
    
    public void printTask()
    {
    	Log.e(TAG, "taskId="+getTaskId());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	//Log.e(TAG, "onNewIntent ="+intent.getPackage());
    }
    
    @Override
    protected void onContinue() {
    	super.onContinue();
    	if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onResume mMediaPlayer="+mMediaPlayer+",mIsSndPause="+mIsSndPause+",mHasFocus="+mHasFocus);
		}
    	mHasPause = false;
		if (mHasFocus) {
			if (mMediaPlayer != null && mIsSndPause) {
				mMediaPlayer.start();
				mIsSndPause = false;
			}

			// 注：弹出眼保健操界面时，只会来onPause，不会来onStop，眼保健操结束后，会来onResume
			// if(mTitleView.getIsRunning())//4.2去掉的
			mTitleView.beginAnim();
			// if(guideBK.getIsRunning())
			guideBK.beginAnim();

			if (isNeedEnterNextAct) {
				enterSelectCanvasAct();
			}
		}
    }
    
    @Override
    public void onReinit() {
    	super.onReinit();
    	//Log.e(TAG, "------onRestart------");
    }
    
    @Override
    protected void onSuspend() {
    	super.onSuspend();
    	if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onPause------");
		}
    	mHasPause = true;
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mIsSndPause = true;
			// Log.e(TAG, "------onPause mMediaPlayer="+mMediaPlayer);
		} else {
			mIsSndPause = false;
		}
    	
    	//注：弹出眼保健操界面时，只会来onPause，不会来onStop，眼保健操结束后，会来onResume
    	if(mTitleView.getIsRunning()) {
			mTitleView.stopAnim();
		}
    	if(guideBK.getIsRunning()) {
			guideBK.stopAnim();
		}
    }
    
    @Override
    public void onHalt() {
    	super.onHalt();
    	if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onStop------");
		}
    	if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}
    }
    
    @Override
    public void onExit() {
    	super.onExit();
    	if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onDestroy------");
		}
    	if(mMediaPlayer != null)
    	{
	    	if(mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
	    	mMediaPlayer.release();
    	}
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	if(HappyDoodleApp.DEBUG) {
			Log.e(TAG, "------onWindowFocusChanged------hasFocus="+hasFocus);
		}
		if (hasFocus) {
			mHasFocus = true;
			{
				if (mMediaPlayer != null && mIsSndPause) {
					mMediaPlayer.start();
					mIsSndPause = false;
				}

				// 注：弹出眼保健操界面时，只会来onPause，不会来onStop，眼保健操结束后，会来onResume
				if (mTitleView.getIsRunning()) {
					mTitleView.beginAnim();
				}
				if (guideBK.getIsRunning()) {
					guideBK.beginAnim();
				}

				if (isNeedEnterNextAct) {
					enterSelectCanvasAct();
				}
			}
		} else {
			mHasFocus = false;
		}
    	super.onWindowFocusChanged(hasFocus);
    }
    
    public int getAge() {
		return mAge;
	}
    
	public static GuideBK getGuideBK() {
		return guideBK;
	}
    
    public boolean getHasPause() {
    	return mHasPause;
	}
    
    public Handler getMainHandler() {
		return mMainHandler;
	}

	/**
     * 播放进入时的提示语，为了保证显示出界面后再播声音，由GuideBK中调用
     */
    public void playEnterSound() {
    	
    	if(mMediaPlayer == null)//避免GuideBK中多次调用
		{
			mMediaPlayer = DataManager.playSound("nh_057.ogg", false, new MyOnCompleteListenner(1));
		}
	}
    
    /**
     * 播放进入选择界面的提示语，由GuideBK播放完其动画后调用
     */
    public void playEnterSelectActSound() {
    	
    	if(mMediaPlayer != null)
		{
			if(mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
	    	mMediaPlayer.release();
		}
    	
    	mMediaPlayer = DataManager.playSound("nh_058.ogg", false, new MyOnCompleteListenner(2));
    	//Log.e(TAG, "------playEnterSelectActSound mMediaPlayer="+mMediaPlayer);
	}
    
    private void enterSelectCanvasAct()
    {
    	Intent intent = new Intent();
    	intent.putExtra("Age", mAge);
    	intent.putExtra("apMode", apMode);
    	if(apMode == Constant.MODE_NORMAL){
    		intent.setClass(HappyDoodleActivity.this,SelectCanvasActivity.class);
    	}else {
    		intent.setClass(HappyDoodleActivity.this,DoodleActivity.class);
    		intent.putExtra("CANVAS_INDEX", 2);
    		if(apMode == Constant.MODE_BABYPLAN){
    			intent.putExtra("babyPicPath0", babyPicPath0);
    			intent.putExtra("babyPicPath1", babyPicPath1);
    		}
		}
    	//Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK必须结合起来使用才能关掉历史task
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
    	launchForResult(intent, -1);
		HappyDoodleActivity.this.finish();
		overridePendingTransition(android.R.anim.slide_in_left,
				android.R.anim.slide_out_right);
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
			ActivityManagerUtil.getInstance().exit();//退出整个程序
			return true;//已经处理了，不再转发

		default:
			break;
		}
		
		return super.onKeyUp(keyCode, event);
	}

	class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what)
			{
				case MSG_PLAY_ENTERSELECTACT_SOUND:
					playEnterSelectActSound();
					break;
				
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
    	/** 声音标识，1代表“EnterSound”，2代表“EnterSelectActSound” */
    	private int flag;
    	public MyOnCompleteListenner(int flag) {
    		this.flag = flag;
		}
		@Override
		public void onCompletion(MediaPlayer mp) 
		{
			if(mp == mMediaPlayer)
			{
				//Log.e(TAG, "------mMediaPlayer onCompletion------flag="+flag+",mHasPause="+mHasPause);
				mIsSndPause = false;
				if(flag == 2)
				{
					if(pm.isScreenOn())
					{
						/*Intent intent = new Intent(HappyDoodleActivity.this,SelectCanvasActivity.class);
						intent.putExtra("Age", mAge);
						//HappyDoodleActivity.this.finish();
						//Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK必须结合起来使用才能关掉历史task
						//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
						launchForResult(intent, -1);
						HappyDoodleActivity.this.finish();
						overridePendingTransition(android.R.anim.slide_in_left,
								android.R.anim.slide_out_right);*/
						enterSelectCanvasAct();
					}
					else 
					{
						isNeedEnterNextAct = true;
					}
				}
			}
		}
	}
}