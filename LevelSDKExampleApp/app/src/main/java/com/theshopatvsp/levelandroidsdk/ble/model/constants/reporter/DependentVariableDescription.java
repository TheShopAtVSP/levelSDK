package com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 10/2/15.
 */
public enum DependentVariableDescription {
    UNITLESS(0), ASCII(1), STEP_PER_TIME(2), ACCELEROMETER_RAW(3), GYROMETER_RAW(4), MAGNETOMETER_RAW(5),
    ACCELEROMETER_FILT(6), GYROMETER_FILT(7), MAGNETOMETER_FILT(8), ACCEL_VARIANCE(9), GYRO_VARIANCE(10),
    MAGNET_VARIANCE(11), BATTERY_PERCENT_REMAIN(12), CYCLE_PER_TIME(13), TILT_ANGLE(14), BOARD_TEMPERATURE(15), EXPERIMENTAL(16);

    DependentVariableDescription(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return id;
    }

    public static DependentVariableDescription getById(int id) {
        for (DependentVariableDescription desc : values()) {
            if (desc.getId() == id) {
                return desc;
            }
        }

        return null;
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();

        for (DependentVariableDescription thing : values()) {
            names.add(thing.name());
        }

        return names;
    }
}
