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
        
        for i in (2...17).reversed() {
            let hexes: [UInt8] = BitsHelper.convertTo2Bytes(number: Int(bytes[i]))
            
            for hex in hexes {
                uuidNoDashes += DeviceUserUuid.back[hex]!
            }
        }
        
        uuidNoDashes.insert("-", at: uuidNoDashes.index(uuidNoDashes.startIndex, offsetBy: 8))
        uuidNoDashes.insert("-", at: uuidNoDashes.index(uuidNoDashes.startIndex, offsetBy: 12))
        uuidNoDashes.insert("-", at: uuidNoDashes.index(uuidNoDashes.startIndex, offsetBy: 16))
        uuidNoDashes.insert("-", at: uuidNoDashes.index(uuidNoDashes.startIndex, offsetBy: 20))
    
        self.init(userUuid: uuidNoDashes)
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8] = [UInt8](repeating: UInt8(), count: 16)
        var counter: Int = 15
        
        let withoutDashes: String = uuid.replacingOccurrences(of: "-", with: "").lowercased()
        
        for i in stride(from: 0, to: 32, by: 2) {
            let first: UInt8 = DeviceUserUuid.hex[withoutDashes[withoutDashes.index(withoutDashes.startIndex, offsetBy: i)]]!
            let second: UInt8 = DeviceUserUuid.hex[withoutDashes[withoutDashes.index(withoutDashes.startIndex, offsetBy: (i+1))]]!
            
            bytes[counter] = BitsHelper.convertToUInt8(msb: first, lsb: second)
            
            counter -= 1
        }
        
        
        return bytes;
    }
}
