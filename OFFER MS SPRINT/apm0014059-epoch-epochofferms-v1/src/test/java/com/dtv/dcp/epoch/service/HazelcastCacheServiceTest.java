package com.dtv.dcp.epoch.service;
//package com.att.idp.video.epoch.service;
//
//import static org.junit.Assert.assertNotNull;
//import static org.mockito.Mockito.when;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import com.att.idp.video.epoch.common.Constants;
//import com.hazelcast.core.HazelcastInstance;
//import com.hazelcast.core.IMap;
//
//public class HazelcastCacheServiceTest {
//	
//	@InjectMocks
//	private HazelcastCacheServiceImpl mHazelcastCacheService;
//	
//	@Mock
//	private HazelcastInstance mHazelcastInstance;
//
//	/** The map name valid. */
//	private String mapNameValid;
//	
//	/** The map name in valid. */
//	private String mapNameInValid;
//	
//	@Mock
//	private IMap<Object, Object> cacheMap;
//	
//	@Before
//	public void setup() {
//		MockitoAnnotations.openMocks(this);
//		mapNameValid = Constants.OFFERSMS_NEAR_CACHE_MAP_NAME;
//		mapNameInValid = "InvalidMapWhichIsNotPresentInOffersMS";
////		cacheMap = new HashMap<>();
//	}
//	
//	@Test
//	public void testFetchCacheInfoWithIncludedData() {
//		boolean includeValues = true;
//		Set<Object> keys = new HashSet<>();
//		mHazelcastInstance.getMap(mapNameValid);
//		when(mHazelcastInstance.getMap(mapNameValid)).thenReturn(cacheMap);
//		when(cacheMap.keySet()).thenReturn(keys);
//		assertNotNull(mHazelcastCacheService.fetchCacheInfo(mapNameValid, includeValues));
//	}
//}