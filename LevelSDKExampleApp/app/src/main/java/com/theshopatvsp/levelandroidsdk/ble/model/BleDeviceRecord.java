package com.theshopatvsp.levelandroidsdk.ble.model;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;

/**
 * Created by andrco on 6/10/16.
 */
public class BleDeviceRecord implements Comparable {
    private BluetoothDevice device;
    private int rssi;

    public BleDeviceRecord(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public int getRssi() {
        return rssi;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int compareTo(Object another) {
        return Integer.compare(((BleDeviceRecord)another).getRssi(), rssi);
    }
}
