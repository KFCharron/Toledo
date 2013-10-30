package com.mediacrossing.publishercheckup;

import java.io.Serializable;

public class IdName implements Serializable {

    private String id;
    private String name;

    public IdName(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
