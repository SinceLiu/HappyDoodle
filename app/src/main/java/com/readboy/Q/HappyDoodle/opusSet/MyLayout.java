package com.readboy.Q.HappyDoodle.opusSet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * 作品集界面的父布局
 * 功能：重写拦截触摸方法
 * @author QZL
 * @version 1.0
 */
public class MyLayout extends LinearLayout
{
	private static final String TAG = "lqn-MyLayout";
	/** 是否弹出了删除作品对话框 */
	private boolean isPopupDialog;
	
	private Context context;
	
	
	/**
	 * 构造
	 * @param context ：上下文
	 * @param attrs ：xml属性
	 */
	public MyLayout(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		this.context = context;
	}
	
	/**
	 * 填充完毕
	 */
	@Override
	protected void onFinishInflate() 
	{
		
	}

	/**
	 * 拦截触摸事件
	 * @return 返回true表示拦截touch消息，子视图就接收不到了，而本身的{@link #onTouchEvent(MotionEvent)}
	 * 回调还能收到；返回false表示不拦截
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) 
	{
		//在当前onTouchEvent内处理点击
		if (isPopupDialog) 
		{
			return true;
		}
		else 
		{
			return false;
		}
		
	}
	
	/**
	 * 触摸事件处理
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
//		Log.v(TAG, "onTouchEvent event,devID="+event.getDeviceId()+",Flag="+event.getFlags()+"act="+event.getAction());
		if (isPopupDialog) 
		{
			if (event.getAction() == MotionEvent.ACTION_MOVE) 
			{
				
			}
			if (event.getAction() ==  MotionEvent.ACTION_UP) 
			{
				isPopupDialog = false;
			}
		}
		return true;
	}
	
	/**
	 * 设置是弹出了对话框，弹出对话框后拦截掉viewpager的touch消息，从而避免其再滑动
	 * @param isPopupDialog
	 */
	public void setIsPopupDialog(boolean isPopupDialog)
	{
		this.isPopupDialog = isPopupDialog;
		
	}
}
