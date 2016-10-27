package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.LevelColor;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.LevelModel;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 5/12/16.
 */
public class Frame extends DataPacket {
    private LevelModel model;
    private LevelColor color;

    public Frame(LevelModel model, LevelColor color) {
        this.model = model;
        this.color = color;
    }

    public Frame(byte[] packet) {
        if (packet != null && packet.length > 2) {
            int m = BitsHelper.convertTo16BitInteger(packet[3], packet[2]);
            int c = BitsHelper.convertTo16BitInteger(packet[5], packet[4]);

            this.model = LevelModel.getByNumber(m);
            this.color = LevelColor.getByNumber(c);
        }
    }

    public LevelModel getModel() {
        return model;
    }

    public LevelColor getColor() {
        return color;
    }

    public byte[] getPacket() {
        byte bytes[] = new byte[4];

        byte modelBytes[] = BitsHelper.convertIntTo2Bytes(model.getModelNumber());
        byte colorBytes[] = BitsHelper.convertIntTo2Bytes(color.getColorNumber());

        bytes[0] = modelBytes[0];
        bytes[1] = modelBytes[1];
        bytes[2] = colorBytes[0];
        bytes[3] = colorBytes[1];

        return bytes;
    }
}
