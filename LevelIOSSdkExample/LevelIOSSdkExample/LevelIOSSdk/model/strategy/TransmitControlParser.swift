//
//  TransmitControlParser.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class TransmitControlParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        return TransmitControlData(totalRecordCount: Int(BitsHelper.convertToUInt16(
            bytes[3], lsb: bytes[2])), totalByteCount: Int(BitsHelper.convertToUInt32([UInt8](bytes[4...7]))))
    }
}
