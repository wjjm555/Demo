package com.qinggan.dms.library.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.qinggan.dms.library.R;
import com.qinggan.dms.library.model.DragItemInfo;

public class DragLayout extends ViewGroup {

    private final int ITEMCOUNT = 5, LARGECOUNT = 2, SMALLCOUNT = ITEMCOUNT - LARGECOUNT;

    private final int LONG_PRESS = 2;

    private final int LONGPRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

    private final int[] STATE_PRESSED = {android.R.attr.state_pressed, android.R.attr.state_enabled},
            STATE_ENABLE = {android.R.attr.state_enabled};

    private int largeWidth = 600, largeHeight = 600, smallWidth = 200, smallHeight = 200, horizontalMargin = 20;

    private int mTouchSlop;

    private float offsetX, offsetY;

    private boolean isCanceled = false;

    private GestureDetector gestureDetector;

    private DragFloatView mFloatView;

    private DragItemView upDragItem, downDragItem, rippleItem;

    private DragItemView[] itemViews = new DragItemView[ITEMCOUNT];

    private MyLongPressHandler mHandler = new MyLongPressHandler();

    private onItemClickListener onItemClickListener;

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        try {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DragLayout, defStyleAttr, defStyleRes);
            for (int i = 0; i < typedArray.getIndexCount(); ++i) {
                int attr = typedArray.getIndex(i);
                if (attr == R.styleable.DragLayout_large_item_width) {
                    largeWidth = typedArray.getDimensionPixelSize(attr, largeWidth);
                } else if (attr == R.styleable.DragLayout_large_item_height) {
                    largeHeight = typedArray.getDimensionPixelSize(attr, largeHeight);
                } else if (attr == R.styleable.DragLayout_small_item_width) {
                    smallWidth = typedArray.getDimensionPixelSize(attr, smallWidth);
                } else if (attr == R.styleable.DragLayout_small_item_height) {
                    smallHeight = typedArray.getDimensionPixelSize(attr, smallHeight);
                } else if (attr == R.styleable.DragLayout_horizontal_item_margin) {
                    horizontalMargin = typedArray.getDimensionPixelSize(attr, horizontalMargin);
                }
            }
            typedArray.recycle();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initViews();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        gestureDetector = new GestureDetector(getContext(), new MySimpleOnGestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (!isCanceled)
                    handleTouchActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                if (!isCanceled)
                    handleTouchActionUp(event);
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureDragViews();
        mFloatView.measure(View.MeasureSpec.makeMeasureSpec(smallWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(smallHeight, View.MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layoutDragViews(l, t, r, b);
            mFloatView.layout(-smallWidth, -smallHeight, 0, 0);
        }
    }

    public void cancelDrag() {
        isCanceled = true;
        mHandler.removeMessages(LONG_PRESS);
        if (mFloatView != null && mFloatView.isShowing())
            mFloatView.dismissAnimation();

        if (rippleItem != null && rippleItem.getForeground() != null)
            rippleItem.getForeground().setState(STATE_ENABLE);

        if (downDragItem != null && downDragItem.getForeground() != null)
            downDragItem.getForeground().setState(STATE_ENABLE);

        if (upDragItem != null) {
            if (downDragItem != null && downDragItem.getTmpInfo() != null && upDragItem.getTmpInfo() != null) {
                downDragItem.clearTmpInfo();
                upDragItem.clearTmpInfo();
                downDragItem.tmpReplaceSuccessAnimation();
                upDragItem.tmpReplaceSuccessAnimation();
            } else {
                upDragItem.contentDisplayAnimation();
                if (downDragItem != null && !upDragItem.getType().equals(downDragItem.getType())) {
                    downDragItem.contentAmplifyAnimation();
                }
            }
        }
        downDragItem = null;
        upDragItem = null;
        rippleItem = null;
    }

    public void updateDragItem(String type, DragItemInfo.Size size, int resId) {
        if (type != null)
            for (DragItemView itemView : itemViews) {
                if (itemView != null) {
                    if (itemView.getInfo() != null && type.equals(itemView.getInfo().getType()))
                        itemView.updateImgResId(size, resId);
                    if (itemView.getTmpInfo() == null)
                        itemView.refreshContent();
                    else if (type.equals(itemView.getTmpInfo().getType())) {
                        itemView.updateTmpImgResId(size, resId);
                        itemView.refreshTmpContent();
                    }
                }
            }
    }

    public void setDragItemInfos(DragItemInfo... infos) {
        for (int i = 0; i < itemViews.length; ++i) {
            DragItemView itemView = itemViews[i];
            if (infos.length > i)
                itemView.setInfo(infos[i]);
            else
                break;
        }
    }

    public DragItemInfo getDragItemInfo(String type) {
        if (type != null) {
            for (DragItemView itemView : itemViews) {
                if (itemView.getInfo() != null && type.equals(itemView.getInfo().getType())) {
                    return itemView.getInfo();
                }
            }
        }
        return null;
    }

    public DragItemInfo[] getDragItemInfoList() {
        if (itemViews != null && itemViews.length > 0) {
            DragItemInfo[] infos = new DragItemInfo[itemViews.length];
            for (int i = 0; i < itemViews.length; ++i) {
                if (itemViews[i] != null)
                    infos[i] = itemViews[i].getInfo();
            }
            return infos;
        }
        return null;
    }

    private void initViews() {
        removeAllViews();
        for (int i = 0; i < ITEMCOUNT; ++i) {
            DragItemView itemView = new DragItemView(getContext());
            itemViews[i] = itemView;
            addView(itemView);
        }
        mFloatView = new DragFloatView(getContext());
        addView(mFloatView);
    }

    private void measureDragViews() {
        for (int i = 0; i < itemViews.length; ++i) {
            DragItemView itemView = itemViews[i];
            if (itemView != null)
                if (i < LARGECOUNT) {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec(largeWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(largeHeight, View.MeasureSpec.EXACTLY));
                    itemView.setSize(DragItemInfo.Size.LARGE);
                } else {
                    itemView.measure(View.MeasureSpec.makeMeasureSpec(smallWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(smallHeight, View.MeasureSpec.EXACTLY));
                    itemView.setSize(DragItemInfo.Size.SMALL);
                }
        }
    }

    private void layoutDragViews(int l, int t, int r, int b) {
        int realLeft = (r - l - largeWidth * 2 - smallWidth - horizontalMargin * 2) / 2;
        int largeRealTop = (b - t - largeHeight) / 2;
        int verticalMargin = (largeHeight - smallHeight * 3) / 2;
        for (int i = 0; i < itemViews.length; ++i) {
            DragItemView itemView = itemViews[i];
            if (itemView != null)
                switch (i) {
                    case 0:
                        itemView.layout(realLeft, largeRealTop, realLeft += itemView.getMeasuredWidth(), largeRealTop + itemView.getMeasuredHeight());
                        break;
                    case 1:
                        itemView.layout(realLeft += horizontalMargin, largeRealTop, realLeft += itemView.getMeasuredWidth(), largeRealTop + itemView.getMeasuredHeight());
                        break;
                    case 2:
                        itemView.layout(realLeft += horizontalMargin, largeRealTop, realLeft + itemView.getMeasuredWidth(), largeRealTop += itemView.getMeasuredHeight());
                        break;
                    case 3:
                        itemView.layout(realLeft, largeRealTop += verticalMargin, realLeft + itemView.getMeasuredWidth(), largeRealTop += itemView.getMeasuredHeight());
                        break;
                    case 4:
                        itemView.layout(realLeft, largeRealTop += verticalMargin, realLeft + itemView.getMeasuredWidth(), largeRealTop + itemView.getMeasuredHeight());
                        break;
                }
        }
    }

    private DragItemView getViewByTouchEvent(float x, float y) {
        for (DragItemView itemView : itemViews)
            if (getViewByTouchEvent(itemView, x, y))
                return itemView;
        return null;
    }

    private boolean getViewByTouchEvent(DragItemView view, float x, float y) {
        return view != null && x > view.getX() && x < view.getX() + view.getWidth() && y > view.getY() && y < view.getY() + view.getHeight();
    }

    private void handleTouchActionMove(MotionEvent event) {
        if (mFloatView != null && mFloatView.isShowing()) {
            float tmpOffsetX = event.getX() + mFloatView.getWidth() / 2, tmpOffsetY = event.getY() + mFloatView.getHeight() / 2;
            boolean positionChange = false;
            if (Math.abs(tmpOffsetX - offsetX) > mTouchSlop) {
                mFloatView.setTranslationX(tmpOffsetX);
                offsetX = tmpOffsetX;
                positionChange = true;
            }
            if (Math.abs(tmpOffsetY - offsetY) > mTouchSlop) {
                mFloatView.setTranslationY(tmpOffsetY);
                offsetY = tmpOffsetY;
                positionChange = true;
            }

            if (positionChange) {
                DragItemView itemView = getViewByTouchEvent(event.getX(), event.getY());
                if (itemView != null) {
                    if (downDragItem == null || !itemView.getType().equals(downDragItem.getType())) {
                        if (downDragItem != null) {
                            if (downDragItem.getForeground() != null)
                                downDragItem.getForeground().setState(STATE_ENABLE);

                            if (downDragItem.getTmpInfo() != null) {
                                downDragItem.tmpReplaceDismissAnimation();
                                downDragItem.setTmpInfo(null);
                            }
                            downDragItem.borderCancelledAnimation();
                            if (upDragItem != null && !upDragItem.getType().equals(downDragItem.getType())) {
                                if (upDragItem.getTmpInfo() != null) {
                                    upDragItem.tmpDismissAnimation();
                                    upDragItem.setTmpInfo(null);
                                }
                                downDragItem.contentAmplifyAnimation();
                            }
                        }
                        downDragItem = itemView;
                        downDragItem.borderSelectedAnimation();
                        if (upDragItem != null && !upDragItem.getType().equals(downDragItem.getType())) {
                            downDragItem.contentShrinkAnimation();
                            if (downDragItem.getForeground() != null)
                                downDragItem.getForeground().setState(STATE_PRESSED);
                        }
                    }
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageDelayed(LONG_PRESS, LONGPRESS_TIMEOUT);
                } else {
                    if (downDragItem != null) {
                        if (downDragItem.getForeground() != null)
                            downDragItem.getForeground().setState(STATE_ENABLE);

                        if (downDragItem.getTmpInfo() != null) {
                            downDragItem.tmpReplaceDismissAnimation();
                            downDragItem.setTmpInfo(null);
                        }
                        downDragItem.borderCancelledAnimation();
                        if (upDragItem != null && !upDragItem.getType().equals(downDragItem.getType())) {
                            if (upDragItem.getTmpInfo() != null) {
                                upDragItem.tmpDismissAnimation();
                                upDragItem.setTmpInfo(null);
                            }
                            downDragItem.contentAmplifyAnimation();
                        }
                    }
                    downDragItem = null;
                }
            }
        }
    }

    private void handleTouchActionUp(MotionEvent event) {
        mHandler.removeMessages(LONG_PRESS);
        if (mFloatView != null && mFloatView.isShowing())
            mFloatView.dismissAnimation();

        if (rippleItem != null && rippleItem.getForeground() != null)
            rippleItem.getForeground().setState(STATE_ENABLE);

        if (downDragItem != null && downDragItem.getForeground() != null)
            downDragItem.getForeground().setState(STATE_ENABLE);

        if (upDragItem != null) {
            if (downDragItem != null && downDragItem.getTmpInfo() != null && upDragItem.getTmpInfo() != null) {
                downDragItem.replaceTmpInfo();
                upDragItem.replaceTmpInfo();
                downDragItem.tmpReplaceSuccessAnimation();
                upDragItem.tmpReplaceSuccessAnimation();
            } else {
                upDragItem.contentDisplayAnimation();
                if (downDragItem != null && !upDragItem.getType().equals(downDragItem.getType())) {
                    downDragItem.contentAmplifyAnimation();
                }
            }
        }
        downDragItem = null;
        upDragItem = null;
        rippleItem = null;
    }

    //内存泄漏隐患
    class MyLongPressHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LONG_PRESS:
                    if (upDragItem != null && downDragItem != null && !upDragItem.getType().equals(downDragItem.getType()) && upDragItem.getTmpInfo() == null && downDragItem.getTmpInfo() == null) {
                        upDragItem.setTmpInfo(downDragItem.getInfo());
                        downDragItem.setTmpInfo(upDragItem.getInfo());
                        upDragItem.tmpDisplayAnimation();
                        downDragItem.tmpReplaceDisplayAnimation();
                        if (downDragItem.getForeground() != null)
                            downDragItem.getForeground().setState(STATE_ENABLE);
                    }
                    break;
            }
        }
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            isCanceled = false;
            rippleItem = getViewByTouchEvent(e.getX(), e.getY());
            if (rippleItem != null && rippleItem.getForeground() != null)
                rippleItem.getForeground().setState(STATE_PRESSED);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isCanceled) {
                DragItemView item = getViewByTouchEvent(e.getX(), e.getY());
                if (item != null && onItemClickListener != null) {
                    onItemClickListener.onItemClick(item.getType());
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!isCanceled) {
                upDragItem = getViewByTouchEvent(e.getX(), e.getY());
                downDragItem = upDragItem;
                if (upDragItem != null && upDragItem.getInfo() != null && mFloatView != null) {
                    float tmpOffsetX = e.getX() + mFloatView.getWidth() / 2, tmpOffsetY = e.getY() + mFloatView.getHeight() / 2;
                    mFloatView.bringToFront();
                    mFloatView.setTranslationX(tmpOffsetX);
                    mFloatView.setTranslationY(tmpOffsetY);
                    mFloatView.setInfo(upDragItem.getInfo());
                    mFloatView.displayAnimation();
                    upDragItem.contentDismissAnimation();
                    upDragItem.borderSelectedAnimation();
                }
            }
        }
    }

    public DragLayout.onItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public void setOnItemClickListener(DragLayout.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface onItemClickListener {
        void onItemClick(String type);
    }
}
