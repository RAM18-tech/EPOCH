package com.dtv.dcp.epoch.cache;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.common.redis.CacheHandlerFactory;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache1;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache2;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsCTCache3;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache1;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache2;
import com.dtv.dcp.epoch.common.redis.EpochOffersMsProductsCTCache3;
import com.dtv.dcp.epoch.common.redis.ICacheHandler;
import com.dtv.dcp.epoch.model.cache.CacheEvictionResponse;
import com.dtv.dcp.epoch.service.RedisCacheServiceImpl;

public class RedisCacheTest {
	

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
	public void testEpochOffersMsCTCache1() {		
		EpochOffersMsCTCache1 cpopOffMsCTCache1 = new EpochOffersMsCTCache1();
		cpopOffMsCTCache1.putCache("key1", "value1");
		Object cache1 = cpopOffMsCTCache1.getCache("key1");
		assertNull(cache1);
		cpopOffMsCTCache1.evictAllCacheValues();
		cpopOffMsCTCache1.evictCache("key1");
	}
	
	
	
	@Test
	public void testEpochOffersMsCTCache2() {		
		EpochOffersMsCTCache2 cpopOffMsCTCache2 = new EpochOffersMsCTCache2();
		cpopOffMsCTCache2.putCache("key2", "value2");
		Object cache2 = cpopOffMsCTCache2.getCache("key2");
		assertNull(cache2);
		cpopOffMsCTCache2.evictAllCacheValues();
		cpopOffMsCTCache2.evictCache("key2");
	}
	
	
	
	@Test
	public void testEpochOffersMsCTCache3() {		
		EpochOffersMsCTCache3 cpopOffMsCTCache3 = new EpochOffersMsCTCache3();
		cpopOffMsCTCache3.putCache("key3", "value3");
		Object cache3 = cpopOffMsCTCache3.getCache("key3");
		assertNull(cache3);
		cpopOffMsCTCache3.evictAllCacheValues();
		cpopOffMsCTCache3.evictCache("key3");
	}
	
	@Test
	public void testEpochOffersMsProductsCTCache1() {		
		EpochOffersMsProductsCTCache1 msProductsCTCache1 = new EpochOffersMsProductsCTCache1();
		msProductsCTCache1.putCache("key1", "value1");
		Object cache1 = msProductsCTCache1.getCache("key1");
		assertNull(cache1);
		msProductsCTCache1.evictAllCacheValues();
		msProductsCTCache1.evictCache("key1");
	}
	
	
	
	@Test
	public void testEpochOffersMsProductsCTCache2() {		
		EpochOffersMsProductsCTCache2 msProductsCTCache2 = new EpochOffersMsProductsCTCache2();
		msProductsCTCache2.putCache("key1", "value1");
		Object cache1 = msProductsCTCache2.getCache("key1");
		assertNull(cache1);
		msProductsCTCache2.evictAllCacheValues();
		msProductsCTCache2.evictCache("key1");
	}
	
	@Test
	public void testEpochOffersMsProductsCTCache3() {		
		EpochOffersMsProductsCTCache3 msProductsCTCache3 = new EpochOffersMsProductsCTCache3();
		msProductsCTCache3.putCache("key1", "value1");
		Object cache1 = msProductsCTCache3.getCache("key1");
		assertNull(cache1);
		msProductsCTCache3.evictAllCacheValues();
		msProductsCTCache3.evictCache("key1");
	}
	
	@Test
	public void testRedisCacheServiceImpl() {		
		RedisCacheServiceImpl redisCacheServiceImpl = new RedisCacheServiceImpl();
		CacheEvictionResponse evictCache1 = redisCacheServiceImpl.evictCache("epochOffersMsCTCache1");
		assertNotNull(evictCache1);
		CacheEvictionResponse evictCache2 = redisCacheServiceImpl.evictCache("epochOffersMsCTCache2");
		assertNotNull(evictCache2);
		CacheEvictionResponse evictCache3 = redisCacheServiceImpl.evictCache("epochOffersMsCTCache3");
		assertNotNull(evictCache3);
		CacheEvictionResponse evictCache4 = redisCacheServiceImpl.evictCache("epochOffersMsProductsCTCache1");
		assertNotNull(evictCache4);
		CacheEvictionResponse evictCache5 = redisCacheServiceImpl.evictCache("epochOffersMsProductsCTCache2");
		assertNotNull(evictCache5);
		CacheEvictionResponse evictCache6 = redisCacheServiceImpl.evictCache("epochOffersMsProductsCTCache3");
		assertNotNull(evictCache6);
		CacheEvictionResponse evictCacheAll = redisCacheServiceImpl.evictCache("all");
		assertNotNull(evictCacheAll);
		
	}

}
