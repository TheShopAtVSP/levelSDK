package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.exception.DataLengthException;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.NackAttributeException;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.NackDataLengthException;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.NackPacketSequenceException;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.NackPacketTypeError;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;

/**
 * Created by andrco on 10/2/15.
 */
public class Nack implements PacketParser {
    @Override
    public DataPacket parse(byte[] packet) throws Exception {
        if (packet == null || packet.length < 2) {
            throw new DataLengthException("packet is null or length < 2");
        }

        switch (NackError.getById(packet[2])) {
            case PACKET_SEQ_ERROR:
                throw new NackPacketSequenceException("NACK packet sequence error");
            case PACKET_TYPE_ERROR:
                throw new NackPacketTypeError("Nack packet type exception");
            case DATA_LENGTH_ERROR:
                throw new NackDataLengthException("NACK data length error");
            case ATTRIBUTE_ERROR:
                throw new NackAttributeException("NACK attribute error");
        }

        return null;
    }

    public enum NackError {
        PACKET_SEQ_ERROR(2), PACKET_TYPE_ERROR(5), DATA_LENGTH_ERROR(6),
        ATTRIBUTE_ERROR(9);

        NackError(int id) {
            this.id = id;
        }

        private int id;

        public int getId() {
            return id;
        }

        public static NackError getById(int id) {
            for (NackError error : values()) {
                if (error.getId() == id) {
                    return error;
                }
            }

            return null;
        }
    }
}
