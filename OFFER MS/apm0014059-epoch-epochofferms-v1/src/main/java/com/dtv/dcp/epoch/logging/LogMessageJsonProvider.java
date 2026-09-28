package com.dtv.dcp.epoch.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.JsonWritingUtils;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;

public class LogMessageJsonProvider extends MessageJsonProvider {
  private static final String MESSAGE = "msg";
  
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
   
    if (event.getMarker().getName().equalsIgnoreCase("spiesapi"))
      JsonWritingUtils.writeStringField(generator, getFieldName(), event.getFormattedMessage()); 
    if (event.getMarker().getName().equalsIgnoreCase("spi"))
      JsonWritingUtils.writeStringField(generator, getFieldName(),event.getFormattedMessage()); 
    if (event.getMarker().getName().equalsIgnoreCase("esapi"))
      JsonWritingUtils.writeStringField(generator, getFieldName(), event.getFormattedMessage());  
  }
}
