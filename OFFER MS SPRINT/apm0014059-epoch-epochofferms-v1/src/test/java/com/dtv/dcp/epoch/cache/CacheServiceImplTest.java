package com.dtv.dcp.epoch.cache;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class CacheServiceImplTest {
	
	/** The object key. */
	private static final String OBJECT_KEY = "session_context_data";
	/** The cache name. */
	private static final String CACHE_NAME = "idseMsCatalogAccount";
	
	@InjectMocks
	public CacheServiceImpl cacheServiceImpl;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		
	}

	@Test
	public void testGetFromCache() {
		cacheServiceImpl.deleteFromCache("keyValue");
		cacheServiceImpl.evictNearCache();
		cacheServiceImpl.evictRemoteCache();
		cacheServiceImpl.evictServicesNearCache();
		cacheServiceImpl.evictCpopOTTNearCache();
		cacheServiceImpl.evictAemMediaNearCache();
		cacheServiceImpl.evictServicesNearCache();
		List<String> listString = new ArrayList<>();
		listString.add("OnlineArea");
		cacheServiceImpl.getFromAEMCacheMap(listString);

		List<String> fromNearCacheMap = new ArrayList<>();
		fromNearCacheMap.add("fromNearCacheMap");
		cacheServiceImpl.getFromNearCacheMap(fromNearCacheMap);

		cacheServiceImpl.getFromCache("getFromCache");

		List<String> getFromServicesCacheMapKey = new ArrayList<>();
		getFromServicesCacheMapKey.add("getFromServicesCacheMapKey");
		cacheServiceImpl.getFromServicesCacheMap(getFromServicesCacheMapKey);

		List<String> getFromAEMCacheMapKey = new ArrayList<>();
		getFromAEMCacheMapKey.add("getFromAEMCacheMap");
		cacheServiceImpl.getFromAEMCacheMap(getFromAEMCacheMapKey);

		List<String> getFromNearCacheMapKey = new ArrayList<>();
		getFromNearCacheMapKey.add("getFromNearCacheMapKey");
		cacheServiceImpl.getFromNearCacheMap(getFromNearCacheMapKey);

	}
	
	@Test
	public void testCacheEntry() {
		
	String key = "DTVNEW " + "sessionId";
	CacheEntry cacheEntry = new CacheEntry(OBJECT_KEY, null, "v1", CACHE_NAME);
	assertNotNull(cacheServiceImpl.getKey("primaryKey"));
	
	cacheServiceImpl.putInCache(key, cacheEntry.getPayLoad());
	}
	
	@Test
	public void testCacheEntryExtraParams() {	
	String keyStr = "DTVNEW " + "sessionId";
	long icacheTTL = 12322278910l;
	long ibackingMapTTL = 66345678910l;	
	long imodDate = 76345678910l;
	
	CacheEntry cacheEntry1 = new CacheEntry(OBJECT_KEY, keyStr, icacheTTL, ibackingMapTTL, imodDate, CACHE_NAME);
	cacheEntry1.setObjectKey("objKey");
	cacheEntry1.setBackingMapTTL(565345678910l);
	cacheEntry1.setModDate(565345678910l);
	cacheEntry1.setRoot("root");
	cacheEntry1.setValue("aaa");
	cacheEntry1.setCacheTTL(565345678910l);
	cacheEntry1.setVersion("v2");
	assertNotNull(cacheEntry1.getVersion()); 
	assertNotNull(cacheEntry1.getCacheTTL()); 
	assertNotNull(cacheEntry1.getBackingMapTTL()); 
	assertNotNull(cacheEntry1.getModDate());
	
	cacheServiceImpl.putInCache(keyStr, cacheEntry1.getPayLoad());
	}

}
