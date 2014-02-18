package com.mediacrossing.sellerList;

import com.mediacrossing.connections.AppNexusService;
import com.mediacrossing.properties.ConfigurationProperties;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class RunSellerList {

    public static void main(String[] args) throws Exception {

        ConfigurationProperties p = new ConfigurationProperties(args);
        AppNexusService an = new AppNexusService(p.getAppNexusUrl(), p.getAppNexusUsername(), p.getAppNexusPassword());
        final List<Seller> s = an.getSellers();
        PrintWriter pw = new PrintWriter(new FileOutputStream("/Users/charronkyle/Desktop/Reports/Sellers.tsv"));
        for (Seller x : s) {
            pw.println(x.name + "\t " + x.id + "\t " + x.type);
        }
        pw.close();
    }
}