package com.dtv.dcp.epoch.processor.ott.sales;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;

public class SalesVideoPlanProcessorTest {

	@InjectMocks
	private SalesVideoPlanProcessor salesVideoPlanProcessor;

	@Mock
	OttSalesMobilityProcessorHelper ottSalesMobilityProcessorHelper;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}

	@Test
	public void testGetVideoPlanOffers() {

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();

		ottSalesMobilityProcessorHelper.getFilteredOffers(offerRequestWrapper, "offerType");
	}
}
