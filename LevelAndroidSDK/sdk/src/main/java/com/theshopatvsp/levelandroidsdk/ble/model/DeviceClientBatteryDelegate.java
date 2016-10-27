package com.theshopatvsp.levelandroidsdk.ble.model;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientBatteryDelegate {
    void onDeviceReady();
    void onBatteryLevel(int level);
    void onBatteryState(BatteryState state);
}
