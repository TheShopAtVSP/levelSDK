//
//  File.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum NackError: Int {
    case PacketSeqError = 2, PacketTypeError = 5, DataLengthError = 6, AttributeError = 9, None = 10
}

class NackParser: DataPacketParserProtocol {
    func parse(bytes: [UInt8]) -> DataPacket? {
        if bytes.count < 2 {
            //TODO find out how exceptions work
        }
        
        let packet: DataPacket = DataPacket()
        
        if let error = NackError(rawValue: Int(bytes[2])) {
            packet.nackError = error
            
            if error == .AttributeError && bytes.count >= 5 {
                packet.subError = ReporterError(rawValue: Int(bytes[4]))!
            }
            /*switch error {
            case NackError.PacketSeqError:
                break
            case .PacketTypeError:
                break
            case .DataLengthError:
                break
            case .AttributeError:
                break
            }*/
        }
        
        return packet
    }
}
