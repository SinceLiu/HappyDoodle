package com.readboy.Q.HappyDoodle;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.readboy.Q.HappyDoodle.data.DataManager;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

public class HappyDoodleApp extends Application {
	private static final String TAG = "lqn-HappyDoodleApp";
	public static final boolean DEBUG = true;
	private static int mWidth;
	/** 资源管理对象 */
	private DataManager dataManager;
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		//com.readboy.encrypt.Encrypt.macCheck();
		initImageLoader(getApplicationContext());   
		//初始化资源管理对象，注意这里传的参数是应用程序的上下文
        dataManager = DataManager.getDataManager(getApplicationContext());
		Resources resources = this.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		float density = dm.density;
		mWidth = dm.widthPixels;
	}

	public static int getScreenWidth(){
		return mWidth;
	}
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 1)
				.denyCacheImageMultipleSizesInMemory()
				.memoryCache(new WeakMemoryCache())
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				//.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		ImageLoader.getInstance().clearDiscCache();
		ImageLoader.getInstance().clearMemoryCache();
	}
	
	public DataManager getDataManager() {
		return dataManager;
	}
}
