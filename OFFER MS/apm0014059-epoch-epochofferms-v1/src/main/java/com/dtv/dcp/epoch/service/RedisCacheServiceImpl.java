package com.dtv.dcp.epoch.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.redis.CacheHandlerFactory;
import com.dtv.dcp.epoch.common.redis.ICacheHandler;
import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
import com.dtv.dcp.epoch.model.cache.CacheEvictionStatusBean;
import com.dtv.dcp.epoch.util.OffersUtils;


@Service
public class RedisCacheServiceImpl implements RedisCacheService{
	
	/** The Constant log. */
	private static final Logger log = LoggerFactory.getLogger(RedisCacheServiceImpl.class);

	@Autowired
	private CacheHandlerFactory cacheHandlerFactory;

	/** The Constant EVICTED. */
	private static final String EVICTED = " map evicted ....";
	
	
	private static final String MSG_INVALID_MAP  = "Invalid Map Name. Please check.";
	
	@Override
	public CacheEvictionResponse evictCache(String mapName) {
		CacheEvictionResponse cacheEvictionResponse = new CacheEvictionResponse();
		List<CacheEvictionStatusBean> cacheEvictionStatusBeans = new ArrayList<>();
		cacheEvictionStatusBeans.add(fetchEvictionStatus(mapName));
		cacheEvictionResponse.setStatus(cacheEvictionStatusBeans);
		return cacheEvictionResponse;
	}
	
	private CacheEvictionStatusBean fetchEvictionStatus(String mapName) {
		CacheEvictionStatusBean cacheEvictionStatusBean = new CacheEvictionStatusBean();
		Set<Object> keys = new HashSet<>();
		String evictionMessage = "";
		Map<Object, Object> offersMsNearCacheMap;
		if (mapName == null || mapName.trim().isEmpty()) {
			evictionMessage = MSG_INVALID_MAP;
		}else {
			try {
				if("all".equalsIgnoreCase(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT);
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME);
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME);
					
					evictionMessage = "[" + 
							Constants.EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME + ", "+
							Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP + ", "+
							Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT + ", "+
							Constants.EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME+ ", "+
							Constants.EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME + ", "+
							Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME+ ", "+
							Constants.EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME + ", "+
							EVICTED ;
				}
				else if(Constants.EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_PRIMARY_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_BACKUP_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else if (Constants.EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCH_PRODUCT_TYPES_ATTR_PVT_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else if(Constants.EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRIMARY_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BACKUP_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PVT_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if(Constants.EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_PRIMARY_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_BACKUP_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_PRODUCTS_PVT_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if(Constants.EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_PRIMARY_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_BACKUP_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_CT_BENEFITS_PVT_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				} 
				else if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_BACKUP);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else if (Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_CACHE_MAP_NAME_PVT);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else if (Constants.EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME.equals(mapName)) {
					evictAllCacheEntries(Constants.EPOCHOFFERSMS_IXP_PRIMARY_CACHE_MAP_NAME);
					evictionMessage = "["+mapName+"] "+ EVICTED;
				}
				else {
					evictionMessage = "Map ["+mapName+"] could not be found. "+ MSG_INVALID_MAP; 
				}
			} catch(Exception ex) {
				log.error("RedisCacheServiceImpl.fetchEvictionStatus() --- Exception ="+ ex);
			}
		}
		cacheEvictionStatusBean.setMapName(mapName);
		cacheEvictionStatusBean.setEvictionMessage(evictionMessage);
		return cacheEvictionStatusBean;
	}
	private void evictAllCacheEntries(String mapName) throws Exception {
		ICacheHandler cacheHandlerInstance = cacheHandlerFactory.getInstance(mapName);
		cacheHandlerInstance.evictAllCacheValues();
	}
	

	private void evictCacheEntriesWithKey(String mapName, List<String> keys) throws Exception {
		ICacheHandler cacheHandlerInstance = cacheHandlerFactory.getInstance(mapName);
		cacheHandlerInstance.evictCache(keys);
	}

	/**
	 *
	 */
	@Override
	public CacheEvictionResponse evictCatalogOneCache(String mapName, List<String> keys) {
		CacheEvictionResponse cacheEvictionResponse = new CacheEvictionResponse();
		List<CacheEvictionStatusBean> cacheEvictionStatusBeans = new ArrayList<>();
		cacheEvictionStatusBeans.add(evictCacheFromCatalogCache(mapName, keys));
		cacheEvictionResponse.setStatus(cacheEvictionStatusBeans);
		return cacheEvictionResponse;
	}

	/**
	 * @param mapName
	 * @param keys
	 * @return
	 */
	private CacheEvictionStatusBean evictCacheFromCatalogCache(String mapName, List<String> keys) {

		CacheEvictionStatusBean cacheEvictionStatusBean = new CacheEvictionStatusBean();
		String evictionMessage = "";
		if (StringUtils.isEmpty(mapName) || !Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME.equals(mapName)) {
			log.error("Invalid mapName or no matching found...");
			evictionMessage = MSG_INVALID_MAP;
		} else {
			try {
				if (!CollectionUtils.isEmpty(keys)) {
					List<String> cacheKeys = keys.stream().map(key -> key).collect(Collectors.toList());
					evictCacheEntriesWithKey(mapName, cacheKeys);
					evictionMessage = "[" + mapName + "] " + EVICTED + "with keys " + "[" + keys + "]";
				} else {
					evictAllCacheEntries(Constants.CPOP_CATALOB_ONE_CACHE_MAP_NAME);
					evictionMessage = "[" + mapName + "] " + EVICTED;
				}
			} catch (Exception ex) {
				log.error("Exception in deleting CatalogOne cache map={}, keys={} Exception=",OffersUtils.sanitizeData(mapName),OffersUtils.sanitizeData(keys.toString()),OffersUtils.sanitizeData(ex.toString()));
			}
		}
		cacheEvictionStatusBean.setMapName(mapName);
		cacheEvictionStatusBean.setEvictionMessage(evictionMessage);
		return cacheEvictionStatusBean;
	}

}