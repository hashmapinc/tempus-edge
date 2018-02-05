package com.hashmapinc.tempus.clientdevice;

import com.hashmapinc.tempus.clientdevice.context.ApplicationContext;
import com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt.MqttManager;
import com.hashmapinc.tempus.clientdevice.services.ApplicationService;
import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApp {
    final static Logger logger = Logger.getLogger(MainApp.class);
    private static void init()
    {
        ApplicationService applicationService = new ApplicationService();
        applicationService.initConfig();
        String deviceId=applicationService.getDeviceId();
        MqttManager mqttManager = new MqttManager();
        ApplicationContext.getContext().setCommunicationManager(mqttManager);
        if(deviceId!=null)
        {
            try {
                mqttManager.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public static void main(String[] args) {


        SpringApplication.run(MainApp.class, args);
        logger.info("Initializing APP");
        init();
    }
}
