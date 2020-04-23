package com.qinggan.cockpit.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.qinggan.cockpit.R;
import com.qinggan.cockpit.model.AppInfo;
import com.qinggan.cockpit.model.DMSService;
import com.qinggan.cockpit.model.IDMSCenter;
import com.qinggan.cockpit.model.ILiveSharingListener;
import com.qinggan.cockpit.model.StoryLine;
import com.qinggan.cockpit.view.TouchView;

public class LiveSharingActivity extends MovieActivity implements StoryLine.StoryListener {

    private IDMSCenter mDMSCenter = null;

    private Bitmap mPicture;

    private ILiveSharingListener.Stub mLiveSharingListener = new ILiveSharingListener.Stub() {
        @Override
        public void onLiveSharingEvent(String imgPath) throws RemoteException {
            Log.i("DMSTest-Serve", "LiveSharing Path:" + imgPath);
            mPicture = BitmapFactory.decodeFile(imgPath);
            stopLiveSharing();
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("DMSTest-Serve", "[LiveSharing - onServiceConnected]");
            mDMSCenter = IDMSCenter.Stub.asInterface(service);
            registerListener();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterListener();
            mDMSCenter = null;
            Log.i("DMSTest-Serve", "[LiveSharing - onServiceDisconnected]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_sharing);

        initMedia(AppInfo.PATH_LIVE_SHARING, createStoryLine());

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

        startLiveSharing();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopLiveSharing();
        mPicture = null;
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
                mDMSCenter.registerLiveSharingListener(mLiveSharingListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void unregisterListener(){
        if(mDMSCenter != null){
            try {
                mDMSCenter.unregisterLiveSharingListener(mLiveSharingListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void startMovePic(){
        LinearLayout layoutBox = findViewById(R.id.layout_img_box);
        startAnimation(layoutBox, R.anim.ani_livesharing_pic_out, new AniListener() {
            @Override
            public void onAnimationEnd() {
                LinearLayout layoutImage = findViewById(R.id.layout_image);
                layoutImage.setVisibility(View.GONE);
            }
        });
    }

    private StoryLine createStoryLine(){
        StoryLine storyLine = new StoryLine();
        storyLine.setListener(this);

        storyLine.addEvent(35, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startLoop(37.8f, 39f);

                TouchView touchView = findViewById(R.id.touchView);
                touchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(44, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startLiveSharing();
            }
        });

        storyLine.addEvent(45, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                View view = findViewById(R.id.view_flash);
                view.setVisibility(View.VISIBLE);

                startAnimation(view, R.anim.ani_flash, new AniListener() {
                    @Override
                    public void onAnimationEnd() {
                        View view = findViewById(R.id.view_flash);
                        view.setVisibility(View.GONE);

                        LinearLayout layoutImage = findViewById(R.id.layout_image);
                        layoutImage.setVisibility(View.VISIBLE);

                        ImageView imageView = findViewById(R.id.img_picture);
                        if(mPicture != null){
                            imageView.setImageBitmap(mPicture);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startMovePic();
                            }
                        }, 1000);
                    }
                });

                LinearLayout layoutBox = findViewById(R.id.layout_img_box);
                startAnimation(layoutBox, R.anim.ani_livesharing_pic_zoom);

            }
        });

        storyLine.addEvent(56, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                TouchView endTouchView = findViewById(R.id.touchView_end);
                endTouchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(58, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                pauseMovie();
            }
        });

        return storyLine;
    }

    @Override
    public void onStoryBegin() {
        TouchView touchView = findViewById(R.id.touchView);
        touchView.setVisibility(View.GONE);
        touchView.addTouchArea(newRect(480, 70, 600 , 520), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                stopLoop();
            }
        });

        // 结束时的事件控制
        TouchView endTouchView = findViewById(R.id.touchView_end);
        endTouchView.setVisibility(View.GONE);
        endTouchView.addTouchArea(newRect(125, 445, 215, 70), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetMovie();
                startLiveSharing();
            }
        });

        endTouchView.addTouchArea(newRect(365, 445, 215, 70), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout layoutImage = findViewById(R.id.layout_image);
        layoutImage.setVisibility(View.GONE);

        View view = findViewById(R.id.view_flash);
        view.setVisibility(View.GONE);
    }

    @Override
    public void onStoryEnd() {
    }

    private void startLiveSharing(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.startCheckLiveSharing();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void stopLiveSharing(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.stopCheckLiveSharing();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
