package com.startek.biota.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.startek.biota.app.global.Global;
import com.startek.biota.app.utils.StrUtils;

public class BootCompletedReceiver extends BroadcastReceiver {

    private final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(StrUtils.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED))
        {
            Global.registerEmailAlarm();
        }
    }
}
