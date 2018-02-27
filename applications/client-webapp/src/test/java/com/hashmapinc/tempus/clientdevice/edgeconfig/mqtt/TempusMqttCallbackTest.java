package com.hashmapinc.tempus.clientdevice.edgeconfig.mqtt;

import com.hashmapinc.tempus.clientdevice.dao.AppConfigDao;
import com.hashmapinc.tempus.clientdevice.dao.ServerConfigDAO;
import com.hashmapinc.tempus.clientdevice.edgeconfig.ConfigMessageHandler;
import com.hashmapinc.tempus.clientdevice.model.Configuration;
import com.hashmapinc.tempus.clientdevice.services.ConfigurationService;
import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;


public class TempusMqttCallbackTest {
    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao = new AppConfigDao(sqlDriver);
        appConfigDao.createApplicationTables();
    }

    @Test
    public void  messageArrivedForSingleService() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        ServerConfigDAO serverConfigDAO = new ServerConfigDAO(sqlDriver);
        ConfigurationService configurationService = new ConfigurationService(serverConfigDAO);
        TempusMqttCallback tempusMqttCallback = new TempusMqttCallback(new ConfigMessageHandler(configurationService), new MqttManager());
        MqttMessage msg = new MqttMessage();
        String config = "{\\\"tempus-test10\\\":{\\\"test\\\":\\\"test\\\"}}";
        String payload = "{\"config\":\"" + config + "\"}";
        msg.setPayload(payload.getBytes());
        try
        {
            tempusMqttCallback.messageArrived("testTopic", msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Configuration configSet=serverConfigDAO.getServiceConfig("tempus-test10");
        Assert.assertEquals("{\"test\":\"test\"}",configSet.getConfig().toString());
    }

    @Test
    public void  messageArrivedForMultipleServices() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        ServerConfigDAO serverConfigDAO = new ServerConfigDAO(sqlDriver);
        ConfigurationService configurationService = new ConfigurationService(serverConfigDAO);
        TempusMqttCallback tempusMqttCallback = new TempusMqttCallback(new ConfigMessageHandler(configurationService), new MqttManager());
        MqttMessage msg = new MqttMessage();
        String config = "\\\"tempus-test3\\\":{\\\"test\\\":\\\"test1\\\"}";
        String config1 = "\\\"tempus-test4\\\":{\\\"test\\\":\\\"test2\\\"}";
        String config2 = "\\\"tempus-test5\\\":{\\\"test\\\":\\\"test3\\\"}";
        String payload = "{\"config\":\"{" + config+","+config1+","+config2 + "}\"}";
        msg.setPayload(payload.getBytes());
        try
        {
            tempusMqttCallback.messageArrived("testTopic", msg);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Configuration configSet=serverConfigDAO.getServiceConfig("tempus-test3");
        Assert.assertEquals("{\"test\":\"test1\"}",configSet.getConfig().toString());
        Configuration configSet1=serverConfigDAO.getServiceConfig("tempus-test4");
        Assert.assertEquals("{\"test\":\"test2\"}",configSet1.getConfig().toString());
        Configuration configSet2=serverConfigDAO.getServiceConfig("tempus-test5");
        Assert.assertEquals("{\"test\":\"test3\"}",configSet2.getConfig().toString());
    }
}
