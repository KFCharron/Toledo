package com.mediacrossing.properties;

import scala.concurrent.duration.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ConfigurationProperties {

    private final String appNexusUrl;
    private final String appNexusUsername;
    private final String appNexusPassword;
    private final String mxUrl;
    private final String mxUsername;
    private final String mxPassword;
    private final int partitionSize;
    private final Duration requestDelayInSeconds;
    private final String outputPath;
    private final String putneyHosts;
    private final Duration putneyReportRequestTimeout;
    private final Duration khajuRequestTimeout;

    public ConfigurationProperties(String[] args) throws IOException {

        Properties prop = new Properties();
        File configFile = new File(args[0].substring("--properties-file=".length()));
        InputStream is = new FileInputStream(configFile);
        try {
            prop.load(is);
        } finally {
            is.close();
        }

        //set the properties
        if (!prop.isEmpty()) {
            appNexusUrl = prop.getProperty("appNexusUrl");
            appNexusUsername = prop.getProperty("appNexusUsername");
            appNexusPassword = prop.getProperty("appNexusPassword");
            partitionSize = Integer.parseInt(prop.getProperty("partitionSize"));
            requestDelayInSeconds = Duration
                    .apply((Integer.parseInt(prop.getProperty("requestDelayInSeconds"))),
                            TimeUnit.SECONDS);
            outputPath = prop.getProperty("outputPath");
            mxUrl = prop.getProperty("mxUrl");
            mxUsername = prop.getProperty("mxUsername");
            mxPassword = prop.getProperty("mxPassword");
            putneyHosts = prop.getProperty("putneyHosts");
            putneyReportRequestTimeout =
                    Duration.apply(
                            Integer.parseInt(prop.getProperty("putneyReportRequestTimeoutSeconds")),
                            TimeUnit.SECONDS);
            khajuRequestTimeout =
                    Duration.apply(
                            Integer.parseInt(prop.getProperty("khajuRequestTimeoutSeconds")),
                            TimeUnit.SECONDS);
        } else {
            throw new RuntimeException("Properties File Failed To Load");
        }
    }

    public String putneyHosts() {
        return putneyHosts;
    }

    public Duration putneyReportRequestTimeout() {
        return putneyReportRequestTimeout;
    }

    public Duration khajuRequestTimeout() {
        return khajuRequestTimeout;
    }

    public String getAppNexusUrl() {
        return appNexusUrl;
    }

    public String getAppNexusUsername() {
        return appNexusUsername;
    }

    public String getAppNexusPassword() {
        return appNexusPassword;
    }

    public String getMxUrl() {
        return mxUrl;
    }

    public String getMxUsername() {
        return mxUsername;
    }

    public String getMxPassword() {
        return mxPassword;
    }

    public int getPartitionSize() {
        return partitionSize;
    }

    public Duration getRequestDelayInSeconds() {
        return requestDelayInSeconds;
    }

    public String getOutputPath() {
        return outputPath;
    }
}
