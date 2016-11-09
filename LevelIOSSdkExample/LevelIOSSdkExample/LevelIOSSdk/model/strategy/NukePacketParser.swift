//
//  NukePacketParser.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/4/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class NukePacketParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return NukeRecordsPacket(bytes: bytes)
    }
}
