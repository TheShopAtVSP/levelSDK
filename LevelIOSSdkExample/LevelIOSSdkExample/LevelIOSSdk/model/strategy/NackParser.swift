//
//  File.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum NackError: Int {
    case PacketSeqError = 2, PacketTypeError = 5, DataLengthError = 6, AttributeError = 9
}

class NackParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        if bytes.count < 2 {
            //TODO find out how exceptions work
        }
        
        if let error = NackError(rawValue: Int(bytes[2])) {
            switch error {
            case NackError.PacketSeqError:
                break
            case .PacketTypeError:
                break
            case .DataLengthError:
                break
            case .AttributeError:
                break
            }
        }
        
        return DataPacket()
    }
}
