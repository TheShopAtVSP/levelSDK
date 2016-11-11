//
//  ReportAttributes.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/24/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class ReportAttributes: DataPacket {
    var indVarDescription: IndependentVariableDescription?
    var indVarScale: Int
    var depVarDescription: DependentVariableDescription?
    var depDataType: DependentDataType?
    var depDataScale: DependentDataScale?
    var dataFieldsPerSample: Int?
    var samplesPerRecord: Int
    var maxRecordsPerReport: Int
    var queryOnly: Bool = false
    
    override init() {
        self.indVarDescription = IndependentVariableDescription.Unitless
        self.indVarScale = -1
        self.depVarDescription = DependentVariableDescription.Unitless
        self.depDataType = DependentDataType.Int32
        self.depDataScale = DependentDataScale.OneToOneBit
        self.dataFieldsPerSample = 0
        self.samplesPerRecord = -1
        self.maxRecordsPerReport = -1
        
        super.init()
    }
    
    convenience init(reporter: Int) {
        self.init()
        
        self.reporter = reporter
        self.queryOnly = true
    }
    
    convenience init(reporter: Int, indVarDesc: IndependentVariableDescription, indVarScale: Int, depVarDesc: DependentVariableDescription,
                     depDataType: DependentDataType, depDataScale: DependentDataScale, dataFieldsPerSample: Int, samplesPerRecord: Int,
                     maxRecordsPerReport: Int) {
        self.init()
        
        self.reporter = reporter
        self.indVarDescription = indVarDesc
        self.indVarScale = indVarScale
        self.depVarDescription = depVarDesc
        self.depDataType = depDataType
        self.depDataScale = depDataScale
        self.dataFieldsPerSample = dataFieldsPerSample
        self.samplesPerRecord = samplesPerRecord
        self.maxRecordsPerReport = maxRecordsPerReport
    }
    
    convenience init(config: ReporterConfig) {
        self.init()
        
        self.reporter = config.reporter.rawValue
        self.indVarDescription = config.indVarDesc
        self.indVarScale = config.samplingFrequency
        
        switch( config.reporter ) {
            case .Steps:
                self.depVarDescription = DependentVariableDescription.StepPerTime
            case .Accel:
                self.depVarDescription = DependentVariableDescription.AccelerometerRaw
            case .Gyro:
                self.depVarDescription = DependentVariableDescription.GyrometerRaw
            default:
                self.depVarDescription = DependentVariableDescription.AccelerometerRaw
        }
        
        self.depDataType = DependentDataType.Int16
        self.depDataScale = config.dependentDataScale
        self.dataFieldsPerSample = config.dataFields
        self.samplesPerRecord = config.samplesPerRecord
        self.maxRecordsPerReport = config.maxNumberOfRecords
    }
    
    convenience init(bytes: [UInt8]) {
        self.init()
        
        self.reporter = Int(bytes[2])
        self.indVarDescription = IndependentVariableDescription(rawValue: Int(bytes[3]))
        self.depVarDescription = DependentVariableDescription(rawValue: Int(bytes[5]))
        self.depDataType = DependentDataType(rawValue: Int(bytes[6]))
        self.depDataScale = DependentDataScale(rawValue: Int(bytes[7]))
        
        //TODO check if any are null
        self.indVarScale = Int(bytes[4])
        self.dataFieldsPerSample = Int(bytes[8])
        self.samplesPerRecord = Int(BitsHelper.convertToUInt16(msb: bytes[10], lsb: bytes[9]))
        self.maxRecordsPerReport = Int(BitsHelper.convertToUInt16(msb: bytes[12], lsb: bytes[11]))
    }
    
    override func getPacket() -> [UInt8] {
        var bytes: [UInt8]
        
        if queryOnly {
            bytes = [UInt8](repeating: UInt8(), count: 1)
        } else {
            bytes = [UInt8](repeating: UInt8(), count: 11)
        }
        
        var samplesPerRecord: [UInt8] = BitsHelper.convertTo2Bytes(number: self.samplesPerRecord)
        var maxRecords: [UInt8] = BitsHelper.convertTo2Bytes(number: self.maxRecordsPerReport)
        
        bytes[0] = UInt8(self.reporter)
        
        if !queryOnly {
            bytes[1] = UInt8((self.indVarDescription?.rawValue)!)
            bytes[2] = UInt8(self.indVarScale)
            bytes[3] = UInt8((self.depVarDescription?.rawValue)!)
            bytes[4] = UInt8((self.depDataType?.rawValue)!)
            bytes[5] = UInt8((self.depDataScale?.rawValue)!)
            bytes[6] = UInt8(self.dataFieldsPerSample!)
            bytes[7] = samplesPerRecord[0]
            bytes[8] = samplesPerRecord[1]
            bytes[9] = maxRecords[0]
            bytes[10] = maxRecords[1]
        }
        
        return bytes
    }
    
    func isEqual(object: AnyObject?) -> Bool {
        if self === object {
            return true
        }
        
        if let that = object as? ReportAttributes {
            if self.indVarScale != that.indVarScale { return false }
            if self.dataFieldsPerSample != that.dataFieldsPerSample { return false }
            if self.samplesPerRecord != that.samplesPerRecord { return false }
            if self.maxRecordsPerReport != that.maxRecordsPerReport { return false }
            if self.indVarDescription != that.indVarDescription { return false }
            if self.depVarDescription != that.depVarDescription { return false }
            if self.depDataType != that.depDataType { return false }
            
            return self.depDataScale == that.depDataScale
        }
        
        return false
    }
}
