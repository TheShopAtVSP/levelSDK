//
//  DeviceClient.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

protocol DeviceObserverCallbacks {
    func onBluetoothNotAvailable()
    func onBluetoothNotOn()
    func onInputLedCode()
    func onLedCodeAccepted()
    func onLedCodeDone()
    func onLedCodeFailed()
    func onLedCodeNotNeeded()
    func onDeviceReady()
    func onConnectionTimeout()
    func onDisconnect()
    func onBondError()
    func onLastUserLocation(location: LastLocation)
    func onStep(step: Step)
    func onBatteryReport(batteryReport: BatteryReport)
    func onMotionData(accelFilt: AccelFilt)
    func onBatteryLevel(level: Int)
    func onBatteryState(state: BatteryState)
    func onFirmwareVersion(firmwareVersion: String)
    func onBootloaderVersion(bootloaderVersion: String)
    func onFrame(frame: Frame)
}

class DeviceClient: NSObject {
    var clientId: NSUUID
    var callbacks: DeviceObserverCallbacks?
    var bleManager: BleManager
  
    static let sharedInstance = DeviceClient()

    override init() {
        self.clientId = NSUUID()
        self.bleManager = BleManager.sharedInstance
    }

    func registerDeviceCallbacks(callbacks: DeviceObserverCallbacks) {
        self.callbacks = callbacks;
        bleManager.registerDeviceCallbacks(clientId: clientId, callbacks: callbacks);
    }

    func unregisterDeviceCallbacks() {
        bleManager.unregisterDeviceCallbacks(clientId: clientId)
    }
    
    func isConnected() -> Bool {
        return self.bleManager.isConnected()
    }

    func connect(frameId: String) {
        debugPrint("connect \(frameId) \(self.bleManager)")
        self.bleManager.connect(frameId: frameId)
    }

    func sendLedCode(code: Int) {
        debugPrint("ble manager is\(self.bleManager)")
        self.bleManager.sendLedCode(code: code)
    }
    
    func deviceLightsNotOn() {
        self.bleManager.deviceLightsNotOn()
    }
    
    func deleteKey() {
        self.bleManager.deleteSavedKey()
    }

    func setUser(user: LevelUser) {
        debugPrint("deviceClient setUser")
        self.bleManager.setUser(user: user)
    }

    func getFirmwareVersion() {
        self.bleManager.getFirmwareVersion()
    }
  
    func getBootloaderVersion() {
      self.bleManager.getBootloaderVersion()
    }

    func getBatteryLevel() {
        self.bleManager.getBatteryLevel()
    }

    func getBatteryState() {
        self.bleManager.getBatteryState()
    }
  
    func setTransmitControlToOn() {
      self.bleManager.setTransmitControlToOn()
    }
  
    func getFrame() {
        self.bleManager.getFrameInfo()
    }

    //TODO: what the fuck is a file in swift?
    func startBootloader(firmwareFile: NSURL, delegate: BootloaderDelegate) {
        self.bleManager.startBootloader(firmwareFile: firmwareFile, delegate: delegate)
    }
}
