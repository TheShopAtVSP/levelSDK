package com.theshopatvsp.levelandroidsdk.ble.model.constants;

/**
 * Created by andrco on 6/12/16.
 */
public enum LedColor {
    YELLOW(0x03), RED(0x02), PURPLE(0x01), WHITE(0x00);

    LedColor(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }
}
