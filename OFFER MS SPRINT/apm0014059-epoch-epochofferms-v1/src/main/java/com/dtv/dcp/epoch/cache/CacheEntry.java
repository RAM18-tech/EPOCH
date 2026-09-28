package com.dtv.dcp.epoch.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The Class CacheEntry.
 *
 * @author dp793b
 */
public class CacheEntry {
	
	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(CacheEntry.class);
			
	/** The object key. */
	private String objectKey;
	
	/** The value. */
	private Object value;
	
	/** The cache TTL. */
	private long cacheTTL;
	
	/** The backing map TTL. */
	private long backingMapTTL;
	
	/** The mod date. */
	private long modDate;
	
	/** The version. */
	private String version;
	
	/** The root. */
	private String root;
	
	/**
	 * Instantiates a new cache entry.
	 *
	 * @param objectKey the object key
	 * @param value the value
	 * @param cacheTTL the cache TTL
	 * @param backingMapTTL the backing map TTL
	 * @param modDate the mod date
	 * @param version the version
	 */
	public CacheEntry(String objectKey, Object value, long cacheTTL, long backingMapTTL, long modDate, String version) {
		super();
		this.objectKey = objectKey;
		this.value = value;
		this.cacheTTL = cacheTTL;
		this.backingMapTTL = backingMapTTL;
		this.modDate = modDate;
		this.version = version;
	}
	
	/**
	 * Instantiates a new cache entry.
	 *
	 * @param objectKey the object key
	 * @param value the value
	 * @param version the version
	 * @param root the root
	 */
	public CacheEntry(String objectKey, Object value, String version, String root) {
		super();
		this.objectKey = objectKey;
		this.value = value;
		this.version = version;
		this.root = root;
	}
	
	/**
	 * Gets the object key.
	 *
	 * @return the objectKey
	 */
	public String getObjectKey() {
		return objectKey;
	}
	
	/**
	 * Sets the object key.
	 *
	 * @param objectKey the objectKey to set
	 */
	public void setObjectKey(String objectKey) {
		this.objectKey = objectKey;
	}
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 *
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	/**
	 * Gets the cache TTL.
	 *
	 * @return the cacheTTL
	 */
	public long getCacheTTL() {
		return cacheTTL;
	}
	
	/**
	 * Sets the cache TTL.
	 *
	 * @param cacheTTL the cacheTTL to set
	 */
	public void setCacheTTL(long cacheTTL) {
		this.cacheTTL = cacheTTL;
	}
	
	/**
	 * Gets the backing map TTL.
	 *
	 * @return the backingMapTTL
	 */
	public long getBackingMapTTL() {
		return backingMapTTL;
	}
	
	/**
	 * Sets the backing map TTL.
	 *
	 * @param backingMapTTL the backingMapTTL to set
	 */
	public void setBackingMapTTL(long backingMapTTL) {
		this.backingMapTTL = backingMapTTL;
	}
	
	/**
	 * Gets the mod date.
	 *
	 * @return the modDate
	 */
	public long getModDate() {
		return modDate;
	}
	
	/**
	 * Sets the mod date.
	 *
	 * @param modDate the modDate to set
	 */
	public void setModDate(long modDate) {
		this.modDate = modDate;
	} 
	
	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Sets the version.
	 *
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
		
	/**
	 * Gets the root.
	 *
	 * @return the root
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Sets the root.
	 *
	 * @param root the new root
	 */
	public void setRoot(String root) {
		this.root = root;
	}

	/**
	 * Gets the pay load.
	 *
	 * @return the pay load
	 */
	public String getPayLoad() {
		ObjectMapper mapper = new ObjectMapper();		
		String jsonPayload = "{\"$idseMsCatalogAccount\":{\"$key\":{\"versions\":\"$version\",\"value\": $value}}}";
		jsonPayload = jsonPayload.replace("$idseMsCatalogAccount", getRoot()).replace("$key", getObjectKey()).replace("$version", getVersion());
		
		try {
			jsonPayload = jsonPayload.replace("$value", mapper.writeValueAsString(getValue()));
		} catch (JsonProcessingException e) {
			log.error("CacheEnry::getPayLoad::JsonProcessingException: {}", e);
			jsonPayload = "";
		}
		return jsonPayload;
	}
}
