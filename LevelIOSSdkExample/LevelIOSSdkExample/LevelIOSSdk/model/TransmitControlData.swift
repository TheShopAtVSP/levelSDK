//
//  TransmitControlData.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/1/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class TransmitControlData: DataPacket {
    var totalByteCount: Int
    var totalRecordCount: Int
    var writeData: UInt8?
    
    init(totalRecordCount: Int, totalByteCount: Int) {
        self.totalRecordCount = totalRecordCount
        self.totalByteCount = totalByteCount
        
        super.init()
    }
    
    convenience init(writeData: UInt8) {
        self.init(totalRecordCount: -1, totalByteCount: -1)
        
        self.writeData = writeData
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8] = [UInt8]()
        
        bytes.append(self.writeData!)
        
        return bytes
    }
}
