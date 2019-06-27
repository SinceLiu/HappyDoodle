package com.readboy.Q.HappyDoodle.SelectCanvas;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * 选择界面的父布局
 * 功能：重写拦截触摸方法
 * @author QZL
 * @version 1.0
 */
public class MyLayout extends LinearLayout
{
	private static final String TAG = "lqn-MyLayout";
	/** 是否拦截消息 */
	private boolean isIntercept;
	
	private Context context;
	
	
	/**
	 * 构造函数
	 * @param context ：上下文
	 * @param attrs ：xml属性
	 */
	public MyLayout(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		this.context = context;
		// TODO Auto-generated constructor stub
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
		if (isIntercept) 
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
		//Log.i(TAG, "onTouchEvent!!!!");
		/*if (isIntercept) 
		{
			if (event.getAction() == MotionEvent.ACTION_MOVE) 
			{
				
			}
			if (event.getAction() ==  MotionEvent.ACTION_UP) 
			{
				
			}
		}*/
		return true;
	}
	
	/**
	 * 设置是否拦截touch消息，从而避免其再滑动
	 * @param isIntercept 是否拦截
	 */
	public void setIsIntercept(boolean isIntercept)
	{
		this.isIntercept = isIntercept;
		
	}
}
