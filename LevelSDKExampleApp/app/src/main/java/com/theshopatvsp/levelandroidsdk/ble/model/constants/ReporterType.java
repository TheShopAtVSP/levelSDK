package com.theshopatvsp.levelandroidsdk.ble.model.constants;

import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentVariableDescription;

/**
 * Created by andrco on 9/25/16.
 */
public enum ReporterType {
    Accel(2, 4, DependentVariableDescription.ACCELEROMETER_RAW), Gyro(1, 2, DependentVariableDescription.GYROMETER_RAW),
    Steps(0, 1, DependentVariableDescription.STEP_PER_TIME);

    ReporterType(int reporter, int reportControlBit, DependentVariableDescription variableDescription) {
        this.reporter = reporter;
        this.reportControlBit = reportControlBit;
        this.depVarDesc = variableDescription;
    }

    private int reporter;
    private int reportControlBit;
    private DependentVariableDescription depVarDesc;

    public int getReporter() {
        return this.reporter;
    }

    public int getReportControlBit() {
        return reportControlBit;
    }

    public DependentVariableDescription getDepVarDesc() {
        return depVarDesc;
    }

    public static ReporterType getByReporter(int reporter) {
        for (ReporterType type : values()) {
            if (type.getReporter() == reporter) {
                return type;
            }
        }

        return null;
    }

    public static ReporterType getByName(String name) {
        for (ReporterType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
