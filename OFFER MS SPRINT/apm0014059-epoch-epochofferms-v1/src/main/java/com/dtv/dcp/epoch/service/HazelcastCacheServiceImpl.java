//package com.dtv.dcp.epoch.service;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.dtv.dcp.epoch.common.Constants;
//import com.dtv.dcp.epoch.model.cache.CacheBean;
//import com.dtv.dcp.epoch.model.cache.CacheEntry;
//import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
//import com.dtv.dcp.epoch.model.cache.CacheEvictionStatusBean;
//import com.dtv.dcp.epoch.model.cache.CacheResponse;
//import com.hazelcast.core.HazelcastInstance;
//
///*@Service*/
//public class HazelcastCacheServiceImpl implements HazelcastCacheService{
//	
//	/** The Constant log. */
//	private static final Logger log = LoggerFactory.getLogger(HazelcastCacheServiceImpl.class);
//
//	@Autowired
//	private HazelcastInstance hazelcastInstance;
//	
//	private Map<Object, Object> cpopOffersMsNearCacheMap;
//	/** The Constant EVICTED. */
//	private static final String EVICTED = " map evicted ....";
//	
//	private static final String COMMA_SEPARATED_EXP = "\\s*,\\s*";
//	
//	private static final String MSG_INVALID_MAP  = "Invalid Map Name. Please check.";
//	
//	@Override
//	public CacheResponse fetchCacheInfo(String mapName, boolean includeValues) {
//
//		cpopOffersMsNearCacheMap = null;
//		CacheResponse cacheResponse = new CacheResponse();
//		List<CacheBean> cache = new ArrayList<>();
//		List<String> cacheMapNames = null;
//		if (mapName == null || mapName.trim().isEmpty()) {
//			cacheMapNames = Arrays.asList(Constants.CATALOG_ALL_CACHE_MAP_NAMES_LIST.split(COMMA_SEPARATED_EXP));
//		} else {
//			cacheMapNames = Arrays.asList(mapName.split(COMMA_SEPARATED_EXP));
//		}
//		cacheMapNames.forEach(map ->
//			cache.add(fetchCache(map, includeValues))
//		);
//		
//		cacheResponse.setCache(cache);
//		return cacheResponse;
//	}
//	
//	private CacheBean fetchCache(String mapName, boolean includeValues) {
//		CacheBean cacheBean = new CacheBean();
//		Set<Object> keys = new HashSet<>();
//		String message = "";
//		
//		if(mapName == null || mapName.trim().isEmpty()) {
//			cacheBean.setMessage(MSG_INVALID_MAP);
//			cacheBean.setMapName("NULL");
//		} else {
//			cacheBean.setMapName(mapName);
//			cpopOffersMsNearCacheMap = hazelcastInstance.getMap(mapName);
//			if (cpopOffersMsNearCacheMap == null) {
//				message = MSG_INVALID_MAP;
//				cacheBean.setMessage(message);
//			} else {
//				keys.addAll(cpopOffersMsNearCacheMap.keySet());
//				if(keys.isEmpty()) {
//					message = "No cache entries found for map ["+ mapName + "]";
//					cacheBean.setMessage(message);
//				} else {
//					cacheBean.setKeys(keys);
//					if(includeValues) {
//						List<CacheEntry> cacheEntries = new ArrayList<>();
//						cpopOffersMsNearCacheMap.forEach((k,v)->{
//							CacheEntry cacheData = new CacheEntry();
//							cacheData.setKey(k);
//							cacheData.setValue(v);
//							cacheEntries.add(cacheData);
//						});
//						cacheBean.setCacheEntries(cacheEntries);
//					}
//				}
//			}
//		}
//		return cacheBean;
//	}
//	
//	@Override
//	public CacheEvictionResponse evictCache(String mapName) {
//		CacheEvictionResponse cacheEvictionResponse = new CacheEvictionResponse();
//		List<CacheEvictionStatusBean> cacheEvictionStatusBeans = new ArrayList<>();
//		cacheEvictionStatusBeans.add(fetchEvictionStatus(mapName));
//		cacheEvictionResponse.setStatus(cacheEvictionStatusBeans);
//		return cacheEvictionResponse;
//	}
//	
//	private CacheEvictionStatusBean fetchEvictionStatus(String mapName) {
//		CacheEvictionStatusBean cacheEvictionStatusBean = new CacheEvictionStatusBean();
//		Set<Object> keys = new HashSet<>();
//		String evictionMessage = "";
//		cpopOffersMsNearCacheMap = null;
//		if (mapName == null || mapName.trim().isEmpty()) {
//			evictionMessage = MSG_INVALID_MAP;
//		}
//		else {
//			if("all".equalsIgnoreCase(mapName)) {
//
//				cpopOffersMsNearCacheMap = hazelcastInstance.getMap(Constants.CPOPOFFERSMS_MAIN_CACHE_MAP_NAME);
//				keys.addAll(cpopOffersMsNearCacheMap.keySet());
//				cpopOffersMsNearCacheMap.clear();
//
//				cpopOffersMsNearCacheMap = hazelcastInstance.getMap(Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME);
//				keys.addAll(cpopOffersMsNearCacheMap.keySet());
//				cpopOffersMsNearCacheMap.clear();
//
//				cpopOffersMsNearCacheMap = hazelcastInstance.getMap(Constants.CPOPOFFERSMS_PROFILE_NEAR_CACHE_MAP_NAME+"X");
//				keys.addAll(cpopOffersMsNearCacheMap.keySet());
//				cpopOffersMsNearCacheMap.clear();
//
//				evictionMessage = "[" + Constants.CPOPOFFERSMS_MAIN_CACHE_MAP_NAME + "] " +	EVICTED ;
//			}
//			else {
//				cpopOffersMsNearCacheMap = hazelcastInstance.getMap(mapName);
//				keys.addAll(cpopOffersMsNearCacheMap.keySet());
//				cpopOffersMsNearCacheMap.clear();
//				evictionMessage = "[" + mapName + "] " + EVICTED;
//			}
//		}
//		cacheEvictionStatusBean.setMapName(mapName);
//		cacheEvictionStatusBean.setEvictionMessage(evictionMessage);
//		return cacheEvictionStatusBean;
//	}
//
//	@Override
//	public Object getValueFromCache(String mapName, List<String> keyList) {
//		
//		Map<Object, Object> offersMsCacheMap = hazelcastInstance.getMap(mapName);
//		return offersMsCacheMap.get(keyList);
//	}
//	
//}