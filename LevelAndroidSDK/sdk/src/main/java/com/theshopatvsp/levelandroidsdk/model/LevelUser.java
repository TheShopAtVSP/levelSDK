package com.theshopatvsp.levelandroidsdk.model;

import com.theshopatvsp.levelandroidsdk.model.constants.Charity;
import com.theshopatvsp.levelandroidsdk.model.constants.Gender;

import java.io.Serializable;
import java.util.List;

/**
 * Created by andrco on 6/13/16.
 */
public class LevelUser implements Serializable {
    private String id;
    private String name;
    private String username;
    private String email;
    private double weightLbs;
    private int heightFeet;
    private int heightInches;
    private int age;
    private Gender gender;
    private List<String> friends;

    private int baseGoal;
    private int stretchGoal;
    private Charity charity;

    public LevelUser() {}
    public LevelUser(String name, String email, double weightLbs, int heightFeet, int heightInches,
                     int age, Gender gender, int baseGoal, int stretchGoal, Charity charity) {
        this.name = name;
        this.email = email;
        this.weightLbs = weightLbs;
        this.heightFeet = heightFeet;
        this.heightInches = heightInches;
        this.age = age;
        this.gender = gender;
        this.baseGoal = baseGoal;
        this.stretchGoal = stretchGoal;
        this.charity = charity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getWeightLbs() {
        return weightLbs;
    }

    public void setWeightLbs(double weightLbs) {
        this.weightLbs = weightLbs;
    }

    public int getHeightFeet() {
        return heightFeet;
    }

    public void setHeightFeet(int heightFeet) {
        this.heightFeet = heightFeet;
    }

    public int getHeightInches() {
        return heightInches;
    }

    public void setHeightInches(int heightInches) {
        this.heightInches = heightInches;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public int getBaseGoal() {
        return baseGoal;
    }

    public void setBaseGoal(int baseGoal) {
        this.baseGoal = baseGoal;
    }

    public int getStretchGoal() {
        return stretchGoal;
    }

    public void setStretchGoal(int stretchGoal) {
        this.stretchGoal = stretchGoal;
    }

    public Charity getCharity() {
        return charity;
    }

    public void setCharity(Charity charity) {
        this.charity = charity;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
