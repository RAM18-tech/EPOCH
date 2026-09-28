package com.dtv.dcp.epoch.processor.ott.sales;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.integration.rsn.RSNClient;
import com.dtv.dcp.epoch.integration.rsn.RSNRequest;
import com.dtv.dcp.epoch.integration.rsn.RSNResponse;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.services.CustomerSubscriptionDetail;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.TestUtility;

public class SalesEquipmentProcessorTest {

	@InjectMocks
	private SalesEquipmentProcessor salesEquipmentProcessor;

	@Mock
    private OttCTOffersProcessor ottCTOffersProcessor;
	
	@Mock
	DtvnSalesRSNFeeOffersProcessor dtvnSalesRSNFeeOffersProcessor;
	
	@Mock
	RSNClient rsnClient;
	
	@Mock
    OffersUtils offersUtils;
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	@Test
	public void testGetOffers(){

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctOfferResponse).when(ottCTOffersProcessor).getEquipmentOffers(ArgumentMatchers.<OfferRequestWrapper>any(),any());
		ctOfferResponse = salesEquipmentProcessor.getEquipmentOffers(offerRequestWrapper);
		assertNotNull(ctOfferResponse);
	}

	@Test
	public void testGetFeeOffers() {
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest = new OfferRequest();
		offerRequestWrapper.setOfferRequest(offerRequest);
		RSNResponse rsnResponse = new RSNResponse();
		CTOfferResponse ctFeeOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/FeeCTOfferResponse.json"),CTOfferResponse.class);
		doReturn(ctFeeOfferResponse).when(ottCTOffersProcessor).getFeeOffers(ArgumentMatchers.<OfferRequestWrapper>any());
		doReturn(rsnResponse).when(rsnClient).getRSNInfo(ArgumentMatchers.<RSNRequest>any());
		salesEquipmentProcessor.getFeeOffers(offerRequestWrapper);
		//assertNotNull(ctOfferResponse);
	}
}