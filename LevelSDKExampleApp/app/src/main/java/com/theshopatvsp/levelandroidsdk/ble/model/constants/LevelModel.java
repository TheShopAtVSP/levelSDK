package com.theshopatvsp.levelandroidsdk.ble.model.constants;

/**
 * Created by andrco on 7/25/16.
 */
public enum LevelModel {
    NotSet(0, 0, "Not Set"), NIKOLA(1, 100, "Nikola"), MINSKY(2, 200, "Minsky"), HEDY(3, 300, "Hedy");

    LevelModel(int position, int modelNumber, String name) {
        this.position = position;
        this.modelNumber = modelNumber;
        this.modelName = name;
    }

    private int position;
    private int modelNumber;
    private final String modelName;

    public int getModelNumber() {
        return modelNumber;
    }

    public String getModelName() {
        return modelName;
    }

    public int getPosition() {
        return position;
    }

    public static LevelModel getByName(String name) {
        for (LevelModel model : values()) {
            if (model.getModelName().equalsIgnoreCase(name)) {
                return model;
            }
        }
        return null;
    }

    public static LevelModel getByNumber(int modelNumber) {
        for (LevelModel model : values()) {
            if (model.getModelNumber() == modelNumber) {
                return model;
            }
        }
        return null;
    }

    public static LevelModel getByPosition(int position) {
        for (LevelModel model : values()) {
            if (model.getPosition() == position) {
                return model;
            }
        }
        return null;
    }
}
