package com.qinggan.cockpit.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qinggan.cockpit.R;
import com.qinggan.cockpit.model.AppInfo;
import com.qinggan.cockpit.model.DMSCenter;
import com.qinggan.cockpit.model.DMSService;
import com.qinggan.cockpit.model.IBorrowListener;
import com.qinggan.cockpit.model.IDMSCenter;
import com.qinggan.cockpit.model.StoryLine;
import com.qinggan.cockpit.view.TouchView;


public class BorrowActivity extends MovieActivity implements StoryLine.StoryListener {

    private IDMSCenter mDMSCenter = null;

    private Bitmap mPicture;

    private IBorrowListener.Stub mBorrowListener = new IBorrowListener.Stub() {
        @Override
        public void onBorrowEvent(String imgPath) throws RemoteException {
            Log.i("DMSTest-Serve", "Borrow Path:" + imgPath);
            mPicture = BitmapFactory.decodeFile(imgPath);
            stopBorrow();
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("DMSTest-Serve", "[Borrow - onServiceConnected]");
            mDMSCenter = IDMSCenter.Stub.asInterface(service);
            registerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterListener();
            mDMSCenter = null;
            Log.i("DMSTest-Serve", "[Borrow - onServiceDisconnected]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_borrow);

        initMedia(AppInfo.PATH_BORROW, createStoryLine());

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

        mPicture = null;
        stopBorrow();
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
                mDMSCenter.registerBorrowListener(mBorrowListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void unregisterListener(){
        if(mDMSCenter != null){
            try {
                mDMSCenter.unregisterBorrowListener(mBorrowListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private StoryLine createStoryLine(){
        StoryLine storyLine = new StoryLine();
        storyLine.setListener(this);

        storyLine.addEvent(30, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startLoop(34.5f, 36.5f);

                TouchView touchView = findViewById(R.id.touchView);
                touchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(45, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startBorrow();
            }
        });

        storyLine.addEvent(46.8f, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                // 图片动画
                ImageView imageView = findViewById(R.id.img_borrow);
                if(mPicture != null){
                    imageView.setImageBitmap(mPicture);
                }
                startAnimation(imageView, R.anim.ani_borrow_img_in);

                // 图片底部的名称栏动画
                ImageView imgBar = findViewById(R.id.bar_img);
                startAnimation(imgBar, R.anim.ani_borrow_bar_in);

                LinearLayout layout = findViewById(R.id.check_face);
                layout.setVisibility(View.VISIBLE);

                /*DMSCenter dmsCenter = DMSCenter.getInstance();
                dmsCenter.startBorrowCheck(new DMSCenter.BorrowCheckListener() {
                    @Override
                    public void onCheckBorrowSuccess() {
                        resumeMovie();
                    }

                    @Override
                    public void onCheckBorrowFail() {
                        resumeMovie();
                    }
                });*/
            }
        });

        storyLine.addEvent(56, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                LinearLayout layoutAlert = findViewById(R.id.img_alert);
                layoutAlert.setVisibility(View.VISIBLE);
                startAnimation(layoutAlert, R.anim.ani_borrow_alert_in);
            }
        });

        storyLine.addEvent(58.5f, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                LinearLayout layout = findViewById(R.id.check_face);
                layout.setVisibility(View.GONE);
                startAnimation(layout, R.anim.ani_borrow_img_out);

                LinearLayout layoutAlert = findViewById(R.id.img_alert);
                layoutAlert.setVisibility(View.GONE);
            }
        });

        storyLine.addEvent(73, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                TouchView endTouchView = findViewById(R.id.touchView_end);
                endTouchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(76, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                pauseMovie();
            }
        });

        return storyLine;
    }

    @Override
    public void onStoryBegin() {

        // 汽车点火按钮控制
        TouchView touchView = findViewById(R.id.touchView);
        touchView.setVisibility(View.GONE);
        touchView.addTouchArea(
                newRect((AppInfo.DisplayWidth - 320)/2, (AppInfo.DisplayHeight - 320)/2, 320 , 320),
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                stopLoop();
            }
        });

        // 结束时的事件控制
        TouchView endTouchView = findViewById(R.id.touchView_end);
        endTouchView.setVisibility(View.GONE);
        endTouchView.addTouchArea(newRect(110, 438, 218, 66), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMovie();
            }
        });

        endTouchView.addTouchArea(newRect(390, 438, 218, 66), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout layout = findViewById(R.id.check_face);
        layout.setVisibility(View.GONE);

        LinearLayout layoutAlert = findViewById(R.id.img_alert);
        layoutAlert.setVisibility(View.GONE);
    }

    @Override
    public void onStoryEnd() {

    }

    private void startBorrow(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.startCheckBorrow();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void stopBorrow(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.stopCheckBorrow();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
