package com.dtv.dcp.epoch.common;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;

public class CacheConstantsTest {

	@Test
	public void testCacheConstantsAttributes() throws Exception {

		CacheConstants cacheConstants = new CacheConstants();

		assertNotNull(cacheConstants.APPLICATION_ID);
		assertNotNull(Constants.CATLG_EXT_ERROR_50001);

	}

}
