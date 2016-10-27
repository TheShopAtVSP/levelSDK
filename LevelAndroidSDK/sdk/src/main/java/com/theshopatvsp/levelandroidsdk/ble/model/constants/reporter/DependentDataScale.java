package com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 10/2/15.
 */
public enum DependentDataScale {
    ONE_TO_ONE_BIT(0), TEN_TO_ONE_BIT(1), ONE_HUNDRED_TO_ONE_BIT(2), PLUS_MINUS_1G_TO_DATA_SIZE(3),
    PLUS_MINUS_2G_TO_DATA_SIZE(4), PLUS_MINUS_4G_TO_DATA_SIZE(5), PLUS_MINUS_8G_TO_DATA_SIZE(6),
    PLUS_MINUS_16G_TO_DATA_SIZE(7), PLUS_MINUS_500_DPS_TO_DATA_SIZE(8), PLUS_MINUS_1000_DPS_TO_DATA_SIZE(9),
    PLUS_MINUS_2000_DPS_TO_DATA_SIZE(10), PLUS_MINUS_4_GAUSS_TO_DATA_SIZE(11), PLUS_MINUS_8_GAUSS_TO_DATA_SIZE(12),
    PLUS_MINUS_12_GAUSS_TO_DATA_SIZE(13), PLUS_MINUS_16_GAUSS_TO_DATA_SIZE(14), PLUS_MINUS_48_GAUSS_TO_DATA_SIZE(15);

    DependentDataScale(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return id;
    }

    public static DependentDataScale getById(int id) {
        for (DependentDataScale scale : values()) {
            if (scale.getId() == id) {
                return scale;
            }
        }

        return null;
    }

    public static List<String> names() {
        List<String> names = new ArrayList<>();

        for (DependentDataScale thing : values()) {
            names.add(thing.name());
        }

        return names;
    }
}
