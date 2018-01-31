package com.hashmapinc.tempus.clientdevice.edgeconfig;

import com.hashmapinc.tempus.clientdevice.services.ConfigurationService;

public class ConfigMessageHandler {

    ConfigurationService configurationService = new ConfigurationService();
    public void persistConfig(String serviceName, String config) {
        configurationService.persistConfig(serviceName,config);
    }
}
