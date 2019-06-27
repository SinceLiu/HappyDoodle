package com.readboy.Q.HappyDoodle.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class ToastUtils {
	private static Toast toast;
	private static Handler handler = new Handler();
	
	private static Runnable run = new Runnable() {
		@Override
		public void run() {
			toast.cancel();
		}
	};
	
	private static void toast(Context context, CharSequence msg, int duration) {
		handler.removeCallbacks(run);
		switch (duration) {
		case Toast.LENGTH_SHORT:
			duration = 1000;
			break;
		case Toast.LENGTH_LONG:
			duration = 3000;
			break;
		default:
			break;
		}
		if(null != toast){
			toast.setText(msg);
		}else {
			toast = Toast.makeText(context, msg, duration);
		}
		handler.postDelayed(run, duration);
		toast.show();
	}
	
	public static void show(Context context, CharSequence msg, int duration) {
		try {
			toast(context, msg, duration);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void show(Context context, int resId, int duration) {
			try {
				toast(context, context.getResources().getString(resId), duration);				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
}
