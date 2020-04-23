package com.jmc.demo.views.pager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;


public class HQRecommendViewPagerParent extends RelativeLayout {

    private GestureDetector gestureDetector;


    private HQRecommendViewPager viewPager;

    public HQRecommendViewPagerParent(Context context) {
        this(context, null);
    }

    public HQRecommendViewPagerParent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HQRecommendViewPagerParent(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HQRecommendViewPagerParent(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        gestureDetector = new GestureDetector(getContext(), new MySimpleOnGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return viewPager.onTouchEvent(event);
    }

    public HQRecommendViewPager getViewPager() {
        return viewPager;
    }

    public void setViewPager(HQRecommendViewPager viewPager) {
        this.viewPager = viewPager;
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (e.getX() < getWidth() / 2)
                viewPager.getAdapter().onItemClick(viewPager.getCurrentItem() - 1, 1);
            else
                viewPager.getAdapter().onItemClick(viewPager.getCurrentItem() + 1, 1);

            return true;
        }

    }
}
