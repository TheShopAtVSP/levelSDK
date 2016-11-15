package com.theshopatvsp.levelandroidsdkexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.theshopatvsp.levelandroidsdk.ble.model.DeviceClient;
import com.theshopatvsp.levelandroidsdk.ble.model.DeviceObserverCallbacks;
import com.theshopatvsp.levelandroidsdk.ble.model.ReporterConfig;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterError;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DataFields;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;

import java.util.Set;

/**
 * Created by andrco on 6/15/16.
 */
public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = DashboardActivity.class.getSimpleName();
    private DeviceClient deviceClient;
    private DeviceObserverCallbacks callbacks = new DeviceObserverCallbacks() {
        @Override
        public void onBluetoothNotAvailable() {
            Log.v(TAG, "onBluetoothNotAvailable");
        }

        @Override
        public void onBluetoothNotOn() {
            Log.v(TAG, "onBluetoothNotOn");
        }

        @Override
        public void onInputLedCode() {
            Log.v(TAG, "onInputLedCode");
        }

        @Override
        public void onLedCodeAccepted() {
            Log.v(TAG, "onLedCodeAccepted");
        }

        @Override
        public void onLedCodeFailed() {
            Log.v(TAG, "onLedCodeFailed");
        }

        @Override
        public void onLedCodeNotNeeded() {
            Log.v(TAG, "onLedCodeNotNeeded");
        }

        @Override
        public void onDisconnect() {Log.v(TAG, "onDisconnect");}

        @Override
        public void onData(RecordData data) {
            Log.v(TAG, "onData");
        }

        @Override
        public void onDataDeleted() {
            Log.v(TAG, "onDataDeleted");
        }

        @Override
        public void onDataStreamChanged(int numOfRecords) {
            Log.v(TAG, "onDataStreamChanged " + numOfRecords);
        }

        @Override
        public void onLedCodeDone() {Log.v(TAG, "onLedCodeDone");}

        @Override
        public void onDeviceReady() {
            Log.v(TAG, "onDeviceReady");
            deviceClient.getBatteryLevel();
            deviceClient.getBatteryState();
        }

        @Override
        public void onSetupSuccess() {
            Log.v(TAG, "onSetupSuccess");
        }

        @Override
        public void onSetupFailed(ReporterError error) {
            Log.v(TAG, "onSetupFailed " + error);
        }

        @Override
        public void onReporterQueried(ReporterConfig config) {
            Log.v(TAG, "onReporterQueried ");
            reporterSpinner.setSelection(config.getType().getReporter());
            sampleFrequency.setText("" + config.getSamplingHz());
            dataScaleSpinner.setSelection(config.getDependentDataScale().getId());
            samplesPerRecord.setText("" + config.getSamplesPerRecord());
            maxRecords.setText("" + config.getMaxNumberOfRecords());

            if( (config.getDataFields() & DataFields.INCLUDE_X_AXIS.getBit()) > 0 ) {
                xaxis.setChecked(true);
            }

            if( (config.getDataFields() & DataFields.INCLUDE_Y_AXIS.getBit()) > 0 ) {
                yaxis.setChecked(true);
            }

            if( (config.getDataFields() & DataFields.INCLUDE_Z_AXIS.getBit()) > 0 ) {
                zaxis.setChecked(true);
            }

            if( (config.getDataFields() & DataFields.INCLUDE_MAGNITUDE.getBit()) > 0 ) {
                magnitude.setChecked(true);
            }
        }

        @Override
        public void onReportersEnabled(Set<ReporterType> activeReporters) {
            Log.v(TAG, "onReportersEnabled");

            for (ReporterType type : activeReporters) {
                Log.v(TAG, "Reporter " + type + " active.");
            }
        }

        @Override
        public void onBatteryLevel(final int level) {
            Log.v(TAG, "onBatteryLevel");
            batteryLevel.post(new Runnable() {
                @Override
                public void run() {
                    batteryLevel.setText("" + level);
                }
            });
        }

        @Override
        public void onBatteryState(final BatteryState state) {
            Log.v(TAG, "onBatteryState");
            batteryState.post(new Runnable() {
                @Override
                public void run() {
                    batteryState.setText(state.name());
                }
            });
        }
    };
    private Spinner reporterSpinner;
    private Spinner dataScaleSpinner;
    private Spinner queryReporterSpinner;
    private Spinner reporterActionSpinner;
    private EditText sampleFrequency;
    private CheckBox xaxis;
    private CheckBox yaxis;
    private CheckBox zaxis;
    private CheckBox magnitude;
    private EditText samplesPerRecord;
    private EditText maxRecords;
    private Button setUp;
    private Button doIt, enableData, nukeData;
    private TextView dataText, batteryLevel, batteryState;
    private boolean dataOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        deviceClient = new DeviceClient();

        reporterSpinner = (Spinner) findViewById(R.id.reporterTypeSpinner);
        ArrayAdapter<CharSequence> reporterAdapter = ArrayAdapter.createFromResource(this, R.array.reporter_types, android.R.layout.simple_spinner_item);
        reporterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reporterSpinner.setAdapter(reporterAdapter);

        dataScaleSpinner = (Spinner) findViewById(R.id.depDataScale);
        ArrayAdapter<CharSequence> dataScaleAdapter = ArrayAdapter.createFromResource(this, R.array.dep_data_scale, android.R.layout.simple_spinner_item);
        dataScaleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataScaleSpinner.setAdapter(dataScaleAdapter);

        queryReporterSpinner = (Spinner) findViewById(R.id.queryReporter);
        ArrayAdapter<CharSequence> queryReporterAdapter = ArrayAdapter.createFromResource(this, R.array.reporter_types, android.R.layout.simple_spinner_item);
        queryReporterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        queryReporterSpinner.setAdapter(queryReporterAdapter);

        reporterActionSpinner = (Spinner) findViewById(R.id.reporterAction);
        ArrayAdapter<CharSequence> reporterActionAdapter = ArrayAdapter.createFromResource(this, R.array.reporter_actions, android.R.layout.simple_spinner_item);
        reporterActionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reporterActionSpinner.setAdapter(reporterActionAdapter);

        sampleFrequency = (EditText) findViewById(R.id.samplingFrequencyText);
        xaxis = (CheckBox) findViewById(R.id.xaxis);
        yaxis = (CheckBox) findViewById(R.id.yaxis);
        zaxis = (CheckBox) findViewById(R.id.zaxis);
        magnitude = (CheckBox) findViewById(R.id.magnitude);

        samplesPerRecord = (EditText) findViewById(R.id.samplesPerRecord);
        maxRecords = (EditText) findViewById(R.id.maxRecords);

        dataText = (TextView) findViewById(R.id.data_view);
        batteryLevel = (TextView) findViewById(R.id.batteryLevelText);
        batteryState = (TextView) findViewById(R.id.batteryStateText);

        setUp = (Button) findViewById(R.id.setUpReporter);

        setUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReporterData();
            }
        });

        doIt = (Button) findViewById(R.id.doIt);

        doIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = (String) queryReporterSpinner.getSelectedItem();
                String action = (String) reporterActionSpinner.getSelectedItem();

                if( action.equalsIgnoreCase("Query")) {
                    deviceClient.queryReporter(ReporterType.getByName(type));
                } else if (action.equalsIgnoreCase("Enable")) {
                    deviceClient.enableReporter(ReporterType.getByName(type));
                } else if (action.equalsIgnoreCase("Disable")) {
                    deviceClient.disableReporter(ReporterType.getByName(type));
                }
            }
        });

        enableData = (Button) findViewById(R.id.enableData);

        enableData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!dataOn) {
                    dataOn = true;
                    enableData.setText("Disable Data Stream");
                    deviceClient.enableDataStream();
                } else {
                    dataOn = false;
                    enableData.setText("Enable Data Stream");
                    deviceClient.disableDataStream();
                }
            }
        });

        nukeData = (Button) findViewById(R.id.nukeData);

        nukeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceClient.deleteAllStoredData();
            }
        });

        Button queryReportControl = (Button) findViewById(R.id.queryReportControl);
        queryReportControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deviceClient.queryEnabledReporters();
            }
        });
    }

    private void sendReporterData() {
        ReporterConfig.Builder configBuilder = new ReporterConfig.Builder();
        String reporterType = (String) reporterSpinner.getSelectedItem();


        if( "Steps".equalsIgnoreCase(reporterType) ) {
            configBuilder = configBuilder.step();
        } else if ("Accel".equalsIgnoreCase(reporterType)) {
            configBuilder = configBuilder.accel();
        } else {
            configBuilder = configBuilder.gyro();
        }


        int samplingFrequency = Integer.valueOf((sampleFrequency.getText() == null) ? "0" : sampleFrequency.getText().toString());
        DependentDataScale scale = DependentDataScale.getById(dataScaleSpinner.getSelectedItemPosition());
        int samplesPerRec = Integer.valueOf((samplesPerRecord.getText() == null) ? "0" : samplesPerRecord.getText().toString());
        int maxRecs = Integer.valueOf((maxRecords.getText() == null) ? "0" : maxRecords.getText().toString());

        configBuilder = configBuilder.samplingFrequency(samplingFrequency)
                .dependentDataScale(scale)
                .samplesPerRecord(samplesPerRec)
                .maxRecordsPerReport(maxRecs);

        if (xaxis.isChecked()) {
            configBuilder = configBuilder.includeXAxis();
        }

        if (yaxis.isChecked()) {
            configBuilder = configBuilder.includeYAxis();
        }

        if (zaxis.isChecked()) {
            configBuilder = configBuilder.includeZAxis();
        }

        if (magnitude.isChecked()) {
            configBuilder = configBuilder.includeMagnitude();
        }

        deviceClient.setUpReporter(configBuilder.build());
    }

    @Override
    public void onPause() {
        super.onPause();

        deviceClient.unregisterDeviceCallbacks();
    }

    @Override
    public void onResume() {
        super.onResume();

        deviceClient.registerDeviceCallbacks(callbacks);
    }
}
