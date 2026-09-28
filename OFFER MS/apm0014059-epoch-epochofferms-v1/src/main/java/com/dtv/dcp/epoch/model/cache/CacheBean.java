package com.dtv.dcp.epoch.model.cache;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The Class CacheBean.
 */
@JsonPropertyOrder({"mapName", "keys", "cacheEntries"})
public class CacheBean implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The keys. */
	@JsonProperty("keys")
	private Set<Object> keys;
	
	/** The map name. */
	@JsonProperty("mapName")
	private String mapName;
	
	/** The cache entries. */
	@JsonProperty("cacheEntries")
	private List<CacheEntry> cacheEntries;
	
	/** The message. */
	private String message;

	/**
	 * Gets the keys.
	 *
	 * @return the keys
	 */
	public Set<Object> getKeys() {
		return keys;
	}

	/**
	 * Sets the keys.
	 *
	 * @param keys the new keys
	 */
	public void setKeys(Set<Object> keys) {
		this.keys = keys;
	}

	/**
	 * Gets the map name.
	 *
	 * @return the map name
	 */
	public String getMapName() {
		return mapName;
	}

	/**
	 * Sets the map name.
	 *
	 * @param mapName the new map name
	 */
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	/**
	 * Gets the cache entries.
	 *
	 * @return the cache entries
	 */
	public List<CacheEntry> getCacheEntries() {
		return cacheEntries;
	}

	/**
	 * Sets the cache entries.
	 *
	 * @param cacheEntries the new cache entries
	 */
	public void setCacheEntries(List<CacheEntry> cacheEntries) {
		this.cacheEntries = cacheEntries;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheBean [keys=");
		builder.append(keys);
		builder.append(", mapName=");
		builder.append(mapName);
		builder.append(", cacheEntries=");
		builder.append(cacheEntries);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}
}