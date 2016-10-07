package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.NukeRecordsPacket;

/**
 * Created by andrco on 9/28/16.
 */
public class NukeRecordsParser implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        return new NukeRecordsPacket(packet);
    }
}
