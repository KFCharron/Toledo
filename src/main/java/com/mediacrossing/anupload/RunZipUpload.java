package com.mediacrossing.anupload;

import au.com.bytecode.opencsv.CSVReader;
import com.mediacrossing.connections.AppNexusService;
import scala.Tuple2;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RunZipUpload {
    public static String anUrl = "http://api.appnexus.com/";
    public static String anUsername = "mediacrossing_api_user";
    public static String anPassword = "Z3^at0Fbr";
    public static String advertiserId = "283120";
    public static String profileId = "8796201";

    public static void main(String[] args) throws Exception {

        // Parse in zip code list
        InputStream is = new FileInputStream("/Users/charronkyle/Desktop/HomeServe Water Zip List.csv");
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
        List<String[]> csvData = reader.readAll();

        ArrayList<Integer> zips = new ArrayList<>();
        for (String[] s : csvData) {
            zips.add(Integer.valueOf(s[0]));
        }

        // Cut zip list into ranges
        ArrayList<Tuple2<Integer, Integer>> fromToList = new ArrayList<>();
        Integer start;
        Integer end = new Integer("0");
        for (int x = 0; x < zips.size(); x++) {
            if (zips.get(x).intValue() > end.intValue()) {
                start = zips.get(x);
                int y = 0;
                while ((x+y < zips.size()) && zips.get(x+y).intValue() == zips.get(x) + y) y++;
                end = zips.get(x+y-1);
                fromToList.add(new Tuple2<Integer, Integer>(start,end));
            }
        }

        System.out.println(fromToList.size());



        // Check to make sure < 4000 ranges
//        if (fromToList.size() > 4000) {
//            System.out.println("Size of Zip Code Ranges is " + fromToList.size() + ". This is larger than " +
//                "the AppNexus limit of 4000 zip code ranges. \n Exiting Program.");
//            System.exit(1);
//        }

        List<Tuple2<Integer, Integer>> shorter = fromToList.subList(5986, 8979);
        FileWriter writer = new FileWriter(new File("/Users/charronkyle/Desktop/Water3.csv"));
        for (Tuple2<Integer, Integer> t : shorter) {
            writer.write("," + t._1().intValue() + "," + t._2().intValue() + "\n");
        }
        writer.flush();
        writer.close();

        // method that takes all ranges and puts them into the json formatting
        String json = "{\"profile\":{\"zip_targets\":[";
        for (Tuple2<Integer, Integer> r : shorter) {
            String startString;
            String endString;

            if (r._1().intValue() < 1000) {
                startString = "00" + r._1().toString();
            } else if (r._1().intValue() < 10000) {
                startString = "0" + r._1().toString();
            } else startString = r._1().toString();

            if (r._2().intValue() < 1000) {
                endString = "00" + r._2().toString();
            } else if (r._2().intValue() < 10000) {
                endString = "0" + r._2().toString();
            } else endString = r._2().toString();

            String range = "{\"from_zip\":\"" + startString + "\",\"to_zip\":\"" + endString + "\"}";
            if (shorter.indexOf(r) < shorter.size() - 1) range = range.concat(",");
            json = json.concat(range);
        }
        json = json.concat("]}}");

        System.out.println(json);

        // call put w/ String
        AppNexusService anConn = new AppNexusService(anUrl, anUsername, anPassword);

        System.out.println(anConn.putRequest("/profile?id=" + profileId + "&advertiser_id=" + advertiserId, json));

    }
}
