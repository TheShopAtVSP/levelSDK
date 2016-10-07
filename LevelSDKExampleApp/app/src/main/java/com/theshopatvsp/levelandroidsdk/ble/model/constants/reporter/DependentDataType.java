package com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 10/2/15.
 */
public enum DependentDataType {
    CHAR(0), UINT8(1), INT8(2), UINT16(3), INT16(4), UINT32(5), INT32(6), INT24(7);

    DependentDataType(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return id;
    }

    public static DependentDataType getById(int id) {
        for (DependentDataType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }

        return null;
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();

        for (DependentDataType thing : values()) {
            names.add(thing.name());
        }

        return names;
    }
}
