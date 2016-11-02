//
//  CalculationHelper.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 7/1/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class CalculationHelper: NSObject {
    private var user: LevelUser
    private var userMetsCorrectionFactor: Double = 0.0
    
    private var inchesPerFoot: Int = 12;
    private var feetPerMile: Int = 5280;
    private var secsPerMinute: Int = 60;
    private var minutesPerDay: Int = 1440
    
    private var METS_SLOW_MULTIPLIER: Double = 1.1898
    private var METS_SLOW_STEPRATE_MULTIPLIER: Double = 0.3794
    private var METS_FAST_MULTIPLIER: Double = 1.1674
    private var METS_FAST_ADDITOR: Double = 2.6979
    private var SPEED_MALE_MULTIPLIER: Double = 0.6915
    private var SPEED_MALE_STEPRATE_MULTIPLIER: Double = 0.824
    private var SPEED_FEMALE_MULTIPLIER: Double = 0.6552
    private var SPEED_FEMALE_STEPRATE_MULTIPLIER: Double = 0.8028
    private var ACTIVE_BURN_MULTIPLIER: Double = 0.454
    private var ACTIVE_BURN_WEIGHT_MULTIPLIER: Double = 0.0167
    private var BMR_MALE_ADDITOR: Double = 66.473
    private var BMR_FEMALE_ADDITOR: Double = 655.0955
    private var BMR_HEIGHT_MULTIPLIER: Double = 2.54
    private var BMR_MALE_HEIGHT_MULTIPLIER: Double = 5.0033
    private var BMR_FEMALE_HEIGHT_MULTIPLIER: Double = 1.8496
    private var BMR_MALE_WEIGHT_MULTIPLIER: Double = 13.7516
    private var BMR_FEMALE_WEIGHT_MULTIPLIER: Double = 9.5634
    private var BMR_MALE_AGE_MULTIPLIER: Double = 6.755
    private var BMR_FEMALE_AGE_MULTIPLIER: Double = 4.6756
    
    init(user: LevelUser) {
        self.user = user
        
        super.init()
        
        self.userMetsCorrectionFactor = 3.5 * (user.weightLbs * ACTIVE_BURN_MULTIPLIER * Double(minutesPerDay) * 5) / (calculateBMR() * 1000)
    }
    
    func calculateMets(stepTotal: Int) -> Double {
        var mets: Double = 0
        let speed: Double = calculateSpeedInMPH(stepTotal: stepTotal)
        
        if speed > 1.51 && speed <= 5 {
            mets = METS_SLOW_MULTIPLIER * exp(speed * METS_SLOW_STEPRATE_MULTIPLIER)
        } else if speed > 5 {
            mets = METS_FAST_MULTIPLIER * speed + METS_FAST_ADDITOR;
        }
        
        return mets
    }
    
    func calculateDistanceInMiles(stepCount: Int) -> Double {
        return (Double(stepCount) * calculateStringLength(stepCount: stepCount)) / Double(feetPerMile)
    }
    
    func calculateActiveBurn(stepTotal: Int) -> Double {
        return calculateActiveBurnWithWeight(stepTotal: stepTotal, weight: Double(user.weightLbs))
    }
    
    private func calculateActiveBurnWithWeight(stepTotal: Int, weight: Double) -> Double {
        let mets: Double = calculateMets(stepTotal: stepTotal)
        
        if mets > 0 {
            return mets * userMetsCorrectionFactor * ACTIVE_BURN_MULTIPLIER * weight * ACTIVE_BURN_WEIGHT_MULTIPLIER
        }
        
        return 0
    }
    
    private func calculateSpeedInMPH(stepTotal: Int) -> Double {
        let stepRate: Double = calculateStepRate(stepCount: stepTotal)
        var speedMPH: Double = 0
        
        if stepRate > 0 {
            if user.gender == .Male {
                speedMPH = SPEED_MALE_MULTIPLIER * exp(SPEED_MALE_STEPRATE_MULTIPLIER * stepRate)
            } else {
                speedMPH = SPEED_FEMALE_MULTIPLIER * exp(SPEED_FEMALE_STEPRATE_MULTIPLIER * stepRate)
            }
        }
        
        return speedMPH
    }
    
    private func calculateStringLength(stepCount: Int) -> Double {
        var stepRate: Double = calculateStepRate(stepCount: stepCount)
        let heightInInches: Double = Double(calculateHeightInInches()) / Double(12)
        var fit: [Double], x: [Double] = [Double](repeating: 0, count: 2), y: [Double] = [Double](repeating: 0, count: 2)
        
        if user.gender == .Male {
            x[0] = 1.6
            x[1] = 3.2
            y[0] = 0.3736 * heightInInches
            y[1] = 0.7433 * heightInInches
            
            if stepRate < 1.4 {
                stepRate = 1.8
            }
        } else {
            x[0] = 1.6;
            x[1] = 3.2;
            y[0] = 0.3778 * heightInInches
            y[1] = 0.7426 * heightInInches
            
            if stepRate < 1.4 {
                stepRate = 1.9
            }
        }
        
        fit = LeastSquaresExponentialFit.fit(x: x, y: y)
        
        return (fit[0] * exp(fit[1] * stepRate))
    }
    
    private func calculateStepRate(stepCount: Int) -> Double {
        return Double(stepCount) / Double(secsPerMinute)
    }
    
    private func calculateHeightInInches() -> Int {
    
        var totalHeight: Int = 0;
    
        if user.heightFeet > 0 || user.heightInches > 0 {
    
            totalHeight = (user.heightFeet * inchesPerFoot) + user.heightInches;
    
        }
    
        return totalHeight;
    
    }
    
    private func calculateBMR() -> Double {
    
        var BMR: Double = 0;
    
        if user.weightLbs > 0 && (user.heightFeet > 0 || user.heightInches > 0) {
            if (user.gender == .Male) {
                BMR = BMR_MALE_ADDITOR + (BMR_MALE_WEIGHT_MULTIPLIER * ACTIVE_BURN_MULTIPLIER * user.weightLbs) +
                    (BMR_MALE_HEIGHT_MULTIPLIER * BMR_HEIGHT_MULTIPLIER * Double(calculateHeightInInches())) -
                        (BMR_MALE_AGE_MULTIPLIER * Double(user.age));
            } else {
                BMR = BMR_FEMALE_ADDITOR + (BMR_FEMALE_WEIGHT_MULTIPLIER * ACTIVE_BURN_MULTIPLIER * Double(user.weightLbs)) +
                    (BMR_FEMALE_HEIGHT_MULTIPLIER * BMR_HEIGHT_MULTIPLIER * Double(calculateHeightInInches())) -
                        (BMR_FEMALE_AGE_MULTIPLIER * Double(user.age));
            }
    
        }
    
        return BMR;
    }
}
