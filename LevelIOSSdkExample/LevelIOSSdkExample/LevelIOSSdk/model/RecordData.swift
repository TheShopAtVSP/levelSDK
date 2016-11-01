//
//  RecordData.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

let HEADER_LENGTH = 8

class RecordData: TimePacket {
    var data: [UInt8]
    var currenBytes: Int = 0
    var totalBytes: Int = 0
    var originalTimestamp: Double = 0
    
    init(bytes: [UInt8]) {
        self.totalBytes = BitsHelper.convertTo12BitInt([UInt8](bytes[4...5])) - HEADER_LENGTH
        //debugPrint("RecordData - \(totalBytes)")
        self.data = [UInt8](count: self.totalBytes, repeatedValue: 0)
        
        super.init()
        
        self.id = Int(BitsHelper.convertToUInt16(bytes[3], lsb: bytes[2]))
        self.reporter = (Int(bytes[5] & 0xF0) >> 4)
        self.timestamp = Double(BitsHelper.convertToUInt32([UInt8](bytes[6...9])))
        self.originalTimestamp = self.timestamp * 1000
        
        if self.totalBytes > 0 {
            for i in HEADER_LENGTH + 2...(bytes.count-1) {
                //debugPrint("why? \(i) -- \(currenBytes)")
                data[currenBytes] = bytes[i]
                currenBytes += 1
                
                if isFinished() {
                    break
                }
            }
        }
    }
    
    func continueRecord(bytes: [UInt8]) -> RecordData {
        for i in 2...(bytes.count - 1) {
            //debugPrint("what what? \(i) -- \(currenBytes)")
            data[currenBytes] = bytes[i]
            currenBytes+=1
            
            if isFinished() {
                break
            }
        }
        
        return self
    }
    
    func isFinished() -> Bool {
        return self.currenBytes == self.totalBytes
    }
    
}
