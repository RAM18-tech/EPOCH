package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;

public class CpopWirelessBackUpBaseClientTest {
	
	@Mock
	private CpopWirelessBackUpBaseClient cpopWirelessBackUpBaseClient;
	
	@Mock
	private RestTemplate restTemplate;
	
	@Mock
	private CTOfferRequest ctOffersRequest;
	
	@Mock
	private CTProductRequest ctProductRequest;
	

	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		when(restTemplate.getInterceptors()).thenReturn(interceptors);
	}
	
	@Test
	public void testInit() throws Exception {
		cpopWirelessBackUpBaseClient.init();
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		assertNotNull(interceptors);

	}
	@Test
	public void testGetProducts() {
		CTProductResponse cTProductResponse = new CTProductResponse();
		when(cpopWirelessBackUpBaseClient.getProducts(ctProductRequest)).thenReturn(cTProductResponse);
		CTProductResponse response = cpopWirelessBackUpBaseClient.getProducts(ctProductRequest);
		assertEquals(cTProductResponse, response);
	}
	
	@Test
	public void testGetOffers() {
		CTOfferResponse cTOfferResponse = new CTOfferResponse();
		when(cpopWirelessBackUpBaseClient.getOffers(ctOffersRequest)).thenReturn(cTOfferResponse);
		CTOfferResponse response = cpopWirelessBackUpBaseClient.getOffers(ctOffersRequest);
		assertEquals(cTOfferResponse, response);
	}

}
