package com.theshopatvsp.levelandroidsdk.ble.model;

import  com.theshopatvsp.levelandroidsdk.model.LevelUser;

import java.io.File;

/**
 * Created by andrco on 6/17/16.
 */
public interface BleClientCallback {
    void connect(String frameId);
    void sendLedCode(int code);
    void setUser(LevelUser user);
    void getFirmwareVersion();
    void getBatteryLevel();
    void getBatteryState();
    void startBootloader(File bootloaderFile);
}
