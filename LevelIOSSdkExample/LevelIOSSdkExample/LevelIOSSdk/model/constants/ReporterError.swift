//
//  ReporterError.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/4/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum ReporterError: Int {
    case NoError = 0, ReporterEnabled = 1, ReporterInstanceError = 2, DependentDataTypeError = 3, ReporterDataNotEmpty = 4
}
