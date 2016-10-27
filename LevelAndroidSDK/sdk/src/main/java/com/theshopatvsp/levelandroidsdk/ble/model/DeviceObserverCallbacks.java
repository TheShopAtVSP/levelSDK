package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterError;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.response.Frame;
import com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;
import com.theshopatvsp.levelandroidsdk.model.LastLocation;

import java.util.Set;

/**
 * Created by andrco on 6/17/16.
 */
public interface DeviceObserverCallbacks {
    void onBluetoothNotAvailable();
    void onBluetoothNotOn();
    void onInputLedCode();
    void onLedCodeAccepted();
    void onLedCodeFailed();
    void onLedCodeNotNeeded();
    void onLedCodeDone();
    void onDeviceReady();
    void onSetupSuccess();
    void onSetupFailed(ReporterError error);
    void onReporterQueried(ReporterConfig config);
    void onReportersEnabled(Set<ReporterType> activeReporters);
    void onDisconnect();
    void onData(RecordData data);
    void onDataDeleted();
    void onBatteryLevel(int level);
    void onBatteryState(BatteryState state);
}
