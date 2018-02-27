package com.hashmapinc.tempus.iofog;

import com.google.gson.Gson;


import com.hashmapinc.tempus.iofog.filter.expression.Computable;
import com.hashmapinc.tempus.iofog.listner.IOFogAPIListenerImpl;
import com.hashmapinc.tempus.iofog.queryParser.QueryParser;
import com.hashmapinc.tempus.edge.PollIOFogClient;
import com.iotracks.elements.IOMessage;
import io.netty.util.internal.StringUtil;


import javax.json.*;


public class IoFogDriver {

    private static Object fetchConfigLock = new Object();
    private static JsonObject config = null;
    private static String containerId = "";
    private static Computable filters=null;
    private static PollIOFogClient ioFogClient;
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
            containerId="M8LtCMJKGfB7y9MByy8VPd";
        }

        String ioFabricHost = System.getProperty("iofog_host", "iofog");
        int ioFabricPort;
        try {
                ioFabricPort = Integer.parseInt(System.getProperty("iofog_port", "54321"));
        } catch (Exception e) {

        }
             ioFabricPort = 54321;

            ioFogClient = new PollIOFogClient(ioFabricHost, ioFabricPort, containerId,"tempusFilter","http://"+ioFabricHost+":8080/config?service-name=tempus-Filter");
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


    }

    public void setConfig(JsonObject configObject) {
        config = configObject;
        Gson gson = new Gson();
        if(config !=null) {
            if (config.containsKey("filter")) {
                if (!config.get("filter").toString().isEmpty()) {
                    filters = QueryParser.getFilter(gson.fromJson(config.get("filter").toString(), com.google.gson.JsonObject.class));
                    System.out.println(gson.toJson(filters));
                }
            }
        }
        synchronized (fetchConfigLock) {
            fetchConfigLock.notifyAll();
        }
    }

    public static void updateConfig(){
        config = null;
        try {
            ioFogClient.fetchLocalContainerConfig(listener,1000L);
            while (config == null) {
                synchronized (fetchConfigLock) {
                    fetchConfigLock.wait(1000);
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching config: " + e.getMessage());
        }
    }

    public static void buildAndSendMessage(IOMessage message) {
        IOMessage tMessage = buildMessage(message);
        if(tMessage != null) {
            ioFogClient.sendMessageToWebSocket(tMessage);
        } else {
            System.out.println("Message did't pass Filter requirement. Nothing to send.");
        }
    }

    private static IOMessage buildMessage(IOMessage message) {

        String contentData=new String(message.getContentData());
        System.out.println(contentData);
        Gson gson = new Gson();
        if(filters!=null)
        {
            if(!filters.compute(gson.fromJson(contentData, com.google.gson.JsonObject.class)))
            return null;
        }
        else
            return null;
        IOMessage newIoMessage = new IOMessage();
        if(config.containsKey("infotype")) {
            if (!config.getString("infotype").isEmpty())
                newIoMessage.setInfoFormat(config.getString("infotype"));
            else
                newIoMessage.setInfoFormat("application/json");
        }
        else
            newIoMessage.setInfoFormat("application/json");


        if(config.containsKey("infoformat")) {
            if (!config.getString("infoformat").isEmpty())
                newIoMessage.setInfoType(config.getString("infoformat"));
            else
                newIoMessage.setInfoFormat("application/json");

        }else
             newIoMessage.setInfoType("filter/tempus-filter");
        newIoMessage.setPublisher(containerId);

        if(config.containsKey("mqtt_config")) {
            if (!config.get("mqtt_config").toString().isEmpty())
                newIoMessage.setContextData(config.get("mqtt_config").toString().getBytes());
            else
                newIoMessage.setContextData(containerId.getBytes());
        } else
            newIoMessage.setContextData(containerId.getBytes());

        newIoMessage.setContentData(message.getContentData());
       return newIoMessage;

    }
}