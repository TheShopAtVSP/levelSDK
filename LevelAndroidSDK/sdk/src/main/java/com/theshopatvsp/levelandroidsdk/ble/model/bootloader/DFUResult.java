package com.theshopatvsp.levelandroidsdk.ble.model.bootloader;

import  com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.DFUResponseType;

/**
 * Created by andrco on 3/8/16.
 */
public class DFUResult {
    private DFUResponseType responseType;
    private int result;
    private int error;

    public DFUResult() {}

    public DFUResult(DFUResponseType responseType, int result, int error) {
        this.responseType = responseType;
        this.result = result;
        this.error = error;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public DFUResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(DFUResponseType responseType) {
        this.responseType = responseType;
    }
}
