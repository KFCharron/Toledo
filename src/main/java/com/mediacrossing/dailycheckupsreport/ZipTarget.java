package com.mediacrossing.dailycheckupsreport;

import java.io.Serializable;

public class ZipTarget implements Serializable{
    private String fromZip;
    private String toZip;

    public ZipTarget (String fromZip, String toZip) {
        this.fromZip = fromZip;
        this.toZip = toZip;
    }

    public String getFromZip() {
        return fromZip;
    }

    public String getToZip() {
        return toZip;
    }
}
