package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TransmitControlData;

/**
 * Created by andrco on 10/2/15.
 */
public class TrasmitControlParser implements PacketParser {
    private static final String TAG = TrasmitControlParser.class.getSimpleName();
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new TransmitControlData(BitsHelper.convertTo16BitInteger(packet[3], packet[2]),
                BitsHelper.convertTo24BitInteger(packet[6], packet[5], packet[4]));
    }
}
