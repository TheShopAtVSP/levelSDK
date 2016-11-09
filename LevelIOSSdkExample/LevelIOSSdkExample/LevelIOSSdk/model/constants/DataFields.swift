//
//  DataFields.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum DataFields: Int {
    case IncludeXAxis = 0b00000001, IncludeYAxis = 0b00000010, IncludeZAxis = 0b00000100, IncludeMagnitude = 0b00001000,
    None = 0
    
    static func cases() -> [DataFields] {
        return [.IncludeXAxis, .IncludeYAxis, .IncludeZAxis, .IncludeMagnitude]
    }
}
