package com.dtv.dcp.epoch.cache;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Repository;

import com.dtv.dcp.epoch.common.Constants;




/*
 * <p>Title: CacheServiceImpl.java</p>
 * <p>Description: Description of the CacheServiceImpl.java</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: AT&T Inc</p>
 * @author vc402t
 * @version 1.0 
 * Created on Mar 20, 2018
 */
//@Component
@Repository
@EnableCaching
public class CacheServiceImpl implements CacheService{
	
	
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	@Override
	public <T> T getFromCache(String key) {
		return null;
	}
	
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	@Cacheable(value=Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, key = "#key", unless="#result==null")
	@Override
	public <T> T getFromServicesCacheMap(List<String> key) {
		return null;
	}
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	@Cacheable(value=Constants.OFFERSMS_NEAR_CACHE_AEM_MAP_NAME, key = "#key", unless="#result==null")
	@Override
	public <T> T getFromAEMCacheMap(List<String> key) {
		return null;
	}
	
	/**
	 * Gets the from cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @return the from cache
	 */
	@Cacheable(value=Constants.OFFERSMS_NEAR_CACHE_MAP_NAME, key = "#key", unless="#result==null")
	@Override
	public <T> T getFromNearCacheMap(List<String> key) {
		return null;
	}
	
	/**
	 * Put in cache.
	 *
	 * @param <T> the generic type
	 * @param key the key
	 * @param payLoad the pay load
	 * @return the t
	 */
	@CachePut(value=Constants.OFFERSMS_MAIN_CACHE_MAP_NAME, key = "#key.toString()", unless="#result==null")
	@Override
	public <T> T putInCache(String key, T payload) {
		return payload;
	}

	/**
	 * Delete from cache.
	 *
	 * @param key the key
	 */
	@CacheEvict(value=Constants.OFFERSMS_MAIN_CACHE_MAP_NAME, key = "#key.toString()")
	@Override
	public void deleteFromCache(String key) {
		// Cache is evicted based on the annotation
	}

	/* 
	 * @see com.att.idp.catalog.cache.CacheService#evictRemoteCache()
	 */
	@CacheEvict(value=Constants.OFFERSMS_MAIN_CACHE_MAP_NAME, allEntries=true)
	@Override
	public void evictRemoteCache() {
		// Remote Cache is evicted based on the annotation
    }
	
	/* 
	 * @see com.att.idp.catalog.cache.CacheService#evictNearCache()
	 */
	@CacheEvict(value=Constants.OFFERSMS_NEAR_CACHE_MAP_NAME, allEntries=true)
	@Override
	public void evictNearCache() {
		// Near Cache is evicted based on the annotation
    }
	
	@CacheEvict(value=Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, allEntries=true)
	@Override
	public void evictServicesNearCache() {
		// Service Near Cache is evicted based on the annotation
	}
	
	@CacheEvict(value=Constants.OFFERSMS_NEAR_CACHE_AEM_MAP_NAME, allEntries=true)
	@Override
	public void evictAemMediaNearCache() {
		// Service Near Cache is evicted based on the annotation
	}
	@CacheEvict(value=Constants.OFFERSMS_CPOP_NEAR_CACHE_MAP_NAME, allEntries=true)
	@Override
	public void evictCpopOTTNearCache() {
		// Service Near Cache is evicted based on the annotation
	}
}