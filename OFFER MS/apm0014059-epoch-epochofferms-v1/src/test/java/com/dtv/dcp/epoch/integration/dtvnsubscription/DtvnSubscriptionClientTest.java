/*
 * 
 */
package com.dtv.dcp.epoch.integration.dtvnsubscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;

import io.reactivex.Observable;

public class DtvnSubscriptionClientTest {

	
	/** The rest template. */
	RestTemplate restTemplate;
	
	/** The rest template bean factory. */
	RestTemplateBeanFactory restTemplateBeanFactory;

	/** The DTVNowClientTest client. */
	@InjectMocks
	private DtvnSubscriptionClient dtvnSubscriptionClient;
	
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
	}

	/**
	 * Test success send mobility details async.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSuccess_SendMobilityDetailsAsync() throws Exception {
		
		ResponseEntity<String> responseEntity = new ResponseEntity<String>("", HttpStatus.NO_CONTENT);
		when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class), ArgumentMatchers.<HttpEntity<?>>any(),
				ArgumentMatchers.<Class<String>>any())).thenReturn(responseEntity);
		Observable<ResponseEntity> resEntity = dtvnSubscriptionClient.postMobilityDetailsAsyn("534195801647","4703097240");
		assertNotNull(resEntity);
	}
	
	/**
	 * Test success send mobility details async.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSuccess_buildRequestWithWirelessAccountDetails() throws Exception {
		
		WirelessAccountDetails wirelessAccountDetails = dtvnSubscriptionClient.buildRequestWithWirelessAccountDetails("534195801647","4703097240");
		assertNotNull(wirelessAccountDetails);
	}
	/**
	 * Test recover service exception.
	 *
	 * @throws Exception the exception
	 */
	@Test
	@Disabled
	public void testRecover_ServiceException() throws Exception {
		RestClientException re = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "failure",
				"{\"error\":{\"errorId\":\"MSLGNWD001\",\"message\":\"Invalid Input, Input is Invalid\"}}".getBytes(),
				null);
		
		ServiceException ex = catchThrowableOfType(() -> dtvnSubscriptionClient.recoverSendMobilityDetails(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.ERROR_VALIDATION_FAILED));
	}
	
	/**
	 * Test recover service excep.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRecover_ServiceExcep() throws Exception {
		RestClientException re = new RestClientException(null);
		
		ServiceException ex = catchThrowableOfType(() -> dtvnSubscriptionClient.recoverSendMobilityDetails(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN));
	}
	
	/**
	 * Test recover serv exception.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRecover_ServException() throws Exception {
		RestClientException re = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "failure",
				"{\"error\":{\"errorId\":\"MSLGNWD001\",\"message\":\"Invalid Input, Input is Invalid\"}}".getBytes(),
				null);
		
		ServiceException ex = catchThrowableOfType(() -> dtvnSubscriptionClient.recoverSendMobilityDetails(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN));
	}
	
	@Test
	public void testWirelessAccountDetails()  {
		WirelessAccountDetails wirelessAccDetails = new WirelessAccountDetails();
		wirelessAccDetails.setAccountNumber("accountNumber");
		wirelessAccDetails.setAccountType("accountType");
		wirelessAccDetails.setSubscriberNumber("subscriberNumber");
		String num = wirelessAccDetails.getAccountNumber() ;
		String type = wirelessAccDetails.getAccountType() ;
		String subscriberNumber = wirelessAccDetails.getSubscriberNumber() ;
		
		String validate = num + ":" + type + ":" + subscriberNumber;
		assertNotNull(wirelessAccDetails.toString());
		assertNotNull(validate.toString());
		
	}
}
