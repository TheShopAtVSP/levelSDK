//
//  FrameParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/11/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class FrameParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return Frame(bytes: bytes)
    }
}