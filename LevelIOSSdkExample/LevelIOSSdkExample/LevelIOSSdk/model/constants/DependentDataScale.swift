//
//  DependentDataScale.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum DependentDataScale: Int {
    case OneToOneBit = 0, TenToOneBit = 1, OneHundredToOneBit = 2, PlusMinus1GToDataSize = 3, PlusMinus2GToDataSize = 4,
    PlusMinus4GToDataSize = 5,  PlusMinus8GToDataSize = 6, PlusMinus16GToDataSize = 7, PlusMinus500DPSToDataSize = 8,
    PlusMinus1000DPSToDataSize = 9, PlusMinus2000DPSToDataSize = 10, PlusMinus4GaussToDataSize = 11, PlusMinus8GaussToDataSize = 12,
    PlusMinus12GaussToDataSize = 13, PlusMinus16GaussToDataSize = 14, PlusMinus48GaussToDataSize = 15
}