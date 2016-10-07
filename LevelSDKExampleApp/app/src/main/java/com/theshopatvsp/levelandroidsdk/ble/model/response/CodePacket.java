package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 5/27/16.
 */
public class CodePacket extends DataPacket {
    private int code;


    public CodePacket(int code) {
        super();
        this.code = code;
    }

    public CodePacket(byte bytes[]) {
        this.code = bytes[0];
    }

    public byte[] getPacket() {
        byte bytes[] = new byte[1];

        bytes[0] = (byte) code;

        return bytes;
    }

    public int getCode() {
        return code;
    }
}
