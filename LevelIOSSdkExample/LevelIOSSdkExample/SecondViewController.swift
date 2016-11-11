//
//  SecondViewController.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 10/28/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import UIKit

class SecondViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource, DeviceObserverCallbacks {
    @IBOutlet weak var sampleFreqText: UITextField!
    @IBOutlet weak var samplesPerRecText: UITextField!
    @IBOutlet weak var maxRecordsText: UITextField!
    @IBOutlet weak var stepsToggle: UISwitch!
    @IBOutlet weak var accelToggle: UISwitch!
    @IBOutlet weak var gyroToggle: UISwitch!
    @IBOutlet weak var xToggle: UISwitch!
    @IBOutlet weak var yToggle: UISwitch!
    @IBOutlet weak var zToggle: UISwitch!
    @IBOutlet weak var magToggle: UISwitch!
    
    @IBOutlet weak var reporterSpinner: UIPickerView!
    @IBOutlet weak var actionSpinner: UIPickerView!
    @IBOutlet weak var enableDataButton: UIButton!
    
    var reporterData: [String] = [String]()
    var actionData: [String] = [String]()
    var dataEnabled: Bool = false
    var deviceClient: DeviceClient
    
    required init?(coder aDecoder: NSCoder) {
        self.deviceClient = DeviceClient()
        
        super.init(coder: aDecoder)
        
        self.deviceClient.registerDeviceCallbacks(callbacks: self)
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        reporterData = ["Steps", "Gyro", "Accel"]
        actionData = ["Enable", "Disable", "Query"]
        
        self.reporterSpinner.delegate = self
        self.actionSpinner.delegate = self
        
        xToggle.setOn(false, animated: false)
        yToggle.setOn(false, animated: false)
        zToggle.setOn(false, animated: false)
        stepsToggle.setOn(false, animated: false)
        accelToggle.setOn(false, animated: false)
        gyroToggle.setOn(false, animated: false)
        magToggle.setOn(false, animated: false)
        
        if !deviceClient.isConnected() {
            debugPrint("NOT CONNECTED!!! WTF")
            deviceClient.connect(frameId: "")
        }
        
        let tap: UITapGestureRecognizer = UITapGestureRecognizer(target: self, action: #selector(SecondViewController.dismissKeyboard))
        
        view.addGestureRecognizer(tap)
    }
    
    func dismissKeyboard() {
        //Causes the view (or one of its embedded text fields) to resign the first responder status.
        view.endEditing(true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func stepsToggled(_ sender: Any) {
        debugPrint("stepsToggled \(stepsToggle.isOn)")
        
        if stepsToggle.isOn {
            if accelToggle.isOn {
                accelToggle.setOn(false, animated: true)
            }
            
            if gyroToggle.isOn {
                gyroToggle.setOn(false, animated: true)
            }
        }
    }
    
    @IBAction func accelToggled(_ sender: Any) {
        if accelToggle.isOn {
            if stepsToggle.isOn {
                stepsToggle.setOn(false, animated: true)
            }
            
            if gyroToggle.isOn {
                gyroToggle.setOn(false, animated: true)
            }
        }
    }
    
    @IBAction func gyroToggled(_ sender: Any) {
        if gyroToggle.isOn {
            if stepsToggle.isOn {
                stepsToggle.setOn(false, animated: true)
            }
            
            if accelToggle.isOn {
                accelToggle.setOn(false, animated: true)
            }
        }
        
    }

    @IBAction func setUpReporterClicked(_ sender: Any) {
        debugPrint("setUpReporterClicked 1 \(xToggle.isOn) 2 \(yToggle.isOn) 3 \(zToggle.isOn) 4 \(magToggle.isOn) 5 \(stepsToggle.isOn) 6 \(accelToggle.isOn) 7 \(gyroToggle.isOn) 8 \(sampleFreqText.text)")
        debugPrint("10 \(samplesPerRecText.text) 11 \(maxRecordsText.text)")
        
        let builder: ReporterConfigBuilder = ReporterConfigBuilder()
        
        if stepsToggle.isOn {
            builder.step()
        }
        
        if accelToggle.isOn {
            builder.accel()
        }
        
        if gyroToggle.isOn {
            builder.gyro()
        }
        
        builder.samplingFrequency = Int(sampleFreqText.text!)!
        builder.samplesPerRecord = Int(samplesPerRecText.text!)!
        builder.maxNumberOfRecords = Int(maxRecordsText.text!)!
        builder.dependentDataScale = DependentDataScale.OneToOneBit
        builder.dataFields = [DataFields]()
        
        if xToggle.isOn {
            builder.dataFields.append(DataFields.IncludeXAxis)
        }
        
        if yToggle.isOn {
            builder.dataFields.append(DataFields.IncludeYAxis)
        }
        
        if zToggle.isOn {
            builder.dataFields.append(DataFields.IncludeZAxis)
        }
        
        if magToggle.isOn {
            builder.dataFields.append(DataFields.IncludeMagnitude)
        }
        
        deviceClient.setUpReporter(config: builder.build())
        
    }
    @IBAction func queryEnabledClicked(_ sender: Any) {
        deviceClient.queryEnabledReporters()
    }
    
    // The number of columns of data
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    // The number of rows of data
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        if pickerView.tag == 0 {
            return reporterData.count
        }
        
        return actionData.count
    }
    
    // The data to return for the row and component (column) that's being passed in
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        if pickerView.tag == 0 {
            return reporterData[row]
        }
        
        return actionData[row]
    }
    @IBAction func doAThingClicked(_ sender: Any) {
        debugPrint("doAThingClicked: \(reporterSpinner.selectedRow(inComponent: 0)) - \(actionSpinner.selectedRow(inComponent: 0))")
        let reporterToUse = ReporterType(rawValue: reporterSpinner.selectedRow(inComponent: 0) )
        let actionToDo = actionSpinner.selectedRow(inComponent: 0)
        
        switch actionToDo {
        case 0: //enable
            deviceClient.enableReporter(reporter: reporterToUse!)
        case 1: //disable
            deviceClient.disableReporter(reporter: reporterToUse!)
        case 2: //query
            deviceClient.queryReporter(reporter: reporterToUse!)
        default:
            debugPrint("doAThing default case :(")
        }
    }
    
    @IBAction func enableDataClicked(_ sender: Any) {
        if dataEnabled {
            deviceClient.pauseDataStream()
            enableDataButton.setTitle("Enable Data", for: .normal)
            dataEnabled = false
        } else {
            deviceClient.enableDataStream()
            enableDataButton.setTitle("Pause Data", for: .normal)
            dataEnabled = true
        }
        
    }
    
    @IBAction func deleteAllDataClicked(_ sender: Any) {
        deviceClient.deleteAllStoredData()
    }
    
    
    
    func onBluetoothNotAvailable() {
        debugPrint("callback: onBluetoothNotAvailable")
    }
    
    func onBluetoothNotOn() {
        debugPrint("callback: onBluetoothNotOn")
    }
    
    func onInputLedCode() {
        debugPrint("callback: onInputLedCode")
    }
    
    func onLedCodeAccepted() {
        debugPrint("callback: onLedCodeAccepted")
    }
    
    func onLedCodeDone() {
        debugPrint("callback: onLedCodeDone")
    }
    
    func onLedCodeFailed() {
        debugPrint("callback: onLedCodeFailed")
    }
    
    func onLedCodeNotNeeded() {
        debugPrint("callback: onLedCodeNotNeeded")
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
    
    func onBatteryLevel(level: Int) {
        debugPrint("callback: onBatteryLevel")
    }
    
    func onBatteryState(state: BatteryState) {
        debugPrint("callback: onBatteryState")
    }
    
    func onSetUpComplete() {
        debugPrint("callback: onSetUpComplete")
    }
    
    func onSetUpFailed(error: ReporterError) {
        debugPrint("callback: onSetUpFailed \(error)")
    }
    
    func onReporterQueried(config: ReporterConfig) {
        debugPrint("callback: onReporterQueried")
    }
    
    func onReportersEnabled(reporters: [ReporterType]) {
        debugPrint("callback: onReportersEnabled")
    }
    
    func onData(data: RecordData) {
        debugPrint("callback: onData")
    }
    
    func onDataStreamEnabled(currentRecordCount: Int) {
        debugPrint("callback: onDataStreamEnabled: \(currentRecordCount)")
    }
    
    func onDataDeleted() {
        debugPrint("callback: onDataDeleted")
    }
}

