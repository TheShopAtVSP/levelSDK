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
    
    func getReportControl() -> Int {
        switch self {
        case .Steps:
            return 1
        case .Gyro:
            return 2
        case .Accel:
            return 4
        default:
            return 0
        }
    }
    
    static func cases() -> [ReporterType] {
        return [.Steps, .Gyro, .Accel]
    }
}
