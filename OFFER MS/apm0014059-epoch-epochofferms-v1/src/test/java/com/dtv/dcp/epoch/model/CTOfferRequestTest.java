package com.dtv.dcp.epoch.model;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;

public class CTOfferRequestTest {
	
	@Test
	public void testSetOfferTrayEnabled(){
		Boolean isOfferTrayEnabled = true;
		CTOfferRequest instance = new CTOfferRequest();
		instance.setOfferTrayEnabled(isOfferTrayEnabled);
		assertEquals(instance.isOfferTrayEnabled(), isOfferTrayEnabled);
	}
	
	@Test
	public void testIsOfferTrayEnabled() {
		CTOfferRequest instance = new CTOfferRequest();
		Boolean expResult = true;
		instance.setOfferTrayEnabled(true);
		Boolean result = instance.isOfferTrayEnabled();
		assertEquals(expResult,result);
	}
	
}
