package com.mediacrossing.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigurationProperties {

    private String putneyUrl;
    private String mxUrl;
    private String mxUsername;
    private String mxPassword;
    private String outputPath;


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
            outputPath = prop.getProperty("outputPath");
            mxUrl = prop.getProperty("mxUrl");
            mxUsername = prop.getProperty("mxUsername");
            mxPassword = prop.getProperty("mxPassword");

        } else {
            LOG.error("Properties File Failed To Load");
        }
    }

    public String getPutneyUrl() {
        return putneyUrl;
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

    public String getOutputPath() {
        return outputPath;
    }

}
