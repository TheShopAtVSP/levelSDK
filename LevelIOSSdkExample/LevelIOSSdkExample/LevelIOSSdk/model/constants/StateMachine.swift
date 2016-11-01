//
//  StateMachine.swift
//  LevelIOSSDK
//
//  Created by Andrew Cook on 6/29/16.
//  Copyright © 2016 TheShop. All rights reserved.
//

import Foundation

class StateMachine<State: Hashable, Transition: Hashable> {
    var state: State
    var transitions = [State: [Transition: State]]()
    
    init(state: State) {
        self.state = state
    }
    
    func addTransition(transition: Transition, from first: State, to second: State) {
        if( transitions[first] == nil ) {
            transitions[first] = [:]
        }
        
        debugPrint("StateMachine: addTransition \(transition) - \(first) - \(second)")
        
        transitions[first]?[transition] = second
    }
    
    func canAdvance(transition: Transition) -> Bool {
        return transitions[state]?[transition] != nil
    }
    
    typealias Observer = (oldState: State, newState: State) -> ()
    
    func advance(transition: Transition, observe: Observer? = nil) -> State {
        let prev = state
        debugPrint("StateMachine: advance \(prev) - \(transition)")
        
        if let next = transitions[prev]?[transition] {
            debugPrint("StateMachine: advance next = \(next)")
            if next != prev {
                debugPrint("StateMachine: advance advancing state")
                state = next
                
                observe?(oldState: prev, newState: next)
            }
        }
        
        debugPrint("StateMachine: advance on exit = \(state)")
        
        return state
    }
}