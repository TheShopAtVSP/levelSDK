//
//  BatteryState.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/22/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum BatteryState: Int {
    case Charging = 1
    case Discharging = 2
    case Charged = 4
    case BatteryError = 5
    
    var description: String {
        switch self {
        case .Charged:
            return "Charged"
        case .Charging:
            return "Charging"
        case .Discharging:
            return "Discharging"
        case .BatteryError:
            return "Battery Error"
        }
    }
}