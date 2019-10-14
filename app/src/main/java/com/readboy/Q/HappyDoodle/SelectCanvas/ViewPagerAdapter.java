package com.readboy.Q.HappyDoodle.SelectCanvas;

import java.util.HashMap;
import java.util.LinkedHashMap;

import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.data.Constant;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;


public class ViewPagerAdapter extends PagerAdapter {
	private static final String TAG = "lqn-ViewPagerAdapter";
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 画布总数 */
	private int mTotalCanvas;
	/** 每一页都是一个GridView，用集合保存起来 */
	//private ArrayList<WeakReference<GridView>> mGridViews = new ArrayList<WeakReference<GridView>>();
	private HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();
	/** 上下文 */
	private Context mContext;


	
	public ViewPagerAdapter(Context context,int pageCount,int totalCanvas) {
		mPageCount = pageCount;
		mContext = context;
		mTotalCanvas = totalCanvas;
		initViews();
	}
	
	private void initViews() {
		for(int i=0;i<mPageCount;i++)
		{
			/*LayoutInflater inflater = LayoutInflater.from(mContext);
			GridView gridView = (GridView) inflater.inflate(R.layout.select_gridview, null);
			gridView.setAdapter(new MyGridViewAdapter(mContext,i,mPageCount,mTotalCanvas));
			//gridView.setSelected(false);
			gridView.setOnItemClickListener(new MyOnItemClickListener(i));
			mGridViews.add(gridView);*/
			//addGridViewToCache(i);
		}
	}

	/**
	 * 实例化一个GridView
	 * @param index GridView在viewpager中的位置
	 * @return GridView
	 */
	private GridView inflateGridView(int index)
	{
		LayoutInflater inflater = LayoutInflater.from(mContext);
		GridView gridView = (GridView) inflater.inflate(R.layout.select_gridview, null);
		gridView.setAdapter(new MyGridViewAdapter(mContext,index,mPageCount,mTotalCanvas));
		gridView.setOnItemClickListener(new MyOnItemClickListener(index));
//		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		return gridView;
	}
	
	@Override
	public int getCount() {
		return mPageCount;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		//Log.e(TAG, "position="+position);
		//GridView gridView = getGridViewFromCache(position);
		GridView gridView = inflateGridView(position);
		if(gridView == null)
		{
			Log.e(TAG, "gridView=null");
			return null;
			//gridView = addGridViewToCache(position);
		}
		((ViewPager)container).addView(gridView);
		setObjectForPosition(gridView,position);
		return gridView;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		mObjs.remove(Integer.valueOf(position));//注，貌似不加这句会导致内存泄露
		((ViewPager) container).removeView((View) object);
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
		ViewPager viewPager = ((SelectCanvasActivity)mContext).getViewPager();
		for (int i = 0; i < viewPager.getChildCount(); i++) {
			v = viewPager.getChildAt(i);
			if (isViewFromObject(v, o)) {
                return v;
            }
		}
		return null;
	}
	
	/**
     * GridView Item点击监听器
     * @author css
     *
     */
    class MyOnItemClickListener implements OnItemClickListener
    {
    	/** 记录是哪一页 */
    	private int pageIdx;
    	
    	public MyOnItemClickListener(int pageIdx) {
    		this.pageIdx = pageIdx;
		}
    	
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//Log.e(TAG, "position="+position);
			/*String path = "";
			if(mAge == 0)
				path = "pic/canvas_3-4/";
			else if(mAge == 1)
				path = "pic/canvas_4-5/";
			else if(mAge == 2)
				path = "pic/canvas_5-6/";*/
			
			/*Intent intent = new Intent(mContext,DoodleActivity.class);
			intent.putExtra("AGE", mAge);
			intent.putExtra("CANVAS_INDEX", Constant.MAX_ITEM_PER_CANVAS*pageIdx+position);
			//intent.putExtra("CANVAS_PATH", path+"canvas_"+(Constant.MAX_ITEM_PER_CANVAS*pageIdx+position)+".png");
			mContext.startActivity(intent);*/
//			if((int)view.getTag()==Constant.MAX_ITEM_PER_CANVAS*pageIdx+position) {
//				((ImageView) view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//				((ImageView) view).setLayoutParams(new GridView.LayoutParams(Constant.OPUS_THUMB_WIDTH * 2, Constant.OPUS_THUMB_HEIGHT * 2));
//			}
			((SelectCanvasActivity)mContext).getMyMainLayout().setIsIntercept(true);
			
			if(SelectCanvasActivity.mIsForbidOp) {
                return ;
            }
			
			((SelectCanvasActivity)mContext).playIconSoundByIndex(Constant.MAX_ITEM_PER_CANVAS*pageIdx+position);
		}
    	
    }

}
