//
//  PacketParserManager.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum PacketErrors: ErrorType {
    case DataLength
    case SequenceId
}

class PacketParserManager {
    var factory: DataPacketFactory
    var record: RecordData?
    var setTime: Bool = false
    var timeDiff: Double = 0
    
    init() {
        record = nil
        factory = DataPacketFactory()
    }
    
    func parse(expectedPacketIdIn: Int, packet: [UInt8]) throws -> DataPacket? {
        if packet.count < 2 {
            throw PacketErrors.DataLength
        }
        
        if expectedPacketIdIn != Int(packet[0]) {
            throw PacketErrors.SequenceId
        }
        
        if let command: DeviceCommand = DeviceCommand(rawValue: Int(packet[1])) {
            if command == DeviceCommand.StartOfRecord {
                record = StartOfRecordParser().parse(packet) as? RecordData
                
                if setTime {
                    record!.timestamp = record!.timestamp + timeDiff
                }
                
                if record!.isFinished() {
                    return record
                }
            } else if command == DeviceCommand.RecordContinue {
                record!.continueRecord(packet)
                
                if record!.isFinished() {
                    return record
                }
            } else {
                if let parser: DataPacketParserProtocol = factory.getDataPacket(packet) {
                    let packet: DataPacket = parser.parse(packet)!
                    
                    if packet is TimePacket {
                        let timePacket: TimePacket = packet as! TimePacket
                        
                        if abs(timePacket.timestamp - timePacket.received.timeIntervalSince1970) > 2 * 60 * 1000 {
                            setTime = true
                            timeDiff = timePacket.received.timeIntervalSince1970 - timePacket.timestamp
                        }
                    }
                    
                    return packet
                }
            }
        }
        
        return nil
    }
}
