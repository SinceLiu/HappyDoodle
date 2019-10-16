package com.readboy.Q.HappyDoodle.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;



import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.util.Log;

/**
 * 资源管理单例类，注意一定要保证在app运行后马上实例化本类，因为这里用到了很多静态方法
 * @author css
 *
 */
public class DataManager {
	private static final String TAG = "lqn-DataManager";
	private static Context context;
	private static DataManager mDataManager;
	private static AssetManager assetManager;
	private SoundPool soundPool; 
	/** 当前用soundpool播放的声音的StreamID */
	private int mCurSndPoolStreamID;
	//private HashMap<DataSource.ID_SND, Integer> soundPoolMap;
	//private MediaPlayer mediaPlayer;
	private DataSource mDataSource;
	
	/**
	 * 私有构造函数实现单例类
	 * @param context
	 */
	private DataManager(Context context) {
		this.context = context;
		
		assetManager = context.getAssets();
		//mediaPlayer = new MediaPlayer();
		//初始化资源
		mDataSource = new DataSource(context);
		
		//initSoundPool();
	}
	
	/**
	 * 用静态方法提供给外部类调用获取资源管理对象
	 * @param context 注：每次调用时传的context不一样可能会有点问题，还未深究，所以建议使用应用程序上下文
	 * 作为参数，activity中可通过getApplicationContext()获得
	 * @return 资源管理对象
	 */
	public static DataManager getDataManager(Context context) {
    	if (mDataManager == null) {
			mDataManager = new DataManager(context);
		}
    	//DataManager.context = context;
		return mDataManager;
	}
	
	/**
	 * 使用MediaPlay播放声音函数
	 * @param idSnd 声音ID
	 * @param isLoop 是否循环播放
	 * @param completionListener 播放结束监听器，不监听则传null
	 * @return mp 返回播放该声音的MediaPlay对象
	 */
	public MediaPlayer playSound(int idSnd,boolean isLoop,OnCompletionListener completionListener) 
	{
		MediaPlayer mp = null;
		try 
		{	
			String sndPath = mDataSource.getSndPath(idSnd);
			if(sndPath == null || "".equals(sndPath))
			{
				Log.e(TAG, "sndPath is empty");
				return mp;
			}
			mp = new MediaPlayer();
			
			mp.reset();//一定要重置，不然第二次来就会抛异常了
			//打开指定的声音文件
			AssetFileDescriptor afd = assetManager.openFd(sndPath);
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			//准备声音
			mp.prepare();
			//播放
			mp.start();
			//设置是否循环播放
			mp.setLooping(isLoop);
			//设置监听器
			if(completionListener != null) {
				mp.setOnCompletionListener(completionListener);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mp;
	}
	
	/**
	 * 使用MediaPlay播放声音函数
	 * @param sndPath 声音在assert文件夹下的路径
	 * @param isLoop 是否循环播放
	 * @param completionListener 播放结束监听器，不监听则传null
	 * @return mp 返回播放该声音的MediaPlay对象
	 */
	public static MediaPlayer playSound(String sndPath,boolean isLoop,OnCompletionListener completionListener) 
	{
		MediaPlayer mp = null;
		try 
		{	
			//String sndPath = mDataSource.getSndPath(idSnd);
			if(sndPath == null || "".equals(sndPath))
			{
				Log.e(TAG, "sndPath is empty");
				return mp;
			}
			mp = new MediaPlayer();
			
			mp.reset();//一定要重置，不然第二次来就会抛异常了
			//打开指定的声音文件
			AssetFileDescriptor afd = assetManager.openFd(Constant.SND_DIR+sndPath);
			mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
			//准备声音
			mp.prepare();
			//播放
			mp.start();
			//设置是否循环播放
			mp.setLooping(isLoop);
			//设置监听器
			if(completionListener != null) {
				mp.setOnCompletionListener(completionListener);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mp;
	}
	
	/*private int loadSoundFromAssert(DataSource.ID_SND idSnd) throws IOException 
	{
		int ret = 1;
		String sndPath = mDataSource.getSndPath(idSnd);
		if(sndPath == null || sndPath.equals(""))
		{
			Log.e(TAG, "sndPath is empty");
			return ret;
		}
		
		
		AssetFileDescriptor afd = assetManager.openFd(sndPath);
		ret = soundPool.load(afd, 1);
		
		return ret;
	}
	
	public boolean initSoundPool() 
	{
		soundPool = new SoundPool(15, AudioManager.STREAM_MUSIC, 1);  
	    soundPoolMap = new HashMap<DataSource.ID_SND, Integer>(); 
	    try 
	    {
	    	soundPoolMap.put(ID_SND.mhwcs_tx1, loadSoundFromAssert(ID_SND.mhwcs_tx1));
			soundPoolMap.put(ID_SND.mhwcs_tx5, loadSoundFromAssert(ID_SND.mhwcs_tx5));
			soundPoolMap.put(ID_SND.mhwcs_tx3, loadSoundFromAssert(ID_SND.mhwcs_tx3));
			soundPoolMap.put(ID_SND.mhwcs_tx6, loadSoundFromAssert(ID_SND.mhwcs_tx6));
			soundPoolMap.put(ID_SND.mhwcs_tx7, loadSoundFromAssert(ID_SND.mhwcs_tx7));
			
			soundPoolMap.put(ID_SND.mhw_yy7, loadSoundFromAssert(ID_SND.mhw_yy7));
			soundPoolMap.put(ID_SND.mhw_yy8, loadSoundFromAssert(ID_SND.mhw_yy8));
			soundPoolMap.put(ID_SND.mhw_yy9, loadSoundFromAssert(ID_SND.mhw_yy9));
			soundPoolMap.put(ID_SND.mhw_yy10, loadSoundFromAssert(ID_SND.mhw_yy10));
			soundPoolMap.put(ID_SND.mhw_yy11, loadSoundFromAssert(ID_SND.mhw_yy11));
			
			soundPoolMap.put(ID_SND.mhw_yy12, loadSoundFromAssert(ID_SND.mhw_yy12));
			soundPoolMap.put(ID_SND.mhw_yy13, loadSoundFromAssert(ID_SND.mhw_yy13));
			soundPoolMap.put(ID_SND.mhw_yy14, loadSoundFromAssert(ID_SND.mhw_yy14));
			soundPoolMap.put(ID_SND.mhw_yy15, loadSoundFromAssert(ID_SND.mhw_yy15));
			soundPoolMap.put(ID_SND.mhw_yy16, loadSoundFromAssert(ID_SND.mhw_yy16));
			
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public void playSoundPool(DataSource.ID_SND idSnd) {  
	    AudioManager mgr = (AudioManager) context.getSystemService(  
	            Context.AUDIO_SERVICE);  
	    int streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);  
	    mCurSndPoolStreamID = soundPool.play(soundPoolMap.get(idSnd), streamVolume, streamVolume, 1,  
	            0, 1f);
	}*/
	
	/**
	 * 暂停所有soundPool声音
	 */
	public void pauseAllSoundPool() {
		soundPool.autoPause();
	}
	
	/**
	 * 恢复所有soundPool声音
	 */
	public void resumeAllSoundPool() {
		soundPool.autoResume();
	}
	
	public void releaseSoundPool() {
		soundPool.release();
		soundPool = null;
	}
	
	/**
	 * 从assert文件夹中解析一张图片
	 * @param bmpPath 图片路径
	 * @return 成功返回图片bitmap对象，出错返回null
	 */
	public static Bitmap decodeBitmapFromAsset(String bmpPath,Config bitmapConfig) {
		Bitmap bitmap = null;
		if(bmpPath == null || "".equals(bmpPath))
		{
			Log.e(TAG, "bmpPath is empty");
			return bitmap;
		}
		//Log.e(TAG, "bmpPath="+bmpPath);
		try 
		{
			Options options = new Options();
			options.inPreferredConfig = bitmapConfig==null?Config.ARGB_8888:bitmapConfig;
			//AssetFileDescriptor afd = assetManager.openFd(bmpPath);
			InputStream is = assetManager.open(bmpPath);//流的方式打开貌似可以避免内存溢出问题
			bitmap = BitmapFactory.decodeStream(is,null,options);
			//Log.e(TAG, "bitmap="+bitmap);
			//bitmap = BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	/**
	 * 从assert文件夹中解析一张图片
	 * @param bmpPath 图片路径
	 * @return 成功返回图片bitmap对象，出错返回null
	 */
	public static Bitmap decodeBitmapFromPath(String bmpPath,Config bitmapConfig) {
		Bitmap bitmap = null;
		if(bmpPath == null || "".equals(bmpPath))
		{
			Log.e(TAG, "bmpPath is empty");
			return bitmap;
		}
		//Log.e(TAG, "bmpPath="+bmpPath);
		try 
		{
			Options options = new Options();
			options.inPreferredConfig = bitmapConfig==null?Config.ARGB_8888:bitmapConfig;
//			InputStream is = assetManager.open(bmpPath);//流的方式打开貌似可以避免内存溢出问题
			InputStream is = new FileInputStream(bmpPath);
			
			bitmap = BitmapFactory.decodeStream(is,null,options);
			//Log.e(TAG, "bitmap="+bitmap);
			//bitmap = BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
	
	public AssetManager getAssetManager() {
		return assetManager;
	}
	
	/*public MediaPlayer getMediaPlayer() {
		return mediaPlayer;
	}*/
	
	public SoundPool getSoundPool() {
		return soundPool;
	}
	
	/*public HashMap<DataSource.ID_SND, Integer> getSoundPoolMap() {
		return soundPoolMap;
	}*/
	
	public int getmCurSndPoolStreamID() {
		return mCurSndPoolStreamID;
	}
}
