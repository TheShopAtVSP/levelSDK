package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.exception.DataLengthException;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;

/**
 * Created by andrco on 10/2/15.
 */
public class StartOfRecord implements PacketParser {
    @Override
    public RecordData parse(byte[] packet) throws Exception {
        if (packet == null || packet.length < 10) {
            throw new DataLengthException("packet is null or length < 10");
        }

        return new RecordData(packet);
    }

    public RecordData continueRecord(RecordData record, byte packet[]) {
        return record.continueRecord(packet);
    }
}
