package com.hashmapinc.tempus.clientdevice.sqlconfig;


import com.hashmapinc.tempus.clientdevice.util.PropertyReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;


public class SQLDriver {


    public static Connection connect() {
        Connection conn = null;
        try {

            String url = PropertyReader.getInstance().getProperty("sqlite.url");
            conn = DriverManager.getConnection(url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }
}