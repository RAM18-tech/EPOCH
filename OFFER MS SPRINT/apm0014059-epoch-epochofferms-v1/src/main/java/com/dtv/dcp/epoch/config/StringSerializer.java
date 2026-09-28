package com.dtv.dcp.epoch.config;

import java.security.AccessControlException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.lang.Nullable;

public class StringSerializer<T> extends GenericToStringSerializer<T> {
  private static final Logger logger = LoggerFactory.getLogger(StringSerializer.class);
  
  private static RedisPropertyManager redisPropertyManager;
  
  private Map<String, String> exceptionMappings;
  
  private long startTime;
  
  @Autowired
  public StringSerializer(Class<T> type) {
    super(type);
  }
  
  public static void setRedisPropertyManager(RedisPropertyManager redisPropertyManager) {
    StringSerializer.redisPropertyManager = redisPropertyManager;
  }
  
  public byte[] serialize(@Nullable T object) {
    String mapName = null;
    String cacheKey = null;
    String microserviceArtifactId = System.getProperty("info.build.artifact");
    Map<Object, Object> collectorMap = keySplitJoin(object);
    if (null != collectorMap)
      for (Map.Entry<Object, Object> map : collectorMap.entrySet()) {
        mapName = map.getKey().toString();
        cacheKey = map.getValue().toString();
      }  
    this.startTime = System.nanoTime();
    logger.debug("Start time " + this.startTime + " nanoseconds to serialise object for map " + mapName + " with key " + cacheKey);
    if (redisPropertyManager.isSkipValidation() == Boolean.TRUE.booleanValue()) {
      if (redisPropertyManager.isSerializerKeyLogging())
        logger.debug("serializing for map " + mapName + " with key " + cacheKey); 
      byte[] serializeObject = super.serialize(object);
    
      return serializeObject;
    } 
    if (null != microserviceArtifactId && null != collectorMap && !collectorMap.isEmpty()) {
      if ((null != mapName && mapName.toUpperCase().startsWith(microserviceArtifactId.toUpperCase())) || (null != this.exceptionMappings && 
        
        processExceptionMapList(this.exceptionMappings, mapName, microserviceArtifactId))) {
        if (redisPropertyManager.isSerializerKeyLogging())
          logger.debug("serializing for map -> {} ", new Object[] { mapName, " with key -->  {} ", cacheKey }); 
        byte[] serializeObject = super.serialize(object);
     
        return serializeObject;
      } 
      throw new AccessControlException("IDPCache-Auth-401 : Exception occurerd  while proceesing cache operation request. Please verify the cache map name starts with the  microservice name followed by a dot . For assistance, Contact Cache Team for cache nomenclature");
    } 
    return new byte[0];
  }
  
  public T deserialize(@Nullable byte[] bytes) {
    this.startTime = System.nanoTime();
    logger.debug("Start Time " + this.startTime + " for Deserialization");
    Object obj = super.deserialize(bytes);
    String key = null;
    Map<Object, Object> collectorMap = keySplitJoin(obj);
    if (null != collectorMap) {
      for (Map.Entry<Object, Object> map : collectorMap.entrySet()) {
        key = map.getValue().toString();
        if (redisPropertyManager.isDeserializerKeyLogging())
          logger.info("deserializing for map {} ", new Object[] { map.getKey(), " with key --> {} ", key }); 
      } 
     
    } 
    return (T)key;
  }
  
  private static Map<Object, Object> keySplitJoin(@Nullable Object object) {
    Map<Object, Object> collectorMap = null;
    String mapNameKeyDelimiter = "::";
    if (null != object && object instanceof String)
      try {
        Stream<String> stream = Stream.of(object.toString());
        collectorMap = stream.collect(Collectors.toMap(val -> val.substring(0, val.indexOf("::")), val -> val.substring(val.indexOf("::") + 2)));
      } catch (Exception ex) {
        logger.error("Exception occured -> " + ex.toString());
      }  
    return (null != collectorMap && collectorMap.isEmpty()) ? Collections.<Object, Object>emptyMap() : collectorMap;
  }
  
  public void setExceptionMappings(Map<String, String> exceptionMappings) {
    this.exceptionMappings = exceptionMappings;
  }
  
  private boolean processExceptionMapList(@Nullable Map<String, String> exceptionMappings, String mapName, String msArtifactId) {
    for (Map.Entry<String, String> map : exceptionMappings.entrySet()) {
      if (null != map.getKey() && ((String)map.getKey()).equalsIgnoreCase(msArtifactId))
        for (String mapException : Arrays.<String>asList(((String)map.getValue()).split(","))) {
          if (null != mapException && mapException.equalsIgnoreCase(mapName))
            return true; 
        }  
    } 
    return false;
  }
}
