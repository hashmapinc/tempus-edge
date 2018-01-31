package com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt;


import com.hashmapinc.tempus.clientdevice.edgeconfig.ConfigMessageHandler;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.util.Map;

public class MqttClientFactory {

    public static MqttClient getClient(String serverIp,String port,String topicName,String password) throws Exception
    {

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
               System.out.println("Reconnecting");
            }
        }
        client.setCallback(new TempusMqttCallback(new ConfigMessageHandler()));

        return client;
    }
}
