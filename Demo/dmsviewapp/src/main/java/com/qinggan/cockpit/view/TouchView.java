package com.qinggan.cockpit.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Vector;

public class TouchView extends View {

    private Vector<TouchArea> mTouchList = new Vector<>();

    private boolean mIsDebug = false;

    public TouchView(Context context) {
        super(context);

        initWithPrivate();
    }

    public TouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initWithPrivate();
    }

    private void initWithPrivate(){
        setClickable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){

            boolean isDispatch = false;
            TouchArea touchArea;
            for(int i = 0;i < mTouchList.size();++i){
                touchArea = mTouchList.elementAt(i);
                if(touchArea.dispatchEvent(event.getX(), event.getY())){
                    isDispatch = true;
                    break;
                }
            }
            return isDispatch;
        }
        else{
            return super.onTouchEvent(event);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mIsDebug){
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0xaaffffff);

            TouchArea touchArea;
            for(int i = 0;i < mTouchList.size();++i){
                touchArea = mTouchList.elementAt(i);
                canvas.drawRect(touchArea.mRect, paint);
            }
        }
    }

    public void setDebug(boolean enable){
        mIsDebug = enable;
        postInvalidate();
    }

    public void addTouchArea(RectF rect, OnClickListener listener){
        if(rect != null){
            mTouchList.addElement(new TouchArea(rect, listener));
        }
    }

    private class TouchArea{
        private RectF mRect;
        private OnClickListener mListener;

        public TouchArea(RectF rect,OnClickListener listener){
            mRect = rect;
            mListener = listener;
        }

        public boolean dispatchEvent(float x, float y){
            boolean isSuccess = false;
            if(mRect.contains(x, y)){
                if(mListener != null){
                    mListener.onClick(TouchView.this);
                }
                isSuccess = true;
            }
            return isSuccess;
        }
    }
}
