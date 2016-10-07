package com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 10/2/15.
 */
public enum IndependentVariableDescription {
    UNITLESS(0), SECONDS(1), MINUTES(2), SAMPLING_HZ(3), ON_CHANGE(4);

    IndependentVariableDescription(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return id;
    }

    public static IndependentVariableDescription getById(int id) {
        for (IndependentVariableDescription desc : values()) {
            if (desc.getId() == id) {
                return desc;
            }
        }

        return null;
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();

        for (IndependentVariableDescription thing : values()) {
            names.add(thing.name());
        }

        return names;
    }
}
