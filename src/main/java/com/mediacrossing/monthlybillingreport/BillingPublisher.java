package com.mediacrossing.monthlybillingreport;

import java.util.ArrayList;

public class BillingPublisher {
    private String name;
    private ArrayList<ImpType> impTypes;

    public BillingPublisher(String name, ArrayList<ImpType> data) {
        this.name = name;
        this.impTypes = data;
    }

    public String getName() {
        return name;
    }

    public ArrayList<ImpType> getImpTypes() {
        return impTypes;
    }
}
