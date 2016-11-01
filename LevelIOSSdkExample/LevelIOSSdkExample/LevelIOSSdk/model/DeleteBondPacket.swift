//
//  DeleteBondPacket.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/14/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DeleteBondPacket: DataPacket {
    var code: Int
    
    override init() {
        self.code = 0
        super.init()
    }
    
    convenience init(code: Int) {
        self.init()
        
        self.code = code
    }
    
    convenience init(bytes: [UInt8]) {
        self.init(code: Int(bytes[0]))
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8] = [UInt8]()
        
        bytes.append(22)
        
        return bytes
    }
}