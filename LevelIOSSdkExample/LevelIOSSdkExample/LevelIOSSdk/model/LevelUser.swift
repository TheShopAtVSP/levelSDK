//
// Created by Andrew Cook on 6/22/16.
// Copyright (c) 2016 TheShop. All rights reserved.
//

import Foundation

enum Gender: String {
    case Male, Female
}

class LevelUser: NSObject {
    var id: String
    var name: String
    var username: String
    var email: String
    var weightLbs: Double
    var heightFeet: Int
    var heightInches: Int
    var age: Int
    var gender: Gender
    var friends: [String]
    var charity: Charity
    var glassName: String
    var deviceId: String?

    init(name: String, email: String, weightLbs: Double, heightFeet: Int, heightInches: Int,
         age: Int, gender: Gender, friends: [String], charity: Charity) {
        self.id = ""
        self.username = ""
        self.name = name
        self.email = email
        self.weightLbs = weightLbs
        self.heightFeet = heightFeet
        self.heightInches = heightInches
        self.age = age
        self.gender = gender;
        self.friends = friends
        self.charity = charity
        self.glassName = ""
        self.deviceId = ""
    }
}
