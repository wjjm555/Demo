package com.qinggan.cockpit.model;

import java.util.Vector;

public class StoryLine {

    private Vector<StoryPoint> mStoreList = new Vector<>();

    private StoryListener mListener = null;

    public void setListener(StoryListener listener){
        mListener = listener;
    }

    public void addEvent(float time, StoryEventListener listener){
        if(listener != null){
            mStoreList.addElement(new StoryPoint((int)(time * 1000), listener));
        }
    }

    public void reset(){
        for(int i = 0;i < mStoreList.size();++i){
            mStoreList.elementAt(i).mIsEnable = true;
        }
    }

    public void checkSeek(float time){
        reset();

        int msec = (int)(time * 1000);
        StoryPoint point;
        for(int i = 0;i < mStoreList.size();++i){
            point = mStoreList.elementAt(i);
            if(point.mTime < msec){
                point.mIsEnable = false;
            }
        }
    }

    public void onBegin(){
        reset();

        if(mListener != null){
            mListener.onStoryBegin();
        }
    }

    public void onEnd(){
        if(mListener != null){
            mListener.onStoryEnd();
        }
    }

    public void onUpdate(int time){
        boolean isHas = false;
        StoryPoint point = null;

        for(int i = 0;i < mStoreList.size();++i){
            point = mStoreList.elementAt(i);
            if(point.mIsEnable){
                isHas = true;
                break;
            }
        }

        if(isHas && point != null){
            if(point.mTime <= time){
                if(point.mListener != null){
                    point.mListener.onStoryHappened();
                }
                point.mIsEnable = false;
            }
        }
    }

    private class StoryPoint{
        private boolean mIsEnable;
        private int mTime;
        private StoryEventListener mListener;

        public StoryPoint(int time, StoryEventListener listener){
            mIsEnable = true;
            mTime = time;
            mListener = listener;
        }

    }

    public interface StoryListener{
        void onStoryBegin();
        void onStoryEnd();
    }

    public interface StoryEventListener{
        void onStoryHappened();
    }
}
