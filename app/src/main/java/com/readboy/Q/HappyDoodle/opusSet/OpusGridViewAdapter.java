package com.readboy.Q.HappyDoodle.opusSet;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readboy.Q.HappyDoodle.HappyDoodleApp;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.util.ImageWorker;
import com.readboy.Q.HappyDoodle.util.Utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class OpusGridViewAdapter extends BaseAdapter {
	
	private static final String TAG = "lqn-OpusGridViewAdapter";
	
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
	
	/** 异步加载图片类对象 */
	private ImageWorker mImageWorker;
	
	public OpusGridViewAdapter(Context context,int pageIndex,int pageCount,int totalCanvas,ImageWorker imageWorker) {
		mContext = context;
		mPageIndex = pageIndex;
		mPageCount = pageCount;
		mTotalCanvas = totalCanvas;
		mItemCount = (mPageIndex == mPageCount-1) ? mTotalCanvas - Constant.MAX_ITEM_PER_CANVAS*mPageIndex 
						: Constant.MAX_ITEM_PER_CANVAS;
		mImageWorker = imageWorker;
	}

	@Override
	public int getCount() {
		//return mItemCount;
		// Size of adapter + number of columns for top empty row
        return mItemCount;
	}

	@Override
	public Object getItem(int position) {
		/*return position < Constant.MAX_ITEM_PER_CANVAS ?
                null : mImageWorker.getAdapter().getItem(Constant.MAX_ITEM_PER_CANVAS*mPageIndex+position);*/
		return null;
	}

	@Override
	public long getItemId(int position) {
		return Constant.MAX_ITEM_PER_CANVAS*mPageIndex+position;
		//return 0;
	}
	
	@Override
    public int getViewTypeCount() {
        // Two types of views, the normal ImageView and the top row of empty views
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.i(TAG, "position="+position+",convertView="+convertView);
		
		final ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(mContext);
			int w = HappyDoodleApp.getScreenWidth();
			if (w>1280){
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				GridView.LayoutParams gl = new GridView.LayoutParams(Utils.dip2px(mContext,Constant.OPUS_THUMB_WIDTH), Utils.dip2px(mContext,Constant.OPUS_THUMB_HEIGHT));
				imageView.setLayoutParams(gl);
				imageView.setPadding(Utils.dip2px(mContext,23), Utils.dip2px(mContext,40), Utils.dip2px(mContext,23), Utils.dip2px(mContext,25));
				imageView.setBackgroundResource(R.drawable.icon_bk);
			}else {
				imageView.setBackgroundResource(R.drawable.icon_bk);
				imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				imageView.setPadding(17, 34, 17, 19);
				imageView.setLayoutParams(new GridView.LayoutParams(Constant.OPUS_THUMB_WIDTH, Constant.OPUS_THUMB_HEIGHT));
			}

		} else {
			imageView = (ImageView) convertView;
		}
		
		String urlString = "file://"+OpusSetActivity.getOpusFilesPath().get(Constant.MAX_ITEM_PER_CANVAS*mPageIndex+position);
		ImageLoader.getInstance().displayImage(urlString, imageView, OpusSetActivity.getOptions());
		return imageView;
	}

}
