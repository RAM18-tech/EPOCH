//package com.dtv.dcp.epoch.resource;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import javax.ws.rs.core.Response;
//
//import com.dtv.dcp.epoch.exception.ServiceException;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.dtv.dcp.epoch.message.ErrorMessages;
//import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
//import com.dtv.dcp.epoch.model.cache.CacheResponse;
//import com.dtv.dcp.epoch.representation.Resource;
//import com.dtv.dcp.epoch.service.HazelcastCacheService;
//
///**
// * The Class CacheResourceImpl.
// */
///* @Controller */
//public class HazelcastCacheResourceImpl implements HazelcastCacheResource {
//
//	/** The HazelcastCacheService. */
//	@Autowired
//	private HazelcastCacheService hazelcastCacheService;
//
//	/**
//	 * Evict cache.
//	 *
//	 * @param mapName
//	 *            the map name
//	 * @param keys
//	 *            the keys
//	 * @return the response
//	 */
//	@Override
//	public Response evictCache(String mapName, List<String> keys) {
//		CacheEvictionResponse response = hazelcastCacheService.evictCache(mapName);
//		Resource<CacheEvictionResponse> resource = null;
//		if (Optional.ofNullable(response).isPresent()) {
//			resource = new Resource<>(response);
//		} else {
//			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR);
//		}
//		return Response.ok(resource).build();
//	}
//
//	/**
//	 * Fetch cache info.
//	 *
//	 * @param mapName
//	 *            the map name
//	 * @param includeValues
//	 *            the include values
//	 * @return the response
//	 */
//	@Override
//	public Response fetchCacheInfo(String mapName, boolean includeValues) {
//		Resource<CacheResponse> resource = null;
//		CacheResponse cacheResponse = hazelcastCacheService.fetchCacheInfo(mapName, includeValues);
//		if (Optional.ofNullable(cacheResponse).isPresent()) {
//			resource = new Resource<>(cacheResponse);
//		} else {
//			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR);
//		}
//		return Response.ok(resource).build();
//	}
//
//	/**
//	 * Gets the value from cache.
//	 *
//	 * @param mapName
//	 *            the map name
//	 * @param key
//	 *            the key
//	 * @return the value from cache
//	 */
//	@Override
//	public Response getValueFromCache(String mapName, String key) {
//
//		String[] keyArray = key.split(",");
//		List<String> keyList = new ArrayList<>();
//		for (String str : keyArray) {
//			keyList.add(str);
//		}
//		Resource<Object> resource = null;
//		Object response = hazelcastCacheService.getValueFromCache(mapName, keyList);
//
//		if (Optional.ofNullable(response).isPresent()) {
//			resource = new Resource<>(response);
//		} else {
//			throw ((new ServiceException(ErrorMessages.CACHE_VALUE_NOT_FOUND))
//					.addDetail(ErrorMessages.CACHE_VALUE_NOT_FOUND_DETAIL, key));
//		}
//		return Response.ok(resource).build();
//
//	}
//
//}