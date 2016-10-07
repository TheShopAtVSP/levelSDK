package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleClientCommand;

/**
 * Created by andrco on 6/17/16.
 */
public class ClientCommand {
    private BleClientCommand command;
    private Object thing;

    public ClientCommand(BleClientCommand command) {
        this.command = command;
    }

    public ClientCommand(BleClientCommand command, Object thing) {
        this.command = command;
        this.thing = thing;
    }

    public BleClientCommand getCommand() {
        return command;
    }

    public Object getThing() {
        return thing;
    }
}
