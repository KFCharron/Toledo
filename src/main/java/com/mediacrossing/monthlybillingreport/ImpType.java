package com.mediacrossing.monthlybillingreport;

public class ImpType {
    private String name;
    private int imps;
    private float grossRevenue;
    private float publisherRevenue;
    private float profit;
    private float anServingFee;
    private float anExchangeFee;
    private float amazonFee;
    private float evidonFee;
    private float totalFees;
    private float pnl;
    private float amazonCpm = .005f;

    public ImpType(String name, int imps, float grossRevenue, float publisherRevenue) {
        this.name = name;
        this.imps = imps;
        this.grossRevenue = grossRevenue;
        this.publisherRevenue = publisherRevenue;
        this.profit = grossRevenue - publisherRevenue;
        if (!name.contains("Error") && !name.equals("Resold") && !name.contains("External")) {
            this.anServingFee = imps * .017f / 1000;
        } else this.anServingFee = 0;
        if (name.equals("Resold")) {
            this.anExchangeFee = grossRevenue * .0135f;
        } else this.anExchangeFee = 0;
        if (!name.contains("External")) {
            this.amazonFee = imps * amazonCpm / 1000;
            if (name.equals("Kept")) {
                this.evidonFee = imps * .01f / 1000;
            } else this.evidonFee = 0;
        } else {
            this.amazonFee = 0;
            this.evidonFee = 0;
        }

        this.totalFees = this.anServingFee + this.anExchangeFee + this.amazonFee + this.evidonFee;
        this.pnl = this.profit - this.totalFees;
    }

    public ImpType() {
        this.imps = 0;
        this.grossRevenue = 0;
        this.publisherRevenue = 0;
        this.profit = 0;
        this.anServingFee = 0;
        this.anExchangeFee = 0;
        this.amazonFee = 0;
        this.evidonFee = 0;
        this.totalFees = 0;
        this.pnl = 0;
    }

    public String getName() {
        return name;
    }

    public int getImps() {
        return imps;
    }

    public float getGrossRevenue() {
        return grossRevenue;
    }

    public float getPublisherRevenue() {
        return publisherRevenue;
    }

    public float getProfit() {
        return profit;
    }

    public float getAnServingFee() {
        return anServingFee;
    }

    public float getAnExchangeFee() {
        return anExchangeFee;
    }

    public float getAmazonFee() {
        return amazonFee;
    }

    public float getEvidonFee() {
        return evidonFee;
    }

    public float getTotalFees() {
        return totalFees;
    }

    public float getPnl() {
        return pnl;
    }

    public void setImps(int imps) {
        this.imps = imps;
    }

    public void setGrossRevenue(float grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public void setPublisherRevenue(float publisherRevenue) {
        this.publisherRevenue = publisherRevenue;
    }

    public void setProfit(float profit) {
        this.profit = profit;
    }

    public void setAnServingFee(float anServingFee) {
        this.anServingFee = anServingFee;
    }

    public void setAnExchangeFee(float anExchangeFee) {
        this.anExchangeFee = anExchangeFee;
    }

    public void setAmazonFee(float amazonFee) {
        this.amazonFee = amazonFee;
    }

    public void setEvidonFee(float evidonFee) {
        this.evidonFee = evidonFee;
    }

    public void setTotalFees(float totalFees) {
        this.totalFees = totalFees;
    }

    public void setPnl(float pnl) {
        this.pnl = pnl;
    }
}
