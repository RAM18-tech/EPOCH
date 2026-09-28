package com.dtv.dcp.epoch.integration.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.common.httpclient.config.RestTemplateBeanFactory;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopBackUpClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.ct.burn.CommerceBurnResponse;
import com.dtv.dcp.epoch.model.ct.burn.OfferBurnRequest;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTCartContext;
import com.dtv.dcp.epoch.model.ct.request.CTCouponsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CpopBackUpClientTest {

	@InjectMocks
	CpopBackUpClient cpopBackupClient;
	@Mock
	BaseRestClient baseRestClient;
	@Mock
	private FeatureManagerHelper featureManagerHelper;

	RestTemplate restCustomTemplate;
	RestTemplate restTemplate;
	RestTemplateBeanFactory restTemplateBeanFactory;

	@Value("${pageLimit}")
	private int pageLimit;

	@Mock
	ResponseEntity responseEntity;

	private static String PRODUCT_RESPONSE = "{\n" +
//			"    \"content\": {\n" +
			"        \"limit\": 50,\n" + "        \"offset\": 0,\n" + "        \"count\": 40,\n"
			+ "        \"total\": 40,\n" + "        \"products\": [\n" + "            {\n"
			+ "                \"id\": \"a5269dc4-b6f5-4ab1-a71f-badfdceca1d3\",\n"
			+ "                \"code\": \"VIDEOPLAN_TESTE2E\",\n" + "                \"productType\": {\n"
			+ "                    \"typeId\": \"product-type\",\n"
			+ "                    \"id\": \"eddcf245-874d-4cca-b069-8ada224b1d1a\",\n"
			+ "                    \"key\": \"video-plan\"\n" + "                },\n" + "                \"name\": {\n"
			+ "                    \"en\": \"VIDEOPLAN_TESTE2E\"\n" + "                },\n"
			+ "                \"description\": {\n" + "                    \"en\": \"VIDEOPLAN_TESTE2E\"\n"
			+ "                },\n" + "                \"categories\": [],\n" + "                \"variants\": [\n"
			+ "                    {\n" + "                        \"id\": \"1\",\n"
			+ "                        \"sku\": \"VIDEOPLAN_TESTE2E_SKU\",\n"
			+ "                        \"key\": \"VIDEOPLAN_TESTE2E-1\",\n" + "                        \"prices\": [\n"
			+ "                            {\n" + "                                \"value\": {\n"
			+ "                                    \"centAmount\": 250000,\n"
			+ "                                    \"currencyCode\": \"USD\",\n"
			+ "                                    \"percentage\": 0,\n"
			+ "                                    \"fractionDigits\": 2,\n"
			+ "                                    \"dollarAmount\": 2500\n" + "                                },\n"
			+ "                                \"id\": \"ecb370de-1eb0-4fb1-aa47-11c25c124df3\",\n"
			+ "                                \"startDate\": \"2019-09-20T00:00:00.000Z\",\n"
			+ "                                \"endDate\": \"9999-12-31T00:00:00.000Z\",\n"
			+ "                                \"recurrenceIndicator\": true,\n"
			+ "                                \"duration\": 0,\n"
			+ "                                \"period\": \"day\",\n"
			+ "                                \"proRateFrequency\": \"daily\",\n"
			+ "                                \"contractIndicator\": \"contract\",\n"
			+ "                                \"grandfathered\": false,\n"
			+ "                                \"contracted\": false\n" + "                            }\n"
			+ "                        ],\n" + "                        \"attributes\": {\n"
			+ "                            \"displayName\": {\n"
			+ "                                \"en\": \"VIDEOPLAN_TESTE2E\"\n" + "                            },\n"
			+ "                            \"descriptionsByKey\": {\n"
			+ "                                \"short-opus\": \"VIDEOPLAN_TESTE2E\"\n"
			+ "                            },\n"
			+ "                            \"billingProductCode\": \"VIDEOPLAN_TESTE2E\",\n"
			+ "                            \"billingProductId\": \"32996\",\n"
			+ "                            \"startDate\": \"2019-09-21T00:00:00.000Z\",\n"
			+ "                            \"endDate\": \"2020-09-26T00:00:00.000Z\",\n"
			+ "                            \"ranking\": 0,\n"
			+ "                            \"productFamily\": \"OTT\",\n"
			+ "                            \"capacityLimit\": 0,\n"
			+ "                            \"videoBasePackageRequired\": false,\n"
			+ "                            \"skipAdsIndicator\": false,\n"
			+ "                            \"serviceFlag\": false,\n"
			+ "                            \"concurrentStreamCount\": 0,\n"
			+ "                            \"preOrder\": false,\n"
			+ "                            \"thirdPartyOTT\": false,\n"
			+ "                            \"dependentPromoIsFreeTrail\": false,\n"
			+ "                            \"productStatus\": \"Active\",\n"
			+ "                            \"contractApplicable\": \"contract\",\n"
			+ "                            \"rsncapable\": false\n" + "                        }\n"
			+ "                    }\n" + "                ]\n" + "            }\n" + "            ]\n" +
			// " }\n" +
			"}";
	private static String BENEFIT_RESPONSE = "{\n" + "    \"limit\": 200,\n" + "    \"offset\": 0,\n"
			+ "    \"count\": 1,\n" + "    \"total\": 1,\n" + "    \"benefits\": [\n" + "        {\n"
			+ "            \"duration\": 25,\n" + "            \"billingBenefitCode\": \"BC_Cpop_percoff_1\",\n"
			+ "            \"promoSourceSkuProductType\": \"video-plan\",\n"
			+ "            \"beneficiaryProductFamily\": \"OTT\",\n" + "            \"billAfterPeriod\": true,\n"
			+ "            \"benefitType\": \"percent-off\",\n"
			+ "            \"promoSourceSkuProductFamily\": \"OTT\",\n"
			+ "            \"contractIndicator\": \"non-contract\",\n" + "            \"promoPriority\": 4,\n"
			+ "            \"benefitDisplayName\": {\n" + "                \"en\": \"BDNCpop_percoff_1\"\n"
			+ "            },\n" + "            \"beneficiaryProductType\": [\n" + "                \"video-plan\"\n"
			+ "            ],\n" + "            \"benefitLevel\": \"account\",\n"
			+ "            \"freeTrialPromo\": false,\n" + "            \"period\": \"NoOfDays\",\n"
			+ "            \"benefitCategory\": \"Product\",\n" + "            \"applicableProducts\": [\n"
			+ "                {\n" + "                    \"products\": [\n" + "                        {\n"
			+ "                            \"typeId\": \"product\",\n"
			+ "                            \"id\": \"30087085-93a0-472d-a3ff-1ab543c6c348\",\n"
			+ "                            \"key\": \"BASE-PLUS-2018\",\n"
			+ "                            \"productType\": \"video-plan\"\n" + "                        }\n"
			+ "                    ]\n" + "                }\n" + "            ],\n"
			+ "            \"id\": \"cc6b7b22-27b8-4863-b5d8-47a893fafd9a\",\n" + "            \"version\": 2,\n"
			+ "            \"code\": \"BC_Cpop_percoff_1\",\n" + "            \"benefitName\": {\n"
			+ "                \"en\": \"BN_Cpop_percoff_1\"\n" + "            },\n"
			+ "            \"description\": {\n" + "                \"en\": \"\"\n" + "            },\n"
			+ "            \"startDate\": \"2019-10-16T00:00:00.000Z\",\n"
			+ "            \"endDate\": \"2019-11-01T00:00:00.000Z\",\n" + "            \"value\": {\n"
			+ "                \"percentage\": 25\n" + "            },\n" + "            \"isQuotaBased\": false\n"
			+ "        }\n" + "    ]\n" + "}";

	/**
	 * Setup.
	 */
	@BeforeEach
	public void init() throws Exception {
		restTemplateBeanFactory = Mockito.mock(RestTemplateBeanFactory.class);
		restTemplate = Mockito.mock(RestTemplate.class);
		restCustomTemplate=Mockito.mock(RestTemplate.class);
		Mockito.when(restTemplateBeanFactory.getObject(ArgumentMatchers.anyString())).thenReturn(restTemplate);

		MockitoAnnotations.openMocks(this);
		// ReflectionTestUtils.setField(cpopBackupClient, "pageLimit", 300);
		try {
			// FieldUtils.writeField(cpopBackupClient, "cpcBaseURL", "http://localhost:80",
			// true);
			ReflectionTestUtils.setField(cpopBackupClient, "pageLimit", 10);
		} catch (Exception e) {
		}
	}

	@Test
	public void testGetProducts() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		CTProductRequest ctProductRequest = new CTProductRequest();
		CTProductResponse ctProductResponse = new CTProductResponse();
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnProductResponse());

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(returnProductResponse());
		
		Mockito.when(restCustomTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnProductResponse());

		Mockito.when(restCustomTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(returnProductResponse());
		
		cpopBackupClient.getProducts(ctProductRequest);

	}

	public ResponseEntity returnProductResponse() {

		ObjectMapper objectMapper = new ObjectMapper();
		CTProductResponse ctProductResponse = null;
		try {
			ctProductResponse = objectMapper.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
		} catch (Exception e) {

		}

		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<CTProductResponse>(ctProductResponse, responseHeaders,
				HttpStatus.OK);
		return response;
	}

	@Test
	public void testGetOffers() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnOfferResponse());
		Mockito.when(restCustomTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnOfferResponse());

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(returnOfferResponse());
		Mockito.when(restCustomTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
		.thenReturn(returnOfferResponse());
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
		cpopBackupClient.getOffers(ctOfferRequest);

	}

	public ResponseEntity returnOfferResponse() {

		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json", CTOfferResponse.class);

		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<CTOfferResponse>(cTOfferResponse, responseHeaders, HttpStatus.OK);
		return response;
	}

	@Test
	public void testGetBenefits() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
		ctBenefitsRequest.setState("stage");
		ctBenefitsRequest.setBenefitCodes(Arrays.asList("3243243432"));
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnBenefitsResponse());

		Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), Mockito.any()))
				.thenReturn(returnBenefitsResponse());
		cpopBackupClient.getBenefits(ctBenefitsRequest);

	}

	public ResponseEntity returnBenefitsResponse() {
		ObjectMapper objectMapper = new ObjectMapper();
		CTBenefitsResponse ctBenefitsResponse = new CTBenefitsResponse();
		JsonNode jsonNode = null;
		try {
			ctBenefitsResponse = objectMapper.readValue(BENEFIT_RESPONSE, CTBenefitsResponse.class);

			jsonNode = objectMapper.readTree(BENEFIT_RESPONSE);

		} catch (Exception e) {
			// do nothing
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<JsonNode>(jsonNode, responseHeaders, HttpStatus.OK);
		return response;
	}

	@Test
	public void testRecoverGetOffers() {
		RestClientException re = new RestClientException(null);
		SocketTimeoutException exception = new SocketTimeoutException();
		re.initCause(exception);
		ServiceException ex = catchThrowableOfType(() -> cpopBackupClient.recoverGetOffers(re), ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_TIMEOUT_ERROR_UNKNOWN));

	}

	@Test
	public void testRecoverGetOffersError() {
		RestClientException re = new RestClientException(null);
		NullPointerException exception = new NullPointerException();
		re.initCause(exception);

		ServiceException ex = catchThrowableOfType(() -> cpopBackupClient.recoverGetOffers(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CPOP_OFFER_EXTERNAL_PROCESSING_ERROR));
	}

	@Test
	public void testRecoverValidateShoppingCart() {
		RestClientException re = new RestClientException(null);
		SocketTimeoutException exception = new SocketTimeoutException();
		re.initCause(exception);
		ServiceException ex = catchThrowableOfType(() -> cpopBackupClient.recoverValidateShoppingCart(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CTLG_TIMEOUT_ERROR_UNKNOWN));

	}

	@Test
	public void testRecoverValidateShoppingCartError() {
		RestClientException re = new RestClientException(null);
		NullPointerException exception = new NullPointerException();
		re.initCause(exception);

		ServiceException ex = catchThrowableOfType(() -> cpopBackupClient.recoverValidateShoppingCart(re),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.EXTERNAL_PROCESSING_ERROR));
	}

	@Test
	public void testGetCoupons() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		CTCouponsRequest cTCouponsRequest = new CTCouponsRequest();
		cTCouponsRequest.setCode("TESTCODE");
		CTCouponResponse cTCouponResponse = new CTCouponResponse();
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnCouponResponse());

		Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), Mockito.any()))
				.thenReturn(returnBenefitsResponse());
		cpopBackupClient.getCoupons(cTCouponsRequest);

	}

	public ResponseEntity returnCouponResponse() {
		ObjectMapper objectMapper = new ObjectMapper();
		CTCouponResponse cTCouponResponse = new CTCouponResponse();

		String COUPON_RESPONSE = "{\"limit\":20,\"offset\":0,\"count\":1,\"total\":1,\"results\":[{\"id\":\"c638869c-fe9c-4767-b2fb-eadab71677f9\",\"version\":87,\"createdAt\":\"2020-05-27T17:29:54.843Z\",\"lastModifiedAt\":\"2020-06-03T17:28:20.848Z\",\"lastModifiedBy\":{\"clientId\":\"DFxSzyd8Gl3JDxetkUCSjodZ\",\"isPlatformClient\":false},\"createdBy\":{\"clientId\":\"DFxSzyd8Gl3JDxetkUCSjodZ\",\"isPlatformClient\":false},\"code\":\"TEST_CAM_DISCOUNT_002\",\"name\":{\"en\":\"TEST_CAM_DISCOUNT_002\"},\"description\":{\"en\":\"TEST_CAM_DISCOUNT_001\"},\"cartDiscounts\":[{\"typeId\":\"cart-discount\",\"id\":\"60f98a67-38eb-4446-a43a-8f9175f55f79\"}],\"isActive\":true,\"maxApplications\":2,\"maxApplicationsPerCustomer\":1,\"references\":[],\"attributeTypes\":{},\"cartFieldTypes\":{},\"lineItemFieldTypes\":{},\"customLineItemFieldTypes\":{},\"custom\":{\"type\":{\"typeId\":\"type\",\"id\":\"3b57d402-769f-47e4-867d-7b0e7c269cf8\"},\"fields\":{\"campaignCode\":\"CAMPAIGN_PROD\",\"couponStatus\":\"redeemed\"}},\"validFrom\":\"2020-05-05T00:00:00.000Z\",\"validUntil\":\"2020-05-31T00:00:00.000Z\",\"groups\":[]}]}";
		JsonNode jsonNode = null;
		try {
			cTCouponResponse = objectMapper.readValue("CTCouponResponse.json", CTCouponResponse.class);
			jsonNode = objectMapper.readTree(COUPON_RESPONSE);
		} catch (Exception e) {
		}

		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<JsonNode>(jsonNode, responseHeaders, HttpStatus.OK);
		return response;
	}

	@Test
	public void testGetOffersWithTwoParams() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		String ctOfferRequest = "{\"offerActionType\":[\"Retention\"],\"offerProductFamily\":[\"OTT\"],\"pagination\":{\"page\":1,\"limit\":300},\"state\":\"staged\",\"expandProductRefs\":true,\"retentionOfferUser\":[\"agent\"],\"isExpiredOffers\":false}";
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnOfferResponse());

		Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(returnOfferResponse());
		
		Mockito.when(restCustomTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
				ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
				.thenReturn(returnOfferResponse());

		Mockito.when(restCustomTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
				.thenReturn(returnOfferResponse());
		
		
		Mockito.when(featureManagerHelper.isEnabled(Mockito.anyString())).thenReturn(true);
        
		String idpConfigEnv = "TEST";
		cpopBackupClient.getOffers(ctOfferRequest, idpConfigEnv);

	}

	@Test
	public void testvalidateShoppingCart() {
		try {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
			CTCartContext ctCartContext = new CTCartContext();
			ctCartContext
					.setBillingProductCodes(Stream.of("OF_BASE-XTRA-201811_contract").collect(Collectors.toList()));
			ctShoppingCartRequest.setCartContext(ctCartContext);
			CTShoppingCartResponse cTShoppingCartResponse = new CTShoppingCartResponse();
			Boolean validateShoppingCartFlag = true;
			Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
					ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
					.thenReturn(returnValidateShoppingCartResponse());

			Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
					.thenReturn(returnValidateShoppingCartResponse());
			cpopBackupClient.validateShoppingCart(ctShoppingCartRequest, validateShoppingCartFlag);
		} catch (Exception err) {

		}
	}

	@Test
	public void testvalidateShoppingCartScenerio2() {
		try {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
			CTCartContext ctCartContext = new CTCartContext();
			ctCartContext
					.setBillingProductCodes(Stream.of("OF_BASE-XTRA-201811_contract").collect(Collectors.toList()));
			ctShoppingCartRequest.setCartContext(ctCartContext);
			Boolean validateShoppingCartFlag = false;
			Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
					ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
					.thenReturn(returnValidateShoppingCartResponse());

			Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
					.thenReturn(returnValidateShoppingCartResponse());
			cpopBackupClient.validateShoppingCart(ctShoppingCartRequest, validateShoppingCartFlag);
		} catch (Exception err) {

		}
	}

	public ResponseEntity returnValidateShoppingCartResponse() {

		CTShoppingCartResponse cTShoppingCartResponse = new DataReader().readFileToObj("CTShoppingCartResponse.json",
				CTShoppingCartResponse.class);

		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<CTShoppingCartResponse>(cTShoppingCartResponse, responseHeaders,
				HttpStatus.OK);
		return response;
	}

	@Test
	public void testBurnQuotaBasedPromotion() {
		try {
			SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
			OfferBurnRequest offerBurnRequest = new OfferBurnRequest();
			Mockito.when(restTemplate.exchange(ArgumentMatchers.anyString(), ArgumentMatchers.any(HttpMethod.class),
					ArgumentMatchers.<HttpEntity<?>>any(), ArgumentMatchers.<Class<String>>any()))
					.thenReturn(returnBurnResponse());

			Mockito.when(restTemplate.postForEntity(ArgumentMatchers.anyString(), Mockito.any(), Mockito.any()))
					.thenReturn(returnBurnResponse());
			cpopBackupClient.burnQuotaBasedPromotion(offerBurnRequest);
		} catch (Exception err) {

		}
	}

	public ResponseEntity returnBurnResponse() {

		CommerceBurnResponse commerceBurnResponse = new CommerceBurnResponse();
		HttpHeaders responseHeaders = new HttpHeaders();
		ResponseEntity response = new ResponseEntity<CommerceBurnResponse>(commerceBurnResponse, responseHeaders,
				HttpStatus.OK);
		return response;
	}

	@Test
	public void testGetProductsByType() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
		try {
			ReflectionTestUtils.setField(cpopBackupClient, "baseUrl", "https://url.att.com/");
		} catch (Exception e) {
		}
		CTProductResponse segmentProductResponse = new CTProductResponse();
		try {
			segmentProductResponse = new DataReader().readFileToObj("acquisition/segment_response.json",
					CTProductResponse.class);

		} catch (Exception e) {
			// do nothing
		}
		HttpHeaders responseHeaders = new HttpHeaders();
		Mockito.when(restTemplate.getForEntity(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
				.thenReturn(new ResponseEntity<>(segmentProductResponse, responseHeaders, HttpStatus.OK));
		CTProductResponse ctProductResponse = cpopBackupClient.getProductsByType(Constants.SEGMENT);
		assertNotNull(ctProductResponse);
	}
}
