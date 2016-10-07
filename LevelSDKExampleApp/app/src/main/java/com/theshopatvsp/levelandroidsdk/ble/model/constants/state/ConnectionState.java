package com.theshopatvsp.levelandroidsdk.ble.model.constants.state;

/**
 * Created by andrco on 6/11/16.
 */
public enum ConnectionState {
    Init, Scan, DeviceFound, NoDevicesFound, ConnectToDevice, Bond, DiscoverServices,
        GetCharacteristics, SendKey, Connected, Disconnected;
}
