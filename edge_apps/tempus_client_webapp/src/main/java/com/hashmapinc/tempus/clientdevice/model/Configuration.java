package com.hashmapinc.tempus.clientdevice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.google.gson.JsonObject;

import java.sql.Timestamp;

public class Configuration {
    public JsonObject getConfig() {
        return config;
    }

    public void setConfig(JsonObject config) {
        this.config = config;
    }

    private JsonObject config;
    private Timestamp timestamp;


    public Configuration(JsonObject config, Timestamp timestamp) {
        this.config = config;
        this.timestamp = timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getJsonString() {
        JsonObject jsonObject=new JsonObject();
        jsonObject.add("config",config);
        jsonObject.addProperty("timestamp",timestamp.getTime());
        return jsonObject.toString();
    }
}
