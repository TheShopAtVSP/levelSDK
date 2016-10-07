package com.theshopatvsp.levelandroidsdk.ble.model.constants.state;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.DeviceCommand;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.state.DeviceInteractionState;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 6/12/16.
 */
public interface LevelDeviceEvent {
    DeviceInteractionState success();
    DeviceInteractionState uhoh();
    DeviceCommand getCommand();
    DataPacket getPacket(int reporters);
}
