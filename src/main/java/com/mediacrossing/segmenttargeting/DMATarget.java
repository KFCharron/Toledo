package com.mediacrossing.segmenttargeting;

public class DMATarget {
    private String dma;
    private String name;

    public DMATarget(String dma, String name) {
        this.dma = dma;
        this.name = name;
    }

    public String getDma() {
        return dma;
    }

    public void setDma(String dma) {
        this.dma = dma;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
