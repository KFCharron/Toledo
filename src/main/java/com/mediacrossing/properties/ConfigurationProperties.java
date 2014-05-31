package com.mediacrossing.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ConfigurationProperties {

    private String putneyUrl;
    private String appNexusUsername;
    private String appNexusPassword;
    private String mxUrl;
    private String mxUsername;
    private String mxPassword;
    private int partitionSize;
    private Duration requestDelayInSeconds;
    private String outputPath;
    private String appNexusUrl;


    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationProperties.class);

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
            putneyUrl = prop.getProperty("putneyUrl");
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
            appNexusUrl = prop.getProperty("appNexusUrl");

        } else {
            LOG.error("Properties File Failed To Load");
        }
    }

    public String getPutneyUrl() {
        return putneyUrl;
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

    public String getAppNexusUrl() {
        return appNexusUrl;
    }
}
