package com.dtv.dcp.epoch.common.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class EpochOffersMsIXPCache1 implements ICacheHandler {
	
	public static final String EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME = "epochOffersMsIXPCache1"; 

	/**
	 * Gets the cache.
	 *
	 * @param key
	 *            the key
	 * @return the cache
	 */
	@Cacheable(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME, key = "#key", unless = "#result==null", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public Object getCache(Object key) {
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
	@CachePut(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public Object putCache(Object key, Object value) {
		return value;
	}

	/**
	 * Evict cache.
	 *
	 * @param key
	 *            the key
	 */
	@CacheEvict(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public void evictCache(Object key) {

	}

	/**
	 * Evict all cache values.
	 */
	@Override
	@CacheEvict(value = EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME, allEntries = true, cacheManager = "redisCacheManagerSecondHolder")
	public void evictAllCacheValues() {

	}
}