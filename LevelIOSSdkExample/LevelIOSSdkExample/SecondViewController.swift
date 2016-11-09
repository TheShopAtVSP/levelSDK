//
//  SecondViewController.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 10/28/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import UIKit

class SecondViewController: UIViewController, UIPickerViewDelegate, UIPickerViewDataSource {
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
    
    var reporterData: [String] = [String]()
    var actionData: [String] = [String]()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        reporterData = ["Steps", "Gyro", "Accel"]
        actionData = ["Enable", "Disable", "Query"]
        
        self.reporterSpinner.delegate = self
        self.actionSpinner.delegate = self
        
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    @IBAction func setUpReporterClicked(_ sender: Any) {
    }
    @IBAction func queryEnabledClicked(_ sender: Any) {
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

}

