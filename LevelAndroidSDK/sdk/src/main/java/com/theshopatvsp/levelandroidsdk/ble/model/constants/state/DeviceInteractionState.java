package com.theshopatvsp.levelandroidsdk.ble.model.constants.state;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.DeviceCommand;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataType;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.IndependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.state.*;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.ReportAttributesData;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TransmitControlData;

/**
 * Created by andrco on 6/12/16.
 */
public enum DeviceInteractionState implements LevelDeviceEvent {
    QueryLock {
        @Override
        public DeviceInteractionState success() {
            return SendLedCode1;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return QueryLock;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.LOCKRD;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    SendLedCode1 {
        @Override
        public DeviceInteractionState success() {
            return SendLedCode2;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return null;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    SendLedCode2 {
        @Override
        public DeviceInteractionState success() {
            return SendLedCode3;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return null;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    SendLedCode3 {
        @Override
        public DeviceInteractionState success() {
            return SendLedCode4;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return null;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    SendLedCode4 {
        @Override
        public DeviceInteractionState success() {
            return QueryTime;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return null;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    QueryTime {
        @Override
        public DeviceInteractionState success() {
            return SetTime;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.TIMERD;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    SetTime {
        @Override
        public DeviceInteractionState success() {
            return QueryReporter0;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.TIMEWR;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    QueryReporter0 {
        @Override
        public DeviceInteractionState success() {
            return QueryReporter1;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.REPORT_ATTRIBUTES;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return new ReportAttributesData(0);
        }
    },
    QueryReporter1 {
        @Override
        public DeviceInteractionState success() {
            return QueryReporter2;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.REPORT_ATTRIBUTES;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return new ReportAttributesData(1);
        }
    },
    QueryReporter2 {
        @Override
        public DeviceInteractionState success() {
            return QueryReportControl;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.REPORT_ATTRIBUTES;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return new ReportAttributesData(2);
        }
    },
    QueryReportControl {
        @Override
        public DeviceInteractionState success() {
            return Done;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return DeviceCommand.REPORT_CONTROL;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    },
    Done {
        @Override
        public DeviceInteractionState success() {
            return SendLedCode2;
        }

        @Override
        public DeviceInteractionState uhoh() {
            return SendLedCode1;
        }

        @Override
        public DeviceCommand getCommand() {
            return null;
        }

        @Override
        public DataPacket getPacket(int reporters) {
            return null;
        }
    }
}
