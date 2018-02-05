package com.hashmapinc.tempus.clientdevice.services;

import com.hashmapinc.tempus.clientdevice.dao.ServerConfigDAO;
import com.hashmapinc.tempus.clientdevice.edgeconfig.ConfigMessageHandler;
import com.hashmapinc.tempus.clientdevice.model.Configuration;
import org.apache.log4j.Logger;

import java.util.Map;

public class ConfigurationService {

    public ConfigurationService() {
    }

    public ConfigurationService(ServerConfigDAO serverConfigDAO) {
        this.serverConfigDAO = serverConfigDAO;
    }

    ServerConfigDAO serverConfigDAO = new ServerConfigDAO();
    public void persistConfig(String serviceName,String config)
    {
        serverConfigDAO.addOrUpdateConfig(serviceName,config);
    }

    public String getConfig(String serviceName) {
        Configuration configuration= serverConfigDAO.getServiceConfig(serviceName);
        if(configuration!=null)
        return configuration.getJsonString();
        else
            return "";
    }


}
