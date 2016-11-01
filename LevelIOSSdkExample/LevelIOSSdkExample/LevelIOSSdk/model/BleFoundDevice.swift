//
// Created by Andrew Cook on 6/23/16.
// Copyright (c) 2016 TheShop. All rights reserved.
//

import Foundation
import CoreBluetooth

class BleFoundDevice {
    var device: CBPeripheral
    var rssi: Int

    init(device: CBPeripheral, rssi: Int) {
        self.device = device
        self.rssi = rssi
    }
}
