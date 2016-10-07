package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.ReportAttributesData;

/**
 * Created by andrco on 10/2/15.
 */
public class ReportAttributeParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new ReportAttributesData(packet);
    }
}
