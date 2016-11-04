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
        
        transitions[first]?[transition] = second
    }
    
    func canAdvance(transition: Transition) -> Bool {
        return transitions[state]?[transition] != nil
    }
    
    typealias Observer = (_ oldState: State, _ newState: State) -> ()
    
    func advance(transition: Transition, observe: Observer? = nil) -> State {
        let prev = state
        
        if let next = transitions[prev]?[transition] {
            if next != prev {
                state = next
                
                observe?(prev, next)
            }
        }
        
        return state
    }
}
