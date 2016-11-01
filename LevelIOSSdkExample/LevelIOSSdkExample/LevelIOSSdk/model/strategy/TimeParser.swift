//
//  TimeParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class TimeParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        if bytes.count == 6 {
            return TimePacket(bytes: bytes)
        }
        
        return nil
    }
}
