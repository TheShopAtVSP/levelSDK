//
//  BleCharacteristics.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/23/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum BleCharacteristics: String {
    case BatteryLevel = "2a19"
    case BatteryState = "2a20"
    case FirmwareVersion = "2a26"
    case BootloaderVersion = "2a28"
    case UartRX = "6e400003-b5a3-f393-e0a9-e50e24dcca9e"
    case UartTx = "6e400002-b5a3-f393-e0a9-e50e24dcca9e"
}