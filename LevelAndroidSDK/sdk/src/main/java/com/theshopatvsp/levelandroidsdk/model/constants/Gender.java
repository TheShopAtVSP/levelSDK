package com.theshopatvsp.levelandroidsdk.model.constants;

/**
 * Created by andrco on 6/13/16.
 */
public enum Gender {
    MALE(0),
    FEMALE(1);

    private Gender(int id) {
        this.id = id;
    }

    private int id;

    public int getId() {
        return this.id;
    }

    public static Gender getByName(String name) {
        for(Gender gender : values()) {
            if(gender.name().equalsIgnoreCase(name)) {
                return gender;
            }
        }

        return null;
    }
}
