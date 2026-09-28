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
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;

public class EpochDataloadBaseClientTest {
	
	@Mock
	private EpochDataloadBaseClient epochDataloadBaseClient;
	
	@Mock
	private ClientHttpRequestInterceptor epochAuthDataloadRequestHeaderInterceptor;
	
	@Mock
	private CTBenefitsRequest ctBenefitsRequest;
	
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
		epochDataloadBaseClient.init();
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		interceptors.add(epochAuthDataloadRequestHeaderInterceptor);
		assertTrue(interceptors.contains(epochAuthDataloadRequestHeaderInterceptor));
	}
	
	@Test
	public void testGetSatelliteBenefits() {
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		when(epochDataloadBaseClient.getSatelliteBenefits(ctBenefitsRequest)).thenReturn(ctBenefitsResponse);
		CTBenefitsResponse response = epochDataloadBaseClient.getSatelliteBenefits(ctBenefitsRequest);
		assertEquals(ctBenefitsResponse, response);
	}

}
