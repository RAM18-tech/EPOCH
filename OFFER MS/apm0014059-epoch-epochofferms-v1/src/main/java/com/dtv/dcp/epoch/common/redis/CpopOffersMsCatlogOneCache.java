package com.dtv.dcp.epoch.common.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;

@Component
public class CpopOffersMsCatlogOneCache implements ICacheHandler {

	/**
	 * Gets the cache.
	 *
	 * @param key the key
	 * @return the cache
	 */
	@Cacheable(value = Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME, key = "#key", unless = "#result==null", cacheManager = "redisCacheManagerFirstHolder")
	@Override
	public Object getCache(Object key) {
		return null;
	}

	/**
	 * Put cache.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return Object, if successful
	 */
	@CachePut(value = Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerFirstHolder")
	@Override
	public Object putCache(Object key, Object value) {
		return value;
	}

	/**
	 * Evict cache.
	 *
	 * @param key the key
	 */
	@CacheEvict(value = Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerFirstHolder")
	@Override
	public void evictCache(Object key) {
	}

	/**
	 * Evict all cache values.
	 */
	@Override
	@CacheEvict(value = Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME, allEntries = true, cacheManager = "redisCacheManagerFirstHolder")
	public void evictAllCacheValues() {
	}

}