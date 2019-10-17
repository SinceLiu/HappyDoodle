package com.readboy.Q.HappyDoodle.SelectCanvas;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.util.Utils;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MyGridViewAdapter extends BaseAdapter {

    private static final String TAG = "lqn-MyGridViewAdapter";

    /**
     * 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁
     */
    private int mAge = 0;
    /**
     * 有多少页，默认3页
     */
    private int mPageCount;
    /**
     * 当前是第几页
     */
    private int mPageIndex;
    /**
     * 当前页有多少item
     */
    private int mItemCount;
    /**
     * 画布总数
     */
    private int mTotalCanvas;
    /** 所有的item，用集合保存起来 */
    //private ArrayList<ImageView> mItems = new ArrayList<ImageView>();
    /**
     * 上下文
     */
    private Context mContext;

    ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.98f, 1.0f, 0.98f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
    ScaleAnimation scaleAnimation1 = new ScaleAnimation(0.98f, 1.0f, 0.98f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

    public MyGridViewAdapter(Context context, int pageIndex, int pageCount, int totalCanvas) {
        mContext = context;
        mPageIndex = pageIndex;
        mPageCount = pageCount;
        mTotalCanvas = totalCanvas;
        mItemCount = (mPageIndex == mPageCount - 1) ? mTotalCanvas - Constant.MAX_ITEM_PER_CANVAS * mPageIndex
                : Constant.MAX_ITEM_PER_CANVAS;
        mAge = ((SelectCanvasActivity) mContext).getAge();
    }

    @Override
    public int getCount() {
        return mItemCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //这里的图片是以1440x960(2880x1920 C20的一半)为基准
            float widthScale = HappyDoodleApp.getScreenWidth() / 1440.0f;
            float heightScale = HappyDoodleApp.getScreenHeight() / 960.0f;
            imageView.setLayoutParams(new GridView.LayoutParams((int) (Constant.OPUS_THUMB_WIDTH * widthScale),
                    (int) (Constant.OPUS_THUMB_HEIGHT * heightScale)));
        } else {
            imageView = (ImageView) convertView;
        }

        String dir = "";
        if (mAge == 0) {
            dir = "pic/canvas_3-4/";
        } else {
            dir = "pic/canvas_5-6/";
        }

        String urlString = "assets://" + dir + (Constant.MAX_ITEM_PER_CANVAS * mPageIndex + position) + ".png";
        ImageLoader.getInstance().displayImage(urlString, imageView, SelectCanvasActivity.getOptions());
        return imageView;
    }

    private void startAnimation(View view, boolean flag) {
        AnimationSet animationSet = new AnimationSet(true);
        if (animationSet != null) {//&& manimationSet != animationSet) {
            if (flag) {
                scaleAnimation.setDuration(50);
                animationSet.addAnimation(scaleAnimation);
                animationSet.setFillAfter(false);
                view.startAnimation(animationSet);
            } else {
                scaleAnimation1.setDuration(50);
                animationSet.addAnimation(scaleAnimation1);
                animationSet.setFillAfter(true);
                view.startAnimation(animationSet);
//				manimationSet = animationSet;
            }
        }
    }

}
