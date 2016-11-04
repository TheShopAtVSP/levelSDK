//
//  BleManager.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

//import Foundation
import CoreBluetooth
import CoreLocation

enum ClientMessages {
    case BluetoothNotAvailable, BluetoothNotOn, InputLedCode, LedCodeAccepted, LedCodeFailed, LedCodeDone, LedCodeNotNeeded, DeviceReady,
        BootloaderFinished, LastUserLocation, Step, BatteryReport, MotionData, BatteryLevel, BatteryState, Firmware, Bootloader, Frame, DeviceDisconnect,
        BootloaderMessage, BondError, ConnectionTimeout
}

enum DefaultKeys: String {
    case deviceKeys
}

private let bleManager = BleManager(restorationId: centralManagerId)
let centralManagerId = "deviceBLEUniqueIdentifierThing"

class BleManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate, CLLocationManagerDelegate, BootloaderDelegate {
    class var sharedInstance: BleManager {
        return bleManager
    }

    var clients: [NSUUID:DeviceObserverCallbacks]
    let clientLockQueue = DispatchQueue(label: "com.theshopatvsp.LockQueue")
    let deviceLockQueue = DispatchQueue(label: "com.theshopatvsp.DeviceLockQueue")

    private var centralManager: CBCentralManager?
    private var foundDevices: [UUID:BleFoundDevice]
    private var device: CBPeripheral?
    private var notifyCount: Int = 0
    private var notifyAckCount: Int = 0
    private var characteristicMap: [BleCharacteristics:CBCharacteristic] = [BleCharacteristics:CBCharacteristic]()
    private var commandQueue: [BleCommand]
    private var sentCommand: BleCommand? = nil
    private var queueRunning: Bool = false
    private var packetParserManager: PacketParserManager
    private var stateMachine: DeviceStateMachine
    private var connected: Bool = false, keySent = false, userIsActive = false, pairing = false, deviceReady = false, activeTimeSet = false
    private var wannaBeActiveSteps: [Step] = [Step]()
    private var inactiveCount: Int = 0
    private var disconnectRetries: Int = 0
    private var connectionTries: Int = 0
    private var frameId: String?

    private var timer: Timer?
    private var disconnectTimer: Timer?
    private var connectTimer: Timer?
    private var activeTimeTimer: Timer?

    private var deviceId: DeviceIdManager

    private var user: LevelUser?
    private var calculationHelper: CalculationHelper?

    //bootloader stuff
    private var bootloaderManager: BootloaderManager?
    private var reboot: Bool = false
    private var firmware: NSURL?
    private var clientBootloaderDelegate: BootloaderDelegate?

    //location Manager stuff
    var locationManager: CLLocationManager = CLLocationManager()
    var counter: Int = 1
    static var currentLocation: CLLocation?

    override init() {

        self.clients = [NSUUID: DeviceObserverCallbacks]()
        self.foundDevices = [UUID:BleFoundDevice]()
        self.deviceId = DeviceIdManager()
        self.commandQueue = [BleCommand]()
        self.packetParserManager = PacketParserManager()
        self.stateMachine = DeviceStateMachine()

        super.init()

        //let centralQueue = dispatch_queue_create("com.theshopatvsp.genesis", DISPATCH_QUEUE_SERIAL)
        //self.centralManager = CBCentralManager(delegate: self, queue: centralQueue)

        self.startCommandQueue()

        self.locationManager.delegate = self
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest
        self.locationManager.distanceFilter = 5
        self.locationManager.pausesLocationUpdatesAutomatically = true
        self.locationManager.activityType = CLActivityType.fitness
        self.locationManager.requestAlwaysAuthorization()
        self.locationManager.startUpdatingLocation()
        NotificationCenter.default.addObserver(forName: Notification.Name.UIApplicationDidEnterBackground, object: nil, queue: nil) {
                                                                  (notification) in
                                                                  debugPrint("Monitoring significant location changes")
                                                                  self.locationManager.stopUpdatingLocation()
                                                                  self.locationManager.startMonitoringSignificantLocationChanges()
        }
        
        NotificationCenter.default.addObserver(forName: Notification.Name.UIApplicationDidBecomeActive,
                                                                object: nil, queue: nil) {
                                                                  (notification) in
                                                                  debugPrint("updating location changes")
                                                                  self.locationManager.stopMonitoringSignificantLocationChanges()
                                                                  self.locationManager.startUpdatingLocation()
        }
//        self.locationManager.startMonitoringSignificantLocationChanges()
//        debugPrint("Requesting locations... \()")
    }

    convenience init(restorationId: String) {
        self.init()

        let centralQueue = DispatchQueue(label: "com.theshopatvsp.genesis")
        self.centralManager = CBCentralManager(delegate: self, queue: centralQueue, options: [CBCentralManagerOptionRestoreIdentifierKey: restorationId])
        debugPrint("Hey made it to the end!!!!")
//        bleManager = self
    }

    deinit {
        stopCommandQueue()
    }

    func registerDeviceCallbacks(clientId: NSUUID, callbacks: DeviceObserverCallbacks) {
        clientLockQueue.sync() {
            self.clients[clientId] = callbacks
        }
    }

    func unregisterDeviceCallbacks(clientId: NSUUID) {
        _ = clientLockQueue.sync() {
            self.clients.removeValue(forKey: clientId)
        }
    }

    func connect(frameId: String) {
        if frameId != "" {
            self.frameId = frameId
        }
      
        startScan()
    }

    func isConnected() -> Bool {
        return connected
    }

    func sendLedCode(code: Int) {
        debugPrint("sendLedCode = \(code)")
        if deviceReady || isBlinkToLink() {
            executeCommand(command: .CodeWR, packet: CodePacket(code: code))

            if stateMachine.getState() == DeviceLifecycle.SendLedCode4 {
                debugPrint("sending the last LED code, setting the disconnect timer")
                setDisconnectTimer()
            }
        }
    }

    func deviceLightsNotOn() {
        debugPrint("deviceLightsNotOn")
        var values = foundDevices.values.sorted(by: {$0.rssi > $1.rssi})

        if let central = self.centralManager {
            if( self.device != nil ) {
                central.cancelPeripheralConnection(self.device!)
                debugPrint("connecting to \(self.device?.name) -- \(self.device?.identifier)")

                // Check for array index out of
              if connectionTries >= 0 && connectionTries < values.count {
                connectToDevice(bleDevice: values[connectionTries].device)
                connectionTries += 1
              }

            }
        }
    }

    func deleteSavedKey() {
        debugPrint("deleteSavedKey")
        if let savedKeys = getDeviceKeys() , savedKeys != "" {
            let keys: [String] = savedKeys.components(separatedBy: "|")

            for key in keys {
                deleteDeviceIdentifier(key: key)
            }
        }
        resetDeviceKey()
    }

    func setUser(user: LevelUser) {
        debugPrint("BleManager setUser")
        self.user = user
        self.calculationHelper = CalculationHelper(user: user)
    }

    func getBatteryLevel() {
        if deviceReady {
            addCommand(command: BleCommand(readWrite: .Read, charac: BleCharacteristics.BatteryLevel))
        }
    }

    func getBatteryState() {
        if deviceReady {
            addCommand(command: BleCommand(readWrite: .Read, charac: BleCharacteristics.BatteryState))
        }
    }

    func getFirmwareVersion() {
        if deviceReady {
            addCommand(command: BleCommand(readWrite: .Read, charac: BleCharacteristics.FirmwareVersion))
        }
    }
  
    func getBootloaderVersion() {
      if deviceReady {
        addCommand(command: BleCommand(readWrite: .Read, charac: BleCharacteristics.BootloaderVersion))
      }
    }
  
    func setTransmitControlToOn() {
      if deviceReady {
        executeCommand(command: DeviceCommand.TransmitControl, packet: CodePacket(code: 1))
      }
    }

    func getFrameInfo() {
        if deviceReady {
            executeCommand(command: DeviceCommand.FrameRD, packet: nil)
        }
    }

    func startBootloader(firmwareFile: NSURL, delegate: BootloaderDelegate) {
        //TODO reboot device
        var b: [UInt8] = [UInt8]()
        b.append(0xEA)
        self.reboot = true
        debugPrint("rebooting device into DFU!!")
        broadcastUpdate(message: .BootloaderMessage, thing: "Writing 0xEA to Battery Level Char" as NSObject)
        addCommand(command: BleCommand(readWrite: .Write, charac: .BatteryLevel, bytes: b))

        self.firmware = firmwareFile
        self.clientBootloaderDelegate = delegate
        self.bootloaderManager = BootloaderManager(delegate: self)
    }

    func isBlinkToLink() -> Bool {
        return stateMachine.getState().rawValue.lowercased().hasPrefix("sendledcode")
    }

    func startScan() {
        if let central = self.centralManager {
            if central.state != .poweredOn {
                debugPrint("CoreBluetooth not correctly initialized !\r\n")
                broadcastUpdate(message: ClientMessages.BluetoothNotOn)
                return
            }
        }

        if let central = self.centralManager {
            if let savedKeys: String = getDeviceKeys() , savedKeys != "" {
                if let frameId = self.frameId {
                    if savedKeys.range(of: frameId) != nil {
                        let deviceName = "Level " + frameId
                        if let identifier = getDeviceIdentifier(key: deviceName) {
                            var devices: [CBPeripheral] = central.retrievePeripherals(withIdentifiers: [identifier as UUID])

                            if !devices.isEmpty {
                                debugPrint("trying to connect to saved device \(devices[0].name)")
                                connectToDevice(bleDevice: devices[0])
                                return
                            }
                        }
                    } else {
                        //scan
                    }
                } else {
                    let keys: [String] = savedKeys.components(separatedBy: "|")

                    for key in keys {
                        if let identifier = getDeviceIdentifier(key: key) {
                            let devices: [CBPeripheral] = central.retrievePeripherals(withIdentifiers: [identifier as UUID])

                            if !devices.isEmpty {
                                debugPrint("trying to connect to saved device \(devices[0].name)")
                                connectToDevice(bleDevice: devices[0])
                                return
                            }
                        }
                    }
                }
            } else {
                //scan
            }
        }

        debugPrint("Starting scan: \(NSDate().timeIntervalSince1970)")
        setTimer()
        if let central = self.centralManager {
            //central.scanForPeripheralsWithServices([CBUUID(string: BleServices.UART.rawValue)], options: nil)
            central.scanForPeripherals(withServices: nil, options: [CBCentralManagerScanOptionAllowDuplicatesKey : NSNumber(value: true)])
        }
    }

    func setTimer() {
        let runLoop: RunLoop = RunLoop.main
        let fireDate: NSDate = NSDate(timeIntervalSinceNow: 15.0)
        self.timer = Timer(fireAt: fireDate as Date, interval: 0.1, target: self, selector: #selector(scanTimerExpired), userInfo: nil, repeats: false)

        runLoop.add(self.timer!, forMode: RunLoopMode.commonModes)
    }

    func setDisconnectTimer() {
        let runLoop: RunLoop = RunLoop.main
        let fireDate: NSDate = NSDate(timeIntervalSinceNow: 1.0)
        self.disconnectTimer = Timer(fireAt: fireDate as Date, interval: 0.1, target: self, selector: #selector(disconnectTimerExpired), userInfo: nil, repeats: false)

        runLoop.add(self.disconnectTimer!, forMode: RunLoopMode.commonModes)
    }

    func setConnectTimer() {
        let runLoop: RunLoop = RunLoop.main
        let fireDate: NSDate = NSDate(timeIntervalSinceNow: 20.0)
        self.connectTimer = Timer(fireAt: fireDate as Date, interval: 0.1, target: self, selector: #selector(connectTimerExpired), userInfo: nil, repeats: false)

        runLoop.add(self.connectTimer!, forMode: RunLoopMode.commonModes)
    }

    func scanTimerExpired() {
        if let central = self.centralManager {
            central.stopScan()
        }

        debugPrint("Stop scanning timeout")

        if !foundDevices.isEmpty {
            var values = foundDevices.values.sorted(by: {$0.rssi > $1.rssi})
//            self.device = nil
//            if( self.device == nil ) {

                debugPrint("connecting to \(self.device?.name) -- \(self.device?.identifier)")
                connectionTries = connectionTries % values.count
                connectToDevice(bleDevice: values[connectionTries].device)
                connectionTries += 1
//            }
        } else {
          connectTimerExpired()
//          self.broadcastUpdate
//            startScan()
        }
    }

    func disconnectTimerExpired() {
        debugPrint("disconnectTimerExpired, sending LedCodeFailed and disconnecting")
        broadcastUpdate(message: .LedCodeFailed)
        /*if let central = self.centralManager {
            central.cancelPeripheralConnection(self.device!)
        }*/
        //disconnect
        //send onLedCodeFailed
    }

    func connectTimerExpired() {
        debugPrint("connectTimerExpired")
        /*if let central = self.centralManager {
            central.cancelPeripheralConnection(self.device!)

            if getDeviceKeys() != nil {
                debugPrint("saved device nuke it and scan")
                resetDeviceKey()
                startScan()
            } else {
                debugPrint("no saved device, already scanned go to next device on the list")
                deviceLightsNotOn()
            }
        }*/
        broadcastUpdate(message: .ConnectionTimeout)
    }

    func reset() {
        debugPrint("reset everything")
        sentCommand = nil
        deviceId.reset()
        connected = false
        deviceReady = false
    }

    func saveDeviceKey(deviceKey: String) {
        debugPrint("saveDeviceKey \(deviceKey)")
        let defaults = UserDefaults.standard

        if let value = defaults.string(forKey: DefaultKeys.deviceKeys.rawValue) , value != "" {
            debugPrint("saveDeviceKey \(value)")
            if value.range(of: deviceKey) != nil {
                debugPrint("saveDeviceKey: didn't find key adding")
                let newValue = value + "|" + deviceKey.lowercased()

                defaults.setValue(newValue, forKey: DefaultKeys.deviceKeys.rawValue)
            }
        } else {
            debugPrint("saveDeviceKey: no keys adding")
            defaults.setValue(deviceKey.lowercased(), forKey: DefaultKeys.deviceKeys.rawValue)
        }
    }

    func resetDeviceKey() {
        debugPrint("resetDeviceKeys")
        let defaults = UserDefaults.standard
        defaults.setValue("", forKey: DefaultKeys.deviceKeys.rawValue)
    }

    func getDeviceKeys() -> String? {
        let defaults = UserDefaults.standard

        return defaults.string(forKey: DefaultKeys.deviceKeys.rawValue)
    }

    func saveDeviceIdentifier(key: String, identifier: String) {
        debugPrint("saveDeviceIdentifier: \(key) \(identifier)")
        let defaults = UserDefaults.standard
        let values = key.components(separatedBy: " ")

        defaults.setValue(identifier, forKey: values[1].lowercased())
    }

    func deleteDeviceIdentifier(key: String) {
        debugPrint("deleteDeviceIdentifier: \(key)")
        let defaults = UserDefaults.standard
        let values = key.components(separatedBy: " ")

        defaults.removeObject(forKey: values[1])
    }

    func getDeviceIdentifier(key: String) -> NSUUID? {
        let defaults = UserDefaults.standard
        let values = key.components(separatedBy: " ")

        //let dict = defaults.dictionaryRepresentation()

        //debugPrint("\(dict)")

        if let value = defaults.value(forKey: values[1].lowercased()) {
            return NSUUID(uuidString: value as! String)
        }

        return nil
    }


// MARK: - CBCentralManagerDelegate

    func centralManager(_ central: CBCentralManager, willRestoreState dict: [String:Any]) {
        if let peripherals = dict[CBCentralManagerRestoredStatePeripheralsKey] as? [CBPeripheral] {
            connectToDevice(bleDevice: peripherals[0])
        } else {
            startScan()
        }
    }

    func centralManager(_ : CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String:Any], rssi RSSI: NSNumber) {
        if Int(RSSI) > 0 {
            return
        }

        if let name = peripheral.name {
            //TODO remove 5b filter
            if name.lowercased().range(of: "level") != nil {
                if let frameId = self.frameId {
                    if frameId == "" {
                        debugPrint("device found: \(peripheral.name) \(RSSI)")
                        self.foundDevices[peripheral.identifier] = BleFoundDevice(device: peripheral, rssi: Int(RSSI))
                        return
                    }

                    if name.lowercased().range(of: frameId.lowercased()) != nil {
                        debugPrint("device found: \(peripheral.name) \(RSSI)")
                        self.foundDevices[peripheral.identifier] = BleFoundDevice(device: peripheral, rssi: Int(RSSI))
                    }
                } else {
                    debugPrint("device found: \(peripheral.name) \(RSSI)")
                    self.foundDevices[peripheral.identifier] = BleFoundDevice(device: peripheral, rssi: Int(RSSI))
                }
            }
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        print("didConnectPeripheral \(peripheral.identifier.uuidString) \(peripheral.name)")

        self.disconnectRetries = 0
        self.connectTimer?.invalidate()
        reset()
        self.device?.discoverServices(nil)
        // Create new service class
        /*if (peripheral == self.device) {
            self.bleService = BLEService(initWithPeripheral: peripheral)
        }

        // Stop scanning for new devices
        central.stopScan()*/
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        print("didDisconnectPeripheral \(peripheral.identifier.uuidString)")
        var unrecoverableError = false
        self.connectTimer?.invalidate()

        if let realError = error {
            let rError = realError as NSError
            debugPrint("error: \(rError.debugDescription) \(rError.code)")

            let errorCode = rError.code
                debugPrint("here \(errorCode)")
                if errorCode == 6 || errorCode == 7 {
                    debugPrint("there")
                    broadcastUpdate(message: .BondError)
                    unrecoverableError = true
                }
        }

        reset()

        if !wannaBeActiveSteps.isEmpty {
            for step in wannaBeActiveSteps {
                broadcastUpdate(message: .Step, thing: step)
            }
        }
        debugPrint("will broadcast location")
        if user != nil {
          if let location = BleManager.currentLocation {
              broadcastUpdate(message: .LastUserLocation, thing: LastLocation(
                lat: location.coordinate.latitude,
                long: location.coordinate.longitude,
                accuracy: location.horizontalAccuracy,
                alt: location.altitude,
                glassName: user!.glassName,
                timezone:NSTimeZone.local.identifier))
                //TODO: don't know how to convert this to swift 3
                //debugPrint("broadcast location \(location.toJsonString())")
          }
        }
      
        debugPrint("broadcast location done")

        if stateMachine.getState() == DeviceLifecycle.Done {
            stateMachine.disconnected()
        } else {
            stateMachine.reset()
        }

        broadcastUpdate(message: .DeviceDisconnect)

        if unrecoverableError {
            return
        }

        //TODO error code 6 the bond is jacked up

        if self.reboot {
            broadcastUpdate(message: .BootloaderMessage, thing: "Received Disconnect, we are rebooting!" as NSObject)
            self.bootloaderManager?.start(firmware: self.firmware!)
            self.reboot = false
            return
        }

        if let central = centralManager {
            var devices: [CBPeripheral] = central.retrievePeripherals(withIdentifiers: [(self.device?.identifier)!])

            if !devices.isEmpty {
                connectToDevice(bleDevice: devices[0])
                //self.device!.discoverServices(nil)
                return
            }
        }
    }

    func connectToDevice(bleDevice: CBPeripheral) {
        if let central = self.centralManager {
            self.device = bleDevice
            self.device?.delegate = self
            self.stateMachine.reset()
            central.connect(self.device!, options: nil)
            setConnectTimer()
        }
    }

    func centralManager(_ central: CBCentralManager,
                        didFailToConnect peripheral: CBPeripheral,
                        error: Error?) {
        debugPrint("didFailToConnect")
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        print("centralManagerDidUpdateState: \(central.state.rawValue)")
        switch (central.state) {
        case CBManagerState.poweredOff:
            print("Powered off")
            let wasConnected = self.connected
            if wasConnected == true {
              self.connected = false
            }
            broadcastUpdate(message: ClientMessages.BluetoothNotOn)
            if wasConnected {
                broadcastUpdate(message: ClientMessages.DeviceDisconnect)
            }
                //self.clearDevices()
        case CBManagerState.unauthorized:
            print("Unauthorized")
            // Indicate to user that the iOS device does not support BLE.
            //NSNotificationCenter.defaultCenter().postNotificationName(deviceMessageNotification, object: nil, userInfo: ["message": DeviceMessages.BluetoothNotSupported.rawValue])
            break
        case CBManagerState.unknown:
            print("unknown")
            // Wait for another event
            break
        case CBManagerState.poweredOn:
            print("Powered On")
            //self.startScan()
            break
        case CBManagerState.resetting:
            print("Reset")
                //self.clearDevices()
        case CBManagerState.unsupported:
            print("unsupported")
            break
        }
    }

// MARK: - CBPeripheralDelegate
  
//  func centralManager(central: CBCentralManager, willRestoreState dict: [String:AnyObject]) {

    func peripheralDidUpdateName(_ peripheral: CBPeripheral) {

    }

    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
    }

    func peripheralDidUpdateRSSI(_ peripheral: CBPeripheral, error: Error?) {
    }

    func peripheral(_ peripheral: CBPeripheral, didReadRSSI RSSI: NSNumber, error: Error?) {
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        print("didDiscoverServices")
        if (peripheral != self.device) {
            // Wrong Peripheral
            return
        }

        if (error != nil) {
            let realError = error as! NSError
            print("didDiscoverService error: \(realError.description) --- \(realError.debugDescription)")
            return
        }

        if ((peripheral.services == nil) || (peripheral.services!.count == 0)) {
            // No Services
            print("didDiscoverService no services")
            return
        }

        for service in peripheral.services! {
            if [CBUUID(string: BleServices.UART.rawValue), CBUUID(string: BleServices.DeviceInfo.rawValue),
                CBUUID(string: BleServices.Battery.rawValue)].contains(service.uuid) {
                debugPrint("service \(service.uuid.uuidString)")
                peripheral.discoverCharacteristics(nil, for: service)
            }
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverIncludedServicesFor service: CBService, error: Error?) {
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        print("didDiscoverCharacteristicsForService called")
        if (peripheral != self.device) {
            // Wrong Peripheral
            return
        }

        if (error != nil) {
            return
        }

        for characteristic in service.characteristics! {
            debugPrint("char UUID = \(characteristic.uuid.uuidString)")

            if let charac = BleCharacteristics(rawValue: characteristic.uuid.uuidString.lowercased()) {
                debugPrint("characteristic \(charac)")
                self.characteristicMap[charac] = characteristic

                let properties: CBCharacteristicProperties = (characteristic as CBCharacteristic).properties


                if charac == .BatteryLevel && properties.rawValue & CBCharacteristicProperties.read.rawValue > 0 {
                    debugPrint("Trying to read value of battery level characteristic")
                    self.pairing = true
                    peripheral.readValue(for: characteristic)
                }

                if charac == .BatteryLevel && properties.rawValue & CBCharacteristicProperties.write.rawValue > 0 {
                    debugPrint("Battery Level CAN WRITE")
                }

                if properties.rawValue & CBCharacteristicProperties.notify.rawValue > 0 {
                    debugPrint("subscribing to notifications")
                    peripheral.setNotifyValue(true, for: characteristic as CBCharacteristic)
                    notifyCount+=1
                }
            }
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        debugPrint("char \(characteristic.uuid.uuidString)")
        let charac = BleCharacteristics(rawValue: characteristic.uuid.uuidString.lowercased())
        let data:NSData? = characteristic.value as NSData?
        if data == nil {
          return
        }
        let bytes: [UInt8] = BitsHelper.nsdataToUInt8(data: data!)

        debugPrint("didUpdateValue called char: \(charac) data: \(bytes)")

        if self.sentCommand != nil {
            self.sentCommand = nil
        }

        if let ch = charac {
            switch ch {
                case .BatteryLevel:
                    if bytes.count > 0 {
                            broadcastUpdate(message: .BatteryLevel, thing: Int(bytes[0]) as NSObject)
                    }

                    if self.pairing {
                        self.pairing = false

                        let deviceKeys = getDeviceKeys()

                        if deviceKeys == nil || deviceKeys?.range(of: (self.device?.name)!) == nil {
                            saveDeviceKey(deviceKey: (self.device?.name)!)
                            saveDeviceIdentifier(key: (self.device?.name!)!, identifier: (self.device?.identifier.uuidString)!)
                        }
                    }
                case .BatteryState:
                    if bytes.count > 0 {
                        broadcastUpdate(message: .BatteryState, thing: Int(bytes[0]) as NSObject)
                    }
                case .FirmwareVersion:
                    if bytes.count > 0 {
                        var version: String = ""

                        for b in bytes {
                            version += String(Character(UnicodeScalar(b)))
                        }

                        broadcastUpdate(message: .Firmware, thing: version as NSObject)
                    }
              case .BootloaderVersion:
                if bytes.count > 0 {
                  var version: String = ""
                  
                  for b in bytes {
                    version += String(Character(UnicodeScalar(b)))
                  }
                  
                    var components = version.components(separatedBy: "BL-")
                  if components.count == 2 {
                    version = components[1]
                  }
                  
                  broadcastUpdate(message: .Bootloader, thing: version as NSObject)
                }

                case .UartRX:
                    handleUartResponse(bytes: bytes)
                default:
                    debugPrint("characteristic not parceable \(charac)")
            }
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        debugPrint("didWriteValueForCharacteristic")
        sentCommand = nil
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        if( error == nil ) {
            print("Updated notification state for characteristic with UUID \(characteristic.uuid) on service with  UUID \(characteristic.service.uuid) on peripheral with UUID \(peripheral.identifier)")
            // Send notification that Bluetooth is connected and all required characteristics are discovered
            notifyAckCount+=1
            if notifyAckCount == notifyCount {
                connected = true
                debugPrint("connected to call chars, ready to do something else")
                //TODO: bonding stuff here
                keySent = true
                executeCommand(command: DeviceCommand.CodeWR, packet: CodePacket(code: 0x88))
            }
        } else {
            let realError = error as! NSError
            print("Error in setting notification state for characteristic with UUID \(characteristic.uuid) on service with  UUID \(characteristic.service.uuid) on peripheral with UUID \(peripheral.identifier)")
            print("Error code was \(realError.description)")
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverDescriptorsFor characteristic: CBCharacteristic, error: Error?) {
        debugPrint("didUpdateValueForDescriptor")
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor descriptor: CBDescriptor, error: Error?) {
        debugPrint("didUpdateValueForDescriptor")
    }

    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor descriptor: CBDescriptor, error: Error?) {
        debugPrint("didWriteValueForDescriptor")
    }

    private var threeWeeks = 60 * 60 * 24 * 7 * 3

    func withinThreeWeeks(timestamp: Double) -> Bool {
        return abs(NSDate().timeIntervalSince1970 - timestamp) <= Double(threeWeeks)
    }

    func handleUartResponse(bytes: [UInt8]) {
        if bytes.count < 2 {
            debugPrint("No idea what came back from the device bytes.count < 2")
            sendNack(reasonCode: NackError.DataLengthError.rawValue)
        }

        if self.deviceId.packetIdIn < 0 {
            deviceId.packetIdIn = Int(bytes[0])
        } else {
            deviceId.incPacketIdIn()
        }

        var dataPacket: DataPacket?

        do {
            dataPacket = try packetParserManager.parse(expectedPacketIdIn: self.deviceId.packetIdIn, packet: bytes)
            debugPrint("data packet = \(dataPacket)")
        } catch PacketErrors.DataLength {
            sendNack(reasonCode: NackError.DataLengthError.rawValue)
            print("Data Length")
        } catch PacketErrors.SequenceId {
            print("Sequence Id")
            sendNack(reasonCode: NackError.PacketSeqError.rawValue)
        } catch {
            print(error)
        }

        if dataPacket != nil {
            if dataPacket is RecordData {
                sendAck()

                let record: RecordData = dataPacket as! RecordData
                //fix the time
                if !stateMachine.isTimeCorrect() && !withinThreeWeeks(timestamp: record.timestamp) {
                    record.timestamp += stateMachine.timeDiff
                }

                parseRecord(record: record)
            }

            if stateMachine.getState() == DeviceLifecycle.SendLedCode4 && dataPacket is CodePacket {
                self.disconnectTimer?.invalidate()
                broadcastUpdate(message: .LedCodeDone)
                Thread.sleep(forTimeInterval: 1.0)
            }


            if stateMachine.getState() == DeviceLifecycle.QueryLock && dataPacket is CodePacket {
                debugPrint("Code received!!!")
                keySent = false
                Thread.sleep(forTimeInterval: 1.0)
                callLifecycleState()
            } else if stateMachine.getState() == .QueryLock && dataPacket is LockPacket {
                debugPrint("Start blink to link")

                stateMachine.processResult(data: dataPacket!)

                if stateMachine.isLedCodeNeeded() {
                    broadcastUpdate(message: .InputLedCode)
                } else {
                    broadcastUpdate(message: .LedCodeNotNeeded)
                }

                if stateMachine.getState().getCommand() != .Unknown {
                    callLifecycleState()
                }
            } else if stateMachine.getState() != DeviceLifecycle.Done {
                debugPrint("doing lifecycle thing")
                if dataPacket is CodePacket {
                    broadcastUpdate(message: .LedCodeAccepted)
                }

                stateMachine.processResult(data: dataPacket!)

                if stateMachine.getState().getCommand() != .Unknown {
                    callLifecycleState()
                }

                if stateMachine.getState() == .Done {
                    //executeCommand(.DeleteBond, packet: DeleteBondPacket())
                    deviceReady = true
                    broadcastUpdate(message: ClientMessages.DeviceReady)

                }
            }

            if dataPacket is Frame {
                broadcastUpdate(message: .Frame, thing: dataPacket!)
            }
        }
    }

    func parseRecord(record: RecordData) {
        let timezone = NSTimeZone.local.identifier
        switch record.reporter {
        case 0:
            debugPrint("Steps!!")

            var total: Int = 0
            var totalDistance: Double = 0

            for b in record.data {
                total += Int(b)

                if let helper = self.calculationHelper {
                    totalDistance += helper.calculateDistanceInMiles(stepCount: Int(b))
                }
            }
            
            // TODO: TimeAgeddon: recordId, deviceTimestamp and originalTimestamo added here
            let step: Step = Step(recordId: Int64(record.id), steps: total, timestamp: (record.timestamp*1000), deviceTimestamp: stateMachine.getDeviceTime(), originalTimestamp: record.originalTimestamp, timezone: timezone)

            if let helper = self.calculationHelper {
                debugPrint("calculationHelper is set!!!")
                step.mets = helper.calculateMets(stepTotal: total)
                step.activeBurn = helper.calculateActiveBurn(stepTotal: total)
                step.distance = totalDistance

                if step.mets > 1.38 {
                    debugPrint("active Step!!!")
                    step.activeTime = 1
                    userIsActive = true
                    inactiveCount = 0

                    if !wannaBeActiveSteps.isEmpty {
                        debugPrint("active so draining active steps")
                        self.activeTimeTimer?.invalidate()
                        activeTimeSet = false
                        let active: Bool = wannaBeActiveSteps.count < 3

                        for dubstep in wannaBeActiveSteps {
                            if active {
                                debugPrint("setting active steps to active")
                                dubstep.activeTime = 1
                            }

                            broadcastUpdate(message: .Step, thing: dubstep)
                        }

                        wannaBeActiveSteps = [Step]()
                    }

                    debugPrint("broadcasting current active step")
                    broadcastUpdate(message: .Step, thing: step)
                } else if userIsActive || inactiveCount > 0 {
                    debugPrint("inactive Step!!!")

                    if inactiveCount <= 3 {
                        wannaBeActiveSteps.append(step)
                    } else {
                        broadcastUpdate(message: .Step, thing: step)
                    }

                    if userIsActive && !activeTimeSet {
                        debugPrint("!!! setting timer")
                        activeTimeSet = true
                        setActiveTimeTimer()
                    }

                    userIsActive = false
                    inactiveCount += 1
                } else {
                    broadcastUpdate(message: .Step, thing: step)
                }
            } else {
                debugPrint("calculationHelper is NOT set")
                broadcastUpdate(message: .Step, thing: step)
            }
        case 1:
            var bytes = record.data
            broadcastUpdate(message: .BatteryReport, thing: BatteryReport(recordId: Int64(record.id), percent: Int(bytes[0]), volt: Int(BitsHelper.convertToUInt16(msb: bytes[2], lsb: bytes[1])), timestamp: record.timestamp, timezone: timezone))
        case 2:
            var counter: Int = 0
            var bytes: [UInt8] = record.data

            for i in stride(from: 0, to: bytes.count, by: 2) {
                let reading = BitsHelper.convertToUInt16(msb: bytes[i+1], lsb: bytes[i])
                let time = record.timestamp + Double(counter * 5)
                
                broadcastUpdate(message: .MotionData, thing: AccelFilt(recordId: Int64(record.id), timestamp: time, timezone: timezone, reading: Int(reading)))
                counter += 1
            }
        default:
            debugPrint("OH NO!, what reporter is this \(record.reporter)")
        }
    }

    func setActiveTimeTimer() {
        debugPrint("setActiveTimer called")
        let runLoop: RunLoop = RunLoop.main
        let fireDate: NSDate = NSDate(timeIntervalSinceNow: 190.0)
        self.activeTimeTimer = Timer(fireAt: fireDate as Date, interval: 0.1, target: self, selector: #selector(activeTimeTimerExpired), userInfo: nil, repeats: false)

        //self.activeTimeTimer = NSTimer.scheduledTimerWithTimeInterval(10.0, target: self, selector: #selector(activeTimeTimerExpired), userInfo: nil, repeats: false)

        runLoop.add(self.activeTimeTimer!, forMode: RunLoopMode.commonModes)
    }

    func activeTimeTimerExpired() {
        debugPrint("activeTimeTimerExpired")
        activeTimeSet = false
        if !wannaBeActiveSteps.isEmpty {
            debugPrint("draining wannaBeActiveSteps")
            for step in wannaBeActiveSteps {
                broadcastUpdate(message: .Step, thing: step)
            }

            wannaBeActiveSteps = [Step]()
        }
    }

    func callLifecycleState() {
        if stateMachine.getState().getCommand() != DeviceCommand.Unknown {
            var bytes: [UInt8] = [UInt8]()
            bytes.append(UInt8(deviceId.packetIdOut))
            bytes.append(UInt8(stateMachine.getState().getCommand().rawValue))

            if !stateMachine.getState().getPacket().isEmpty {
                bytes = bytes + stateMachine.getState().getPacket()
            }

            self.deviceId.incPacketIdOut()
            addCommand(command: BleCommand(readWrite: .Write, charac: .UartTx, bytes: bytes))
        }
    }

    //MARK: command stuff here
    func executeCommand(command: DeviceCommand, packet: DataPacket?) {
        debugPrint("executeCommand: \(command)")
        clientLockQueue.sync() {
            var bytes: [UInt8] = [UInt8]()

            bytes.append(UInt8(self.deviceId.packetIdOut))
            bytes.append(UInt8(command.rawValue))

            if let data = packet {
                let thing = data.getPacket()
                bytes = bytes + thing
            }

            self.deviceId.incPacketIdOut()

            self.addCommand(command: BleCommand(readWrite: ReadWrite.Write, charac: BleCharacteristics.UartTx, bytes: bytes))
        }
    }

    func addCommand(command: BleCommand) {
        //if connected {
            debugPrint("addCommand \(command.characteristic)")
            self.commandQueue.append(command)
        //}
    }

    func startCommandQueue() {
        self.queueRunning = true

        DispatchQueue.global(qos: .utility).async {
            while self.queueRunning {
                //print("command queue running \(self.sentCommand)")
                if !self.commandQueue.isEmpty && self.sentCommand == nil {
                    print("found command to execute")
                    self.sentCommand = self.commandQueue.removeFirst()

                    if self.sentCommand!.readOrWrite == ReadWrite.Read {
                        self.readCharacteristic(characteristic: self.sentCommand!.characteristic)
                    } else if self.sentCommand!.readOrWrite == ReadWrite.Write {
                        self.writeToCharacteristic(characteristic: self.sentCommand!.characteristic, data: self.sentCommand!.data)
                    }
                }

                //TODO: timeout or disconnect if no response
                if self.sentCommand != nil {

                }

                Thread.sleep(forTimeInterval: 0.5)

                //acks are fucked up and need to be cleared manually
                if self.sentCommand != nil {
                    if let data = self.sentCommand?.data {
                        if data.count > 1 && data[1] == UInt8(DeviceCommand.Ack.rawValue) {
                            self.sentCommand = nil
                        }
                    }
                }
            }
        }
    }

    func stopCommandQueue() {
        queueRunning = false
    }

    func writeToCharacteristic(characteristic: BleCharacteristics, data: [UInt8]) {
        print("writing characteristic \(data) \(characteristic)")
        if data.count > 0 {
            let packet: NSData = NSData(bytes: data, length: data.count)

            if characteristic == .UartTx || characteristic == .BatteryLevel {

                // Added to avoid crash while trying to write nil characteristic device
                guard let characteristicToWrite = self.characteristicMap[characteristic] else {
                  print("writing characteristic Error : \(characteristic)  NOT FOUND")
                  return
                }
                self.device!.writeValue(packet as Data, for: characteristicToWrite, type: .withoutResponse)

                if self.reboot {
                    Thread.sleep(forTimeInterval: 1.0)

                    if let central = self.centralManager {
                        central.cancelPeripheralConnection(self.device!)
                    }
                }
            }
            else {
                self.device!.writeValue(packet as Data, for: self.characteristicMap[characteristic]!, type: .withResponse)
            }
        }
    }

    func readCharacteristic(characteristic: BleCharacteristics) {
        print("read characteristic \(characteristic)")
        self.device!.readValue(for: self.characteristicMap[characteristic]!)
    }

    func sendAck() {
        let bytes: [UInt8] = [UInt8(self.deviceId.packetIdOut), UInt8(DeviceCommand.Ack.rawValue), UInt8(deviceId.packetIdIn)]

        debugPrint("sending ack \(bytes)")

        addCommand(command: BleCommand(readWrite: ReadWrite.Write, charac: BleCharacteristics.UartTx, bytes: bytes))
        deviceId.incPacketIdOut()
    }

    func sendNack(reasonCode: Int) {
        let bytes: [UInt8] = [UInt8(deviceId.packetIdOut), UInt8(DeviceCommand.Nack.rawValue), UInt8(reasonCode), UInt8(deviceId.packetIdIn)]

        addCommand(command: BleCommand(readWrite: ReadWrite.Write, charac: BleCharacteristics.UartTx, bytes: bytes))
        deviceId.incPacketIdOut()
    }

    func broadcastUpdate(message: ClientMessages) {
        debugPrint("broadcast update \(message)")
        if !clients.isEmpty {
            for client: DeviceObserverCallbacks in clients.values {
                switch message {
                case .DeviceReady:
                    client.onDeviceReady()
                case .InputLedCode:
                    client.onInputLedCode()
                case .LedCodeNotNeeded:
                    client.onLedCodeNotNeeded()
                case .LedCodeAccepted:
                    client.onLedCodeAccepted()
                case .LedCodeFailed:
                    client.onLedCodeFailed()
                case .LedCodeDone:
                    client.onLedCodeDone()
                case.BluetoothNotOn:
                    client.onBluetoothNotOn()
                case .DeviceDisconnect:
                    client.onDisconnect()
                case .BondError:
                    client.onBondError()
                case .ConnectionTimeout:
                    client.onConnectionTimeout()
                default:
                    debugPrint("OH NO!!! bad client message")
                }
            }
        }
    }

    func broadcastUpdate(message: ClientMessages, thing: NSObject) {
        debugPrint("broadcast update \(message)")
        if !clients.isEmpty {
            for client: DeviceObserverCallbacks in clients.values {
                switch message {
                case .Step:
                    client.onStep(step: thing as! Step)
                case .BatteryReport:
                    client.onBatteryReport(batteryReport: thing as! BatteryReport)
                case .MotionData:
                    client.onMotionData(accelFilt: thing as! AccelFilt)
                case .BatteryLevel:
                    client.onBatteryLevel(level: thing as! Int)
                case .BatteryState:
                    client.onBatteryState(state: BatteryState(rawValue: thing as! Int)!)
                case .Firmware:
                    client.onFirmwareVersion(firmwareVersion: thing as! String)
                case .Bootloader:
                  client.onBootloaderVersion(bootloaderVersion: thing as! String)
                case .Frame:
                    client.onFrame(frame: thing as! Frame)
                case .LastUserLocation:
                    client.onLastUserLocation(location: thing as! LastLocation)
                default:
                    debugPrint("OH NO!!! bad client message")
                }
            }
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {

        if let location = locations.last {
            print("got location")
            if NSDate().timeIntervalSince1970 - location.timestamp.timeIntervalSince1970 < 60 && location.horizontalAccuracy >= 0 &&
                location.horizontalAccuracy < 50 {

                print("location is good")
                counter += 1
                if counter % 3 == 0 {
                    print("pausing")
                    self.locationManager.allowDeferredLocationUpdates(untilTraveled: 10, timeout: 120)
                    NotificationCenter.default.post(name: NSNotification.Name(rawValue: "locationUpdaterThingieStatus"), object: self, userInfo: ["status": "pausing"])
                }

                if let savedLocation: CLLocation = BleManager.currentLocation {
                    print("there's a saved location: \(location.timestamp.timeIntervalSince1970) - \(savedLocation.timestamp.timeIntervalSince1970)")
                    print("\(location.horizontalAccuracy) - \(savedLocation.horizontalAccuracy)")
                    if location.timestamp.timeIntervalSince1970 - savedLocation.timestamp.timeIntervalSince1970 > 300 || //saved location is out of date by 5 min
                        (location.horizontalAccuracy < savedLocation.horizontalAccuracy) || //better accurracy
                        (location.coordinate.latitude != savedLocation.coordinate.latitude || location.coordinate.longitude != savedLocation.coordinate.longitude) || //user moved!
                        (location.timestamp.timeIntervalSince1970 >= savedLocation.timestamp.timeIntervalSince1970 && ( location.horizontalAccuracy < savedLocation.horizontalAccuracy  ||
                            abs(savedLocation.horizontalAccuracy - location.horizontalAccuracy) < 5 )) { //new location with better accuracy or close enough better accuracy
                        print("saving location")
                        BleManager.currentLocation = location
                        NotificationCenter.default.post(name: NSNotification.Name("locationUpdaterThingieStatus"), object: self, userInfo: ["status": "saved"])
                    }
                } else {
                    print("no saved location")
                    BleManager.currentLocation = location
                    NotificationCenter.default.post(name: NSNotification.Name("locationUpdaterThingieStatus"), object: self, userInfo: ["status": "1st save"])
                }
            }

            NotificationCenter.default.post(name: NSNotification.Name("locationUpdaterThingie"), object: self, userInfo: ["lat": location.coordinate.latitude, "long": location.coordinate.longitude, "accr": location.horizontalAccuracy,
                "timestamp": location.timestamp])
            print("\(counter) didUpdateLocations:  \(location.coordinate.latitude), \(location.coordinate.longitude), \(location.horizontalAccuracy)")
        }

    }

    func bootloaderFinished() {
        //need to try and find the device again
        startScan()

        self.clientBootloaderDelegate?.bootloaderFinished()
    }

    func bootloaderProgress(progress: Int) {
        self.clientBootloaderDelegate?.bootloaderProgress(progress: progress)
    }

    func bootloaderErrorOccured(errorCode: BootloaderError) {
        startScan()

        self.clientBootloaderDelegate?.bootloaderErrorOccured(errorCode: errorCode)
    }

}
