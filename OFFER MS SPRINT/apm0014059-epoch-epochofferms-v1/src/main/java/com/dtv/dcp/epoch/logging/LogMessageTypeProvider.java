package com.dtv.dcp.epoch.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import net.logstash.logback.composite.loggingevent.MessageJsonProvider;

public class LogMessageTypeProvider extends MessageJsonProvider {
  public void writeTo(JsonGenerator generator, ILoggingEvent event) throws IOException {
    String msgType = getMsgType(event);
    if (msgType != null)
      generator.writeStringField(getFieldName(), getMsgType(event)); 
  }
  
  private String getMsgType(ILoggingEvent event) {
    String msgType = null;
    String msg = event.getMessage();
    switch (msg) {
      case "req":
        msgType = "REST-IB-REQ";
        return msgType;
      case "res":
        if (event.getLevel() == Level.ERROR) {
          msgType = "REST-IB-ERROR";
        } else {
          msgType = "REST-IB-RES";
        } 
        return msgType;
      case "racReq":
        msgType = "REST-OB-REQ";
        return msgType;
      case "sacReq":
        msgType = "SOAP-OB-REQ";
        return msgType;
      case "ejbcReq":
        msgType = "EJB-OB-REQ";
        return msgType;
      case "racRes":
        if (event.getLevel() == Level.ERROR) {
          msgType = "REST-OB-ERROR";
        } else {
          msgType = "REST-OB-RES";
        } 
        return msgType;
      case "sacRes":
        msgType = "SOAP-OB-RES";
        return msgType;
      case "ejbcRes":
        msgType = "EJB-OB-RES";
        return msgType;
      case "sacFault":
        msgType = "SOAP-OB-ERROR";
        return msgType;
    } 
    if (event.getLevel() == Level.ERROR) {
      msgType = "SERVICE-MSG-ERROR";
    } else {
      msgType = "SERVICE-MSG";
    } 
    return msgType;
  }
}
