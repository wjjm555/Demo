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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qinggan.cockpit.R;
import com.qinggan.cockpit.model.AppInfo;
import com.qinggan.cockpit.model.DMSService;
import com.qinggan.cockpit.model.IDMSCenter;
import com.qinggan.cockpit.model.IHitchhikeListener;
import com.qinggan.cockpit.model.StoryLine;
import com.qinggan.cockpit.view.TouchView;

import java.util.Timer;
import java.util.TimerTask;


public class HitchhikeActivity extends MovieActivity implements StoryLine.StoryListener {

    private IDMSCenter mDMSCenter = null;

    private Bitmap mPicture;

    private IHitchhikeListener.Stub mHitchhikeListener = new IHitchhikeListener.Stub() {
        @Override
        public void onHitchhikeEvent(String imgPath) throws RemoteException {
            Log.i("DMSTest-Serve", "Hitchhike Path:" + imgPath);
            mPicture = BitmapFactory.decodeFile(imgPath);
            stopHitchhike();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hitchhike);

        initMedia(AppInfo.PATH_HITCHHIKE, createStoryLine());

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
    protected void onResume() {
        super.onResume();

        startHitchhike();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPicture = null;
        stopHitchhike();
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
                mDMSCenter.registerHitchhikeListener(mHitchhikeListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void unregisterListener(){
        if(mDMSCenter != null){
            try {
                mDMSCenter.unregisterHitchhikeListener(mHitchhikeListener);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private StoryLine createStoryLine(){
        StoryLine storyLine = new StoryLine();
        storyLine.setListener(this);

        storyLine.addEvent(29, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startLoop(31f, 32.5f);

                TouchView touchView = findViewById(R.id.touchView);
                touchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(46, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startHitchhike();
            }
        });

        storyLine.addEvent(48, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                startCaptureAni();
            }
        });

        storyLine.addEvent(59, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                FrameLayout layoutMsg = findViewById(R.id.layout_msg);
                layoutMsg.setVisibility(View.GONE);

                LinearLayout layoutText = findViewById(R.id.layout_msg_text);
                layoutText.setVisibility(View.GONE);
            }
        });

        storyLine.addEvent(60, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                TouchView endTouchView = findViewById(R.id.touchView_end);
                endTouchView.setVisibility(View.VISIBLE);
            }
        });

        storyLine.addEvent(62, new StoryLine.StoryEventListener() {
            @Override
            public void onStoryHappened() {
                pauseMovie();
            }
        });

        return storyLine;
    }


    private void startCaptureAni(){
        LinearLayout layoutImage = findViewById(R.id.layout_image);
        layoutImage.setVisibility(View.VISIBLE);

        View view = findViewById(R.id.view_flash);
        view.setVisibility(View.VISIBLE);

        startAnimation(view, R.anim.ani_flash, new AniListener() {
            @Override
            public void onAnimationEnd() {
                View view = findViewById(R.id.view_flash);
                view.setVisibility(View.GONE);
            }
        });

        ImageView imageView = findViewById(R.id.img_picture);
        if(mPicture != null){
            imageView.setImageBitmap(mPicture);
        }

        LinearLayout layoutImg = findViewById(R.id.layout_picture);
        layoutImg.setVisibility(View.VISIBLE);
        startAnimation(layoutImg, R.anim.ani_hitchhike_pic_zoom, new AniListener() {
            @Override
            public void onAnimationEnd() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMovePicAni();
                    }
                }, 500);
            }
        });
    }

    private void startMovePicAni(){
        LinearLayout layoutImg = findViewById(R.id.layout_picture);
        startAnimation(layoutImg, R.anim.ani_hitchhike_pic_out, new AniListener() {
            @Override
            public void onAnimationEnd() {
                LinearLayout layoutImg = findViewById(R.id.layout_picture);
                layoutImg.setVisibility(View.GONE);

                startShowMsg();
            }
        });
    }

    private void startShowMsg(){
        FrameLayout layoutMsg = findViewById(R.id.layout_msg);
        layoutMsg.setVisibility(View.VISIBLE);
        LinearLayout layoutMsgBox = findViewById(R.id.layout_msg_box);
        layoutMsgBox.setVisibility(View.VISIBLE);

        ImageView imageView = findViewById(R.id.img_center);
        if(mPicture != null){
            imageView.setImageBitmap(mPicture);
        }

        startAnimation(layoutMsg, R.anim.ani_hitchhike_msg_in, new AniListener() {
            @Override
            public void onAnimationEnd() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startMoveMsg();
                    }
                }, 4000);
            }
        });
    }

    private void startMoveMsg(){
        LinearLayout layoutBox = findViewById(R.id.layout_msg_box);
        layoutBox.setVisibility(View.VISIBLE);
        startAnimation(layoutBox, R.anim.ani_hitchhike_msg_out, new AniListener() {
            @Override
            public void onAnimationEnd() {
                LinearLayout layout = findViewById(R.id.layout_msg_box);
                layout.setVisibility(View.GONE);
            }
        });

        LinearLayout layoutText = findViewById(R.id.layout_msg_text);
        layoutText.setVisibility(View.VISIBLE);
        startAnimation(layoutText, R.anim.ani_hitchhike_text_in);

        LinearLayout layoutPhone = findViewById(R.id.layout_phone);
        startAnimation(layoutPhone, R.anim.ani_hitchhike_phone_an1, new AniListener() {
            @Override
            public void onAnimationEnd() {
                TextView textView = findViewById(R.id.text_msg);
                textView.setText("Message\n confirmation");
                startShowOK();

                LinearLayout layoutPhone = findViewById(R.id.layout_phone);
                startAnimation(layoutPhone, R.anim.ani_hitchhike_phone_an2);
            }
        });
    }

    private void startShowOK(){
        ImageView imageView = findViewById(R.id.img_hitchhike_ok);
        imageView.setVisibility(View.VISIBLE);
        startAnimation(imageView, R.anim.ani_hitchhike_ok_zoom);
    }

    @Override
    public void onStoryBegin() {

        // 导航按钮
        TouchView touchView = findViewById(R.id.touchView);
        touchView.setVisibility(View.GONE);
        touchView.addTouchArea(newRect(495, 470, 390, 95), new View.OnClickListener() {
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
                startHitchhike();
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

        LinearLayout layoutImg = findViewById(R.id.layout_picture);
        layoutImg.setVisibility(View.GONE);

        View view = findViewById(R.id.view_flash);
        view.setVisibility(View.GONE);

        FrameLayout layoutMsg = findViewById(R.id.layout_msg);
        layoutMsg.setVisibility(View.GONE);

        LinearLayout layoutText = findViewById(R.id.layout_msg_text);
        layoutText.setVisibility(View.GONE);

        TextView textView = findViewById(R.id.text_msg);
        textView.setText("A message\n from Yuna's DMS");

        ImageView imageView = findViewById(R.id.img_hitchhike_ok);
        imageView.setVisibility(View.GONE);
    }

    @Override
    public void onStoryEnd() {

    }

    private void startHitchhike(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.startCheckHitchhike();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private void stopHitchhike(){
        if(mDMSCenter != null){
            try{
                mDMSCenter.stopCheckHitchhike();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
