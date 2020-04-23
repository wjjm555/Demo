package com.qinggan.cockpit.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileInputStream;
import android.util.Log;

public class MsgManager {

    private static String Key_Action = "key_action";
    private static String Key_Name = "key_name";
    private static String Key_Icon_Path = "key_icon_path";
    private static String Key_Age = "key_age";
    /* sex 0: male / 1: female */
    private static String Key_Sex = "key_sex";
    private static String Key_Pwd = "key_pwd";
    private static String Key_UI_ID = "key_ui_id";

    private static int Action_UI_Change = 0x00;
    private static int Action_Login = 0x01;
    private static int Action_Login_Unknown = 0x02;
    private static int Action_Register = 0x03;
    private static int Action_Logout = 0x04;
    private static int Action_RemoveUser = 0x05;

    private static MsgManager sMsgManager = new MsgManager();

    public static MsgManager getInstance(){
        return sMsgManager;
    }

    private LoginListener mLoginListener = null;

    public void setLoginListener(LoginListener listener){
        mLoginListener = listener;
    }

    public void notifiyUIChange(Context context, int id){
        Intent intent = createIntent();
        intent.putExtra(Key_Action, Action_UI_Change);
        intent.putExtra(Key_UI_ID, id);
        context.sendBroadcast(intent);
    }

    public void registerUser(Context context, String name, String pwd){
        Intent intent = createIntent();
        intent.putExtra(Key_Action, Action_Register);
        intent.putExtra(Key_Name, name);
        intent.putExtra(Key_Pwd, pwd);
        context.sendBroadcast(intent);
    }

    public void logout(Context context){
        Intent intent = createIntent();
        intent.putExtra(Key_Action, Action_Logout);
        context.sendBroadcast(intent);
    }

    public void removeUser(Context context){
        Intent intent = createIntent();
        intent.putExtra(Key_Action, Action_RemoveUser);
        context.sendBroadcast(intent);
    }

    /**
     * 接受处理小屏数据广播
     * @param intent
     */
    void onReceive(Intent intent){
        if(intent != null){
            int action = intent.getIntExtra(Key_Action, -1);
            if(action != -1){
                if(action == Action_Login){
                    String name = intent.getStringExtra(Key_Name);
                    String path = intent.getStringExtra(Key_Icon_Path);
                    int age = intent.getIntExtra(Key_Age, -1);
                    int sex = intent.getIntExtra(Key_Sex, -1);

                    Bitmap icon = null;
                    if(path != null){
                        icon = loadBitmap(path);
                    }

                    if(mLoginListener != null){
                        mLoginListener.onUserLogin(name, icon, age, sex);
                    }
                }
                else if(action == Action_Login_Unknown){
                    String path = intent.getStringExtra(Key_Icon_Path);
                    int age = intent.getIntExtra(Key_Age, -1);
                    int sex = intent.getIntExtra(Key_Sex, -1);

                    Bitmap icon = null;
                    if(path != null){
                        icon = loadBitmap(path);
                    }

                    if(mLoginListener != null){
                        mLoginListener.onUnknowLogin(icon, age, sex);
                    }
                }
            }
        }
    }

//    /**
//     * 接受处理小屏数据广播
//     * @param intent
//     */
//    public void onReceive(Intent intent){
//        if(intent != null){
//            int action = intent.getIntExtra(Key_Action, -1);
//            if(action != -1){
//                if(action == ActionLogin){
//                    /* 此处接收数据后需要更新UI */
//                    String name = intent.getStringExtra(Key_Name);
//                    if(name.equalsIgnoreCase("unkown")){
//                        /* do not register */
//                    }else{
//                        /* has been registered */
//                    }
//                    int age = intent.getIntExtra(Key_Age, -1);
//                    int sex = intent.getIntExtra(Key_Sex, -1);
//
//                }else if(action == ActionFatigue){
//
//                }else if(action == ActionBorrow){
//
//                }else if(action == ActionLift){
//
//                }else if(action == ActionCap){
//
//                }
//            }
//        }
//    }

    private Intent createIntent(){
        Intent intent = new Intent();
        intent.setAction(AppInfo.ID_DMSPadDemo);
        return intent;
    }

    public interface LoginListener{
        void onUserLogin(String name, Bitmap icon, int age, int sex);
        void onUnknowLogin(Bitmap icon, int age, int sex);
    }

    private Bitmap loadBitmap(String path){
        Bitmap bitmap = null;
        if(path != null) {
            bitmap = BitmapFactory.decodeFile(path);
        }
        return bitmap;
    }
}
