package com.theshopatvsp.levelandroidsdk.ble.model.strategy;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.DeviceCommand;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.DataLengthException;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.SequenceIdException;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;

/**
 * Created by andrco on 10/2/15.
 */
public class PacketParserManager {
    private static RecordData record;

    public static DataPacket parse(int expectedPacketIdIn, byte packet[]) throws Exception {

        DeviceCommand command = getCommand(expectedPacketIdIn, packet);

        if (command == null) {
            return null;
        }

        if (command == DeviceCommand.START_OF_RECORD) {
            record = new StartOfRecord().parse(packet);

            if( record.isFinished() )
                return record;
        } else if (command == DeviceCommand.RECORD_CONTINUE) {
            record = new StartOfRecord().continueRecord(record, packet);

            if (record.isFinished()) {
                return record;
            }
        } else {
            PacketParser parser = command.getParser();

            if (parser != null) {
                DataPacket data = parser.parse(packet);

                return data;
            }
        }

        return null;
    }

    private static DeviceCommand getCommand(int expectedPacketIdIn, byte packet[]) throws DataLengthException, SequenceIdException {
        int packetIdIn = 0x00 << 24 | packet[0] & 0xff;

        if( packet == null || packet.length < 2 ) {
            throw new DataLengthException("Packet null of length is less then 2.");
        }

        if( expectedPacketIdIn != packetIdIn) {
            throw new SequenceIdException("Packet ID: " + packetIdIn + " does not match " + expectedPacketIdIn);
        }

        return DeviceCommand.getByCommand(packet[1]);
    }
}
