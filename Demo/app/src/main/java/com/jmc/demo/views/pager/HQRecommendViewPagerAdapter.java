package com.jmc.demo.views.pager;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jmc.demo.R;

public abstract class HQRecommendViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final int POSITION_MUSIC = 0, POSITION_NEW = 1, POSITION_RADIO = 2;

    private final int CLICKOFFSET = 1;

    protected HQRecommendViewPagerItem[] items;

    private OnItemClickListener onItemClickListener;

    protected HQRecommendViewPagerAdapter(HQRecommendViewPagerItem... items) {
        this.items = items;
    }

    protected HQRecommendViewPagerAdapter(int count) {
        this.items = new HQRecommendViewPagerItem[count];
    }

    public HQRecommendViewPagerItem getItem(int position) {
        return items[position % items.length];
    }

    public void onItemClick(int position, int offset) {
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(position, offset, items.length, getItem(position));
    }

    public void notifyItem(int position, int source, String url, String describe, String id) {
        if (position >= 0 && position < items.length) {
            HQRecommendViewPagerItem item = items[position];
            if (item != null) {
                item.setSource(source);
                item.setUrl(url);
                item.setDescribe(describe);
                item.setId(id);
            }
        }
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    public abstract View getView(@NonNull ViewGroup container, int position);

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = getView(container, position);
        view.setOnClickListener(this);
        view.setTag(R.id.tag_position, position);
        container.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(R.id.tag_position);
        onItemClick(position, CLICKOFFSET);
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, int offset, int total, HQRecommendViewPagerItem item);
    }

}
