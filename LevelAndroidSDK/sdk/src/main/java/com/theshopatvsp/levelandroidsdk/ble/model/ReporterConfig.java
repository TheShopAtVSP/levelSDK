package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DataFields;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.IndependentVariableDescription;
import com.theshopatvsp.levelandroidsdk.ble.model.response.ReportAttributesData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrco on 9/25/16.
 */
public class ReporterConfig implements Serializable {
    private ReporterType type;
    private int samplingHz;
    private DependentDataScale dependentDataScale;
    private IndependentVariableDescription indVarDesc;
    private int dataFields;
    private int samplesPerRecord;
    private int maxNumberOfRecords;

    public ReporterType getType() {
        return type;
    }

    public int getSamplingHz() {
        return samplingHz;
    }

    public DependentDataScale getDependentDataScale() {
        return dependentDataScale;
    }

    public IndependentVariableDescription getIndVarDesc() {
        return indVarDesc;
    }

    public int getDataFields() {
        return dataFields;
    }

    public int getSamplesPerRecord() {
        return samplesPerRecord;
    }

    public int getMaxNumberOfRecords() {
        return maxNumberOfRecords;
    }

    private ReporterConfig(ReporterType type, int samplingHz, DependentDataScale dependentDataScale,
                           IndependentVariableDescription indVarDesc, int dataFields, int samplesPerRecord, int maxNumberOfRecords) {
        this.type = type;
        this.samplingHz = samplingHz;
        this.dependentDataScale = dependentDataScale;
        this.indVarDesc = indVarDesc;
        this.dataFields = dataFields;
        this.samplesPerRecord = samplesPerRecord;
        this.maxNumberOfRecords = maxNumberOfRecords;


    }

    public static class Builder {
        private ReporterType type;
        private int samplingHz;
        private DependentDataScale dependentDataScale;
        private List<DataFields> dataFields = new ArrayList<>();
        private int samplesPerRecord;
        private int maxNumberOfRecords;
        private IndependentVariableDescription indVarDesc;

        public Builder attrs(ReportAttributesData attrs) {
            type = ReporterType.getByReporter(attrs.getReporter());
            dependentDataScale = attrs.getDepDataScale();
            samplingHz = attrs.getIndVarScale();
            samplesPerRecord = attrs.getSamplePerRecord();
            maxNumberOfRecords = attrs.getMaxRecordsPerReport();

            if (type == ReporterType.Steps) {
                this.indVarDesc = IndependentVariableDescription.SECONDS;
            } else {
                this.indVarDesc = IndependentVariableDescription.SAMPLING_HZ;
            }

            for (DataFields field : DataFields.values()) {
                if ((field.getBit() & attrs.getDataFieldsPerSample()) > 0) {
                    dataFields.add(field);
                }
            }

            return this;
        }

        public Builder step() {
            this.type = ReporterType.Steps;
            this.indVarDesc = IndependentVariableDescription.SECONDS;

            return this;
        }

        public Builder accel() {
            this.type = ReporterType.Accel;
            this.indVarDesc = IndependentVariableDescription.SAMPLING_HZ;

            return this;
        }

        public Builder gyro() {
            this.type = ReporterType.Gyro;
            this.indVarDesc = IndependentVariableDescription.SAMPLING_HZ;

            return this;
        }

        public Builder samplingFrequency(int samplingHz) {
            this.samplingHz = samplingHz;

            return this;
        }

        public Builder dependentDataScale(DependentDataScale scale) {
            this.dependentDataScale = scale;

            return this;
        }

        public Builder includeAllAxis() {
            this.dataFields.add(DataFields.INCLUDE_X_AXIS);
            this.dataFields.add(DataFields.INCLUDE_Y_AXIS);
            this.dataFields.add(DataFields.INCLUDE_Z_AXIS);

            return this;
        }

        public Builder includeMagnitude() {
            this.dataFields.add(DataFields.INCLUDE_MAGNITUDE);

            return this;
        }

        public Builder includeXAxis() {
            this.dataFields.add(DataFields.INCLUDE_X_AXIS);

            return this;
        }

        public Builder includeYAxis() {
            this.dataFields.add(DataFields.INCLUDE_Y_AXIS);

            return this;
        }

        public Builder includeZAxis() {
            this.dataFields.add(DataFields.INCLUDE_Z_AXIS);

            return this;
        }

        public Builder samplesPerRecord(int samplesPerRecord) {
            this.samplesPerRecord = samplesPerRecord;

            return this;
        }

        public Builder maxRecordsPerReport(int maxNumberOfRecords) {
            this.maxNumberOfRecords = maxNumberOfRecords;

            return this;
        }

        public ReporterConfig build() {
            int dataFieldsInt = 0;

            if (dataFields != null && !dataFields.isEmpty()) {
                for (DataFields dataField : dataFields) {
                    dataFieldsInt |= dataField.getBit();
                }
            }

            if (dependentDataScale == null) {
                dependentDataScale = DependentDataScale.ONE_TO_ONE_BIT;
            }

            return new ReporterConfig(type, samplingHz, dependentDataScale, indVarDesc, dataFieldsInt, samplesPerRecord, maxNumberOfRecords);
        }
    }
}
