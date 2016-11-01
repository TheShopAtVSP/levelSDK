//
//  DeviceIdManager.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/28/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DeviceIdManager {
    var packetIdOut: Int = 0
    var packetIdIn: Int = -1
    
    func incPacketIdOut() {
        self.packetIdOut += 1
        
        if self.packetIdOut > 255 {
            self.packetIdOut = 0
        }
    }
    
    func incPacketIdIn() {
        self.packetIdIn += 1
        
        if self.packetIdIn > 255 {
            self.packetIdIn = 0
        }
    }
    
    func reset() {
        self.packetIdIn = -1
        self.packetIdOut = 0
    }
}