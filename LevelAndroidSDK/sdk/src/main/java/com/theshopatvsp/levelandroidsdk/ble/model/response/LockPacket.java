package com.theshopatvsp.levelandroidsdk.ble.model.response;

/**
 * Created by andrco on 7/15/16.
 */
public class LockPacket extends DataPacket {
    private int lock;

    public LockPacket(int lock) {
        super();
        this.lock = lock;
    }

    public LockPacket(byte bytes[]) {
        this.lock = bytes[2];
    }

    public byte[] getPacket() {

        return null;
    }

    public int getLock() {
        return lock;
    }
}
