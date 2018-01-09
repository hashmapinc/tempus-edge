package com.hashmap.tempus.timeSeriesGenerator;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataValue {
    @JsonProperty("ts")
    private String timeStamp;

    private Map<String, String> values;

    public String getTimeStamp(){
        return timeStamp;
    }

    public Map getValues(){
        return values;
    }

    public DataValue(){
        values = new HashMap<String, String>();
    }

    public void setTimeStamp(String timeStamp){
        this.timeStamp = timeStamp;
    }

    public void addValue(String key, String value){
        values.put(key, value);
    }
}

