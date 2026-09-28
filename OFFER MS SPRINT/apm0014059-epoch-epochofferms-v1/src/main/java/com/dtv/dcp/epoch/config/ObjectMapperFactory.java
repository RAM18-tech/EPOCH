package com.dtv.dcp.epoch.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperFactory {
  private static ObjectMapperFactory instance;
  
  private static ObjectMapper objectMapperDefault;
  
  private static ObjectMapper objectMapper;
  
  public static ObjectMapperFactory getInstance() {
    if (null == instance)
      instance = new ObjectMapperFactory(); 
    return instance;
  }
  
  public static ObjectMapper objectMapperOnly() {
    if (objectMapper == null)
      objectMapper = new ObjectMapper(); 
    return objectMapper;
  }
  
  public static ObjectMapper objectMapperDefault() {
    if (objectMapperDefault == null)
      objectMapperDefault = new ObjectMapper(); 
    objectMapperDefault.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapperDefault.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapperDefault.enable(new MapperFeature[] { MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES });
    objectMapperDefault.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    objectMapperDefault.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    objectMapperDefault.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    objectMapperDefault.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    return objectMapperDefault;
  }
}
