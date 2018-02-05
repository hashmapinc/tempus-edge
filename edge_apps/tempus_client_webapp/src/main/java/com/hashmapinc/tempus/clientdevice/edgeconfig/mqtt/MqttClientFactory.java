package com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt;


import com.hashmapinc.tempus.clientdevice.edgeconfig.ConfigMessageHandler;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.Map;

public class MqttClientFactory {
    final static Logger logger = Logger.getLogger(MqttClientFactory.class);
    public static MqttClient getClient(String serverIp,String port,String topicName,String password) throws Exception
    {
       logger.info("Creating new MQTT client");
        MqttClient client = null;
        try {
            client = new MqttClient(serverIp+":"+port,"iofog");
        } catch (MqttException e) {
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
        MqttConnectOptions mqttConnectOptions= new MqttConnectOptions();
        mqttConnectOptions.setUserName(topicName);
        mqttConnectOptions.setPassword(password.toCharArray());
        while(!client.isConnected()) {
            try {
                client.connect(mqttConnectOptions);
            } catch (MqttSecurityException e) {
                throw new Exception("Security Exception:"+e.getMessage());
            } catch (MqttException e) {
               logger.info("Unable to connect because of "+e.getMessage());
               logger.info("Retrying");
            }
        }


        return client;
    }
}
