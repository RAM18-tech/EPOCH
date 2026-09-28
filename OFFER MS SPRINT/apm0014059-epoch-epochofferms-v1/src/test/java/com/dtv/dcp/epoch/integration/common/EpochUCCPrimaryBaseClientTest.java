package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertTrue;
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

import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;

public class EpochUCCPrimaryBaseClientTest {
	
	@Mock
	private EpochUCCPrimaryBaseClient epochUCCPrimaryBaseClient;
	
	@Mock
	private ClientHttpRequestInterceptor epochAuthDataloadRequestHeaderInterceptor;
	
	@Mock
	private CTBenefitsRequest ctBenefitsRequest;
	
	@Mock
	private CTOfferRequest ctOffersRequest;
	
	
	@Mock
	private RestTemplate restTemplate;

	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		when(restTemplate.getInterceptors()).thenReturn(interceptors);
	}
	
	@Test
	public void testInit() throws Exception {
		epochUCCPrimaryBaseClient.init();
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		interceptors.add(epochAuthDataloadRequestHeaderInterceptor);
		assertTrue(interceptors.contains(epochAuthDataloadRequestHeaderInterceptor));

	}
	
	@Test
	public void testGetBenefits() {
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		when(epochUCCPrimaryBaseClient.getBenefits(ctBenefitsRequest)).thenReturn(ctBenefitsResponse);
		CTBenefitsResponse response = epochUCCPrimaryBaseClient.getBenefits(ctBenefitsRequest);
		assertEquals(ctBenefitsResponse, response);
	}
	
	@Test
	public void testGetOffers() {
		CTOfferResponse ctBenefitsResponse = new CTOfferResponse();
		when(epochUCCPrimaryBaseClient.getOffers(ctOffersRequest)).thenReturn(ctBenefitsResponse);
		CTOfferResponse response = epochUCCPrimaryBaseClient.getOffers(ctOffersRequest);
		assertEquals(ctBenefitsResponse, response);
	}
	
}
