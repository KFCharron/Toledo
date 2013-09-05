package com.mediacrossing.segmenttargeting;

import java.io.*;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSVWriter {

    private static final String CSV_SEPARATOR = ",";
    private static final Logger LOG = LoggerFactory.getLogger(HTTPRequest.class);

    public void writeFrequencyFile(ArrayList<Campaign> campaignArrayList, String outputPath) {
        //Frequency csv
        try
        {
            BufferedWriter bw =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath, "FrequencyReport.csv")), "UTF-8"));
            bw.write("Advertiser, Line Item, Campaign ID, Campaign Name, MaxImps/Person," +
                    " MaxImps/Person/Day, MinMinutesBetweenImps");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(campaign.getAdvertiserID());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getLineItemID());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getName().replace(",","\",\""));
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxLifetimeImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMaxDayImps());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getFrequencyTargets().getMinMinutesPerImp());

                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
            LOG.info("FrequencyReport.csv written to " + outputPath);
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}

    }

    public void writeDaypartFile(ArrayList<Campaign> campaignArrayList, String outputPath) {
        //Daypart CSV
        try
        {
            BufferedWriter bw =
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputPath, "DaypartReport.csv")), "UTF-8"));
            bw.write("Advertiser, Line Item, Campaign ID, Name, Days, 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {


                for (int b = 0; b < campaign.getDaypartTargetArrayList().size(); b++) {

                    StringBuffer oneLine = new StringBuffer();
                    oneLine.append(campaign.getAdvertiserID());
                    oneLine.append(CSV_SEPARATOR);
                    oneLine.append(campaign.getLineItemID());
                    oneLine.append(CSV_SEPARATOR);
                    oneLine.append(campaign.getId());
                    oneLine.append(CSV_SEPARATOR);

                    oneLine.append(campaign.getName().replace(",","\",\""));
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
            LOG.info("DaypartReport.csv written to " + outputPath);
        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}
    }

    public void writeGeographyFile(ArrayList<Campaign> campaignArrayList, String outputPath) {
        //Geography CSV
        try
        {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream(new File(outputPath, "GeographyReport.csv")), "UTF-8"));
            bw.write("Advertiser, Line Item, Campaign ID, Campaign, Action, Countries, Zips, DMA Action, Designated Market Areas");
            bw.newLine();
            for (Campaign campaign : campaignArrayList)
            {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(campaign.getAdvertiserID());
                oneLine.append(CSV_SEPARATOR);
                oneLine.append(campaign.getLineItemID());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getId());
                oneLine.append(CSV_SEPARATOR);

                oneLine.append(campaign.getName().replace(",","\",\""));
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

                ArrayList<ZipTarget> zipTargetArrayList =
                        campaign.getGeographyTargets().getZipTargetList();

                for (int c = 0; c < zipTargetArrayList.size(); c++) {
                    oneLine.append(zipTargetArrayList.get(c).getFromZip());
                    oneLine.append(" - ");
                    oneLine.append(zipTargetArrayList.get(c).getToZip());
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
            LOG.info("GeographyReport.csv written to " + outputPath);

        }
        catch (UnsupportedEncodingException e) {}
        catch (FileNotFoundException e){}
        catch (IOException e){}

    }

}
