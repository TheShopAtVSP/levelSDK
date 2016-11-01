//
//  LockPacket.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/5/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class LockPacket: DataPacket {
    var lock: Int
    
    override init() {
        self.lock = 0
        super.init()
    }
    
    convenience init(code: Int) {
        self.init()
        
        self.lock = code
    }
    
    convenience init(bytes: [UInt8]) {
        self.init(code: Int(bytes[2]))
    }
}