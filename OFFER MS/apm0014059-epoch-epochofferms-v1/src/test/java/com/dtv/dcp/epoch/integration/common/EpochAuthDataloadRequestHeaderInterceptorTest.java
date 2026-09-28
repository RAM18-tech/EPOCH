package com.dtv.dcp.epoch.integration.common;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthDataloadTokenService;

public class EpochAuthDataloadRequestHeaderInterceptorTest {

	@InjectMocks
	private EpochAuthDataloadRequestHeaderInterceptor epochAuthDataloadRequestHeaderInterceptor;

	@Mock
	private OAuthDataloadTokenService oAuthDataloadTokenService;

	@Mock
	private HttpRequest request;

	@Mock
	private ClientHttpRequestExecution execution;

	@Mock
	private ClientHttpResponse response;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}


	@Test
	public void testIntercept() throws IOException {
		byte[] body = new byte[1];
		HttpHeaders headers = new HttpHeaders();
		when(oAuthDataloadTokenService.getAccessToken()).thenReturn("Bearer 1234");
		when(request.getHeaders()).thenReturn(headers);
		when(execution.execute(request, body)).thenReturn(response);
		epochAuthDataloadRequestHeaderInterceptor.intercept(request, body, execution);
		verify(oAuthDataloadTokenService, times(1)).getAccessToken();
		when(response.getStatusCode()).thenReturn(HttpStatus.UNAUTHORIZED);
		ClientHttpResponse response1=epochAuthDataloadRequestHeaderInterceptor.intercept(request, body, execution);
		assertNotNull(response1);
	}
}
