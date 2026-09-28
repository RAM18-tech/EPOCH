package com.dtv.dcp.epoch.resource;

//package com.dtv.dcp.epoch.resource;

//
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Mockito.when;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.att.idp.core.exception.ServiceException;
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.att.idp.video.epoch.common.Constants;
//import com.att.idp.video.epoch.model.cache.CacheEvictionResponse;
//import com.att.idp.video.epoch.model.cache.CacheResponse;
//import com.att.idp.video.epoch.service.HazelcastCacheService;
//
///**
// * The Class HazelcastCacheResourceTest.
// */
//public class HazelcastCacheResourceTest {
//
//	/** The hazelcast cache resource. */
//	@InjectMocks
//	private HazelcastCacheResourceImpl hazelcastCacheResource;
//
//	/** The m hazelcast cache service. */
//	@Mock
//	private HazelcastCacheService mHazelcastCacheService;
//
//	/** The map name valid. */
//	private String mapNameValid;
//
//	/** The map name in valid. */
//	private String mapNameInValid;
//
//	/**
//	 * Setup.
//	 */
//	@Before
//	public void setup() {
//		MockitoAnnotations.openMocks(this);
//		mapNameValid = Constants.OFFERSMS_NEAR_CACHE_MAP_NAME;
//		mapNameInValid = "InvalidMapWhichIsNotPresentInOffersMS";
//	}
//
//	/**
//	 * Test evict cache success.
//	 */
//	@Test
//	public void testEvictCacheSuccess() {
//		when(mHazelcastCacheService.evictCache(mapNameValid)).thenReturn(new CacheEvictionResponse());
//		assertNotNull(hazelcastCacheResource.evictCache(mapNameValid, null));
//	}
//
//	/**
//	 * Test evict cache exception.
//	 */
//	@Test(expected = ServiceException.class)
//	public void testEvictCacheException() {
//		when(mHazelcastCacheService.evictCache(mapNameInValid)).thenReturn(null);
//		assertNotNull(hazelcastCacheResource.evictCache(mapNameValid, null));
//	}
//
//	/**
//	 * Test cache info success with included data.
//	 */
//	@Test
//	public void testCacheInfoSuccessWithIncludedData() {
//		boolean includeValues = true;
//		when(mHazelcastCacheService.fetchCacheInfo(mapNameValid, includeValues)).thenReturn(new CacheResponse());
//		assertNotNull(hazelcastCacheResource.fetchCacheInfo(mapNameValid, includeValues));
//	}
//
//	/**
//	 * Test cache info success excluded data.
//	 */
//	@Test
//	public void testCacheInfoSuccessExcludedData() {
//		boolean includeValues = false;
//		when(mHazelcastCacheService.fetchCacheInfo(mapNameValid, includeValues)).thenReturn(new CacheResponse());
//		assertNotNull(hazelcastCacheResource.fetchCacheInfo(mapNameValid, includeValues));
//	}
//
//	/**
//	 * Test cache info exception with included data.
//	 */
//	@Test(expected = ServiceException.class)
//	public void testCacheInfoExceptionWithIncludedData() {
//		boolean includeValues = true;
//		when(mHazelcastCacheService.fetchCacheInfo(mapNameValid, includeValues)).thenReturn(null);
//		assertNotNull(hazelcastCacheResource.fetchCacheInfo(mapNameValid, includeValues));
//	}
//
//	/**
//	 * Test cache info exception with exluded data.
//	 */
//	@Test(expected = ServiceException.class)
//	public void testCacheInfoExceptionWithExludedData() {
//		boolean includeValues = false;
//		when(mHazelcastCacheService.fetchCacheInfo(mapNameValid, includeValues)).thenReturn(null);
//		assertNotNull(hazelcastCacheResource.fetchCacheInfo(mapNameValid, includeValues));
//	}
//
//	@Test
//	public void testCacheValue() throws Exception {
//		String key = "test,key";
//		String[] keyArray = key.split(",");
//		List<String> keyList = new ArrayList<>();
//		for(String str: keyArray) {
//			keyList.add(str);
//		}
//		when(mHazelcastCacheService.getValueFromCache(Constants.OFFERSMS_NEAR_CACHE_AEM_MAP_NAME, keyList)).thenReturn(new Object());
//		Object response = hazelcastCacheResource.getValueFromCache(Constants.OFFERSMS_NEAR_CACHE_AEM_MAP_NAME, "test,key");
//		assertNotNull(response);
//
//		response = null;
//		when(mHazelcastCacheService.getValueFromCache(Constants.OFFERSMS_NEAR_CACHE_MAP_NAME, keyList)).thenReturn(new Object());
//		response = hazelcastCacheResource.getValueFromCache(Constants.OFFERSMS_NEAR_CACHE_MAP_NAME, "test,key");
//		assertNotNull(response);
//
//		response = null;
//		when(mHazelcastCacheService.getValueFromCache(Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, keyList)).thenReturn(new Object());
//		response = hazelcastCacheResource.getValueFromCache(Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, "test,key");
//		assertNotNull(response);
//
//		keyList.add("test1");
//		try {
//			when(mHazelcastCacheService.getValueFromCache(Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, keyList)).thenReturn(new Object());
//			response = hazelcastCacheResource.getValueFromCache(Constants.OFFERSMS_SERVICES_CACHE_MAP_NAME, "test");
//		}catch(ServiceException se) {
//			assertNotNull(se);
//		}
//
//
//	}
//}