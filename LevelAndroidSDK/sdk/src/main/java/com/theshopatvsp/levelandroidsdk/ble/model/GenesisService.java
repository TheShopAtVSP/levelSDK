package com.theshopatvsp.levelandroidsdk.ble.model;

import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by andrco on 10/10/14.
 */
public class GenesisService {
    private String name;
    private UUID guid;
    private BluetoothGattService service;

    public GenesisService(String name, UUID guid) {
        this.name = name;
        this.guid = guid;
    }

    public GenesisService(String name, UUID guid, BluetoothGattService service) {
        this.name = name;
        this.guid = guid;
        this.service = service;
    }

    public UUID getGuid() {
        return guid;
    }

    public String getName() {
        return name;
    }

    public BluetoothGattService getService() {
        return service;
    }

    public void setService(BluetoothGattService service) {
        this.service = service;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenesisService)) return false;

        GenesisService service1 = (GenesisService) o;

        if (!guid.equals(service1.guid)) return false;
        if (!name.equals(service1.name)) return false;
        if (service != null ? !service.equals(service1.service) : service1.service != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + guid.hashCode();
        result = 31 * result + (service != null ? service.hashCode() : 0);
        return result;
    }
}
