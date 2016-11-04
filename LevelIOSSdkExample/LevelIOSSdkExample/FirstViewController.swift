//
//  FirstViewController.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 10/28/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import UIKit

class FirstViewController: UIViewController, DeviceObserverCallbacks {
    var deviceClient: DeviceClient?

    @IBOutlet weak var statusLabel: UILabel!
    
    required init?(coder aDecoder: NSCoder) {
        super.init(coder: aDecoder)
        
        self.deviceClient = DeviceClient()
        self.deviceClient?.registerDeviceCallbacks(callbacks: self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        self.deviceClient?.connect(frameId: "")
        
        self.statusLabel.text = "Connecting please wait..."
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func whiteButtonClicked(_ sender: Any) {
        statusLabel.text = statusLabel.text! + " White"
        deviceClient!.sendLedCode(code: 0)
    }

    @IBAction func redButtonClicked(_ sender: Any) {
        statusLabel.text = statusLabel.text! + " Red"
        deviceClient!.sendLedCode(code: 2)
    }
    @IBAction func purpleButtonClicked(_ sender: Any) {
        statusLabel.text = statusLabel.text! + " Purple"
        deviceClient!.sendLedCode(code: 1)
    }
    @IBAction func yellowButtonClicked(_ sender: Any) {
        statusLabel.text = statusLabel.text! + " Yellow"
        deviceClient!.sendLedCode(code: 3)
    }
    
    func onBluetoothNotAvailable() {
        debugPrint("callback: onBluetoothNotAvailable")
    }
    
    func onBluetoothNotOn() {
        debugPrint("callback: onBluetoothNotOn")
    }
    
    func onInputLedCode() {
        debugPrint("callback: onInputLedCode")
        DispatchQueue.main.sync {
            self.statusLabel.text = "Connected! Input the Led Code"
        }
    }
    
    func onLedCodeAccepted() {
        debugPrint("callback: onLedCodeAccepted")
    }
    
    func onLedCodeDone() {
        debugPrint("callback: onLedCodeDone")
        
        DispatchQueue.main.sync {
            self.tabBarController?.selectedIndex = 1
        }
    }
    
    func onLedCodeFailed() {
        debugPrint("callback: onLedCodeFailed")
    }
    
    func onLedCodeNotNeeded() {
        debugPrint("callback: onLedCodeNotNeeded")
        
        DispatchQueue.main.sync {
            self.tabBarController?.selectedIndex = 1
        }
    }
    
    func onDeviceReady() {
        debugPrint("callback: onDeviceReady")
    }
    
    func onConnectionTimeout() {
        debugPrint("callback: onConnectionTimeout")
    }
    
    func onDisconnect() {
        debugPrint("callback: onDisconnect")
    }
    func onBondError() {
        debugPrint("callback: onBondError")
    }
    func onLastUserLocation(location: LastLocation) {
        debugPrint("callback: onLastUserLocation")
    }
    
    func onStep(step: Step) {
        debugPrint("callback: onStep")
    }
    
    func onBatteryReport(batteryReport: BatteryReport) {
        debugPrint("callback: onBatteryReport")
    }
    func onMotionData(accelFilt: AccelFilt) {
        debugPrint("callback: onMotionData")
    }
    
    func onBatteryLevel(level: Int) {
        debugPrint("callback: onBatteryLevel")
    }
    func onBatteryState(state: BatteryState) {
        debugPrint("callback: onBatteryState")
    }
    
    func onFirmwareVersion(firmwareVersion: String) {
        debugPrint("callback: onFirmwareVersion")
    }
    
    func onBootloaderVersion(bootloaderVersion: String) {
        debugPrint("callback: onBootloaderVersion")
    }
    
    func onFrame(frame: Frame) {
        debugPrint("callback: onFrame")
    }

}

