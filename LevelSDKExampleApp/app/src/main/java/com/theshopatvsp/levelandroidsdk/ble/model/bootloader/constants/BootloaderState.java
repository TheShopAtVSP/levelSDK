package com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants;

import  com.theshopatvsp.levelandroidsdk.ble.model.LevelCommand;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.CharacteristicEnum;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.ReadWriteEnum;

/**
 * Created by andrco on 3/7/16.
 */
public enum BootloaderState implements BootloaderEvent {
    GET_VERSION {
        @Override
        public BootloaderState process() {
            return START_DFU;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return new LevelCommand(ReadWriteEnum.READ, CharacteristicEnum.DFU_VERSION);
        }
    },
    START_DFU {
        @Override
        public BootloaderState process() {
            return SEND_IMAGE_SIZE;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[2];
            bytesToSend[0] = 0x01;
            bytesToSend[1] = bytes[0];

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    SEND_IMAGE_SIZE {
        @Override
        public BootloaderState process() {
            return SEND_INIT_PACKET_START;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_PACKET, bytes);
        }
    },
    SEND_INIT_PACKET_START {
        @Override
        public BootloaderState process() {
            return SEND_INIT_PACKET;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[2];
            bytesToSend[0] = 0x02;
            bytesToSend[1] = 0x00;

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    SEND_INIT_PACKET {
        @Override
        public BootloaderState process() {
            return SEND_INIT_PACKET_COMPLETE;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_PACKET, bytes);
        }
    },
    SEND_INIT_PACKET_COMPLETE {
        @Override
        public BootloaderState process() {
            return SEND_PACKET_NUM;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[2];
            bytesToSend[0] = 0x02;
            bytesToSend[1] = 0x01;

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    SEND_PACKET_NUM {
        @Override
        public BootloaderState process() {
            return SEND_WRITE_START;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[3];
            bytesToSend[0] = 0x08;
            bytesToSend[1] = (byte) (bytes[0] & 0xFF);
            bytesToSend[2] = (byte) ((bytes[0] >> 8) & 0xFF);

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    SEND_WRITE_START {
        @Override
        public BootloaderState process() {
            return UPLOAD_IMAGE;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[1];
            bytesToSend[0] = 0x03;

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    UPLOAD_IMAGE {
        @Override
        public BootloaderState process() {
            return WAIT_FOR_NOTIFICATION;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_PACKET, bytes);
        }
    },
    WAIT_FOR_NOTIFICATION {
        @Override
        public BootloaderState process() {
            return SEND_VALIDATE;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return null;
        }
    },
    SEND_VALIDATE {
        @Override
        public BootloaderState process() {
            return SEND_ACTIVATE_AND_REBOOT;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[1];
            bytesToSend[0] = 0x04;

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    SEND_ACTIVATE_AND_REBOOT {
        @Override
        public BootloaderState process() {
            return RECONNECT_AND_VALIDATE;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            byte bytesToSend[] = new byte[1];
            bytesToSend[0] = 0x05;

            return new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.DFU_CONTROL_POINT, bytesToSend);
        }
    },
    RECONNECT_AND_VALIDATE {
        @Override
        public BootloaderState process() {
            return null;
        }

        @Override
        public LevelCommand getCommand(byte[] bytes) {
            return null;
        }
    }
}
