package com.hashmapinc.tempus.clientdevice.dao;


import com.hashmapinc.tempus.clientdevice.ApplicationConstants;
import com.hashmapinc.tempus.clientdevice.dao.AppConfigDao;
import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Assert;

import java.util.Map;


public class AppConfigDaoTest {

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao= new AppConfigDao(sqlDriver);
        appConfigDao.createApplicationTables();
    }

    @Test
    public void addDevice() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao = new AppConfigDao(sqlDriver);


        String name = "testName";
        String ip = "127.0.0.1";
        String password = "password";
        String port = "3434";

        appConfigDao.configureDevice(name, password, ip, port);
        Map context = appConfigDao.getContext();

        Assert.assertEquals(context.get(ApplicationConstants.SERVER_PORT),port);
        Assert.assertEquals(context.get(ApplicationConstants.SERVER_IP),ip);
        Assert.assertEquals(context.get(ApplicationConstants.DEVICE_ID),name);
        Assert.assertEquals(context.get(ApplicationConstants.PASSWORD),password);


    }

    @Test
    public void updateDevice() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao = new AppConfigDao(sqlDriver);


        String name = "testName";
        String ip = "127.0.0.1";
        String password = "password";
        String port = "3434";

        appConfigDao.configureDevice(name, password, ip, port);

        String nameupdated = "nameChanged";


        appConfigDao.configureDevice(nameupdated, password, ip, port);
        Map context = appConfigDao.getContext();

        Assert.assertEquals(context.get(ApplicationConstants.DEVICE_ID),nameupdated);

    }

    @Test
    public void getDevice() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao = new AppConfigDao(sqlDriver);


        String name = "test123";
        String ip = "127.0.0.1";
        String password = "password";
        String port = "3434";

        appConfigDao.configureDevice(name, password, ip, port);
        String deviceId= appConfigDao.getDeviceId();

        Assert.assertEquals(deviceId,name);

    }
}
