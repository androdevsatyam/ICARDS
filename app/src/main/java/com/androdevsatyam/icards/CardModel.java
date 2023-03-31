package com.androdevsatyam.icards;

/**
 * Created by SATYAM on 09/12/22.
 * Associated with IOVRVF
 * Contact me on: satyamiovrvf@gmail.com
 */
public class CardModel {
    String designation,name,code;

    public CardModel(String designation, String name, String code) {
        this.designation = designation;
        this.name = name;
        this.code = code;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
