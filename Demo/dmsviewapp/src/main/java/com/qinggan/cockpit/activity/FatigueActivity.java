package com.qinggan.cockpit.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.qinggan.cockpit.R;
import com.qinggan.cockpit.model.AppInfo;
import com.qinggan.cockpit.model.DMSCenter;
import com.qinggan.cockpit.model.DMSService;
import com.qinggan.cockpit.model.IDMSCenter;
import com.qinggan.cockpit.model.IFatigueListener;
import com.qinggan.cockpit.model.StoryLine;
import com.qinggan.cockpit.view.TouchView;


public class FatigueActivity extends MovieActivity implements StoryLine.StoryListener  {

    private IDMSCenter mDMSCenter = null;

    private IFatigueListener.Stub mFatigueListener = new IFatigueListener.Stub() {
        @Override
        public void onFatigueEvent(int level) throws RemoteException {
            if(level == AppInfo.FATIGUE_LOW){
                stopFatigue();
                stopLoop();
            }
            else if(level == AppInfo.FATIGUE_MEDIUM){
                stopFatigue();
                stopLoop();

                seekTo(83);
            }
            else if(level == AppInfo.FATIGUE_HIGH){
                stopFatigue();
                stopLoop();

                seekTo(130);
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("DMSTest-Serve", "[Fatigue - onServiceConnected]");
            mDMSCenter = IDMSCenter.Stub.asInterface(service);
            registerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterListener();
            mDMSCenter = null;
            Log.i("DMSTest-Serve", "[Home - onServiceDisconnected]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fatigue);

        initMedia(AppInfo.PATH_FATIGUE, createStoryLine());

        ImageView imageView = findViewById(R.id.btn_exit);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // 连接服务
        Intent intent = new Intent();
        intent.setClass(this, DMSService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopFatigue();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterListener();
        unbindService(mServiceConnection);
    }

    private void registerListener(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.registerFatigueListener(mFatigueListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void unregisterListener(){
        if(mDMSCenter != null){
            try {
                mDMSCenter.unregisterFatigueListener(mFatigueListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private StoryLine createStoryLine(){
        StoryLine storyLine = new StoryLine();
        storyLine.setListener(this);

        storyLine.addEvent(24, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startLoop(25, 42);
                startFatigue();

//                DMSCenter dmsCenter = DMSCenter.getInstance();
//                dmsCenter.startFatigueCheck(new DMSCenter.FatigueCheckListener() {
//                    @Override
//                    public void onFindFatigue(DMSCenter.FatigueType type) {
//
//                        DMSCenter dmsCenter = DMSCenter.getInstance();
//                        dmsCenter.stopFatigueCheck();
//
//                        stopLoop();
//
//                        if(type == DMSCenter.FatigueType.LOW){
//                            //seekTo(45);
//                        }
//                        else if(type == DMSCenter.FatigueType.MEDIUM){
//                            seekTo(83);
//                        }
//                        else if(type == DMSCenter.FatigueType.HIGH){
//                            seekTo(130);
//                        }
//                    }
//                });
            }
        });

        storyLine.addEvent(77, mStartListener);
        storyLine.addEvent(81, mPauseListener);

        storyLine.addEvent(123, mStartListener);
        storyLine.addEvent(129, mPauseListener);

        storyLine.addEvent(175, mStartListener);
        storyLine.addEvent(178, mPauseListener);

        return storyLine;
    }

    private StoryLine.StoryEventListener mStartListener = new StoryLine.StoryEventListener() {
        @Override
        public void onStoryHappened() {
            TouchView touchView = findViewById(R.id.touchView);
            touchView.setVisibility(View.VISIBLE);
        }
    };

    private StoryLine.StoryEventListener mPauseListener = new StoryLine.StoryEventListener() {
        @Override
        public void onStoryHappened() {
            pauseMovie();
        }
    };

    @Override
    public void onStoryBegin() {

        TouchView touchView = findViewById(R.id.touchView);
        touchView.setVisibility(View.GONE);
        touchView.addTouchArea(newRect(145, 300, 550 , 125), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);

                seekTo(21);
                resumeMovie();
            }
        });
    }

    @Override
    public void onStoryEnd() {
    }

    private void startFatigue(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.startCheckFatigue();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void stopFatigue(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.stopCheckFatigue();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
