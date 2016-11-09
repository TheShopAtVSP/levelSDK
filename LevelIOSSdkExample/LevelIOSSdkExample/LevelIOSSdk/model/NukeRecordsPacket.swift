//
//  NukeRecordsPacket.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/4/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class NukeRecordsPacket: DataPacket {
    var payload: Int
    
    override init() {
        self.payload = 0
        
        super.init()
    }
    
    convenience init(payload: Int) {
        self.init()
        
        self.payload = payload
    }
    
    convenience init(bytes: [UInt8]) {
        self.init()
        
        self.payload = Int(bytes[0])
    }
    
    override func getPacket() -> [UInt8] {
        var bytes = [UInt8]()
        
        bytes.append(UInt8(22))
        
        return bytes
    }
}
