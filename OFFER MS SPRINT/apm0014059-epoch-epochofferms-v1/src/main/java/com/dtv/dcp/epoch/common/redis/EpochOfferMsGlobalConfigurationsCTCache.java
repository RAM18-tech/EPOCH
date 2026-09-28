package com.dtv.dcp.epoch.common.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;

@Component
public class EpochOfferMsGlobalConfigurationsCTCache implements ICacheHandler {

	/**
	 * Gets the cache.
	 *
	 * @param key
	 *            the key
	 * @return the cache
	 */
	@Cacheable(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME, key = "#key", unless = "#result==null", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public Object getCache(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Put cache.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return Object, if successful
	 */
	@CachePut(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public Object putCache(Object key, Object value) {
		// TODO Auto-generated method stub
		return value;
	}

	/**
	 * Evict cache.
	 *
	 * @param key
	 *            the key
	 */
	@CacheEvict(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public void evictCache(Object key) {
		// TODO Auto-generated method stub

	}

	/**
	 * Evict all cache values.
	 */
	@Override
	@CacheEvict(value = Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME, allEntries = true, cacheManager = "redisCacheManagerSecondHolder")

	public void evictAllCacheValues() {
		// TODO Auto-generated method stub

	}

}