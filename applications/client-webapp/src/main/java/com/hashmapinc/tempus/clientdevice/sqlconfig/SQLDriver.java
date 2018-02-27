package com.hashmapinc.tempus.clientdevice.sqlconfig;


import com.hashmapinc.tempus.clientdevice.util.PropertyReader;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SQLDriver {


    final static Logger logger = Logger.getLogger(SQLDriver.class);
    String url;
    public SQLDriver()
    {
         url = PropertyReader.getInstance().getProperty("sqlite.url");
    }
    public SQLDriver(String url)
    {
        this.url=url;
    }
    public  Connection connect() {
        Connection conn = null;
        try {


            conn = DriverManager.getConnection(url);
            logger.info("Created new SQL connection");

        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Error creating connection"+e.getMessage());
        }

        return conn;
    }

}