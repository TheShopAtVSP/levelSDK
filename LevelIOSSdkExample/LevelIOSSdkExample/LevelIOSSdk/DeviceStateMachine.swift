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
        SetTime, QueryReportControl, QueryReporter0, QueryReporter1, QueryReporter2, Done, Unknown
    
    func getCommand() -> DeviceCommand {
        switch self {
        case .QueryTime: return DeviceCommand.TimeRD
        case .SetTime: return DeviceCommand.TimeWR
        case .QueryReportControl: return DeviceCommand.ReportControl
        case .QueryReporter0, .QueryReporter1, .QueryReporter2: return DeviceCommand.ReporterAttributes
        case .QueryLock: return DeviceCommand.LockRD
        default: return DeviceCommand.Unknown
        }
    }
    
    func getPacket() -> [UInt8] {
        switch self {
        case .SetTime:
            return BitsHelper.convertTo4Bytes(time: NSDate().timeIntervalSince1970)
        case .QueryReporter0:
            let bytes: [UInt8] = [0]
            return bytes
        case .QueryReporter1:
            let bytes: [UInt8] = [1]
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
    var reporter0Config: ReportAttributes? = nil, reporter1Config: ReportAttributes? = nil, reporter2Config: ReportAttributes? = nil
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
        
        stateMachine.addTransition(transition: .NeedLedCode, from: .QueryLock, to: .SendLedCode1)
        stateMachine.addTransition(transition: .GotLock, from: .QueryLock, to: .QueryTime)
        stateMachine.addTransition(transition: .GotTime, from: .QueryTime, to: .SetTime)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode1, to: .SendLedCode2)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode2, to: .SendLedCode3)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode3, to: .SendLedCode4)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode4, to: .QueryTime)
        stateMachine.addTransition(transition: .Disconnected, from: .SendLedCode4, to: .QueryLock)
        stateMachine.addTransition(transition: .TimeWasSet, from: .SetTime, to: .QueryReportControl)
        stateMachine.addTransition(transition: .GotReporterControl, from: .QueryReportControl, to: .QueryReporter0)
        stateMachine.addTransition(transition: .GotReporter0Attriburtes, from: .QueryReporter0, to: .QueryReporter1)
        stateMachine.addTransition(transition: .GotReporter1Attriburtes, from: .QueryReporter1, to: .QueryReporter2)
        stateMachine.addTransition(transition: .GotReporter2Attriburtes, from: .QueryReporter2, to: .Done)
    }
    
    func processResult(data: DataPacket) {
        debugPrint("state machine processing result \(stateMachine.state) --- \(data)")
        
        if stateMachine.state == DeviceLifecycle.QueryLock && data is LockPacket {
            let lock: LockPacket = data as! LockPacket
            
            debugPrint("QueryLock is what is going on \(lock.lock)")
            
            if lock.lock == 0 {
                _ = stateMachine.advance(transition: DeviceTransition.NeedLedCode)
                ledCodeNeeded = true
            } else {
                _ = stateMachine.advance(transition: DeviceTransition.GotLock)
            }
            
            debugPrint("state on exit of QueryLock is \(stateMachine.state)")
            
            return
        }
        
        if stateMachine.state.rawValue.lowercased().hasPrefix("sendledcode") {
            debugPrint("led code received, moving on")
            _ = stateMachine.advance(transition: stateMachine.state.getTransition())
            return
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
        
        if stateMachine.state == DeviceLifecycle.QueryReporter0 && data is ReportAttributes {
            debugPrint("reporter 0 is right")
            reporter0Config = data as? ReportAttributes
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReporter1 && data is ReportAttributes {
            debugPrint("reporter 1 is right")
            reporter1Config = data as? ReportAttributes
        }
        
        if stateMachine.state == DeviceLifecycle.QueryReporter2 && data is ReportAttributes {
            debugPrint("reporter2 is right")
            reporter0Config = data as? ReportAttributes
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
        
        _ = stateMachine.advance(transition: stateMachine.state.getTransition())
        
        if stateMachine.state == DeviceLifecycle.SetTime && timeIsCorrect {
            debugPrint("time is correct next state please")
            _ = stateMachine.advance(transition: stateMachine.state.getTransition())
        }
        
        debugPrint("state on exit of processResult is \(stateMachine.state)")
    }
    
    func disconnected() {
        _ = stateMachine.advance(transition: DeviceTransition.Disconnected)
    }
    
    func reset() {
        stateMachine = StateMachine<DeviceLifecycle, DeviceTransition>(state: .QueryLock)
        stateMachine.addTransition(transition: .NeedLedCode, from: .QueryLock, to: .SendLedCode1)
        stateMachine.addTransition(transition: .GotLock, from: .QueryLock, to: .QueryTime)
        stateMachine.addTransition(transition: .GotTime, from: .QueryTime, to: .SetTime)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode1, to: .SendLedCode2)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode2, to: .SendLedCode3)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode3, to: .SendLedCode4)
        stateMachine.addTransition(transition: .CodeWasReceived, from: .SendLedCode4, to: .QueryTime)
        stateMachine.addTransition(transition: .Disconnected, from: .SendLedCode4, to: .QueryLock)
        stateMachine.addTransition(transition: .TimeWasSet, from: .SetTime, to: .QueryReportControl)
        stateMachine.addTransition(transition: .GotReporterControl, from: .QueryReportControl, to: .QueryReporter0)
        stateMachine.addTransition(transition: .GotReporter0Attriburtes, from: .QueryReporter0, to: .QueryReporter1)
        stateMachine.addTransition(transition: .GotReporter1Attriburtes, from: .QueryReporter1, to: .QueryReporter2)
        stateMachine.addTransition(transition: .GotReporter2Attriburtes, from: .QueryReporter2, to: .Done)
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
