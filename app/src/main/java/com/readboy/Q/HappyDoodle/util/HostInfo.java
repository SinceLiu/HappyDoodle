package com.readboy.Q.HappyDoodle.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;

public class HostInfo {
	public static String getHostQ2() {
		return "http://mall.readboy.com:12680/rbq2/toAndroid.php";
	}

	public static String getHostQ2Baby() {
		return "http://mall.readboy.com:12680/rbBabyFile/toAndroid.php";
	}
	
	public static String getHostQ5() {
		return "http://mall.readboy.com:12680/q5/toAndroid.php";
	}
	
	public static String getHostG100() {
		return "http://mall.readboy.com:12680/rbg100/toAndroid.php";
	}
	
	public static String getHostG200() {
		return "http://mall.readboy.com:12680/rbg200/toAndroid.php";
	}
	
	public static String getHostG30() {
		return "http://mall.readboy.com:12680/rbg30/toAndroid.php";
	}
	
	public static String getHostG18A33() {
		return "http://mall.readboy.com:12680/rbg18a33/toAndroid.php";
	}
	
	public static String getHostF300() {
		return "http://mall.readboy.com:12680/rbf300/toAndroid.php";
	}
	
	public static String getHostP100() {
		return "http://mall.readboy.com:12680/rbp100/toAndroid.php";
	}
	
	public static String getHostT100() {
		return "http://mall.readboy.com:12680/rbt100/toAndroid.php";
	}
	
	/**
	 * 老商城：G11、G12、G50、P50
	 * @return
	 */
	public static String getHostOld() {
		return "http://apk.readboy.com:12680/toAndroid.php";
	}
	
	/**
	 * 完整地址拼接
	 * @param host 对应机型的host
	 * @param packageName 要更新的包名
	 * @return 拼接后的完整地址，有打印tag:GetHttp
	 */
	public static String getHttp(String host, String packageName) {
		String http = host; // + "?mode=AutoUpdateAPK&package=" + packageName; // 完整地址(服务器拼装了)
		Log.i("GetHttp", "http = " + http);
		return http;
	}
	
	/**
	 * 完整地址拼接，此方法能过获取机器型号来拼接，如果同一型号有不同固件，请慎用，如：G50有老版本（NDK）和新版本（纯Android）
	 * @param context
	 * @return
	 */
	public static String getHttp(Context context) {
		// host
		String host = "";
		
		// 获取包名
		String packageName = context.getPackageName();
		
		// 获取设备信息
		String deviceInfo = Build.MODEL;
		// 获取出错
		if (deviceInfo.length() < 1) {
			return "";
		}
		// 转化成小写
		deviceInfo = deviceInfo.toLowerCase();
		
		// 判断机型
		if (deviceInfo.contains("q2baby")) {
			host = getHostQ2Baby(); 
		} else if (deviceInfo.contains("q2")) {
			host = getHostQ2();
		} else if (deviceInfo.contains("q5")) {
			host = getHostQ5();
		} else if (deviceInfo.contains("g100")) {
			host = getHostG100();
		} else if (deviceInfo.contains("g200")) {
			host = getHostG200();
		} else if (deviceInfo.contains("g30")) {
			host = getHostG30();
		} else if (deviceInfo.contains("g18")) {
			host = getHostG18A33();
		} else if (deviceInfo.contains("f300")) {
			host = getHostF300();
		} else if (deviceInfo.contains("p100")) {
			host = getHostP100();
		} else if (deviceInfo.contains("t100")) {
			host = getHostT100();
		} else if (deviceInfo.contains("g11")) {
			host = getHostOld();
		} else if (deviceInfo.contains("g12")) {
			host = getHostOld();
		} else if (deviceInfo.contains("g50")) {
			host = getHostOld();
		} else if (deviceInfo.contains("p50")) {
			host = getHostOld();
		}
		
		return getHttp(host, packageName);
	}
}
