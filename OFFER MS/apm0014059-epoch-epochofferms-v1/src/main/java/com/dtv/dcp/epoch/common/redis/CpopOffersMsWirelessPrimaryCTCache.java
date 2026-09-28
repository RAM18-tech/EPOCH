package com.dtv.dcp.epoch.common.redis;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;

@Component
public class CpopOffersMsWirelessPrimaryCTCache implements ICacheHandler {

	/**
	 * Gets the cache.
	 *
	 * @param key
	 *            the key
	 * @return the cache
	 */
	@Cacheable(value = Constants.CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME, key = "#key", unless = "#result==null", cacheManager = "redisCacheManagerThirdHolder")
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
	@CachePut(value = Constants.CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerThirdHolder")
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
	@CacheEvict(value = Constants.CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME, key = "#key", cacheManager = "redisCacheManagerThirdHolder")
	@Override
	public void evictCache(Object key) {

	}

	/**
	 * Evict all cache values.
	 */
	@Override
	@CacheEvict(value = Constants.CPOPOFFERSMS_CT_PRIMARY_WIRELESS_CACHE_MAP_NAME, allEntries = true, cacheManager = "redisCacheManagerThirdHolder")
	public void evictAllCacheValues() {

	}
}