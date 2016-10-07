package com.theshopatvsp.levelandroidsdk.ble.model;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientStepDelegate {
    void onDeviceReady();
    void onStep(Step step);
}
