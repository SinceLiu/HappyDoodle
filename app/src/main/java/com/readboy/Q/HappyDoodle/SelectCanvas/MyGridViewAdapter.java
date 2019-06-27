package com.readboy.Q.HappyDoodle.SelectCanvas;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.util.Utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MyGridViewAdapter extends BaseAdapter {
	
	private static final String TAG = "lqn-MyGridViewAdapter";
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 有多少页，默认3页 */
	private int mPageCount;
	/** 当前是第几页*/
	private int mPageIndex;
	/** 当前页有多少item */
	private int mItemCount;
	/** 画布总数 */
	private int mTotalCanvas;
	/** 所有的item，用集合保存起来 */
	//private ArrayList<ImageView> mItems = new ArrayList<ImageView>();
	/** 上下文 */
	private Context mContext;
	
	public MyGridViewAdapter(Context context,int pageIndex,int pageCount,int totalCanvas) {
		mContext = context;
		mPageIndex = pageIndex;
		mPageCount = pageCount;
		mTotalCanvas = totalCanvas;
		mItemCount = (mPageIndex == mPageCount-1) ? mTotalCanvas - Constant.MAX_ITEM_PER_CANVAS*mPageIndex 
						: Constant.MAX_ITEM_PER_CANVAS;
		mAge = ((SelectCanvasActivity)mContext).getAge();
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
		/*if(convertView == null)
		{
			ImageView imageView = new ImageView(mContext);
			Bitmap bm = DataManager.decodeBitmapFromAsset("pic/canvas_3-4/"+(6*mPageIndex+position)+".png");
			imageView.setImageBitmap(bm);
			convertView = imageView;
		} 
		return convertView;*/
		
		//Log.i(TAG, "position="+position+",convertView="+convertView);
		
		final ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);

            int w = HappyDoodleApp.getScreenWidth();
            if (w>1280){
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				GridView.LayoutParams gl = new GridView.LayoutParams(Utils.dip2px(mContext,Constant.OPUS_THUMB_WIDTH-10), Utils.dip2px(mContext,Constant.OPUS_THUMB_HEIGHT-10));
				imageView.setLayoutParams(gl);
            }else {
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setLayoutParams(new GridView.LayoutParams(Constant.OPUS_THUMB_WIDTH, Constant.OPUS_THUMB_HEIGHT));
			}
		} else {
			imageView = (ImageView) convertView;
		}
		
		String dir = "";
		if(mAge == 0)
			dir = "pic/canvas_3-4/";
		else
			dir = "pic/canvas_5-6/";
		
		String urlString = "assets://"+dir+(Constant.MAX_ITEM_PER_CANVAS*mPageIndex+position)+".png";
//		Log.v(TAG, "-------------urlString = "+urlString);
		
		ImageLoader.getInstance().displayImage(urlString, imageView, SelectCanvasActivity.getOptions());
		return imageView;
	}

}
