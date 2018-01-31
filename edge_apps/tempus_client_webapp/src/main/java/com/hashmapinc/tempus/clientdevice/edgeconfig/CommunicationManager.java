package com.hashmapinc.tempus.clientdevice.edgeconfig;

public interface CommunicationManager {
     public void start();
     public Status getStatus();
     public void restart();
     public  void stop();

}
