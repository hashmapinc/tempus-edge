package com.hashmapinc.tempus.clientdevice.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    final static Logger logger = Logger.getLogger(PropertyReader.class);
    private static PropertyReader propertyReader=null;
    public Properties prop = null;
    private PropertyReader()
    {
        init();
    }
    private void init()
    {
        logger.info("Initializing properties");
        Properties prop = new Properties();
        InputStream input = null;
        try {
           prop.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            this.prop=prop;
        } catch (Exception ex) {
           logger.error("Initializing failed due to "+ ex.getMessage());
        }
    }
    public String getProperty(String propertyName)
    {
        return prop.getProperty(propertyName);
    }

    public static synchronized PropertyReader getInstance()
    {
        if(propertyReader==null)
        {
            propertyReader = new PropertyReader();
        }
        return propertyReader;
    }

}
