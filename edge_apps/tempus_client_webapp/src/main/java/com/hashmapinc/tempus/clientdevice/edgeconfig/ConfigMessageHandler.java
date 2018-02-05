package com.hashmapinc.tempus.clientdevice.edgeconfig;

import com.hashmapinc.tempus.clientdevice.services.ConfigurationService;

public class ConfigMessageHandler {

    ConfigurationService configurationService = new ConfigurationService();

    public ConfigMessageHandler(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConfigMessageHandler() {
    }

    public void persistConfig(String serviceName, String config) {
        configurationService.persistConfig(serviceName,config);
    }
}
