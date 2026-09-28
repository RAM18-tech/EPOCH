package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

@Disabled
public class CpopWirelessBaseClientTest {
	
	@InjectMocks
	private CpopWirelessBaseClient cpopWirelessBaseClient;
	
	@Mock
	private ClientHttpRequestInterceptor cpopWirelessAuthRequestHeaderInterceptor;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	//write unit test cases for init method
		@Test
		public void testInit() throws Exception {
			//cpopWirelessBaseClient.init();
		}
	//write unit test cases for getOffers method
		//@Test
		@Disabled
		public void testGetOffers() {
			CTOfferRequest ctOffersRequest = new CTOfferRequest();
			CTOfferResponse cTOfferResponse = cpopWirelessBaseClient.getOffers(ctOffersRequest);
			assertNotNull(cTOfferResponse);
		}
		
	
	

}
