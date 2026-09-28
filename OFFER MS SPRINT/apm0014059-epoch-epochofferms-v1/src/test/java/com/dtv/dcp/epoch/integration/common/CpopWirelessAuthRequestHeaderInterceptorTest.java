package com.dtv.dcp.epoch.integration.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.UriInfo;

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

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthWirelessTokenService;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;

public class CpopWirelessAuthRequestHeaderInterceptorTest {
	
	@InjectMocks
	private CpopWirelessAuthRequestHeaderInterceptor wirelessInterceptor;
	
	@Mock
	private OAuthWirelessTokenService oAuthTokenService;


	/**
	 * The headers.
	 */
	org.springframework.http.HttpHeaders headers;

	OfferRequest offerRequest;

	/**
	 * The uri info.
	 */
	@Mock
	UriInfo mUriInfo;

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		headers = Mockito.mock(HttpHeaders.class);
	}

	@Test
	public void testIntercept() throws Exception {
		HttpRequest request = Mockito.mock(HttpRequest.class);
		byte[] body = new byte[1];
		ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);
		ClientHttpResponse response = Mockito.mock(ClientHttpResponse.class);
		when(oAuthTokenService.getAccessToken()).thenReturn("token");
		when(request.getHeaders()).thenReturn(headers);
		when(headers.getFirst("idp-trace-id")).thenReturn("idp-trace-id");
		when(execution.execute(request, body)).thenReturn(response);
		when(response.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
		response = wirelessInterceptor.intercept(request, body, execution);
		assertNotNull(response);
	}
	
}
