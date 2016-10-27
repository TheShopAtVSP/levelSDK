package com.theshopatvsp.levelandroidsdk.ble.model;

import android.content.Intent;
import android.util.Log;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.BleDeviceOutput;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataType;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.IndependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.state.DeviceInteractionState;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.CodePacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.LockPacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.ReportAttributesData;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TimePacket;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.TransmitControlData;

/**
 * Created by andrco on 6/12/16.
 */
public class DeviceStateMachine {
    private static final String TAG = DeviceStateMachine.class.getSimpleName();
    private DeviceInteractionState state;
    private boolean reporter0Right = false, reporter1Right = false, reporter0IsOn = false, reporter1IsOn = false;
    private boolean reporter2Right = false, reporter2IsOn = false;
    private int packetsToDownload = 0, currentPacketsDownloaded = 0;
    private boolean timeIsCorrect = false, transmitControlOn = false, needLedCode = false;
    private long timeDiff, deviceTime;


    private ReportAttributesData reporter0 = new ReportAttributesData(0, IndependentVariableDescription.SECONDS, 15,
            DependentVariableDescription.STEP_PER_TIME, DependentDataType.UINT8, DependentDataScale.ONE_TO_ONE_BIT,
            0, 4, 0);
    private ReportAttributesData reporter1 = new ReportAttributesData(1, IndependentVariableDescription.ON_CHANGE, 1,
            DependentVariableDescription.BATTERY_PERCENT_REMAIN, DependentDataType.INT24, DependentDataScale.ONE_TO_ONE_BIT,
            0, 1, 0);
    private ReportAttributesData reporter2 = null;
    public DeviceStateMachine() {
    state = DeviceInteractionState.QueryLock;
    }

    public DataPacket processResult(DataPacket data) {
        Log.v(TAG, "processing result state = " + state);

        if (state == DeviceInteractionState.QueryLock && data instanceof LockPacket) {
            LockPacket lock = (LockPacket)data;

            if (lock.getLock() == 0) {
                needLedCode = true;
            } else {
                needLedCode = false;
                state = DeviceInteractionState.QueryTime;
                return null;
            }
        }

        if (state.name().toLowerCase().startsWith("sendledcode") && data instanceof CodePacket) {
            state = state.success();
            return null;
        }

        /*if (state == DeviceInteractionState.DownloadData && currentPacketsDownloaded < packetsToDownload) {
            Log.v(TAG, "Downloading data total = " + packetsToDownload + " current = " + currentPacketsDownloaded);
            return null;
        }*/

        if( state == DeviceInteractionState.QueryTime && data != null && data instanceof TimePacket ) {
            TimePacket time = (TimePacket) data;

            deviceTime = time.getTimestamp();

            if( Math.abs(time.getReceived().getTime() - time.getTimestamp()) < (60 * 1000) ) {
                timeIsCorrect = true;
            } else {
                timeDiff = time.getReceived().getTime() - time.getTimestamp();
            }
        }

        /*if( state == DeviceInteractionState.QueryReporter0 && data != null && data.equals(DeviceInteractionState.SetupReporter0.getPacket(0))) {
            Log.v(TAG, "reporter 0 is right!!!");
            reporter0Right = true;
        }

        if( state == DeviceInteractionState.QueryReporter1 && data != null && data.equals(DeviceInteractionState.SetupReporter1.getPacket(0))) {
            Log.v(TAG, "reporter 1 is right!!!");
            reporter1Right = true;
        }

        if( state == DeviceInteractionState.QueryReporter2 && data != null && data.equals(DeviceInteractionState.SetupReporter2.getPacket(0))) {
            Log.v(TAG, "reporter 2 is right!!!");
            reporter2Right = true;
        }

        if (state == DeviceInteractionState.QueryReportControl && data != null) {
            int reportControl = data.getReportControl();

            if ((reportControl & 0x01) == 0x01) {
                reporter0IsOn = true;
            }

            if ((reportControl & 0x02) == 0x02) {
                reporter1IsOn = true;
            }

            if ((reportControl & 0x04) == 0x04) {
                reporter2IsOn = true;
            }
        }

        if (state == DeviceInteractionState.QueryData && data instanceof TransmitControlData) {
            packetsToDownload = ((TransmitControlData)data).getTotalRecordCount();
        }

        if (state == DeviceInteractionState.DrainData) {
            transmitControlOn = true;
        }*/

        state = state.success();

        int reporters = 0;

        if (state == DeviceInteractionState.SetTime && timeIsCorrect) {
            state = state.success();
        }

        /*if( state == DeviceInteractionState.DisableReporters && ((reporter0Right && reporter1Right & reporter2IsOn) || data.getReportControl() == 0) ) {
            state = state.success();
        }

        if( state == DeviceInteractionState.DisableReporters ) {
            if( reporter0Right ) {
                reporters |= 0x01;
            } else {
                reporter0IsOn = false;
            }

            if( reporter1Right ) {
                reporters |= 0x02;
            } else {
                reporter1IsOn = false;
            }

            if (reporter2Right) {
                reporters |= 0x04;
            } else {
                reporter2IsOn = false;
            }
        }

        if (state == DeviceInteractionState.DrainData && packetsToDownload == 0) {
            state = state.success();
            state = state.success();
        }

        if( state == DeviceInteractionState.SetupReporter0 && reporter0Right ) {
            Log.v(TAG, "not setting reporter 0 cuz its right!!!");
            state = state.success();
        }

        if( state == DeviceInteractionState.SetupReporter1 && reporter1Right ) {
            Log.v(TAG, "not setting reporter 1 cuz its right!!!");
            state = state.success();
        }

        if( state == DeviceInteractionState.SetupReporter2 && reporter2Right ) {
            Log.v(TAG, "not setting reporter 2 cuz its right!!!");
            state = state.success();
        }

        if( state == DeviceInteractionState.EnableReporters && reporter0IsOn && reporter1IsOn && reporter2IsOn ) {
            state = state.success();
        }

        if (state == DeviceInteractionState.EnsureTransmitControlIsOn && transmitControlOn) {
            state = state.success();
        }*/

        Log.v(TAG, "processing result OUT state = " + state);

        return state.getPacket(reporters);
    }

    public Intent disconnect() {
        state = state.uhoh();

        if( state.name().toLowerCase().startsWith("sendledcode") ) {
            Log.v(TAG, "disconnected, sending LedCodeFailed");
            return new Intent(BleDeviceOutput.LedCodeFailed.name());
        }

        reporter0Right = false;
        reporter1Right = false;
        reporter0IsOn = false;
        reporter1IsOn = false;
        reporter2Right = false;
        reporter2IsOn = false;
        packetsToDownload = 0;
        currentPacketsDownloaded = 0;
        timeIsCorrect = false;
        transmitControlOn = false;
        timeDiff = 0;

        return null;
    }

    public void isBonded() {
        state = DeviceInteractionState.QueryLock;
    }

    public DeviceInteractionState getState() {
        return state;
    }

    public boolean sendPacket(int reporter) {
        if ((reporter == 0 && reporter0Right) || (reporter == 1 && reporter1Right) ||
                (reporter == 2 && reporter2Right)) {
            return true;
        }

        return false;
    }

    public boolean isTimeIsCorrect() {
        return timeIsCorrect;
    }

    public long getTimeDiff() {
        return timeDiff;
    }

    public void incPacketToDownload() {
        currentPacketsDownloaded++;
    }

    public boolean needLedCode() {
        return needLedCode;
    }

    public long getDeviceTime() {
        return deviceTime;
    }

    public void reset() {
        reporter0Right = false;
        reporter1Right = false;
        reporter0IsOn = false;
        reporter1IsOn = false;
        reporter2Right = false;
        reporter2IsOn = false;
        packetsToDownload = 0;
        currentPacketsDownloaded = 0;
        timeIsCorrect = false;
        transmitControlOn = false;
        timeDiff = 0;
        deviceTime = 0;
        if( state == DeviceInteractionState.Done ) {
            state = DeviceInteractionState.QueryTime;
        } else {
            state = DeviceInteractionState.QueryLock;
        }
    }
}
