//
//  TimePacket.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/1/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class TimePacket: DataPacket {
    var timestamp: Double
    
    override init() {
        self.timestamp = 0
        
        super.init()
    }
    
    convenience init(bytes: [UInt8]) {
        self.init()
        
        timestamp = Double(BitsHelper.convertToUInt32(bytes: [UInt8](bytes[2...(bytes.count-1)])))
    }
    
    override func getPacket() -> [UInt8] {
        return BitsHelper.convertTo4Bytes(time: self.timestamp)
    }
    
}
