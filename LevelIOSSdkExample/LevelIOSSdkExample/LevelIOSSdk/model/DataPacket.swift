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
    var nackError: NackError
    var subError: ReporterError
    
    override init() {
        self.id = -1
        self.reporter = -1
        self.reportControl = -1
        self.command = .Nack
        received = NSDate()
        self.nackError = .None
        subError = .NoError
    }
    
    convenience init(reportControl: Int) {
        self.init()
        self.reportControl = reportControl
    }
    
    func getPacket() -> [UInt8] {
        var bytes = [UInt8]()
        bytes.append(UInt8(self.reportControl))
        
        return bytes
    }
}
