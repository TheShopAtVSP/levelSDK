package com.theshopatvsp.levelandroidsdk.ble.model;

import android.util.Log;

import com.theshopatvsp.levelandroidsdk.ble.BleManager;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;

import java.util.UUID;

/**
 * Created by andrco on 6/17/16.
 */
public class DeviceClient {
    private static final String TAG = DeviceClient.class.getSimpleName();
    private UUID id;
    private DeviceObserverCallbacks callbacks;

    public DeviceClient() {
        this.id = UUID.randomUUID();
    }

    /**
     * Register the device observer callbacks.
     *
     * @param callbacks - callbacks to be called when there's data from the device.
     */
    public void registerDeviceCallbacks(DeviceObserverCallbacks callbacks) {
        Log.v(TAG, "registerDeviceCallbacks called");
        this.callbacks = callbacks;

        BleManager.registerObserver(this.id, callbacks);
    }

    /**
     * Unregister callbacks to stop receiving notifications from the device.
     */
    public void unregisterDeviceCallbacks() {
        Log.v(TAG, "unregisterDeviceCallbacks called");
        BleManager.unregisterObserver(this.id);
    }

    /**
     * Connect to a specific device or connect to the closest device
     *
     * @param frameId - connect to the device ending in the frameId, set to null or the empty string to connect to the closest.
     */
    public void connect(String frameId) {
        Log.v(TAG, "connect called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.InitiateConnection, frameId));
    }

    /**
     * Disconnect from the current device.
     */
    public void disconnect() {
        Log.v(TAG, "disconnect called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.Disconnect));
    }

    /**
     * Is the device connected to the app?
     *
     * @return - true (connected) / false (not connected)
     */
    public boolean isConnected() {
        Log.v(TAG, "isConnected called");
        return BleManager.isConnected();
    }

    /**
     * Send the led code from Blink to link to the device
     *
     * @param code - Hex code of the led color
     */
    public void sendLedCode(int code) {
        Log.v(TAG, "sendLedCode called: " + code);
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.SendLedCode, code));
    }

    /**
     * If the led on the device has not light up, call this method to go to the next closest device
     */
    public void deviceLightsNotOn() {
        Log.v(TAG, "deviceLightsNotOn called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DeviceLightsNotOn));
    }

    /**
     * Get the battery level from the device
     */
    public void getBatteryLevel() {
        Log.v(TAG, "getBatteryLevel called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.GetBatteryLevel));
    }

    /**
     * Get the battery state of the device, @see com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState
     */
    public void getBatteryState() {
        Log.v(TAG, "getBatteryState called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.GetBatteryState));
    }

    /**
     * Query the configuration of the given reporter type
     *
     * @param type - the reporter type to query
     */
    public void queryReporter(ReporterType type) {
        Log.v(TAG, "queryReporter called: " + type);
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.QueryReporter, type.getReporter()));
    }

    /**
     * Configure the device reporters to specific settings
     *
     * @param config - config object @see com.theshopatvsp.levelandroidsdk.ble.model.ReporterConfig
     */
    public void setUpDevice(DeviceConfig config) {
        Log.v(TAG, "setUpDevice called: " + config.toString());
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.SetUpReporter, config));
    }

    /**
     * Turn off the reporter to keep it from generating data
     *
     * @param type - the reporter type to turn off
     */
    public void disableReporter(ReporterType type) {
        Log.v(TAG, "disableReporter called: " + type);
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DisableReporter, type));
    }

    /**
     * Turn on the reporter to get it to start generating data
     *
     * @param type - the reporter type to turn off
     */
    public void enableReporter(ReporterType type) {
        Log.v(TAG, "enableReporter called: " + type);
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.EnableReporter, type));
    }

    public void queryEnabledReporters() {
        Log.v(TAG, "queryEnabledReporters called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.QueryReportControl));
    }

    /**
     * Turn on the data stream to download the generated data from the device
     */
    public void enableDataStream() {
        Log.v(TAG, "enableDataStream called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.EnableDataStream));
    }

    /**
     * Turn off the data stream to download the generated data from the device
     */
    public void disableDataStream() {
        Log.v(TAG, "disableDataStream called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DisableDataStream));
    }

    /**
     * Delete all fo the stored data from the device
     */
    public void deleteAllStoredData() {
        Log.v(TAG, "deleteAllStoredData called");
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DeleteStoredData));
    }
}
