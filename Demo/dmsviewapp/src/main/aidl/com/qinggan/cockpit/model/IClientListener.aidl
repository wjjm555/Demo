// IClientListener.aidl
package com.qinggan.cockpit.model;

// Declare any non-default types here with import statements

interface IClientListener {

    // Login Scene
    boolean onRegisterUser(String name, String pwd);
    boolean onLogout(String name);
    boolean onRemoveUser(String name);

    void onStartCheckLogin();
    void onStopCheckLogin();

    void onStartCheckFatigue();
    void onStopCheckFatigue();

    void onStartCheckBorrow();
    void onStopCheckBorrow();

    void onStartCheckLiveSharing();
    void onStopCheckLiveSharing();

    void onStartCheckHitchhike();
    void onStopCheckHitchhike();

}
