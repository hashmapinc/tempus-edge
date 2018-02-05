package com.hashmapinc.tempus.clientdevice.dao;

import com.hashmapinc.tempus.clientdevice.dao.AppConfigDao;
import com.hashmapinc.tempus.clientdevice.dao.ServerConfigDAO;
import com.hashmapinc.tempus.clientdevice.model.Configuration;
import com.hashmapinc.tempus.clientdevice.sqlconfig.SQLDriver;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

public class ServerConfigTestDao {
    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        AppConfigDao appConfigDao= new AppConfigDao(sqlDriver);
        appConfigDao.createApplicationTables();
    }

    @Test
    public void addServiceConfig() {
        SQLDriver sqlDriver = new SQLDriver("jdbc:sqlite:resources:Test.db");
        ServerConfigDAO serverConfigDao = new ServerConfigDAO(sqlDriver);
        String servicename="tempus-test";
        String config="{\"level\":\"test\"}";

        serverConfigDao.addOrUpdateConfig(servicename,config);
        Configuration configuration=serverConfigDao.getServiceConfig(servicename);
        Assert.assertEquals(configuration.getJsonString(),config);

    }

}
