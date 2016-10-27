/*
package com.theshopatvsp.level.levelandroidsdk.ble.model;

import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theshopatvsp.level.levelandroidsdk.ble.model.constants.BatteryState;
import com.theshopatvsp.level.levelandroidsdk.model.LastLocation;
import com.theshopatvsp.level.model.BatteryStatusLog;

import java.util.Date;

import rx.Subscriber;


*/
/**
 * Created by nandpa on 7/26/16.
 *//*

public class DeviceObserverCallbacksImpl implements DeviceObserverCallbacks {
    private DeviceClientBluetoothDelegate deviceClientBluetoothDelegate;
    private DeviceClientConnectionDelegate deviceClientConnectionDelegate;
    private DeviceClientFirmwareVersionDelegate deviceClientFirmwareVersionDelegate;
    private DeviceClientFrameDelegate deviceClientFrameDelegate;
    private DeviceClientInternalDataDelegate deviceClientInternalDataDelegate;
    private DeviceClientLinkDelegate deviceClientLinkDelegate;
    private DeviceClientLocationDelegate deviceClientLocationDelegate;
    private DeviceClientStepDelegate deviceClientStepDelegate;
    private DeviceClientBatteryDelegate deviceClientBatteryDelegate;


    public TextView statusView;

    public TextView codeView;


    @Override
    public void onBluetoothNotAvailable() {
        //Log.v(TAG, "onBluetoothNotAvailable");
    }

    @Override
    public void onBluetoothNotOn() {

        //Log.v(TAG, "onBluetoothNotOn");
    }

    @Override
    public void onInputLedCode() {
        //Log.v(TAG, "onInputLedCode");
       */
/* statusView.post(new Runnable() {
            @Override
            public void run() {
                statusView.setText("Device Ready Input Led Code.");
            }
        });*//*

        //deviceClientLinkDelegate.onInputLedCode();
    }

    @Override
    public void onLedCodeAccepted() {
        deviceClientLinkDelegate.onLedCodeAccepted();
    }

    @Override
    public void onLedCodeFailed() {
        System.out.println("********* in call back onLedCodeFailed");
        deviceClientLinkDelegate.onLedCodeFailed();
    }

    @Override
    public void onLedCodeNotNeeded() {
        //Log.v(TAG, "onLedCodeNotNeeded");
       */
/* startActivity(new Intent(MainActivity.this, DashboardActivity.class));*//*

    }

    @Override
    public void onLedCodeDone() {

    }

    @Override
    public void onDeviceReady() {
        //Log.v(TAG, "onDeviceReady");
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onBootloaderProgress(int progress) {

    }

    @Override
    public void onBootloaderFinished() {

        //Log.v(TAG, "onBootloaderFinished");
    }


    @Override
    public void onLastUserLocation(LastLocation location) {

    }

    @Override
    public void onStep(Step step) {
      deviceClientStepDelegate.onStep(step);
    }

    @Override
    public void onBatteryReport(BatteryReport batteryReport) {
        System.out.printf(" ********************* onBatteryReport");

    }

    @Override
    public void onMotionData(AccelFilt accelFilt) {

    }

    @Override
    public void onBatteryLevel(int level) {
        System.out.printf(" ********************* onBatteryLevel" + level);
        if(deviceClientBatteryDelegate != null) {
            deviceClientBatteryDelegate.onBatteryLevel(level);
        }
    }

    @Override
    public void onBatteryState(BatteryState state) {
        try {
            System.out.printf(" ********************* onBatteryState" + new ObjectMapper().writeValueAsString(state));
            if(deviceClientBatteryDelegate != null) {
                deviceClientBatteryDelegate.onBatteryState(state);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFirmwareVersion(String firmwareVersion) {
        deviceClientFirmwareVersionDelegate.onFirmwareVersion(firmwareVersion);
    }

    @Override
    public void onFrame(com.theshopatvsp.level.levelandroidsdk.ble.model.response.Frame frame) {

    }

    public DeviceClientBluetoothDelegate getDeviceClientBluetoothDelegate() {
        return deviceClientBluetoothDelegate;
    }

    public void setDeviceClientBluetoothDelegate(DeviceClientBluetoothDelegate deviceClientBluetoothDelegate) {
        this.deviceClientBluetoothDelegate = deviceClientBluetoothDelegate;
    }

    public DeviceClientConnectionDelegate getDeviceClientConnectionDelegate() {
        return deviceClientConnectionDelegate;
    }

    public void setDeviceClientConnectionDelegate(DeviceClientConnectionDelegate deviceClientConnectionDelegate) {
        this.deviceClientConnectionDelegate = deviceClientConnectionDelegate;
    }

    public DeviceClientFirmwareVersionDelegate getDeviceClientFirmwareVersionDelegate() {
        return deviceClientFirmwareVersionDelegate;
    }

    public void setDeviceClientFirmwareVersionDelegate(DeviceClientFirmwareVersionDelegate deviceClientFirmwareVersionDelegate) {
        this.deviceClientFirmwareVersionDelegate = deviceClientFirmwareVersionDelegate;
    }

    public DeviceClientFrameDelegate getDeviceClientFrameDelegate() {
        return deviceClientFrameDelegate;
    }

    public void setDeviceClientFrameDelegate(DeviceClientFrameDelegate deviceClientFrameDelegate) {
        this.deviceClientFrameDelegate = deviceClientFrameDelegate;
    }

    public DeviceClientInternalDataDelegate getDeviceClientInternalDataDelegate() {
        return deviceClientInternalDataDelegate;
    }

    public void setDeviceClientInternalDataDelegate(DeviceClientInternalDataDelegate deviceClientInternalDataDelegate) {
        this.deviceClientInternalDataDelegate = deviceClientInternalDataDelegate;
    }

    public DeviceClientLinkDelegate getDeviceClientLinkDelegate() {
        return deviceClientLinkDelegate;
    }

    public void setDeviceClientLinkDelegate(DeviceClientLinkDelegate deviceClientLinkDelegate) {
        this.deviceClientLinkDelegate = deviceClientLinkDelegate;
    }

    public DeviceClientLocationDelegate getDeviceClientLocationDelegate() {
        return deviceClientLocationDelegate;
    }

    public void setDeviceClientLocationDelegate(DeviceClientLocationDelegate deviceClientLocationDelegate) {
        this.deviceClientLocationDelegate = deviceClientLocationDelegate;
    }

    public DeviceClientStepDelegate getDeviceClientStepDelegate() {
        return deviceClientStepDelegate;
    }

    public void setDeviceClientStepDelegate(DeviceClientStepDelegate deviceClientStepDelegate) {
        this.deviceClientStepDelegate = deviceClientStepDelegate;
    }

    public DeviceClientBatteryDelegate getDeviceClientBatteryDelegate() {
        return deviceClientBatteryDelegate;
    }

    public void setDeviceClientBatteryDelegate(DeviceClientBatteryDelegate deviceClientBatteryDelegate) {
        this.deviceClientBatteryDelegate = deviceClientBatteryDelegate;
    }
}
*/
