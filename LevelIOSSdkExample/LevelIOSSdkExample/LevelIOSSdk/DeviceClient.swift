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
    func onSetUpComplete()
    func onSetUpFailed(error: ReporterError)
    func onReporterQueried(config: ReporterConfig)
    func onReportersEnabled(reporters: [ReporterType])
    func onData(data: RecordData)
    func onDataDeleted()
    func onConnectionTimeout()
    func onDisconnect()
    func onBondError()
    func onBatteryLevel(level: Int)
    func onBatteryState(state: BatteryState)
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

    func getBatteryLevel() {
        self.bleManager.getBatteryLevel()
    }

    func getBatteryState() {
        self.bleManager.getBatteryState()
    }
    
    func queryReporter(reporter: ReporterType) {
        self.bleManager.queryReporter(reporter: reporter)
    }
    
    func setUpReporter(config: ReporterConfig) {
        self.bleManager.configureReporter(config: config)
    }
    
    func enableReporter(reporter: ReporterType) {
        self.bleManager.enableReporter(reporter: reporter)
    }
    
    func disableReporter(reporter: ReporterType) {
        self.bleManager.disableReporter(reporter: reporter)
    }
    
    func pauseDataStream() {
        self.bleManager.pauseData()
    }
    
    func enableDataStream() {
        self.bleManager.enableData()
    }
    
    func deleteAllStoredData() {
        self.bleManager.deleteAllData()
    }
}
