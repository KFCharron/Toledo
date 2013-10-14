package com.mediacrossing.segmenttargeting;

import java.io.Serializable;

public class DMATarget implements Serializable{
    private String dma;
    private String name;

    public DMATarget(String dma, String name) {
        this.dma = dma;
        this.name = name;
    }

    public String getDma() {
        return dma;
    }

    public String getName() {
        return name;
    }

}
