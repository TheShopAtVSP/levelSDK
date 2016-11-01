//
//  CodeParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class CodeParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return CodePacket(bytes: bytes)
    }
}
