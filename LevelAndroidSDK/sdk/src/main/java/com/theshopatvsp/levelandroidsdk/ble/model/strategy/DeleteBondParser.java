package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 6/1/16.
 */
public class DeleteBondParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new DeleteBondPacket(packet);
    }
}
