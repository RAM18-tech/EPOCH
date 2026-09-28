package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthWirelessBackupTokenService;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;

public class CpopWirelessBackUpAuthRequestHeaderInterceptorTest {
	
	@InjectMocks
	private CpopWirelessBackUpAuthRequestHeaderInterceptor cpopWirelessBackUpAuthRequestHeaderInterceptor;
	
	@Mock
	private OAuthWirelessBackupTokenService oAuthTokenService;
	
	/**
	 * The headers.
	 */
	org.springframework.http.HttpHeaders headers;

	OfferRequest offerRequest;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
	}

	@Test
	public void testIntercept() throws Exception {
		HttpRequest request = Mockito.mock(HttpRequest.class);
		ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
		ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
		byte[] body = new byte[1];
		when(oAuthTokenService.getAccessToken()).thenReturn("Bearer 1234");
		when(request.getHeaders()).thenReturn(headers);
		when(headers.getFirst("idp-trace-id")).thenReturn("1234");
		when(execution.execute(request, body)).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
		ClientHttpResponse response1 = cpopWirelessBackUpAuthRequestHeaderInterceptor.intercept(request, body,
				execution);
		assertNotNull(response1);
	}

}
