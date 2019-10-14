package com.readboy.Q.HappyDoodle.OpusShow;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.opusSet.OpusSetActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class OpusShowViewPagerAdapter extends PagerAdapter {
	private static final String TAG = "lqn-OpusShowViewPagerAdapter";
	
	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 画布总数 */
	private int mTotalCanvas;
	/** 每一页都是一个GridView，用集合保存起来 */
	//private ArrayList<WeakReference<GridView>> mGridViews = new ArrayList<WeakReference<GridView>>();
	private HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();
	/** 上下文 */
	private Context mContext;
	private LayoutInflater inflater;
	
	public OpusShowViewPagerAdapter(Context context,int pageCount,int totalCanvas) {
		// TODO Auto-generated constructor stub
		mPageCount = pageCount;
		mContext = context;
		mTotalCanvas = totalCanvas;
		inflater = LayoutInflater.from(mContext);
		initViews();
	}
	
	private void initViews() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mPageCount;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		// TODO Auto-generated method stub
		return arg0.equals(arg1);//arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		/*Log.e(TAG, "position="+position);
		GridView gridView = getGridViewFromCache(position);
		if(gridView == null)
		{
			Log.e(TAG, "gridView=null");
			gridView = addGridViewToCache(position);
		}
		((ViewPager)container).addView(gridView);
		return gridView;*/
		
		View imageLayout = inflater.inflate(R.layout.opus_show_item_pager_image, container, false);
		//View imageLayout = View.inflate(mContext.getApplicationContext(),R.layout.opus_show_item_pager_image, null);
		ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
		final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

		String uri = "file://"+((OpusShowActivity)mContext).getOpusFilesPath().get(position);
		ImageLoader.getInstance().displayImage(uri, imageView, ((OpusShowActivity)mContext).getOptions(), new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				spinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						System.gc();
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
				Toast toast = Toast.makeText(mContext, message, -1);
				toast.setDuration(Toast.LENGTH_SHORT);
				toast.show();

				spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				spinner.setVisibility(View.GONE);
			}
		});

		((ViewPager) container).addView(imageLayout,0);
		setObjectForPosition(imageLayout,position);
		return imageLayout;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		mObjs.remove(Integer.valueOf(position));//注，貌似不加这句会导致内存泄露
		((ViewPager) container).removeView((View) object);
		object = null;
	}
	
	@Override
	public void finishUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		//super.finishUpdate(container);
	}
	
	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
		// TODO Auto-generated method stub
		//super.restoreState(state, loader);
	}
	
	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void startUpdate(ViewGroup container) {
		// TODO Auto-generated method stub
		//super.startUpdate(container);
	}
	
	public void setObjectForPosition(Object obj, int position) {
		mObjs.put(Integer.valueOf(position), obj);
	}
	
	public View findViewFromObject(int position) {
		Object o = mObjs.get(Integer.valueOf(position));
		if (o == null) {
			return null;
		}
		
		View v;
		ViewPager viewPager = ((OpusShowActivity)mContext).getViewPager();
		for (int i = 0; i < viewPager.getChildCount(); i++) {
			v = viewPager.getChildAt(i);
			if (isViewFromObject(v, o)) {
                return v;
            }
		}
		return null;
	}

}
