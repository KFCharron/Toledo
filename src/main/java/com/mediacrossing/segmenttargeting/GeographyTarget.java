package com.mediacrossing.segmenttargeting;

import java.util.ArrayList;

public class GeographyTarget {
    private ArrayList<CountryTarget> countryTargetList = new ArrayList<CountryTarget>();
    private ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();
    private ArrayList<ZipTarget> zipTargetList = new ArrayList<ZipTarget>();
    private String countryAction;
    private String dmaAction;

    public GeographyTarget (ArrayList<CountryTarget> countryTargetList,
                            ArrayList<DMATarget> dmaTargetList, String countryAction,
                            String dmaAction, ArrayList<ZipTarget> zipTargetList) {

        this.countryTargetList = countryTargetList;
        this.dmaTargetList = dmaTargetList;
        this.countryAction = countryAction;
        this.dmaAction = dmaAction;
        this.zipTargetList = zipTargetList;
    }

    public ArrayList<CountryTarget> getCountryTargetList() {
        return countryTargetList;
    }

    public String getCountryAction() {
        return countryAction;
    }

    public ArrayList<DMATarget> getDmaTargetList() {
        return dmaTargetList;
    }

    public String getDmaAction() {
        return dmaAction;
    }

    public ArrayList<ZipTarget> getZipTargetList() {
        return zipTargetList;
    }
}
