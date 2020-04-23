// ILoginListener.aidl
package com.qinggan.cockpit.model;

// Declare any non-default types here with import statements

interface ILoginListener {

    void onLoginInUser(String name, String iconPath, int age, int sex);
    void onLoginInUnknown(String iconPath, int age, int sex);
    void onLogoutInAuto();
}
