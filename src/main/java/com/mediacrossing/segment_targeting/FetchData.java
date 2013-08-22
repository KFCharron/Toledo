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

        //Add each profile to the list
        for(int x = 0; x < jarray.size(); x++) {

            //Create new profile
            Profile newProfile = new Profile();
            jobject = jarray.get(x).getAsJsonObject();

            //Add id, frequency metrics
            newProfile.setId(jobject.get("id").toString());
            newProfile.setMaxDayImps(jobject.get("max_day_imps").toString());
            newProfile.setMaxLifetimeImps(jobject.get("max_lifetime_imps").toString());
            newProfile.setMaxPageImps(jobject.get("max_page_imps").toString());
            newProfile.setMaxSessionImps(jobject.get("max_session_imps").toString());
            newProfile.setMinMinutesPerImp(jobject.get("min_minutes_per_imp").toString());
            newProfile.setMinSessionImps(jobject.get("min_session_imps").toString());

            //Move to daypart Array
            JsonArray karray = jobject.getAsJsonArray("daypart_targets");
            ArrayList<DaypartTarget> daypartTargetList = new ArrayList<DaypartTarget>();
            for(int y = 0; y < karray.size(); y++) {
                JsonObject kobject = karray.get(y).getAsJsonObject();
                DaypartTarget newDaypart = new DaypartTarget();

                //add variables to DaypartTarget
                newDaypart.setDay(kobject.get("day").toString());
                newDaypart.setStartHour(kobject.get("start_hour").toString());
                newDaypart.setEndHour(kobject.get("end_hour").toString());
                //add DaypartTargat to daypartTargetList
                daypartTargetList.add(y, newDaypart);
            }
            //Add daypartTargetList to profile
            newProfile.setDaypartTargetList(daypartTargetList);

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
                oneLine.append(profile.getMaxLifetimeImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getMinSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getMaxSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getMaxDayImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getMinMinutesPerImp());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(profile.getMaxPageImps());
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
