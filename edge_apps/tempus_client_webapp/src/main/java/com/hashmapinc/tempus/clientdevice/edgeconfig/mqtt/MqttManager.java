package com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt;

import com.hashmapinc.tempus.clientdevice.ApplicationConstants;
import com.hashmapinc.tempus.clientdevice.edgeconfig.CommunicationManager;
import com.hashmapinc.tempus.clientdevice.edgeconfig.ConfigMessageHandler;
import com.hashmapinc.tempus.clientdevice.edgeconfig.Status;
import com.hashmapinc.tempus.clientdevice.services.ApplicationService;
import com.hashmapinc.tempus.clientdevice.util.PropertyReader;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.*;
import java.util.Map;

public class MqttManager implements CommunicationManager{

    ConfigMessageHandler configMessageHandler = new ConfigMessageHandler() ;
    Status status=Status.CONFIGURING;
    final static Logger logger = Logger.getLogger(MqttManager.class);
    public Status  getStatus() {
        return status;
    }

    MqttClient client=null;
     public void start() {
        if(status==Status.CONNECTED)
            restart();
        try {

            ApplicationService applicationService = new ApplicationService();
            Map<String,String> context=applicationService.getContext();
            String topicName=context.get(ApplicationConstants.DEVICE_ID);
            String serverIp=context.get(ApplicationConstants.SERVER_IP);
            String port=context.get(ApplicationConstants.SERVER_PORT);
            String password=context.get(ApplicationConstants.PASSWORD);
            client.setCallback(new TempusMqttCallback(new ConfigMessageHandler(),this));
            logger.info("Mqtt Client Started");
            client= MqttClientFactory.getClient("tcp://"+serverIp,port,topicName,password);
            client.subscribe(PropertyReader.getInstance().getProperty("tempus.mqtt.subscribetopic"));
            client.publish(PropertyReader.getInstance().getProperty("tempus.mqtt.publishtopic"),new MqttMessage(("{\"sharedKeys\":\""+ApplicationConstants.CONFIG_PARAM+"\"}").getBytes()));

        } catch (Exception e) {
           status=Status.INVALID_SECURITY_INFO;
           return;
        }
        status=Status.CONNECTED;
    }


    public void restart()
    {
        logger.info("Restarting MQTT Client");
       if(status==Status.CONNECTED)
       {
           try {
               client.disconnect();

           } catch (MqttException e) {
              logger.error("Error while disconnecting mqtt client " +e.getMessage());
           }
           status=Status.CONFIGURING;
       }
       start();
    }

    @Override
    public void stop()  {
        logger.info("Stopping MQTT Client");
        try {
            client.close(true);
        } catch (MqttException e) {
            logger.error("Error while clossing mqtt client " +e.getMessage());
        }
       status=Status.DISCONNECTED;
    }
}
