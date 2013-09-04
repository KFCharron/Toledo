package com.mediacrossing.segmenttargeting;

public class ServingFee {
    private String brokerName;
    private String paymentType;
    private String value;
    private String description;

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
}
