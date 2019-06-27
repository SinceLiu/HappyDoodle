package com.readboy.Q.HappyDoodle;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ReadboySurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 主界面背景动画
 * @author css
 * @version 1.0
 */
public class GuideBK extends ReadboySurfaceView implements SurfaceHolder.Callback{

	private final static String TAG = "lqn-GuideBK";
	private SurfaceHolder mSHolder;
	private Timer timer = new Timer();
	private TimerTask task = null;
	/** 刷图间隔 */
	private int timeout = 100;
	/** 当前刷到哪一张图片 */
	private int mTimeCount;
	/** 总图片数 */
	private int mTotalPic = Constant.GUIDE_BK_TOTAL_PIC;
	/** 图片目录 */
	private String mPicDir = Constant.GUIDE_BK_TOTAL_PIC_DIR;
	
	/** surfaceView是否已经准备好了 */
	private boolean isSurfaceCreated;
	/** 是否已经开始刷动画了 */
	private boolean isRunning = false;
	/** 是否已经开始刷第一步的动画了 */
	//private boolean isBeginFirstStepAnim;
	/** 是否需要恢复暂停的动画，初始值置为true，因为一开始需要刷一张图片，用于按了home键或进入了其他界面 */
	private boolean mIsNeedResumeAnim = true;
	
	/** 是否已经退出了，用于停掉线程 */
	private boolean isDestroy;
	
	private Context mContext;

	private int mWidth;
//	private int mHeight;
	/** 为了加快动画速度，使用线程解图，把解出来的图全部缓存到内存中 */
	//private ArrayList<SoftReference<Bitmap>> imageCache = new ArrayList<SoftReference<Bitmap>>();
	private ArrayList<Bitmap> imageCache = new ArrayList<Bitmap>();
	
	public GuideBK(Context context) {
		super(context);
		init(context,null);
	}
	
	/**
	 * 将此view放置于xml布局中时必须提供此构造函数
	 * @param context 上下文
	 * @param attrs 自定义属性
	 */
	public GuideBK(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	
	/**
	 * 初始化surfaceView，主要是使其透明
	 */
	private void init(Context context, AttributeSet attrs) {
		Log.i(TAG, "init");
		mContext = context;
		mSHolder = this.getSurface();
		//使背景透明
		//setZOrderOnTop(true);//注：因为是整个背景，所以不用置顶，否则其上面就不能再显示一些常用控件了
		mSHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSHolder.addCallback(this);

		mWidth = HappyDoodleApp.getScreenWidth();
//		Resources resources = this.getResources();
//		DisplayMetrics dm = resources.getDisplayMetrics();
//		float density = dm.density;
//		mWidth = dm.widthPixels;
//		mHeight = dm.heightPixels;
		
		new Thread(new DecodeBitmapThread()).start();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "surfaceCreated");
		isSurfaceCreated = true;
		if (mIsNeedResumeAnim) {
			beginAnim();
		} else { 		// 如果不需要，则表明动画已显示完，画上最后一张图
			drawOnePic(mPicDir + (mTotalPic - 1) + ".png");
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.e(TAG, "surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "surfaceDestroyed");
		isSurfaceCreated = false;
		if (task != null){
			task.cancel();
			task = null;	
		}
	}
	
	/**
	 * 用于快速进入选择界面
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if(event.getAction() == MotionEvent.ACTION_UP)
		{
			isDestroy = true;
			if (task != null){
				task.cancel();
				task = null;	
			}
			if(!((HappyDoodleActivity)mContext).getHasPause() && guideBKCallback != null){
				guideBKCallback.nextScene();
			}
			
			
			/*Intent intent = new Intent(mContext,SelectCanvasActivity.class);
			intent.putExtra("Age", ((HappyDoodleActivity)mContext).getAge());
			mContext.startActivity(intent);
			((HappyDoodleActivity)mContext).finish();
			((HappyDoodleActivity)mContext).overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);*/
		}
		
		return true;
	}
	
	/**
	 * 当这个view移除窗口时，关闭动画并释放掉缓存的图片
	 */
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "onDetachedFromWindow");
		isDestroy = true;
		timer.cancel();
		freeImageCache();
		
	}
	
	/**
	 * 释放掉缓存的图片
	 */
	private void freeImageCache()
	{
		for(int i=0; i < imageCache.size(); i++)
		{
			Bitmap bitmap = imageCache.get(i);
			
			if(bitmap != null && !bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
			}
		}
	}
	
	/**
	 * 开始动画，有两个地方会调用到，一是第一组动画播放后，二是正在播放动画时按了home键然后再显示
	 * 注：两个地方可能会同时来，所以需要同步
	 */
	public synchronized void beginAnim()
	{
		//Log.e(TAG, "beginAnim isDestroy="+isDestroy+",isRunning="+isRunning);
		if (!isSurfaceCreated) {
			Log.e(TAG, "beginAnim isSurfaceCreated=" + isSurfaceCreated);
			return;
		}

		if (((HappyDoodleActivity) mContext).getHasPause()) {
			Log.e(TAG, "========HappyDoodleActivity hasPause=========");
			return;
		}
		if (isDestroy){//|| mTimeCount == mTotalPic) {
			Log.e(TAG, "beginAnim end,mTimeCount=" + mTimeCount);
			return;
		}

		if (mTimeCount >= mTotalPic - 1){ // 表明动画已显示完，画上最后一张图
			Log.e(TAG, "beginAnim end");
			drawOnePic(mPicDir + (mTotalPic - 1) + ".png");
		}

		if (!isRunning && mTimeCount == 0){ // 还未开始第一步的动画，则画上第一张图
			drawOnePic(mPicDir + mTimeCount + ".png");// 刷第一张图
			((HappyDoodleActivity) mContext).playEnterSound();
		} else {
			if (task != null) {
				task.cancel();
				task = null;
			}
			task = new MyTimerTask();
			timer.schedule(task, 0, timeout);
		}
	}
	
	/**
	 * 提供给外部调用以停止动画
	 */
	public synchronized void stopAnim() 
	{
		if (task != null){
			task.cancel();
			task = null;	
		}
	}
	
	
	
	/**
	 * 根据图片路径刷一张图到画布上
	 * @param picPath assert中的图片路径 
	 */
	private void drawOnePic(String picPath) 
	{
		if(((HappyDoodleActivity)mContext).getHasPause())
		{
			if(HappyDoodleApp.DEBUG)
				Log.e(TAG, "========drawOnePic HappyDoodleActivity hasPause=========");
			return ;
		}
		Canvas canvas = null;
		try 
		{
			canvas = mSHolder.lockCanvas();
			if(canvas == null)
				Log.e(TAG, "mTimeCount="+mTimeCount+",mSHolder="+mSHolder+",drawOnePic canvas=null");
			else {
				// 清除画布内容
				canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
				Bitmap bitmap = imageCache.size() > 0 ? imageCache.get(mTimeCount) : null;
				if (HappyDoodleApp.DEBUG)
					Log.e(TAG, "bitmap=" + bitmap);
				if (bitmap == null){ // 说明已经被回收，需重新解图
					bitmap = DataManager.decodeBitmapFromAsset(picPath, Config.RGB_565);
				}
				float w,h,sw;
				Matrix matrix;
				w = 0;
				h = 0;
				sw = 0;
				float scaleX;
				if (bitmap != null) {
					w = bitmap.getWidth();
					h = bitmap.getHeight();
					if (mWidth!=1280){
						sw = 1280;// 默认情况
						scaleX = mWidth / sw;
						matrix = new Matrix();
						matrix.postScale(scaleX, scaleX, 0, 0);
						canvas.drawBitmap(bitmap, matrix, null);
					}else {
						canvas.drawBitmap(bitmap, 0, 0, null);
					}
				}
			}
			
		} catch (Exception e) {
			Log.e(TAG, "mTimeCount="+mTimeCount+",mSHolder="+mSHolder+",drawOnePic error!");
		}
		finally 
		{
			if (canvas != null) {
				try {
					mSHolder.unlockCanvasAndPost(canvas);					
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
		
		
	}
	
	public void setIsRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean getIsRunning() {
		return isRunning;
	}
	
	/**
	 * 统一的timer调度任务内部类
	 * @author css
	 *
	 */
	class MyTimerTask extends TimerTask
	{

		@Override
		public void run() {
			mTimeCount++;
			//Log.e(TAG, "timeCount="+mTimeCount);
			if(mTimeCount >= mTotalPic)
			{
				//mTimeCount = 0;
				isRunning = false;
				mIsNeedResumeAnim = false;
				task.cancel();
				task = null;
				//timer.cancel();
				//((HappyDoodleActivity)mContext).finish();
				((HappyDoodleActivity)mContext).getMainHandler().sendEmptyMessage(
						HappyDoodleActivity.MSG_PLAY_ENTERSELECTACT_SOUND);
			}
			else 
			{
				drawOnePic(mPicDir+mTimeCount+".png");
			}
		}
	}
	
	/**
	 * 解图线程
	 */
	class DecodeBitmapThread implements Runnable
	{

		@Override
		public void run() {
			for(int i=0; isDestroy && i < mTotalPic; i++)
			{
				Bitmap bitmap = DataManager.decodeBitmapFromAsset(mPicDir + i + ".png",Config.RGB_565);
				/*SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(bitmap);
				imageCache.add(softReference);*/
				imageCache.add(bitmap);
			}
		}
	}

	public interface GuideBKCallback{
		public void nextScene();
	}
	private GuideBKCallback guideBKCallback;
	public void setCallback(GuideBKCallback guideBKCallback) {
		this.guideBKCallback = guideBKCallback;
	}
}


