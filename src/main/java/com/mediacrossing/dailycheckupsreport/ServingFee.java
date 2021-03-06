package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;

public class ServingFee implements Serializable{
    private String brokerName;
    private String paymentType;
    private String value;
    private String description;
    private float totalFee;

    public ServingFee(String brokerName, String paymentType, String value, String description) {
        this.brokerName = brokerName;
        this.paymentType = paymentType;
        this.value = value;
        this.description = description;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public float getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(float totalFee) {
        this.totalFee = totalFee;
    }
}
