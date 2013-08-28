package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class GeographyTarget {
    private ArrayList<CountryTarget> countryTargetList = new ArrayList<CountryTarget>();
    private ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();
    private String countryAction;
    private String dmaAction;

    public ArrayList<CountryTarget> getCountryTargetList() {
        return countryTargetList;
    }

    public void setCountryTargetList(ArrayList<CountryTarget> countryTargetList) {
        this.countryTargetList = countryTargetList;
    }

    public String getCountryAction() {
        return countryAction;
    }

    public void setCountryAction(String countryAction) {
        this.countryAction = countryAction;
    }

    public ArrayList<DMATarget> getDmaTargetList() {
        return dmaTargetList;
    }

    public void setDmaTargetList(ArrayList<DMATarget> dmaTargetList) {
        this.dmaTargetList = dmaTargetList;
    }

    public String getDmaAction() {
        return dmaAction;
    }

    public void setDmaAction(String dmaAction) {
        this.dmaAction = dmaAction;
    }
}
