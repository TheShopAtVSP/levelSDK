//
//  BleCommand.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum ReadWrite {
    case Read, Write, NotifyOnly
}

class BleCommand {
    var readOrWrite: ReadWrite
    var characteristic: BleCharacteristics
    var data: [UInt8]
    
    init(readWrite: ReadWrite, charac: BleCharacteristics) {
        
        self.readOrWrite = readWrite
        self.characteristic = charac
        self.data = [UInt8]()
    }
    
    convenience init(readWrite: ReadWrite, charac: BleCharacteristics, bytes: [UInt8]) {
        self.init(readWrite: readWrite, charac: charac)
        
        self.data = bytes
    }
}
