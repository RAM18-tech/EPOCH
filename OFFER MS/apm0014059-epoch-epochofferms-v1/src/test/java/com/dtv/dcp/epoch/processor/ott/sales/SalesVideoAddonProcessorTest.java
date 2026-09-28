package com.dtv.dcp.epoch.processor.ott.sales;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;


public class SalesVideoAddonProcessorTest {
	
//*//** The salesVideoAddonProcessor . *//*
	
//	@InjectMocks
//	private SalesVideoAddonProcessor salesVideoAddonProcessor;

	@Mock
	SalesVideoAddonProcessor salesVideoAddonProcessor;
	

//	*//**
//	 * Setup.
//	 *//*
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	@Test
	public void testGetOffers(){


		OfferRequestWrapper offers = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		List<String> actionType = new ArrayList<>();
		actionType.add("Acquisition");
		offerRequest.setOfferActionType(actionType);
		List<String> productType = new ArrayList<>();
		productType.add("video-plan");
		offerRequest.setOfferProductType(actionType);
		offers.setOfferRequest(offerRequest);
		
		salesVideoAddonProcessor.getVideoAddonOffers(offers);
		salesVideoAddonProcessor.getVideoAddonStandalonOffers(offers);
	}


}