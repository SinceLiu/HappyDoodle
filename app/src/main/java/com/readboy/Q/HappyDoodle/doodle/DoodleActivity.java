package com.readboy.Q.HappyDoodle.doodle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.OpusShow.OpusShowActivity;
import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.DialogStyle;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.OnBtnClickCallback;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;
import com.readboy.Q.HappyDoodle.util.ActivityManagerUtil;
import com.readboy.Q.HappyDoodle.util.BaseActivity;
import com.readboy.Q.HappyDoodle.util.BitmapMethod;
import com.readboy.Q.HappyDoodle.util.ToastUtils;
import com.readboy.Q.HappyDoodle.util.Utils;
import com.readboy.reward.Reward;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DoodleActivity extends BaseActivity {
	private static final String TAG = "lqn-DoodleActivity";
	/** 一次涂鸦完成，用于更新mCanvas */
	private static final int MSG_DOODLE_FINISH = 0x100;
	/** 显示进度条 */
	private static final int MSG_SHOW_PROGRESS = 0x101;
	/** 隐藏进度条 */
	private static final int MSG_HIDE_PROGRESS = 0x102;
	/** timer消息 */
	private static final int MSG_TIMER = 0x103;
	/** canvas标题 */
	private ImageView mTitle;
	/** 关闭按钮 */
	private Button mCloseBtn;
	/** 作品集按钮 */
	private Button mOpusBtn;
	/** 展示按钮 */
	private Button mShowBtn;
	/** 宝贝计划上传按钮 */
	private Button mUploadBtn;
	/** 参考图提示按钮 */
	private Button mHintBtn;
	/** 换下一副画布按钮 */
	private Button mNextCanvasBtn;

	/** 上一页颜色按钮 */
	private Button mLastColorPageBtn;
	/** 下一页颜色按钮 */
	private Button mNextColorPageBtn;
	/** viewpager控件 */
	private VerticalViewPager mViewPager;
	/** viewpager控件的适配器 */
	private ColorViewPagerAdapter mViewPagerAdapter;
	/** 画布 *//*
				 * private ImageView mCanvas;
				 */
	/** 自定义填充view */
	private DoodleView mDoodleView;
	/** 进度条 */
	private ProgressBar mProgressBar;
	/** 当前颜色，默认红色(在颜色数组中下标为10) */
	private int mCurColor = Constant.getColor(0);
	/** 当前选中颜色索引 */
	private int mCurColorSelected = 0;
	/** 涂鸦的目标bitmap */
	private Bitmap bitmapCache = null;
	/** 涂鸦的canvas的bitmap */
	private Bitmap bitmap;

	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 所有作品的路径 */
	private ArrayList<String> mOpusFilesPath;
	/** canvas图片索引 */
	private int mPicIndex;
	/** 是否需要保存 */
	private boolean mIsNeedSave;
	/** 提示对话框 */
	private ProgressDialog mProgressDialog;
	private Handler mMainHandler;
	/** 是否禁止再有操作 */
	public static boolean mIsForbidOp = true;
	/** DoodleView是否初始化完毕 */
	public static boolean isDoodleViewInitEnd = false;
	/** 是否有焦点 */
	private boolean mHasFocus;
	/** activity是否已暂停 */
	private boolean mHasPause;

	/** 弹出是否保存对话框的几种操作 */
	private enum OPERATION {
		DOWN_CLOSE_BTN, DOWN_OPUS_BTN, DOWN_NEXTCANVAS_BTN;
	}

	/** 记录下弹出是否保存对话框的操作 */
	private OPERATION mOperation;

	/** 播放声音对象 */
	private MediaPlayer mMediaPlayer;
	/** 声音是否被打断，比如按home键或进入其它界面了，用于返回该界面时恢复播放 */
	private boolean mIsSndPause;

	/** 图片显示选项 */
	private static DisplayImageOptions options;

	private int apMode; // ap模式
	// private boolean needStartTimer = false; //是否需要开启定时器，默认不开启，只有从其他ap跳转进来才要
	private Timer timer; // 定时器
	private int timerCount; // 定时器计数
	private boolean playTipVoice = false; // 默认过20秒后会播放提示音，播放后该标志位置为false
	private static final int COUNT_SUM = 200;// 计时20秒
	/** 蚕宝宝成长记瓢虫的世界或者瓢虫的世界模式保存图片的路径 */
	private String savePicPath = null;
	/** 宝贝计划调用时，会传进来两张本地图片的路径和返回给宝贝计划图片的保存路径key值 */
	private String babyPicPath0 = null; // 前景，涂鸦后刷上一层，让图片变圆滑
	private String babyPicPath1 = null; // 背景，用来涂鸦的
	private String outputPath = null;

	private ImageView mouseView; // 老鼠动画
	private int mouseAnimType = 0; // 老鼠状态，0：正常 1、点击
	private int mouseCount = 0; // 老鼠动画图片计数
	private String mousePic = ""; // 老鼠图片名字
	private int showBtnCount = 0; // 展示按钮计时
	private Drawable drawable0; // 展示按钮图片（在蚕宝宝模式显示的按钮）
	private Drawable drawable1;
	private Drawable drawable2;

	private CustomDialog customDialog; // 对话框

	private static final int NORMAL_POINT = 2;
	private static final int SHOW_POINT = 2;

	@Override
	public boolean onInit() {
		// Log.e(TAG, "000000--------------onInit--"+getTaskId()+"------activity
		// = "+this);
		setContentView(R.layout.doodle);
		ActivityManagerUtil.getInstance().addActivity(this);// 将该activity加入activity管理类中

		// Log.i(TAG, "mPicIndex="+mPicIndex);
		isDoodleViewInitEnd = false;

		MyOnClickListenner onClickListenner = new MyOnClickListenner();
		mTitle = (ImageView) findViewById(R.id.title);
		mCloseBtn = (Button) findViewById(R.id.closeBtn);
		mOpusBtn = (Button) findViewById(R.id.opusBtn);
		mShowBtn = (Button) findViewById(R.id.showBtn);
		mUploadBtn = (Button) findViewById(R.id.uploadBtn);
		// mHintBtn = (Button) findViewById(R.id.hintBtn);
		mNextCanvasBtn = (Button) findViewById(R.id.nextCanvasBtn);
		mouseView = (ImageView) findViewById(R.id.mouse);
		mouseView.setSoundEffectsEnabled(false);

		Intent intent = getIntent();
		mAge = intent.getIntExtra("AGE", 0);
		mPicIndex = intent.getIntExtra("CANVAS_INDEX", 0);
		apMode = intent.getIntExtra("apMode", Constant.MODE_NORMAL);
		if (apMode == Constant.MODE_SILKWORM) {
			mAge = 0;
			mPicIndex = Constant.SILKWORM_INDEX;
			playTipVoice = true;
		} else if (apMode == Constant.MODE_LADYBUG) {
			mAge = 0;
			mPicIndex = Constant.LADYBUG_INDEX;
			playTipVoice = true;
		} else if (apMode == Constant.MODE_BABYPLAN) {
			babyPicPath0 = intent.getStringExtra("Path0");
			babyPicPath1 = intent.getStringExtra("Path1");
			outputPath = intent.getStringExtra(MediaStore.EXTRA_OUTPUT);
			if (outputPath == null || outputPath.isEmpty()) {
				// outputPath =
				// "mnt/sdcard/happydoodle/happydoodle_babyplan.png";
			}
			Log.w(TAG, "--------path0 = " + babyPicPath0 + "----path1 = " + babyPicPath1 + "----outputPaht = "
					+ outputPath);
		}

		if (apMode != Constant.MODE_NORMAL) {
			mOpusBtn.setVisibility(View.INVISIBLE);
			mShowBtn.setVisibility(View.VISIBLE);
			mNextCanvasBtn.setVisibility(View.INVISIBLE);
			if (apMode == Constant.MODE_BABYPLAN) {
				mUploadBtn.setVisibility(View.VISIBLE);
				mTitle.setVisibility(View.INVISIBLE);
				mShowBtn.setVisibility(View.INVISIBLE);
			}
		}

		mViewPager = (VerticalViewPager) findViewById(R.id.viewpager);
		mLastColorPageBtn = (Button) findViewById(R.id.lastColorPageBtn);
		mNextColorPageBtn = (Button) findViewById(R.id.nextColorPageBtn);

		/*
		 * mCanvas = (ImageView) findViewById(R.id.canvas); //为mCanvas设置监听器
		 * mCanvas.setOnTouchListener(new canvasTouchListener());
		 */

		mDoodleView = (DoodleView) findViewById(R.id.doodle_view);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mMainHandler = new MyHandler();

		initViews();

		initImageOptions();

		mCloseBtn.setOnClickListener(onClickListenner);
		mOpusBtn.setOnClickListener(onClickListenner);
		// mHintBtn.setOnClickListener(onClickListenner);
		mNextCanvasBtn.setOnClickListener(onClickListenner);
		mShowBtn.setOnClickListener(onClickListenner);
		mUploadBtn.setOnClickListener(onClickListenner);
		mLastColorPageBtn.setOnClickListener(onClickListenner);
		mNextColorPageBtn.setOnClickListener(onClickListenner);
		mouseView.setOnClickListener(onClickListenner);

		mViewPagerAdapter = new ColorViewPagerAdapter(this);
		mViewPager.setAdapter(mViewPagerAdapter);
		// mViewPager.setOnPageChangeListener(new MyOnPagerChangeListenner());

		// mMediaPlayer = playSnd("nh_060.ogg", false, null);
		return true;
	}

	private void initViews() {
		/*
		 * Bitmap bm =
		 * DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(mPicIndex),
		 * null); Log.e(TAG, "1 bitmap="+bm); //mCanvas.setImageBitmap(bm);
		 * setCanvasByBmp(bm);
		 * 
		 * BitmapDrawable bitmapDrawable = (BitmapDrawable)
		 * mCanvas.getDrawable(); //获取第一个图片显示框中的位图 bitmap =
		 * bitmapDrawable.getBitmap(); //copy一份可修改(即mutable)的bitmap，不然默认的是不可修改的，
		 * 则setPixel会抛出IllegalStateException异常 bitmapCache =
		 * bitmap.copy(Bitmap.Config.ARGB_8888, true);
		 */

		/*
		 * Bitmap bm =
		 * DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(mPicIndex),
		 * null); //Log.e(TAG, "1 bitmap="+bm); Bitmap bg =
		 * DataManager.decodeBitmapFromAsset(getCanvasBgPathByIndex(mPicIndex),
		 * null); //Log.e(TAG, "2 bitmap="+bg); mDoodleView.setTemp2(bg);
		 * mDoodleView.setTemp(bm);
		 */
		initDoodleViewByThread(mPicIndex);
		mDoodleView.setColor(mCurColor);

		Bitmap title = DataManager.decodeBitmapFromAsset(getCanvasTitleByIndex(mPicIndex), null);
		// Log.e(TAG, "3 bitmap="+title);
		// mCanvas.setImageBitmap(bm);
		setTitleByBmp(title);

		String saveDir = "";
		// 如果手机插入了SD卡，而且应用程序具有访问SD的权限
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File sdCardDir = Environment.getExternalStorageDirectory();
			try {
				saveDir = sdCardDir.getCanonicalPath() + Constant.SAVE_PATH;
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Log.e(TAG, "-----sapce =
			// "+Utils.getUsableSpace(sdCardDir)+"---min =
			// "+Constant.MIN_DISK_SPACE);
			if (Utils.getUsableSpace(sdCardDir) < Constant.MIN_DISK_SPACE) {
				if (HappyDoodleApp.DEBUG)
					Log.e(TAG, "DISK SPACE not enough");
				if (apMode == Constant.MODE_NORMAL) {
					ToastUtils.show(this, "小朋友，SD卡空间不足10M了，作品有可能保存不了噢，请删掉一些文件后再来吧！", Toast.LENGTH_LONG);
				}
				// 空间不足时需清空缓存，不然将不能解析图片
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().clearDiscCache();
			}
		}
		mOpusFilesPath = getFiles(saveDir);
	}

	private synchronized void initDoodleView(int picIndex) {
		// Log.e(TAG, "initDoodleView picIndex="+picIndex);
		isDoodleViewInitEnd = false;
		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_SHOW_PROGRESS, null));
		Bitmap bm = null;
		Bitmap bg = null;
		if (apMode == Constant.MODE_BABYPLAN && babyPicPath0 != null && (!babyPicPath0.isEmpty())
				&& babyPicPath1 != null && (!babyPicPath1.isEmpty())) {
			bm = DataManager.decodeBitmapFromPath(babyPicPath0, null);
			bg = DataManager.decodeBitmapFromPath(babyPicPath1, null);
		} else {
			bm = DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(picIndex), null);
			bg = DataManager.decodeBitmapFromAsset(getCanvasBgPathByIndex(picIndex), null);
		}

		if (bm == null || bg == null) {
			bm = DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(picIndex), null);
			bg = DataManager.decodeBitmapFromAsset(getCanvasBgPathByIndex(picIndex), null);
		}

		// Log.e(TAG, "1 bitmap="+bm);
		// Log.e(TAG, "2 bitmap="+bg);
		mDoodleView.setTemp2(bg);
		mDoodleView.setTemp(bm);
		mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_HIDE_PROGRESS, null));
	}

	private void initDoodleViewByThread(final int picIndex) {
		// Log.i(TAG, "initDoodleViewByThread picIndex="+picIndex);
		new Thread() {
			@Override
			public void run() {
				// Log.i(TAG, "run picIndex="+picIndex);
				initDoodleView(picIndex);
			}
		}.start();
	}

	private void initImageOptions() {
		// 磁盘空间不足时不使用硬盘缓存
		if (Utils.getUsableSpace(Environment.getExternalStorageDirectory()) < Constant.MIN_DISK_SPACE) {
			options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub)
					.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error)
					.resetViewBeforeLoading(true).cacheInMemory(true)
					// .cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
					.displayer(new FadeInBitmapDisplayer(300)).build();
		} else {
			options = new DisplayImageOptions.Builder().showStubImage(R.drawable.ic_stub)
					.showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error)
					.resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
					.displayer(new FadeInBitmapDisplayer(300)).build();
		}
	}

	/**
	 * 在绘制选中状态时，为了避免闪烁，重新设置显示选项，即把如“存根图、错误图”等，以及动画效果去掉
	 */
	public void initSelectedImageOptions() {
		// 磁盘空间不足时不使用硬盘缓存
		if (Utils.getUsableSpace(Environment.getExternalStorageDirectory()) < Constant.MIN_DISK_SPACE) {
			options = new DisplayImageOptions.Builder()
					// .resetViewBeforeLoading(true)
					.cacheInMemory(true)
					// .cacheOnDisc(true)
					.imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).build();
		} else {
			options = new DisplayImageOptions.Builder()
					// .resetViewBeforeLoading(true)
					.cacheInMemory(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
					.bitmapConfig(Bitmap.Config.RGB_565).build();
		}
	}

	private void initDialog() {
		if (apMode == Constant.MODE_NORMAL) {
			CustomDialog.initialize(this, DialogStyle.SAVE, new OnBtnClickCallback() {

				@Override
				public void onBtnClick(View view, String btnName) {
					// Log.e(TAG, "btnName="+btnName);
					dismissDialog();
					if (btnName.equals("yes_btn")) {
						if (!savaFile()) // 保存失败则不进操作
							return;
						else {
							Reward.pointScore(DoodleActivity.this, NORMAL_POINT);
						}
					}

					mIsNeedSave = false;
					mDoodleView.setDraw(false);

					if (mOperation == OPERATION.DOWN_CLOSE_BTN) {
						// SelectCanvasActivity.mIsNeedPauseBkSnd = false;
						finish();
						// ActivityManagerUtil.getInstance().exit();//退出整个程序
						overridePendingTransition(0, R.anim.slide_out_up);
					} else if (mOperation == OPERATION.DOWN_OPUS_BTN) {
						// SelectCanvasActivity.mIsNeedPauseBkSnd = false;
						Intent intent = new Intent(DoodleActivity.this, OpusSetActivity.class);
						launchForResult(intent, -1);
						finish();
						overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
					} else if (mOperation == OPERATION.DOWN_NEXTCANVAS_BTN) {
						if (mPicIndex == Constant.TOTAL_CANVAS_EACH_AGE[mAge] - 1) {
							mPicIndex = 0;
						} else {
							mPicIndex++;
						}
						setTitleByIndex(mPicIndex);
						/*
						 * setCanvasByIndex(mPicIndex);
						 * setTitleByIndex(mPicIndex); //需要更新缓存图片
						 * updateBitmapCache();
						 */

						/*
						 * Bitmap bm = DataManager.decodeBitmapFromAsset(
						 * getCanvasPathByIndex(mPicIndex),null); Log.e(TAG,
						 * "1 bitmap="+bm); Bitmap bg =
						 * DataManager.decodeBitmapFromAsset(
						 * getCanvasBgPathByIndex(mPicIndex),null); Log.e(TAG,
						 * "2 bitmap="+bg); mDoodleView.setTemp2(bg);
						 * mDoodleView.setTemp(bm);
						 */
						initDoodleViewByThread(mPicIndex);
					}
				}
			});
		} else if (apMode == Constant.MODE_BABYPLAN || apMode == Constant.MODE_SILKWORM
				|| apMode == Constant.MODE_LADYBUG) {
			DialogStyle dialogStyle = DialogStyle.EXIT_BABY;
			if (apMode == Constant.MODE_SILKWORM) {
				dialogStyle = DialogStyle.EXIT_SILKWORM;
			} else if (apMode == Constant.MODE_LADYBUG) {
				dialogStyle = DialogStyle.EXIT_LADYBUG;
			}
			CustomDialog.initialize(this, dialogStyle, new OnBtnClickCallback() {

				@Override
				public void onBtnClick(View view, String btnName) {
					// Log.e(TAG, "btnName="+btnName);
					if (btnName.equals("yes_btn")) { // 退出宝贝计划涂鸦/蚕宝宝成长记/瓢虫的世界，返回原ap
						dismissDialog();
						finish();
					} else if (btnName.equals("no_btn")) {
						if (mMediaPlayer != null) {
							if (mMediaPlayer.isPlaying())
								mMediaPlayer.stop();
							mMediaPlayer.reset();
							mMediaPlayer.release();
							mMediaPlayer = null;
						}
						dismissDialog();
						//
					}
				}
			});
		}
	}

	/**
	 * 显示对话框
	 */
	private void showCustomDialog() {
		if (customDialog == null) {
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

	private String getCanvasDirByAge(int age) {
		String dir = "";
		if (mAge == 0)
			dir = "pic/canvas_3-4/";
		else
			dir = "pic/canvas_5-6/";
		// Log.e(TAG, "path="+dir);
		return dir;
	}

	private String getCanvasPathByIndex(int index) {
		String path = "";

		path = getCanvasDirByAge(mAge) + "canvas_" + index + ".png";
		// Log.e(TAG, "path="+path);
		return path;
	}

	private String getCanvasBgPathByIndex(int index) {
		String path = "";

		path = getCanvasDirByAge(mAge) + "canvas_bg_" + index + ".png";
		// Log.e(TAG, "path="+path);
		return path;
	}

	private String getCanvasTitleByIndex(int index) {
		String path = "";

		path = getCanvasDirByAge(mAge) + "title_" + index + ".png";
		// Log.e(TAG, "path="+path);
		return path;
	}

	private void setCanvasByIndex(int index) {
		Bitmap bitmap = DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(index), null);
		// Log.e(TAG, "bitmap="+bitmap);
		// setCanvasByBmp(bitmap);
	}

	private void setTitleByIndex(int index) {
		Bitmap bitmap = DataManager.decodeBitmapFromAsset(getCanvasTitleByIndex(index), null);
		// Log.e(TAG, "2 bitmap="+bitmap);
		setTitleByBmp(bitmap);
	}

	/**
	 * 需要更新缓存图片
	 */
	/*
	 * private void updateBitmapCache() { BitmapDrawable bitmapDrawable =
	 * (BitmapDrawable) mCanvas.getDrawable(); //获取第一个图片显示框中的位图 bitmap =
	 * bitmapDrawable.getBitmap(); //copy一份可修改(即mutable)的bitmap，不然默认的是不可修改的，
	 * 则setPixel会抛出IllegalStateException异常 bitmapCache =
	 * bitmap.copy(Bitmap.Config.ARGB_8888, true); }
	 */

	/*
	 * private void setCanvasByBmp(Bitmap bitmap) { Log.e(TAG,
	 * "setCanvasByBmp +++++++++"); mCanvas.setImageBitmap(bitmap); Log.e(TAG,
	 * "setCanvasByBmp ---------"); }
	 */

	private void setTitleByBmp(Bitmap bitmap) {
		int w = HappyDoodleApp.getScreenWidth();
		if (w>1280) {
			ViewGroup.LayoutParams lp;
			lp = mTitle.getLayoutParams();
			lp.width = Utils.dip2px(getApplicationContext(), 189);
			lp.height = Utils.dip2px(getApplicationContext(), 44);
			mTitle.setLayoutParams(lp);
		}
		mTitle.setImageBitmap(bitmap);
	}

	public void setBitmapCache(Bitmap bitmapCache) {
		this.bitmapCache = bitmapCache;
	}

	/**
	 * 设置当前显示页
	 * 
	 * @param pageIdx
	 *            当前显示页索引
	 */
	private void setCurrentColorPage(int pageIdx) {
		mViewPager.setCurrentItem(pageIdx, true);
	}

	/**
	 * 根据颜色索引设置当前颜色
	 * 
	 * @param colorIndex
	 */
	public void setCurColor(int colorIndex) {
		mCurColorSelected = colorIndex;
		mCurColor = Constant.getColor(colorIndex);
		mDoodleView.setColor(mCurColor);
	}

	public int getCurColorSelected() {
		return mCurColorSelected;
	}

	public static DisplayImageOptions getOptions() {
		return options;
	}

	public VerticalViewPager getViewPager() {
		return mViewPager;
	}

	public DoodleView getDoodleView() {
		return mDoodleView;
	}

	public boolean getHasPause() {
		return mHasPause;
	}

	/**
	 * 播放提示音
	 */
	private void playHintSnd() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
		}
		String sound = "nh_060.ogg";
		int n = (int) (Math.random() * 2);
		if (apMode == Constant.MODE_NORMAL) {
			sound = "nh_060.ogg";
		} else if (apMode == Constant.MODE_SILKWORM) {
			if (n == 1) {
				sound = "silkworm0.ogg";
			} else {
				sound = "xn_1671.ogg";
			}
		} else if (apMode == Constant.MODE_LADYBUG) {
			if (n == 1) {
				sound = "ladybug0.ogg";
			} else {
				sound = "xn_1671.ogg";
			}
		} else if (apMode == Constant.MODE_BABYPLAN) {
			if (n == 1) {
				sound = "babyplan.ogg";
			}
		}
		mMediaPlayer = DataManager.playSound(sound, false, new MyOnCompleteListenner(0));
		// Log.e(TAG, "------playHintSnd------mIsSndPause="+mIsSndPause);
		if (mIsSndPause || !mHasFocus || mHasPause) {
			mMediaPlayer.pause();
			mIsSndPause = true;
		}
	}

	/**
	 * 播放提示音
	 */
	private void playSnd(String soundPath, boolean isLoop, OnCompletionListener completionListener) {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.reset();
			mMediaPlayer.release();
		}
		mMediaPlayer = DataManager.playSound(soundPath, isLoop, completionListener);
		// Log.e(TAG, "------playHintSnd------mIsSndPause="+mIsSndPause);
		if (mIsSndPause || !mHasFocus || mHasPause) {
			mMediaPlayer.pause();
			mIsSndPause = true;
		}
	}

	// 定时器
	public void onStartTimer() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg = new Message();
				msg.what = MSG_TIMER;
				mMainHandler.sendMessage(msg);
			}
		}, 0, 100);
	}

	private void closeTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// return super.onKeyUp(keyCode, event);
		// Log.e(TAG, "------onKeyUp------keyCode="+keyCode);
		if (!mHasFocus) // 在还没获得焦点前，不响应按键操作
		{
			// Log.e(TAG, "------onKeyUp------mHasFocus="+mHasFocus);
			return true;
		}

		// 是否移出了按钮范围，即用户可能不想响应该按钮了
		boolean isCanceled = ((KeyEvent.FLAG_CANCELED & event.getFlags()) == 0) ? false : true;
		// Log.e(TAG, "event="+event.getFlags()+",isCanceled="+isCanceled);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (isCanceled)
				return true;
			mIsNeedSave = mDoodleView.isDraw();
			if (mIsNeedSave) {
				mOperation = OPERATION.DOWN_CLOSE_BTN;
				showCustomDialog();
				return true;// 已经处理了，不再转发
			} else
				break;

		default:
			break;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}

	/**
	 * 从其他activity返回时会来
	 */
	@Override
	protected void onContinue() {
		super.onContinue();
		initDialog();

		if (HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onResume------");
		mHasPause = false;
		SelectCanvasActivity.mIsNeedPauseBkSnd = true;
		if (mHasFocus) {
			SelectCanvasActivity.resumeBkSnd();
			onStartTimer();
		}
	}

	/**
	 * 按了home键后重新进入或先锁屏然后解锁时会来
	 */
	@Override
	public void onReinit() {
		super.onReinit();
		if (HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onRestart------");
		// bkAnim.start();
		// launchBK.beginAnim();
		if (mIsSndPause && mMediaPlayer != null) {
			mMediaPlayer.start();
			mIsSndPause = false;
		}
	}

	/**
	 * 切换界面时会来
	 */
	@Override
	protected void onSuspend() {
		super.onSuspend();
		if (HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onPause------");
		mHasPause = true;
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			mIsSndPause = true;
		} else
			mIsSndPause = false;

		SelectCanvasActivity.pauseBkSnd();
		closeTimer();
	}

	/**
	 * 按了home键后或锁屏或关闭该activity时会来
	 */
	@Override
	public void onHalt() {
		super.onHalt();
		if (HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onStop------");
		// bkAnim.stop();
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
		}

	}

	@Override
	public void onExit() {
		super.onExit();
		if (HappyDoodleApp.DEBUG)
			Log.i(TAG, "------onDestroy------");
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying())
				mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}

		dismissDialog();// 防止按home键泄露对话框窗口

		/*
		 * Bitmap bm = ((BitmapDrawable)mCanvas.getDrawable()).getBitmap();
		 * if(bm != null && !bm.isRecycled()) { Log.e(TAG, "3 bitmap="+bm);
		 * bm.recycle(); bm = null; }
		 */

		mDoodleView.recycleBitmap();

		Bitmap title = ((BitmapDrawable) mTitle.getDrawable()).getBitmap();
		if (title != null && !title.isRecycled()) {
			// Log.e(TAG, "4 bitmap="+title);
			title.recycle();
			title = null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Log.e(TAG, "==========onSaveInstanceState===========");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onAttachedToWindow() {
		// Log.e(TAG, "------onAttachedToWindow------");
		super.onAttachedToWindow();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (HappyDoodleApp.DEBUG)
			Log.e(TAG, "------onWindowFocusChanged------hasFocus=" + hasFocus);
		if (hasFocus) {
			mIsForbidOp = false;
			mHasFocus = true;

			SelectCanvasActivity.mIsNeedPauseBkSnd = true;
			if (!mHasPause)
				SelectCanvasActivity.resumeBkSnd();
			onStartTimer();
		} else {
			mIsForbidOp = true;
			mHasFocus = false;
		}
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// Log.e(TAG, "------dispatchTouchEvent------ev="+ev.getAction());
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 获取指定文件夹下的某种类型的文件名
	 */
	private ArrayList<String> getFiles(String path) {
		ArrayList<String> listImageFiles = new ArrayList<String>();
		File files = null;
		try {
			files = new File(path);
			String[] strFiles = files.list(new ImageFileFilter());
			// Log.i(TAG, "-------------strFiles = "+strFiles);
			if (strFiles != null && strFiles.length > 0) {
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

	private String getCurTime() {

		// or Time t=new Time("GMT+8"); 加上Time Zone资料。
		Time t = new Time();
		// 取得系统时间。
		t.setToNow();
		int year = t.year;
		int month = t.month + 1;
		int date = t.monthDay;
		int hour = t.hour; // 0-23
		int minute = t.minute;
		int second = t.second;
		return "涂鸦作品 " + year + "-" + month + "-" + date + "-" + hour + "-" + minute + "-" + second;
	}

	/**
	 * 生成新的文件名。
	 * 
	 * @return
	 */
	private String createNewFileName() {
		int num, index;
		String fileName = null, newFileName = null;

		/*
		 * for (num = 1; num <= Constant.MAX_OPUS; num++) { newFileName =
		 * "作品"+num/100+(num%100)/10+num%10+".png"; if (mOpusFilesPath.size() ==
		 * 0) { break; }
		 * 
		 * for (index = 0; index < mOpusFilesPath.size(); index++) { String
		 * tempPath = mOpusFilesPath.get(index); fileName =
		 * tempPath.substring(tempPath.lastIndexOf('/')+1); //Log.i(TAG,
		 * "fileName="+fileName);
		 * 
		 * if (fileName.equals(newFileName)) { break; } } if (index >=
		 * mOpusFilesPath.size()) { break; } }
		 */
		if (apMode == Constant.MODE_NORMAL) {
			newFileName = getCurTime() + ".png";
		} else if (apMode == Constant.MODE_SILKWORM) {
			newFileName = Constant.SILKWORM_NAME + ".png";
		} else if (apMode == Constant.MODE_LADYBUG) {
			newFileName = Constant.LADYBUG_NAME + ".png";
		} else if (apMode == Constant.MODE_BABYPLAN) {
			newFileName = getCurTime() + ".png";
		}

		// Log.e(TAG, "newFileName="+newFileName);
		return newFileName;
	}

	/**
	 * 保存涂鸦作品
	 * 
	 * @return
	 */
	public boolean savaFile() {
		// Log.e(TAG, "savaFile");
		try {
			// 如果手机插入了SD卡，而且应用程序具有访问SD的权限
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				boolean bRet = true;
				String saveDir;
				if (apMode == Constant.MODE_BABYPLAN) {
					if (outputPath == null || outputPath.isEmpty()) {
						ToastUtils.show(this, "保存图片失败，不能上传图片", Toast.LENGTH_LONG);
						return false;
					}
					saveDir = outputPath.substring(0, outputPath.lastIndexOf("/"));
				} else {
					File sdCardDir = Environment.getExternalStorageDirectory();
					saveDir = sdCardDir.getCanonicalPath() + Constant.SAVE_PATH;
				}
				File tempFile = new File(saveDir);

				if (!tempFile.exists()) {
					bRet = tempFile.mkdirs();
				}
				if (bRet == false) {
					Log.e(TAG, "mkdirs fail");
					if (apMode == Constant.MODE_BABYPLAN) {
						ToastUtils.show(this, "保存图片失败，不能上传图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_SILKWORM) {
						ToastUtils.show(this, "保存图片失败，不能展示图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_LADYBUG) {
						ToastUtils.show(this, "保存图片失败，不能展示图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_NORMAL) {
						ToastUtils.show(this, "保存作品失败", Toast.LENGTH_LONG);
					}
					return false;
				}

				// 先清掉缓存
				ImageLoader.getInstance().clearMemoryCache();
				ImageLoader.getInstance().clearDiscCache();

				// Log.e(TAG, "----UsableSpace =
				// "+Utils.getUsableSpace(tempFile)+"---Constant.SPACE =
				// "+Constant.MIN_DISK_SPACE);

				if (Utils.getUsableSpace(tempFile) < Constant.MIN_DISK_SPACE) {
					if (HappyDoodleApp.DEBUG)
						Log.e(TAG, "DISK SPACE not enough to save");
					if (apMode == Constant.MODE_BABYPLAN) {
						ToastUtils.show(this, "小朋友，SD卡空间不足10M，不能上传图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_SILKWORM) {
						ToastUtils.show(this, "小朋友，SD卡空间不足10M，不能展示图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_LADYBUG) {
						ToastUtils.show(this, "小朋友，SD卡空间不足10M，不能展示图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_NORMAL) {
						ToastUtils.show(this, "小朋友，SD卡空间不足10M了，作品无法保存哦！", Toast.LENGTH_LONG);
					}
					return false;
				}

				String path;
				if (apMode == Constant.MODE_BABYPLAN) { // 保存宝贝计划图片
					path = outputPath;
				} else {
					path = saveDir + createNewFileName();
					savePicPath = path;
				}

				if (path == null && apMode != Constant.MODE_NORMAL) {
					if (apMode == Constant.MODE_BABYPLAN) {
						ToastUtils.show(this, "保存图片失败，不能上传图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_SILKWORM) {
						ToastUtils.show(this, "保存图片失败，不能展示图片", Toast.LENGTH_LONG);
					} else if (apMode == Constant.MODE_LADYBUG) {
						ToastUtils.show(this, "保存图片失败，不能展示图片", Toast.LENGTH_LONG);
					}
					return false;
				}

				if (path == null || mOpusFilesPath.size() >= Constant.MAX_OPUS) {
					if (HappyDoodleApp.DEBUG)
						Log.e(TAG, "out of capacity");
					ToastUtils.show(this, "小朋友，作品太多了，不能保存了，请删掉一些后再来吧！", Toast.LENGTH_LONG);
					return false;
				}

				File saveFile = new File(path);
				/*
				 * if(!saveFile.exists()) saveFile.createNewFile();
				 */

				FileOutputStream ios = new FileOutputStream(saveFile);
				// Bitmap bitmap =
				// ((BitmapDrawable)mCanvas.getDrawable()).getBitmap();
				Bitmap bitmap = null;
				if (apMode == Constant.MODE_BABYPLAN) {
					bitmap = BitmapMethod.combineBitmap(mDoodleView.getBm(), mDoodleView.getTemp2(), 1);
				} else {
					bitmap = BitmapMethod.combineBitmap(mDoodleView.getBm(), mDoodleView.getTemp2(), 0);
				}
				bitmap.compress(CompressFormat.PNG, 0, ios);
				ios.close();
				if (bitmap != null && bitmap.isRecycled())
					bitmap.recycle();
				mOpusFilesPath.add(path);
				/*
				 * Utils.requestScanFileForAdd(this,saveFile);//已将保存目录隐藏，
				 * 所以不添加到数据库了 Log.e(TAG, "scanFile end");
				 */
			} else {
				if (HappyDoodleApp.DEBUG)
					Log.e(TAG, "sdcard error");
				if (apMode == Constant.MODE_NORMAL) {
					ToastUtils.show(this, "SD卡不可用，保存失败，请确保已经正确关闭了usb存储设备", Toast.LENGTH_LONG);
				} else if (apMode == Constant.MODE_BABYPLAN) {
					ToastUtils.show(this, "SD卡不可用，不能上传图片", Toast.LENGTH_LONG);
				} else if (apMode == Constant.MODE_SILKWORM) {
					ToastUtils.show(this, "SD卡不可用，不能展示图片", Toast.LENGTH_LONG);
				} else if (apMode == Constant.MODE_LADYBUG) {
					ToastUtils.show(this, "SD卡不可用，不能展示图片", Toast.LENGTH_LONG);
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (apMode == Constant.MODE_NORMAL) {
				ToastUtils.show(this, "SD卡异常，保存失败，请重试", Toast.LENGTH_LONG);
			} else if (apMode == Constant.MODE_BABYPLAN) {
				ToastUtils.show(this, "SD卡异常，不能上传图片", Toast.LENGTH_LONG);
			} else if (apMode == Constant.MODE_SILKWORM) {
				ToastUtils.show(this, "SD卡异常，不能展示图片", Toast.LENGTH_LONG);
			} else if (apMode == Constant.MODE_LADYBUG) {
				ToastUtils.show(this, "SD卡异常，不能展示图片", Toast.LENGTH_LONG);
			}
			return false;
		}

		return true;
	}

	/***
	 * 返回宝贝计划ap，让宝宝计划上传图片
	 */
	private void uploadToBaby() {
		playSnd("upload.ogg", false, new MyOnCompleteListenner(2));
		if (savaFile()) {
			setResult(RESULT_OK);
			finish();
		} else {
			finish();
			return; // 保存图片失败，无操作
		}

		/*
		 * Bitmap bitmap = BitmapMethod.combineBitmap(mDoodleView.getBm(),
		 * mDoodleView.getTemp2()); Intent intent = new Intent();
		 * ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 * bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); byte []
		 * bitmapByte = baos.toByteArray(); intent.putExtra("kltyBitmap",
		 * bitmapByte); finish(); overridePendingTransition(0,
		 * R.anim.slide_out_up);
		 */
	}

	private void fillBitmap(int x, int y) {
		// 填充前copy一份可修改(即mutable)的bitmap，以备恢复
		Bitmap bitmapCopy = bitmapCache.copy(Bitmap.Config.ARGB_8888, true);
		FloodFillAlgorithm ffa = new FloodFillAlgorithm(bitmapCache);
		boolean ret = ffa.floodFillScanLineWithStack(x, y, mCurColor, bitmapCache.getPixel(x, y));
		/*
		 * setCanvasImgSrc(bitmapCache); mProgressDialog.dismiss();
		 */
		if (!ret)
			bitmapCache = bitmapCopy;// .copy(Bitmap.Config.ARGB_8888, true);
		if (bitmapCache != null) {
			mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_DOODLE_FINISH, bitmapCache));
		}
	}

	/*
	 * class canvasTouchListener implements OnTouchListener {
	 * 
	 * @Override public synchronized boolean onTouch(View v, MotionEvent event)
	 * { if (event.getAction() == MotionEvent.ACTION_DOWN) { //mProgressDialog =
	 * ProgressDialog.show( // DoodleActivity.this, null, "请稍候……        ", true,
	 * false);
	 * 
	 * int show_x,show_y,show_w,show_h,bm_w,bm_h; int
	 * touch_x,touch_y,imageView_w,imageView_h; double scale; BitmapDrawable
	 * bitmapDrawable = (BitmapDrawable) mCanvas.getDrawable(); //获取第一个图片显示框中的位图
	 * final Bitmap bitmap = bitmapDrawable.getBitmap();
	 * //bitmap图片实际大小与第一个ImageView的缩放比列
	 *//**
		 * 注：bitmap中解析出来的图片的宽高是以像素为单位的，
		 * 且直接从文件中解析出来（decodeResource）的图片的宽高是原来的像素值乘以了密度系统的，
		 * 而用createBitmap()创建的bitmap则直接就是指定的宽高
		 */
	@SuppressLint("HandlerLeak")
	/*
	 * bm_w = bitmap.getWidth(); bm_h = bitmap.getHeight(); imageView_w =
	 * mCanvas.getWidth(); imageView_h= mCanvas.getHeight(); Log.e("css",
	 * "22 imageView1(w,h)=("+imageView_w+","+imageView_h+")"); Log.e("css",
	 * "bitmap (w,h)=("+bm_w+","+bm_h+")"); if(bm_w < imageView_w && bm_h <
	 * imageView_h)//原始图片都小于imageview的大小，说明是放大 { if(imageView_w/bm_w <
	 * imageView_h/bm_h)//纵向缩放比例较大，则以横向放大比例为准，这是与缩小的区别所在 { scale =
	 * imageView_w/(double)bm_w; } else//横向缩放比例较大 { scale =
	 * imageView_h/(double)bm_h; } //获取需要显示的图片的开始点 show_w = (int) (bm_w*scale);
	 * show_h = (int) (bm_h*scale); } else //原始图片宽高到少有一个大于imageview宽高 {
	 * if(bm_w/imageView_w<bm_h/imageView_h)//纵向缩放比例较大 { scale =
	 * bm_h/(double)imageView_h; } else//横向缩放比例较大 { scale =
	 * bm_w/(double)imageView_w; } //获取需要显示的图片的开始点 show_w = (int) (bm_w/scale);
	 * show_h = (int) (bm_h/scale); } //double scale = bitmap.getHeight()/360.0;
	 * Log.e("css", "scale="+scale);
	 * 
	 * show_x = (imageView_w-show_w)/2; show_y = (imageView_h-show_h)/2; touch_x
	 * = (int) event.getX(); touch_y = (int) event.getY(); Log.e("css",
	 * "touch_x="+touch_x+",touch_y="+touch_y); Log.e("css",
	 * "show_w="+show_w+",show_h="+show_h+"show_x="+show_x+",show_y="+show_y);
	 * //在图片显示区域 if(touch_x>=show_x && touch_x<=show_x+show_w && touch_y>=show_y
	 * && touch_y<=show_y+show_h) { final int x; final int y;
	 * 
	 * if(scale>=1)//表明是放大了 { x = (int) ((touch_x-show_x)/scale); y = (int)
	 * ((touch_y-show_y)/scale); } else//缩小 { x = (int)
	 * ((touch_x-show_x)*scale); y = (int) ((touch_y-show_y)*scale); } //int
	 * temp = (int) (120*1.5);//120为imageView2的宽高（dp为单位）1.5为像素密度
	 * 
	 * //显示图片的指定区域 Log.i("css", "x="+x+",y="+y);
	 * 
	 * //不能直接用bitmap，否则填充后的图片会覆盖原图，相当于填充了原图 //bitmapCache =
	 * Bitmap.createBitmap(bitmap); //copy一份可修改(即mutable)的bitmap，不然默认的是不可修改的，
	 * 则setPixel会抛出IllegalStateException异常 bitmapCache =
	 * bitmap.copy(Bitmap.Config.ARGB_8888, true); Log.e("css",
	 * "bitmapCache="+bitmapCache+",bitmap="+bitmap); Log.e("css",
	 * "bitmapCache(w,h)=("+bitmapCache.getWidth()+","+bitmapCache.getHeight()+
	 * ")"); //addUndoData(new Rect(0, 0, bitmap.getWidth(),
	 * bitmap.getHeight()), bitmap);
	 * 
	 * fillBitmap(x,y); new Thread() { public void run() { fillBitmap(x,y);
	 * //填充前copy一份可修改(即mutable)的bitmap，以备恢复 Bitmap bitmapCopy =
	 * bitmapCache.copy(Bitmap.Config.ARGB_8888, true); FloodFillAlgorithm ffa =
	 * new FloodFillAlgorithm(bitmapCache); boolean ret =
	 * ffa.floodFillScanLineWithStack(x, y, mCurColor, bitmapCache.getPixel(x,
	 * y)); setCanvasImgSrc(bitmapCache); mProgressDialog.dismiss(); if(!ret)
	 * bitmapCache = bitmapCopy;//.copy(Bitmap.Config.ARGB_8888, true); if
	 * (bitmapCache != null) {
	 * mMainHandler.sendMessage(mMainHandler.obtainMessage( MSG_DOODLE_FINISH,
	 * bitmapCache)); } if(bitmapCache!=null && !bitmapCache.isRecycled()) {
	 * bitmapCache.recycle(); bitmapCache = null; } }; }.start(); }
	 * 
	 * } return true; }
	 * 
	 * }
	 */

	class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_DOODLE_FINISH:
				// setCanvasByBmp((Bitmap)msg.obj);
				// mProgressDialog.dismiss();
				mIsNeedSave = true;
				break;
			case MSG_SHOW_PROGRESS:
				mProgressBar.setVisibility(View.VISIBLE);
				mNextCanvasBtn.setEnabled(false);
				break;
			case MSG_HIDE_PROGRESS:
				mProgressBar.setVisibility(View.INVISIBLE);
				isDoodleViewInitEnd = true;
				mDoodleView.invalidate();
				playHintSnd();
				mNextCanvasBtn.setEnabled(true);
				break;
			case MSG_TIMER:
				if (playTipVoice && ++timerCount >= COUNT_SUM) {
					playTipVoice = false;
					playSnd("click_show.ogg", false, new MyOnCompleteListenner(1));
				}

				if (mouseAnimType == 0) {
					if (++mouseCount >= Constant.MOUSE_NORMAL_SUM) {
						mouseCount = 0;
					}
					mousePic = Constant.MOUSE_NORMAL_PIC + mouseCount;
					mouseView.setBackgroundResource(getPicId(DoodleActivity.this, mousePic));
				} else {
					if (mouseCount < Constant.MOUSE_CLICK_SUM) {
						mousePic = Constant.MOUSE_CLICK_PIC + mouseCount;
						mouseView.setBackgroundResource(getPicId(DoodleActivity.this, mousePic));
					}
					if (++mouseCount >= Constant.MOUSE_CLICK_SUM) {
						mouseCount = 0;
						mouseAnimType = 0;
					}
				}

				if (apMode == Constant.MODE_SILKWORM || apMode == Constant.MODE_LADYBUG) {
					if (showBtnCount == 0) {
						drawable0 = getResources().getDrawable(R.drawable.show0);
						drawable1 = getResources().getDrawable(R.drawable.show1);
						drawable2 = getResources().getDrawable(R.drawable.show2);
					}
					try {
						showBtnCount++;
						if (showBtnCount % 5 == 0) {
							StateListDrawable states = new StateListDrawable();
							states.addState(new int[] { android.R.attr.state_pressed }, drawable1);
							states.addState(new int[] { android.R.attr.state_focused }, drawable1);
							if (showBtnCount / 5 % 2 == 0) {
								states.addState(new int[] {}, drawable0);
							} else {
								states.addState(new int[] {}, drawable2);
							}
							mShowBtn.setBackgroundDrawable(states);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				break;
			}
		}
	}

	/**
	 * viewPager页面改变监听器
	 * 
	 * @author css
	 *
	 */
	class MyOnPagerChangeListenner implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int pageIdx) {

		}

	}

	/**
	 * 按钮等view的单击监听器
	 * 
	 * @author css
	 *
	 */
	class MyOnClickListenner implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (mIsForbidOp) {
				// Log.e(TAG, "ForbidOp!!!");
				return;
			}
			mIsNeedSave = mDoodleView.isDraw();
			if (v == mCloseBtn) {
				if (apMode == Constant.MODE_BABYPLAN || apMode == Constant.MODE_SILKWORM
						|| apMode == Constant.MODE_LADYBUG) {
					if (apMode == Constant.MODE_BABYPLAN) {
						playSnd("canceledit.ogg", false, new MyOnCompleteListenner(2));
					} else if (apMode == Constant.MODE_SILKWORM) {
						playSnd("sureexit.ogg", false, new MyOnCompleteListenner(2));
					}
					showCustomDialog();
					return;
				}
				if (mIsNeedSave && apMode == Constant.MODE_NORMAL) {
					mOperation = OPERATION.DOWN_CLOSE_BTN;
					playSnd("want_to_save.ogg", false, new MyOnCompleteListenner(2));
					showCustomDialog();
				} else {
					// SelectCanvasActivity.mIsNeedPauseBkSnd = false;
					finish();
					// ActivityManagerUtil.getInstance().exit();//退出整个程序
					overridePendingTransition(0, R.anim.slide_out_up);
				}
			} else if (v == mOpusBtn) {
				if (mIsNeedSave) {
					mOperation = OPERATION.DOWN_OPUS_BTN;
					playSnd("want_to_save.ogg", false, new MyOnCompleteListenner(2));
					showCustomDialog();
				} else {
					// SelectCanvasActivity.mIsNeedPauseBkSnd = false;
					Intent intent = new Intent(DoodleActivity.this, OpusSetActivity.class);
					launchForResult(intent, -1);
					finish();
					overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
				}
			} else if (v == mShowBtn) // 展示按钮
			{
				if (savaFile()) {
					if (mIsNeedSave)
						Reward.pointScore(DoodleActivity.this, SHOW_POINT); // 展示加分
					Intent intent = new Intent(DoodleActivity.this, OpusShowActivity.class);
					intent.putExtra("apMode", apMode);
					intent.putExtra("savePicPath", savePicPath);
					launchForResult(intent, -1);
					finish();
					/*
					 * overridePendingTransition(android.R.anim.slide_in_left,
					 * android.R.anim.slide_out_right);
					 */
				} else {
					return; // 保存图片失败，无操作
				}
			} else if (v == mUploadBtn) // 宝贝计划 上传图片
			{
				// 返回宝贝计划ap
				uploadToBaby();
			}
			/*
			 * else if(v == mHintBtn) {
			 * 
			 * }
			 */
			else if (v == mNextCanvasBtn) {
				if (mIsNeedSave) {
					mOperation = OPERATION.DOWN_NEXTCANVAS_BTN;
					playSnd("want_to_save.ogg", false, new MyOnCompleteListenner(2));
					showCustomDialog();
				} else {
					if (mPicIndex == Constant.TOTAL_CANVAS_EACH_AGE[mAge] - 1) {
						mPicIndex = 0;
					} else {
						mPicIndex++;
					}
					setTitleByIndex(mPicIndex);
					/*
					 * setCanvasByIndex(mPicIndex); setTitleByIndex(mPicIndex);
					 * //需要更新缓存图片 updateBitmapCache();
					 */

					/*
					 * Bitmap bm =
					 * DataManager.decodeBitmapFromAsset(getCanvasPathByIndex(
					 * mPicIndex),null); //Log.e(TAG, "1 bitmap="+bm); Bitmap bg
					 * =
					 * DataManager.decodeBitmapFromAsset(getCanvasBgPathByIndex(
					 * mPicIndex),null); //Log.e(TAG, "2 bitmap="+bg);
					 * mDoodleView.setTemp2(bg); mDoodleView.setTemp(bm);
					 */
					initDoodleViewByThread(mPicIndex);
				}
			} else if (v == mLastColorPageBtn) {
				int curPage, lastPage;
				curPage = mViewPager.getCurrentItem();
				// Log.e(TAG, "curPage="+curPage);
				if (curPage == 0) {
					// lastPage = Constant.DOODLE_COLOR_PAGES-1;
					return;
				} else
					lastPage = --curPage;
				// Log.e(TAG, "lastPage="+lastPage);
				setCurrentColorPage(lastPage);
			} else if (v == mNextColorPageBtn) {
				int curPage, nextPage;
				curPage = mViewPager.getCurrentItem();
				// Log.e(TAG, "curPage="+curPage);
				if (curPage == Constant.DOODLE_COLOR_PAGES - 1) {
					// nextPage = 0;
					return;
				} else
					nextPage = ++curPage;
				// Log.e(TAG, "nextPage="+nextPage);
				setCurrentColorPage(nextPage);
			} else if (v == mouseView) {
				if (mouseAnimType == 0) {
					mouseCount = 0;
					mouseAnimType = 1;
				}
			}

		}

	}

	/**
	 * 声音播放结束监听器
	 * 
	 * @author css
	 *
	 */
	class MyOnCompleteListenner implements OnCompletionListener {
		/** 0代表提示语 */
		private int flag;

		/**
		 * 构造函数
		 * 
		 * @param flag
		 *            0代表提示语
		 */
		public MyOnCompleteListenner(int flag) {
			this.flag = flag;
		}

		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mp == mMediaPlayer) {
				switch (flag) {
				case 0:
					mIsSndPause = false;
					break;
				case 1:

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
			if (name.indexOf(".png") != -1) {
				isImage = true;
			}
			return isImage;
		}
	}

	/**
	 * 根据图片名字获取图片的id
	 * 
	 * @param context
	 *            上下文
	 * @param picName
	 *            图片名字
	 * @return 返回图片id
	 */
	public static int getPicId(Context context, String picName) {
		int picId = context.getResources().getIdentifier(picName, "drawable", context.getPackageName());
		return picId;
	}
}
