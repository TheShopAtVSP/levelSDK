//
//  DeviceStateMachine.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/29/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

enum DeviceLifecycle: String {
    case QueryLock, SendLedCode1, SendLedCode2, SendLedCode3, SendLedCode4, QueryTime,
        SetTime, QueryReportControl, QueryReporter0, QueryReporter1, QueryReporter2, DisableReporters, QueryStoredData,
        RetrieveData, RetrievingData, SetupReporter0, SetupReporter1, SetupReporter2, EnableReporters, EnsureTransmitControlOn, Done, Unknown
    
    func getCommand() -> DeviceCommand {
        switch self {
        case .QueryTime: return DeviceCommand.TimeRD
        case .SetTime: return DeviceCommand.TimeWR
        case .QueryReportControl, .EnableReporters, .DisableReporters: return DeviceCommand.ReportControl
        case .QueryReporter0, .QueryReporter1, .QueryReporter2, .SetupReporter0, .SetupReporter1, .SetupReporter2: return DeviceCommand.ReporterAttributes
        case .QueryStoredData, .RetrieveData, .EnsureTransmitControlOn: return DeviceCommand.TransmitControl
        case .RetrievingData: return DeviceCommand.Unknown
        case .QueryLock: return DeviceCommand.LockRD
        default: return DeviceCommand.Unknown
        }
    }
    
    func getPacket() -> [UInt8] {
        switch self {
        case .SetTime:
            return BitsHelper.convertTo4Bytes(NSDate().timeIntervalSince1970)
        case .DisableReporters, QueryReporter0:
            let bytes: [UInt8] = [0]
            return bytes
        case .RetrieveData, QueryReporter1, .EnsureTransmitControlOn:
            let bytes: [UInt8] = [1]
            return bytes
        case .EnableReporters:
            let bytes: [UInt8] = [7]
            return bytes
        case .SetupReporter0:
            let bytes: [UInt8] = [0, UInt8(IndependentVariableDescription.Seconds.rawValue), 15, UInt8(DependentVariableDescription.StepPerTime.rawValue),
                                  UInt8(DependentDataType.UInt8.rawValue), UInt8(DependentDataScale.OneToOneBit.rawValue), 0, 4, 0, 0, 0]
            return bytes
        case .SetupReporter1:
            let bytes: [UInt8] = [1, UInt8(IndependentVariableDescription.OnChange.rawValue), 1, UInt8(DependentVariableDescription.BatteryPercentRemaining.rawValue),
                                  UInt8(DependentDataType.Int24.rawValue), UInt8(DependentDataScale.OneToOneBit.rawValue), 0, 1, 0, 0, 0]
            return bytes
        case .SetupReporter2:
            let bytes: [UInt8] = [2, UInt8(IndependentVariableDescription.Seconds.rawValue), 5, UInt8(DependentVariableDescription.AccelerometerFILT.rawValue),
                                  UInt8(DependentDataType.Int16.rawValue), UInt8(DependentDataScale.PlusMinus1GToDataSize.rawValue), 0, 24, 0, 0, 0]
            return bytes
        case .QueryReporter2:
            let bytes: [UInt8] = [2]
            return bytes
        default: return [UInt8]()
        }
    }
    
    func getTransition() -> DeviceTransition {
        switch self {
        case .SendLedCode1, .SendLedCode2, .SendLedCode3, .SendLedCode4:
            return .CodeWasReceived
        case .QueryTime:
            return .GotTime
        case .SetTime:
            return .TimeWasSet
        case .QueryReportControl:
            return .GotReporterControl
        case .QueryReporter0:
            return .GotReporter0Attriburtes
        case .QueryReporter1:
            return .GotReporter1Attriburtes
        case .QueryReporter2:
            return .GotReporter2Attriburtes
        case .DisableReporters:
            return .ReportersDisabled
        case .QueryStoredData:
            return .GotTransmitControl
        case .RetrieveData, .EnsureTransmitControlOn:
            return .TurnedTransmitControlOn
        case .RetrievingData:
            return .GotData
        case .SetupReporter0:
            return .Reporter0AttributesSet
        case .SetupReporter1:
            return .Reporter1AttributesSet
        case .SetupReporter2:
            return .Reporter2AttributesSet
        case .EnableReporters:
            return .ReporterEnabled
        default:
            return .Unknown
        }
    }
}

enum DeviceTransition {
    case GotLock, NeedLedCode, GotTime, Disconnected, CodeWasReceived, TimeWasSet, GotReporterControl, GotReporter0Attriburtes, GotReporter1Attriburtes, GotReporter2Attriburtes,
    ReportersDisabled, GotTransmitControl, TurnedTransmitControlOn, GotData, Reporter0AttributesSet, Reporter1AttributesSet, Reporter2AttributesSet, ReporterEnabled, Unknown
}

class DeviceStateMachine {
    var stateMachine: StateMachine<DeviceLifecycle, DeviceTransition>
    var currentTransition: Int = 0
    var enabled: Bool = false
    var reporter0Right: Bool = false, reporter1Right: Bool = false, reporter2Right: Bool = false
    var reporter0On: Bool = false, reporter1On: Bool = false, reporter2On: Bool = false
    var timeIsCorrect: Bool = false, transmitControlOn = false
    var timeDiff: Double = 0, deviceTime: Double = 0
    var totalRecordsToDownload: Int = 0
    var recordsDownloaded: Int = 0
    var ledCodeNeeded: Bool = false
    
    var expectedReport0Attributes: ReportAttributes = ReportAttributes(reporter: 0, indVarDesc: IndependentVariableDescription.Seconds, indVarScale: 15, depVarDesc: DependentVariableDescription.StepPerTime,
                                                                      depDataType: DependentDataType.UInt8, depDataScale: DependentDataScale.OneToOneBit, dataFieldsPerSample: 0, samplesPerRecord: 4, maxRecordsPerReport: 0)
    var expectedReport1Attributes: ReportAttributes = ReportAttributes(reporter: 1, indVarDesc: IndependentVariableDescription.OnChange, indVarScale: 1, depVarDesc: DependentVariableDescription.BatteryPercentRemaining,
                                                                       depDataType: DependentDataType.Int24, depDataScale: DependentDataScale.OneToOneBit, dataFieldsPerSample: 0, samplesPerRecord: 1, maxRecordsPerReport: 0)
    var expectedReport2Attributes: ReportAttributes = ReportAttributes(reporter: 2, indVarDesc: IndependentVariableDescription.Seconds, indVarScale: 5, depVarDesc: DependentVariableDescription.AccelerometerFILT,
                                                                       depDataType: DependentDataType.Int16, depDataScale: DependentDataScale.PlusMinus1GToDataSize, dataFieldsPerSample: 0, samplesPerRecord: 24, maxRecordsPerReport: 0)
    
    init() {
        stateMachine = StateMachine<DeviceLifecycle, DeviceTransition>(state: .QueryLock)
        
        stateMachine.addTransition(.NeedLedCode, from: .QueryLock, to: .SendLedCode1)
        stateMachine.addTransition(.GotLock, from: .QueryLock, to: .QueryTime)
        stateMachine.addTransition(.GotTime, from: .QueryTime, to: .SetTime)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode1, to: .SendLedCode2)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode2, to: .SendLedCode3)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode3, to: .SendLedCode4)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode4, to: .QueryTime)
        stateMachine.addTransition(.Disconnected, from: .SendLedCode4, to: .QueryLock)
        stateMachine.addTransition(.TimeWasSet, from: .SetTime, to: .QueryReportControl)
        stateMachine.addTransition(.GotReporterControl, from: .QueryReportControl, to: .QueryReporter0)
        stateMachine.addTransition(.GotReporter0Attriburtes, from: .QueryReporter0, to: .QueryReporter1)
        stateMachine.addTransition(.GotReporter1Attriburtes, from: .QueryReporter1, to: .QueryReporter2)
        stateMachine.addTransition(.GotReporter2Attriburtes, from: .QueryReporter2, to: .DisableReporters)
        stateMachine.addTransition(.ReportersDisabled, from: .DisableReporters, to: .QueryStoredData)
        stateMachine.addTransition(.GotTransmitControl, from: .QueryStoredData, to: .RetrieveData)
        stateMachine.addTransition(.TurnedTransmitControlOn, from: .RetrieveData, to: .RetrievingData)
        stateMachine.addTransition(.GotData, from: .RetrievingData, to: .SetupReporter0)
        stateMachine.addTransition(.Reporter0AttributesSet, from: .SetupReporter0, to: .SetupReporter1)
        stateMachine.addTransition(.Reporter1AttributesSet, from: .SetupReporter1, to: .SetupReporter2)
        stateMachine.addTransition(.Reporter2AttributesSet, from: .SetupReporter2, to: .EnableReporters)
        stateMachine.addTransition(.ReporterEnabled, from: .EnableReporters, to: .EnsureTransmitControlOn)
        stateMachine.addTransition(.TurnedTransmitControlOn, from: .EnsureTransmitControlOn, to: .Done)
        stateMachine.addTransition(.Disconnected, from: .Done, to: .QueryTime)
    }
    
    func processResult(data: DataPacket) {
        debugPrint("state machine processing result \(stateMachine.state) --- \(data)")
        
        if stateMachine.state == DeviceLifecycle.QueryLock && data is LockPacket {
            let lock: LockPacket = data as! LockPacket
            
            debugPrint("QueryLock is what is going on \(lock.lock)")
            
            if lock.lock == 0 {
                stateMachine.advance(DeviceTransition.NeedLedCode)
                ledCodeNeeded = true
            } else {
                stateMachine.advance(DeviceTransition.GotLock)
            }
            
            debugPrint("state on exit of QueryLock is \(stateMachine.state)")
            
            return
        }
        
        if stateMachine.state.rawValue.lowercaseString.hasPrefix("sendledcode") {
            debugPrint("led code received, moving on")
            stateMachine.advance(stateMachine.state.getTransition())
            return
        }
        
        if stateMachine.state == DeviceLifecycle.RetrievingData {
            recordsDownloaded += 1
            
            if recordsDownloaded < totalRecordsToDownload {
                debugPrint("downloading data: \(recordsDownloaded) \(totalRecordsToDownload)")
                return
            }
        }
        
        if stateMachine.state == DeviceLifecycle.QueryTime && data is TimePacket {
            debugPrint("Got time")
            let time: TimePacket = data as! TimePacket
            deviceTime = time.timestamp * 1000
            
          if abs(time.received.timeIntervalSince1970 - time.timestamp) < 60 {
                debugPrint("Time is correct")
               timeIsCorrect = true
            } else {
                timeDiff = time.received.timeIntervalSince1970 - time.timestamp
                debugPrint("time wrong timeDiff = \(timeDiff)")
            }
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReporter0 && expectedReport0Attributes.isEqual(data) {
            debugPrint("reporter 0 is right")
            reporter0Right = true
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReporter1 && expectedReport1Attributes.isEqual(data) {
            debugPrint("reporter 1 is right")
            reporter1Right = true
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReporter2 && expectedReport2Attributes.isEqual(data) {
            debugPrint("reporter2 is right")
            reporter2Right = true
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReportControl {
            let reportControl = data.reportControl
            debugPrint("looking at reportControl \(reportControl)")
            
            if reportControl & 0x01 == 0x01 {
                debugPrint("reporter 0 is on")
                reporter0On = true
            }
            
            if reportControl & 0x02 == 0x02 {
                debugPrint("reporter 1 is on")
                reporter1On = true
            }
            
            if reportControl & 0x04 == 0x04 {
                debugPrint("reporter 2 is on")
                reporter2On = true
            }
        }
        
        if stateMachine.state == DeviceLifecycle.QueryStoredData {
            // check to avoid crash
          guard let transmitControl = data as? TransmitControlData else {
            debugPrint("can't convert data to TransmitControlData")
            return
          }
          totalRecordsToDownload = transmitControl.totalRecordCount
          debugPrint("records to download = \(totalRecordsToDownload)")

        }
      
        if stateMachine.state == DeviceLifecycle.RetrieveData {
            debugPrint("transmit control is on")
            transmitControlOn = true
        }
        
        stateMachine.advance(stateMachine.state.getTransition())
        
        if stateMachine.state == DeviceLifecycle.SetTime && timeIsCorrect {
            debugPrint("time is correct next state please")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.DisableReporters && ((reporter0Right && reporter1Right && reporter2Right) || data.reportControl == 0) {
            debugPrint("reporters are right or already disabled, next!")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.RetrieveData && totalRecordsToDownload == 0 {
            debugPrint("no data to download next!")
            stateMachine.advance(stateMachine.state.getTransition())
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.SetupReporter0 && reporter0Right {
            debugPrint("no need to setup reporter 0")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.SetupReporter1 && reporter1Right {
            debugPrint("no need to setup reporter 1")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.SetupReporter2 && reporter2Right {
            debugPrint("no need to setup reporter 2")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.EnableReporters && reporter0On && reporter1On && reporter2On {
            debugPrint("reporters are all enabled next")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.EnsureTransmitControlOn && transmitControlOn {
            debugPrint("trasmit control is already on! next")
            stateMachine.advance(stateMachine.state.getTransition())
        }
        
        if stateMachine.state == DeviceLifecycle.DisableReporters {
            debugPrint("Reporters might be enabled, but disabling")
            reporter0On = false
            reporter1On = false
            reporter2On = false
        }
        
        debugPrint("state on exit of processResult is \(stateMachine.state)")
    }
    
    func disconnected() {
        stateMachine.advance(DeviceTransition.Disconnected)
    }
    
    func reset() {
        stateMachine = StateMachine<DeviceLifecycle, DeviceTransition>(state: .QueryLock)
        stateMachine.addTransition(.NeedLedCode, from: .QueryLock, to: .SendLedCode1)
        stateMachine.addTransition(.GotLock, from: .QueryLock, to: .QueryTime)
        stateMachine.addTransition(.GotTime, from: .QueryTime, to: .SetTime)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode1, to: .SendLedCode2)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode2, to: .SendLedCode3)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode3, to: .SendLedCode4)
        stateMachine.addTransition(.CodeWasReceived, from: .SendLedCode4, to: .QueryTime)
        stateMachine.addTransition(.Disconnected, from: .SendLedCode4, to: .QueryLock)
        stateMachine.addTransition(.TimeWasSet, from: .SetTime, to: .QueryReportControl)
        stateMachine.addTransition(.GotReporterControl, from: .QueryReportControl, to: .QueryReporter0)
        stateMachine.addTransition(.GotReporter0Attriburtes, from: .QueryReporter0, to: .QueryReporter1)
        stateMachine.addTransition(.GotReporter1Attriburtes, from: .QueryReporter1, to: .QueryReporter2)
        stateMachine.addTransition(.GotReporter2Attriburtes, from: .QueryReporter2, to: .DisableReporters)
        stateMachine.addTransition(.ReportersDisabled, from: .DisableReporters, to: .QueryStoredData)
        stateMachine.addTransition(.GotTransmitControl, from: .QueryStoredData, to: .RetrieveData)
        stateMachine.addTransition(.TurnedTransmitControlOn, from: .RetrieveData, to: .RetrievingData)
        stateMachine.addTransition(.GotData, from: .RetrievingData, to: .SetupReporter0)
        stateMachine.addTransition(.Reporter0AttributesSet, from: .SetupReporter0, to: .SetupReporter1)
        stateMachine.addTransition(.Reporter1AttributesSet, from: .SetupReporter1, to: .SetupReporter2)
        stateMachine.addTransition(.Reporter2AttributesSet, from: .SetupReporter2, to: .EnableReporters)
        stateMachine.addTransition(.ReporterEnabled, from: .EnableReporters, to: .EnsureTransmitControlOn)
        stateMachine.addTransition(.TurnedTransmitControlOn, from: .EnsureTransmitControlOn, to: .Done)
        stateMachine.addTransition(.Disconnected, from: .Done, to: .QueryTime)

    }
    
    func getState() -> DeviceLifecycle {
        return stateMachine.state
    }
    
    func isTimeCorrect() -> Bool {
        return timeIsCorrect
    }
    
    func isLedCodeNeeded() -> Bool {
        return ledCodeNeeded
    }
    
    func getDeviceTime() -> Double {
        return deviceTime
    }
}