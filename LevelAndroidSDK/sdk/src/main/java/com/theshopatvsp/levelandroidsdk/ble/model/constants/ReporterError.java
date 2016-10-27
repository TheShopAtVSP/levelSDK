package com.theshopatvsp.levelandroidsdk.ble.model.constants;

/**
 * Created by andrco on 10/3/16.
 */
public enum ReporterError {
    NOERROR(0), ReporterEnabled(1), ReporterInstanceError(2), DependentDataTypeError(3), ReporterDataNotEmpty(4);

    ReporterError(int e) {
        this.errorCode = e;
    }

    private int errorCode;

    public int getErrorCode() {
        return errorCode;
    }

    public static ReporterError getByErrorCode(int code) {
        for (ReporterError e : values()) {
            if (e.getErrorCode() == code) {
                return e;
            }
        }

        return null;
    }
}
