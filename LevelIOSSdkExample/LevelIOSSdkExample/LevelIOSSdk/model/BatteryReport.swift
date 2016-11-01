//
//  BatteryReport.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class BatteryReport: NSObject {
    var recordId: Int64
    var percentage: Int
    var voltage: Int
    var timestamp: String
    var timezone: String
    
    init(recordId: Int64, percent: Int, volt: Int, timestamp: Double, timezone: String) {
        self.recordId = recordId
        self.percentage = percent
        self.voltage = volt
        self.timestamp = NSDate(timeIntervalSince1970:timestamp).toString(format: DateFormat.Custom("yyyy-MM-dd'T'HH:mm:ss.SSS"))
        self.timezone = timezone
    }
}