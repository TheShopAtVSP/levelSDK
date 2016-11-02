//
//  LeastSquaresExponentialFit.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/1/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class LeastSquaresExponentialFit {
    class func fit(x: [Double], y: [Double]) -> [Double] {
        var a: Double = 0.0, b: Double = 0.0, part1: Double = 0.0, part2: Double = 0.0, part3: Double = 0.0,
            part4: Double = 0.0, part5: Double = 0.0, part6: Double = 0.0
        
      for i in (1...x.count) {
      //for (var i = 0; i < 2; i = i+1) {
            part1 += pow(x[i], 2) * y[i]
            part2 += log(y[i]) * y[i]
            part3 += x[i] * y[i]
            part4 += x[i] * y[i] * log(y[i])
            part5 += y[i]
            part6 += pow(x[i], 2) * y[i]
        }
        
        a = ((part1 * part2) - (part3 * part4)) / ((part5 * part6) - pow(part3, 2))
        b = ((part5 * part4) - (part3 * part2)) / ((part5 * part1) - pow(part3, 2))
        
        return [exp(a), b]
    }
}
