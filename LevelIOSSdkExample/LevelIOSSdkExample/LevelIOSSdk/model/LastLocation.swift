//
//  LastLocation.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class LastLocation: NSObject {
    var latitude: Double
    var longitude: Double
    var accuracy: Double
    var altitude: Double
    var timezone: String
    var glassName: String
    var lastTimestamp: Double
  
  init(lat: Double, long: Double, accuracy: Double, alt: Double, glassName: String, timezone: String) {
        self.latitude = lat
        self.longitude = long
        self.accuracy = accuracy
        self.altitude = alt
        self.timezone = timezone
        self.glassName = glassName
        self.lastTimestamp = NSDate().timeIntervalSince1970
    }
}