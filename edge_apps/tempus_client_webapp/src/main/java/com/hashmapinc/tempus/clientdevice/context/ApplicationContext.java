package com.hashmapinc.tempus.clientdevice.context;

import com.hashmapinc.tempus.clientdevice.edgeconfig.CommunicationManager;

public class ApplicationContext {

    private static ApplicationContext context=null;
    private CommunicationManager communicationManager;
    public synchronized static ApplicationContext getContext()
    {
        if(context==null)
        {
            context=new ApplicationContext();
        }
        return context;
    }

    public void setCommunicationManager(CommunicationManager communicationManager) {
        this.communicationManager = communicationManager;
    }

    public CommunicationManager getCommunicationManager() {
        return this.communicationManager;
    }
}
