//
//  Step.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/1/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class Step: NSObject {
    var recordId: Int64
    var steps: Int
    var mets: Double
    var timestamp: Double
    var deviceTimestamp: Double
    var originalTimestamp: Double
    var timeZone: String
    var activeBurn: Double
    var activeTime: Int
    var distance: Double
    
    init(recordId: Int64, steps: Int, timestamp: Double, deviceTimestamp: Double, originalTimestamp: Double, timezone: String) {
        self.recordId = recordId
        self.steps = steps
        self.timestamp = timestamp
        self.deviceTimestamp = deviceTimestamp
        self.originalTimestamp = originalTimestamp
        self.timeZone = timezone
        self.mets = 0
        self.activeBurn = 0
        self.activeTime = 0
        self.distance = 0
    }
}
