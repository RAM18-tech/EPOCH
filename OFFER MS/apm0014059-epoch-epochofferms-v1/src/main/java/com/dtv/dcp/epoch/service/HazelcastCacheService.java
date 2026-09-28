package com.dtv.dcp.epoch.service;

import java.util.List;

import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
import com.dtv.dcp.epoch.model.cache.CacheResponse;

/**
 * The Interface CacheService.
 */
public interface HazelcastCacheService {
	
	/**
	 * Fetch cache info.
	 *
	 * @param mapName the map name
	 * @param includeValues the include values
	 * @return the cache response
	 */
	public CacheResponse fetchCacheInfo(String mapName, boolean includeValues);
	

	/**
	 * Evict cache.
	 *
	 * @param mapName the map name
	 * @return the cache eviction response
	 */
	public CacheEvictionResponse evictCache(String mapName);


	/**
	 * Gets the value from cache.
	 *
	 * @param mapName the map name
	 * @param keyList the key list
	 * @return the value from cache
	 */
	public Object getValueFromCache(String mapName, List<String> keyList);

}