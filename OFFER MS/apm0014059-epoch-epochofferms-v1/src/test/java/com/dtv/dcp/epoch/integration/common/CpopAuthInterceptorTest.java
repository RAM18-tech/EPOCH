package com.dtv.dcp.epoch.integration.common;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;

import com.dtv.dcp.epoch.integration.common.oauthtoken.OAuthTokenServiceImpl;
import com.dtv.dcp.epoch.util.SystemPropertiesLoader;

public class CpopAuthInterceptorTest {

	@InjectMocks
	OAuthTokenServiceImpl oAuthTokenService;

	static {
		SystemPropertiesLoader.addSystemProperties();
	}

	@Value("${apiclient.rest.customergraphdtvnow.username}")
	private String userName;
	@Value("${apiclient.rest.customergraphdtvnow.password}")
	private String password;

	@BeforeEach
	public void init() throws Exception {
		MockitoAnnotations.openMocks(this);

	}

	ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);

	@Test
	public void testCustomerGraphAuthRequestHeaderInterceptor() {
		MockClientHttpRequest request = new MockClientHttpRequest();
		CustomerGraphAuthRequestHeaderInterceptor customerGraphAuthRequestHeader = new CustomerGraphAuthRequestHeaderInterceptor();
		try {
			customerGraphAuthRequestHeader.intercept(request, new byte[0], execution);
		} catch (Exception e) {
		}

	}

	@Test
	public void testCpopAuthRequestHeaderInterceptor() {
		MockClientHttpRequest request = new MockClientHttpRequest();
		CpopAuthRequestHeaderInterceptor cpopAuthRequestHeaderInterceptor = new CpopAuthRequestHeaderInterceptor();
		try {
			cpopAuthRequestHeaderInterceptor.intercept(request, new byte[0], execution);
		} catch (Exception e) {
		}

	}

	@Test
	public void testCpopAuthPVTRequestHeaderInterceptor() {
		MockClientHttpRequest request = new MockClientHttpRequest();
		CpopAuthPVTRequestHeaderInterceptor cpopAuthPVTRequestHeaderInterceptor = new CpopAuthPVTRequestHeaderInterceptor();
		try {
			cpopAuthPVTRequestHeaderInterceptor.intercept(request, new byte[0], execution);
		} catch (Exception e) {
		}

	}

	@Test
	public void testCpopAuthBackupRequestHeaderInterceptor() {
		MockClientHttpRequest request = new MockClientHttpRequest();
		CpopAuthBackupRequestHeaderInterceptor cpopAuthBackupRequestHeaderInterceptor = new CpopAuthBackupRequestHeaderInterceptor();
		try {
			cpopAuthBackupRequestHeaderInterceptor.intercept(request, new byte[0], execution);
		} catch (Exception e) {
		}

	}

	@Test
	public void testCpopWirelessAuthRequestHeaderInterceptor() {
		MockClientHttpRequest request = new MockClientHttpRequest();
		CpopWirelessAuthRequestHeaderInterceptor cpopWirelessAuthRequestHeaderInterceptor = new CpopWirelessAuthRequestHeaderInterceptor();
		try {
			cpopWirelessAuthRequestHeaderInterceptor.intercept(request, new byte[0], execution);
		} catch (Exception e) {
		}

	}

}
