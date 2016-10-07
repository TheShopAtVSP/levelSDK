package com.theshopatvsp.levelandroidsdk;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.theshopatvsp.levelandroidsdk.ble.BleManager;

/**
 * Created by andrco on 6/11/16.
 */
public class LevelApplication extends Application {
    private static final String TAG = LevelApplication.class.getSimpleName();

    public void onCreate() {
        Log.v(TAG, "starting service!!");
        super.onCreate();
        startService(new Intent(this, BleManager.class));
    }
}
