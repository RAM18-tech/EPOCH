package com.dtv.dcp.epoch.integration.wirelessaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.wireless.WirelessResponse;

public class WirelessClientTest {

	RestTemplate restTemplate;
	RestTemplateBeanFactory restTemplateBeanFactory;

	/** The wireless client. */
	@InjectMocks
	private WirelessClient wirelessClient;

	@BeforeEach
	public void init() throws Exception {
		restTemplateBeanFactory = Mockito.mock(RestTemplateBeanFactory.class);
		restTemplate = Mockito.mock(RestTemplate.class);
		when(restTemplateBeanFactory.getObject(ArgumentMatchers.anyString())).thenReturn(restTemplate);
		MockitoAnnotations.openMocks(this);
		try {
			ReflectionTestUtils.setField(wirelessClient, "wirelessBaseURL", "http://localhost:80");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testAccountCall() throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("account", "accountNumber");
		when(restTemplate.exchange("http://localhost:80/details/{account}", HttpMethod.GET, null, String.class, params))
				.thenReturn(returnAccountData());
		WirelessResponse response = wirelessClient.getByAccountNumber("accountNumber");
		assertNotNull(response);

	}

	// @Test(expected = ClientException.class)
	public void testAccountCall_ClientException() throws ClientException {
		Map<String, Object> params = new HashMap<>();
		params.put("account", "accountNumber");
		when(restTemplate.exchange("http://localhost:80/details/{account}", HttpMethod.GET, null, String.class, params))
				.thenThrow(returnException());
		wirelessClient.getByAccountNumber("accountNumber");
	}

	public RestClientException returnException() {
		return new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "failure",
				"{\"error\":{\"errorId\":\"MSLGNWD001\",\"message\":\"Invalid Input, Account Number is Invalid\"}}"
						.getBytes(),
				null);
	}

	/**
	 * Return data.
	 *
	 * @return the response entity
	 */
	public ResponseEntity<String> returnData() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("MyResponseHeader", "MyValue");
		return new ResponseEntity<String>(
				"{\"content\":{\"currentPlanDetails\":[{\"accountNumber\":\"177066076012\",\"planCode\":\"SDGM20\",\"planCost\":20,\"netPlanCost\":20,\"netMonthlyPlanCost\":60,\"groupId\":\"G24115548\",\"autopayEnrolled\":false,\"paperlessEnrolled\":false,\"subscriberDetails\":[{\"subscriberNumber\":\"2142065066\",\"sdfCode\":\"SDFIPLCMH\",\"sdfCost\":10,\"primary\":true,\"netSdfCost\":10},{\"subscriberNumber\":\"2142065070\",\"sdfCode\":\"SDFIPLCMH\",\"sdfCost\":10,\"primary\":false,\"netSdfCost\":10}]}],\"offeredPlans\":[{\"planCode\":\"SDGUNPMM\",\"planCost\":110,\"netPlanCost\":70,\"netMonthlyPlanCost\":190,\"discountInfo\":[{\"type\":\"MSV\",\"amount\":20},{\"type\":\"ABP\",\"amount\":20}],\"subscriberDetails\":[{\"subscriberNumber\":\"2142065070\",\"sdfCode\":\"SDFIPLCMH\",\"sdfCost\":30,\"primary\":false,\"netSdfCost\":30},{\"subscriberNumber\":\"2142065066\",\"sdfCode\":\"SDFIPLCMH\",\"sdfCost\":30,\"primary\":true,\"netSdfCost\":30}]}]}}",
				responseHeaders, HttpStatus.OK);
	}

	public ResponseEntity<String> returnAccountData() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("MyResponseHeader", "MyValue");
		return new ResponseEntity<String>(
				"{\"content\":[{\"accountNumber\":\"177068234476\",\"status\":\"ACTIVE\",\"type\":\"I\",\"billingFirstName\":\"CSSDDATA\",\"billingLastName\":\"DONOTUSE\",\"subType\":\"R\",\"emailAddress\":\"PS522Y@ATT.COM\",\"bmgIndicator\":false,\"addresses\":[{\"type\":\"BILLING\",\"addressLine1\":\"1125 E CAMPBELL RD\",\"city\":\"RICHARDSON\",\"zipCode\":\"75081\",\"zipCodeExtention\":\"1934\",\"country\":\"USA\"}],\"subscribers\":[{\"subscriberNumber\":\"2147734250\",\"subscriberStatus\":\"ACTIVE\",\"primary\":true,\"groupType\":\"SHARED_PLAN\",\"groupId\":\"G23819296\",\"additionalOfferings\":[{\"code\":\"SDTRACK\",\"shareDataGrpInd\":\"P\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"SDGUNL1P\",\"shareDataGrpInd\":\"G\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"ALUCTS\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"DPTRACK5E\",\"shareDataGrpInd\":\"T\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"ENBUM\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"DSABR2\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"VOLTE\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"SDFIPLUP\",\"shareDataGrpInd\":\"P\",\"effectiveDate\":\"2018-01-09\"},{\"code\":\"DPTRACK2A\",\"shareDataGrpInd\":\"T\",\"effectiveDate\":\"2018-01-09\"}]}]}]}",
				responseHeaders, HttpStatus.OK);
	}

	@Test
	@Disabled
	public void testGetByAccountNumberHttpStatusCodeException() throws Exception {
		RestClientException re = new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "failure",
				"{\"error\":{\"errorId\":\"MSLGNWD001\",\"message\":\"Invalid Input, Input is Invalid\"}}".getBytes(),
				null);
		
		ServiceException ex = catchThrowableOfType(() -> wirelessClient.recoverGetByAccountNumber(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.ERROR_VALIDATION_FAILED));
	}

	@Test
	public void tesGetByAccountNumberHttpServerErrorException() throws Exception {
		HttpServerErrorException re = new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

		ServiceException ex = catchThrowableOfType(() -> wirelessClient.recoverGetByAccountNumber(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN));
	}

	@Test
	public void recoverGetByAccountNumber() throws Exception {
		RestClientException re = new RestClientException(HttpStatus.INTERNAL_SERVER_ERROR.toString());

		ServiceException ex = catchThrowableOfType(() -> wirelessClient.recoverGetByAccountNumber(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_WIRELESS_ERROR_UNKNOWN));
	}

}
