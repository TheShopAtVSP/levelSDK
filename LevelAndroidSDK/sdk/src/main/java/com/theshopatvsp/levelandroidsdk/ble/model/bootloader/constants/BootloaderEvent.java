package com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants;

import  com.theshopatvsp.levelandroidsdk.ble.model.LevelCommand;
import  com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.BootloaderState;

/**
 * Created by andrco on 3/7/16.
 */
public interface BootloaderEvent {
    BootloaderState process();
    LevelCommand getCommand(byte bytes[]);
}
