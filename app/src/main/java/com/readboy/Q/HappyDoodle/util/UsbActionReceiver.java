package com.readboy.Q.HappyDoodle.util;

import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;

import android.app.ReadboyActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReadboyBroadcastReceiver;
import android.util.Log;

public class UsbActionReceiver extends ReadboyBroadcastReceiver 
{  
	private static final String TAG = "lqn-UsbActionReceiver";
    private boolean isRegisterReceiver = false;  
  
    @Override  
    public void onArrive(Context context, Intent intent) 
    {  
        String action = intent.getAction();  
        if (action.equals(Intent.ACTION_USER_PRESENT)) 
        {  
//        	if(mp != null && isPause)
//    		{
//    			mp.start();
//    			isPause = false;
//    		}
        	Log.d(TAG, "ACTION_USER_PRESENT");
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
        	Log.d(TAG, "ACTION_SCREEN_ON");
        }
        else if (action.equals(Intent.ACTION_SCREEN_OFF))
        {
			Log.d(TAG, "ACTION_SCREEN_OFF");
		}
        else if (action.equals(Intent.ACTION_MEDIA_SHARED))
        {
        	Log.e(TAG, "==================  ACTION_MEDIA_SHARED");
        	Intent i = new Intent(context, SelectCanvasActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
            	((ReadboyActivity)context).launchForResult(i, -1);
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "unable to start SelectCanvasActivity activity", e);
            }
		}
    }  
  
    /**
     * 注册广播
     * @param mContext
     */
    public void registerScreenActionReceiver(Context mContext) 
    {  
        if (!isRegisterReceiver) 
        {  
        	Log.e(TAG, "===registerScreenActionReceiver");
            isRegisterReceiver = true;  	  
            IntentFilter filter = new IntentFilter();  
           // filter.addAction(Intent.ACTION_SCREEN_OFF); //屏幕休眠
           // filter.addAction(Intent.ACTION_SCREEN_ON);  //点亮屏幕
           // filter.addAction(Intent.ACTION_USER_PRESENT);  //解锁
            filter.addAction(Intent.ACTION_MEDIA_SHARED);//USB大容量存储打开
            filter.addDataScheme("file");
            mContext.registerReceiver(UsbActionReceiver.this, filter);  
        }  
    }  
  
    /**
     * 注销广播
     * @param mContext
     */
    public void unRegisterScreenActionReceiver(Context mContext) 
    {  
        if (isRegisterReceiver) 
        {  
            isRegisterReceiver = false;  
            mContext.unregisterReceiver(UsbActionReceiver.this);  
        }  
    }	  
}
