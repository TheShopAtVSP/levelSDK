package com.theshopatvsp.levelandroidsdk.ble;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theshopatvsp.levelandroidsdk.ble.helper.AlarmHelper;
import com.theshopatvsp.levelandroidsdk.ble.helper.BitsHelper;
import com.theshopatvsp.levelandroidsdk.ble.helper.CalculationHelper;
import com.theshopatvsp.levelandroidsdk.ble.helper.FileHelper;
import com.theshopatvsp.levelandroidsdk.ble.model.AccelFilt;
import com.theshopatvsp.levelandroidsdk.ble.model.BatteryReport;
import com.theshopatvsp.levelandroidsdk.ble.model.BleDeviceRecord;
import com.theshopatvsp.levelandroidsdk.ble.model.ClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.DeviceIdManager;
import com.theshopatvsp.levelandroidsdk.ble.model.DeviceObserverCallbacks;
import com.theshopatvsp.levelandroidsdk.ble.model.DeviceStateMachine;
import com.theshopatvsp.levelandroidsdk.ble.model.GenesisCharacteristic;
import com.theshopatvsp.levelandroidsdk.ble.model.LevelCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.ReporterConfig;
import com.theshopatvsp.levelandroidsdk.ble.model.Step;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.BootloaderPayload;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.BootloaderClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BatteryState;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleDeviceOutput;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.CharacteristicEnum;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.DeviceCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReadWriteEnum;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterError;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReporterType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ServiceEnum;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataScale;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentDataType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.DependentVariableDescription;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.reporter.IndependentVariableDescription;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.state.ConnectionState;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.state.DeviceInteractionState;
import com.theshopatvsp.levelandroidsdk.ble.model.exception.DataLengthException;
import com.theshopatvsp.levelandroidsdk.ble.model.exception.NackAttributeException;
import com.theshopatvsp.levelandroidsdk.ble.model.exception.SequenceIdException;
import com.theshopatvsp.levelandroidsdk.ble.model.response.CodePacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.DataPacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.Frame;
import com.theshopatvsp.levelandroidsdk.ble.model.response.LockPacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.NukeRecordsPacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.RecordData;
import com.theshopatvsp.levelandroidsdk.ble.model.response.ReportAttributesData;
import com.theshopatvsp.levelandroidsdk.ble.model.response.TimePacket;
import com.theshopatvsp.levelandroidsdk.ble.model.response.TransmitControlData;
import com.theshopatvsp.levelandroidsdk.ble.model.strategy.Nack;
import com.theshopatvsp.levelandroidsdk.ble.model.strategy.PacketParserManager;
import com.theshopatvsp.levelandroidsdk.model.LastLocation;
import com.theshopatvsp.levelandroidsdk.model.LevelUser;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by andrco on 6/10/16.
 */
public class BleManager extends Service implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = BleManager.class.getSimpleName();

    public static final String LEVEL_FRAME_ID = "com.theshop.level.frame_id";
    public static final String LEVEL = "level";
    public static final String LEVEL_BLE_PREFERENCES = "com.theshop.level.ble.preferences";
    public static final String LEVEL_BLE_SAVED_DEVICE_ADDRESS = "com.theshop.level.ble.saved_device_address";
    public static final String LEVEL_LED_CODE = "com.theshop.level.ble.led_code_thing";
    public static final String SERVICE_CREATED = "com.theshop.level.ble.service.created.thing";
    final static public UUID CHAR_CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final String LEVEL_USER_FILE = "com.theshopatvsp.ble.level.user.file";
    public static final String LEVEL_PAYLOAD = "com.theshopatvsp.ble.level.payload";
    public static final String LEVEL_BOOTLOADER_FILE = "com.theshopatvsp.ble.level.bootloader";

    private BluetoothAdapter adapter;
    private BluetoothDevice connectedDevice;
    private String savedDeviceAddress = null;
    private BluetoothGatt gatt;

    private LevelUser user;
    private CalculationHelper calculationHelper;
    private List<Step> wannabeActiveSteps = new LinkedList<>();
    private Handler activeTimeout = new Handler();

    //connection stuff
    private static boolean appOpen = false;
    private boolean reboot = false, reporterQuery = false;
    private static boolean deviceReady = false;
    private static ConnectionState connectionState;
    private int closestAttempts = 0;
    private int failedAttempts = 0;
    private String frameId;
    private Map<String, BleDeviceRecord> foundDevices;
    private Handler leScanTimeoutHandler = new Handler();

    private static List<ServiceEnum> services = new ArrayList<>();
    private static Set<UUID> validNotifyCharacteristics = new HashSet<>();
    private BootloaderPayload payload;
    private Frame frame;
    private int globalReportControl[] = {0, 0, 0};
    private boolean setReporter = false;

    static {
        services.add(ServiceEnum.UART);
        services.add(ServiceEnum.BATTERY);

        validNotifyCharacteristics.add(CharacteristicEnum.BATTERY_LEVEL.getUuid());
        validNotifyCharacteristics.add(CharacteristicEnum.BATTERY_STATE.getUuid());
        validNotifyCharacteristics.add(CharacteristicEnum.UART_RX.getUuid());
    }

    private Map<CharacteristicEnum, GenesisCharacteristic> characteristicMap;
    private Stack<BluetoothGattCharacteristic> notifyStack = new Stack<>();
    private boolean notifying = false;
    private Stack<LevelCommand> waitingCommands = new Stack<>();

    //device comm stuff
    private DeviceIdManager deviceId;
    private Queue<LevelCommand> commandQueue = new LinkedBlockingQueue<>(100);
    private LevelCommand sentCommand;
    private boolean ackSent = false;
    private boolean runCommandThread = false;
    private DeviceStateMachine deviceStateMachine;
    private boolean userIsActive = false;
    private int inactiveCount = 0;
    private ObjectMapper mapper = new ObjectMapper();

    private static Map<UUID, DeviceObserverCallbacks> observers = new HashMap<>();
    private static Queue<ClientCommand> clientCommands = new LinkedBlockingQueue<>(100);
    private ReportAttributesData blank = new ReportAttributesData(ReporterType.Steps.getReporter(), IndependentVariableDescription.UNITLESS, 0, DependentVariableDescription.UNITLESS,
            DependentDataType.CHAR, DependentDataScale.ONE_TO_ONE_BIT, 0, 1, 1);
    private boolean disableSteps = false;

    @Override
    public void onCreate() {
        super.onCreate();

        connectionState = ConnectionState.Init;
        deviceId = new DeviceIdManager();
        getApplication().registerActivityLifecycleCallbacks(this);

        Log.v(TAG, "registering for intents");
        deviceStateMachine = new DeviceStateMachine();
        registerReceiver(receiver, makeGattUpdateIntentFilter());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        startClientCommandThread();

        SharedPreferences preferences = getSharedPreferences(LEVEL_BLE_PREFERENCES, MODE_PRIVATE);
        String create = preferences.getString(SERVICE_CREATED, null);

        /*if (create == null) {
            Log.v(TAG, "CREATED IS NOT SET wiwiwiwiwiwiwiwiw");
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(SERVICE_CREATED, "CREATED");
            editor.commit();
            appOpen = true;
        }*/
        appOpen = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getApplication().unregisterActivityLifecycleCallbacks(this);
        unregisterReceiver(receiver);
        stopCommandThread();
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.v(TAG, "event received in service " + appOpen + " ?? " + intent.getPackage());

        int permission = getApplication().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                getApplication().getPackageName());
        if (permission == PackageManager.PERMISSION_GRANTED) {
            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_MIN, TEN_METERS, locationListener);
        }

        if (!appOpen) {
            if (user == null) {
                Log.v(TAG, "starting background, user is not set");
                String userString = FileHelper.read(getApplicationContext(), LEVEL_USER_FILE);

                if (userString != null) {
                    try {
                        Log.v(TAG, "reading from file to set user");
                        user = mapper.readValue(userString, LevelUser.class);
                        Log.v(TAG, "user is " + (user == null ? "" : " not ") + " null");
                        calculationHelper = new CalculationHelper(user, TimeZone.getDefault().getID());
                    } catch (IOException e) {
                        Log.e(TAG, "Json read error: ", e);
                    }
                }
            }

            startScan();
        }

        return Service.START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static Object clientLock = new Object();
    private static Object clientCommandLock = new Object();

    public static void registerObserver(UUID clientId, DeviceObserverCallbacks callbacks) {
        Log.v(TAG, "registering client: " + clientId);
        synchronized (clientLock) {
            observers.put(clientId, callbacks);
        }
    }

    public static void unregisterObserver(UUID clientId) {
        Log.v(TAG, "UNregistering client: " + clientId);
        synchronized (clientLock) {
            Map<UUID, DeviceObserverCallbacks> temp = new HashMap<>(observers);
            temp.remove(clientId);

            observers = new HashMap<>(temp);
        }
    }

    public static void addClientCommand(ClientCommand command) {
        if( appOpen ) {
            Log.v(TAG, "adding client command: " + command.getCommand());
            clientCommands.add(command);
        }
    }

    public static boolean isConnected() {
        return ConnectionState.Connected == connectionState;
    }


    public static boolean isDeviceReady() {
        return deviceReady;
    }

    private void initBluetooth() {
        if (!initBt()) {
            broadcastUpdate(BleDeviceOutput.BluetoothNotAvailable);
        }

        if (!isBleAvailable()) {
            broadcastUpdate(BleDeviceOutput.BluetoothNotAvailable);
        }

        if (!isBtEnabled()) {
            broadcastUpdate(BleDeviceOutput.BluetoothNotOn);
        }
    }

    private boolean initBt() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager != null) adapter = btManager.getAdapter();

        return (btManager != null) && (adapter != null);

    }

    private boolean isBleAvailable() {

        Log.i(TAG, "Checking if BLE hardware is available");

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && adapter != null) {
            Log.i(TAG, "BLE hardware available");
        } else {
            Log.i(TAG, "BLE hardware is missing!");
            return false;
        }

        return true;
    }

    private boolean isBtEnabled() {

        Log.i(TAG, "Checking if BT is enabled");
        if (adapter != null && adapter.isEnabled()) {
            Log.v(TAG, "BT is enabled");
        } else {
            Log.i(TAG, "BT is disabled. Use Setting to enable it and then come back to this app");
            return false;
        }
        return true;
    }

    private synchronized void startScan() {
        Log.v(TAG, "start Scan " + connectionState);
        if (connectionState == ConnectionState.Init || connectionState == ConnectionState.Disconnected) {
            connectionState = ConnectionState.Scan;
            initBluetooth();
            characteristicMap = new HashMap<>();

            //are we bonded?
            savedDeviceAddress = getSavedDeviceAddress();
            BluetoothDevice temp = null;

            try { //null pointer exception from the if statement which should be impossible, but is happening, surrounding with try/catch to start rescan
                if (adapter != null && adapter.getBondedDevices() != null && !adapter.getBondedDevices().isEmpty()) {
                    for (BluetoothDevice device : adapter.getBondedDevices()) {
                        if (device != null && device.getAddress().equalsIgnoreCase(savedDeviceAddress)) {
                            temp = device;
                            break;
                        } else if (device != null && device.getName().toLowerCase().startsWith("level")) {
                            temp = device;
                            break;
                        }
                    }

                    if (temp != null) {
                        connectionState = ConnectionState.DeviceFound;
                        connectToDevice(temp);
                    }
                }

                Log.v(TAG, "START SCAN IS APP OPEN " + appOpen);

                if (!appOpen) { //somehow the app isn't open and we don't have a bonded device, just give up
                    connectionState = ConnectionState.Init;
                    return;
                }

                //not bonded or savedAddress not found in bonded devices
                if (savedDeviceAddress == null || temp == null) {
                    savedDeviceAddress = null;

                    if (adapter != null) {
                        closestAttempts = 0;
                        foundDevices = new HashMap<>();
                        adapter.startLeScan(scanCallback);

                        Runnable scanTimeout = new Runnable() {
                            @Override
                            public void run() {
                                if (!foundDevices.isEmpty()) {
                                    connectionState = ConnectionState.DeviceFound;
                                    List<BleDeviceRecord> devices = new LinkedList<>(foundDevices.values());
                                    Collections.sort(devices);
                                    Log.v(TAG, "closestAttempts = " + closestAttempts + " - " + foundDevices.size());
                                    connectToDevice(devices.get(closestAttempts++).getDevice());
                                    Log.v(TAG, "after closestAttempts = " + closestAttempts);
                                } else {
                                    connectionState = ConnectionState.NoDevicesFound;
                                    stopScan();
                                }
                            }
                        };

                        leScanTimeoutHandler.postDelayed(scanTimeout, 10000);
                    }
                }
            } catch (Exception e) {
                connectionState = ConnectionState.Disconnected;
                startScan();
            }
        }
    }

    private void incDevice() {
        disconnectFromDevice();

        if (foundDevices != null) {
            List<BleDeviceRecord> devices = new LinkedList<>(foundDevices.values());
            Collections.sort(devices);

            Log.v(TAG, "incDevice: closestAttempts = " + closestAttempts);

            if (!devices.isEmpty() && closestAttempts < devices.size()) {
                connectionState = ConnectionState.DeviceFound;
                connectToDevice(devices.get(closestAttempts++).getDevice());
            }
        }
    }

    private void stopScan() {
        if (adapter != null) {
            adapter.stopLeScan(scanCallback);
            Log.i(TAG, "Searching for devices with Genesis service stopped");
        }

        if (leScanTimeoutHandler != null) {
            leScanTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    private synchronized void connectToDevice(BluetoothDevice device) {
        if (connectionState == ConnectionState.DeviceFound) {
            connectionState = ConnectionState.ConnectToDevice;
            stopScan();
            deviceStateMachine.reset();

            connectedDevice = device;
            startCommandThread();

            Log.i(TAG, "Connecting to the device NAME: " + device.getName() + " HWADDR: " + device.getAddress());
            if (Build.MANUFACTURER.equalsIgnoreCase("samsung") || Build.MANUFACTURER.contains("samsung")) {

                Log.v(TAG, "Connecting Bluetooth for Samsung");
                new Handler(getApplication().getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            gatt = connectedDevice.connectGatt(getApplicationContext(), false, gattCallback);
                        } catch (Exception e) {
                            disconnectFromDevice();
                        }
                    }
                });
            } else {
                gatt = device.connectGatt(getApplicationContext(), false, gattCallback);
            }
        }

        //TODO do we need a timeout here to help connections?
    }

    private void disconnectFromDevice() {
        Log.v(TAG, "Disconnecting from device: ");

        if (characteristicMap != null && !characteristicMap.isEmpty()) {
            for (Map.Entry<CharacteristicEnum, GenesisCharacteristic> entry : this.characteristicMap.entrySet()) {
                if (entry.getValue().getNotifyCharacteristic() != null) {
                    disableNotification(entry.getValue().getNotifyCharacteristic());
                }
            }
        }

        this.characteristicMap = new HashMap<>();

        resetBluetoothState();

        deviceReady = false;
        notifying = false;
        commandQueue = new LinkedBlockingQueue<>(100);
        waitingCommands = new Stack<>();
        sentCommand = null;
        deviceId.reset();

        stopCommandThread();
        connectionState = ConnectionState.Disconnected;
        broadcastUpdate(BleDeviceOutput.Disconnected);
        deviceStateMachine.reset();
        String glassName = (frame != null ? frame.getModel().toString() + frame.getColor().toString() : "" );

        Log.v(TAG, "disconnect but " + appOpen + " state = " + deviceStateMachine.getState());
        if (appOpen && !reboot) {
            startScan();
            if (currentLocation != null) {
                broadcastUpdate(BleDeviceOutput.LastUserLocation, new LastLocation(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), currentLocation.getAccuracy(), currentLocation.getAltitude(), new Date().getTime(), glassName));
            }
        } else if (reboot) {
            reboot = false;
        } else if (currentLocation != null) {
            int permission = getApplication().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    getApplication().getPackageName());
            if (permission == PackageManager.PERMISSION_GRANTED)
                locationManager.removeUpdates(locationListener);
            Log.v(TAG, "LLFJDKFJDLSFJDSKLFJ LOCATION UPDATED!!!!");

            try {
                mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
                FileHelper.writeToFile(getApplicationContext(), FileHelper.LAST_USER_LOCATION_FILE, mapper.writeValueAsString(new LastLocation(currentLocation.getLatitude(),
                        currentLocation.getLongitude(), currentLocation.getAccuracy(), currentLocation.getAltitude(), new Date().getTime(), glassName )), false);
            } catch (JsonProcessingException e) {
                Log.e(TAG, "error converting location", e);
            }
        }
    }

    private void resetBluetoothState() {
        Log.v(TAG, "Resetting Bluetooth State...");

        stopScan();

        adapter = null;
        connectedDevice = null;

        closeGatt();
    }

    private void closeGatt() {
        if (gatt != null) {
            try {
                Log.v(TAG, "DISCONNECTING GATT*()*(");
                gatt.disconnect();
                if (gatt != null) {
                    Log.v(TAG, "CLOSING GATT*(*@(#(@$&");
                    gatt.close();
                }

                gatt = null;
            } catch (Exception e) {
                Log.v(TAG, "btGatt Close: Exception...");
            }
        }
    }

    private void saveDeviceAddress() {
        if (connectedDevice != null) {
            SharedPreferences preferences = getSharedPreferences(LEVEL_BLE_PREFERENCES, MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();

            editor.putString(LEVEL_BLE_SAVED_DEVICE_ADDRESS, connectedDevice.getAddress());
            editor.commit();
        }
    }

    private String getSavedDeviceAddress() {
        SharedPreferences preferences = getSharedPreferences(LEVEL_BLE_PREFERENCES, MODE_PRIVATE);

        return preferences.getString(LEVEL_BLE_SAVED_DEVICE_ADDRESS, null);
    }

    private BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {

            if (device.getName() != null && device.getName().toLowerCase().startsWith(LEVEL)) {
                if (frameId != null & device != null && device.getName() != null && device.getName().toLowerCase().endsWith(frameId.toLowerCase())) {
                    Log.v(TAG, "Device Found: " + device.getAddress());
                    foundDevices.clear();
                    connectionState = ConnectionState.DeviceFound;
                    connectToDevice(device);
                } else {
                    foundDevices.put(device.getAddress(), new BleDeviceRecord(device, rssi));
                }
            }
        }
    };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public final String GTAG = BluetoothGattCallback.class.getSimpleName();

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(GTAG, "onConnectionStateChange " + newState + " - " + status);
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(GTAG, "onConnectionStateChange - connected");
                connectionState = ConnectionState.DiscoverServices;
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(GTAG, "onConnectionStateChange - disconnected");
                connectionState = ConnectionState.Disconnected;
                disconnectFromDevice();
            } else if (newState == BluetoothProfile.STATE_CONNECTED && status != BluetoothGatt.GATT_SUCCESS) {
                Log.i(GTAG, "onConnectionStateChange - uh connected but not");
                connectionState = ConnectionState.Disconnected;
                disconnectFromDevice();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(GTAG, "Services Discovered!!");

                connectionState = ConnectionState.Bond;
                createBond();
                //getServices();
            } else {
                Log.i(GTAG, "Unable to discover services.");
                disconnectFromDevice();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v(TAG, "BLEService.gattCallback.onCharacteristicRead() *******************************************");
            sentCommand = null;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte val[] = characteristic.getValue();

                Log.v(TAG, "READ = " + Arrays.toString(val));
                CharacteristicEnum charac = CharacteristicEnum.getByUuid(characteristic.getUuid());

                if (charac != null) {
                    switch (charac) {
                        case BATTERY_LEVEL:
                            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                                int batteryLevel = characteristic.getValue()[0];

                                broadcastUpdate(BleDeviceOutput.BatteryLevel, batteryLevel);
                            }
                            break;
                        case BATTERY_STATE:
                            if (characteristic.getValue() != null && characteristic.getValue().length > 0) {
                                int batteryState = characteristic.getValue()[0];

                                broadcastUpdate(BleDeviceOutput.BatteryState, BatteryState.getByState(batteryState));
                            }
                            break;
                        case FIRWARE_VERSION:
                            String version = "";

                            for (byte v : val) {
                                char c = (char) v;

                                version += c;
                            }

                            broadcastUpdate(BleDeviceOutput.FirmwareVersion, version);

                            //TODO send out software version
                            //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.SoftwareVersion, version));
                            break;
                        case SOFTWARE_VERSION:
                            String softVersion = "";

                            for (byte v : val) {
                                char c = (char) v;

                                softVersion += c;
                            }

                            String vs[] = softVersion.split("-");

                            broadcastUpdate(BleDeviceOutput.BootloaderVersion, vs[vs.length-1]);
                            break;
                        default:
                            Log.v(TAG, "Characteristic NOT FOUND");
                            break;

                    }
                }
            } else if (BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION == status ||
                    BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION == status || status == 0x89) {
                Log.v(TAG, "onReadCharacteristic status = " + status);
                //final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                //getApplication().registerReceiver(bondingReceiver, filter);
            } else {
                Log.v(GTAG, "read failed!");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.v(TAG, "BLEService.gattCallback.onCharacteristicWrite() *******************************************");
            if (!characteristic.getUuid().equals(CharacteristicEnum.UART_TX) || ackSent) {
                sentCommand = null;

                if (ackSent) {
                    ackSent = false;
                }
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Write Success: " + characteristic.getUuid());
                if (reboot) {
                    disconnectHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            disconnectFromDevice();
                            Log.v(TAG, "sending StartRealBootloader");
                            Intent intent = new Intent(BootloaderClientCommand.StartRealBootloader.name());
                            intent.putExtra(BootloaderService.BOOTLOADER_PAYLOAD_THING, payload);
                            sendBroadcast(intent);
                        }
                    }, 2000);
                }
            } else {
                Log.i(TAG, "Write Failure: " + characteristic.getUuid());
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.v(TAG, "BLEService.gattCallback.onCharacteristicChanged() *******************************************");
            if (characteristic.getUuid().equals(CharacteristicEnum.UART_RX))
                sentCommand = null;

            CharacteristicEnum charac = CharacteristicEnum.getByUuid(characteristic.getUuid());

            if (charac != null) {
                Log.v(TAG, "Data received char: " + charac.getName() + " data: " + Arrays.toString(characteristic.getValue()));

                if (charac == CharacteristicEnum.UART_RX) {
                    handleUartResponse(characteristic.getValue());
                } else if (charac == CharacteristicEnum.BATTERY_LEVEL) {
                    int batteryLevel = characteristic.getValue()[0];

                    broadcastUpdate(BleDeviceOutput.BatteryLevel, batteryLevel);
                } else if (charac == CharacteristicEnum.BATTERY_STATE) {
                    int batteryState = characteristic.getValue()[0];

                    broadcastUpdate(BleDeviceOutput.BatteryState, BatteryState.getByState(batteryState));
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.v(TAG, "onDescriptorWrite..............." + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (!notifyStack.isEmpty()) {
                    notifying = false;
                    enableNotification(notifyStack.pop());
                } else {
                    //bond stuff was here
                    connectionState = ConnectionState.SendKey;
                    executeCommand(DeviceCommand.CODEWR, new CodePacket(0x88));
                }
            } else {
                Log.v(TAG, "onDescriptorWRite FAILED!!!*!(@*!(@!(*@!(");
            }
        }
    };

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private synchronized void handleUartResponse(byte packet[]) {
        if (deviceId.getPacketIdIn() < 0 && packet != null && packet.length > 0) {
            deviceId.setPacketIdIn(0x00 << 24 | packet[0] & 0xff);
        } else if (packet == null || packet.length < 2) {
            sendNack((byte) Nack.NackError.DATA_LENGTH_ERROR.getId());
        } else {
            deviceId.incPacketIdIn();
        }

        DataPacket data;

        try {
            data = PacketParserManager.parse(deviceId.getPacketIdIn(), packet);
        } catch (DataLengthException dle) {
            sendNack((byte) Nack.NackError.DATA_LENGTH_ERROR.getId());
            return;
        } catch (SequenceIdException sie) {
            sendNack((byte) Nack.NackError.PACKET_SEQ_ERROR.getId());
            return;
        } catch( NackAttributeException nae ) {
            ReporterError error = null;
            if( packet.length >= 5 ) {
                error = ReporterError.getByErrorCode(packet[4]);
            }

            broadcastUpdate(BleDeviceOutput.ReporterSetupFailed, error);
            return;
        } catch (Exception e) {
            //disconnectFromDevice(false);
            throw new RuntimeException(e);
        }

        if (data instanceof RecordData) {
            Log.v(TAG, data.toString());
            sendAck();

            //deviceStateMachine.incPacketToDownload();

            RecordData record = (RecordData) data;
            if (!deviceStateMachine.isTimeIsCorrect() && !withInThreeWeeks(record.getTimestamp())) {
                record.setTimestamp(record.getTimestamp() + deviceStateMachine.getTimeDiff());
            }

            broadcastUpdate(BleDeviceOutput.Data, record);
            return;
        }

        if (connectionState == ConnectionState.SendKey && data instanceof CodePacket) {
            connected();

            if (deviceStateMachine.getState().getCommand() != null) {
                executeCommand(deviceStateMachine.getState().getCommand(), deviceStateMachine.getState().getPacket(0));
                return;
            }
        } else {
            if (deviceStateMachine.getState() == DeviceInteractionState.QueryLock) {
                LockPacket lock = (LockPacket) data;
                if (lock.getLock() == 0) {
                    broadcastUpdate(BleDeviceOutput.InputLedCode);
                } else {
                    broadcastUpdate(BleDeviceOutput.LedCodeNotNeeded);
                }
            } else if (deviceStateMachine.getState() == DeviceInteractionState.SendLedCode4) {
                disconnectHandler.removeCallbacks(disconnectRunnable);
                broadcastUpdate(BleDeviceOutput.LedCodeDone);
                Log.v(TAG, "sending LED code 44444");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "jfdkfjds", e);
                }
            }

            if (deviceStateMachine.getState() != DeviceInteractionState.Done) {
                DataPacket next = deviceStateMachine.processResult(data);

                if (deviceStateMachine.getState().getCommand() != null) {
                    if (deviceStateMachine.getState().getCommand() == DeviceCommand.TIMEWR) {
                        executeCommand(deviceStateMachine.getState().getCommand(), new TimePacket(new Date().getTime()));
                    } else {
                        executeCommand(deviceStateMachine.getState().getCommand(), next);
                    }
                }

                if (deviceStateMachine.getState() == DeviceInteractionState.Done) {
                    Log.v(TAG, "sending out device ready");
                    if (appOpen) {
                        Log.v(TAG, "APP IS OPENS");
                        setStuffUp();
                        broadcastUpdate(BleDeviceOutput.DeviceReady);
                        deviceReady = true;
                    } else {
                        Log.v(TAG, "APP IS CLOSED!!!");
                        //wait a sec in case an ack is in flight
                        disconnectHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                disconnectFromDevice();
                            }
                        }, 1000);
                    }
                }

                return;
            }

            if (data instanceof CodePacket) {
                broadcastUpdate(BleDeviceOutput.LedCodeAccepted);
            } else if (data instanceof ReportAttributesData) {
                if (disableSteps) {
                    disableSteps = false;
                    return;
                }

                ReportAttributesData attrs = (ReportAttributesData) data;
                ReporterType type = ReporterType.getByReporter(data.getReporter());

                switch (type) {
                    case Steps:
                        globalReportControl[0] = 1;
                        break;
                    case Accel:
                        globalReportControl[2] = 1;
                        break;
                    case Gyro:
                        globalReportControl[1] = 1;
                        break;
                }

                if( !reporterQuery ) {
                    executeCommand(DeviceCommand.REPORT_CONTROL, new DataPacket(convertToNumber(globalReportControl)));
                }
                else {
                    reporterQuery = false;

                    broadcastUpdate(BleDeviceOutput.ReporterQueried, new ReporterConfig.Builder().attrs(attrs).build());
                }
            } else if (data instanceof TransmitControlData) {
                broadcastUpdate(BleDeviceOutput.TransmitControl, ((TransmitControlData)data).getTotalRecordCount());
            } else if (data instanceof NukeRecordsPacket) {
                broadcastUpdate(BleDeviceOutput.DataNuked);
            } else if (data instanceof DataPacket) {
                if( setReporter ) {
                    broadcastUpdate(BleDeviceOutput.ReporterSetupSuccess);
                    setReporter = false;
                }
                int reportControl = data.getReportControl();
                Set<ReporterType> types = new HashSet<>();

                if (disableSteps) {
                    executeCommand(DeviceCommand.REPORT_ATTRIBUTES, blank);
                }

                for (ReporterType t : ReporterType.values()) {
                    if ((reportControl & t.getReportControlBit()) > 0) {
                        types.add(t);

                        switch (t) {
                            case Steps:
                                globalReportControl[0] = 1;
                                break;
                            case Accel:
                                globalReportControl[2] = 1;
                                break;
                            case Gyro:
                                globalReportControl[1] = 1;
                                break;
                        }
                    }
                }

                broadcastUpdate(BleDeviceOutput.ReporterChanged, (Serializable) types);
            }
        }


        //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.Data, data));
    }

    private void setStuffUp() {
        if( deviceStateMachine.isReporter0IsOn() ) {
            globalReportControl[0] = 1;
        }

        if( deviceStateMachine.isReporter1IsOn() ) {
            globalReportControl[1] = 1;
        }

        if( deviceStateMachine.isReporter2IsOn() ) {
            globalReportControl[2] = 1;
        }
    }

    private Runnable activeTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (!wannabeActiveSteps.isEmpty()) {
                for (Step step : wannabeActiveSteps) {
                    broadcastUpdate(BleDeviceOutput.Step, step);
                }

                wannabeActiveSteps.clear();
            }
        }
    };

    private static final long threeWeeks = 1000 * 60 * 60 * 24 * 7 * 3;

    private boolean withInThreeWeeks(long timestamp) {
        return Math.abs(new Date().getTime() - timestamp) <= threeWeeks;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createBond() {
        if (savedDeviceAddress == null || !connectedDevice.getAddress().equalsIgnoreCase(savedDeviceAddress)) {
            savedDeviceAddress = connectedDevice.getAddress();
            saveDeviceAddress();
            connectionState = ConnectionState.Bond;

            Log.v(TAG, "creating bond dammit!");

            final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            getApplication().registerReceiver(bondingReceiver, filter);
            this.connectedDevice.createBond();
        } else {
            deviceStateMachine.isBonded();
            //connectionState = ConnectionState.SendKey;
            //executeCommand(DeviceCommand.CODEWR, new CodePacket(0x88));
            getServices();
        }
    }

    private void connected() {
        connectionState = ConnectionState.Connected;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Thread sleep died!", e);
        }

        if (!waitingCommands.isEmpty()) {
            while (!waitingCommands.isEmpty()) {
                addCommand(waitingCommands.pop());
            }
        }

        Log.v(TAG, "connected state = " + deviceStateMachine.getState());
        if (deviceStateMachine.getState() == DeviceInteractionState.SendLedCode1) {
            broadcastUpdate(BleDeviceOutput.InputLedCode);
        }
    }

    private BroadcastReceiver bondingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final BluetoothDevice localDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
            final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

            if (connectedDevice != null) {
                Log.d(TAG, "Bond state changed for: " + connectedDevice.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

                // skip other devices
                if (localDevice != null && !localDevice.getAddress().equals(connectedDevice.getAddress()))
                    return;
            }

            if (bondState == BluetoothDevice.BOND_BONDED) {
                // Continue to do what you've started before
                Log.v(TAG, "bonded dummy do something: " + connectionState);
                if (connectionState == ConnectionState.Bond) {
                    //connectionState = ConnectionState.SendKey;
                    //executeCommand(DeviceCommand.CODEWR, new CodePacket(0x88));
                    connectionState = ConnectionState.GetCharacteristics;
                    getServices();
                }

                getApplication().unregisterReceiver(this);
                //mCallbacks.onBonded();
            }
        }
    };

    private void sendNack(byte reasonCode) {
        byte[] packet = {
                (byte) deviceId.getPacketIdOut(),
                (byte) DeviceCommand.NACK.getCommand(),
                reasonCode,
                (byte) deviceId.getPacketIdIn()
        };

        deviceId.incPacketIdOut();
        addCommand(new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.UART_TX, packet));
    }

    private void sendAck() {
        Log.v(TAG, "sending ACK for record");
        byte[] packet = {
                (byte) deviceId.getPacketIdOut(),
                (byte) DeviceCommand.ACK.getCommand(),
                (byte) deviceId.getPacketIdIn()
        };

        deviceId.incPacketIdOut();
        addCommand(new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.UART_TX, packet));
    }

    private void getServices() {
        Log.v(TAG, "getServices called");
        for (ServiceEnum service : services) {
            BluetoothGattService s = gatt.getService(service.getUuid());
            if (s != null) {
                for (CharacteristicEnum chars : CharacteristicEnum.getByParentUUID(service.getUuid())) {
                    characteristicMap.put(chars, getCharacteristic(s, chars.getUuid()));
                }
            }
        }

        BluetoothGattService deviceInfo = gatt.getService(ServiceEnum.DEVICE_INFO.getUuid());

        if (deviceInfo != null) {
            characteristicMap.put(CharacteristicEnum.FIRWARE_VERSION, getCharacteristic(deviceInfo,
                    CharacteristicEnum.FIRWARE_VERSION.getUuid()));

            characteristicMap.put(CharacteristicEnum.SOFTWARE_VERSION, getCharacteristic(deviceInfo,
                    CharacteristicEnum.SOFTWARE_VERSION.getUuid()));
        }
    }

    private GenesisCharacteristic getCharacteristic(BluetoothGattService service, UUID characteristicUuid) {
        Log.v(TAG, "getCharacteristic " + (service != null ? service.getUuid().toString() : "service is null ") + " - " + characteristicUuid);
        GenesisCharacteristic charac = new GenesisCharacteristic(CharacteristicEnum.getByUuid(characteristicUuid));

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        Log.i(TAG, "characteristic: " + characteristicUuid.toString() + " " + (characteristic == null ? "null" : "found"));

        if (characteristic != null) {
            // Clear any pending notifications *********
            final int charaProp = characteristic.getProperties();

            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                Log.v(TAG, "found read char");
                charac.setReadCharacteristic(characteristic);
            } else {
                charac.setReadCharacteristic(null);
            }

            // Obtain the Write Characteristic for sending commands ***********
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 || (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                Log.v(TAG, "found write char");
                charac.setWriteCharacteristic(characteristic);
            } else {
                charac.setWriteCharacteristic(null);
            }

            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) { //&& (charac.getCharacteristic() == CharacteristicEnum.UART_RX)) {
                Log.v(TAG, "found notification char");
                // Clear out the old one...
                if (characteristicMap.get(CharacteristicEnum.getByUuid(characteristicUuid)) != null &&
                        characteristicMap.get(CharacteristicEnum.getByUuid(characteristicUuid)).getNotifyCharacteristic() != null) {
                    disableNotification(characteristic);
                }

                enableNotification(characteristic);
                charac.setNotifyCharacteristic(characteristic);
            } else {
                charac.setNotifyCharacteristic(null);
            }
        }

        return charac;
    }

    //Note if there are multiple chars that have notify, need to implement a stack
    //if you subscribe to all chars at once, without waiting for a response, you will only subscribe to one!
    //currently there is one char with notify, so don't need it
    private void enableNotification(BluetoothGattCharacteristic gattCharacteristic) {
        Log.d(TAG, "BLEService.enableNotificationForGenesis() ++++++++++++++++++++++++++++++++++++");
        if (!validNotifyCharacteristics.contains(gattCharacteristic.getUuid())) {
            Log.v(TAG, "Not UART_RX or Battery Level, not enabling notifications");
            return;
        } else if (!appOpen && (gattCharacteristic.getUuid().equals(CharacteristicEnum.BATTERY_LEVEL) ||
                gattCharacteristic.getUuid().equals(CharacteristicEnum.BATTERY_STATE))) {
            Log.v(TAG, "Battery Level but app not open, not enabling notifications");
            return;
        }
        Log.i(TAG, "Enabling notification for Genesis " + gattCharacteristic.getUuid());

        if (notifying) {
            Log.v(TAG, "PUSHING ONTO STACK!");
            notifyStack.push(gattCharacteristic);
            return;
        } else {
            Log.v(TAG, "NOTIFYING NOW!!I!@");
            notifying = true;
        }

        boolean success = gatt.setCharacteristicNotification(gattCharacteristic, true);

        if (!success) {
            Log.i(TAG, "Enabling notification failed!");
            return;
        }

        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG);

        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

            Log.i(TAG, "Notification enabled");
        } else {
            Log.i(TAG, "Could not get descriptor for characteristic! Notification are not enabled.");
        }
    }

    private void disableNotification(BluetoothGattCharacteristic gattCharacteristic) {
        Log.d(TAG, "BLEService.disableNotificationForGenesis() ++++++++++++++++++++++++++++++++++++");
        try {
            if ((gatt != null) && (gattCharacteristic != null)) {

                Log.i(TAG, "Disabling notification for Genesis ");

                boolean success = gatt.setCharacteristicNotification(gattCharacteristic, false);

                if (!success) {
                    Log.i(TAG, "Disabling notification failed!");
                    return;
                }

                BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(CHAR_CLIENT_CONFIG);

                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    Log.i(TAG, "Notification disabled");
                } else {
                    Log.i(TAG, "Could not get descriptor for characteristic! Disabling notifications may have failed.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception disconnecting ignore", e);
            resetBluetoothState();
        }
    }

    private static final ReentrantReadWriteLock broadCastUpdateLock = new ReentrantReadWriteLock();

    //broadcast stuff
    private void broadcastUpdate(BleDeviceOutput output) {

        Log.v(TAG, "broadcastUpdate: " + output);
        if (appOpen) {
            switch (output) {
                case BluetoothNotAvailable:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onBluetoothNotAvailable();
                        }
                    }
                    break;
                case BluetoothNotOn:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onBluetoothNotOn();
                        }
                    }
                    break;
                case InputLedCode:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onInputLedCode();
                        }
                    }
                    break;
                case LedCodeAccepted:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onLedCodeAccepted();
                        }
                    }
                    break;
                case LedCodeFailed:
                    if (!observers.isEmpty()) {

                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onLedCodeFailed();
                        }
                    }
                    break;
                case LedCodeDone:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onLedCodeDone();
                        }
                    }
                    break;
                case LedCodeNotNeeded:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onLedCodeNotNeeded();
                        }
                    }
                    break;
                case Disconnected:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onDisconnect();
                        }
                    }
                    break;
                case ReporterSetupSuccess:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onSetupSuccess();
                        }
                    }
                    break;
                case DeviceReady:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onDeviceReady();
                        }
                    }
                    break;
                case DataNuked:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onDataDeleted();
                        }
                    }
                    break;
                default:
                    Log.v(TAG, "broadcast Update unknow " + output);
            }
        }
    }

    private void broadcastUpdate(BleDeviceOutput output, Serializable thing) {
            Log.v(TAG, "sending thing out to the app: " + thing.getClass().getSimpleName());
            switch (output) {
                case Data:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onData((RecordData) thing);
                        }
                    }
                    break;
                case ReporterChanged:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onReportersEnabled((HashSet<ReporterType>) thing);
                        }
                    }
                    break;
                case ReporterQueried:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onReporterQueried((ReporterConfig) thing);
                        }
                    }
                    break;
                case BatteryLevel:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onBatteryLevel((Integer) thing);
                        }
                    }
                    break;
                case BatteryState:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onBatteryState((BatteryState) thing);
                        }
                    }
                    break;
                case ReporterSetupFailed:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onSetupFailed((ReporterError)thing);
                        }
                    }
                    break;
                case TransmitControl:
                    if (!observers.isEmpty()) {
                        for (DeviceObserverCallbacks callbacks : observers.values()) {
                            callbacks.onDataStreamChanged((Integer)thing);
                        }
                    }
                    break;

            }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onReceive called " + intent.getAction());
            if (intent.getAction().equalsIgnoreCase(BleDeviceOutput.BootloaderFinished.name())) {
                stopService(new Intent(BleManager.this, BootloaderService.class));
                broadcastUpdate(BleDeviceOutput.BootloaderFinished);
                startScan();
            } else if (intent.getAction().equalsIgnoreCase(BleDeviceOutput.BootloaderProgress.name())) {
                broadcastUpdate(BleDeviceOutput.BootloaderProgress, intent.getIntExtra("BOOTLOADER_PROGRESS", 0));
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleDeviceOutput.BootloaderFinished.name());
        intentFilter.addAction(BleDeviceOutput.BootloaderProgress.name());

        return intentFilter;
    }

    //Device writing stuff

    private synchronized void executeCommand(DeviceCommand command, DataPacket packet) {
        if (command != null) {
            byte payloadPacket[] = (packet != null ? packet.getPacket() : null);
            byte bytes[] = new byte[2 + (payloadPacket != null ? payloadPacket.length : 0)];
            bytes[0] = (byte) deviceId.getPacketIdOut();
            bytes[1] = (byte) command.getCommand();

            if (bytes.length > 2) {
                for (int i = 0; i < payloadPacket.length; i++) {
                    bytes[i + 2] = payloadPacket[i];
                }
            }

            deviceId.incPacketIdOut();

            addCommand(new LevelCommand(ReadWriteEnum.WRITE, CharacteristicEnum.UART_TX, bytes));
        }
    }

    private synchronized void addCommand(LevelCommand command) {
        Log.v(TAG, "addCommand called " + command.getReadOrWrite() + " - " + command.getCharacteristic());

        if (connectionState == ConnectionState.GetCharacteristics) {
            waitingCommands.add(command);
        } else {
            commandQueue.add(command);
        }
    }

    private void readCharacteristic(CharacteristicEnum characteristic) {
        if (characteristicMap != null && characteristicMap.get(characteristic) != null) {
            Log.v(TAG, "Reading char: " + characteristic + " - " + (characteristicMap.get(characteristic) == null ? "null" : "found") + " - " +
                    (characteristicMap.get(characteristic).getReadCharacteristic() == null ? "null" : "found"));

            if (characteristicMap.get(characteristic).getReadCharacteristic() != null) {
                Log.v(TAG, "really reading");
                gatt.readCharacteristic(characteristicMap.get(characteristic).getReadCharacteristic());
            }
        } else {
            Log.v(TAG, "characteristicMap is null");
        }
    }

    private void writeCharacteristic(CharacteristicEnum characteristic, byte data[]) {
        Log.v(TAG, "Writing char: " + characteristic + " data: " + Arrays.toString(data));

        if (characteristicMap != null && characteristicMap.containsKey(characteristic) &&
                characteristicMap.get(characteristic).getWriteCharacteristic() != null) {
            BluetoothGattCharacteristic ch = characteristicMap.get(characteristic).getWriteCharacteristic();
            if (adapter == null || gatt == null) return;

            Log.v(TAG, "really writing");

            ch.setValue(data);
            gatt.writeCharacteristic(ch);
        }
    }

    private final Object threadLock = new Object();
    private boolean runClientCommandThread = false;

    private void startCommandThread() {
        if (!runCommandThread) {
            synchronized (threadLock) {
                if (!runCommandThread) {
                    runCommandThread = true;
                    Thread thread = new Thread(new CommandThread());
                    thread.start();
                }
            }
        }
    }

    private void startClientCommandThread() {
        if (!runClientCommandThread) {
            synchronized (threadLock) {
                if (!runClientCommandThread) {
                    runClientCommandThread = true;
                    Thread thread = new Thread(new ClientCommandThread());
                    thread.start();
                }
            }
        }
    }

    private void stopCommandThread() {
        runCommandThread = false;
    }

    private void stopClientCommandThread() {
        runClientCommandThread = false;
    }

    //command thread
    private class CommandThread implements Runnable {
        private static final int MAX_TIMEOUT = 2 * 1000; //2 seconds
        private static final int MAX_RETRIES = 3;
        private long timestamp;
        private int currentRetries;

        private void callCommand(LevelCommand command) {
            if (command == null) {
                Log.v(TAG, "command is null");
                return;
            }

            Log.v(TAG, "thread exec, callCommand called!!!@@!!");
            sentCommand = command;
            timestamp = new Date().getTime();

            if (command.getReadOrWrite() == ReadWriteEnum.READ) {
                Log.v(TAG, "Reading characteristic: " + command.getCharacteristic().getName() + " " + command.getCharacteristic().getUuid());
                readCharacteristic(command.getCharacteristic());
            } else if (command.getReadOrWrite() == ReadWriteEnum.WRITE) {
                try {
                    Log.v(TAG, "writing: " + command.getCharacteristic().getName() + " " + command.getCharacteristic().getUuid() + " - " + (command.getData() != null && command.getData().length > 0 ? Arrays.toString(command.getData()) : " data is null"));
                    Log.v(TAG, "writing raw: " + command.getCharacteristic().getName() + " " + command.getCharacteristic().getUuid() + " - " + new String(command.getData(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                writeCharacteristic(command.getCharacteristic(), command.getData());
            }
        }

        public void run() {
            Log.d(TAG, "CommandThread starting");
            int timer = 0;

            while (runCommandThread) {
                if ((!commandQueue.isEmpty()) && (sentCommand == null)) {
                    Log.d(TAG, "CommandThread.run(): sending command " + commandQueue.size());
                    currentRetries = 0;
                    callCommand(commandQueue.poll());
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Log.w(TAG, "CommandThread.run(): " + ie.getMessage());
                }
            }
        }
    }

    private class ClientCommandThread implements Runnable {
        public void run() {
            Log.d(TAG, "c");
            int timer = 0;

            while (runClientCommandThread) {
                if (!clientCommands.isEmpty()) {
                    Log.v(TAG, "picking up client commands");
                    while (!clientCommands.isEmpty()) {
                        ClientCommand command = clientCommands.poll();

                        switch (command.getCommand()) {
                            case InitiateConnection:
                                Log.v(TAG, "CONNECTIONS");
                                if (connectionState == ConnectionState.Init || connectionState == ConnectionState.Disconnected) {
                                    frameId = (String) command.getThing();
                                    startScan();
                                }
                                break;
                            case DeviceLightsNotOn:
                                Log.v(TAG, "ClientCommand DeviceLightsNotOn");
                                incDevice();
                                break;
                            case SendLedCode:
                                Log.v(TAG, "ClientCommand SendLedCode");
                                if (connectionState == ConnectionState.Connected) {
                                    Log.v(TAG, "Do a thing like send the code dummy");
                                    int code = (Integer) command.getThing();
                                    if (code >= 0) {
                                        executeCommand(DeviceCommand.CODEWR, new CodePacket(code));

                                        if (deviceStateMachine.getState() == DeviceInteractionState.SendLedCode4) {
                                            Log.v(TAG, "sending LEDCODE4!!!!");
                                            setDisconnectTimeout();
                                        }
                                    }
                                }
                                break;
                            case GetBatteryLevel:
                                if (deviceReady) {
                                    addCommand(new LevelCommand(ReadWriteEnum.READ, CharacteristicEnum.BATTERY_LEVEL));
                                }
                                break;
                            case GetBatteryState:
                                if (deviceReady) {
                                    addCommand(new LevelCommand(ReadWriteEnum.READ, CharacteristicEnum.BATTERY_STATE));
                                }
                                break;
                            case QueryReporter:
                                reporterQuery = true;
                                executeCommand(DeviceCommand.REPORT_ATTRIBUTES, new ReportAttributesData((Integer)command.getThing()));
                                break;
                            case QueryReportControl:
                                executeCommand(DeviceCommand.REPORT_CONTROL, null);
                                break;
                            case SetUpReporter:
                                ReporterConfig config = (ReporterConfig) command.getThing();
                                ReportAttributesData data = new ReportAttributesData(config.getType().getReporter(), config.getIndVarDesc(), config.getSamplingHz(),
                                        config.getType().getDepVarDesc(), DependentDataType.INT16, config.getDependentDataScale(), config.getDataFields(),
                                        config.getSamplesPerRecord(), config.getMaxNumberOfRecords());

                                //TODO enable reporter!!!
                                setReporter = true;
                                executeCommand(DeviceCommand.REPORT_ATTRIBUTES, data);
                                break;
                            case DisableReporter:
                            case EnableReporter:
                                ReporterType type = (ReporterType)command.getThing();
                                switch (type) {
                                    case Steps:
                                        if(command.getCommand() == BleClientCommand.EnableReporter) {
                                            globalReportControl[0] = 1;
                                        } else {
                                            disableSteps = true;
                                            globalReportControl[0] = 0;
                                        }
                                        break;
                                    case Accel:
                                        if(command.getCommand() == BleClientCommand.EnableReporter)
                                            globalReportControl[2] = 1;
                                        else
                                            globalReportControl[2] = 0;
                                        break;
                                    case Gyro:
                                        if(command.getCommand() == BleClientCommand.EnableReporter)
                                            globalReportControl[1] = 1;
                                        else
                                            globalReportControl[1] = 0;
                                        break;
                                }

                                executeCommand(DeviceCommand.REPORT_CONTROL, new DataPacket(convertToNumber(globalReportControl)));
                                break;
                            case EnableDataStream:
                                executeCommand(DeviceCommand.TRANSMIT_CONTROL, new TransmitControlData((byte)0x01));
                                break;
                            case DisableDataStream:
                                executeCommand(DeviceCommand.TRANSMIT_CONTROL, new TransmitControlData((byte)0x00));
                                break;
                            case DeleteStoredData:
                                executeCommand(DeviceCommand.NUKE_RECORDS, new NukeRecordsPacket(0));
                                break;
                            default:
                                Log.v(TAG, "Shit oh shit!");
                                break;
                        }
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Log.w(TAG, "CommandThread.run(): " + ie.getMessage());
                }
            }
        }
    }

    private int convertToNumber(int bits[]) {
        int num = 0;

        for( int i = 0; i < bits.length; i++) {
            if (bits[i] > 0) {
                num += Math.pow(2, i);
            }
        }

        return num;
    }

    private Handler disconnectHandler = new Handler();
    private Runnable disconnectRunnable = new Runnable() {
        @Override
        public void run() {
            broadcastUpdate(BleDeviceOutput.LedCodeFailed);
            disconnectFromDevice();
        }
    };

    private void setDisconnectTimeout() {
        disconnectHandler.postDelayed(disconnectRunnable, 1200);
    }

    private static final int FIVE_MIN = 5 * 60 * 1000;
    private static final int TWO_MIN = 2 * 60 * 1000;
    private static final int TEN_METERS = 10;
    private Location currentLocation = null;

    LocationManager locationManager;

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            //makeUseOfNewLocation(location);
            if (isBetterLocation(location, currentLocation)) {
                currentLocation = location;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MIN;
        boolean isSignificantlyOlder = timeDelta < -TWO_MIN;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

// Register the listener with the Location Manager to receive location updates

    private boolean restarted = false;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated " + activity.getClass().getSimpleName());
        reactivate();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.v(TAG, "onActivityStarted " + activity.getClass().getSimpleName());
        reactivate();
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.v(TAG, "onActivityResumed " + activity.getClass().getSimpleName());

        reactivate();
    }

    private synchronized void reactivate() {
        if (!appOpen) {
            int permission = getApplication().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                    getApplication().getPackageName());
            if (permission == PackageManager.PERMISSION_GRANTED) {
                if (currentLocation == null) {
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_MIN, TEN_METERS, locationListener);
            }
            Log.v(TAG, "sssettting appOpen = true");
            appOpen = true;
            reconnectAndCancelAlarm();
        } else {
            restarted = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.v(TAG, "onActivityPaused " + activity.getClass().getSimpleName());
        restarted = false;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG, "Starting async task");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.v(TAG, "LOOOSER");
                }

                if (!restarted) {
                    appOpen = false;
                    Log.v(TAG, "setting appOpen to false");
                    int permission = getApplication().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                            getApplication().getPackageName());
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        locationManager.removeUpdates(locationListener);
                    }
                    disconnectAndSetAlarm();
                }
            }
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.v(TAG, "killing connection because not restarted!");
//                if (!restarted) {
//                    appOpen = false;
//                    int permission = getApplication().getPackageManager().checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
//                            getApplication().getPackageName());
//                    if (permission == PackageManager.PERMISSION_GRANTED) {
//                        locationManager.removeUpdates(locationListener);
//                    }
//                    disconnectAndSetAlarm();
//                }
//            }
//        }, 2000);

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.v(TAG, "onActivityStopped " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.v(TAG, "onActivitySaveInstanceState " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.v(TAG, "onActivityDestroyed " + activity.getClass().getSimpleName());
    }

    private void disconnectAndSetAlarm() {
        disconnectFromDevice();
        stopCommandThread();
        //AlarmHelper.setAlarm(getApplicationContext());
    }

    private void reconnectAndCancelAlarm() {
        //AlarmHelper.cancelAlarm(getApplicationContext());
        startClientCommandThread();
        //startScan();
    }
}
