package com.theshopatvsp.levelandroidsdk.ble.model;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientConnectionDelegate {
    void onDeviceReady();
    void onBootloaderFinished();
}
