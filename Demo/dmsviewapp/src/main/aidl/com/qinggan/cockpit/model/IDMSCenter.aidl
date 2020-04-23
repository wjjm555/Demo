// IDMSCenter.aidl
package com.qinggan.cockpit.model;

// Declare any non-default types here with import statements

import com.qinggan.cockpit.model.ILoginListener;
import com.qinggan.cockpit.model.IClientListener;
import com.qinggan.cockpit.model.IFatigueListener;
import com.qinggan.cockpit.model.IBorrowListener;
import com.qinggan.cockpit.model.ILiveSharingListener;
import com.qinggan.cockpit.model.IHitchhikeListener;


interface IDMSCenter {

    // Client To Serve
    void loginInUser(String name, String iconPath, int age, int sex);
    void loginInUnknown(String iconPath, int age, int sex);
    void logoutInAuto();
    int getCurMode();
    void postFatigueEvent(int level);
    void postBorrowEvent(String imgPath);
    void postLiveSharingEvent(String imgPath);
    void postHitchhikeEvent(String imgPath);

    // Client Listener
    void registerClientListener(IClientListener listener);
    void unregisterClientListener(IClientListener listener);

    // Serve To Client
    boolean requestRegisterUser(String name, String pwd);
    boolean requestLogout(String name);
    boolean requestRemoveUser(String name);

    void startCheckLogin();
    void stopCheckLogin();

    void startCheckFatigue();
    void stopCheckFatigue();

    void startCheckBorrow();
    void stopCheckBorrow();

    void startCheckLiveSharing();
    void stopCheckLiveSharing();

    void startCheckHitchhike();
    void stopCheckHitchhike();

    // Serve Listener
    void registerLoginListener(ILoginListener listener);
    void unregisterLoginListener(ILoginListener listener);

    void registerFatigueListener(IFatigueListener listener);
    void unregisterFatigueListener(IFatigueListener listener);

    void registerBorrowListener(IBorrowListener listener);
    void unregisterBorrowListener(IBorrowListener listener);

    void registerLiveSharingListener(ILiveSharingListener listener);
    void unregisterLiveSharingListener(ILiveSharingListener listener);

    void registerHitchhikeListener(IHitchhikeListener listener);
    void unregisterHitchhikeListener(IHitchhikeListener listener);

}
