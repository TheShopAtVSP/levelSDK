package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.model.response.*;

/**
 * Created by andrco on 10/2/15.
 */
public class TransmitControlData extends DataPacket {
    private int totalByteCount;
    private int totalRecordCount;
    private byte writeData;

    public TransmitControlData(int totalRecordCount, int totalByteCount) {
        super();
        this.totalByteCount = totalByteCount;
        this.totalRecordCount = totalRecordCount;
    }

    public TransmitControlData(byte writeData) {
        super();
        this.writeData = writeData;
    }

    public int getTotalByteCount() {
        return totalByteCount;
    }

    public void setTotalByteCount(int totalByteCount) {
        this.totalByteCount = totalByteCount;
    }

    public int getTotalRecordCount() {
        return totalRecordCount;
    }

    public void setTotalRecordCount(int totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public byte[] getPacket() {
        byte bytes[] = new byte[1];

        bytes[0] = this.writeData;

        return bytes;
    }
}
