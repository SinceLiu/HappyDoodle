package com.readboy.Q.HappyDoodle.opusSet;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.readboy.Q.HappyDoodle.R;
import com.readboy.Q.HappyDoodle.OpusShow.OpusShowActivity;
import com.readboy.Q.HappyDoodle.data.Constant;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.DialogStyle;
import com.readboy.Q.HappyDoodle.doodle.CustomDialog.OnBtnClickCallback;
import com.readboy.Q.HappyDoodle.util.ImageWorker;
import com.readboy.Q.HappyDoodle.util.ToastUtils;

import android.app.ReadboyActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

public class OpusViewPagerAdapter extends PagerAdapter {
	private static final String TAG = "lqn-OpusViewPagerAdapter";
	
	/** 年龄,3-4岁记为0，5-6岁记为1，7岁也记为1，默认3-4岁 */
	private int mAge = 0;
	/** 有多少页，默认3页 */
	private int mPageCount = 3;
	/** 画布总数 */
	private int mTotalCanvas;
	/** 当前选中的item索引，是在所有作品中的下标 */
	private int mSelectIndex;
	/** viewpager控件 */
	private ViewPager mViewPager;
	/** 每一页都是一个GridView，用集合保存起来 */
	//private ArrayList<WeakReference<GridView>> mGridViews = new ArrayList<WeakReference<GridView>>();
	private HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();
	/** 上下文 */
	private Context mContext;
	
	/** 异步加载图片类对象 */
	private ImageWorker mImageWorker;
	
	public OpusViewPagerAdapter(Context context,ViewPager viewPager,int pageCount,int totalCanvas,ImageWorker imageWorker) {
		mPageCount = pageCount;
		mContext = context;
		mTotalCanvas = totalCanvas;
		mImageWorker = imageWorker;
		mViewPager = viewPager;
		
		initViews();
		
		initDialog();

		
	}
	
	public void initDialog(){
		CustomDialog.initialize(mContext,DialogStyle.DELETE,new OnBtnClickCallback() {
					
					@Override
					public void onBtnClick(View view, String btnName) {
						//Log.e(TAG, "btnName="+btnName);
						((OpusSetActivity)mContext).dismissDialog();
						if("yes_btn".equals(btnName))
						{
							deleteOpus(mSelectIndex);
						}
					}
				});
	}
	
	/**
	 * 初始化每一页
	 */
	private void initViews() {
		for(int i=0;i<mPageCount;i++)
		{
			//addGridViewToCache(i);
		}
	}
	
	/*private GridView addGridViewToCache(int index)
	{
		LayoutInflater inflater = LayoutInflater.from(mContext);
		GridView gridView = (GridView) inflater.inflate(R.layout.select_gridview, null);
		gridView.setAdapter(new OpusGridViewAdapter(mContext,index,mPageCount,mTotalCanvas,mImageWorker));
		//gridView.setSelected(false);
		gridView.setOnItemClickListener(new MyOnItemClickListener(index));
		gridView.setOnItemLongClickListener(new MyOnItemLongClickListener(index));
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
		GridView gridView = (GridView) inflater.inflate(R.layout.select_gridview, null);
		gridView.setAdapter(new OpusGridViewAdapter(mContext,index,mPageCount,mTotalCanvas,mImageWorker));
		//gridView.setSelected(false);
		gridView.setOnItemClickListener(new MyOnItemClickListener(index));
		gridView.setOnItemLongClickListener(new MyOnItemLongClickListener(index));
//		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		return gridView;
	}
	
	/*private GridView getGridViewFromCache(int index)
	{
		return mGridViews.get(index).get();
	}*/
	
	/**
     * 删除一个作品
     * @param index 作品索引
     */
    public void deleteOpus(int index) {
		File file = new File(OpusSetActivity.getOpusFilesPath().get(index));
		if(file.isFile() && file.exists())
		{
			file.delete();
			OpusSetActivity.getOpusFilesPath().remove(index);
			//删除后要清空缓存，不然如果先建的作品名字如果与删除的作品名字一样的话，还是显示删除的那一张（缓存里的）
			ImageLoader.getInstance().clearMemoryCache();
			ImageLoader.getInstance().clearDiscCache();
			
			int lastPager = mViewPager.getCurrentItem();
			//先移除之前的所有gridView
			mViewPager.removeAllViews();
			//mGridViews.clear();
			mObjs.clear();
			mTotalCanvas--;
			mPageCount = OpusSetActivity.calculatePageCount();
			initViews();
			
			notifyDataSetChanged();//通知PagerAdapter更新数据
			//Utils.requestScanFileForDelete(mContext,file,0);
			
			RadioGroup radioGroup = OpusSetActivity.getRadioGroup();
			if(radioGroup.getChildCount() > mPageCount)//表明某一页的item被删光了
			{
				radioGroup.removeViewAt(mPageCount);
				//PagerAdapter已经自动做了下面两件事
				//OpusSetActivity.setCheckedRadioButton(mPageCount-1);
				//mViewPager.setCurrentItem(lastPager-1);
			}
		}
		else 
		{
			ToastUtils.show(mContext, "删除失败！", Toast.LENGTH_LONG);
		}
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
		return mPageCount;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	@Override
	public Object instantiateItem(View container, int position) {
		
	/*	@Override
		public Object instantiateItem(View container, int position) {
//			Log.w(tag, "----------------------------instantiateItem");
			((ViewPager)container).addView(viewList.get(position));
			return viewList.get(position);
		}*/ 
//		Log.w(TAG, "----------position = "+position+"------view = "+findViewFromObject(position));
		GridView gridView = inflateGridView(position);
		if(gridView == null)
		{
			return null;
		}
		((ViewPager)container).addView(gridView);
		setObjectForPosition(gridView,position);
		return gridView;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		GridView gridView = (GridView) object;
		/*for(int i=0;i<gridView.getChildCount();i++)
		{
			ImageView imageView = (ImageView) gridView.getChildAt(i);
			ImageWorker.cancelWork(imageView);
			imageView.setImageDrawable(null);
			imageView = null;
		}*/
		mObjs.remove(Integer.valueOf(position));//注，貌似不加这句会导致内存泄露
		((ViewPager) container).removeView(gridView);
		gridView = null;
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
		ViewPager viewPager = ((OpusSetActivity)mContext).getViewPager();
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
//			((ImageView)view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//			((ImageView)view).setLayoutParams(new GridView.LayoutParams(Constant.OPUS_THUMB_WIDTH*2, Constant.OPUS_THUMB_HEIGHT*2));
			Intent intent = new Intent(mContext,OpusShowActivity.class);
			intent.putExtra("TOTAL_PAGE", mTotalCanvas);
			intent.putExtra("CUR_PAGE", Constant.MAX_ITEM_PER_CANVAS*pageIdx+position);
			intent.putExtra("OPUS_PATH", OpusSetActivity.getOpusFilesPath());
			((ReadboyActivity)mContext).launchForResult(intent, -1);
			((OpusSetActivity)mContext).overridePendingTransition(R.anim.slide_in_top,
					R.anim.slide_out_bottom);
			((OpusSetActivity)mContext).getmMediaPlayer().pause();
			((OpusSetActivity)mContext).setmIsSndPause(false);
		}
    	
    }
    
    /**
     * GridView Item长按监听器
     * @author css
     *
     */
    class MyOnItemLongClickListener implements OnItemLongClickListener
    {
    	/** 记录是哪一页 */
    	private int pageIdx;
    	
    	public MyOnItemLongClickListener(int pageIdx) {
    		this.pageIdx = pageIdx;
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			//Log.e(TAG, "position="+position);
//			((ImageView)view).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//			((ImageView)view).setLayoutParams(new GridView.LayoutParams(Constant.OPUS_THUMB_WIDTH*2, Constant.OPUS_THUMB_HEIGHT*2));
			mSelectIndex = Constant.MAX_ITEM_PER_CANVAS*pageIdx+position;
			((OpusSetActivity)mContext).showCustomDialog();
			OpusSetActivity.getMyMainLayout().setIsPopupDialog(true);
			//mViewPager.clearFocus();
			return true;
		}
    	
		/*@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.e(TAG, "position="+position);
			
			Intent intent = new Intent(mContext,OpusShowActivity.class);
			intent.putExtra("TOTAL_PAGE", mTotalCanvas);
			intent.putExtra("CUR_PAGE", Constant.MAX_ITEM_PER_CANVAS*pageIdx+position);
			intent.putExtra("OPUS_PATH", OpusSetActivity.getOpusFilesPath());
			mContext.startActivity(intent);
		}*/
    	
    }

}
