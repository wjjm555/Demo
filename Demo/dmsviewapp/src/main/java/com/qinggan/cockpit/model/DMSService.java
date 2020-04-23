package com.qinggan.cockpit.model;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.util.Log;


public class DMSService extends Service {

    private RemoteCallbackList<IClientListener> mClientCBList = new RemoteCallbackList<>();

    private RemoteCallbackList<ILoginListener> mLoginCBList = new RemoteCallbackList<>();

    private RemoteCallbackList<IFatigueListener> mFatigueCBList = new RemoteCallbackList<>();

    private RemoteCallbackList<IBorrowListener> mBorrowCBList = new RemoteCallbackList<>();

    private RemoteCallbackList<ILiveSharingListener> mLiveSharingCBList = new RemoteCallbackList<>();

    private RemoteCallbackList<IHitchhikeListener> mHitchhikeCBList = new RemoteCallbackList<>();

    private int mCurMode = AppInfo.MODE_UNDEF;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private IDMSCenter.Stub mBinder = new IDMSCenter.Stub() {

        public void loginInUser(String name, String iconPath, int age, int sex){
            try {
                int count = mLoginCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mLoginCBList.getBroadcastItem(i).onLoginInUser(name, iconPath, age, sex);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mLoginCBList.finishBroadcast();
            }
        }

        public void loginInUnknown(String iconPath, int age, int sex){
            try {
                int count = mLoginCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mLoginCBList.getBroadcastItem(i).onLoginInUnknown(iconPath, age, sex);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mLoginCBList.finishBroadcast();
            }
        }

        public void logoutInAuto(){
            try {
                int count = mLoginCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mLoginCBList.getBroadcastItem(i).onLogoutInAuto();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mLoginCBList.finishBroadcast();
            }
        }

        public int getCurMode(){
            return mCurMode;
        }

        public boolean requestRegisterUser(String name, String pwd){
            boolean isSuccess = false;
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    isSuccess = mClientCBList.getBroadcastItem(i).onRegisterUser(name, pwd);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
            return isSuccess;
        }

        public boolean requestLogout(String name){
            boolean isSuccess = false;
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    isSuccess = mClientCBList.getBroadcastItem(i).onLogout(name);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
            return isSuccess;
        }

        public boolean requestRemoveUser(String name){
            boolean isSuccess = false;
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    isSuccess = mClientCBList.getBroadcastItem(i).onRemoveUser(name);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
            return isSuccess;
        }

        public void postFatigueEvent(int level){
            try {
                int count = mFatigueCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mFatigueCBList.getBroadcastItem(i).onFatigueEvent(level);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mFatigueCBList.finishBroadcast();
            }
        }

        public void postBorrowEvent(String imgPath){
            try {
                int count = mBorrowCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mBorrowCBList.getBroadcastItem(i).onBorrowEvent(imgPath);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mBorrowCBList.finishBroadcast();
            }
        }

        public void postLiveSharingEvent(String imgPath){
            try {
                int count = mLiveSharingCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mLiveSharingCBList.getBroadcastItem(i).onLiveSharingEvent(imgPath);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mLiveSharingCBList.finishBroadcast();
            }
        }

        public void postHitchhikeEvent(String imgPath){
            try {
                int count = mHitchhikeCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mHitchhikeCBList.getBroadcastItem(i).onHitchhikeEvent(imgPath);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mHitchhikeCBList.finishBroadcast();
            }
        }

        public void startCheckLogin(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStartCheckLogin();
                }

                mCurMode = AppInfo.MODE_LOGIN;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void stopCheckLogin(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStopCheckLogin();
                }

                mCurMode = AppInfo.MODE_UNDEF;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void startCheckFatigue(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStartCheckFatigue();
                }

                mCurMode = AppInfo.MODE_FATIGUE;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void stopCheckFatigue(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStopCheckFatigue();
                }

                mCurMode = AppInfo.MODE_UNDEF;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void startCheckBorrow(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStartCheckBorrow();
                }

                mCurMode = AppInfo.MODE_BORROW;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void stopCheckBorrow(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStopCheckBorrow();
                }

                mCurMode = AppInfo.MODE_UNDEF;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void startCheckLiveSharing(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStartCheckLiveSharing();
                }

                mCurMode = AppInfo.MODE_LIVESHARING;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void stopCheckLiveSharing(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStopCheckLiveSharing();
                }

                mCurMode = AppInfo.MODE_UNDEF;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void startCheckHitchhike(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStartCheckHitchhike();
                }

                mCurMode = AppInfo.MODE_HITCHHIKE;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void stopCheckHitchhike(){
            try {
                int count = mClientCBList.beginBroadcast();
                for (int i = 0; i < count; ++i) {
                    mClientCBList.getBroadcastItem(i).onStopCheckHitchhike();
                }

                mCurMode = AppInfo.MODE_UNDEF;
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                mClientCBList.finishBroadcast();
            }
        }

        public void registerLoginListener(ILoginListener listener){
            if(listener != null) {
                mLoginCBList.register(listener);
            }
        }

        public void unregisterLoginListener(ILoginListener listener){
            if(listener != null) {
                mLoginCBList.unregister(listener);
            }
        }

        public void registerClientListener(IClientListener listener){
            if(listener != null){
                mClientCBList.register(listener);
            }
        }

        public void unregisterClientListener(IClientListener listener){
            if(listener != null){
                mClientCBList.unregister(listener);
            }
        }

        public void registerFatigueListener(IFatigueListener listener){
            if(listener != null){
                mFatigueCBList.register(listener);
            }
        }

        public void unregisterFatigueListener(IFatigueListener listener){
            if(listener != null){
                mFatigueCBList.unregister(listener);
            }
        }

        public void registerBorrowListener(IBorrowListener listener){
            if(listener != null){
                mBorrowCBList.register(listener);
            }
        }

        public void unregisterBorrowListener(IBorrowListener listener){
            if(listener != null){
                mBorrowCBList.unregister(listener);
            }
        }

        public void registerLiveSharingListener(ILiveSharingListener listener){
            if(listener != null){
                mLiveSharingCBList.register(listener);
            }
        }

        public void unregisterLiveSharingListener(ILiveSharingListener listener){
            if(listener != null){
                mLiveSharingCBList.unregister(listener);
            }
        }

        public void registerHitchhikeListener(IHitchhikeListener listener){
            if(listener != null){
                mHitchhikeCBList.register(listener);
            }
        }

        public void unregisterHitchhikeListener(IHitchhikeListener listener){
            if(listener != null){
                mHitchhikeCBList.unregister(listener);
            }
        }

    };
}
