package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.DeviceCommand;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by andrco on 10/2/15.
 */
public class DataPacket implements Serializable {
    int id;
    int reporter;
    int reportControl;
    DeviceCommand command;
    Date received;

    public DataPacket() {
        received = new Date();
    }

    public DataPacket(int reportControl) {
        this();
        this.reportControl = reportControl;
    }

    public boolean finished() {
        return true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReporter() {
        return reporter;
    }

    public void setReporter(int reporter) {
        this.reporter = reporter;
    }

    public DeviceCommand getCommand() {
        return command;
    }

    public void setCommand(DeviceCommand command) {
        this.command = command;
    }

    public int getReportControl() {
        return reportControl;
    }

    public void setReportControl(int reportControl) {
        this.reportControl = reportControl;
    }

    public Date getReceived() {
        return received;
    }

    public void setReceived(Date received) {
        this.received = received;
    }

    public byte[] getPacket() {
        byte bytes[] = new byte[1];

        bytes[0] = (byte) this.reportControl;

        return bytes;
    }

    @Override
    public String toString() {
        return "DataPacket{" +
                "id=" + id +
                ", reporter=" + reporter +
                ", reportControl=" + reportControl +
                ", command=" + command +
                ", received=" + received +
                '}';
    }
}
