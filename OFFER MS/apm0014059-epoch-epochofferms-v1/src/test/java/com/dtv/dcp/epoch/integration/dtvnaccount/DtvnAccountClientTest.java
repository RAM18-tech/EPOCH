package com.dtv.dcp.epoch.integration.dtvnaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;


public class DtvnAccountClientTest {

	/** The dtvn account client. */
	@InjectMocks
	private DtvnAccountClient dtvnAccountClient;
	
	
	/** The rest template. */
	RestTemplate restTemplate;
	
	/** The rest template bean factory. */
	RestTemplateBeanFactory restTemplateBeanFactory;
	
	/**
	 * Inits the.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	public void init() throws Exception {	
		restTemplateBeanFactory=Mockito.mock(RestTemplateBeanFactory.class);
		restTemplate=Mockito.mock(RestTemplate.class);
		when(restTemplateBeanFactory.getObject(ArgumentMatchers.anyString())).thenReturn(restTemplate);
		MockitoAnnotations.openMocks(this);			
		try {
			ReflectionTestUtils.setField(dtvnAccountClient, "dtvnAccountBaseURL", "http://localhost:80");
		} catch (Exception e) {
		}
	}
	
	/**
	 * Test dtvn customer account status.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testDtvnCustomerAccountStatus() throws Exception {
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.<HttpEntity<?>>any(),
				ArgumentMatchers.<Class<String>>any())).thenReturn(returnDtvnAccountResponse());
		String customerId = "181015202639756";
		Boolean DtvnAccountResponse=dtvnAccountClient.getDtvnCustomerAccountStatus(customerId);
		assertNotNull(DtvnAccountResponse);
	}
	
	/**
	 * Test recover get dtvn customer account status.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRecoverGetDtvnCustomerAccountStatus() throws Exception {
		HttpServerErrorException re = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
		ServiceException ex = catchThrowableOfType(() -> dtvnAccountClient.recoverGetDtvnCustomerAccountStatus(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EXTERNAL_PROCESSING_ERROR));
	}
	
	/**
	 * Test recover get dtvn customer account status socket time out excep.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRecoverGetDtvnCustomerAccountStatusSocketTimeOutExcep() throws Exception {
		RestClientException re = new RestClientException(null);
		SocketTimeoutException exception = new SocketTimeoutException();
		re.initCause(exception);
		ServiceException ex = catchThrowableOfType(() -> dtvnAccountClient.recoverGetDtvnCustomerAccountStatus(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN));
	
	}

	/**
	 * Return dtvn account response.
	 *
	 * @return the response entity
	 */
	private ResponseEntity<String> returnDtvnAccountResponse() {
		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity<String> response = new ResponseEntity<String>(
				"{\"content\":{\"status\":\"success\",\"accountResponse\":{\"responseCode\":\"1\",\"message\":\"SUCCESS\",\"subscribable\":true,\"currentAccountStatus\":\"FREEVIEW\"}}}",
				responseHeaders, HttpStatus.OK);
		return response;
	}
}
