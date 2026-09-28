package com.dtv.dcp.epoch.service;

import java.util.List;

import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;

/**
 * The Interface CacheService.
 */
public interface RedisCacheService {

	/**
	 * Evict cache.
	 *
	 * @param mapName the map name
	 * @return the cache eviction response
	 */
	public CacheEvictionResponse evictCache(String mapName);	

	public CacheEvictionResponse evictCatalogOneCache(String mapName, List<String> keys);

}