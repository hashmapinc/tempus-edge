package com.hashmapinc.tempus.clientdevice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private static PropertyReader propertyReader=null;
    public Properties prop = null;
    private PropertyReader()
    {
        init();
    }
    private void init()
    {
        Properties prop = new Properties();
        InputStream input = null;
        try {
           prop.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            this.prop=prop;
        } catch (Exception ex) {
            ex.printStackTrace();
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
