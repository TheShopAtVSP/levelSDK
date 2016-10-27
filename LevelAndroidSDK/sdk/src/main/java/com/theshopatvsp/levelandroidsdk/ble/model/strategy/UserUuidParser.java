package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.UserUuid;

/**
 * Created by andrco on 1/27/16.
 */
public class UserUuidParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new UserUuid(packet);
    }
}
