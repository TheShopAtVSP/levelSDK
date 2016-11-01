//
//  DeviceCommand.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum DeviceCommand: Int {
    case Unknown = -1
    case Nack = 0
    case Ack = 1
    case BOOTLOADER1 = 2
    case BOOTLOADER2 = 3
    case BOOTLOADER3 = 4
    case StartOfRecord = 5
    case RecordContinue = 6
    case ReporterAttributes = 7
    case ReportControl = 8
    case TransmitControl = 9
    case TimeRD = 10
    case TimeWR = 11
    case UserUUIDRD = 12
    case UserUUIDWR = 13
    case FrameRD = 14
    case FrameWR = 15
    case CodeWR = 17
    case DeleteBond = 18 //send 0x22 as payload
    case LockRD = 20
}