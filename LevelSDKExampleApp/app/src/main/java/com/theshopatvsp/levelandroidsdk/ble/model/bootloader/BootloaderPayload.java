package com.theshopatvsp.levelandroidsdk.ble.model.bootloader;

import java.io.File;
import java.io.Serializable;

/**
 * Created by andrco on 6/13/16.
 */
public class BootloaderPayload implements Serializable {
    private String deviceMac;
    private File bootloaderFile;

    public BootloaderPayload(String deviceMac, File bootloaderFile) {
        this.deviceMac = deviceMac;
        this.bootloaderFile = bootloaderFile;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public File getBootloaderFile() {
        return bootloaderFile;
    }
}
