package com.theshopatvsp.levelandroidsdk.ble.model.constants;

/**
 * Created by andrco on 6/12/16.
 */
public enum BatteryState {
    Charging(1), Discharging(2), Charged(4);

    BatteryState(int state) {
        this.state = state;
    }

    private int state;

    public int getState() {
        return state;
    }

    public static BatteryState getByState(int state) {
        for (BatteryState batteryState : values()) {
            if (batteryState.getState() == state) {
                return batteryState;
            }
        }

        return null;
    }
}
