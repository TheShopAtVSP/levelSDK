//
//  DeleteBondParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/14/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DeleteBondParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return DeleteBondPacket(bytes: bytes)
    }
}