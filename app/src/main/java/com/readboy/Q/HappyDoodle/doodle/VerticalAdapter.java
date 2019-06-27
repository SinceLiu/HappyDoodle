package com.readboy.Q.HappyDoodle.doodle;

import java.util.List;

import android.view.View;

public class VerticalAdapter extends VerticalPagerAdapter {
	List<View> mListViews;

	public VerticalAdapter(List<View> mListViews) {
		this.mListViews = mListViews;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((VerticalViewPager) arg0).removeView(mListViews.get(arg1));
	}

	@Override
	public int getCount() {
		return mListViews.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((VerticalViewPager) arg0).addView(mListViews.get(arg1), 0);
		return mListViews.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

}
