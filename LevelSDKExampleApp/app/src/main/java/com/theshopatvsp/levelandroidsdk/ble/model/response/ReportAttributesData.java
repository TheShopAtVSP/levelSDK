package com.theshopatvsp.levelandroidsdk.ble.model.response;

import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataType;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.IndependentVariableDescription;
import  com.theshopatvsp.levelandroidsdk.ble.model.exception.PacketIntegrityException;
import  com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import  com.theshopatvsp.levelandroidsdk.ble.model.response.*;

/**
 * Created by andrco on 10/2/15.
 */
public class ReportAttributesData extends DataPacket {
    private IndependentVariableDescription indVarDescription;
    private int indVarScale;
    private DependentVariableDescription depVarDescription;
    private DependentDataType depDataType;
    private DependentDataScale depDataScale;
    private int dataFieldsPerSample;
    private int samplePerRecord;
    private int maxRecordsPerReport;
    private boolean queryOnly = false;

    public ReportAttributesData(int reporter) {
        this.reporter = reporter;
        this.queryOnly = true;
    }

    public ReportAttributesData(int reporter, IndependentVariableDescription indVarDescription, int indVarScale,
                                DependentVariableDescription depVarDescription, DependentDataType depDataType,
                                DependentDataScale depDataScale, int dataFieldsPerSample,
                                int samplePerRecord, int maxRecordsPerReport) {
        super();
        this.setReporter(reporter);
        this.indVarDescription = indVarDescription;
        this.indVarScale = indVarScale;
        this.depVarDescription = depVarDescription;
        this.depDataType = depDataType;
        this.depDataScale = depDataScale;
        this.dataFieldsPerSample = dataFieldsPerSample;
        this.samplePerRecord = samplePerRecord;
        this.maxRecordsPerReport = maxRecordsPerReport;
    }

    public ReportAttributesData(byte data[]) throws PacketIntegrityException {
        super();
        this.setReporter(data[2]);
        this.indVarDescription = IndependentVariableDescription.getById(data[3]);
        this.depVarDescription = DependentVariableDescription.getById(data[5]);
        this.depDataType = DependentDataType.getById(data[6]);
        this.depDataScale = DependentDataScale.getById(data[7]);

        if (indVarDescription == null || depVarDescription == null || depDataType == null || depDataScale == null) {
            throw new PacketIntegrityException("Packet data was incorrect and caused Report Attribute enum to be null");
        }

        this.indVarScale = data[4];

        this.dataFieldsPerSample = data[8];
        this.samplePerRecord = BitsHelper.convertTo16BitInteger(data[10], data[9]);
        this.maxRecordsPerReport = BitsHelper.convertTo16BitInteger(data[12], data[11]);
    }

    public IndependentVariableDescription getIndVarDescription() {
        return indVarDescription;
    }

    public int getIndVarScale() {
        return indVarScale;
    }

    public DependentVariableDescription getDepVarDescription() {
        return depVarDescription;
    }

    public DependentDataType getDepDataType() {
        return depDataType;
    }

    public DependentDataScale getDepDataScale() {
        return depDataScale;
    }

    public int getDataFieldsPerSample() {
        return dataFieldsPerSample;
    }

    public int getSamplePerRecord() {
        return samplePerRecord;
    }

    public int getMaxRecordsPerReport() {
        return maxRecordsPerReport;
    }

    public byte[] getPacket() {
        byte bytes[] = null;

        if( !queryOnly ) {
            bytes = new byte[11];

            byte samplesPerRecord[] = BitsHelper.convertIntTo2Bytes(this.samplePerRecord);
            byte maxRecords[] = BitsHelper.convertIntTo2Bytes(this.maxRecordsPerReport);

            bytes[0] = (byte) this.reporter;
            bytes[1] = (byte) this.indVarDescription.getId();
            bytes[2] = (byte) this.indVarScale;
            bytes[3] = (byte) this.depVarDescription.getId();
            bytes[4] = (byte) this.depDataType.getId();
            bytes[5] = (byte) this.depDataScale.getId();
            bytes[6] = (byte) this.dataFieldsPerSample;
            bytes[7] = samplesPerRecord[0];
            bytes[8] = samplesPerRecord[1];
            bytes[9] = maxRecords[0];
            bytes[10] = maxRecords[1];
        }
        else {
            bytes = new byte[1];

            bytes[0] = (byte) this.reporter;
        }

        return bytes;
    }

    public boolean isQueryOnly() {
        return queryOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportAttributesData that = (ReportAttributesData) o;

        if (indVarScale != that.indVarScale) return false;
        if (dataFieldsPerSample != that.dataFieldsPerSample) return false;
        if (samplePerRecord != that.samplePerRecord) return false;
        if (maxRecordsPerReport != that.maxRecordsPerReport) return false;
        if (indVarDescription != that.indVarDescription) return false;
        if (depVarDescription != that.depVarDescription) return false;
        if (depDataType != that.depDataType) return false;
        boolean ret = depDataScale == that.depDataScale;

        return ret;

    }

    @Override
    public int hashCode() {
        int result = indVarDescription != null ? indVarDescription.hashCode() : 0;
        result = 31 * result + indVarScale;
        result = 31 * result + (depVarDescription != null ? depVarDescription.hashCode() : 0);
        result = 31 * result + (depDataType != null ? depDataType.hashCode() : 0);
        result = 31 * result + (depDataScale != null ? depDataScale.hashCode() : 0);
        result = 31 * result + dataFieldsPerSample;
        result = 31 * result + samplePerRecord;
        result = 31 * result + maxRecordsPerReport;
        return result;
    }

    @Override
    public String toString() {
        if( queryOnly )
            return "reporter: " + reporter;
        else
            return "reporter: " + reporter +
                ", indVarDesc: " + indVarDescription +
                ", indVarScale: " + indVarScale +
                ", depVarDesc: " + depVarDescription +
                ", depDataType: " + depDataType +
                ", depDataScale: " + depDataScale +
                ", dataFieldsPerSample: " + dataFieldsPerSample +
                ", samplePerRecord: " + samplePerRecord +
                ", maxRecordsPerReport: " + maxRecordsPerReport;
    }
}
