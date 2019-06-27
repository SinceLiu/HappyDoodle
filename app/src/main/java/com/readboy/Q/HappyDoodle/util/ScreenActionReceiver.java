package com.readboy.Q.HappyDoodle.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * 自定义的有关屏幕事件的广播接收器
 * @author css
 * @version 1.0
 */
public class ScreenActionReceiver extends BroadcastReceiver 
{  
	private static final String TAG = "lqn-ScreenActionReceiver";
	private final String SYSTEM_DIALOG_REASON_KEY = "reason";
	private final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
	private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
	private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

    private boolean isRegisterReceiver = false;  
    private Context mContext;
    /** 接收到广播后的回调函数 */
    private ActionCallback mActionCallback;
    
    public ScreenActionReceiver(Context context,ActionCallback actionCallback) {
    	mContext = context;
    	mActionCallback = actionCallback;
	}
  
    @Override  
    public void onReceive(Context context, Intent intent) 
    {  
        String action = intent.getAction();  
        if (action.equals(Intent.ACTION_USER_PRESENT)) 
        {  
//        	if(mp != null && isPause)
//    		{
//    			mp.start();
//    			isPause = false;
//    		}
//        	Log.d(TAG, "ACTION_USER_PRESENT");
        } 
        else if (action.equals(Intent.ACTION_SCREEN_ON)) 
        {  
//        	KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
//        	if(!keyguardManager.inKeyguardRestrictedInputMode()) //没有设置锁屏
//        	{
//        		if(mp != null && isPause)
//	    		{
//	    			mp.start();
//	    			isPause = false;
//	    		}
//        	}
//        	Log.d(TAG, "ACTION_SCREEN_ON");
        }
        else if (action.equals(Intent.ACTION_SCREEN_OFF))
        {
//			Log.d(TAG, "ACTION_SCREEN_OFF");
		}
        /*else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) 
        {
			String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
			if (reason != null) 
			{
				Log.e(TAG, "action:" + action + ",reason:" + reason);
				if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) 
				{
					// home键  
				} else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) 
				{
					// 长按home键  
				}
			}
		}*/
        
        mActionCallback.onActionCallback(action);
    }  
  
    /**
     * 注册广播
     * @param mContext
     */
    public void registerScreenActionReceiver() 
    {  
        if (!isRegisterReceiver) 
        {  
//        	Log.e(TAG, "===registerScreenActionReceiver");
            isRegisterReceiver = true;  	  
            IntentFilter filter = new IntentFilter();  
           // filter.addAction(Intent.ACTION_SCREEN_OFF); //屏幕休眠
            filter.addAction(Intent.ACTION_SCREEN_ON);  //点亮屏幕
           // filter.addAction(Intent.ACTION_USER_PRESENT);  //解锁
            /*filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);	//关闭系统对话框，用于检测home键
            filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);*/
            
            mContext.registerReceiver(ScreenActionReceiver.this, filter);  
        }  
    }  
  
    /**
     * 注销广播
     * @param mContext
     */
    public void unRegisterScreenActionReceiver() 
    {  
        if (isRegisterReceiver) 
        {  
            isRegisterReceiver = false;  
            mContext.unregisterReceiver(ScreenActionReceiver.this);  
        }  
    }
    
    /**
     * 响应广播的回调
     * @author css
     *
     */
    public interface ActionCallback
    {
    	/**
    	 * 接到广播后的回调函数
    	 * @param action 广播名
    	 */
    	void onActionCallback(String action);
    }
}
