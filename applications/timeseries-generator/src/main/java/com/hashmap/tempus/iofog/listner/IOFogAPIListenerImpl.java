package com.hashmap.tempus.iofog.listner;


import com.hashmap.tempus.iofog.IoFogDriver;
import com.iotracks.api.listener.IOFogAPIListener;
import com.iotracks.elements.IOMessage;

import javax.json.JsonObject;
import java.util.List;

public class IOFogAPIListenerImpl implements IOFogAPIListener {

    private final IoFogDriver mainLogInstance;

    public IOFogAPIListenerImpl(IoFogDriver mainLogInstance) {
        this.mainLogInstance = mainLogInstance;
    }

    @Override
    public void onMessages(List<IOMessage> list) {
        System.out.println("IOFogAPIListenerImpl.onMessages");
       // list.forEach(message -> mainLogInstance.buildAndSendMessage(message));
    }

    @Override
    public void onMessagesQuery(long l, long l1, List<IOMessage> list) {
        System.out.println("IOFogAPIListenerImpl.onMessagesQuery");
        /* do nothing */
    }

    @Override
    public void onError(Throwable throwable) {
        //System.out.println("IOFogAPIListenerImpl.onError");
        System.err.println("Error:" + throwable);
    }

    @Override
    public void onBadRequest(String s) {
        //System.out.println("IOFogAPIListenerImpl.onBadRequest");
        System.err.println("Bad Request: " + s);
    }

    @Override
    public void onMessageReceipt(String s, long l) {
        System.out.println("IOFogAPIListenerImpl.onMessageReceipt"+s);
        /* do nothing */
    }

    @Override
    public void onNewConfig(JsonObject jsonObject) {
        System.out.println("asasa");
        System.out.println("IOFogAPIListenerImpl.onNewConfig"+jsonObject.toString());
        mainLogInstance.setConfig(jsonObject);
    }

    @Override
    public void onNewConfigSignal() {
        System.out.println("IOFogAPIListenerImpl.onNewConfigSignal");
        mainLogInstance.updateConfig();
    }

}