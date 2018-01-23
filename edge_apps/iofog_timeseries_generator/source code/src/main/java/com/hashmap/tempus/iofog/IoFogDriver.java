package com.hashmap.tempus.iofog;

import com.google.gson.Gson;
import com.hashmap.tempus.iofog.listner.IOFogAPIListenerImpl;
import com.hashmap.tempus.timeSeriesGenerator.TimeSeriesGenerator;
import com.iotracks.api.IOFogClient;
import com.iotracks.elements.IOMessage;
import io.netty.util.internal.StringUtil;


import javax.json.*;


public class IoFogDriver {

    private static Object fetchConfigLock = new Object();
    private static JsonObject config = null;
    private static String containerId = "";

    private static IOFogClient ioFogClient;
    private static IOFogAPIListenerImpl listener;

    public static void main(String[] args) throws Exception {

        IoFogDriver instance = new IoFogDriver();

        if (args.length > 0 && args[0].startsWith("--id=")) {
            containerId = args[0].substring(args[0].indexOf('=') + 1);
        } else {
            containerId = System.getenv("SELFNAME");
        }

        if (StringUtil.isNullOrEmpty(containerId)) {
            System.err.println("Container Id is not specified. Use --id=XXXX parameter or set the id as SELFNAME=XXXX environment property");
            containerId = "8ytMDXGFHfT7QZD997rPYC";
        }
      /*
        String ioFabricHost = System.getProperty("iofog_host", "iofog");
        int ioFabricPort;
        try {
            ioFabricPort = Integer.parseInt(System.getProperty("iofog_port", "54321"));
        } catch (Exception e) {

        }
        ioFabricPort = 54321;

        ioFogClient = new IOFogClient(ioFabricHost, ioFabricPort, containerId);
        listener = new IOFogAPIListenerImpl(instance);
        updateConfig();
        try {
            ioFogClient.openControlWebSocket(listener);
        } catch (Exception e) {
            System.err.println("Unable to open Control WebSocket to ioFog: " + e.getMessage());
        }

        try {
            ioFogClient.openMessageWebSocket(listener);
        } catch (Exception e) {
            System.err.println("Unable to open Message WebSocket to ioFog: " + e.getMessage());
        }
*/
        String tsConfig = "{\"generators\":[  { \"name\": \"monthly-basis\",\"type\": \"monthly\",\"points\": {\"january\": 3.3, \"february\": 3.7, \"march\": 6.8, \"april\": 9.8, \"may\": 13.6, \"june\": 16.2,\"july\": 18.4, \"august\": 18, \"september\": 14.9, \"october\": 11.1, \"november\": 6.8, \"december\": 3.9}},{\"name\": \"daily-basis\",\"type\": \"monthly\",\"points\": {\"january\": 3.3, \"february\": 3.7, \"march\": 6.8, \"april\": 9.8, \"may\": 13.6, \"june\": 16.2,\"july\": 18.4, \"august\": 18, \"september\": 14.9, \"october\": 11.1, \"november\": 6.8, \"december\": 3.9}},{\"name\": \"generator1\",\"type\": \"gaussian\",\"seed\": 42,\"std\": 0.5},{\"name\": \"generator2\",\"type\": \"gaussian\",\"seed\": 11,\"std\": 0.9}],\"exported\":[{\"name\": \"temperature\", \"generator\": \"generator1\", \"frequency\": 6000},{\"name\": \"pressure\", \"generator\": \"monthly-basis\", \"frequency\": 3000},{\"name\": \"torque\", \"generator\": \"generator2\", \"frequency\": 6000},{\"name\": \"rpm\", \"generator\": \"daily-basis\", \"frequency\": 3000},{\"name\": \"density\",\"generator\": \"generator1\", \"frequency\": 6000},{\"name\": \"porosity\", \"generator\": \"daily-basis\", \"frequency\": 3000},{\"name\": \"resistivity\", \"generator\": \"generator2\", \"frequency\": 6000},{\"name\": \"crpm\", \"generator\": \"monthly-basis\", \"frequency\": 3000},{\"name\": \"aprs\", \"generator\": \"daily-basis\", \"frequency\": 6000},{\"name\": \"stor\", \"generator\": \"monthly-basis\", \"frequency\": 3000},{\"name\": \"rpm\", \"generator\": \"generator1\", \"frequency\": 6000},{\"name\": \"gamma\", \"generator\": \"generator2\", \"frequency\": 1000},{\"name\": \"Attn\", \"generator\": \"generator1\", \"frequency\": 2000}],\"from\": \"2016-01-01 00:00:00.000\",\"to\": \"2017-12-31 23:59:59.999\"}";
        if (config != null) {
          if(config.containsKey("tsconfig"))
            {
              if (!config.get("tsConfig").toString().isEmpty())
                tsConfig = config.get("tsConfig").toString();
            }
        }
             TimeSeriesGenerator timeSeriesGenerator = new TimeSeriesGenerator(tsConfig);
        Gson gson = new Gson();
        while(true)
        {
            String json=timeSeriesGenerator.generateData(false,true,"UTC");
            Thread.sleep(20000);
            System.out.println(timeSeriesGenerator.generateData(false,true,"UTC"));
            IoFogDriver.buildAndSendMessage(json);
        }

    }

    public void setConfig(JsonObject configObject) {
        config = configObject;
        synchronized (fetchConfigLock) {
            fetchConfigLock.notifyAll();
        }
    }

    public static void updateConfig(){
        config = null;
        try {
            while (config == null) {
                ioFogClient.fetchContainerConfig(listener);
                synchronized (fetchConfigLock) {
                    fetchConfigLock.wait(1000);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching config: " + e.getMessage());
        }
    }

    public static void buildAndSendMessage(String message) {
        IOMessage tMessage = buildMessage(message);
        if(tMessage != null) {
            ioFogClient.sendMessageToWebSocket(tMessage);
        } else {
            System.out.println("Message did't pass transformation. Nothing to send.");
        }
    }

    private static IOMessage buildMessage(String message) {

        IOMessage newIoMessage = new IOMessage();
        newIoMessage.setInfoFormat("application/json");
        newIoMessage.setInfoType("timeSeries/tempus-timeSeries");
        newIoMessage.setPublisher(containerId);
        newIoMessage.setContextData(containerId.getBytes());
        newIoMessage.setContentData(message.getBytes());
        return newIoMessage;

    }
}