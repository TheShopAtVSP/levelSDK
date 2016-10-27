package com.theshopatvsp.levelandroidsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.theshopatvsp.levelandroidsdk.ble.helper.AlarmHelper;

/**
 * Created by andrco on 2/16/15.
 */
public class AlarmBootReceiver extends BroadcastReceiver {
    private static final String TAG = AlarmBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive called! " + intent.getAction());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            AlarmHelper.setAlarm(context);
        }
    }
}
