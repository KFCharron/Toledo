package com.mediacrossing.segmenttargeting;

import java.io.Serializable;

public class CountryTarget implements Serializable{
    private String country;
    private String name;

    public CountryTarget(String country, String name) {
        this.country = country;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

}
