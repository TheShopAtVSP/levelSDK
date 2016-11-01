//
//  StartOfRecordParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class StartOfRecordParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        if bytes.count < 2 {
            
        }
        
        return RecordData(bytes: bytes)
    }
}
