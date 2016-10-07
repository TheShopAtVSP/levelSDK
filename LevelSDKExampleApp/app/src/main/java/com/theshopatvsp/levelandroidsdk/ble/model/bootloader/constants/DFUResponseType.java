package com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants;

/**
 * Created by andrco on 3/8/16.
 */
public enum DFUResponseType {
    PACKET_RECEIPT(0x11), RESPONSE_CODE(0x10), UNKNOWN(-1);

    DFUResponseType(int type) {
        this.responseType = type;
    }

    private final int responseType;

    public int getResponseType() {
        return responseType;
    }

    public static final DFUResponseType getByType(int responseType) {
        for (DFUResponseType type : values()) {
            if (type.getResponseType() == responseType) {
                return type;
            }
        }

        return UNKNOWN;
    }
}
