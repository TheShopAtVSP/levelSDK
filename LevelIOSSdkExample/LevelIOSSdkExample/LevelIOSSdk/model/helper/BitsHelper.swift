//
//  BitsHelper.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class BitsHelper {
    class func convertTo12BitInt(bytes: [UInt8]) -> Int {
        let first: Int = (Int(bytes[1] & 0x0F) << 0o10)
        let second: Int = Int((bytes[0] & 0xFF))
        
        return Int(first) + Int(second)
    }
    
    class func convertToUInt16(msb: UInt8, lsb: UInt8) -> UInt16 {
        return (UInt16(msb) << 8) + UInt16(lsb)
    }
    
    class func convertToUInt8(msb: UInt8, lsb: UInt8) -> UInt8 {
        return (msb << 4) + lsb
    }
    
    class func convertToUInt32(bytes: [UInt8]) -> UInt32 {
        var value : UInt32 = 0
        for byte in bytes.reverse() {
            value = value << 8
            value = value | UInt32(byte)
        }
        
        return value
    }
    
    class func nsdataToUInt8(data: NSData) -> [UInt8] {
        if data.length > 0 {
            let count = data.length / sizeof(UInt8)
            
            if count > 0 {
                var array = [UInt8](count: count, repeatedValue: 0)
                
                data.getBytes(&array, length: count * sizeof(UInt8))
                
                return array
            }
        }
        
        return [UInt8]()
    }
    
    class func convertTo4Bytes(time: Double) -> [UInt8] {
        let nixTime: Int = Int(time)
        var bytes: [UInt8] = [UInt8](count: 4, repeatedValue: UInt8())
        
        bytes[0] = UInt8(nixTime & 0x000000FF)
        bytes[1] = UInt8((nixTime >> 8) & 0x000000FF)
        bytes[2] = UInt8((nixTime >> 16) & 0x000000FF)
        bytes[3] = UInt8((nixTime >> 24) & 0x000000FF)
        
        return bytes
    }
    
    class func convertTo2Bytes(number: Int) -> [UInt8] {
        var bytes: [UInt8] = [UInt8](count: 2, repeatedValue: UInt8())
        
        bytes[0] = UInt8(number & 0x000000FF)
        bytes[1] = UInt8((number >> 8) & 0x000000FF)
        
        return bytes
    }
}
