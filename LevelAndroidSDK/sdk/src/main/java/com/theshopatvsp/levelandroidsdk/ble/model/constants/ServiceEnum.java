package com.theshopatvsp.levelandroidsdk.ble.model.constants;

import  com.theshopatvsp.levelandroidsdk.ble.BootloaderService;

import java.util.UUID;

/**
 * Created by andrco on 2/6/15.
 */
public enum ServiceEnum {
    BATTERY("BATTERY", "0000180f-0000-1000-8000-00805f9b34fb"),
    STEPS("STEPS", "00001899-0000-1000-8000-00805f9b34fb"),
    DEVICE_INFO("DEVICE_INFO", "0000180a-0000-1000-8000-00805f9b34fb"),
    UART("UART", "6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
    DFU("DFU",BootloaderService.DFU_SERVICE_UUID);

    ServiceEnum(String name, String uuid) {
        this.name = name;
        this.uuid = UUID.fromString(uuid);
    }

    ServiceEnum(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    private String name;
    private UUID uuid;

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public static ServiceEnum getByUuid(UUID uuid) {
        for (ServiceEnum charac : values()) {
            if (charac.getUuid().equals(uuid)) {
                return charac;
            }
        }

        return null;
    }
}
