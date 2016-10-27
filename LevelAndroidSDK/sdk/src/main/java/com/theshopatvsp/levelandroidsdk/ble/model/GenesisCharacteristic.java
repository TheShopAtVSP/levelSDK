package com.theshopatvsp.levelandroidsdk.ble.model;

import android.bluetooth.BluetoothGattCharacteristic;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.CharacteristicEnum;

import java.util.UUID;

/**
 * Created by andrco on 10/10/14.
 */
public class GenesisCharacteristic {
    private CharacteristicEnum characteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    public GenesisCharacteristic(String name, UUID guid) {
        this.characteristic = CharacteristicEnum.getByUuid(guid);
    }

    public GenesisCharacteristic(CharacteristicEnum characteristic) {
        this.characteristic = characteristic;
    }

    public BluetoothGattCharacteristic getReadCharacteristic() {
        return readCharacteristic;
    }

    public void setReadCharacteristic(BluetoothGattCharacteristic readCharacteristic) {
        this.readCharacteristic = readCharacteristic;
    }

    public BluetoothGattCharacteristic getWriteCharacteristic() {
        return writeCharacteristic;
    }

    public void setWriteCharacteristic(BluetoothGattCharacteristic writeCharacteristic) {
        this.writeCharacteristic = writeCharacteristic;
    }

    public BluetoothGattCharacteristic getNotifyCharacteristic() {
        return notifyCharacteristic;
    }

    public void setNotifyCharacteristic(BluetoothGattCharacteristic notifyCharacteristic) {
        this.notifyCharacteristic = notifyCharacteristic;
    }

    public String getName() {
        return characteristic.getName();
    }

    public UUID getGuid() {
        return characteristic.getUuid();
    }

    public CharacteristicEnum getCharacteristic() {
        return characteristic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GenesisCharacteristic)) return false;

        GenesisCharacteristic that = (GenesisCharacteristic) o;

        if (characteristic != that.characteristic) return false;
        if (notifyCharacteristic != null ? !notifyCharacteristic.equals(that.notifyCharacteristic) : that.notifyCharacteristic != null)
            return false;
        if (readCharacteristic != null ? !readCharacteristic.equals(that.readCharacteristic) : that.readCharacteristic != null)
            return false;
        if (writeCharacteristic != null ? !writeCharacteristic.equals(that.writeCharacteristic) : that.writeCharacteristic != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = characteristic.hashCode();
        result = 31 * result + (readCharacteristic != null ? readCharacteristic.hashCode() : 0);
        result = 31 * result + (writeCharacteristic != null ? writeCharacteristic.hashCode() : 0);
        result = 31 * result + (notifyCharacteristic != null ? notifyCharacteristic.hashCode() : 0);
        return result;
    }
}
