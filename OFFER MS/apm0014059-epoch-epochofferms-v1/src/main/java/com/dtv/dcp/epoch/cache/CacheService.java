package com.dtv.dcp.epoch.cache;

import java.util.List;

import com.dtv.dcp.epoch.common.Constants;


//CacheService class defines the services used for cache
/*
 * <p>Title: CacheService.java</p>
 * <p>Description: Description of the CacheService.java</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: AT&T Inc</p>
 * @author vc402t
 * @version 1.0 
 * Created on Mar 20, 2018
 * 
 *  @CachePut will trigger put api of cosc to cache the value against the objectKey passed
 *  @Cacheable will be executed only once for the given cachekey and subsequent requests won't execute the method, until the cache expires or gets flushed
 */

/**
 * The Interface CacheService.
 */
public interface CacheService {
	
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	//@Cacheable(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	<T> T getFromCache(String key);
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	//@Cacheable(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	<T> T getFromServicesCacheMap(List<String> key);
	
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	//@Cacheable(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	<T> T getFromAEMCacheMap(List<String> key);
	
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	//@Cacheable(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	<T> T getFromNearCacheMap(List<String> key);
	
	/**
	 * Put in cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param payLoad the pay load
	 * @return the t
	 */
	//@CachePut(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	<T> T putInCache(String key, T payLoad );
	
	/**
	 * Delete from cache.
	 *
	 * @param key the key
	 */
	//@CacheEvict(cacheResolver="cacheResolver", cacheNames="idpSDKNativeHazelcastCache", keyGenerator="keyGenerator")
	void deleteFromCache(String key);
	/**
	 * Gets the key.
	 *
	 * @param primaryKey
	 *            the primary key
	 * @return the key
	 */
	default String getKey(String primaryKey) {
		return  primaryKey+"_"+Constants.CACHE_NAME + "_" + Constants.OBJECT_KEY+"_"+Constants.CACHE_VERSION_ID+"_"+Constants.APPLICATION_ID ;
	}

	/**
	 * Evict remote cache.
	 */
	void evictRemoteCache();

	/**
	 * Evict near cache.
	 */
	void evictNearCache();

	/**
	 * Evict services near cache.
	 */
	void evictServicesNearCache();
	
	/**
	 * Evict Aem Media near cache.
	 */
	void evictAemMediaNearCache();
	/**
	 * Evict epoch ott near cache.
	 */
    void evictCpopOTTNearCache();
}