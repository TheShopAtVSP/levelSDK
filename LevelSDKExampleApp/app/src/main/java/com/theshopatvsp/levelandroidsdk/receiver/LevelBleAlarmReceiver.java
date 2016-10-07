package com.theshopatvsp.levelandroidsdk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.theshopatvsp.levelandroidsdk.ble.BleManager;

/**
 * Created by andrco on 6/15/16.
 */
public class LevelBleAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = LevelBleAlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceived called " + intent.getAction());

        Intent stepService = new Intent(context, BleManager.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startService(stepService);
    }
}
