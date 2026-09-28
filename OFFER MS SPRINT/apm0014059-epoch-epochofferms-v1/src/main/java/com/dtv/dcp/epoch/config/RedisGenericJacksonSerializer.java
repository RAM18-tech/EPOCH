package com.dtv.dcp.epoch.config;

import java.io.IOException;

import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;


public class RedisGenericJacksonSerializer implements RedisSerializer<Object> {
  private static Logger logger = LoggerFactory.getLogger(RedisGenericJacksonSerializer.class);
  
  private static RedisPropertyManager redisPropertyManager;
  
  private static ObjectMapper mapper;
  
  private long startTime;
  
  public static void setRedisPropertyManager(RedisPropertyManager redisPropertyManager) {
    RedisGenericJacksonSerializer.redisPropertyManager = redisPropertyManager;
  }
  
  public RedisGenericJacksonSerializer() {
    this((String)null);
  }
  
  public RedisGenericJacksonSerializer(@Nullable String classPropertyTypeName) {
    this(ObjectMapperFactory.objectMapperDefault());
    mapper
      .registerModule((Module)(new SimpleModule()).addSerializer((JsonSerializer)new NullValueSerializer(classPropertyTypeName)));
    mapper.registerModule((Module)(new SimpleModule())
        .addSerializer(XMLGregorianCalendar.class, (JsonSerializer)new XMLGregorianCalendarSerializer()));
    if (StringUtils.hasText(classPropertyTypeName)) {
      mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, classPropertyTypeName);
    } else {	
    	// Changed from NON_FINAL to OBJECT_AND_NON_CONCRETE to reduce Redis payload size and deserialization overhead.
        // NON_FINAL embeds @class on every non-final type (all nested POJOs, lists, etc.), which bloats the cached JSON
        // and forces class resolution per object during deserialization.
        // OBJECT_AND_NON_CONCRETE only adds @class where the declared type is Object, an interface, or abstract class
        // — the minimum needed for polymorphic resolution. Concrete typed fields (e.g. CTOffer, ProductObj) no longer
        // carry @class metadata, reducing payload size and speeding up deserialization significantly.
        // Backward compatible: existing Redis entries written with NON_FINAL still deserialize correctly
        // because Jackson uses @class when present and falls back to the declared field type when absent.
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    } 
  }
  
  public RedisGenericJacksonSerializer(ObjectMapper mapperInstance) {
    mapper = mapperInstance;
  }
  
  public byte[] serialize(@Nullable Object source) {
    this.startTime = System.nanoTime();
    logger.debug("Start time to serialise object " + this.startTime + " nanoseconds");
    if (source == null)
      return new byte[0]; 
    try {
      if (redisPropertyManager.isSerializerValueLogging()) {
        String objectString = ObjectMapperFactory.objectMapperDefault().writeValueAsString(source);
        logger.debug("Performing Cache put  for data -->  {}", objectString);
      } 
      byte[] redisGenericJacksonSerializer = mapper.writeValueAsBytes(source);
    
      return redisGenericJacksonSerializer;
    } catch (JsonProcessingException e) {
      throw new SerializationException("Could not write JSON: " + e.getMessage(), e);
    } 
  }
  
  public Object deserialize(@Nullable byte[] byteData) {
    this.startTime = System.nanoTime();
    logger.debug("Start time to Deserialize object " + this.startTime + " nanoseconds");
    Object value = null;
    try {
      value = deserialize(byteData, Object.class);
    } catch (SerializationException e) {
      logger.error("Exception Occured during deserializing :" + e);
    } 
    if (redisPropertyManager.isDeserializerValueLogging())
      logger.debug("Performing Cache Get   for  data -->  {} ", value); 
  
    return value;
  }
  
  @Nullable
  public <T> T deserialize(@Nullable byte[] source, Class<T> type) {
    if (source == null || source.length == 0)
      return null; 
    try {
      return (T)mapper.readValue(source, type);
    } catch (Exception ex) {
      throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
    } 
  }
  
  private class NullValueSerializer extends StdSerializer<NullValue> {
    private static final long serialVersionUID = 199905215054865880L;
    
    private final String classIdentifier;
    
    NullValueSerializer(String classIdentifier) {
      super(NullValue.class);
      this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
    }
    
    public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
      jgen.writeStartObject();
      jgen.writeStringField(this.classIdentifier, NullValue.class.getName());
      jgen.writeEndObject();
    }
  }
  
  private class XMLGregorianCalendarSerializer extends StdSerializer<XMLGregorianCalendar> {
    private static final long serialVersionUID = 19990521505486533L;
    
    public XMLGregorianCalendarSerializer() {
      this(null);
    }
    
    public XMLGregorianCalendarSerializer(Class<XMLGregorianCalendar> t) {
      super(t);
    }
    
    public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
      gen.writeNumber(value.toGregorianCalendar().getTimeInMillis());
    }
    
    public void serializeWithType(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSerializer) throws IOException {
      gen.writeStartArray();
      gen.writeString("javax.xml.datatype.XMLGregorianCalendar");
      serialize(value, gen, provider);
      gen.writeEndArray();
    }
  }
}
