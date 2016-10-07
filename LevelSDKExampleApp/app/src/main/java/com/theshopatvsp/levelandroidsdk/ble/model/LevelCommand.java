package com.theshopatvsp.levelandroidsdk.ble.model;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.CharacteristicEnum;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.ReadWriteEnum;

import java.util.Arrays;

/**
 * Created by andrco on 6/11/16.
 */
public class LevelCommand {
    private ReadWriteEnum readOrWrite;
    private CharacteristicEnum characteristic;
    private byte data[];
    private int priority;

    public LevelCommand(ReadWriteEnum readWriteEnum, CharacteristicEnum characteristicEnum) {
        this.readOrWrite = readWriteEnum;
        this.characteristic = characteristicEnum;
        this.priority = 1;
    }

    public LevelCommand(ReadWriteEnum readWriteEnum, CharacteristicEnum characteristicEnum, byte data[]) {
        this(readWriteEnum, characteristicEnum);
        this.data = data;
    }

    public ReadWriteEnum getReadOrWrite() {
        return readOrWrite;
    }

    public CharacteristicEnum getCharacteristic() {
        return characteristic;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LevelCommand)) return false;

        LevelCommand that = (LevelCommand) o;

        if (readOrWrite != that.readOrWrite) return false;
        if (characteristic != that.characteristic) return false;

        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = readOrWrite != null ? readOrWrite.hashCode() : 0;
        result = 31 * result + (characteristic != null ? characteristic.hashCode() : 0);
        result = 31 * result + (data != null ? Arrays.hashCode(data) : 0);
        result = 31 * result + priority;
        return result;
    }
}
