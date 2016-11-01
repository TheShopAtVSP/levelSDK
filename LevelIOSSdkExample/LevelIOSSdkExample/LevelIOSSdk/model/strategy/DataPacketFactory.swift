//
//  DataPacketFactory.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class DataPacketFactory {
    var parserMap: [DeviceCommand: DataPacketParserProtocol?]
    
    init() {
        self.parserMap = [DeviceCommand: DataPacketParserProtocol?]()
        
        self.parserMap[DeviceCommand.Nack] = NackParser()
        self.parserMap[DeviceCommand.StartOfRecord] = StartOfRecordParser()
        self.parserMap[DeviceCommand.ReporterAttributes] = ReportAttributesParser()
        self.parserMap[DeviceCommand.ReportControl] = ReportControlParser()
        self.parserMap[DeviceCommand.TransmitControl] = TransmitControlParser()
        self.parserMap[DeviceCommand.TimeRD] = TimeParser()
        self.parserMap[DeviceCommand.TimeWR] = TimeParser()
        self.parserMap[DeviceCommand.CodeWR] = CodeParser()
        self.parserMap[DeviceCommand.UserUUIDRD] = UserUuidParser()
        self.parserMap[DeviceCommand.UserUUIDWR] = UserUuidParser()
        self.parserMap[DeviceCommand.LockRD] = LockParser()
        self.parserMap[DeviceCommand.FrameRD] = FrameParser()
        self.parserMap[DeviceCommand.FrameWR] = FrameParser()
        self.parserMap[DeviceCommand.DeleteBond] = DeleteBondParser()
    }
    
    func getDataPacket(bytes: [UInt8]) -> DataPacketParserProtocol? {
        if let command: DeviceCommand = DeviceCommand(rawValue: Int(bytes[1])) {
            if let val: DataPacketParserProtocol = self.parserMap[command]! {
                return val
            }
        }
        
        return nil
    }
}
