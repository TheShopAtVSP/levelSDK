//
//  DataPacket.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DataPacket: NSObject {
    var id: Int
    var reporter: Int
    var reportControl: Int
    var command: DeviceCommand
    var received: NSDate
    
    override init() {
        self.id = -1
        self.reporter = -1
        self.reportControl = -1
        self.command = .Nack
        received = NSDate()
    }
    
    convenience init(reportControl: Int) {
        self.init()
        self.reportControl = reportControl
    }
    
    func getPacket() -> [UInt8] {
        return [UInt8]()
    }
}