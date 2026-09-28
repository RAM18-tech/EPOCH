package com.dtv.dcp.epoch.common.redis;

import com.dtv.dcp.epoch.common.Constants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class EpochOfferMsGlobalEligibilityConfigCTCachePvt implements ICacheHandler {

	/**
	 * Gets the cache.
	 *
	 * @param key
	 *            the key
	 * @return the cache
	 */
	@Cacheable(value = Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_PVT, key = "#key", unless = "#result==null", cacheManager = "redisCacheManagerSecondHolder")
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
	@CachePut(value = Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_PVT, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
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
	@CacheEvict(value = Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_PVT, key = "#key", cacheManager = "redisCacheManagerSecondHolder")
	@Override
	public void evictCache(Object key) {
		// TODO Auto-generated method stub

	}

	/**
	 * Evict all cache values.
	 */
	@Override
	@CacheEvict(value = Constants.EPOCHOFFERSMS_GLOBAL_ELIGIBILITY_CONFIGURATIONS_CACHE_MAP_NAME_PVT, allEntries = true, cacheManager = "redisCacheManagerSecondHolder")
	public void evictAllCacheValues() {
		// TODO Auto-generated method stub

	}

}