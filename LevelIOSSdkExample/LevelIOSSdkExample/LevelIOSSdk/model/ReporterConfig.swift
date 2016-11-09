//
//  ReporterConfig.swift
//  LevelIOSSdkExample
//
//  Created by Andrew Cook on 11/3/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class ReporterConfigBuilder {
    var reporter: ReporterType
    var samplingFrequency: Int
    var dependentDataScale: DependentDataScale
    var indVarDesc: IndependentVariableDescription
    var dataFields: [DataFields]
    var samplesPerRecord: Int
    var maxNumberOfRecords: Int
    
    init() {
        self.reporter = ReporterType.None
        self.samplingFrequency = 0
        self.dependentDataScale = DependentDataScale.OneToOneBit
        self.indVarDesc = IndependentVariableDescription.Seconds
        self.dataFields = [DataFields]()
        self.samplesPerRecord = 10
        self.maxNumberOfRecords = 10
    }
    
    func step() {
        self.reporter = ReporterType.Steps
    }
    
    func accel() {
        self.reporter = ReporterType.Accel
    }
    
    func gyro() {
        self.reporter = ReporterType.Gyro
    }
    
    func reportAttributes(attrs: ReportAttributes) {
        self.reporter = ReporterType(rawValue: attrs.reporter)!
        self.samplingFrequency = attrs.indVarScale
        self.dependentDataScale = attrs.depDataScale!
        self.indVarDesc = attrs.indVarDescription!
        self.samplesPerRecord = attrs.samplesPerRecord
        self.maxNumberOfRecords = attrs.maxRecordsPerReport
        self.dataFields = [DataFields]()
        
        for field in DataFields.cases() {
            if (attrs.dataFieldsPerSample! & field.rawValue) > 0 {
                self.dataFields.append(field)
            }
        }
    }
    
    func build() -> ReporterConfig {
        return ReporterConfig(builder: self)
    }
}

class ReporterConfig: NSObject {
    var reporter: ReporterType
    var samplingFrequency: Int
    var dependentDataScale: DependentDataScale
    var indVarDesc: IndependentVariableDescription
    var dataFields: Int
    var samplesPerRecord: Int
    var maxNumberOfRecords: Int
    
    init(builder: ReporterConfigBuilder) {
        self.reporter = builder.reporter
        self.samplingFrequency = builder.samplingFrequency
        self.dependentDataScale = builder.dependentDataScale
        self.indVarDesc = builder.indVarDesc
        
        var fields = 0
        
        for data in builder.dataFields {
            fields |= data.rawValue
        }
        
        self.dataFields = fields
        self.samplesPerRecord = builder.samplesPerRecord
        self.maxNumberOfRecords = builder.maxNumberOfRecords
    }
}
