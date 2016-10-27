package com.theshopatvsp.levelandroidsdk.ble.model.constants;

import  com.theshopatvsp.levelandroidsdk.ble.BootloaderService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by andrco on 11/4/14.
 */
public enum CharacteristicEnum {
    BATTERY_LEVEL("BATTERY_LEVEL", "00002a19-0000-1000-8000-00805f9b34fb", "0000180f-0000-1000-8000-00805f9b34fb"),
    BATTERY_STATE("BATTERY_STATE", "00002a20-0000-1000-8000-00805f9b34fb", "0000180f-0000-1000-8000-00805f9b34fb"),
    STEP_LOG("STEP_LOG", "00002b30-0000-1000-8000-00805f9b34fb", "00001899-0000-1000-8000-00805f9b34fb"),
    STEP_TOTAL("STEP_TOTAL", "00002b31-0000-1000-8000-00805f9b34fb", "00001899-0000-1000-8000-00805f9b34fb"),
    MANUFACTURER("MANUFACTURER", "00002a29-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    MODEL_NUMBER("MODEL_NUMBER", "00002a24-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    HARDWARE_VERSION("HARDWARE_VERSION", "00002a27-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    SOFTWARE_VERSION("SOFTWARE_VERSION", "00002a28-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    FIRWARE_VERSION("FIRMWARE_VERSION", "00002a26-0000-1000-8000-00805f9b34fb", "0000180a-0000-1000-8000-00805f9b34fb"),
    UART_RX("UART_RX", "6e400003-b5a3-f393-e0a9-e50e24dcca9e", "6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
    UART_TX("UART_TX", "6e400002-b5a3-f393-e0a9-e50e24dcca9e", "6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
    DFU_CONTROL_POINT("DFU_CONTROL_POINT", BootloaderService.DFU_CONTROL_POINT_CHARACTERISTIC_UUID, BootloaderService.DFU_SERVICE_UUID),
    DFU_PACKET("DFU_PACKET", BootloaderService.DFU_PACKET_CHARACTERISTIC_UUID, BootloaderService.DFU_SERVICE_UUID),
    DFU_VERSION("DFU_VERSION", BootloaderService.DFU_VERSION_CHARACTERISTIC_UUID, BootloaderService.DFU_SERVICE_UUID);

    private CharacteristicEnum(String name, String uuid, String parentService) {
        this.name = name;
        this.uuid = UUID.fromString(uuid);
        this.parentService = UUID.fromString(parentService);
    }

    private CharacteristicEnum(String name, UUID uuid, UUID parentService) {
        this.name = name;
        this.uuid = uuid;
        this.parentService = parentService;
    }

    private String name;
    private UUID uuid;
    private UUID parentService;

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getParentService() {
        return parentService;
    }

    public static CharacteristicEnum getByUuid(UUID uuid) {
        for (CharacteristicEnum charac : values()) {
            if (charac.getUuid().equals(uuid)) {
                return charac;
            }
        }

        return null;
    }

    public static List<CharacteristicEnum> getByParentUUID(UUID parentService) {
        List<CharacteristicEnum> chars = new ArrayList<>();

        for (CharacteristicEnum c : values()) {
            if( c.getParentService().equals(parentService)) {
                chars.add(c);
            }
        }

        return chars;
    }

    public static CharacteristicEnum getByName(String name) {
        for (CharacteristicEnum charac : values()) {
            if (charac.getName().equals(name)) {
                return charac;
            }
        }

        return null;
    }
}