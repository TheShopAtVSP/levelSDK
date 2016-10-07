package com.theshopatvsp.levelandroidsdk.ble.helper;

/**
 * Created by andrco on 10/5/15.
 */
public class BitsHelper {
    public static int convertTo16BitInteger(byte msb, byte lsb) {
        return (((int)msb & 0xFF) << 8) + ((int)lsb & 0xFF);
    }

    public static int convertTo12BitInteger(byte msb, byte lsb) {
        return (((int)msb & 0x0F) << 8) + (lsb & 0xFF);
    }

    public static long convetTo32BitLong(byte msb, byte mlsb, byte llsb, byte lsb) {
        //long ret = (long)(msb << 24) + (long)(mlsb << 16) + (long)(llsb << 8) + ((long)lsb);
        byte bytes[] = new byte[4];
        bytes[3] = lsb;
        bytes[2] = llsb;
        bytes[1] = mlsb;
        bytes[0] = msb;

        return toInt(bytes, 0);
    }

    public static int convertTo24BitInteger(byte msb, byte mmsb, byte lsb) {
        return  (((int)msb & 0xFF) << 16) + (((int)mmsb & 0xFF) << 8) + ((int)lsb & 0xFF);
    }

    public static byte[] convertIntTo4Bytes(int value) {
        byte data[] = new byte[4];

        data[0] = (byte) (value & 0x000000FF);
        data[1] = (byte) ((value >> 8) & 0x000000FF);
        data[2] = (byte) ((value >> 16) & 0x000000FF);
        data[3] = (byte) ((value >> 24) & 0x000000FF);

        return data;
    }

    public static byte[] convertLongTo4Bytes(long time) {
        int itime = (int) (time / 1000);
        byte data[] = new byte[4];

        data[0] = (byte) (itime & 0x000000FF);
        data[1] = (byte) ((itime >> 8) & 0x000000FF);
        data[2] = (byte) ((itime >> 16) & 0x000000FF);
        data[3] = (byte) ((itime >> 24) & 0x000000FF);

        return data;
    }

    public static byte[] convertIntTo2Bytes(int num) {
        byte data[] = new byte[2];

        data[0] = (byte) (num & 0x000000FF);
        data[1] = (byte) ((num >> 8) & 0x000000FF);

        return data;
    }

    public static byte[] convertIntTo2HexBytes(int num) {
        byte data[] = new byte[2];

        data[0] = (byte) ((num >> 4) & 0x0000000F);
        data[1] = (byte) (num & 0x0000000F);

        return data;
    }

    public static byte convert2BytesToHexByte(byte first, byte second) {
        return (byte) ((first & 0x0F) << 4 | (second & 0x0F));
    }

    public static int toInt(byte[] bytes, int offset) {
        int ret = 0;
        for (int i=0; i<4 && i+offset<bytes.length; i++) {
            ret <<= 8;
            ret |= (int)bytes[i] & 0xFF;
        }
        return ret;
    }
}
