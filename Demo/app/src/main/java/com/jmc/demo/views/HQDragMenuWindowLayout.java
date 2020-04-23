package com.jmc.demo.views;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.jmc.demo.interfaces.DragPercentListener;

import java.lang.ref.WeakReference;

public class HQDragMenuWindowLayout extends LinearLayout {

    private final int DURATION = 200;

    private final int MSGWHAT_ACTIONUP = 0x1001, MSGWHAT_AUTODISMISS = 0x1010, MSGWHAT_CLOSE = 0x1100;

    private final float AUTOTHRESHOLD = 1 / 3f, MINVELOCITY = 1000;

    private final long ACTIONUP_DELAYMILLIS = 100, AUDODISMISS_DELAYMILLIS = 1000 * 10;

    private boolean ignoreActionUp, autoDismiss;

    private GestureDetector gestureDetector;

    private WindowManager mWindowManager;

    private WindowManager.LayoutParams params;

    private View controlView, contentView;

    private Handler mHandler = new MyHandler(this);

    private int mTouchDownPosition;

    private int screenWidth, screenHeight;

    private boolean isFirst = true, isShowing;

    private DragPercentListener dragPercentListener;

    private State state = State.CLOSE;


    private enum State {
        OPEN, CLOSE
    }

    public HQDragMenuWindowLayout(@NonNull Context context, int screenWidth, int screenHeight) {
        super(context);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        gestureDetector = new GestureDetector(getContext(), new MySimpleOnGestureListener());
        mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    }

    public void addView(int orientation, View controlView, View contentView) {
        this.removeAllViews();
        this.controlView = controlView;
        this.contentView = contentView;
        setOrientation(orientation);

        LayoutParams controlParams, contentParams;

        if (orientation == LinearLayout.HORIZONTAL) {
            controlParams = new LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
            contentParams = new LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);

            addView(contentView, contentParams);
            addView(controlView, controlParams);
        } else {
            controlParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            contentParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            addView(controlView, controlParams);
            addView(contentView, contentParams);
        }

    }


    public void show() {
        params = new WindowManager.LayoutParams();
        params.format = PixelFormat.TRANSLUCENT;
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if (getOrientation() == LinearLayout.HORIZONTAL) {
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.START;
        } else {
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.gravity = Gravity.TOP;
        }

        controlView.setAlpha(1);

        mWindowManager.addView(this, params);
        isShowing = true;
        isFirst = true;
    }

    public void dismiss() {
        mWindowManager.removeView(this);
        isShowing = false;
    }

    public void closeDelay(long delayMillis) {
        mHandler.sendEmptyMessageDelayed(MSGWHAT_CLOSE, delayMillis);
    }


    public boolean isAutoDismiss() {
        return autoDismiss;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        this.autoDismiss = autoDismiss;
    }

    public DragPercentListener getDragPercentListener() {
        return dragPercentListener;
    }

    public void setDragPercentListener(DragPercentListener dragPercentListener) {
        this.dragPercentListener = dragPercentListener;
    }

    public boolean isShowing() {
        return isShowing;
    }

    private boolean isOpen() {
        return state == State.OPEN;
    }

    private boolean isClose() {
        return state == State.CLOSE;
    }

    private void open() {
        state = State.OPEN;
        int start, end;
        if (getOrientation() == LinearLayout.HORIZONTAL) {
            start = params.x;
            end = 0;
        } else {
            start = params.y;
            end = screenHeight - getHeight();
        }
        executeAnimation(start, end);

        if (dragPercentListener != null)
            dragPercentListener.onDragPercentSet(1, controlView, contentView);

        if (autoDismiss)
            mHandler.sendEmptyMessageDelayed(MSGWHAT_AUTODISMISS, AUDODISMISS_DELAYMILLIS);
    }

    private void close() {
        state = State.CLOSE;
        int start, end;
        if (getOrientation() == LinearLayout.HORIZONTAL) {
            start = params.x;
            end = -contentView.getWidth();
        } else {
            start = params.y;
            end = screenHeight - controlView.getHeight();

        }
        executeAnimation(start, end);

        if (dragPercentListener != null)
            dragPercentListener.onDragPercentSet(0, controlView, contentView);
    }

    private void executeAnimation(int start, int end) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(DURATION).start();
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                if (getOrientation() == LinearLayout.HORIZONTAL) {
                    updateX(value);
                } else {
                    updateY(value);
                }
            }
        });
    }

    private void judgeTouchUp() {

        if (ignoreActionUp) {
            ignoreActionUp = false;
            return;
        }

        float translation, total, offset;

        if (getOrientation() == LinearLayout.HORIZONTAL) {
            translation = Math.abs(params.x);
            total = contentView.getWidth();
        } else {
            translation = Math.abs(screenHeight - params.y - getHeight());
            total = contentView.getHeight();
        }

        if (isOpen())
            offset = total * AUTOTHRESHOLD;
        else
            offset = total * (1 - AUTOTHRESHOLD);

        if (translation > offset)
            close();
        else
            open();
    }

    private void updateX(int x) {
        params.x = x;
        mWindowManager.updateViewLayout(this, params);

    }

    private void updateY(int y) {
        params.y = y;
        mWindowManager.updateViewLayout(this, params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mHandler.sendEmptyMessageDelayed(MSGWHAT_ACTIONUP, ACTIONUP_DELAYMILLIS);
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > 0 && isFirst) {
            isFirst = false;
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                updateX(-contentView.getMeasuredWidth());
            } else {
                updateY(screenHeight - controlView.getMeasuredHeight());
            }
        }
    }

    class MySimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            mHandler.removeMessages(MSGWHAT_AUTODISMISS);
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                mTouchDownPosition = (int) e.getX();
            } else {
                mTouchDownPosition = (int) e.getY();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float position;
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                position = e.getX();
                if (position < controlView.getX()) {
                    return false;
                }

            } else {
                position = e.getY();
                if (position > controlView.getY()) {
                    return false;
                }
            }

            if (isClose()) {
                open();
                return true;
            }

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float position;
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                position = e2.getRawX() - mTouchDownPosition;
                if (position > 0) {
                    position = 0;
                } else if (position < -contentView.getWidth()) {
                    position = -contentView.getWidth();
                }
                updateX((int) position);
            } else {
                position = e2.getRawY() - mTouchDownPosition;
                if (position > screenHeight - controlView.getMeasuredHeight()) {
                    position = screenHeight - controlView.getMeasuredHeight();
                } else if (position < screenHeight - getHeight()) {
                    position = screenHeight - getHeight();
                }
                updateY((int) position);
            }

            float percent;
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                percent = (contentView.getWidth() - Math.abs(params.x)) / ((float) contentView.getWidth());
            } else {
                percent = (screenHeight - params.y - controlView.getHeight()) / ((float) contentView.getHeight());
            }
            if (dragPercentListener != null)
                dragPercentListener.onDragPercentChanged(percent, controlView, contentView);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (getOrientation() == LinearLayout.HORIZONTAL) {
                if (Math.abs(velocityX) > MINVELOCITY) {
                    ignoreActionUp = true;
                    if (velocityX > 0)
                        open();
                    else
                        close();
                }
            } else {
                if (Math.abs(velocityY) > MINVELOCITY) {
                    ignoreActionUp = true;
                    if (velocityY > 0)
                        close();
                    else
                        open();
                }
            }
            return true;
        }
    }

    static class MyHandler extends Handler {

        private final WeakReference<HQDragMenuWindowLayout> mTarget;

        MyHandler(HQDragMenuWindowLayout mTarget) {
            super(Looper.getMainLooper());
            this.mTarget = new WeakReference<>(mTarget);
        }

        @Override
        public void handleMessage(Message msg) {
            HQDragMenuWindowLayout layout = mTarget.get();
            if (layout != null) {
                if (msg.what == layout.MSGWHAT_ACTIONUP)
                    layout.judgeTouchUp();
                else if (msg.what == layout.MSGWHAT_AUTODISMISS) {
                    if (layout.isOpen())
                        layout.close();
                }
            }
        }
    }
}
