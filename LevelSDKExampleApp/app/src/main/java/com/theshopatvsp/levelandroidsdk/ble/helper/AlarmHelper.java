package com.theshopatvsp.levelandroidsdk.ble.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import  com.theshopatvsp.levelandroidsdk.receiver.AlarmBootReceiver;

/**
 * Created by andrco on 2/16/15.
 */
public class AlarmHelper {
    private static final String TAG = AlarmHelper.class.getSimpleName();
    private static final int FIFTEEN_MIN = 15 * 60 * 1000;
    private static final int FIVE_MIN = 5 * 60 * 1000;
    private static final int THREE_MIN = 3 * 60 * 1000;
    private static final int ONE_MIN = 1 * 60 * 1000;
    private static final String BLE_CONNECTION_ALARM = "levelBleConnectionAlarm.START_ALARM";

    public static void enableRebootAlarm(Context context) {
        configureRebootAlarm(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
    }

    public static void disableRebootAlarm(Context context) {
        configureRebootAlarm(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
    }

    private static void configureRebootAlarm(Context context, int componentEnabledState) {
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                componentEnabledState,
                PackageManager.DONT_KILL_APP);
    }

    public static void setAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        int alarmInterval = FIFTEEN_MIN;
        Intent intent = new Intent(BLE_CONNECTION_ALARM);
        //intent.setAction("stepLogAlarm.START_ALARM");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Log.v(TAG, "setting alarm for " + System.currentTimeMillis() + " = " + alarmInterval);

        boolean alarmUp = (PendingIntent.getBroadcast(context, 0,
                new Intent(BLE_CONNECTION_ALARM),
                PendingIntent.FLAG_NO_CREATE) != null);


        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + alarmInterval, alarmInterval, alarmIntent);


        //context.startService(new Intent(context, StepLogService.class));

        enableRebootAlarm(context);
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(BLE_CONNECTION_ALARM);
        //intent.setAction("stepLogAlarm.START_ALARM");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        alarmManager.cancel(alarmIntent);
    }
}
