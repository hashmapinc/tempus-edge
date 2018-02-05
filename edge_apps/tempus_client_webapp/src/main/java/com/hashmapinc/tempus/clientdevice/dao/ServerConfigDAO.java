package com.hashmapinc.tempus.clientdevice.dao;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hashmapinc.tempus.clientdevice.model.Configuration;
import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;
import jdk.nashorn.internal.ir.WhileNode;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class ServerConfigDAO {
    final static Logger logger = Logger.getLogger(ServerConfigDAO.class);
    SQLDriver sqlDriver =new SQLDriver();
    public ServerConfigDAO() {
        sqlDriver =new SQLDriver();
    }

    public ServerConfigDAO(SQLDriver sqlDriver) {
        this.sqlDriver = sqlDriver;
    }
    public void addOrUpdateConfig(String serviceName,String config) {

        logger.info("Adding service config");
        logger.debug("Adding Config for service "+serviceName+" value "+config);
        String sql = "INSERT OR REPLACE into serviceconfig (servicename,config,timestamp) values (?,?,?)";

        try
        {
            Connection conn = sqlDriver.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            // set the corresponding param
            pstmt.setString(1, serviceName);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(2, config);

            // update
            pstmt.executeUpdate();
            conn.close();
        } catch (SQLException e) {
            logger.error("Error adding service config "+e.getMessage());
        }
    }

    public Configuration getServiceConfig(String serviceName) {
        logger.info("Getting Service Config");
        logger.debug("Getting service config for service:"+serviceName);
        String sql = "Select * from serviceconfig  "
                + "WHERE servicename = ?";
        Configuration config = null;
        try {
            Connection conn = sqlDriver.connect();

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, serviceName);
            ResultSet rs    = pstmt.executeQuery();
            Gson gson = new Gson();
            if(rs.next()) {
                JsonParser parser = new JsonParser();
                JsonObject configJson = parser.parse(rs.getString("config")).getAsJsonObject();
                config = new Configuration(configJson,rs.getTimestamp("timestamp")) ;
            }
            rs.close();
            conn.close();
        } catch (SQLException e) {
            logger.error("Error getting service config "+e.getMessage());
        }
        return config;
    }


}
