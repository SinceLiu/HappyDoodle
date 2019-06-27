package com.readboy.Q.HappyDoodle.data;


import android.content.Context;
import android.util.Log;

/**
 * 原始资源集合类，包括声音和图片的ID定义，并提供获取方法，一般供DataManager调用
 * @author css
 * @version 1.0
 */
public class DataSource {
	
	private static final String SND_DIR = "sound/";
	/**
	 * 对应 arrays.xml中定义的声音文件数组sndFilePath
	 */
	//private final String[] mSndPath;
	
	
	/**
	 * 构造函数，获取原始资源
	 * @param context 上下文
	 */
	public DataSource(Context context) {
		// TODO Auto-generated constructor stub
		
	}
	
	/**
	 * 根据声音ID获取保存在mSndPath数组中对应的声音路径
	 * @param idSnd 声音ID
	 * @return 声音路径
	 */
	public String getSndPath(int idSnd) {
		String sndPath = null;
		
		Log.d("=css", sndPath);
		return sndPath;
	}
}
