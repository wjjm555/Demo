package com.qinggan.cockpit;

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
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qinggan.cockpit.activity.BaseActivity;
import com.qinggan.cockpit.activity.BorrowActivity;
import com.qinggan.cockpit.activity.FatigueActivity;
import com.qinggan.cockpit.activity.HitchhikeActivity;
import com.qinggan.cockpit.activity.LiveSharingActivity;
import com.qinggan.cockpit.model.DMSService;
import com.qinggan.cockpit.model.IDMSCenter;
import com.qinggan.cockpit.model.ILoginListener;
import com.qinggan.cockpit.model.UserInfo;
import com.qinggan.cockpit.utils.PermisionUtils;
import com.qinggan.cockpit.view.UserHeadView;
import com.qinggan.dms.library.model.DragItemInfo;
import com.qinggan.dms.library.views.DragLayout;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


public class MainActivity extends BaseActivity {

    private static long AutoCloseTime = 30 * 1000;

    private enum UserStatus {
        LOGINED,        // 已登录状态
        UNREGISTER,     // 已识别头像，但未注册登录
        UNCHECK         // 未识别头像状态
    }

    private EditText mCurEditText = null;

    private Vector<Button> mBtnKeyList = new Vector<>();

    /**
     * 是否为大写模式
     */
    private boolean mKeyIsUpper = true;

    private Handler mHandler;

    private UserInfo mUserInfo;

    private UserStatus mUserStatus;

    private IDMSCenter mDMSCenter;

    private Timer mAutoCloser = new Timer();

    private ILoginListener mLoginListener = new ILoginListener.Stub() {
        @Override
        public void onLoginInUser(final String name, final String iconPath, final int age, final int sex) throws RemoteException {
            Log.i("DMSTest-Serve", "--------->onLoginInUser");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginInUser(name, iconPath, age, sex);
                }
            });

        }

        @Override
        public void onLoginInUnknown(final String iconPath, final int age, final int sex) throws RemoteException {
            Log.i("DMSTest-Serve", "--------->onLoginInUnknown");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loginInUnknown(iconPath, age, sex);
                }
            });
        }

        @Override
        public void onLogoutInAuto() throws RemoteException {
            Log.i("DMSTest-Serve", "--------->onLogoutInAuto");
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    logoutInAuto();
//                }
//            });
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("DMSTest-Serve", "[Home - onServiceConnected]");
            mDMSCenter = IDMSCenter.Stub.asInterface(service);
            registerListener();
            startCheckLogin();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            unregisterListener();
            mDMSCenter = null;
            Log.i("DMSTest-Serve", "[Home - onServiceDisconnected]");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PermisionUtils.verifyStoragePermissions(this);

        initMenuUI();
        initSubmenuUI();
        initRegisterUI();

        mHandler = new Handler();
        changeStatus(UserStatus.UNCHECK);

        Intent intent = new Intent();
        intent.setClass(this, DMSService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        initFunctionUI();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mUserStatus == UserStatus.UNCHECK) {
            startCheckLogin();
        }

        testDragLayout();

//        // 如果注册界面没有显示，就开启人脸检测, 自动登出功能
//        ConstraintLayout layout = findViewById(R.id.layout_register);
//        if(layout.getVisibility() != View.VISIBLE){
//            startCheckLogin();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopCheckLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterListener();
        unbindService(mServiceConnection);
    }

    private void registerListener() {
        if (mDMSCenter != null) {
            try {
                mDMSCenter.registerLoginListener(mLoginListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterListener() {
        if (mDMSCenter != null) {
            try {
                mDMSCenter.unregisterLoginListener(mLoginListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 修改当前用户状态
     *
     * @param status 用户状态
     */
    private void changeStatus(UserStatus status) {
        mUserStatus = status;

        TextView textLogin = findViewById(R.id.text_login);
        TextView textLogout = findViewById(R.id.text_logout);
        TextView textDelete = findViewById(R.id.text_delete);

        if (mUserStatus == UserStatus.LOGINED) {
            textLogin.setEnabled(false);
            textLogout.setEnabled(true);
            textDelete.setEnabled(true);
        } else if (mUserStatus == UserStatus.UNREGISTER) {
            textLogin.setEnabled(true);
            textLogout.setEnabled(false);
            textDelete.setEnabled(true);
        } else if (mUserStatus == UserStatus.UNCHECK) {
            textLogin.setEnabled(false);
            textLogout.setEnabled(false);
            textDelete.setEnabled(false);
            mUserInfo = null;
        }
        setUIInfo();
    }

    /**
     * 开始实时监测登录
     */
    private void startCheckLogin() {
        if (mDMSCenter != null) {
            try {
                mDMSCenter.startCheckLogin();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void stopCheckLogin() {
        try {
            if (mDMSCenter != null) {
                mDMSCenter.stopCheckLogin();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFunctionUI() {
        DragLayout dragLayout = findViewById(R.id.dragView);
        dragLayout.setDragItemInfos(new DragItemInfo("MUSIC", R.mipmap.img_big_music_undef, R.mipmap.img_small_music),
                new DragItemInfo("NAVI", R.mipmap.img_big_navi_undef, R.mipmap.img_small_navi),
                new DragItemInfo("AIR", R.mipmap.img_big_air, R.mipmap.img_small_air),
                new DragItemInfo("NEWS", R.mipmap.img_big_news, R.mipmap.img_small_news),
                new DragItemInfo("WEATHER", R.mipmap.img_big_weather, R.mipmap.img_small_weather)
        );
    }

    private void setUIInfo() {
        String name = "Unknown";
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_def);
        int img1 = R.mipmap.img_big_music_undef;
        int img2 = R.mipmap.img_big_navi_undef;
        int imgBg = R.mipmap.bg_main;

        if (mUserInfo != null) {
            if (mUserInfo.userName != null) {
                name = mUserInfo.userName;
            }

            if (mUserInfo.userIcon != null) {
                icon = mUserInfo.userIcon;
            }

            if (mUserInfo.sex == 0) {
                if (mUserInfo.age >= 40) {
                    img1 = R.mipmap.img_big_music_male_greater40;
                } else {
                    img1 = R.mipmap.img_big_music_male_less40;
                }
                img2 = R.mipmap.img_big_navi_male;
                imgBg = R.mipmap.bg_male;
            } else if (mUserInfo.sex == 1) {
                img1 = R.mipmap.img_big_music_female;
                img2 = R.mipmap.img_big_navi_female;
                imgBg = R.mipmap.bg_female;
            } else {
                img2 = R.mipmap.img_big_navi_login;
            }
        }

        UserHeadView headView = findViewById(R.id.user_icon);
        headView.setIcon(icon);

        TextView textView = findViewById(R.id.user_name);
        textView.setText(name);

        DragLayout dragLayout = findViewById(R.id.dragView);
        dragLayout.updateDragItem("MUSIC", DragItemInfo.Size.LARGE, img1);
        dragLayout.updateDragItem("NAVI", DragItemInfo.Size.LARGE, img2);

//        ImageView imgFunction1 = findViewById(R.id.function_main1);
//        imgFunction1.setImageBitmap(BitmapFactory.decodeResource(getResources(), img1));
//
//        ImageView imgFunction2 = findViewById(R.id.function_main2);
//        imgFunction2.setImageBitmap(BitmapFactory.decodeResource(getResources(), img2));

        FrameLayout bgLayout = findViewById(R.id.root);
        bgLayout.setBackgroundResource(imgBg);
    }

    private void testDragLayout() {
        final int[] imgs1 = {R.mipmap.img_big_music_undef, R.mipmap.img_big_music_male_greater40, R.mipmap.img_big_music_male_less40, R.mipmap.img_big_music_female};
        final int[] imgs2 = {R.mipmap.img_big_navi_undef, R.mipmap.img_big_navi_login, R.mipmap.img_big_navi_male, R.mipmap.img_big_navi_female};

        new Thread() {
            @Override
            public void run() {
                int i = 0;
                while (i < Integer.MAX_VALUE) {
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int j = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DragLayout dragLayout = findViewById(R.id.dragView);
                            dragLayout.cancelDrag();
//                            dragLayout.updateDragItem("MUSIC", DragItemInfo.Size.LARGE, imgs1[j % imgs1.length]);
//                            dragLayout.updateDragItem("NAVI", DragItemInfo.Size.LARGE, imgs2[j % imgs2.length]);
                        }
                    });
                    ++i;
                }
            }
        }.start();
    }

    /**
     * 初始化子菜单
     */
    private void initSubmenuUI() {
        LinearLayout layout = findViewById(R.id.layout_sub_menu);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSubmenuUI();
            }
        });

        TextView textView;
        textView = findViewById(R.id.text_login);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterUI();
                hideSubmenuUI();
            }
        });

        textView = findViewById(R.id.text_logout);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLogout();

                hideSubmenuUI();
                mUserInfo = null;
                changeStatus(UserStatus.UNCHECK);
                startCheckLogin();
            }
        });

        textView = findViewById(R.id.text_delete);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRemoveUser();

                hideSubmenuUI();
                mUserInfo = null;
                changeStatus(UserStatus.UNCHECK);
                startCheckLogin();
            }
        });
    }

    /**
     * 显示子菜单
     */
    private void showSubmenuUI() {
        LinearLayout layout = findViewById(R.id.layout_sub_menu);
        layout.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏子菜单
     */
    private void hideSubmenuUI() {
        LinearLayout layout = findViewById(R.id.layout_sub_menu);
        layout.setVisibility(View.GONE);
    }


    /**
     * 初始化注册界面
     */
    private void initRegisterUI() {
        ConstraintLayout layout = findViewById(R.id.layout_register);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 取消点击其他区域关闭注册界面的功能
                //hideRegisterUI();

                // 关闭注册窗口，开启人脸检测
                //startCheckLogin();
            }
        });

        EditText editUser = findViewById(R.id.edit_user);
        disableShowInput(editUser);
        editUser.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mCurEditText = (EditText) view;
                }
            }
        });

        EditText editPwd = findViewById(R.id.edit_pwd);
        disableShowInput(editPwd);
        editPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mCurEditText = (EditText) view;
                }
            }
        });

        ImageButton btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editUser = findViewById(R.id.edit_user);
                editUser.setText("");
            }
        });

        Button btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText editUser = findViewById(R.id.edit_user);
                EditText editPwd = findViewById(R.id.edit_pwd);

                String name = editUser.getText().toString();
                String pwd = editPwd.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    showMsg("Please enter your name");
                } else if (TextUtils.isEmpty(pwd)) {
                    showMsg("Please enter account password");
                } else {
                    if (mUserInfo != null) {
                        mUserInfo.userName = name;
                    }

                    if (requestRegisterUser(name, pwd)) {
                        showMsg("Register user success");
                        changeStatus(UserStatus.LOGINED);
                        hideRegisterUI();

                        // 关闭注册窗口，开启人脸检测, 自动登出功能
                        //startCheckLogin();
                    } else {
                        showMsg("Register user fail");
                    }
                }
            }
        });

        Button cancelRegister = findViewById(R.id.btn_cancel);
        cancelRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideRegisterUI();

                // 关闭注册窗口，开启人脸检测， 自动登出功能
                // startCheckLogin();
            }
        });

        LinearLayout[] keyLayout = new LinearLayout[3];
        keyLayout[0] = findViewById(R.id.layout_key_line1);
        keyLayout[1] = findViewById(R.id.layout_key_line2);
        keyLayout[2] = findViewById(R.id.layout_key_line3);

        String[] keyChar = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};

        View spView;
        Button btn;
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(71, 72);
        LinearLayout.LayoutParams spParams = new LinearLayout.LayoutParams(15, 72);
        for (int i = 0; i < keyLayout.length; ++i) {
            for (int j = 0; j < keyChar[i].length(); ++j) {
                btn = new Button(this);
                btn.setText("" + keyChar[i].charAt(j));
                btn.setTextColor(0xffffffff);
                btn.setTextSize(16);
                btn.setBackgroundResource(R.drawable.btn_key);
                btn.setOnClickListener(mKeyOnClickListener);
                btn.setAllCaps(false);
                keyLayout[i].addView(btn, btnParams);
                mBtnKeyList.addElement(btn);

                if (j < keyChar[i].length() - 1) {
                    spView = new View(this);
                    keyLayout[i].addView(spView, spParams);
                }
            }
        }

        // 添加Shift按钮
        btn = new Button(this);
        btn.setText("SHIFT");
        btn.setTextColor(0xffffffff);
        btn.setTextSize(12);
        btn.setBackgroundResource(R.drawable.btn_key);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyText;

                mKeyIsUpper = !mKeyIsUpper;
                for (Button btn : mBtnKeyList) {
                    keyText = btn.getText().toString();
                    if (mKeyIsUpper) {
                        keyText = keyText.toUpperCase();
                    } else {
                        keyText = keyText.toLowerCase();
                    }
                    btn.setText(keyText);
                }

                // 自动关闭注册界面
                // resetCloser();
            }
        });

        keyLayout[2].addView(btn, 0, btnParams);
        spView = new View(this);
        keyLayout[2].addView(spView, 1, spParams);

        // 添加Del按钮
        spView = new View(this);
        keyLayout[2].addView(spView, spParams);

        ImageButton imgBtn = new ImageButton(this);
        imgBtn.setImageResource(R.mipmap.btn_del);
        imgBtn.setBackgroundResource(R.drawable.btn_key);
        imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurEditText != null) {
                    String text = mCurEditText.getText().toString();
                    if (text.length() > 0) {
                        mCurEditText.setText(text.substring(0, text.length() - 1));
                    }
                }

                // 自动关闭注册界面
                //resetCloser();
            }
        });
        keyLayout[2].addView(imgBtn, btnParams);

        // 添加空格按钮
//        spView = new View(this);
//        keyLayout[2].addView(spView, spParams);
//
//        btn = new Button(this);
//        btn.setText(" ");
//        btn.setTextColor(0xffffffff);
//        btn.setTextSize(16);
//        btn.setBackgroundResource(R.drawable.btn_key);
//        btn.setOnClickListener(mKeyOnClickListener);
//        keyLayout[2].addView(btn, btnParams);

    }

    /**
     * 字母按钮监听事件
     */
    private View.OnClickListener mKeyOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mCurEditText != null) {
                Button btn = (Button) view;
                String text = mCurEditText.getText().toString();
                if (text.length() < 50) {
                    mCurEditText.setText(text + btn.getText());
                }
            }

            // 自动关闭注册界面
            //resetCloser();
        }
    };

    /**
     * 显示注册界面
     */
    private void showRegisterUI() {
        EditText editUser = findViewById(R.id.edit_user);
        editUser.setText("");

        EditText editPwd = findViewById(R.id.edit_pwd);
        editPwd.setText("");

        ConstraintLayout layout = findViewById(R.id.layout_register);
        layout.setVisibility(View.VISIBLE);

        // 自动关闭注册界面
        //resetCloser();
    }

    /**
     * 隐藏注册界面
     */
    private void hideRegisterUI() {
        ConstraintLayout layout = findViewById(R.id.layout_register);
        layout.setVisibility(View.GONE);
    }

    /**
     * 初始化菜单界面
     */
    private void initMenuUI() {
        LinearLayout layout;

        layout = findViewById(R.id.btn_fatigue);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, FatigueActivity.class);
                overridePendingTransition(0, 0);
                startActivityForResult(intent, 0);
            }
        });

        layout = findViewById(R.id.btn_borrow);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BorrowActivity.class);
                overridePendingTransition(0, 0);
                startActivityForResult(intent, 0);
            }
        });

        layout = findViewById(R.id.btn_live_sharing);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, LiveSharingActivity.class);
                overridePendingTransition(0, 0);
                startActivityForResult(intent, 0);
            }
        });

        layout = findViewById(R.id.btn_hitchhike);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, HitchhikeActivity.class);
                overridePendingTransition(0, 0);
                startActivityForResult(intent, 0);
            }
        });

        TextView textView = findViewById(R.id.user_mgr);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubmenuUI();
            }
        });
    }

    public void disableShowInput(final EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {//TODO: handle exception
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {//TODO: handle exception
            }

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable e) {
                    editText.setSelection(editText.length(), editText.length());
                }
            });
        }
    }

    private void loginInUser(String name, String path, int age, int sex) {
        mUserInfo = new UserInfo();
        mUserInfo.userName = name;
        mUserInfo.userIcon = BitmapFactory.decodeFile(path);
        mUserInfo.age = age;
        mUserInfo.sex = sex;

        if (mUserInfo.userIcon == null) {
            mUserInfo.userIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_def);
        }

        // 添加自动登出，还要保持继续检测
        stopCheckLogin();

        changeStatus(UserStatus.LOGINED);
        showMsg("Login Success");

        Log.i("[DMSTest-Serve]", "[loginInUser]:" + name + ";" + path + ";" + age + ";" + sex);
    }

    private void loginInUnknown(String path, int age, int sex) {
        mUserInfo = new UserInfo();
        mUserInfo.userIcon = BitmapFactory.decodeFile(path);
        mUserInfo.age = age;
        mUserInfo.sex = sex;

        if (mUserInfo.userIcon == null) {
            mUserInfo.userIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_def);
        }

        stopCheckLogin();

        changeStatus(UserStatus.UNREGISTER);
        hideSubmenuUI();
        showRegisterUI();

        Log.i("[DMSTest-Serve]", "[loginInUnknown]:" + path + ";" + age + ";" + sex);
    }

    private void logoutInAuto() {
        mUserInfo = null;
        changeStatus(UserStatus.UNCHECK);
        hideSubmenuUI();
        hideRegisterUI();
    }

    private boolean requestRegisterUser(String name, String pwd) {
        boolean isSuccess = false;
        try {
            if (mDMSCenter != null) {
                isSuccess = mDMSCenter.requestRegisterUser(name, pwd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    private void requestLogout() {
        try {
            if (mDMSCenter != null && mUserInfo != null) {
                mDMSCenter.requestLogout(mUserInfo.userName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean requestRemoveUser() {
        boolean isSuccess = false;
        try {
            if (mDMSCenter != null && mUserInfo != null) {
                isSuccess = mDMSCenter.requestRemoveUser(mUserInfo.userName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isSuccess;
    }

    private void resetCloser() {
        mAutoCloser.cancel();
        mAutoCloser.schedule(mAutoCloseTask, AutoCloseTime);
    }

    private TimerTask mAutoCloseTask = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideRegisterUI();

                    // 关闭注册窗口，开启人脸检测
                    startCheckLogin();
                }
            });
        }
    };

}
