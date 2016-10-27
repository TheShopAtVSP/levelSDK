package com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 10/2/15.
 */
public enum  DataFields {
    INCLUDE_X_AXIS(0b00000001), INCLUDE_Y_AXIS(0b00000010), INCLUDE_Z_AXIS(0b00000100),
    INCLUDE_MAGNITUDE(0b00001000), NONE(0);

    DataFields(int bit) {
        this.bit = bit;
    }

    private int bit;

    public int getBit() {
        return bit;
    }

    public static DataFields getById(byte id) {
        for (DataFields data : values()) {
            if (data.bit == id) {
                return data;
            }
        }

        return null;
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();

        for (DataFields thing : values()) {
            names.add(thing.name());
        }

        return names;
    }
}
