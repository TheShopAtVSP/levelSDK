package com.theshopatvsp.levelandroidsdk.ble.model;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientInternalDataDelegate {
    void onDeviceReady();
    void onBatteryReport(BatteryReport batteryReport);
    void onMotionData(AccelFilt accelFilt);
}
