//
//  DeviceUserUuid.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/28/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DeviceUserUuid: DataPacket {
    var uuid: String
    static var hex: [Character: UInt8] = ["0": 0, "1": 1, "2": 2, "3": 3, "4": 4, "5": 5,
                               "6": 6, "7": 7, "8": 8, "9": 9, "a": 10, "b": 11,
                               "c": 12, "d": 13, "e": 14, "f": 15]
    static var back: [UInt8: String] = [0: "0", 1: "1", 2: "2", 3: "3", 4: "4", 5: "5",
                                6: "6", 7: "7", 8: "8", 9: "9", 10: "a", 11: "b",
                                12: "c", 13: "d", 14: "e", 15: "f"]
    init(userUuid: String) {
        self.uuid = userUuid
    
        super.init()
    }
    
    convenience init(bytes: [UInt8]) {
        var uuidNoDashes: String = ""
        
        for i in (2...17).reverse() {
            let hexes: [UInt8] = BitsHelper.convertTo2Bytes(Int(bytes[i]))
            
            for hex in hexes {
                uuidNoDashes += DeviceUserUuid.back[hex]!
            }
        }
        
        uuidNoDashes.insert("-", atIndex: uuidNoDashes.startIndex.advancedBy(8))
        uuidNoDashes.insert("-", atIndex: uuidNoDashes.startIndex.advancedBy(12))
        uuidNoDashes.insert("-", atIndex: uuidNoDashes.startIndex.advancedBy(16))
        uuidNoDashes.insert("-", atIndex: uuidNoDashes.startIndex.advancedBy(20))
    
        self.init(userUuid: uuidNoDashes)
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8] = [UInt8](count: 16, repeatedValue: UInt8())
        var counter: Int = 15
        
        let withoutDashes: String = uuid.stringByReplacingOccurrencesOfString("-", withString: "").lowercaseString
        
        for i in 0.stride(to: 32, by: 2) {
            let first: UInt8 = DeviceUserUuid.hex[withoutDashes[withoutDashes.startIndex.advancedBy(i)]]!
            let second: UInt8 = DeviceUserUuid.hex[withoutDashes[withoutDashes.startIndex.advancedBy(i+1)]]!
            
            bytes[counter] = BitsHelper.convertToUInt8(first, lsb: second)
            
            counter -= 1
        }
        
        
        return bytes;
    }
}