package com.readboy.Q.HappyDoodle.doodle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.OpusShow.OpusShowActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.opusSet.OpusGridViewAdapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

public class ColorViewPagerAdapter extends VerticalPagerAdapter {
	private static final String TAG = "lqn-ViewPagerAdapter";
	
	/** 每一页都是一个GridView，用集合保存起来 */
	private ArrayList<WeakReference<GridView>> mGridViews = new ArrayList<WeakReference<GridView>>();
	private HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();
	/** 上下文 */
	private Context mContext;
	
	public ColorViewPagerAdapter(Context context) {
		mContext = context;
		initViews();
	}
	
	/**
	 * 初始化每一页
	 */
	private void initViews() {
		for(int i=0;i<Constant.DOODLE_COLOR_PAGES;i++)
		{
			/*LayoutInflater inflater = LayoutInflater.from(mContext);
			GridView gridView = (GridView) inflater.inflate(R.layout.color_gridview, null);
			gridView.setAdapter(new ColorGridViewAdapter(mContext,i));
			//gridView.setSelected(false);
			gridView.setOnItemClickListener(new MyOnItemClickListener(i));
			mGridViews.add(gridView);*/
			//addGridViewToCache(i);
		}
	}
	
	/*private GridView addGridViewToCache(int index)
	{
		LayoutInflater inflater = LayoutInflater.from(mContext);
		GridView gridView = (GridView) inflater.inflate(R.layout.color_gridview, null);
		gridView.setAdapter(new ColorGridViewAdapter(mContext,index));
		//gridView.setSelected(false);
		gridView.setOnItemClickListener(new MyOnItemClickListener(index));
		mGridViews.add(new WeakReference<GridView>(gridView));
		return gridView;
	}*/
	
	/**
	 * 实例化一个GridView
	 * @param index GridView在viewpager中的位置
	 * @return GridView
	 */
	private GridView inflateGridView(int index)
	{
		LayoutInflater inflater = LayoutInflater.from(mContext);
		GridView gridView = (GridView) inflater.inflate(R.layout.color_gridview, null);
		gridView.setAdapter(new ColorGridViewAdapter(mContext,index));
		//gridView.setSelected(false);
		gridView.setOnItemClickListener(new MyOnItemClickListener(index));
		return gridView;
	}
	
	private GridView getGridViewFromCache(int index)
	{
		return mGridViews.get(index).get();
	}
	
	/**
     * 重写该方法很重要，因为当调用notifyDataSetChanged()时，会根据此函数的返回值来决定是否要更新数据
     */
    @Override
    public int getItemPosition(Object object) {
    	return POSITION_NONE;
    }

	@Override
	public int getCount() {
		return Constant.DOODLE_COLOR_PAGES;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(View container, int position) {
		//Log.e(TAG, "position="+position);
		GridView gridView = inflateGridView(position);//getGridViewFromCache(position);
		if(gridView == null)
		{
			//Log.e(TAG, "gridView=null");
			return null;
			//gridView = addGridViewToCache(position);
		}
		((VerticalViewPager)container).addView(gridView);
		setObjectForPosition(gridView,position);
		return gridView;
	}
	
	@Override
	public void destroyItem(View container, int position, Object object) {
		mObjs.remove(Integer.valueOf(position));//注，貌似不加这句会导致内存泄露
		((VerticalViewPager) container).removeView((View) object);
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
		VerticalViewPager viewPager = ((DoodleActivity)mContext).getViewPager();
		for (int i = 0; i < viewPager.getChildCount(); i++) {
			v = viewPager.getChildAt(i);
			if (isViewFromObject(v, o))
				return v;
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
			//Log.i(TAG, "pageIdx="+pageIdx+",position="+position);
			/*int index = Constant.MAX_COLOR_PER_PAGE*pageIdx + position;
			((DoodleActivity)mContext).setCurColor(index);*/
			
			ColorGridViewAdapter adapter = (ColorGridViewAdapter)((GridView)findViewFromObject(pageIdx)).getAdapter();
			if(adapter.getSelectedInThisPage() == position
					&& ((DoodleActivity)mContext).getCurColorSelected()/Constant.MAX_COLOR_PER_PAGE == pageIdx)//有选中项且在当前页，表示是第二次点击
			{
				//Log.e(TAG, "second click");
			}
			else
			{
				adapter.setSelected(position);
				int index = Constant.MAX_COLOR_PER_PAGE*pageIdx + position;
				((DoodleActivity)mContext).setCurColor(index);
				((DoodleActivity)mContext).initSelectedImageOptions();
				notifyDataSetChanged();//通知PagerAdapter更新数据
				//String urlString = "assets://"+"pic/template/selected_"+(Constant.MAX_ITEM_PER_PAGE*pageIdx+position)+".png";
				//ImageLoader.getInstance().displayImage(urlString, (ImageView)view, SelectTemplateActivity.getOptions());
				
			}
		}
    	
    }

}
