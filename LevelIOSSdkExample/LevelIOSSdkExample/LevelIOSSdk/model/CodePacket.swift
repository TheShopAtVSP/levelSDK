//
// Created by Andrew Cook on 6/24/16.
// Copyright (c) 2016 TheShop. All rights reserved.
//

import Foundation

class CodePacket: DataPacket {
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
        
        bytes.append(UInt8(code))
        
        return bytes
    }
}
