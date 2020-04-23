package com.qinggan.cockpit.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


import com.qinggan.cockpit.R;
import com.qinggan.cockpit.model.StoryLine;

import java.util.Timer;
import java.util.TimerTask;


public class MovieActivity extends BaseActivity implements SurfaceHolder.Callback {

    private MediaPlayer mMediaPlayer = null;

    private String mMoviePath = null;

    private Timer mTimer = null;

    private StoryLine mStoreLine = null;

    private int mLoopBegin = -1;
    private int mLoopEnd = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_borrow);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createMedia();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMedia();
    }

    @Override
    public void surfaceCreated(final SurfaceHolder holder) {
        try{
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                    mMediaPlayer.setDisplay(holder);
                    if(mStoreLine != null){
                        mStoreLine.onBegin();
                    }
                    mMediaPlayer.start();
                    startTimer();
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    protected void initMedia(String path, StoryLine storyLine){
        mMoviePath = path;
        mStoreLine = storyLine;
    }

    private void createMedia(){
        try {
            SurfaceView surfaceView = findViewById(R.id.surface_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.setKeepScreenOn(true);
            surfaceHolder.addCallback(this);

            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(mStoreLine != null){
                        mStoreLine.onEnd();
                    }
                }
            });
            mMediaPlayer.setDataSource(mMoviePath);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void releaseMedia(){
        try {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mLoopBegin = -1;
            mLoopEnd = -1;

            stopTimer();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startTimer(){
        stopTimer();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateProcess();
            }
        }, 0, 50);
    }

    private void stopTimer(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void updateProcess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mMediaPlayer != null) {
                    int time = mMediaPlayer.getCurrentPosition();
                    if (mLoopBegin == -1 && mLoopEnd == -1) {
                        if (mStoreLine != null) {
                            mStoreLine.onUpdate(time);
                        }
                    } else {
                        if (time >= mLoopEnd) {
                            mMediaPlayer.seekTo(mLoopBegin);
                        }
                    }
                }
            }
        });
    }

    protected void startLoop(float toTime){
        mLoopBegin = (int)(toTime * 1000);
        mLoopEnd = mMediaPlayer.getCurrentPosition();
        if(mLoopEnd <= mLoopBegin){
            stopLoop();
        }
    }

    protected void startLoop(float begin, float end){
        mLoopBegin = (int)(begin * 1000);
        mLoopEnd = (int)(end * 1000);
        if(mLoopEnd <= mLoopBegin){
            stopLoop();
        }
    }

    protected void stopLoop(){
        mMediaPlayer.seekTo(mLoopEnd + 1000);
        mLoopBegin = -1;
        mLoopEnd = -1;
    }

    protected void pauseMovie(){
        if(mMediaPlayer != null){
            mMediaPlayer.pause();
        }
    }

    protected void resumeMovie(){
        if(mMediaPlayer != null){
            mMediaPlayer.start();
        }
    }

    protected void seekTo(float time){
        if(mStoreLine != null){
            mStoreLine.checkSeek(time);
        }
        if(mMediaPlayer != null){
            mMediaPlayer.seekTo((int)(time * 1000));
        }
    }

    protected void resetMovie(){
        if(mStoreLine != null){
            mStoreLine.reset();
            mStoreLine.onBegin();
        }
        if(mMediaPlayer != null) {
            mMediaPlayer.start();
            mMediaPlayer.seekTo(0);
        }
    }

    protected void resetStoryLine(){
        if(mStoreLine != null){
            mStoreLine.reset();
        }
    }

}
