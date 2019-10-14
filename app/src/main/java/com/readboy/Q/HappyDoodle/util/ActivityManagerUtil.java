package com.readboy.Q.HappyDoodle.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.util.Log;

/**
 * 全部Activity管理类，用于一次性退出所有activity
 * 使用方法：在每个Activity的onCreate方法中调用ActivityManagerUtil的addActivity方法，
 * 然后在需要使用一键退出的地方调用ActivityManagerUtil中的exit方法即可。
 * @author css
 *
 */
public class ActivityManagerUtil {
	 /**
     * Activity列表，务必使用弱引用，不然会造成activity泄漏，导致资源不能释放
     */
	private ArrayList<WeakReference<Activity>> mActivityList = new ArrayList<WeakReference<Activity>>();
	//private ArrayList<Activity> mActivityList = new ArrayList<Activity>();
	 /**
     * 全局唯一实例
     */
	private static ActivityManagerUtil mInstance;
	
	/**
     * 该类采用单例模式，不能被外部实例化
     */
	private ActivityManagerUtil() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 获取类实例对象，注，此方法是线程不安全的，多线程访问情况下有可能会new出多个实例（加锁或给该类采用静态内部类实现可解决），
	 * 现只有自己用到，且是在单线程下的，所以没改
	 * @return ActivityManagerUtil
	 */
	public static ActivityManagerUtil getInstance() {
		if(null == mInstance) {
            mInstance = new ActivityManagerUtil();
        }
		return mInstance;
	}
	
	/**
	 * 保存Activity到现有列表中
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		
		if(null != activity)
		{
			//Log.e("css==", "add:"+activity.getLocalClassName()+",taskID="+activity.getTaskId());
			WeakReference<Activity> actRef = new WeakReference<Activity>(activity);
			mActivityList.add(actRef);
			
			//mActivityList.add(activity);
		}
	}
	
	/**
     * 关闭保存的Activity
     */
	public void exit() 
	{
		if(null != mActivityList)
		{
			for (WeakReference<Activity> activityRef : mActivityList) 
			//for (Activity activity : mActivityList) 
			{
				Activity activity = activityRef.get();
				//Log.d("css==", "activity="+activity);
				if(null != activity && !activity.isFinishing())
				{
					//Log.d("css==", "exit:"+activity.getLocalClassName());
					activity.finish();
				}
				
				activity = null;
			}
		}
		
		System.exit(0);
	}
}
