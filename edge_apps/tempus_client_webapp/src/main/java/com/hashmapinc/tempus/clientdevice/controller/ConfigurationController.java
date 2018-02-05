package com.hashmapinc.tempus.clientdevice.controller;

import com.hashmapinc.tempus.clientdevice.services.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationController {
    ConfigurationService configurationService = new ConfigurationService();
    final static Logger logger = Logger.getLogger(ConfigurationController.class);
    @GetMapping("/config")
    public String getConfig(@RequestParam("service-name") String serviceName) {
        logger.info("Getting config for:"+serviceName);
        return configurationService.getConfig(serviceName);
    }
}
