package com.jmm.www.calendar.weather;

/**
 * 市
 */
public class CityInfoModel {

    private String name;

    public CityInfoModel() {
        super();
    }

    public CityInfoModel(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return "CityInfoModel [name=" + name + "]";
    }
}