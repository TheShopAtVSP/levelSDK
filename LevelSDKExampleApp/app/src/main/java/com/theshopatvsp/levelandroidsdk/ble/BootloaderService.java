package com.theshopatvsp.levelandroidsdk.ble;

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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.theshopatvsp.levelandroidsdk.ble.helper.ArchiveInputStream;
import com.theshopatvsp.levelandroidsdk.ble.model.GenesisCharacteristic;
import com.theshopatvsp.levelandroidsdk.ble.model.LevelCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.BootloaderPayload;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.BootloaderStateMachine;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.BootloaderClientCommand;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.BootloaderState;
import com.theshopatvsp.levelandroidsdk.ble.model.bootloader.constants.DFUResponseType;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.BleDeviceOutput;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.CharacteristicEnum;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ReadWriteEnum;
import com.theshopatvsp.levelandroidsdk.ble.model.constants.ServiceEnum;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import no.nordicsemi.android.dfu.DfuSettingsConstants;

/**
 * Created by andrco on 3/3/16.
 */
public class BootloaderService extends Service {
    private static final String TAG = BootloaderService.class.getSimpleName();
    public static final String DEVICE_MAC = "device_mac";
    public static final String DEVICE_NAME = "device_name";
    public static final UUID DFU_SERVICE_UUID = new UUID(0x000015301212EFDEl, 0x1523785FEABCD123l);
    public static final UUID DFU_CONTROL_POINT_CHARACTERISTIC_UUID = new UUID(0x000015311212EFDEl, 0x1523785FEABCD123l);
    public static final UUID DFU_PACKET_CHARACTERISTIC_UUID = new UUID(0x000015321212EFDEl, 0x1523785FEABCD123l);
    public static final UUID DFU_VERSION_CHARACTERISTIC_UUID = new UUID(0x000015341212EFDEl, 0x1523785FEABCD123l);
    public static final String EXTRA_FILE_PATH = "com.theshopatvsp.android.dfu.EXTRA_FILE_PATH";
    public static final String EXTRA_FILE_URI = "com.theshopatvsp.android.dfu.EXTRA_FILE_URI";
    public static final int PACKET_NUM_REQ = 50;
    private static final String DFU_DEVICE_NAME = "DFU";
    public static final int DFU_STATUS_SUCCESS = 1;
    private static final int TYPE_APPLICATION = 0x04, TYPE_AUTO = 0x00;
    public static final String BOOTLOADER_PAYLOAD_THING = "com.theshopatvsp.bootloader.payload.thing";

    private BluetoothAdapter adapter;
    private BluetoothDevice device;
    private String deviceAddress;
    private Handler leScanTimeoutHandler = new Handler();
    private boolean connecting = false, connected = false, notifying = false;
    private BluetoothGatt gatt;
    private boolean started = false;

    private Map<ServiceEnum, BluetoothGattService> serviceMap = null;
    private Map<CharacteristicEnum, GenesisCharacteristic> characteristicMap = new HashMap<>();

    private Stack<BluetoothGattCharacteristic> notifyStack = new Stack<BluetoothGattCharacteristic>();
    private Stack<LevelCommand> waitingCommands = new Stack<LevelCommand>();

    private Queue<LevelCommand> commandQueue = new LinkedBlockingQueue<>(100);
    private LevelCommand sentCommand;
    private boolean runCommandThread = false;
    private int packetIdOut = 0;

    private int fileSize = 0, currentPercentage = 0;

    private BootloaderStateMachine stateMachine = new BootloaderStateMachine();
    private String deviceMac;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Bootloader service created!!!");
        super.onCreate();

        registerReceiver(receiver, makeGattUpdateIntentFilter());
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Bootloader service destroyed!!!");
        super.onDestroy();

        unregisterReceiver(receiver);
        stopCommandThread();
    }

    protected void startBootloader(BootloaderPayload payload) {
        String macs[] = payload.getDeviceMac().split(":");

        int lastAddr = Integer.valueOf(macs[macs.length - 1], 16);
        lastAddr++;
        macs[macs.length - 1] = String.format("%02X", (0xFF & lastAddr));

        deviceMac = TextUtils.join(":", macs);

        final File file = payload.getBootloaderFile();
        String filePath = file.getAbsolutePath();

        ArchiveInputStream is = null;
        InputStream initIs = null;
        int softDeviceImageSize = 0, bootloaderImageSize = 0, appImageSize = 0;

        String mimeType = "application/zip";
        int mbrSize = DfuSettingsConstants.SETTINGS_DEFAULT_MBR_SIZE;
        int fileType = TYPE_AUTO;

        if (filePath.toLowerCase().endsWith("zip")) {
            try {
                is = new ArchiveInputStream(new FileInputStream(filePath), mbrSize, fileType);

                if (is.getContentType() == TYPE_APPLICATION) {
                    if( is.getApplicationInit() != null )
                        initIs = new ByteArrayInputStream(is.getApplicationInit());
                } else if(is.getSystemInit() != null ) {
                    initIs = new ByteArrayInputStream(is.getSystemInit());
                }

                softDeviceImageSize = is.softDeviceImageSize();
                bootloaderImageSize = is.bootloaderImageSize();
                appImageSize = is.applicationImageSize();

                fileSize = softDeviceImageSize + bootloaderImageSize + appImageSize;

                stateMachine.setInitPacket(initIs);
                stateMachine.setImageSizes(softDeviceImageSize, bootloaderImageSize, appImageSize);
                stateMachine.setFirmwareImage(is);
                stateMachine.setContentType(is.getContentType());
                stateMachine.setPacketNum(PACKET_NUM_REQ);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File Not Found: ", e);
                return;
            } catch (IOException e) {
                Log.e(TAG, "IOExcepiton: ", e);
                return;
            }
        }

        startScan(DFU_DEVICE_NAME, deviceMac);
    }

    private void startScan(String deviceName, String deviceMac) {
        Log.v(TAG, "Staring scan for " + deviceName + " " + deviceMac);

        if (!initBt()) {
            Log.v(TAG, "If you get here, oh shit BT isn't supported WTF!");
        }

        if (!isBleAvailable()) {
            Log.v(TAG, "If you get here, oh shit BT isn't available WTF!");
        }

        if (!isBtEnabled()) {
            Log.v(TAG, "If you get here, oh shit BT isn't available WTF!");
        }

        this.device = null;
        this.deviceAddress = deviceMac;

        adapter.startLeScan(deviceFoundCallback);

        // please, remember to add timeout for that scan
        Runnable timeout = new Runnable() {
            @Override
            public void run() {
                stopSearching();

                if (device == null) {
                    Log.v(TAG, "NO DEVICES FOUND!");
                    //TODO WTF rescan again?
                    //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderStatus, "Device not found :(", true));
                }
            }
        };

        leScanTimeoutHandler.postDelayed(timeout, 10000); // 10 seconds
    }

    private void stopSearching() {
        if (adapter != null) {
            adapter.stopLeScan(deviceFoundCallback);
            //Log.i(TAG, "Searching for devices with Genesis service stopped");
        }

        if (leScanTimeoutHandler != null) {
            leScanTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    private boolean initBt() {
        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (btManager != null) adapter = btManager.getAdapter();

        return (btManager != null) && (adapter != null);

    }

    private boolean isBleAvailable() {

        //Log.i(TAG, "Checking if BLE hardware is available");

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) && adapter != null) {
            //Log.i(TAG, "BLE hardware available");
        } else {
            //Log.i(TAG, "BLE hardware is missing!");
            return false;
        }

        return true;
    }

    private boolean isBtEnabled() {

        //Log.i(TAG, "Checking if BT is enabled");
        if (adapter.isEnabled()) {
            //Log.v(TAG, "BT is enabled");
        } else {
            //Log.i(TAG, "BT is disabled. Use Setting to enable it and then come back to this app");
            return false;
        }
        return true;
    }

    private Object thing = new Object();

    private static Set<BootloaderState> notificationStates = new HashSet<>();

    static {
        notificationStates.add(BootloaderState.SEND_IMAGE_SIZE);
        notificationStates.add(BootloaderState.SEND_INIT_PACKET_COMPLETE);
        notificationStates.add(BootloaderState.SEND_VALIDATE);
    }



    private synchronized void connectToDevice(BluetoothDevice bledevice) {
        //Log.v(TAG, "connect to device" + connecting);
        if (!connecting && !connected) {
            connecting = true;
            try {
                //resetBluetoothState();
                stopSearching();
                //start the command thread, we're going to need it!
                startCommandThread();
                this.device = bledevice;

                //Log.i(TAG, "Connecting to the device NAME: " + device.getName() + " HWADDR: " + device.getAddress());

                if (Build.MANUFACTURER.equalsIgnoreCase("samsung") || Build.MANUFACTURER.toLowerCase().contains("samsung")) {

                    //Log.v(TAG, "Connecting Bluetooth for Samsung");
                    new Handler(getApplication().getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                gatt = device.connectGatt(getApplicationContext(), false, gattCallback);
                            } catch (Exception e) {
                                //this is stupid
                                disconnectFromDevice(true);
                            }
                        }
                    });
                } else {
                    gatt = device.connectGatt(getApplicationContext(), false, gattCallback);
                }
            } catch (Exception e) {
                Log.e(TAG, "exception in connectToDevice", e);
                //send conncection failure
                disconnectFromDevice(true);
            }
        }
    }

    private void discoverServices() {
        //Log.i(TAG, "Starting discovering services");
        try {
            if (gatt != null) {
                gatt.discoverServices();
            } else {
                disconnectFromDevice(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "discoverServices error", e);
            disconnectFromDevice(true);
        }
    }

    private void disconnectFromDevice(boolean reconnect) {
        //Log.v(TAG, "Disconnecting from device: ");

        if (!this.characteristicMap.isEmpty()) {
            for (Map.Entry<CharacteristicEnum, GenesisCharacteristic> entry : this.characteristicMap.entrySet()) {
                if (entry.getValue().getNotifyCharacteristic() != null) {
                    disableNotification(entry.getValue().getNotifyCharacteristic());
                }
            }
        }

        this.characteristicMap = new HashMap<>();

        resetBluetoothState();

        commandQueue = new LinkedBlockingQueue<>(100);
        waitingCommands = new Stack<LevelCommand>();
        packetIdOut = 0;

        stopCommandThread();
        connecting = false;
        connected = false;

        //Log.v(TAG, "Sending disconnect message");
        //TODO do i need to fix this??
        //EventBus.getDefault().post(new ClientEvent(ClientDeviceEvent.Disconnected));

    }

    private void resetBluetoothState() {
        //Log.v(TAG, "Resetting Bluetooth State...");

        stopSearching();

        adapter = null;
        device = null;
        //TODO add simple state machine
        //bootstrapStateMachine = new DeviceBootstrapStateMachine();

        closeGatt();
    }

    private void closeGatt() {
        if (gatt != null) {
            try {
                gatt.disconnect();
                if (gatt != null) {
                    gatt.close();
                }

                gatt = null;
            } catch (Exception e) {
                //Log.v(TAG, "btGatt Close: Exception...");
            }
        }
    }

    private void startCommandThread() {
        runCommandThread = true;
        Thread thread = new Thread(new CommandThread());
        thread.start();
    }

    private void stopCommandThread() {
        //Log.v(TAG, "stopCommandThread");
        runCommandThread = false;
    }

    //command thread
    private class CommandThread implements Runnable {
        private static final int MAX_TIMEOUT = 2 * 1000; //2 seconds
        private static final int MAX_RETRIES = 3;
        private long timestamp;
        private int currentRetries;

        private void callCommand(LevelCommand command) {
            sentCommand = command;
            timestamp = new Date().getTime();

            if (command.getReadOrWrite() == ReadWriteEnum.READ) {
                Log.v(TAG, "Reading characteristic: " + command.getCharacteristic().getName() + " " + command.getCharacteristic().getUuid());
                readCharacteristic(command.getCharacteristic());
            } else if (command.getReadOrWrite() == ReadWriteEnum.WRITE) {
                try {
                    Log.v(TAG, "writing: " + command.getCharacteristic().getName() + " " + command.getCharacteristic().getUuid() + " - " + new String(command.getData(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                writeCharacteristic(command.getCharacteristic(), command.getData());
            }
        }

        public void run() {
            //Log.d(TAG, "CommandThread starting");
            int timer = 0;

            while (runCommandThread) {
                ////Log.v(TAG, "commandQueue " + commandQueue.isEmpty() + " - " + sentCommand);
                if ((!commandQueue.isEmpty()) && (sentCommand == null)) {
                    synchronized (this) {
                        if ((!commandQueue.isEmpty()) && (sentCommand == null)) {
                            //Log.d(TAG, "CommandThread.run(): sending command");
                            currentRetries = 0;
                            callCommand(commandQueue.remove());
                        }
                    }
                }
                // should timeout and disconnect if no response....
                if (sentCommand != null) {
                    if (timer / 1000 > 0) {
                        //Log.d(TAG, "CommandThread.run(): waitingCommand is NOT null!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        timer = 0;
                    }

                    long elapsedTime = new Date().getTime() - timestamp;

                    //if no response to sent command in 2 seconds, retry
                    /*if( elapsedTime > MAX_TIMEOUT && currentRetries < MAX_RETRIES) {
                        currentRetries++;
                        callCommand(sentCommand);
                    } else {
                        disconnectFromDevice();
                    }*/

                    ////Log.d(TAG, "    elapsedTime = " + elapsedTime);
                }

                timer++;
                if (!commandQueue.isEmpty()) {
                    //currentRetries = 0;
                    ////Log.d(TAG, "CommandThread.run(): commandStack is NOT empty!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Log.w(TAG, "CommandThread.run(): " + ie.getMessage());
                }
            }
        }
    }

    private LevelCommand prevCommand;

    private synchronized void addCommand(LevelCommand command) {
        Log.v(TAG, "addCommand called " + command.getCharacteristic());
        //if (!command.equals(prevCommand))
            commandQueue.add(command);

        prevCommand = command;
    }

    private synchronized void addWriteCommad(LevelCommand command) {
        //Log.v(TAG, "addWriteCommad called " + command.getCharacteristic());
        addCommand(command);
        packetIdOut++;

        if (packetIdOut > 255)
            packetIdOut = 0;
    }

    private void readCharacteristic(CharacteristicEnum characteristic) {
        //Log.v(TAG, "Reading char: " + characteristic + " - " + (characteristicMap.get(characteristic) == null ? "null" : "found") + " - " +
        //        (characteristicMap.get(characteristic).getReadCharacteristic() == null ? "null" : "found"));

        if (characteristicMap.get(characteristic).getReadCharacteristic() != null) {
            //Log.v(TAG, "really reading");
            gatt.readCharacteristic(characteristicMap.get(characteristic).getReadCharacteristic());
        }
    }

    private void writeCharacteristic(CharacteristicEnum characteristic, byte data[]) {
        Log.v(TAG, "Writing char: " + characteristic + " data: " + Arrays.toString(data));

        GenesisCharacteristic gc = characteristicMap.get(characteristic);
        if (characteristicMap.get(characteristic).getWriteCharacteristic() != null) {
            BluetoothGattCharacteristic ch = characteristicMap.get(characteristic).getWriteCharacteristic();
            if (adapter == null || gatt == null) return;

            Log.v(TAG, "really writing");

            ch.setValue(data);
            gatt.writeCharacteristic(ch);
        }
    }

    private void getBootloaderCharacteristics() {
        if (gatt != null && this.serviceMap.containsKey(ServiceEnum.DFU)) {
            BluetoothGattService dfu = this.serviceMap.get(ServiceEnum.DFU);

            this.characteristicMap.put(CharacteristicEnum.DFU_CONTROL_POINT, getCharacteristic(dfu, CharacteristicEnum.DFU_CONTROL_POINT.getUuid()));
            this.characteristicMap.put(CharacteristicEnum.DFU_PACKET, getCharacteristic(dfu, CharacteristicEnum.DFU_PACKET.getUuid()));
            this.characteristicMap.put(CharacteristicEnum.DFU_VERSION, getCharacteristic(dfu, CharacteristicEnum.DFU_VERSION.getUuid()));
        } else {
            //TODO throw exception
        }
    }

    private void startStateMachine() {
        addCommand(stateMachine.getState().getCommand(null));
    }

    private void processResult() {
        byte data[] = new byte[0];
        boolean error = false;
        Log.v(TAG, "current state = " + stateMachine.getState());

        try {
            data = stateMachine.process(null);
        } catch (IOException e) {
            Log.e(TAG, "OH SHIT!!", e);
            error = true;
        }

        Log.v(TAG, "double current state = " + stateMachine.getState());

        if (stateMachine.getState() == BootloaderState.UPLOAD_IMAGE && !upload) {
            Log.v(TAG, "settingupload to true");
            upload = true;
        } else if (stateMachine.getState() != BootloaderState.UPLOAD_IMAGE) {
            Log.v(TAG, "settingupload to false");
            upload = false;
        }

        if( stateMachine.getState() != BootloaderState.UPLOAD_IMAGE ) {
            //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderStatus, "State: " + stateMachine.getState(), true));
        } else if(stateMachine.getPacketsSent() != 0 && stateMachine.getPacketsSent() % PACKET_NUM_REQ == 0) {
            //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderStatus, "State: " + stateMachine.getState() +
              //      " bytes uploaded: " + stateMachine.getBytesUploaded() + " of " + stateMachine.getAppImageSize(), true));
        }

        if (!error && stateMachine.getState().getCommand(data) != null) {
            if(stateMachine.getState() == BootloaderState.UPLOAD_IMAGE) {
                int p = (int) ((100f * stateMachine.getBytesUploaded()) / fileSize);

                if (p > currentPercentage) {
                    Intent intent = new Intent(BleDeviceOutput.BootloaderProgress.name());
                    intent.putExtra("BOOTLOADER_PROGRESS", p);
                    sendBroadcast(intent);
                    currentPercentage = p;
                }
                LevelCommand command = stateMachine.getState().getCommand(data);

                writeCharacteristic(command.getCharacteristic(), command.getData());
            } else {
                addCommand(stateMachine.getState().getCommand(data));
            }
        }

        if (stateMachine.getState() == BootloaderState.RECONNECT_AND_VALIDATE) {
            sendBroadcast(new Intent(BleDeviceOutput.BootloaderFinished.name()));
        }
    }

    private GenesisCharacteristic getCharacteristic(BluetoothGattService service, UUID characteristicUuid) {
        GenesisCharacteristic charac = new GenesisCharacteristic(CharacteristicEnum.getByUuid(characteristicUuid));

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        //Log.i(TAG, "characteristic: " + characteristicUuid.toString() + " " + (characteristic == null ? "null" : "found"));

        if (characteristic != null) {
            // Clear any pending notifications *********
            final int charaProp = characteristic.getProperties();

            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                //Log.v(TAG, "found read char");
                charac.setReadCharacteristic(characteristic);
            } else {
                charac.setReadCharacteristic(null);
            }

            // Obtain the Write Characteristic for sending commands ***********
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0 || (charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                //Log.v(TAG, "found write char");
                charac.setWriteCharacteristic(characteristic);
            } else {
                charac.setWriteCharacteristic(null);
            }

            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) { //&& (charac.getCharacteristic() == CharacteristicEnum.UART_RX)) {
                //Log.v(TAG, "found notification char");
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
        //Log.d(TAG, "BLEService.enableNotificationForGenesis() ++++++++++++++++++++++++++++++++++++");
        //Log.i(TAG, "Enabling notification for Genesis " + gattCharacteristic.getUuid());

        if (notifying) {
            notifyStack.push(gattCharacteristic);
            return;
        } else {
            notifying = true;
        }

        boolean success = gatt.setCharacteristicNotification(gattCharacteristic, true);

        if (!success) {
            //Log.i(TAG, "Enabling notification failed!");
            return;
        }

        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(BleManager.CHAR_CLIENT_CONFIG);

        if (descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);

            //Log.i(TAG, "Notification enabled");
        } else {
            //Log.i(TAG, "Could not get descriptor for characteristic! Notification are not enabled.");
        }
    }

    private void disableNotification(BluetoothGattCharacteristic gattCharacteristic) {
        //Log.d(TAG, "BLEService.disableNotificationForGenesis() ++++++++++++++++++++++++++++++++++++");
        try {
            if ((gatt != null) && (gattCharacteristic != null)) {

                //Log.i(TAG, "Disabling notification for Genesis ");

                boolean success = gatt.setCharacteristicNotification(gattCharacteristic, false);

                if (!success) {
                    //Log.i(TAG, "Disabling notification failed!");
                    return;
                }

                BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(BleManager.CHAR_CLIENT_CONFIG);

                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    //Log.i(TAG, "Notification disabled");
                } else {
                    //Log.i(TAG, "Could not get descriptor for characteristic! Disabling notifications may have failed.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception disconnecting ignore", e);
            resetBluetoothState();
        }
    }

    private boolean remoteError = false;
    private int packetNotificationCount = PACKET_NUM_REQ;
    private int packetsSent = 0;
    private boolean upload = false;
    public int packetIdIn = -1;

    public void resetCounter() {
        packetIdIn = -1;
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            //Log.i(TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                //Log.i(TAG, "onConnectionStateChange - connected");
                resetCounter();
                //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.Connected, true));
                discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //Log.i(TAG, "onConnectionStateChange - disconnected");
                resetCounter();
                connected = false;
                disconnectFromDevice(false);
                startScan(DFU_DEVICE_NAME, deviceMac);
            } else if (newState == BluetoothProfile.STATE_CONNECTED && status != BluetoothGatt.GATT_SUCCESS) {
                //Log.i(TAG, "onConnectionStateChange - uh connected but not");
                resetCounter();
                gatt.connect();
                //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.NotReallyConnected, true));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Log.i(TAG, "Services discovered");

                ServiceEnum snum = ServiceEnum.getByUuid(BootloaderService.DFU_SERVICE_UUID);
                BluetoothGattService service = gatt.getService(BootloaderService.DFU_SERVICE_UUID);

                if (snum != null) {
                    if (serviceMap == null) {
                        serviceMap = new HashMap<>();
                    }

                    serviceMap.put(snum, service);
                }

                characteristicMap = new HashMap<>();
                getBootloaderCharacteristics();
                //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.FoundServices, serviceMap, true));
            } else {
                //Log.i(TAG, "Unable to discover services");
                disconnectFromDevice(true);
                connected = false;
                //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.Error, true));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //Log.v(TAG, "BLEService.gattCallback.onCharacteristicChanged() *******************************************");
            CharacteristicEnum charac = CharacteristicEnum.getByUuid(characteristic.getUuid());

            Log.v(TAG, "Data received char: " + charac.getName() + " data: " + Arrays.toString(characteristic.getValue()));

            //EventBus.getDefault().post(new RawDataEvent(characteristic.getValue(), false));

            final int responseType = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

            //Log.v(TAG, "response type = " + responseType);

            switch (DFUResponseType.getByType(responseType)) {
                case PACKET_RECEIPT:
                    sentCommand = null;
                    processResult();
                    //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderPacketNotificationSuccess, true));
                    break;
                case RESPONSE_CODE:
                case UNKNOWN:
                default:
                    if (remoteError)
                        break;

                    final int status = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);

                    if (status != DFU_STATUS_SUCCESS) {
                        remoteError = true;
                        Log.e(TAG, "REMOTE ERROR!!!");
                        //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.DeviceError, status, true));
                        disconnectFromDevice(false);
                    } else {
                        Log.v(TAG, "REMOTE SUCCESS!!");
                        byte data[] = characteristic.getValue();

                        sentCommand = null;
                        processResult();
                        //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.DeviceWriteSuccess, data, true));
                    }


                    break;
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //Log.v(TAG, "BLEService.gattCallback.onDescriptorWrite() *******************************************");
            //Log.i(TAG, "onDescriptorWrite: " + descriptor.getCharacteristic().getUuid() + " status: " + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (!notifyStack.isEmpty()) {
                    enableNotification(notifyStack.pop());
                } else {
                    notifying = false;

                    if (characteristicMap.size() == 3) {
                        startStateMachine();
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            //Log.v(TAG, "BLEService.gattCallback.onCharacteristicRead() *******************************************");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                CharacteristicEnum charac = CharacteristicEnum.getByUuid(characteristic.getUuid());

                if( charac != null && charac == CharacteristicEnum.DFU_VERSION ) {
                    int version = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    sentCommand = null;

                    if (version > 5) {
                        //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderStatus, "Version > 5 proceeding.", true));
                        processResult();
                    }
                    //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.Data, characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0), true));
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

            Log.v(TAG, "BLEService.gattCallback.onCharacteristicWrite() *******************************************");

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Write Success: " + characteristic.getUuid());
                if (CharacteristicEnum.DFU_PACKET.getUuid().equals(characteristic.getUuid())) {
                    Log.i(TAG, "DFU PACKET Char: " + packetsSent + " = " + packetNotificationCount);
                    if (upload && ++packetsSent == packetNotificationCount) {
                        //Log.i(TAG, "reseting packetSent to zero");
                        packetsSent = 0;
                    } else {
                        sentCommand = null;
                        if (!notificationStates.contains(stateMachine.getState())) {
                            processResult();
                        }
                        //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.WriteSuccess, true));
                    }
                } else {
                    sentCommand = null;
                    if (!notificationStates.contains(stateMachine.getState())) {
                        processResult();
                    }
                    //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.WriteSuccess, true));
                }
            } else {
                Log.i(TAG, "Write Failure: " + characteristic.getUuid());
            }
        }
    };

    public void onEvent(Object deviceEvent) {
        //Log.v(TAG, "BleDeviceEvent onEvent " + deviceEvent.getDeviceEvent());

                //EventBus.getDefault().post(new BleDeviceEvent(DeviceEvent.BootloaderStatus, "Starting scan for " + BootloaderService.DFU_DEVICE_NAME));
        //TODO deal with reboot
                startScan(DFU_DEVICE_NAME, deviceMac);
    }

    private BluetoothAdapter.LeScanCallback deviceFoundCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null && device.getName() != null && device.getName().contains(DFU_DEVICE_NAME) && deviceMac.equalsIgnoreCase(device.getAddress())) {
                connectToDevice(device);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BootloaderClientCommand.StartRealBootloader.name());

        return intentFilter;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "received " + intent.getAction());
            BootloaderClientCommand command = BootloaderClientCommand.valueOf(intent.getAction());

            switch (command) {
                case StartRealBootloader:
                    BootloaderPayload payload = (BootloaderPayload) intent.getSerializableExtra(BOOTLOADER_PAYLOAD_THING);

                    startBootloader(payload);
                    break;
                default:
                    Log.v(TAG, "Client Command not found WTF");
                    break;
            }
        }
    };
}
