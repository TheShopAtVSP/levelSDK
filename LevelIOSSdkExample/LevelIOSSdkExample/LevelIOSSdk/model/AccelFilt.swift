//
//  AccelFilt.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class AccelFilt: NSObject {
    var recordId: Int64
    var timestamp: Double
    var timezone: String
    var reading: Int
    
  init(recordId: Int64, timestamp: Double, timezone: String, reading: Int) {
        self.recordId = recordId
        self.timestamp = timestamp
        self.timezone = timezone
        self.reading = reading
    }
}
