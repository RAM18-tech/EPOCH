package com.dtv.dcp.epoch.common.redis;

public interface ICacheHandler {
	
	/**
	 * Gets the cache.
	 *
	 * @param key the key
	 * @return the cache
	 */
	public Object getCache(Object key) ;
	
	/**
	 * Put cache.
	 *
	 * @param key the key
	 * @param value the value
	 * @return Object, if successful
	 */
	public Object putCache(Object key, Object value);
	
	/**
	 * Evict cache.
	 *
	 * @param key the key
	 */
	public void evictCache(Object key);
		
	/**
	 * Evict all cache values.
	 */
	public void evictAllCacheValues();
}