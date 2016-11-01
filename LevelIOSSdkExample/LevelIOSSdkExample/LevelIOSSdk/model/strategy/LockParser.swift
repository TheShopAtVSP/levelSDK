//
//  LockParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/5/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class LockParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return LockPacket(bytes: bytes)
    }
}