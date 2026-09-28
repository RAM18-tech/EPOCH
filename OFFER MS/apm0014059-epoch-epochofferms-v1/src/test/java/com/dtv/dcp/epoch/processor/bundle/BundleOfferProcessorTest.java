package com.dtv.dcp.epoch.processor.bundle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGServiceInfo;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BundleOfferProcessorTest {
	
	@InjectMocks
    BundleOfferProcessor BundleOfferProcessor;
	
	@Mock
	CpopClientHelper cpopClient;
	
	@Mock
	BundleShoppingCartProcessor bundleShoppingCartProcessor;
	
	@Mock
	CPOPAdditionalOfferHelper cpopAdditionalOfferHelper;
	
	@Mock
    FeatureManagerHelper featureHelper;
	
	@Mock
	CustomerGraphService customerGraphService;

	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	@Test
	public void testGetRewardOffers(){
		OfferRequest offerRequest = getOfferRequestObject();
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	
	@Test
	public void testGetQuotaBaseRewardOffersFromCache(){
		OfferRequest offerRequest = getOfferRequestObject();
		String sessionId = "12345";
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		quotabaseOffers.put("key", true);
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setSessionId(sessionId);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache(sessionId,"")).thenReturn(quotabaseOffers);
		when(cpopAdditionalOfferHelper.createQuotaBaseBenefitMap(Mockito.any())).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	
	@Test
	public void testGetQuotaBaseRewardOffers(){
		OfferRequest offerRequest = getOfferRequestObject();
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		quotabaseOffers.put("key", true);
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(cpopAdditionalOfferHelper.createQuotaBaseBenefitMap(Mockito.any())).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	
	@Test
	public void testOfferValidation(){
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		OfferValidationResponse offerValidationResponse = new OfferValidationResponse();
		when(bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, offerValidationResponse)).thenReturn(offerValidationResponse);
		BundleOfferProcessor.offerValidation(offerValidationRequest);
	}
	
	@Test
	public void testGetOffersFromCT(){
		OfferRequest offerRequest = getOfferRequestObject();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = null;
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		
		ServiceException ex = catchThrowableOfType(() -> BundleOfferProcessor.getBundleOffers(offerRequestWrapper),
				ServiceException.class);

		assertThat(ex.getError().getErrorId())
				.isEqualTo(ResourceManager.getIdentifier(ErrorMessages.CT_ERROR));
	}
	
	@Test
	public void testBundleProductOfferWithCustIntentWithCustContext(){
		OfferRequest offerRequest = getBundleOfferRequestObject(with_cust_intent_with_cust_context);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	@Test
	public void testBundleProductOfferWithCustIntentNoCustContext(){
		OfferRequest offerRequest = getBundleOfferRequestObject(with_cust_intent_no_cust_context);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	@Test
	public void testBundleProductOfferWithoutCustomerIntent(){
		OfferRequest offerRequest = getBundleOfferRequestObject(no_cust_intent_with_cust_context);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		CGResponse cgResponse = new CGResponse();
		when(customerGraphService.getActiveSubscriptions("190925162250755","")).thenReturn(cgResponse);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	@Test
	public void testBundleProductOfferWithNoCustomerIntentNoCustomerContext(){
		OfferRequest offerRequest = getBundleOfferRequestObject(no_cust_context_no_cust_intent);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setDtvnAccount("190925162250755");
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		CGResponse cgResponse = new CGResponse();
		List<CGServiceInfo> CGServiceInfoList = new ArrayList<>();
		CGServiceInfo cgServiceInfo = new CGServiceInfo();
		cgServiceInfo.setServiceID("12345");
		CGServiceInfoList.add(cgServiceInfo);
		CGServiceInfo[] cgServiceInfoArray = new CGServiceInfo[1];
		cgServiceInfoArray[0] = CGServiceInfoList.get(0);
		cgResponse.setServiceInfo(cgServiceInfoArray);
		when(customerGraphService.getActiveSubscriptions("190925162250755","")).thenReturn(cgResponse);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	
	@Test
	public void testBundleProductOfferWithWirelessAndIPTV(){
		OfferRequest offerRequest = getBundleOfferRequestObject(request3);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		CGResponse cgResponse = new CGResponse();
		when(customerGraphService.getActiveSubscriptions("190925162250755","")).thenReturn(cgResponse);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}
	private OfferRequest getOfferRequestObject(){
		OfferRequest offerRequest = new OfferRequest();
		List<String> offerActionType = new ArrayList<>();
		offerActionType.add("Acquisition");
		List<String> salesChannel = new ArrayList<>();
		salesChannel.add("Online");
		List<String> businessSegment = new ArrayList<>();
		List<String> customerSegments = new ArrayList<>();
		List<String> offerType = new ArrayList<>();
		List<String> offerProdFamily = new ArrayList<>();
		List<String> offerStatus = new ArrayList<>();
		List<String> offerProductTypes = new ArrayList<>();
		offerRequest.setOfferActionType(offerActionType);
		offerRequest.setSalesChannel(salesChannel);
		offerRequest.setBusinessSegment(businessSegment);
		offerRequest.setOfferTypes(offerType);
		offerRequest.setOfferProductFamily(offerProdFamily);
		offerRequest.setOfferStatus(offerStatus);
		offerRequest.setOfferProductType(offerProductTypes);
		CustomerEligibility customerEligibility = new CustomerEligibility();
		List<String> dmas = new ArrayList<>();
		dmas.add("500");
		List<String> zipcodes = new ArrayList<>();
		zipcodes.add("75038");
		customerEligibility.setDma(dmas);
		customerEligibility.setZipCode(zipcodes);
		offerRequest.setCustomerEligibility(customerEligibility);
		return offerRequest;
	}
	
	private OfferRequest getBundleOfferRequestObject(String jsonRequest){
		return JsonService.getObjectFromJson(jsonRequest, OfferRequest.class);
	}
	
	public static final String with_cust_intent_with_cust_context = "{\"pagination\":{\"page\":1,\"limit\":50},\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"businessSegment\":[\"CONS\"],\"offerProductTypes\":[\"internet-plan\"],\"offerProductFamily\":[\"broadband\"],\"customerSegments\":[\"Residential\"],\"customerEligibility\":{\"dma\":[\"500\"],\"city\":[\"plano\"],\"state\":[\"TX\"],\"zipCode\":[\"32957\"],\"county\":[\"collins\"],\"region\":[\"midwest\"]},\"customerContext\":{\"existingAccounts\":[\"OTT\"],\"OTT\":{\"accountNumber\":\"190925162250755\",\"products\":[{\"productId\":\"12345\",\"productCode\":\"max\",\"productType\":\"video-plan\"},{\"productId\":\"12346\",\"productCode\":\"hbo\",\"productType\":\"video-addon\"}]}},\"customerIntent\":[{\"productFamily\":\"OTT\",\"productStatus\":\"existing\"},{\"productFamily\":\"broadband\",\"productStatus\":\"new\"}]}";
	
	public static final String with_cust_intent_no_cust_context = "{\"pagination\":{\"page\":1,\"limit\":50},\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"businessSegment\":[\"CONS\"],\"offerProductTypes\":[\"internet-plan\"],\"offerProductFamily\":[\"broadband\"],\"customerSegments\":[\"Residential\"],\"customerEligibility\":{\"dma\":[\"500\"],\"city\":[\"plano\"],\"state\":[\"TX\"],\"zipCode\":[\"32957\"],\"county\":[\"collins\"],\"region\":[\"midwest\"]},\"customerContext\":{},\"customerIntent\":[{\"productFamily\":\"OTT\",\"productStatus\":\"existing\"},{\"productFamily\":\"broadband\",\"productStatus\":\"new\"}]}";
	
	public static final String no_cust_intent_with_cust_context = "{\"pagination\":{\"page\":1,\"limit\":50},\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"businessSegment\":[\"CONS\"],\"offerProductTypes\":[\"video-plan\",\"internet-plan\"],\"offerProductFamily\":[\"broadband\",\"satellite\"],\"customerSegments\":[\"Residential\"],\"customerEligibility\":{\"dma\":[\"500\"],\"city\":[\"plano\"],\"state\":[\"TX\"],\"zipCode\":[\"32957\"],\"county\":[\"collins\"],\"region\":[\"midwest\"]},\"customerContext\":{\"existingAccounts\":[\"OTT\"],\"OTT\":{\"accountNumber\":\"190925162250755\",\"products\":[{\"productId\":\"12345\",\"productCode\":\"max\",\"productType\":\"video-plan\"},{\"productId\":\"12346\",\"productCode\":\"hbo\",\"productType\":\"video-addon\"}]}},\"customerIntent\":[]}";
	
	public static final String no_cust_context_no_cust_intent = "{\"pagination\":{\"page\":1,\"limit\":50},\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"businessSegment\":[\"CONS\"],\"offerProductTypes\":[\"video-plan\",\"internet-plan\"],\"offerProductFamily\":[\"broadband\",\"satellite\"],\"customerSegments\":[\"Residential\"],\"customerEligibility\":{\"dma\":[\"500\"],\"city\":[\"plano\"],\"state\":[\"TX\"],\"zipCode\":[\"32957\"],\"county\":[\"collins\"],\"region\":[\"midwest\"]},\"customerContext\":{},\"customerIntent\":[]}";
	public static final String request3 = "{\"pagination\":{\"page\":1,\"limit\":50},\"offerActionType\":[\"Acquisition\"],\"salesChannel\":[\"online\"],\"businessSegment\":[\"CONS\"],\"offerProductTypes\":[\"video-plan\"],\"offerProductFamily\":[\"IPTV\"],\"customerSegments\":[\"Mobility\"],\"customerEligibility\":{\"dma\":[\"500\"],\"city\":[\"plano\"],\"state\":[\"TX\"],\"zipCode\":[\"32957\"],\"county\":[\"collins\"],\"region\":[\"midwest\"]},\"cartContext\":{},\"customerContext\":{\"existingAccounts\":[\"Wireless\"]},\"customerIntent\":[]}";
	
	@Test
	public void testCPOPAdditionalOfferHelper(){
		OfferRequest offerRequest = getBundleOfferRequestObject(request3);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		cpopAdditionalOfferHelper.isRewardCapabilityEnabled("online");
		assertNotNull(cpopAdditionalOfferHelper.isRewardCapabilityEnabled());	
		assertNotNull(cpopAdditionalOfferHelper.isRewardCapabilityEnabled());	
		
		assertNull(cpopAdditionalOfferHelper.getConfiguredEnvrironment());
		
		Map<String, Boolean> benefitsMap = null;
		
		assertNotNull(cpopAdditionalOfferHelper.putQuotaBaseOfferMapInCache("sessionId",	benefitsMap, "isCTBackUpenabled")); 
		assertNotNull(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("sessionId",	 "isCTBackUpenabled"));
		
		
		
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);
		when(cpopAdditionalOfferHelper.getQuotaBaseOfferMapFromCache("","")).thenReturn(quotabaseOffers);
		when(featureHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		CGResponse cgResponse = new CGResponse();
		when(customerGraphService.getActiveSubscriptions("190925162250755","")).thenReturn(cgResponse);
		BundleOfferProcessor.getBundleOffers(offerRequestWrapper);
	}   
	
	@Test
	public void testCreateQuotaBaseBenefitMap() throws JsonParseException, JsonMappingException, IOException {
		OfferRequest offerRequest = getBundleOfferRequestObject(request3);
		Map<String, Boolean> quotabaseOffers = new HashMap<>();
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		cpopAdditionalOfferHelper.isRewardCapabilityEnabled("online");
		ObjectMapper objectMapperRes = new ObjectMapper();
		ctOfferResponse =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
		when(cpopClient.getOffers(Mockito.any())).thenReturn(ctOfferResponse);

		Map<String, Boolean> benefitsMap = cpopAdditionalOfferHelper.createQuotaBaseBenefitMap(ctOfferResponse);

	}
	
	private static String OFFER_RESPONSE = "{\n" +
			// "    \"content\": {\n" +
			"        \"limit\": 20,\n" +
			"        \"offset\": 0,\n" +
			"        \"count\": 20,\n" +
			"        \"total\": 32,\n" +
			"        \"offers\": [\n" +
			"            {\n" +
			"                \"id\": \"477cdfef-2856-4557-bd61-be325a0288d8\",\n" +
			"                \"code\": \"Test-offer-mail\",\n" +
			"                \"status\": \"waiting_for_approval\",\n" +
			"                \"name\": {\n" +
			"                    \"en\": \"Test-offer-mail\"\n" +
			"                },\n" +
			"                \"description\": {\n" +
			"                    \"en\": \"Test-offer-mail\"\n" +
			"                },\n" +
			"                \"attributes\": {\n" +
			"                    \"offerType\": \"free-trial\",\n" +
			"                    \"offerActionType\": \"Retention\",\n" +
			"                    \"migratedOffer\": false,\n" +
			"                    \"benefits\": [],\n" +
			"                    \"leadOffer\": false,\n" +
			"                    \"primary\": true,\n" +
			"                    \"isVirtual\": false,\n" +
			"                    \"isPrimary\": true,\n" +
			"                    \"flashSaleOffer\": false\n" +
			"                }\n" +
			"            },\n" +
			"            {\n" +
			"                \"id\": \"9aa6db36-6395-459c-8f79-654bbb517ed9\",\n" +
			"                \"code\": \"CPOPPC5OFF1month\",\n" +
			"                \"status\": \"in_progress\",\n" +
			"                \"name\": {\n" +
			"                    \"en\": \"CPOP_PC_TEST_$5 Off for 1 month on Video package\"\n" +
			"                },\n" +
			"                \"description\": {\n" +
			"                    \"en\": \"Applicable to all AT&T TV customers. Customers must have a disconnect intent\\nOvers are available to all POD agents. This offer is given to customers based on the Loyalty score matrix for ATT TV\\n\"\n" +
			"                },\n" +
			"                \"startDate\": \"2019-10-21T07:00:00.000Z\",\n" +
			"                \"endDate\": \"2020-01-01T07:59:00.000Z\",\n" +
			"                \"attributes\": {\n" +
			"                    \"offerProductType\": \"video-plan\",\n" +
			"                    \"offerType\": \"flat-off\",\n" +
			"                    \"offerActionType\": \"Retention\",\n" +
			"                    \"rank\": \"2\",\n" +
			"                    \"displayName\": {\n" +
			"                        \"en\": \"$5 Off for 1 Month on video package\"\n" +
			"                    },\n" +
			"                    \"displayNamesByKey\": {},\n" +
			"                    \"descriptionsByKey\": {\n" +
			"                        \"att.com\": \"Get $5 off for 1 month on any AT&T TV video packages\"\n" +
			"                    },\n" +
			"                    \"migratedOffer\": false,\n" +
			"                    \"associatedProducts\": [\n" +
			"                        {\n" +
			"                            \"qualifyingProducts\": [\n" +
			"                                {\n" +
			"                                    \"constraints\": {\n" +
			"                                        \"productFamily\": \"OTT\",\n" +
			"                                        \"productStatus\": \"Active\",\n" +
			"                                        \"dependent\": false,\n" +
			"                                        \"anchor\": false\n" +
			"                                    },\n" +
			"                                    \"products\": []\n" +
			"                                }\n" +
			"                            ]\n" +
			"                        }\n" +
			"                    ],\n" +
			"                    \"benefits\": [],\n" +
			"                    \"eligibility\": {\n" +
			"                        \"constraints\": [\n" +
			"                            {\n" +
			"                                \"minimumPurchaseAmount\": 0,\n" +
			"                                \"salesChannel\": [\n" +
			"                                    \"online\"\n" +
			"                                ],\n" +
			"                                \"heartValue\": [\n" +
			"                                    \"TV 1\"\n" +
			"                                ],\n" +
			"                                \"existingCustomerType\": [\n" +
			"                                    \"OTT\"\n" +
			"                                ]\n" +
			"                            }\n" +
			"                        ]\n" +
			"                    },\n" +
			"                    \"leadOffer\": false,\n" +
			"                    \"contractIndicator\": \"non-contract\",\n" +
			"                    \"primary\": true,\n" +
			"                    \"isVirtual\": false,\n" +
			"                    \"isPrimary\": true,\n" +
			"                    \"flashSaleOffer\": false\n" +
			"                }\n" +
			"            }\n" +
			"            ]\n" +
			// "    }\n" +
			"}";
}
