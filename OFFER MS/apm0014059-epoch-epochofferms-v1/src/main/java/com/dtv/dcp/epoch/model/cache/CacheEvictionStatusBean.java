package com.dtv.dcp.epoch.model.cache;

import java.io.Serializable;
import java.util.Map;

/**
 * The Class CacheEvictionStatusBean.
 */
public class CacheEvictionStatusBean implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The map name. */
	private String mapName;
	
	/** The eviction message. */
	private String evictionMessage;
	
	private Map<String, Boolean> cacheKeyMap;
	
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
	 * Gets the eviction message.
	 *
	 * @return the eviction message
	 */
	public String getEvictionMessage() {
		return evictionMessage;
	}
	
	/**
	 * Sets the eviction message.
	 *
	 * @param evictionMessage the new eviction message
	 */
	public void setEvictionMessage(String evictionMessage) {
		this.evictionMessage = evictionMessage;
	}
	
	
	public Map<String, Boolean> getCacheKeyMap() {
		return cacheKeyMap;
	}

	public void setCacheKeyMap(Map<String, Boolean> cacheKeyMap) {
		this.cacheKeyMap = cacheKeyMap;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheEvictionStatusBean [mapName=");
		builder.append(mapName);
		builder.append(", evictionMessage=");
		builder.append(evictionMessage);
		builder.append("]");
		return builder.toString();
	}
}