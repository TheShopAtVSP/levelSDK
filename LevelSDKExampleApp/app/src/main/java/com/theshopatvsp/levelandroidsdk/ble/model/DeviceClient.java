package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.ble.BleManager;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;

import java.util.UUID;

/**
 * Created by andrco on 6/17/16.
 */
public class DeviceClient {
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
        this.callbacks = callbacks;

        BleManager.registerObserver(this.id, callbacks);
    }

    /**
     * Unregister callbacks to stop receiving notifications from the device.
     */
    public void unregisterDeviceCallbacks() {
        BleManager.unregisterObserver(this.id);
    }

    /**
     * Connect to a specific device or connect to the closest device
     *
     * @param frameId - connect to the device ending in the frameId, set to null or the empty string to connect to the closest.
     */
    public void connect(String frameId) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.InitiateConnection, frameId));
    }

    /**
     * Disconnect from the current device.
     */
    public void disconnet() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.Disconnect));
    }

    /**
     * Is the device connected to the app?
     *
     * @return - true (connected) / false (not connected)
     */
    public boolean isConnected() {
        return BleManager.isConnected();
    }

    /**
     * Send the led code from Blink to link to the device
     *
     * @param code - Hex code of the led color
     */
    public void sendLedCode(int code) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.SendLedCode, code));
    }

    /**
     * If the led on the device has not light up, call this method to go to the next closest device
     */
    public void deviceLightsNotOn() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DeviceLightsNotOn));
    }

    /**
     * Get the battery level from the device
     */
    public void getBatteryLevel() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.GetBatteryLevel));
    }

    /**
     * Get the battery state of the device, @see com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState
     */
    public void getBatteryState() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.GetBatteryState));
    }

    /**
     * Query the configuration of the given reporter type
     *
     * @param type - the reporter type to query
     */
    public void queryReporter(ReporterType type) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.QueryReporter, type.getReporter()));
    }

    /**
     * Configure the reporter to specific settings
     *
     * @param config - config object @see com.theshopatvsp.levelandroidsdk.ble.model.ReporterConfig
     */
    public void setUpReporter(ReporterConfig config) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.SetUpReporter, config));
    }

    /**
     * Turn off the reporter to keep it from generating data
     *
     * @param type - the reporter type to turn off
     */
    public void disableReporter(ReporterType type) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DisableReporter, type));
    }

    /**
     * Turn on the reporter to get it to start generating data
     *
     * @param type - the reporter type to turn off
     */
    public void enableReporter(ReporterType type) {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.EnableReporter, type));
    }

    public void queryEnabledReporters() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.QueryReportControl));
    }

    /**
     * Turn on the data stream to download the generated data from the device
     */
    public void enableDataStream() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.EnableDataStream));
    }

    /**
     * Turn off the data stream to download the generated data from the device
     */
    public void disableDataStream() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DisableDataStream));
    }

    /**
     * Delete all fo the stored data from the device
     */
    public void deleteAllStoredData() {
        BleManager.addClientCommand(new ClientCommand(BleClientCommand.DeleteStoredData));
    }
}
