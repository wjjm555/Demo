package com.qinggan.cockpit.model;

import android.graphics.Bitmap;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class DMSCenter {

    private static DMSCenter sDMSCenter = new DMSCenter();

    public static DMSCenter getInstance(){
        return sDMSCenter;
    }

    private Timer mTimer;

    public enum FatigueType{
        LOW, MEDIUM, HIGH
    }

    private DMSCenter(){

    }

    /**
     *
     * @param listener
     * @return
     */
    public boolean startBorrowCheck(final BorrowCheckListener listener){
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(listener != null){
                    listener.onCheckBorrowFail();
                }
            }
        }, 5000);

        return true;
    }

    /**
     *
     */
    public void stopBorrowCheck(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void startFatigueCheck(final FatigueCheckListener listener){
        if(mTimer != null){
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(listener != null){

                    FatigueType type;
                    Random random = new Random();
                    int index = random.nextInt(3);
                    if(index == 0){
                        type = FatigueType.LOW;
                    }
                    else if(index == 1){
                        type = FatigueType.MEDIUM;
                    }
                    else{
                        type = FatigueType.HIGH;
                    }

                    listener.onFindFatigue(FatigueType.HIGH);
                }
            }
        }, 5000);
    }

    public void stopFatigueCheck(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     *
     */
    public interface BorrowCheckListener{
        void onCheckBorrowSuccess();
        void onCheckBorrowFail();
    }

    public interface FatigueCheckListener{
        void onFindFatigue(FatigueType type);
    }
}
