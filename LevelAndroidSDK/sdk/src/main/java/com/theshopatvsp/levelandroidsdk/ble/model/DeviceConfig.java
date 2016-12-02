package com.theshopatvsp.levelandroidsdk.ble.model;

import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DataFields;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by andrco on 12/1/16.
 */

public class DeviceConfig {
    private Set<ReporterConfig> reporterConfigs;
    private Set<ReporterType> enable;
    private Set<ReporterType> disable;

    private DeviceConfig(Builder builder) {
        this.reporterConfigs = builder.configs;
        this.enable = builder.enable;
        this.disable = builder.disable;
    }

    public Set<ReporterConfig> getReporterConfigs() {
        return reporterConfigs;
    }

    public Set<ReporterType> getEnable() {
        return enable;
    }

    public Set<ReporterType> getDisable() {
        return disable;
    }

    @Override
    public String toString() {
        return "DeviceConfig{" +
                "reporterConfigs=" + reporterConfigs +
                ", enable=" + enable +
                ", disable=" + disable +
                '}';
    }

    public static class Builder {
        private Set<ReporterConfig> configs;
        private Set<ReporterType> enable;
        private Set<ReporterType> disable;

        public Builder() {
            configs = new HashSet<>();
            enable = new HashSet<>();
            disable = new HashSet<>();
        }

        public Builder setUpStepReporter(int samplingFrequency, DependentDataScale dependentDataScale,
                                         int samplesPerRecord, int maxRecords) {
            configs.add(new ReporterConfig.Builder()
                    .step()
                    .samplingFrequency(samplingFrequency)
                    .dependentDataScale(dependentDataScale)
                    .samplesPerRecord(samplesPerRecord)
                    .maxRecordsPerReport(maxRecords)
                    .build());

            return this;
        }

        public Builder setUpGryoReporter(int samplingFrequency, DependentDataScale dependentDataScale,
                                         Set<DataFields> dataFields, int samplesPerRecord, int maxRecords) {
            configs.add(new ReporterConfig.Builder()
                    .gyro()
                    .dataFields(dataFields)
                    .samplingFrequency(samplingFrequency)
                    .dependentDataScale(dependentDataScale)
                    .samplesPerRecord(samplesPerRecord)
                    .maxRecordsPerReport(maxRecords)
                    .build());

            return this;
        }

        public Builder setUpAccelReporter(int samplingFrequency, DependentDataScale dependentDataScale,
                                         Set<DataFields> dataFields, int samplesPerRecord, int maxRecords) {
            configs.add(new ReporterConfig.Builder()
                    .accel()
                    .dataFields(dataFields)
                    .samplingFrequency(samplingFrequency)
                    .dependentDataScale(dependentDataScale)
                    .samplesPerRecord(samplesPerRecord)
                    .maxRecordsPerReport(maxRecords)
                    .build());

            return this;
        }

        public Builder enableReporter(ReporterType type) {
            this.enable.add(type);

            return this;
        }

        public Builder enableReporters(Set<ReporterType> reportersToEnable) {
            this.enable.addAll(reportersToEnable);

            return this;
        }

        public Builder disableReporter(ReporterType type) {
            this.disable.add(type);

            return this;
        }

        public Builder disableReporters(Set<ReporterType> reportersToDisable) {
            this.disable.addAll(reportersToDisable);

            return this;
        }

        public DeviceConfig build() {
            for( ReporterType d : enable ) {
                if (!disable.contains(d)) {
                    disable.add(d);
                }
            }

            return new DeviceConfig(this);
        }
    }
}
