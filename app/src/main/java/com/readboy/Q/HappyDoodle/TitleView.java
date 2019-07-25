package com.readboy.Q.HappyDoodle;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ReadboySurfaceView;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * 主界面标题动画
 * @author css
 * @version 1.0
 */
public class TitleView extends ReadboySurfaceView implements SurfaceHolder.Callback{

	private final static String TAG = "lqn-TitleView";
	private SurfaceHolder mSHolder;
	private Timer timer = new Timer();
	private TimerTask task = null;
	/** 刷图间隔 */
	private int timeout = 10;
	/** 当前刷到哪一张图片 */
	private int mTimeCount;
	/** 总图片数 */
	private int mTotalPic = Constant.GUIDE_TITLE_TOTAL_PIC;
	/** 图片目录 */
	private String mPicDir = Constant.GUIDE_TITLE_TOTAL_PIC_DIR;
	
	/** surfaceView是否已经准备好了 */
	private boolean isSurfaceCreated;
	/** 是否已经开始刷动画了 */
	private boolean isRunning;
	
	private Context mContext;
	private int mWidth;
	private int mHeight;
	
	/** 为了加快动画速度，使用线程解图，把解出来的图全部缓存到内存中 */
	//private ArrayList<SoftReference<Bitmap>> imageCache = new ArrayList<SoftReference<Bitmap>>();
	private ArrayList<Bitmap> imageCache = new ArrayList<Bitmap>();
	
	public TitleView(Context context) {
		super(context);
//		init(context,null);
	}
	
	/**
	 * 将此view放置于xml布局中时必须提供此构造函数
	 * @param context 上下文
	 * @param attrs 自定义属性
	 */
	public TitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		init(context,attrs);
	}
	
	/**
	 * 初始化surfaceView，主要是使其透明
	 */
	public void init(Context context, int apMode) {
		Log.i(TAG, "init");
		if(apMode == Constant.MODE_SILKWORM){
			mPicDir = Constant.GUIDE_TITLE1_TOTAL_PIC_DIR;
		}else if (apMode == Constant.MODE_LADYBUG) {
			mPicDir = Constant.GUIDE_TITLE2_TOTAL_PIC_DIR;
		}
		mContext = context;
		mSHolder = this.getSurface();
		//使背景透明
		setZOrderOnTop(true);
		mSHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSHolder.addCallback(this);

		mWidth = HappyDoodleApp.getScreenWidth();
//		Resources resources = this.getResources();
//		DisplayMetrics dm = resources.getDisplayMetrics();
//		float density = dm.density;
//		mWidth = dm.widthPixels;
//		mHeight = dm.heightPixels;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "surfaceCreated");
		isSurfaceCreated = true;
		beginAnim();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "surfaceDestroyed");
		isSurfaceCreated = false;
		if(task != null){
			task.cancel();
			task = null;
		}
		
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(HappyDoodleApp.DEBUG)
			Log.e(TAG, "onDetachedFromWindow");
		timer.cancel();
		//freeImageCache();
		
	}
	
	
	
	private void freeImageCache()
	{
		for(int i=0; i < mTotalPic; i++)
		{
			Bitmap bitmap = imageCache.get(i);
			
			if(bitmap != null && !bitmap.isRecycled())
			{
				bitmap.recycle();
				bitmap = null;
			}
		}
	}
	
	
	public void beginAnim()
	{
		if (!isSurfaceCreated) {
			Log.e(TAG, "beginAnim isSurfaceCreated=" + isSurfaceCreated);
			return;
		}

		if (((HappyDoodleActivity) mContext).getHasPause()) {
			Log.e(TAG, "========HappyDoodleActivity hasPause=========");
			return;
		}
		
		if(mTimeCount >= mTotalPic-1) {		//表明动画已显示完，画上最后一张图
			Log.e(TAG, "beginAnim end");
			drawOnePic(mPicDir+(mTotalPic-1)+".png");
			if(mTimeCount >= mTotalPic)//避免重新刷背景动画
				return ;
		}
		
		if(task != null){
			task.cancel();
			task = null;
		}
		task = new MyTimerTask();
		timer.schedule(task, 0, timeout);
		isRunning = true;
		Log.e(TAG, "666666====HappyDoodleActivity hasPause=========");
	}
	
	/**
	 * 提供给外部调用以停止动画
	 */
	public void stopAnim() 
	{
		if(task != null){
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
		if (((HappyDoodleActivity) mContext).getHasPause()) {
			if (HappyDoodleApp.DEBUG)
				Log.e(TAG, "========drawOnePic HappyDoodleActivity hasPause=========");
			return;
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
				Bitmap bitmap = DataManager.decodeBitmapFromAsset(picPath, Config.RGB_565);
				float w,h,sw;
				Matrix matrix;
				w = 0;
				h = 0;
				sw = 0;
				float scaleX;
				if (bitmap != null) {
					w = bitmap.getWidth();
					h = bitmap.getHeight();
					if (mWidth!=1280&&mWidth!=Constant.C20_WIDTH){
						sw = 1280f;// 默认情况
						scaleX = mWidth / sw;
						matrix = new Matrix();
						matrix.postScale(scaleX, scaleX, 0,0);
//						matrix.postTranslate((mWidth-w*scaleX)/2, 0);
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
			if (mTimeCount >= mTotalPic) {
				isRunning = false;
				task.cancel();
				task = null;
				HappyDoodleActivity.getGuideBK().setIsRunning(true);
				HappyDoodleActivity.getGuideBK().beginAnim();
			} else {
				drawOnePic(mPicDir + mTimeCount + ".png");
			}
		}
	}
	
	

}
