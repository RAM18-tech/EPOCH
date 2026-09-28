package com.dtv.dcp.epoch.model.common;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.model.common.request.OfferRequest;

public class OfferRequestTest {

	@Test
	public void testIsOfferTrayEnabled() {
		OfferRequest instance = new OfferRequest();
		Boolean expResult = true;
		instance.setOfferTrayEnabled(true);
		Boolean result = instance.isOfferTrayEnabled();
		assertEquals(expResult,result);
	}
	
	@Test
	public void testIsMigrationEnabled() {
		OfferRequest instance = new OfferRequest();
		Boolean expResult = true;
		instance.setIsMigrationRequired(true);
		Boolean result = instance.getIsMigrationRequired();
		assertEquals(expResult,result);
	}
}
