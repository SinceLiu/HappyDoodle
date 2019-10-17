package com.readboy.Q.HappyDoodle.doodle;

import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.SelectCanvas.SelectCanvasActivity;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;
import com.readboy.Q.HappyDoodle.util.ScreenActionReceiver;
import com.readboy.Q.HappyDoodle.util.ScreenActionReceiver.ActionCallback;

import android.app.ReadboyDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 自定义的对话框，调用showDialog()显示对话框之前必须先调用initialize()初始化
 * lqn修改(2014.12.2)：由于flash会跳转改ap，需要把对话框改为不是静态变量，不然会出现问题
 * @author css
 * @version 1.0
 */
public class CustomDialog extends ReadboyDialog {
	private static final String TAG = "lqn-CustomDialog";
	/** “是的”按钮 */
	private Button yes_btn;
	/** “不要”按钮 */
	private Button no_btn;
	/** 提示文字 */
	private ImageView hintText;
	private static Context context;
	/** 触发按钮后的回调函数 */
	private static OnBtnClickCallback mCallback;
	/** 窗口类型 */
	private static DialogStyle mDialogStyle;
	/** 是否有焦点 */
	private static boolean mHasFocus;
	/** 屏幕事件广播接收器 */
	private static ScreenActionReceiver mScreenActionReceiver;

	/** 窗口类型 */
	public static enum DialogStyle
	{
		/** 询问是否保存对话框 */
		SAVE,
		/** 询问是否删除对话框 */
		DELETE,
		/** 询问是否退出宝贝计划涂鸦对话框 */
		EXIT_BABY,
		/** 询问是否退出蚕宝宝成长记对话框 */
		EXIT_SILKWORM,
		/** 询问是否退出瓢虫对话框 */
		EXIT_LADYBUG;
	}

	/**
	 * 初始化对话框
	 * @param context 上下文
	 * @param dialogStyle 对话框类型
	 * @param callback 点击按钮后的回调
	 */
	public static void initialize(Context context,DialogStyle dialogStyle,OnBtnClickCallback callback)
	{
		CustomDialog.context = context;
		mCallback = callback;
		mDialogStyle = dialogStyle;
		mHasFocus = false;

//		initScreenActionReceiver();
    }

	private static void initScreenActionReceiver() {
		mScreenActionReceiver = new ScreenActionReceiver(context, new ActionCallback()
		{
			@Override
			public void onActionCallback(String action) {
				if(HappyDoodleApp.DEBUG)
//					Log.e(TAG, "============onActionCallback========"+action);
                {
                    if(mHasFocus && action.equals(Intent.ACTION_SCREEN_ON))
                    {
                        SelectCanvasActivity.mIsNeedPauseBkSnd = true;
                        SelectCanvasActivity.resumeBkSnd();
                    }
                }
			}
		});
	}

	/**
	 * 显示对话框，并保证一次只弹出一个
	 */
	public static CustomDialog CreateDialog(Context context) {
        // There should be only one dialog running at a time.
		//Log.e(TAG, "dialog="+dialog);

    	CustomDialog dialog = new CustomDialog(context);
        //dialog.setCancelable(false);//不响应BACK键

        if(mScreenActionReceiver == null) {
            initScreenActionReceiver();
        }

        mScreenActionReceiver.registerScreenActionReceiver();
        dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				//Log.e(TAG, "============onDismiss========");
				if(mScreenActionReceiver != null)
	            {
		            mScreenActionReceiver.unRegisterScreenActionReceiver();
		            mScreenActionReceiver = null;
	            }
			}
		});

        dialog.getWindow().setWindowAnimations(R.style.dialog_down_slide_style);

        Window mWindow = dialog.getWindow();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)//Android5.0
//			mWindow.addPrivateFlags(0x80000000);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)//Android4.4
        {
            mWindow.setFlags(0x80000000, 0x80000000);
        } else {
            mWindow.setFlags(0x02000000, 0x02000000);
        }

        Log.v(TAG, "------------dialog = "+dialog);
//        dialog.show();
        return dialog;
    }

	/**
	 * 私有构造函数
	 */
	private CustomDialog(Context mContext) {
		super(context,R.style.AskSaveDialog);
		context = mContext;
		View view = View.inflate(context, R.layout.ask_dialog, null);
		addContentView(view, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		getWindow().setGravity(Gravity.TOP|Gravity.FILL_HORIZONTAL);
//		getWindow().getAttributes().systemUiVisibility |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

		yes_btn = (Button) view.findViewById(R.id.yesBtn);
		no_btn = (Button) view.findViewById(R.id.noBtn);
		hintText = (ImageView) view.findViewById(R.id.hintText);
		if(mDialogStyle == DialogStyle.SAVE) {
            hintText.setImageResource(R.drawable.save_hint);
        } else if(mDialogStyle == DialogStyle.DELETE) {
            hintText.setImageResource(R.drawable.delete_hint);
        } else if(mDialogStyle == DialogStyle.EXIT_BABY) {
            hintText.setImageResource(R.drawable.exitbaby_hint);
        } else if(mDialogStyle == DialogStyle.EXIT_SILKWORM) {
            hintText.setImageResource(R.drawable.exit_silkworm);
        } else if(mDialogStyle == DialogStyle.EXIT_LADYBUG) {
            hintText.setImageResource(R.drawable.exit_silkworm);
        }

		MyOnClickListenner clickListenner = new MyOnClickListenner();
		yes_btn.setOnClickListener(clickListenner);
		no_btn.setOnClickListener(clickListenner);
	}


	/**
     * 判断所属activity是否已经暂停了
     * @return 暂停返回true，否则false
     */
    private boolean isOwnerActPause()
    {
    	boolean ret = false;
    	if(context instanceof DoodleActivity)
		{
			if(((DoodleActivity)context).getHasPause())
			{
				//Log.e(TAG, "========DoodleActivity hasPause=========");
				ret = true ;
			}
		}
		else if(context instanceof OpusSetActivity)
		{
			if(((OpusSetActivity)context).getHasPause())
			{
				//Log.e(TAG, "========OpusSetActivity hasPause=========");
				ret = true ;
			}
		}

    	return ret;
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		//Log.e(TAG, "============onBackPressed========");
		mCallback.onBtnClick(null, "no_btn");
	}


	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(HappyDoodleApp.DEBUG)
//			Log.e(TAG, "------onWindowFocusChanged------hasFocus="+hasFocus);
        {
            if(hasFocus)
            {
                mHasFocus = true;
                SelectCanvasActivity.mIsNeedPauseBkSnd = true;
                if(!isOwnerActPause()) {
                    SelectCanvasActivity.resumeBkSnd();
                }
            }
            else
            {
                mHasFocus = false;
            }
        }

		super.onWindowFocusChanged(hasFocus);
	}


	/**
     * 按钮等view的单击监听器
     * 注：因为dialog本身继承了一个DialogInterface接口，而改接口中有同样名字的OnClickListener()方法，
     * 所以需要全限定名
     * @author css
     *
     */
    class MyOnClickListenner implements android.view.View.OnClickListener
    {

		@Override
		public void onClick(View v) {
			if(v == yes_btn)
			{
				mCallback.onBtnClick(v, "yes_btn");
			}
			else if(v == no_btn)
			{
				//savaFile();
				mCallback.onBtnClick(v, "no_btn");
			}


		}

    }

    /**
     * 按钮回调
     * @author css
     *
     */
    public interface OnBtnClickCallback
    {
    	/**
    	 * 点击按钮后的回调函数
    	 * @param view 对应按钮对象
    	 * @param btnName 按钮名字，暂时定义的，“yes_btn”代表“是的”按钮，“no_btn”代表“不要”按钮
    	 */
    	void onBtnClick(View view,String btnName);
    }

}
