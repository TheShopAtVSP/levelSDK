package com.theshopatvsp.levelandroidsdk.ble.model.constants;

/**
 * Created by andrco on 7/25/16.
 */
public enum LevelColor {
    NOT_SET(0, "Not Set"), TORTOISE(200, "Tortoise"), SOFT_COPPER(225, "Soft Copper Tortoise"), BLACK(1, "Black"),
    GREY_TORTOISE(60, "Grey Tortoise"), SOFT_INDIGO_TORTOISE(410, "Soft Indigo Tortoise"),
    OTHER_BLACK(11, "Black"), BLACK_TORTOISE(15, "Black Tortoise"), PALE_ROSE_GOLD(600, "Pale Rose Gold"),
    OLIVE_GREY(300, "Olive Grey"), SLATE(400, "Slate"), DARK_GREY(65, "Dark Grey");

    LevelColor(int colorNumber, String name) {
        this.colorNumber = colorNumber;
        this.colorName = name;
    }

    private int colorNumber;
    private String colorName;

    public int getColorNumber() {
        return colorNumber;
    }

    public String getColorName() {
        return colorName;
    }

    public static LevelColor getByName(String name) {
        for (LevelColor color : values()) {
            if (color.getColorName().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }

    public static LevelColor getByNumber(int colorNumber) {
        for (LevelColor color : values()) {
            if (color.getColorNumber() == colorNumber) {
                return color;
            }
        }
        return null;
    }
}
