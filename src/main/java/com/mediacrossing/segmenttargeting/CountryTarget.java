package com.mediacrossing.segmenttargeting;

public class CountryTarget {
    private String country;
    private String name;

    public CountryTarget(String country, String name) {
        this.country = country;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
