package com.dtv.dcp.epoch.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.marker.MapEntriesAppendingMarker;

public class LogArgumentsJsonProvider extends ArgumentsJsonProvider {
  static final String FIELD_NAME_MAP = "map";
  
  static final String MESSAGE = "msg";
  
  static final String SPI = "spi";
  
  static final String ESAPI = "esapi";
  
  static final String SPI_ESAPI = "spiesapi";
  
  private static Field mapField = null;

  
  public LogArgumentsJsonProvider() {
    try {
      mapField = MapEntriesAppendingMarker.class.getDeclaredField("map");
      mapField.setAccessible(true);
    } catch (NoSuchFieldException|SecurityException ex) {
      ex.printStackTrace();
    } 
  }

  
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
    if (!isIncludeStructuredArguments() && !isIncludeNonStructuredArguments())
      return; 
    boolean hasWrittenLogMsg = false;

    Object[] args = event.getArgumentArray();
    if (getFieldName() != null && (
      isIncludeStructuredArguments() || isIncludeNonStructuredArguments())) {
      generator.writeObjectFieldStart(getFieldName());

        generator.writeFieldName("msg");
        if (args == null || args.length == 0) {
          generator.writeObject(event.getMessage());
          hasWrittenLogMsg = true;
        } 
  
    } 
    if (args == null || args.length == 0) {
      if (hasWrittenLogMsg)
        generator.writeEndObject(); 
      return;
    } 

    for (int argIndex = 0; argIndex < args.length; argIndex++) {
      Object arg = args[argIndex];
      if (arg instanceof StructuredArgument) {
        StructuredArgument structuredArgument = (StructuredArgument)arg;
        if (isIncludeStructuredArguments()) {
          if (!hasWrittenLogMsg )
            generator.writeObject(event.getMessage()); 
          if (structuredArgument instanceof MapEntriesAppendingMarker) {
            MapEntriesAppendingMarker mapEntriesAppendingMarker = (MapEntriesAppendingMarker)structuredArgument;
            writeTo(generator, mapEntriesAppendingMarker);
          } else {
            structuredArgument.writeTo(generator);
          } 
          hasWrittenLogMsg = true;
        } 
      } else if (isIncludeNonStructuredArguments()) {
        String fieldName = getNonStructuredArgumentsFieldPrefix() + getNonStructuredArgumentsFieldPrefix();
        generator.writeObjectField(fieldName, arg);
        hasWrittenLogMsg = true;
      } else if (!hasWrittenLogMsg && !isIncludeNonStructuredArguments()) {
        writeMsgWithNoArgs(generator, event);
        hasWrittenLogMsg = true;
      } 
    } 
    if (hasWrittenLogMsg)
      generator.writeEndObject(); 
  }
  
  private void writeTo(JsonGenerator generator, MapEntriesAppendingMarker meam) {
    try {
      Map<?, ?> map = (Map<?, ?>)mapField.get(meam);
      if (map != null)
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
            generator.writeFieldName(String.valueOf(entry.getKey()));
            generator.writeObject(
                getMarkedLogArgValue(String.valueOf(entry.getKey()), entry.getValue()));
            continue;
          } 
          if (entry.getKey() instanceof String && entry.getValue() instanceof Map) {
            Map<String, Object> spiMap = new HashMap<>();
            ((Map)entry.getValue()).forEach((key, value) -> spiMap.put((String)key, getMarkedLogArgValue(String.valueOf(key), value)));
            generator.writeFieldName(String.valueOf(entry.getKey()));
            generator.writeObject(spiMap);
            continue;
          } 
          generator.writeFieldName(String.valueOf(entry.getKey()));
          generator.writeObject(entry.getValue());
        }  
    } catch (Exception ex) {
      ex.printStackTrace();
    } 
  }
  
  private Object getMarkedLogArgValue(String key, Object value) {
  
    return String.valueOf(value);
  }
  
  private void writeMsgWithNoArgs(JsonGenerator generator, ILoggingEvent event) throws IOException {
	  generator.writeObject(
		  event.getFormattedMessage());
	  }
  

}
