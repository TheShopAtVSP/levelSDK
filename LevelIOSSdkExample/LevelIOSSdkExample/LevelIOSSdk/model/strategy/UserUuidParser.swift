//
//  UserUuidParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/29/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class UserUuidParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return DeviceUserUuid(bytes: bytes)
    }
}