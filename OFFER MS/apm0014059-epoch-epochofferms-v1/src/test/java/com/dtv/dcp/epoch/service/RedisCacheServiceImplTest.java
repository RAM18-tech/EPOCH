package com.dtv.dcp.epoch.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.redis.CacheHandlerFactory;
import com.dtv.dcp.epoch.common.redis.CpopOffersMsWirelessBackupCTCache;
import com.dtv.dcp.epoch.common.redis.CpopOffersMsWirelessPrimaryCTCache;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache1;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache2;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache3;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache1;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache2;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache3;
import com.dtv.dcp.epoch.common.redis.ICacheHandler;
import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;

@ExtendWith(MockitoExtension.class)
public class RedisCacheServiceImplTest {
	
	@InjectMocks
	RedisCacheServiceImpl redisCacheServiceImpl;
	
	@Mock
	CacheHandlerFactory cacheHandlerFactory;
	
	@Mock
	EpochOffersMsCTCache1 epochOffersMsCTCache1;
	
	/** The generic cache handler. */
	@Mock
	EpochOffersMsCTCache2 epochOffersMsCTCache2;
	/** The generic cache handler. */
	@Mock
	EpochOffersMsCTCache3 epochOffersMsCTCache3;
	
	@Mock
	EpochOffersMsProductsCTCache1 msProductsCTCache1;
	
	@Mock
	EpochOffersMsProductsCTCache2 msProductsCTCache2;
	
	@Mock
	EpochOffersMsProductsCTCache3 msProductsCTCache3;
	
	@Mock
	CpopOffersMsWirelessPrimaryCTCache wirelessPrimaryCache;
	
	@Mock
	CpopOffersMsWirelessBackupCTCache wirelessBackupCache;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testGetInstance() {
		
		CacheHandlerFactory casheHanderFac = new CacheHandlerFactory();
		ICacheHandler instance = casheHanderFac.getInstance("mapName");
		ICacheHandler instance1 = casheHanderFac.getInstance("epochOffersMsCTCache1");
		ICacheHandler instance2 = casheHanderFac.getInstance("epochOffersMsCTCache2");
		ICacheHandler instance3 = casheHanderFac.getInstance("epochOffersMsCTCache3");
		ICacheHandler instance4 = casheHanderFac.getInstance("epochOffersMsProductsCTCache1");
		ICacheHandler instance5 = casheHanderFac.getInstance("epochOffersMsProductsCTCache2");
		ICacheHandler instance6 = casheHanderFac.getInstance("epochOffersMsProductsCTCache3");
		assertNull(instance1);
		assertNull(instance2);
		assertNull(instance3);
		assertNull(instance);
		assertNull(instance4);
		assertNull(instance5);
		assertNull(instance6);
		
	}
	
	@Test
	public void testEvictCacheForAll() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = epochOffersMsCTCache1;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("All");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsCTCache1() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = epochOffersMsCTCache1;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache1");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsCTCache2() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = epochOffersMsCTCache2;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache2");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsCTCache3() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = epochOffersMsCTCache3;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache3");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsProductCTCache1() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = msProductsCTCache1;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache1");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsProductCTCache2() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = msProductsCTCache2;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache2");
		assertNotNull(response);
	}
	
	@Test
	public void testEvictCacheForEpochOffersMsProductCTCache3() {
		CacheEvictionResponse response = new CacheEvictionResponse();
		ICacheHandler cacheHandler = msProductsCTCache3;
		doReturn(cacheHandler).when(cacheHandlerFactory).getInstance(Mockito.any());
		response = redisCacheServiceImpl.evictCache("epochOffersMsCTCache3");
		assertNotNull(response);
	}
}
