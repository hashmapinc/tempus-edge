package com.hashmapinc.tempus.clientdevice.dao;

import com.hashmapinc.tempus.clientdevice.ApplicationConstants;

import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AppConfigDao {
    public void createApplicationTables( ) {

        String sql = "CREATE TABLE IF NOT EXISTS appconfig (\n"
                + "	propertyname text PRIMARY KEY NOT NULL,\n"
                + "	propertyvalue text\n"

                + ");";
        Connection conn = SQLDriver.connect();

        try {
            Statement stmt = conn.createStatement();
            // create a new table

            System.out.println(stmt.execute(sql));
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String sqlService = "CREATE TABLE IF NOT EXISTS serviceconfig (\n"
                + "	servicename text PRIMARY KEY NOT NULL,\n"
                + "	config text\n,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try {
            Connection conn1 = SQLDriver.connect();
            Statement stmt1 = conn1.createStatement();
            stmt1.execute(sqlService);
            conn1.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }

    private void addPropertytoAppConfig(String propertyName,String propertyValue)
    {
        String sql = "INSERT OR REPLACE into appconfig (propertyname,propertyvalue) values (?,?)";

        try
        {
            Connection conn = SQLDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // set the corresponding param
            pstmt.setString(1, propertyName);
            pstmt.setString(2, propertyValue);

            // update
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void addDeviceId(String deviceId) {

     addPropertytoAppConfig(ApplicationConstants.DEVICE_ID,deviceId);
    }

    public String getDeviceId() {
        String sql = "Select * from appconfig  "
                + "WHERE propertyname like \"device-id\"";
        String deviceId = null;
        try {
            Connection conn = SQLDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs    = pstmt.executeQuery();
            if(rs.next()) {
                deviceId = rs.getString("propertyvalue" );
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return deviceId;
    }

    public Map<String,String> getContext() {
        String sql = "Select * from appconfig";
        Map<String ,String> contextMap=new HashMap<String, String>();
        try {
            Connection conn = SQLDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs    = pstmt.executeQuery();
            while(rs.next()) {
                contextMap.put(rs.getString("propertyname" ),rs.getString("propertyvalue" ));
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return contextMap;
    }

    public void configureDevice(String name, String password, String serverIP, String port) {
        addPropertytoAppConfig(ApplicationConstants.DEVICE_ID,name);
        addPropertytoAppConfig(ApplicationConstants.PASSWORD,password);
        addPropertytoAppConfig(ApplicationConstants.SERVER_IP,serverIP);
        addPropertytoAppConfig(ApplicationConstants.SERVER_PORT,port);
    }


}
