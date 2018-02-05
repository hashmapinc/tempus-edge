package com.hashmapinc.tempus.clientdevice.dao;

import com.hashmapinc.tempus.clientdevice.ApplicationConstants;

import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AppConfigDao {
    final static Logger logger = Logger.getLogger(AppConfigDao.class);
    SQLDriver sqlDriver;
    public AppConfigDao() {
        sqlDriver =new SQLDriver();
    }

    public AppConfigDao(SQLDriver sqlDriver) {
        this.sqlDriver = sqlDriver;
    }


    public void createApplicationTables( ) {

        logger.info("Intial table creation");
        logger.debug("Creating appconfig Table");
        String sql = "CREATE TABLE IF NOT EXISTS appconfig (\n"
                + "	propertyname text PRIMARY KEY NOT NULL,\n"
                + "	propertyvalue text\n"

                + ");";
        Connection conn = sqlDriver.connect();

        try {
            Statement stmt = conn.createStatement();
            // create a new table
            boolean isCreated=stmt.execute(sql);
            logger.debug("Create statement for apconfig return :"+isCreated);
            conn.close();
        } catch (SQLException e) {
            logger.error("Error Creating appconfig table:"+e.getMessage());
        }

        logger.debug("Creating serviceconfig Table");
        String sqlService = "CREATE TABLE IF NOT EXISTS serviceconfig (\n"
                + "	servicename text PRIMARY KEY NOT NULL,\n"
                + "	config text\n,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try {
            Connection conn1 = sqlDriver.connect();
            Statement stmt1 = conn1.createStatement();
            boolean isCreated=stmt1.execute(sqlService);
            logger.debug("Create statement for serviceconfig return :"+isCreated);
            conn1.close();
        } catch (SQLException e) {
            logger.error("Error Creating serviceconfig table:"+e.getMessage());
        }


    }

    private void addPropertytoAppConfig(String propertyName,String propertyValue)
    {
        logger.debug("Inserting App Config "+propertyName+" with value " + propertyValue);
        String sql = "INSERT OR REPLACE into appconfig (propertyname,propertyvalue) values (?,?)";

        try
        {
            Connection conn = sqlDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // set the corresponding param
            pstmt.setString(1, propertyName);
            pstmt.setString(2, propertyValue);

            // update
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            logger.error("Error inserting app config :"+e.getMessage());
        }
    }

    public String getDeviceId() {
        String sql = "Select * from appconfig  "
                + "WHERE propertyname like \"device-id\"";
        String deviceId = null;
        try {
            Connection conn = sqlDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs    = pstmt.executeQuery();
            if(rs.next()) {
                deviceId = rs.getString("propertyvalue" );
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {

            logger.error("Error while getting divice id "+e.getMessage());
        }
        return deviceId;
    }

    public Map<String,String> getContext() {
        logger.info("Getting context of Applocation");
        String sql = "Select * from appconfig";
        Map<String ,String> contextMap=new HashMap<String, String>();
        try {
            Connection conn = sqlDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs    = pstmt.executeQuery();
            while(rs.next()) {
                contextMap.put(rs.getString("propertyname" ),rs.getString("propertyvalue" ));
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            logger.error("Error getting app context "+e.getMessage());
        }
        return contextMap;
    }

    public void configureDevice(String name, String password, String serverIP, String port) {
        logger.info("Configuring device");
        addPropertytoAppConfig(ApplicationConstants.DEVICE_ID,name);
        addPropertytoAppConfig(ApplicationConstants.PASSWORD,password);
        addPropertytoAppConfig(ApplicationConstants.SERVER_IP,serverIP);
        addPropertytoAppConfig(ApplicationConstants.SERVER_PORT,port);
    }


}
