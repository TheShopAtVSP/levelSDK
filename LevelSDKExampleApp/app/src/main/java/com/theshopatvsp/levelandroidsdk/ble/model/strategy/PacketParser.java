package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 10/2/15.
 */
public interface PacketParser {
    DataPacket parse(byte packet[]) throws Exception;
}
