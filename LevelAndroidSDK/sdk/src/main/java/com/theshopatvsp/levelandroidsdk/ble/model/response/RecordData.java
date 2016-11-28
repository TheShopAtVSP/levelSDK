package com.theshopatvsp.levelandroidsdk.ble.model.response;

import android.text.format.DateUtils;
import android.util.Log;

import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TimePacket;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by andrco on 10/5/15.
 */
public class RecordData extends TimePacket {
    public static final int HEADER_LEGTH = 8;
    private static final String TAG = RecordData.class.getSimpleName();

    private int data[];
    private int currentBytes = 0;
    private int totalBytes = 0;
    private long originalTimestamp;
    private ReporterType type;

    public RecordData() {
        super();
    }

    //used to start a record
    public RecordData(byte packet[]) {
        super();
        id = BitsHelper.convertTo16BitInteger(packet[3], packet[2]);
        totalBytes = BitsHelper.convertTo12BitInteger(packet[5], packet[4]) - HEADER_LEGTH; //subtract the header length
        reporter = (packet[5] & 0xF0) >> 4;
        type = ReporterType.getByReporter(reporter);
        long now = new Date().getTime();
        timestamp = BitsHelper.convetTo32BitLong(packet[9], packet[8], packet[7], packet[6]) * 1000; //convert seconds to millis
        originalTimestamp = timestamp;

        Log.v(TAG, "new record total = " + totalBytes);
        Log.v(TAG, "RECORDDATA TIMESTAMP = " + timestamp + "readable data: " + new Date(timestamp));

        if (totalBytes > 0) {
            data = new int[totalBytes / 2];

            for( int i = HEADER_LEGTH + 2; i < packet.length; i+=2 ) {
                data[currentBytes++] = BitsHelper.convertTo16BitInteger(packet[i+1], packet[i]);

                if (isFinished()) {
                    break;
                }
            }
        }
    }

    public RecordData continueRecord(byte packet[]) {
        for (int i = 2; i < packet.length; i+=2) {
            data[currentBytes++] = BitsHelper.convertTo16BitInteger(packet[i+1], packet[i]);;

            if (isFinished()) {
                break;
            }
        }

        Log.v(TAG, "cur Bytes = " + currentBytes + " total = " + totalBytes);

        return this;
    }

    public boolean isFinished() {
        return (currentBytes*2) == totalBytes;
    }

    public int[] getData() {
        return data;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    /*public List<StepData> toSteps() {
        List<StepData> steps = new ArrayList<>();

        return steps;
    }*/

    public long getOriginalTimestamp() {
        return originalTimestamp;
    }


    public void setData(int[] data) {
        this.data = data;
    }

    public void setCurrentBytes(int currentBytes) {
        this.currentBytes = currentBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public ReporterType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "RecordData{" +
                "data=" + Arrays.toString(data) +
                ", currentBytes=" + currentBytes +
                ", totalBytes=" + totalBytes +
                "} " + super.toString();
    }
}
