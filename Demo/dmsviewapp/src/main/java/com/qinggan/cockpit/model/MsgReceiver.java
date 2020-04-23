package com.qinggan.cockpit.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MsgReceiver extends BroadcastReceiver {

    private static String cockpitAction = "com.qinggan.cockpit.msg";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(cockpitAction)){
            Log.i("kexin", "onReceive.............");
            MsgManager msgMgr = MsgManager.getInstance();
            msgMgr.onReceive(intent);
        }
    }
}
