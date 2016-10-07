package com.theshopatvsp.levelandroidsdk.ble.model;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientLinkDelegate {
    void onInputLedCode();
    void onLedCodeAccepted();

    void onDeviceReady();

    void onLedCodeFailed();
    void onLedCodeNotNeeded();
}
