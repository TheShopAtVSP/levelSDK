//
//  ReporterType.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/3/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum ReporterType: Int {
    case Steps = 0, Gyro = 1, Accel = 2, None = 3
    
    static func cases() -> [ReporterType] {
        return [.Steps, .Gyro, .Accel]
    }
}
