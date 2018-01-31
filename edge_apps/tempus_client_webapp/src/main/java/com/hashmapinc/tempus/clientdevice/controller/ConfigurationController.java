package com.hashmapinc.tempus.clientdevice.controller;

import com.hashmapinc.tempus.clientdevice.services.ConfigurationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigurationController {
    ConfigurationService configurationService = new ConfigurationService();
    @GetMapping("/config")
    public String getConfig(@RequestParam("service-name") String serviceName) {

        return configurationService.getConfig(serviceName);
    }
}
