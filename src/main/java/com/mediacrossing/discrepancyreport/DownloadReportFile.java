package com.mediacrossing.discrepancyreport;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.util.Charsets;
import com.google.api.services.dfareporting.Dfareporting;
import com.google.api.services.dfareporting.model.File;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DownloadReportFile {

    public static List<String[]> run(Dfareporting reporting, File reportFile)
            throws Exception {
        System.out.println("=================================================================");
        System.out.printf("Retrieving and printing a report file for report with ID %s%n",
                reportFile.getReportId());
        System.out.printf("The ID number of this report file is %s%n", reportFile.getId());
        System.out.println("=================================================================");

        HttpResponse fileContents = reporting.files()
                .get(reportFile.getReportId(), reportFile.getId())
                .executeMedia();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(fileContents.getContent(), Charsets.UTF_8));
            List<String[]> list = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                String[] a = line.split(",");
                list.add(a);
            }
            return list;
        } finally {
            fileContents.disconnect();
        }
    }
}
