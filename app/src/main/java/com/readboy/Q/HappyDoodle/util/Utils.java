/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.readboy.Q.HappyDoodle.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * Class containing some static utility methods.
 */
public class Utils {
	public static final String TAG = "lqn-Utils";
    public static final int IO_BUFFER_SIZE = 8 * 1024;
    /**
	 * 按钮等上次点击的时间，用于防止快速点击
	 */
	private static long lastClickTime;

    private Utils() {};

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressLint("NewApi")
    public static long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        
    }

    /**
     * Get the memory class of this device (approx. per-app memory limit)
     *
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     *
     * @return
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if ActionBar is available.
     *
     * @return
     */
    public static boolean hasActionBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    
    /**
     * 用于防止快速点击操作
     * @return 如果两次点击间隔时间小于设定值，则返回true，否则false
     */
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 800) {   
            return true;   
        }   
        lastClickTime = time;   
        return false;   
    }
    
    /**
     * 打印当前运行的任务栈
     * @param context 上下文
     */
    public static void printTaskInfo(Context context)
    {
    	ActivityManager manager = (ActivityManager) context
    		    .getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningTaskInfo> runningtasks = manager.getRunningTasks(1);
    	Log.e(TAG, "size="+runningtasks.size());
    	for(int i=0;i<runningtasks.size();i++)
    	{
    		RunningTaskInfo rti = runningtasks.get(i);
    		Log.e(TAG, "taskId="+rti.id+",numActivities="+rti.numActivities);
    	}
    }
    
    /**
	 * 通知媒体存储服务扫描新生产的文件
	 * @param context 上下文
	 * @param file 要扫描的目标文件对象
	 */
	public static void requestScanFileForAdd(Context context, File file)
	{
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
		intent.setData(Uri.fromFile(file));
		context.sendBroadcast(intent);
	}
	
	/**
	 * 通知媒体存储服务扫描删除的文件
	 * @param context 上下文
	 * @param file 要删除的目标文件对象
	 * @param mediaType 媒体类型，0代表图片，1代表音频，2代表视频
	 */
	public static void requestScanFileForDelete(Context context, File file, int mediaType)
	{
		Uri uri = null;
		String data = null;
		if(mediaType == 0)
		{
			uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			data = MediaStore.Images.Media.DATA;
		}
		else if(mediaType == 1)
		{
			uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			data = MediaStore.Audio.Media.DATA;
		}
		else if(mediaType == 2)
		{
			uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			data = MediaStore.Video.Media.DATA;
		}
		else 
		{
			Log.e("css", "error mediaType="+mediaType);
		}
		
		if(uri != null && file != null)
		{
			context.getContentResolver().delete(uri, 
					data+"=\""+file.getAbsolutePath()+"\"", null);
		}
	}

    public static int dip2px(Context context, int dipValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
