package com.mediacrossing.segmenttargeting;

import java.io.*;
import java.util.ArrayList;

public class CSVWriter {

    private static final String CSV_SEPARATOR = ",";

    public void writeFrequencyFile(ArrayList<Campaign> campaignArrayList) {
        //Frequency csv
        try
        {
            BufferedWriter bw =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("FrequencyReport.csv"), "UTF-8"));
            bw.write("Profile ID, Name, MaxImps/Person," +
                    " MaxImps/Person/Day, MinMinutesBetweenImps");
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
//                oneLine.append(campaign.getFrequencyTargets().getMinSessionImps());
//                oneLine.append(CSV_SEPARATOR);
//                oneLine.append(campaign.getFrequencyTargets().getMaxSessionImps());
//                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxDayImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMinMinutesPerImp());
//                oneLine.append(CSV_SEPARATOR);
//                oneLine.append(campaign.getFrequencyTargets().getMaxPageImps());
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
            BufferedWriter bw =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream("DaypartReport.csv"), "UTF-8"));
            bw.write("ID, Name, Days, 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {


                for (int b = 0; b < campaign.getDaypartTargetArrayList().size(); b++) {

                    StringBuffer oneLine = new StringBuffer();
                    oneLine.append(campaign.getId());
                    oneLine.append(CSV_SEPARATOR);

                    oneLine.append(campaign.getName());
                    oneLine.append(CSV_SEPARATOR);


                    oneLine.append(campaign.getDaypartTargetArrayList().get(b).getDay());
                    oneLine.append(CSV_SEPARATOR);
                    for(int c = 0; c <= 23; c++) {
                        if(c >= campaign.getDaypartTargetArrayList().get(b).getStartHour() &&
                                c <= campaign.getDaypartTargetArrayList().get(b).getEndHour()) {
                            oneLine.append("X");
                            if((c+1) <= 23) {
                                oneLine.append(CSV_SEPARATOR);
                            }

                        }
                        else {
                            oneLine.append(" ");
                            if((c+1) <= 23) {
                                oneLine.append(CSV_SEPARATOR);
                            }
                        }
                    }
                    bw.write(oneLine.toString());
                    bw.newLine();
                }



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
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream("GeographyReport.csv"), "UTF-8"));
            bw.write("ID, Campaign, Action, Countries, Action, Designated Market Areas");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getName());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getGeographyTargets().getCountryAction());
                oneLine.append(CSV_SEPARATOR);

                ArrayList<CountryTarget> countryTargetArrayList =
                        campaign.getGeographyTargets().getCountryTargetList();

                for (int b = 0; b < countryTargetArrayList.size(); b++) {
                    oneLine.append(countryTargetArrayList.get(b).getName());
                    if((b+1) > countryTargetArrayList.size()) {
                        oneLine.append("\",\"");
                    }
                }
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getGeographyTargets().getDmaAction());
                oneLine.append(CSV_SEPARATOR);

                ArrayList<DMATarget> dmaTargetArrayList =
                        campaign.getGeographyTargets().getDmaTargetList();

                for (int x = 0; x < dmaTargetArrayList.size(); x++) {
                    oneLine.append(dmaTargetArrayList.get(x).getName());
                    if ((x+1) > countryTargetArrayList.size()) {
                        oneLine.append("\",\"");
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

    public void writeSegmentFIle(ArrayList<Campaign> campaignArrayList) {
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream("SegmentReport.csv"), "UTF-8"));
            bw.write("ID, Campaign, Segments");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();

                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getName());
                oneLine.append(CSV_SEPARATOR);


                for(int x = 0; x < campaign.getSegmentGroupTargetList().size(); x++) {
                    oneLine.append("{");
                    ArrayList<Segment> currentSegmentArray =
                            campaign.getSegmentGroupTargetList().get(x).getSegmentArrayList();
                    for(int y = 0; y < currentSegmentArray.size(); y++) {
                        oneLine.append("[");
                        oneLine.append("("+currentSegmentArray.get(y).getAction()+")");
                        oneLine.append(currentSegmentArray.get(y).getName());
                        oneLine.append("]");
                        if((y+1) < currentSegmentArray.size()) {
                            oneLine.append(" " + currentSegmentArray.get(y).getBoolOp() + " ");
                        }
                    }
                    oneLine.append("}");
                    if ((x+1) < campaign.getSegmentGroupTargetList().size()) {
                        oneLine.append(" -" + (campaign.getSegmentGroupTargetList().get(x).getBoolOp()) + "- ");
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
