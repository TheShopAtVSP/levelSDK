package com.theshopatvsp.levelandroidsdk.ble.model;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.Frame;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientFrameDelegate {
    void onFrame(Frame frame);
}
