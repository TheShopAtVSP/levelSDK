package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by andrco on 1/19/16.
 */
public class UserUuid extends DataPacket {
    private String userUuid;

    private static Map<Character, Byte> hex = new HashMap<>();
    static {
        hex.put('0', (byte) 0);
        hex.put('1', (byte) 1);
        hex.put('2', (byte) 2);
        hex.put('3', (byte) 3);
        hex.put('4', (byte) 4);
        hex.put('5', (byte) 5);
        hex.put('6', (byte) 6);
        hex.put('7', (byte) 7);
        hex.put('8', (byte) 8);
        hex.put('9', (byte) 9);
        hex.put('a', (byte) 10);
        hex.put('b', (byte) 11);
        hex.put('c', (byte) 12);
        hex.put('d', (byte) 13);
        hex.put('e', (byte) 14);
        hex.put('f', (byte) 15);
    }

    private static Map<Byte, Character> back = new HashMap<>();
    static {
        back.put((byte) 0, '0');
        back.put((byte) 1, '1');
        back.put((byte) 2, '2');
        back.put((byte) 3, '3');
        back.put((byte) 4, '4');
        back.put((byte) 5, '5');
        back.put((byte) 6, '6');
        back.put((byte) 7, '7');
        back.put((byte) 8, '8');
        back.put((byte) 9, '9');
        back.put((byte) 10, 'a');
        back.put((byte) 11, 'b');
        back.put((byte) 12, 'c');
        back.put((byte) 13, 'd');
        back.put((byte) 14, 'e');
        back.put((byte) 15, 'f');
    }

    public UserUuid(String userUuid) {
        this.userUuid = userUuid;
    }

    public UserUuid(byte[] packet) {
        if( packet.length > 2 ) {
            String uuidNoDashes = "";

            for (int i = 17; i > 1; i--) {
                byte num[] = BitsHelper.convertIntTo2HexBytes(packet[i]);

                for (byte hex : num) {
                    uuidNoDashes += back.get(hex);
                }
            }

            this.userUuid = uuidNoDashes.substring(0, 8) + "-" + uuidNoDashes.substring(8, 12) + "-" +
                    uuidNoDashes.substring(12, 16) + "-" + uuidNoDashes.substring(16, 20) + "-" +
                    uuidNoDashes.substring(20);
        }
    }

    public String getUserUuid() {
        return userUuid;
    }

    public byte[] getPacket() {
        String withoutDashes = this.userUuid.replace("-", "").toLowerCase();
        byte bytes[] = new byte[16];
        int counter = 15;

        for (int i = 0; i < 32; i+=2) {
            byte first = hex.get(withoutDashes.charAt(i));
            byte second = hex.get(withoutDashes.charAt(i + 1));

            bytes[counter--] = BitsHelper.convert2BytesToHexByte(first, second);
        }

        return bytes;
    }
}
