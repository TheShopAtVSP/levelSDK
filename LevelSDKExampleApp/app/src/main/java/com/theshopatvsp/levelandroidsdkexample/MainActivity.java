package com.theshopatvsp.levelandroidsdkexample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.theshopatvsp.levelandroidsdk.ble.model.DeviceClient;
import com.theshopatvsp.levelandroidsdk.ble.model.DeviceObserverCallbacks;
import com.theshopatvsp.levelandroidsdk.ble.model.ReporterConfig;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.LedColor;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterError;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int COARSE_LOCATION_PERMISSION = 1234;
    private static final int WRITE_EXTERNAL_PERMISSION = 4321;
    private Handler connectHandler = new Handler();
    private TextView statusView, codeView;
    private int acceptedCodes = 0;
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
            statusView.post(new Runnable() {
                @Override
                public void run() {
                    statusView.setText("Device Ready Input Led Code.");
                }
            });
        }

        @Override
        public void onLedCodeAccepted() {
            Log.v(TAG, "onLedCodeAccepted");
            statusView.post(new Runnable() {
                @Override
                public void run() {
                    statusView.setText("Led Code accepted, Input the next one.");
                }
            });

            if( ++acceptedCodes >= 4 ) {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            }
        }

        @Override
        public void onLedCodeFailed() {
            Log.v(TAG, "onLedCodeFailed");
            statusView.post(new Runnable() {
                @Override
                public void run() {
                    statusView.setText("Input Code Failed, Please try again.");
                    codeView.setText("");
                }
            });
        }

        @Override
        public void onLedCodeNotNeeded() {
            Log.v(TAG, "onLedCodeNotNeeded");
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        }

        @Override
        public void onLedCodeDone() {
            Log.v(TAG, "onLedCodeDone");
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
        }

        @Override
        public void onDeviceReady() {
            Log.v(TAG, "onDeviceReady");
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

        }

        @Override
        public void onReportersEnabled(Set<ReporterType> activeReporters) {
            Log.v(TAG, "onReportersEnabled");
        }

        @Override
        public void onDisconnect() {
            Log.v(TAG, "onDisconnect");
        }

        @Override
        public void onData(RecordData data) {
            Log.v(TAG, "onData");
        }

        @Override
        public void onDataStreamChanged(int numOfRecords) {
            Log.v(TAG, "onDataStreamChanged " + numOfRecords);
        }

        @Override
        public void onDataDeleted() {
            Log.v(TAG, "onDataDeleted");
        }

        @Override
        public void onBatteryLevel(int level) {
            Log.v(TAG, "onBatteryLevel");
        }

        @Override
        public void onBatteryState(BatteryState state) {
            Log.v(TAG, "onBatteryState");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceClient = new DeviceClient();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        COARSE_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //have to delay by a second because service startup makes it miss the intent otherwise
            connectHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG, "sending InitiateConnection Intent");
                    deviceClient.registerDeviceCallbacks(callbacks);
                    deviceClient.connect("");
                }
            }, 1000);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        WRITE_EXTERNAL_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        statusView = (TextView) findViewById(R.id.light_status);
        codeView = (TextView) findViewById(R.id.code_view);


        ImageButton yellow = (ImageButton) findViewById(R.id.yellow_button);
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "YELLOW!!");
                codeView.setText(codeView.getText() + " " + LedColor.YELLOW.name());
                sendCode(LedColor.YELLOW.getCode());
            }
        });

        ImageButton red = (ImageButton) findViewById(R.id.red_button);
        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "RED!!");

                codeView.setText(codeView.getText() + " " + LedColor.RED.name());
                sendCode(LedColor.RED.getCode());
            }
        });

        ImageButton purple = (ImageButton) findViewById(R.id.purple_button);
        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "PURPLE!!");
                codeView.setText(codeView.getText() + " " + LedColor.PURPLE.name());
                sendCode(LedColor.PURPLE.getCode());
            }
        });

        ImageButton white = (ImageButton) findViewById(R.id.white_button);
        white.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "WHITE!!!");
                codeView.setText(codeView.getText() + " " + LedColor.WHITE.name());
                sendCode(LedColor.WHITE.getCode());
            }
        });

        /*startActivityForResult(new Intent(
                BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);*/
    }

    @Override
    public void onPause() {
        super.onPause();

        deviceClient.registerDeviceCallbacks(callbacks);
    }

    @Override
    public void onResume() {
        super.onResume();

        deviceClient.unregisterDeviceCallbacks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case COARSE_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    connectHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.v(TAG, "sending InitiateConnection Intent");

                            deviceClient.registerDeviceCallbacks(callbacks);
                            deviceClient.connect(null);
                        }
                    }, 1000);
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case WRITE_EXTERNAL_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "WRITE Permissions granted!!!");
                }
                break;

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void sendCode(int code) {
        Log.v(TAG, "Sending code to manager " + code);

        deviceClient.sendLedCode(code);
    }
}
