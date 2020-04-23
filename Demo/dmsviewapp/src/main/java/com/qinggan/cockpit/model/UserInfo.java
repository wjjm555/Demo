package com.qinggan.cockpit.model;

import android.graphics.Bitmap;

public class UserInfo {
    /** 用户名*/
    public String userName = null;

    /** 用户头像*/
    public Bitmap userIcon = null;

    /** 特征值，一般为照片识别的特征值*/
    public String token = null;

    /** 用户的年龄*/
    public int age = 0;

    /** 性别属性，0表示男性，1表示女性，其它表示未知 */
    public int sex = -1;
}
