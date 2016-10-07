package com.theshopatvsp.levelandroidsdk.ble.model.bootloader;

import android.util.Log;

import  com.theshopatvsp.levelandroidsdk.ble.helper.ArchiveInputStream;
import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import  com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.BootloaderState;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by andrco on 3/7/16.
 */
public class BootloaderStateMachine {
    private static final String TAG = BootloaderStateMachine.class.getSimpleName();
    private BootloaderState state;
    private int bytesUploaded = 0, packetsSent = 0;
    private int softDeviceImageSize, bootloaderImageSize, appImageSize;
    private InputStream initPacket;
    private ArchiveInputStream firmwareImage;
    private int contentType;
    private int packetNum;
    private long startTime;

    public BootloaderStateMachine() {
        state = BootloaderState.GET_VERSION;
    }

    public int getBytesUploaded() {
        return bytesUploaded;
    }

    public byte[] process(DFUResult result) throws IOException {
        //Log.v(TAG, "state machine process: " + state);

        if( state != BootloaderState.UPLOAD_IMAGE && state != BootloaderState.SEND_INIT_PACKET)
            state = state.process();

        if( state == BootloaderState.START_DFU ) {
            byte bytes[] = new byte[1];
            bytes[0] = (byte) contentType;

            return bytes;
        }

        if (state == BootloaderState.SEND_IMAGE_SIZE) {
            byte sdSize[] = BitsHelper.convertIntTo4Bytes(this.softDeviceImageSize);
            byte blSize[] = BitsHelper.convertIntTo4Bytes(this.bootloaderImageSize);
            byte appSize[] = BitsHelper.convertIntTo4Bytes(this.appImageSize);

            byte data[] = new byte[12];
            int counter = 0;

            for( int i = 0; i < sdSize.length; i++ ) {
                data[counter++] = sdSize[i];
            }

            for( int i = 0; i < blSize.length; i++ ) {
                data[counter++] = blSize[i];
            }

            for( int i = 0; i < appSize.length; i++ ) {
                data[counter++] = appSize[i];
            }

            return data;
        }

        if (state == BootloaderState.SEND_INIT_PACKET) {
            byte data[] = new byte[20];

            if (initPacket.available() > 0) {
                int size = initPacket.read(data, 0, data.length);

                return checkSize(data, size);
            } else {
                state = state.process();
            }
        }

        if (state == BootloaderState.SEND_PACKET_NUM) {
            byte data[] = new byte[1];

            data[0] = (byte) this.packetNum;

            return data;
        }

        if( state == BootloaderState.UPLOAD_IMAGE) {
            if (startTime == 0) {
                startTime = new Date().getTime();
            }
            byte data[] = new byte[20];

            if (firmwareImage.available() > 0 ) {
                int size = firmwareImage.read(data);

                bytesUploaded += size;
                packetsSent++;

                return checkSize(data, size);
            } else {
                long now = (new Date().getTime() - startTime) / 1000;
                Log.v(TAG, "UPLOAD TOOK: " + now + " seconds.");
                state = state.process();
            }
        }

        return null;
    }

    private byte[] checkSize(byte[] data, int size) {
        if (size != data.length) {
            byte buffer[] = new byte[size];
            System.arraycopy(data, 0, buffer, 0, size);

            return buffer;
        }

        return data;
    }

    public BootloaderState getState() {
        return state;
    }

    public void setImageSizes(int softDeviceImageSize, int bootloaderImageSize, int appImageSize) {
        this.softDeviceImageSize = softDeviceImageSize;
        this.bootloaderImageSize = bootloaderImageSize;
        this.appImageSize = appImageSize;
    }

    public void setInitPacket(InputStream initPacket) {
        this.initPacket = initPacket;
    }

    public void setFirmwareImage(ArchiveInputStream firmwareImage) {
        this.firmwareImage = firmwareImage;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public void setPacketNum(int packetNum) {
        this.packetNum = packetNum;
    }

    public int getPacketsSent() {
        return packetsSent;
    }

    public int getAppImageSize() {
        return appImageSize;
    }
}
