package com.jmc.demo.views.pager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class HQRecommendViewPager extends ViewPager implements HQRecommendViewPagerAdapter.OnItemClickListener {

    final int OffscreenPageLimit = 8;

    private DepthPageTransformer transformer;

    private OnItemClickListener onItemClickListener;


    private OnItemNotifyListener onItemNotifyListener;

    private boolean clickAutoScroll = true;

    public HQRecommendViewPager(@NonNull Context context) {
        this(context, null);
    }

    public HQRecommendViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        transformer = new DepthPageTransformer();
        setClipChildren(false);
        setOffscreenPageLimit(OffscreenPageLimit);
        setPageTransformer(true, transformer);
        setClickable(false);
    }

    public void setAdapter(@Nullable HQRecommendViewPagerAdapter adapter) {
        super.setAdapter(adapter);
        setCurrentItem(Integer.MAX_VALUE / 2 + 1);
        if (adapter != null) {
            addOnPageChangeListener(adapter);
            adapter.setOnItemClickListener(this);
        }
    }

    public HQRecommendViewPagerAdapter getAdapter() {
        return (HQRecommendViewPagerAdapter) super.getAdapter();
    }

    public float getCover() {
        return transformer.getCover();
    }

    public void setCover(float cover) {
        transformer.setCover(cover);
    }

    public float getZoom() {
        return transformer.getZoom();
    }

    public void setZoom(float zoom) {
        transformer.setZoom(zoom);
    }

    public boolean isClickAutoScroll() {
        return clickAutoScroll;
    }

    public void setClickAutoScroll(boolean clickAutoScroll) {
        this.clickAutoScroll = clickAutoScroll;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemNotifyListener getOnItemNotifyListener() {
        return onItemNotifyListener;
    }

    public void setOnItemNotifyListener(OnItemNotifyListener onItemNotifyListener) {
        this.onItemNotifyListener = onItemNotifyListener;
    }

    public void notifyDataSetChanged() {
        if (onItemNotifyListener != null)
            for (int i = 0; i < getChildCount(); ++i) {
                View child = getChildAt(i);
                if (child != null)
                    onItemNotifyListener.onItemNotify(child);
            }
    }

    @Override
    public void onItemClick(int position, int offset, int total, HQRecommendViewPagerItem item) {
        if (isClickAutoScroll()) setCurrentItem(position + offset);
        if (onItemClickListener != null)
            onItemClickListener.onItemClick(this, position % total, item);
    }

    public interface OnItemClickListener {
        void onItemClick(ViewPager viewPager, int position, HQRecommendViewPagerItem item);
    }

    public interface OnItemNotifyListener {
        void onItemNotify(View view);
    }

    class DepthPageTransformer implements PageTransformer {

        private float cover = -0.02f, zoom = 0.2f;

        @Override
        public void transformPage(@NonNull View page, float position) {

            float offset, translationX, width = page.getWidth();

            if (position < -2 || position > 2) {
                page.setAlpha(0);
            } else if (position > 1 && position <= 2) {
                offset = 2 - position;
                translationX = (-width * (1 - offset) - ((zoom / 2) * width * offset)) - cover * width;
                page.setScaleX((1 - zoom) * offset);
                page.setScaleY((1 - zoom) * offset);
                page.setTranslationX(translationX);
                page.setAlpha(offset);
            } else if (position > 0 && position <= 1) {
                offset = 1 - position;
                translationX = (-(zoom / 2) * width * position) - (cover * width * position);
                page.setScaleX(offset * zoom + (1 - zoom));
                page.setScaleY(offset * zoom + (1 - zoom));
                page.setTranslationX(translationX);
                page.setAlpha(1);
            } else if (position >= -1 && position < 0) {
                offset = -position;
                translationX = (-(zoom / 2) * width * offset) - (cover * width * position);
                page.setScaleX(offset * zoom + 1f);
                page.setScaleY(offset * zoom + 1f);
                page.setTranslationX(translationX);
                page.setAlpha(1);
            } else if (position >= -2 && position < -1) {
                offset = 2 + position;
                translationX = (-(zoom / 2) * width * offset + ((1 - offset) * zoom * width)) + cover * width;
                page.setScaleX((1 + zoom) * offset);
                page.setScaleY((1 + zoom) * offset);
                page.setAlpha(offset);
                page.setTranslationX(translationX);
            } else {
                page.setTranslationX(0);
                page.setAlpha(1);
                page.setScaleY(1);
                page.setScaleX(1);
            }

        }

        private float getCover() {
            return cover;
        }

        private void setCover(float cover) {
            this.cover = cover;
        }

        private float getZoom() {
            return zoom;
        }

        private void setZoom(float zoom) {
            this.zoom = zoom;
        }
    }

}
