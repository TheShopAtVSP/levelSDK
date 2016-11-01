//
// Created by Andrew Cook on 6/22/16.
// Copyright (c) 2016 TheShop. All rights reserved.
//

import Foundation

enum BleServices:String {
    case Battery = "0000180f-0000-1000-8000-00805f9b34fb"
    case DeviceInfo = "0000180a-0000-1000-8000-00805f9b34fb"
    case UART = "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
    case DFU = "00001530-1212-efde-1523-785feabcd123"
}
