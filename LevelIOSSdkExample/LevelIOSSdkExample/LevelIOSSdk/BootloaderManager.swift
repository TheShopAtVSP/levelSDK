//
//  BootloaderManager.swift
//  iosLevelSDK
//
//  Created by Gabriel Helman on 6/22/16.
//  Copyright © 2016 The Shop. All rights reserved.
//

import Foundation
import CoreBluetooth
import iOSDFULibrary

/**
 Holds our various service UUIDs for the level device so we can scan for the right thing.
 */
enum ServiceUUID: String {
    case DEVICE_INFO_UUID = "0000180a-0000-1000-8000-00805f9b34fb", UART_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e", DFU_UUID = "00001530-1212-efde-1523-785feabcd123"
}

/**
 Encapsulates the various errors that the bootloader can run into.  The names should be reasonably self-explanatory.
 */
public enum BootloaderError {
    case NoDeviceFound
    case UnableToConnectToDevice
    case BluetoothPoweredOff
    case BluetoothUnsupported
    case BluetoothUnauthorized
    case TooManyRetries
    /**
     Something bad happened inside Nordic's DFU driver.  Probably this means to reboot and try again.
     However, the actual error returned by the driver is included, if you're feeling sassy and want to try
     something fancy.
     */
    case FailureWithDFU(DFUError)
    
    
}


/**
 The delegate of the BootloaderManager must adopt the `BootloaderDelegate` protocol.  The delegate can use these
 methods to monitor how the bootloading process is going, and be notified when the load is finsihed.
 
 There are no optional methods, because we don't play that way.
 
 */

public protocol BootloaderDelegate {
    
    /**
     Called when the load had finished, successfully or not.
     */
    func bootloaderFinished()
    
    /**
     Updates the delegate with the progress of the load.
     
     - Parameter progress: An int from 0 to 100 indicating how the load is going.  A value of 100 does NOT mean the load is done.  It means its "almost" done.
     
     */
    func bootloaderProgress(progress: Int)
    
    
    /**
     Did something break? This will let you know about it.
     */
    func bootloaderErrorOccured(errorCode: BootloaderError)
    
    
}


/**
 Manages the bootloading.
 Implements, like, 10 zillion protocols, because why not?
 
 
 Ideally, calling code should be able to create one of these, call `start(...)` and wait for the callback that it's done.
 
 
 From the outside, you shouldn't need to call anything buy .start() and .cancel().
 
 */
class BootloaderManager: NSObject, CBCentralManagerDelegate, CBPeripheralDelegate, LoggerDelegate, DFUServiceDelegate, DFUProgressDelegate {
    
    private var 👓:CBPeripheral?  //the peripheral is the pair of Level Glasses
    private var 📱:CBCentralManager? //the central is the user's phone
    private var controller: DFUServiceController?
    //this is the entry point for the nordic code
    
    
    var firmwareURL: NSURL?
    
    var delegate: BootloaderDelegate?
    
    private var discovered = [Double: CBPeripheral]()
    
    private let numberOfTimesToRetry : Int
    private var numberOfAttempts : Int
    private var connected : Bool
    
    
    // MARK: the public `interface`
    
    /**
     Init the bootloader-er.  You really should provide a delegate.
     */
    init(delegate: BootloaderDelegate) {
        self.delegate = delegate
        numberOfAttempts = 0
        numberOfTimesToRetry = 2
        connected = false
    }
    
    
    
    /**
     The entry point for firing up the bootloader process.
     
     The outside world is in charge of telling this process where the firmware zip file is, and then this takes over.
     
     - Parameter firmware: a URL to a zip file of firmware to update.
     
     */
    func start(firmware: NSURL) {
        NSLog("Let's do this thing")
        self.firmwareURL = firmware
        //reset everything, because we're staring over
        self.numberOfAttempts = 0
        self.👓 = nil
        self.discovered.removeAll()
        
        
        self.📱 = CBCentralManager(delegate: self, queue: nil)
        if let cm = self.📱 {
            NSLog("cm got created")
            print(cm.state)
        }
    }
    
    
    /**
     Change your mind about this whole "new firmware" thing?  Call this, and everything should stop.
     
     */
    func cancel() {
        self.stopScanning()
        guard let cm = self.📱
            else {return}
        if let d = self.👓{
            cm.cancelPeripheralConnection(d)
            NSLog("cancelled connection")
        }
        discovered.removeAll()
        self.connected = false
    }
    
    
    
    
    
    
    // MARK: internal control methods
    
    /**
     Clears out all discovered devices.
     */
    func clearDevices(){
        self.discovered.removeAll()
        self.👓 = nil
        
    }
    
    /**
     Begins the updating process.
     
     Does the following:
     
     - Starts scanning for devices in DFU mode.  Add all discovered DFU devices to a list.
     - After five seconds, stop scanning.
     - If more than one device is found, take the one with the lowest (therefore closest) RSSI value
     - Try to connect to that device
     - When connected, hand off to the Nordic DFU library to update the firmware.
     
     This should not be called directly; this is triggered by the bluetooth central manager registering
     a "Power On" state change.
     
     */
    func startScan() {
        guard let cm = self.📱
            else {return}
        
        if (cm.state != .PoweredOn) {
            NSLog("CoreBluetooth not correctly initialized !\r\n")
            BleManager.sharedInstance.broadcastUpdate(ClientMessages.BluetoothNotOn)
            return
        }
        
        self.👓 = nil
        
        NSLog("beginning scan")
        cm.scanForPeripheralsWithServices(nil, options: nil)
        
        let dfu = CBUUID(string: ServiceUUID.DFU_UUID.rawValue)
        cm.scanForPeripheralsWithServices([dfu], options: nil)
        
        /*
         cm.scanForPeripheralsWithServices([CBUUID(string: ServiceUUID.UART_UUID.rawValue),
         CBUUID(string: ServiceUUID.DEVICE_INFO_UUID.rawValue),
         CBUUID(string: ServiceUUID.DFU_UUID.rawValue),
         ], options: nil)
         */
        
        
        //start a timer; in x seconds go look at the list of discovered gizmos and do something
        self.delay(5.0) {
            guard let cm = self.📱
                else {return}
            
            self.stopScanning()
            
            if(self.discovered.count == 0){
                self.reportError(BootloaderError.NoDeviceFound )
                return
            }
            
            let discoveredRssiValues = Array(self.discovered.keys)
            
            if let peripheral = self.discovered[self.findClosestRssi(discoveredRssiValues)] {
                self.👓 = peripheral;
                NSLog("Okay, let's rock...")
                cm.connectPeripheral(peripheral, options: nil)
                
                //we need a timeout here in case we can't connect, otherwise the iphone will just keep trying forever
                self.delay(5.0){
                    if(self.connected == false){
                        NSLog("Looks like we failed to connect")
                        self.cancel()
                        self.reportError(BootloaderError.UnableToConnectToDevice)
                    }
                }
                
                
                
            } else {
                NSLog("Selected RSSI not found in the list of discovered devices, so that's bad.")
                self.reportError(BootloaderError.NoDeviceFound)
            }
            
        }
    }
    
    /**
     Takes the list of rssis discovered by the central mananger and finds the closest one
     */
    func findClosestRssi(rssis: [Double]) -> Double{
        let sorted = rssis.sort(>)
        NSLog("selecting device with this rssi: \(sorted.first)" )
        return sorted.first!
    }
    
    
    
    /**
     Fires a closure after a specified delay.  Thanks, Stack Overflow!
     */
    func delay(delay: Double, closure: () -> ()) {
        dispatch_after(
            dispatch_time(
                DISPATCH_TIME_NOW,
                Int64(delay * Double(NSEC_PER_SEC))
            ),
            dispatch_get_main_queue(), closure)
    }
    
    
    /**
     Quits scanning for bluetooth devices.
     */
    func stopScanning() {
        NSLog("Stopping the scan")
        guard let cm = self.📱
            else {return}
        
        cm.stopScan()
        
    }
    
    /**
     Central place to log errors and tell our delegate about them.
     */
    func reportError(errorCode: BootloaderError){
        NSLog("We had an error, bootloaderError= \(errorCode)" )
        guard let d = self.delegate
            else{return}
        
        d.bootloaderErrorOccured(errorCode)
        
        self.cancel()
    }
    
    
    //MARK: from CBCentralManagerDelegate
    
    /**
     Delegate method called when the central manager's state changes---the main job of this method is to start the scanning
     and updating process when bluetooth powers on.
     */
    func centralManagerDidUpdateState(central: CBCentralManager) {
        
        NSLog("update state \(central.state)  \(central.state.rawValue)")
        switch (central.state) {
        case CBCentralManagerState.PoweredOff:
            NSLog("CM: Powered off")
            self.reportError(.BluetoothPoweredOff)
            
            
        case CBCentralManagerState.Unauthorized:
            NSLog("CM: Unauthorized")
            self.reportError(.BluetoothUnauthorized)
            
            
        case CBCentralManagerState.Unknown:
            NSLog("CM: unknown")
            // Wait for another event
            break
            
        case CBCentralManagerState.PoweredOn:
            NSLog("CM: Powered On")
            self.startScan()
            
            
        case CBCentralManagerState.Resetting:
            NSLog("CM: Reset")
            self.clearDevices()
            
        case CBCentralManagerState.Unsupported:
            NSLog("CM: unsupported")
            self.reportError(.BluetoothUnsupported)
            
        }
        
    }
    
    
    /**
     When a device is discovered, add it to the list of discovered devices (And nothing else, yet.)
     */
    func centralManager(central: CBCentralManager, didDiscoverPeripheral peripheral: CBPeripheral, advertisementData: [String:AnyObject], RSSI: NSNumber) {
        
        NSLog("didDiscoverPeripheral \(peripheral) \(advertisementData) \(RSSI)" )
        discovered[Double(RSSI)] = peripheral
    }
    
    
    /**
     Do some cleanup when the device disconnects.
     */
    func centralManager(central: CBCentralManager, didConnectPeripheral peripheral: CBPeripheral) {
        self.👓 = peripheral
        discovered.removeAll()
        self.connected = true
        letsSeeIfThisWorks()
        self.stopScanning() //Hang on, do we need this?  I think we do?
    }
    
    /**
     On a failure, report an error back out.
     */
    func centralManager(central: CBCentralManager, didFailToConnectPeripheral peripheral: CBPeripheral, error: NSError?) {
        NSLog("Could not connect \(error)")
        self.reportError(.UnableToConnectToDevice)
        
    }
    
    
    //MARK: down here deals with updating the device itself
    
    
    /**
     Having found a device, let's update that sucker.  You should probably let the connected to device delegate method call this.
     */
    func letsSeeIfThisWorks() {
        guard let cm = 📱
            else{return}
        guard let d = 👓
            else{return}
        
        guard let url = self.firmwareURL
            else {return}
        
        if(numberOfAttempts >= numberOfTimesToRetry){
            self.reportError(.TooManyRetries)
        }
        numberOfAttempts += 1
        
        let selectedFirmware = DFUFirmware(urlToZipFile: url)
        
        if let firm = selectedFirmware {
            NSLog("firmware: \(selectedFirmware!.fileName) \(selectedFirmware!.debugDescription)")
            
            //now we're really in trouble, Nordic, I hope you guys can program
            let initiator = DFUServiceInitiator(centralManager: cm, target: d).withFirmwareFile(firm)
            
            // Optional:
            // initiator.forceDfu = true/false; // default false
            // initiator.packetReceiptNotificationParameter = N; // default is 12
            initiator.logger = self; // - to get log info
            initiator.delegate = self; // - to be informed about current state and errors
            initiator.progressDelegate = self; // - to show progress bar
            // initiator.peripheralSelector = ... // the default selector is used
            NSLog("Starting update... NOW")
            controller = initiator.start()
        }
    }
    
    //MARK: nordic logger delegate
    
    /**
     We're going to just pass Nordic's log messages right along.
     We're also going to ignore log levels for the moment, because that's how we roll.
     */
    func logWith(level: LogLevel, message: String) {
        NSLog("Log message from the DFU: %@", message)
    }
    
    //MARK: nordic service delegate
    
    func didStateChangedTo(state: State) {
        NSLog("DFU state is now: ")
        
        switch state {
        case .Connecting:
            NSLog("DFU: connecting")
        case .Starting:
            NSLog("DFU: Starting")
        case .EnablingDfuMode:
            NSLog("DFU: EnablingDfuMode")
        case .Uploading:
            NSLog("DFU: Uploading")
        case .Validating:
            NSLog("DFU: Validating")
        case .Disconnecting:
            NSLog("DFU: Disconnecting")
        case .Completed:
            NSLog("DFU: Completed")
            self.cancel()
            self.delegate?.bootloaderFinished()
        case .Aborted:
            NSLog("DFU: Aborted")
        }
        
        
    }
    
    func didErrorOccur(error: DFUError, withMessage message: String) {
        NSLog("Error! \(error) \(message)")
        
        //RemoteInvalidState - retry on this one
        
        switch error{
        case .RemoteInvalidState:
            NSLog("retry case \(numberOfAttempts) \(numberOfTimesToRetry)")
            //retry
            if(numberOfAttempts < numberOfTimesToRetry){
                NSLog("Let's do a retry!")
                letsSeeIfThisWorks()
            } else {
                NSLog("Do, we're not going to keep trying.")
                self.reportError(.FailureWithDFU(error))
            }
        default:
            NSLog("report and bail")
            //report an error and bail
            self.reportError(.FailureWithDFU(error))
            
        }
        
        
    }
    
    //MARK: nordic progress delegate
    
    func onUploadProgress(part: Int, totalParts: Int, progress: Int, currentSpeedBytesPerSecond: Double, avgSpeedBytesPerSecond: Double) {
        
        NSLog("Progress! ", part, totalParts, progress)
        guard let delegate = self.delegate
            else {return}
        delegate.bootloaderProgress(progress)
        
    }
    
    
}

