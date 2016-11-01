//
//  DependentVariableDescription.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum DependentVariableDescription: Int {
    case Unitless = 0, Ascii = 1, StepPerTime = 2, AccelerometerRaw = 3, GyrometerRaw = 4, MagnetometerRaw = 5,
    AccelerometerFILT = 6, GyrometerFILT = 7, MagnetometerFILT = 8, AcclerometerVariance = 9, GyrometerVariance = 10,
    MagnetometerVariance = 11, BatteryPercentRemaining = 12, CyclePerTime = 13, TiltAngle = 14, BoardTemp = 15,
    Experimental = 16
}