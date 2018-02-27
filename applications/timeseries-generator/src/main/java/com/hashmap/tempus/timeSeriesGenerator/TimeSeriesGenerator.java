package com.hashmap.tempus.timeSeriesGenerator;

import org.joda.time.LocalDateTime;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import com.fasterxml.jackson.core.JsonProcessingException;
import be.cetic.tsimulus.config.Configuration;
import scala.Some;
import com.hashmap.tempus.iofog.simulator.*;
import scala.Tuple3;
import com.fasterxml.jackson.databind.ObjectMapper;
import scala.collection.JavaConverters;

public class TimeSeriesGenerator {
	 private ObjectMapper mapper = new ObjectMapper();
	 private Configuration simConfig = null;
	 public TimeSeriesGenerator(String config) {
         this.loadConfiguration(config);
	}
	 private void loadConfiguration(String config)
	    {
	        simConfig = SimController.getConfiguration(config);
	    }
	public String generateData(boolean printHeader, boolean longTimestamp, String Timezone)
    {
        LocalDateTime queryTime = LocalDateTime.now();

		// Get the time Values for the current time
        scala.collection.Iterable<Tuple3<String, LocalDateTime, Object>> data = SimController.getTimeValue(simConfig.timeSeries(), queryTime);

        // Convert the Scala Iterable to a Java one
        Iterable<Tuple3<String, LocalDateTime, Object>> generatedValues = (Iterable<Tuple3<String, LocalDateTime, Object>>) JavaConverters.asJavaIterableConverter(data).asJava();

        String resultString = "";

        resultString = generateJson(longTimestamp, Timezone, generatedValues);
        

        return resultString;
    }

    private String generateJson(boolean longTimestamp, String Timezone, Iterable<Tuple3<String, LocalDateTime, Object>> generatedValues) {
        DataValue value = new DataValue();
        generatedValues.forEach(tv -> {
            String dataValue = ((Some) tv._3()).get().toString();
            String ts = tv._2().toString();
            if (longTimestamp) {
                DateTime localTime = tv._2().toDateTime(DateTimeZone.forID(Timezone));
                ts = String.valueOf(localTime.getMillis());
            }
            value.setTimeStamp(ts);
            value.addValue(tv._1(), dataValue);
        });
        String output = "";
        try {
            output = mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {

        }
        return output;
    }


}
