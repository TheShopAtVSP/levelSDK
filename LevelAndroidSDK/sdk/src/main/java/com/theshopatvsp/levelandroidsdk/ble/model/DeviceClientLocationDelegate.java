package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.model.LastLocation;

/**
 * Created by nandpa on 7/26/16.
 */
public interface DeviceClientLocationDelegate {
    void onLastUserLocation(LastLocation location);
}
