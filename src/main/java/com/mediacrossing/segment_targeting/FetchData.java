package com.mediacrossing.segment_targeting;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;
import com.google.gson.*;

/**
 * Created with IntelliJ IDEA.
 * User: charronkyle
 * Date: 8/20/13
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class FetchData {

    public static void main(String[] args) throws Exception {

        FetchData http = new FetchData();
        String authToken = http.requestAuthorizationToken();
        String profileData = http.requestProfiles(authToken);
        ArrayList<Profile> profileList = http.parseData(profileData);
        http.writeToCSV(profileList);
    }

    // HTTP POST request
    private String requestAuthorizationToken() throws Exception {

        String url = "https://api.appnexus.com/auth";
        URL obj = new URL(url);
        HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");

        //Authorization JSON data
        String urlParameters = "{\"auth\":{\"username\":\"MC_report\",\"password\":\"13MediaCrossing!\"}}";

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //Received JSON data
        String rawJSON = response.toString();
        System.out.println(rawJSON);

        //Parse JSON, obtain token
        JsonElement jelement = new JsonParser().parse(rawJSON);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        String token = jobject.get("token").toString();
        token = token.replace("\"","");
        System.out.println(token);

        return token;
    }

    // HTTP GET request
    private String requestProfiles(String token) throws Exception {

        String url = "http://api.appnexus.com/profile?advertiser_id=165002";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        //default connection is GET

        //add token header
        con.setRequestProperty("Authorization", token);

        //Send GET request
        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        String rawJSON = response.toString();
        System.out.println(rawJSON);

        return rawJSON;
    }

    //Parse JSON into Profile Objects, add them to a list
    private ArrayList<Profile> parseData(String data) {

        //Parse JSON, obtain data
        JsonElement jelement = new JsonParser().parse(data);
        JsonObject  jobject = jelement.getAsJsonObject();
        jobject = jobject.getAsJsonObject("response");
        JsonArray jarray = jobject.getAsJsonArray("profiles");

        //Create a profile list
        ArrayList<Profile> profileList = new ArrayList<Profile>();

        //Create a profile, populate it, add it to the profile list
        for(int x = 0; x < jarray.size(); x++) {

            //Create new profile
            Profile newProfile = new Profile();
            //Set the json object
            jobject = jarray.get(x).getAsJsonObject();
            //Set ID
            newProfile.setId(jobject.get("id").toString());

            //Set Frequency Targets
            FrequencyTargets frequencyTargets = new FrequencyTargets();
            frequencyTargets.setMaxDayImps(jobject.get("max_day_imps").toString());
            frequencyTargets.setMaxLifetimeImps(jobject.get("max_lifetime_imps").toString());
            frequencyTargets.setMaxPageImps(jobject.get("max_page_imps").toString());
            frequencyTargets.setMaxSessionImps(jobject.get("max_session_imps").toString());
            frequencyTargets.setMinMinutesPerImp(jobject.get("min_minutes_per_imp").toString());
            frequencyTargets.setMinSessionImps(jobject.get("min_session_imps").toString());
            newProfile.setFrequencyTargets(frequencyTargets);

            JsonArray karray;
            //Move to daypart Array
            if(!jobject.get("daypart_targets").isJsonNull()) {
                karray = jobject.getAsJsonArray("daypart_targets");
                ArrayList<DaypartTarget> daypartTargetList = new ArrayList<DaypartTarget>();
                for(int y = 0; y < karray.size(); y++) {
                    JsonObject kobject = karray.get(y).getAsJsonObject();
                    DaypartTarget newDaypart = new DaypartTarget();
                    //add variables to DaypartTarget
                    newDaypart.setDay(kobject.get("day").toString());
                    newDaypart.setStartHour(kobject.get("start_hour").getAsInt());
                    newDaypart.setEndHour(kobject.get("end_hour").getAsInt());
                    //add DaypartTargat to daypartTargetList
                    daypartTargetList.add(y, newDaypart);
                }
                //Add daypartTargetList to profile
                newProfile.setDaypartTargetList(daypartTargetList);
            }


            //Create new GeographyTargets
            GeographyTargets geographyTargets = new GeographyTargets();
            //Move to country target array
            //check for null value
            if (!jobject.get("country_targets").isJsonNull()) {
                karray = jobject.getAsJsonArray("country_targets");
                //Create new country list
                ArrayList<CountryTarget> countryTargetList = new ArrayList<CountryTarget>();
                for (int z = 0; z < karray.size(); z++) {
                    JsonObject lobject = karray.get(z).getAsJsonObject();
                    CountryTarget newCountry = new CountryTarget();
                    newCountry.setCountry(lobject.get("country").toString());
                    newCountry.setName(lobject.get("name").toString());
                    countryTargetList.add(z, newCountry);
                }
                //Add countryTargetList to geography targets
                geographyTargets.setCountryTargetList(countryTargetList);
            }

            //set country action
            geographyTargets.setCountryAction(jobject.get("country_action").toString());

            //Check for null value
            if (!jobject.get("dma_targets").isJsonNull()) {
                //Move to dma target array
                karray = jobject.getAsJsonArray("dma_targets");
                //Create new dma list
                ArrayList<DMATarget> dmaTargetList = new ArrayList<DMATarget>();
                for (int i = 0; i < karray.size(); i++) {
                    JsonObject pobject = karray.get(i).getAsJsonObject();
                    DMATarget newDMATarget = new DMATarget();
                    newDMATarget.setDma(pobject.get("dma").toString());
                    newDMATarget.setName(pobject.get("name").toString());
                    dmaTargetList.add(i, newDMATarget);
                }
                //Add dmaTargetList to geography targets
                geographyTargets.setDmaTargetList(dmaTargetList);
            }

            //set dma action
            geographyTargets.setDmaAction(jobject.get("dma_action").toString());
            //Add geography target to profile
            newProfile.setGeographyTargets(geographyTargets);

            //Add completed profile to the list
            profileList.add(x, newProfile);
        }

        return profileList;
    }

    //Take profileList, write to CSV file
    private static final String CSV_SEPARATOR = ",";
    private static void writeToCSV(ArrayList<Profile> profileList)
    {
        //Frequency csv
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("frequencyReport.csv"), "UTF-8"));
            bw.write("Profile ID, MaxImps/Person, MinImps/Person/Session, MaxImps/Person/Session," +
                    " MaxImps/Person/Day, MinMinutesBetweenImps, MaxImpsPerPageLoad");
            bw.newLine();
            for (Profile profile : profileList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(profile.getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMaxLifetimeImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMinSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMaxSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMaxDayImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMinMinutesPerImp());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getFrequencyTargets().getMaxPageImps());
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}

        //Daypart CSV
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("daypartReport.csv"), "UTF-8"));
            bw.write("Profile ID, Days, 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
            bw.newLine();
            for (Profile profile : profileList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(profile.getId());
                oneLine.append(CSV_SEPARATOR);

                for (int b = 0; b < profile.getDaypartTargetList().size(); b++) {
                    oneLine.append(profile.getDaypartTargetList().get(b).getDay());
                    for(int c = 0; c < 23; c++) {
                       if(c >= profile.getDaypartTargetList().get(b).getStartHour() &&
                               c <= profile.getDaypartTargetList().get(b).getEndHour()) {
                           oneLine.append("X");
                           oneLine.append(CSV_SEPARATOR);
                       }
                       else {
                           oneLine.append(" ");
                           oneLine.append(CSV_SEPARATOR);
                       }
                    }
                }


                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }
}
