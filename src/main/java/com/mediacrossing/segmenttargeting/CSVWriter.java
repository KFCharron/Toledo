package com.mediacrossing.segmenttargeting;

import java.io.*;
import java.util.ArrayList;

public class CSVWriter {

    private static final String CSV_SEPARATOR = ",";

    public void writeFrequencyFile(ArrayList<Campaign> campaignArrayList) {
        //Frequency csv
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("frequencyReport.csv"), "UTF-8"));
            bw.write("Profile ID, Name, MaxImps/Person, MinImps/Person/Session, MaxImps/Person/Session," +
                    " MaxImps/Person/Day, MinMinutesBetweenImps, MaxImpsPerPageLoad");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getName());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxLifetimeImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMinSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxSessionImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxDayImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMinMinutesPerImp());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxPageImps());
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

    public void writeDaypartFile(ArrayList<Campaign> campaignArrayList) {
        //Daypart CSV
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("daypartReport.csv"), "UTF-8"));
            bw.write("ID, Name, Days, 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getName());
                oneLine.append(CSV_SEPARATOR);

                for (int b = 0; b < campaign.getDaypartTargetArrayList().size(); b++) {
                    oneLine.append(campaign.getDaypartTargetArrayList().get(b).getDay());
                    for(int c = 0; c < 23; c++) {
                        if(c >= campaign.getDaypartTargetArrayList().get(b).getStartHour() &&
                                c <= campaign.getDaypartTargetArrayList().get(b).getEndHour()) {
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

    public void writeGeographyFile(ArrayList<Campaign> campaignArrayList) {
        //Geography CSV
        //TODO
    }

    public void writeSegmentFIle(ArrayList<Campaign> campaignArrayList) {
        //TODO
    }
}
