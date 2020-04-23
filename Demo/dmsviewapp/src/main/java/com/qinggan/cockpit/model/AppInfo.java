package com.qinggan.cockpit.model;

public class AppInfo {

    public static final String FileBasePath = "/sdcard/DMSDemo";

    public static final int UI_BASE = 0x100;
    public static final int UI_Home = UI_BASE + 1;
    public static final int UI_Fatigue = UI_BASE + 2;
    public static final int UI_Borrow = UI_BASE + 3;
    public static final int UI_LiveSharing = UI_BASE + 4;
    public static final int UI_Hitchhike = UI_BASE + 5;

    public static final String ID_DMSPadDemo = "com.pateo.auto.dmsdemo.pad";

    public static final int DisplayWidth = 1920;
    public static final int DisplayHeight = 720;

    public static final String PATH_FATIGUE = "/sdcard/DMSDemo/DMS_Fatigue.mp4";
    public static final String PATH_BORROW = "/sdcard/DMSDemo/DMS_Borrow.mp4";
    public static final String PATH_LIVE_SHARING = "/sdcard/DMSDemo/DMS_LivesSharing.mp4";
    public static final String PATH_HITCHHIKE = "/sdcard/DMSDemo/DMS_Hitchhike.mp4";

    public static final int MODE_UNDEF = 0x00;
    public static final int MODE_LOGIN = MODE_UNDEF + 1;
    public static final int MODE_FATIGUE = MODE_UNDEF + 2;
    public static final int MODE_BORROW = MODE_UNDEF + 3;
    public static final int MODE_LIVESHARING = MODE_UNDEF + 4;
    public static final int MODE_HITCHHIKE = MODE_UNDEF + 5;

    public static final int FATIGUE_LOW = 0x01;
    public static final int FATIGUE_MEDIUM = 0x02;
    public static final int FATIGUE_HIGH = 0x03;

}
