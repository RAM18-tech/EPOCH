package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

public class CustomerGraphBaseClientTest {
	@Mock
	private CustomerGraphBaseClient customerGraphBaseClient;

	@Mock
	private ClientHttpRequestInterceptor customerGraphAuthRequestHeaderInterceptor;

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
		customerGraphBaseClient.init();
		List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
		interceptors.add(customerGraphAuthRequestHeaderInterceptor);
		assertTrue(interceptors.contains(customerGraphAuthRequestHeaderInterceptor));
	}
	

	@Test
	public void testGetAccountById() {
		customerGraphBaseClient.getAccountById("accountId");
	}

	@Test
	public void testGetUverseAccountProducts() {
		customerGraphBaseClient.getUverseAccountProducts("customerId", "accountId", "accountType", true, Object.class);
	}

}
