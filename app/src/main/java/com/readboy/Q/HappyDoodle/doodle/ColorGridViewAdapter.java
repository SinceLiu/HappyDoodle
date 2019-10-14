package com.readboy.Q.HappyDoodle.doodle;

import java.util.ArrayList;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.data.DataManager;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;
import com.readboy.Q.HappyDoodle.util.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorGridViewAdapter extends BaseAdapter {
	
	private static final String TAG = "lqn-ColorGridViewAdapter";
	
	
	/** 当前是第几页*/
	private int mPageIndex;
	/** 当前页有多少item */
	private int mItemCount;
	/** 当前页选中的是哪一项，-1代表没有选中项（选中项在其它页） */
	private int mSelectedInThisPage = -1;
	
	/** 所有的item，用集合保存起来 */
	//private ArrayList<ImageView> mItems = new ArrayList<ImageView>();
	/** 上下文 */
	private Context mContext;
	
	public ColorGridViewAdapter(Context context,int pageIndex) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mPageIndex = pageIndex;
		mItemCount = (mPageIndex == Constant.DOODLE_COLOR_PAGES-1) ? Constant.TOTAL_COLORS - Constant.MAX_COLOR_PER_PAGE*mPageIndex 
						: Constant.MAX_COLOR_PER_PAGE;
		mSelectedInThisPage = getSelected();
		//Log.e(TAG, "mSelectedInThisPage="+mSelectedInThisPage);
	}
	
	/**
	 * 获得当前页中哪一项被选中了
	 * @return 当前页选中的是哪一项，-1代表没有选中项（选中项在其它页）
	 */
	private int getSelected()
	{
		int selected = -1;
		if(((DoodleActivity)mContext).getCurColorSelected()/Constant.MAX_COLOR_PER_PAGE == mPageIndex)//选中项在当前页
		{
			selected = ((DoodleActivity)mContext).getCurColorSelected()%Constant.MAX_COLOR_PER_PAGE;
		}
		return selected;
	}
	
	/**
	 * 设置当前页中哪一项被选中了
	 * @param selIndex 被选中项在当前页的索引
	 */
	public void setSelected(int selIndex)
	{
		if(selIndex > -1 && selIndex < mItemCount) {
            mSelectedInThisPage = selIndex;
        }
		//notifyDataSetChanged();
	}
	
	public int getSelectedInThisPage() {
		return mSelectedInThisPage;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItemCount;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		/*if(convertView == null)
		{
			ImageView imageView = new ImageView(mContext);
			Bitmap bm = DataManager.decodeBitmapFromAsset("pic/color/color"+(mPageIndex+1)+"_0"+(position+1)+".png");
			imageView.setImageBitmap(bm);
			convertView = imageView;
		}
		return convertView;*/
		
		//Log.e(TAG, "position="+position+",convertView="+convertView);
		
		final ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			int w = HappyDoodleApp.getScreenWidth();
			if (w==Constant.C20_WIDTH){
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setLayoutParams(new GridView.LayoutParams(Constant.COLOR_THUMB_WIDTH*2, Constant.COLOR_THUMB_HEIGHT*2));
			}
			else if (w>1280){
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				GridView.LayoutParams gl;
//				if (w==1920){
					gl = new GridView.LayoutParams(Utils.dip2px(mContext,Constant.COLOR_THUMB_WIDTH), Utils.dip2px(mContext,Constant.COLOR_THUMB_HEIGHT));
//				}else {
//					gl = new GridView.LayoutParams(Utils.dip2px(mContext, Constant.COLOR_THUMB_WIDTH - 10), Utils.dip2px(mContext, Constant.COLOR_THUMB_HEIGHT - 10));
//				}
				imageView.setLayoutParams(gl);
			}else {
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setLayoutParams(new GridView.LayoutParams(Constant.COLOR_THUMB_WIDTH, Constant.COLOR_THUMB_HEIGHT));
			}
		} else {
			imageView = (ImageView) convertView;
		}
		
		String urlString;
        if(mSelectedInThisPage != -1 && mSelectedInThisPage == position
        		&& ((DoodleActivity)mContext).getCurColorSelected()/Constant.MAX_COLOR_PER_PAGE == mPageIndex)//有选中项且在当前页
        {
        	urlString = "assets://"+"pic/color/color_sel/"+(Constant.MAX_COLOR_PER_PAGE*mPageIndex+position+1)+".png";
        }
        else
        {
        	urlString = "assets://"+"pic/color/color_nor/"+(Constant.MAX_COLOR_PER_PAGE*mPageIndex+position+1)+".png";
        }
		
		//String urlString = "assets://"+"pic/color/color_nor/"+(mPageIndex*Constant.MAX_COLOR_PER_PAGE+position+1)+".png";
		ImageLoader.getInstance().displayImage(urlString, imageView, DoodleActivity.getOptions());
		return imageView;
	}

}
