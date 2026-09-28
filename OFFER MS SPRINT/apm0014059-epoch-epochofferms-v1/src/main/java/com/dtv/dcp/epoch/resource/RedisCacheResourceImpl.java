package com.dtv.dcp.epoch.resource;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
import com.dtv.dcp.epoch.representation.Resource;
import com.dtv.dcp.epoch.service.RedisCacheService;
import com.dtv.dcp.epoch.util.OffersUtils;
/**
 * The Class CacheResourceImpl.
 */
@Controller
public class RedisCacheResourceImpl implements RedisCacheResource {
	
	/** The HazelcastCacheService. */
	@Autowired
	private RedisCacheService redisCacheService;
	
	private static final Logger log = LoggerFactory.getLogger(RedisCacheResourceImpl.class);

	/**
	 * Evict cache.
	 *
	 * @param mapName the map name
	 * @param keys the keys
	 * @return the response
	 */
	@Override
	public CacheEvictionResponse evictCache(String mapName) {
		CacheEvictionResponse response = redisCacheService.evictCache(mapName);
		Resource<CacheEvictionResponse> resource = null;
		if (Optional.ofNullable(response).isPresent()) {
			resource = new Resource<>(response);
		} else {
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR);
		}
		return response;
	}

	@Override
	public CacheEvictionResponse deleteCatalogCache(String mapName, List<String> keys) {
		CacheEvictionResponse response = redisCacheService.evictCatalogOneCache(mapName, keys);
		Resource<CacheEvictionResponse> resource = null;
		if (Optional.ofNullable(response).isPresent()) {
			resource = new Resource<>(response);
		} else {
			log.error("Unable to delete CatalogOne Cache mapName={}, keys={}",OffersUtils.sanitizeData(mapName), OffersUtils.sanitizeData(keys.toString()));
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR);
		}
		return response;
	}
}