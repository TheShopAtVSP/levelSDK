package com.theshopatvsp.levelandroidsdk.ble.model.constants;

import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.CodeParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.DeleteBondParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.FrameParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.LockParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.Nack;
import com.theshopatvsp.levelandroidsdk.ble.model.strategy.NukeRecordsParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.PacketParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.ReportAttributeParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.ReportControlParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.StartOfRecord;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.TimeParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.TrasmitControlParser;
import  com.theshopatvsp.levelandroidsdk.ble.model.strategy.UserUuidParser;

/**
 * Created by andrco on 10/1/15.
 */
public enum DeviceCommand {
    NACK(0, new Nack()), ACK(1, null), BOOTLOADER1(2, null), BOOTLOADER2(3, null), BOOTLOADER3(4, null),
    START_OF_RECORD(5, new StartOfRecord()), RECORD_CONTINUE(6, new StartOfRecord()),
    REPORT_ATTRIBUTES(7, new ReportAttributeParser()), REPORT_CONTROL(8, new ReportControlParser()),
    TRANSMIT_CONTROL(9, new TrasmitControlParser()), TIMERD(10, new TimeParser()), TIMEWR(11, new TimeParser()),
    USERUUIDRD(12, new UserUuidParser()), USERUUIDWR(13, new UserUuidParser()), FRAMERD(14, new FrameParser()),
    FRAMEWR(15, new FrameParser()), CODEWR(17, new CodeParser()), DELETE_BOND(18, new DeleteBondParser()), //send 22 as payload, send back an ack;
    NUKE_RECORDS(19, new NukeRecordsParser()), LOCKRD(20, new LockParser());

    DeviceCommand(int command, PacketParser parser) {
        this.command = command;
        this.parser = parser;
    }

    private int command;
    private PacketParser parser;

    public int getCommand() {
        return command;
    }

    public PacketParser getParser() {
        return parser;
    }

    public static DeviceCommand getByCommand(int command) {
        for (DeviceCommand comm : values()) {
            if (comm.getCommand() == command) {
                return comm;
            }
        }

        return null;
    }
}
