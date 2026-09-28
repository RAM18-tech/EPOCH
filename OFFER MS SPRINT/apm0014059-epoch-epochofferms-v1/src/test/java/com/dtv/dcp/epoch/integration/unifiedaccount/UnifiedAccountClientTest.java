package com.dtv.dcp.epoch.integration.unifiedaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.model.unifiedaccount.UnifiedProductDetailsRequest;
import com.dtv.dcp.epoch.model.unifiedaccount.UnifiedProductDetailsResponse;
import com.dtv.dcp.epoch.util.JsonService;

public class UnifiedAccountClientTest {

	/** The rest template. */
	RestTemplate restTemplate;
	
	/** The rest template bean factory. */
	RestTemplateBeanFactory restTemplateBeanFactory;

	/** The UnifiedProductDetailsInfoClient. */
	@InjectMocks
	private UnifiedAccountClient unifiedProductDetailsInfoClient;

	/** The is local. */
	@Value("${genericLocalConfigEnabled:false}")
	private boolean isLocal = false;

	/** The mds base URL. */
	private String unifiedAccountProductDetailsInfoBaseURL = "https://zlt19578.vci.att.com:31808/msapi/unified/v1/productDetailsInfo";
	
	/**
	 * Inits the.
	 *
	 * @throws Exception the exception
	 */
	@BeforeEach
	public void init() throws Exception {	
		restTemplateBeanFactory=Mockito.mock(RestTemplateBeanFactory.class);
		restTemplate=Mockito.mock(RestTemplate.class);
		when(restTemplateBeanFactory.getObject(Mockito.anyString())).thenReturn(restTemplate);
		MockitoAnnotations.openMocks(this);	
		ReflectionTestUtils.setField(unifiedProductDetailsInfoClient, "unifiedAccountProductDetailsInfoBaseURL", unifiedAccountProductDetailsInfoBaseURL);
		ReflectionTestUtils.setField(unifiedProductDetailsInfoClient, "isLocal", true);
	}
	
	/**
	 * Test success.
	 * @throws ClientException 
	 */
	@Test
	public void testSuccess() throws ClientException {
		UnifiedProductDetailsRequest unifiedProductDetailsRequest = JsonService.getObjectFromJson(UNIFIED_ACCT_PRODUCT_DETAILS_REQUEST, UnifiedProductDetailsRequest.class);
		HttpEntity<UnifiedProductDetailsRequest> request = new HttpEntity<>(unifiedProductDetailsRequest);
		ResponseEntity<String> unifiedAccountProductDetailsInfoResponse = new ResponseEntity<String>(UNIFIED_ACCT_PRODUCT_DETAILS_RESPONSE, null, HttpStatus.OK);
		when(restTemplate.exchange(unifiedAccountProductDetailsInfoBaseURL, HttpMethod.POST, request, String.class)).thenReturn(unifiedAccountProductDetailsInfoResponse);
		Object response = unifiedProductDetailsInfoClient.getProductDetailsInfoByBAN(unifiedProductDetailsRequest);
		assertNotNull(response);
		assertEquals(response instanceof UnifiedProductDetailsResponse, Boolean.TRUE);
	}
	
	@Test
	public void testException() throws Exception {
		UnifiedProductDetailsRequest unifiedProductDetailsRequest = JsonService
				.getObjectFromJson(UNIFIED_ACCT_PRODUCT_DETAILS_REQUEST, UnifiedProductDetailsRequest.class);
		HttpEntity<UnifiedProductDetailsRequest> request = new HttpEntity<>(unifiedProductDetailsRequest);
		when(restTemplate.exchange(unifiedAccountProductDetailsInfoBaseURL, HttpMethod.POST, request, String.class))
				.thenThrow(new NullPointerException());
		NullPointerException ex = catchThrowableOfType(
				() -> unifiedProductDetailsInfoClient.getProductDetailsInfoByBAN(unifiedProductDetailsRequest),
				NullPointerException.class);
		assertThat(ex.getMessage()).isEqualTo(null);
	}
	
	@Test
	public void testClientException() throws Exception {
		UnifiedProductDetailsRequest unifiedProductDetailsRequest = JsonService.getObjectFromJson(UNIFIED_ACCT_PRODUCT_DETAILS_REQUEST, UnifiedProductDetailsRequest.class);
		HttpEntity<UnifiedProductDetailsRequest> request = new HttpEntity<>(unifiedProductDetailsRequest);
		when(restTemplate.exchange(unifiedAccountProductDetailsInfoBaseURL, HttpMethod.POST, request, String.class)).thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
		HttpServerErrorException ex = catchThrowableOfType(
				() -> unifiedProductDetailsInfoClient.getProductDetailsInfoByBAN(unifiedProductDetailsRequest),
				HttpServerErrorException.class);
		assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	public static final String UNIFIED_ACCT_PRODUCT_DETAILS_REQUEST ="{\"unifiedBans\":[\"625011713\"],\"productStatuses\":[\"AC\"],\"productStates\":[\"AS\"]}";
	public static final String UNIFIED_ACCT_PRODUCT_DETAILS_RESPONSE = "{\"content\":{\"serviceInfo\":{\"625011713\":{\"iptv\":{\"compCode\":\"VideoBasic\",\"status\":\"Assigned\",\"state\":\"Active\",\"basePackageName\":\"U200\",\"numberOfSetTopBox\":1,\"programingChannels\":[],\"pricePlanData\":[{\"primary\":true,\"pricePlanCode\":\"31931\",\"actualPrice\":0,\"name\":\"ATT U-verse TV\",\"promotionDescription\":\"AT&T U-verse TV Installation\",\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\"},{\"primary\":true,\"pricePlanCode\":\"31931\",\"actualPrice\":92,\"name\":\"ATT U-verse TV\",\"promotionDescription\":\"AT&T U-verse TV\",\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\"}],\"receiverInfoList\":{\"wifiDvrList\":[],\"regularDvrList\":[{\"equipmentModel\":\"VIP1216\",\"equipmentDescription\":\"U-verse TV Receiver with DVR\",\"manufacturer\":\"Motorola\"}],\"xboxList\":[],\"wifiStbList\":[],\"stbList\":[]}},\"hsia\":{\"compCode\":\"HSIA\",\"status\":\"Assigned\",\"state\":\"Active\",\"speed\":\"Hsia50x10\",\"premiumTiers\":\"NP\",\"originalInstallationType\":\"Tech\",\"numberOfWifiNextGenExtenders\":0,\"pricePlanData\":[{\"primary\":true,\"pricePlanCode\":\"88541733\",\"actualPrice\":60,\"name\":\"Broadband EDSP2\",\"promotionDescription\":\"AT&T Internet\",\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\"},{\"primary\":false,\"pricePlanCode\":\"88548223\",\"actualPrice\":0,\"name\":\"Internet Usage Allowance - Cap H\",\"promotionDescription\":\"Internet Usage Allowance 1TB\",\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\"},{\"primary\":false,\"pricePlanCode\":\"88551633\",\"actualPrice\":0,\"name\":\"IPUB Bolt On Unlimited IPTVHSI SI\",\"promotionDescription\":\"U-verse TV and Internet Unlimited Usage Plan\",\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\"}],\"boltOnDetails\":{\"boltOnIndicator\":true,\"boltOnList\":[{\"name\":\"IPUB Bolt On Unlimited IPTVHSI SI\",\"type\":\"SYSTEM\",\"dataAmount\":\"Unlimited\"}],\"availableBoltOnOption\":\"NONE\",\"boltOnUnlimited\":true},\"allowanceInformation\":{\"baseAllowanceInformation\":\"1024GB\",\"bucketAllowanceInformation\":\"50GB\",\"pricePerBucketAllowanceInfo\":\"$10\",\"unlimitedData\":false}},\"currentPromotions\":[{\"name\":\"IPUB Bolt On Unlimited IPTVHSI SI\",\"description\":\"U-verse TV and Internet Unlimited Usage Plan\",\"pricePlanCode\":\"88551633\",\"promoCrossPkgInd\":false,\"effectiveDate\":\"2019-01-08T00:00:00.000+0000\",\"pricePlanType\":\"AD\",\"applicableService\":\"HSIA\"}]}}}}";
}
