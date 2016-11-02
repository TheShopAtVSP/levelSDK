//
//  Frame.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class Frame: DataPacket {
    var model: LevelModel
    var color: LevelColor
    
    override init() {
        self.model = .NotSet
        self.color = .NotSet
        
        super.init();
    }
    
    convenience init(model: LevelModel, color: LevelColor) {
        self.init()
        
        self.model = model
        self.color = color
    }
    
    convenience init(bytes: [UInt8]) {
        self.init()
        
        let modelNum = Int(BitsHelper.convertToUInt16(msb: bytes[3], lsb: bytes[2]))
        let colorNum = Int(BitsHelper.convertToUInt16(msb: bytes[5], lsb: bytes[4]))
        
        self.model = LevelModel(rawValue: modelNum)!
        self.color = LevelColor(rawValue: colorNum)!
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8] = [UInt8]()
        
        var modelBytes: [UInt8] = BitsHelper.convertTo2Bytes(number: model.rawValue)
        var colorBytes: [UInt8] = BitsHelper.convertTo2Bytes(number: color.rawValue)
        
        bytes.append(modelBytes[0])
        bytes.append(modelBytes[1])
        bytes.append(colorBytes[0])
        bytes.append(colorBytes[1])
        
        return bytes
    }
}
