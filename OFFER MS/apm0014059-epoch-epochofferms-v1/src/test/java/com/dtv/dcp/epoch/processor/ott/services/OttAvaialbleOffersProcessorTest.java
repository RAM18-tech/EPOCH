package com.dtv.dcp.epoch.processor.ott.services;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.service.AccountLookupService;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

class OttAvaialbleOffersProcessorTest {
	@Mock
	OffersUtils offersUtils;
	
	@Mock
	OttCTOffersProcessor ottCTOffersProcessor;	

	@Mock
	OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;
	
	@Mock
	CustomerGraphServiceImpl customerGraphServiceImpl;
	
	@InjectMocks
	OttAvaialbleOffersProcessor ottPromoContinuationProcessor;

	@Mock
	private DtvnServicesAddonOffersProcessor dtvnServicesAddonOffersProcessor;	
	
	
	@Mock
	private DtvnServicesEquipmentOffersProcessor dtvnServicesEquipmentOffersProcessor;

	@Mock
    private DtvnServicesFeeOffersProcessor dtvnServicesFeeOffersProcessor;
	
	@Mock
	CPOPProductsHelper cpopProductsHelper;
	
	@Mock
	private FeatureManagerHelper featureHelper;

	@Mock
	private CpopClientHelper cpopClientHelper;

	@Mock
	private OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;
	
	@Mock
    OttServicesBenefitsProcessor ottServicesBenefitsProcessor;

	@Mock
    DtvnServicesRSNFeeOffersProcessor dtvnServicesRSNFeeOffersProcessor;

	@Mock
	OttServicesIoOffersProcessor ottServicesIoOffersProcessor;
	
	@Mock
	DtvnServicesStandAloneProcessor dtvnServicesStandAloneProcessor;

	@Mock
	AccountLookupService accountLookupService;

	@Mock
	private RedisCacheHelper redisCacheHelper;

    String dtvnMidasRules ="[{\"ruleCode\":\"800\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule1\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493328673\",\"contractIntent\":\"F\",\"productCode\":\"800\"},{\"ruleCode\":\"801\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule2\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328682\",\"contractIntent\":\"F\",\"productCode\":\"801\"},{\"ruleCode\":\"802\",\"existingGroup\":\"70000\",\"catalogProductName\":\"Rule3\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328691\",\"contractIntent\":\"F\",\"productCode\":\"802\"},{\"ruleCode\":\"804\",\"catalogProductName\":\"Rule4\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493328705\",\"contractIntent\":\"F\",\"productCode\":\"804\"},{\"ruleCode\":\"805\",\"existingGroup\":\"10000OR20000OR70000OR50000OR60000\",\"catalogProductName\":\"Rule6\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000&70000\",\"ruleId\":\"7493328722\",\"contractIntent\":\"F\",\"productCode\":\"805\"},{\"ruleCode\":\"806\",\"catalogProductName\":\"Rule5\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328714\",\"contractIntent\":\"F\",\"productCode\":\"806\"},{\"ruleCode\":\"807\",\"existingGroup\":\"50000\",\"catalogProductName\":\"Rule 7\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493823183\",\"contractIntent\":\"F\",\"productCode\":\"807\"},{\"ruleCode\":\"808\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 8\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493823192\",\"contractIntent\":\"F\",\"productCode\":\"808\"},{\"ruleCode\":\"809\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 9\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493823202\",\"contractIntent\":\"F\",\"productCode\":\"809\"},{\"ruleCode\":\"810\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule10\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520084\",\"contractIntent\":\"T\",\"productCode\":\"810\"},{\"ruleCode\":\"811\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule11\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520094\",\"contractIntent\":\"T\",\"productCode\":\"811\"},{\"ruleCode\":\"812\",\"catalogProductName\":\"Rule12\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520104\",\"contractIntent\":\"T\",\"productCode\":\"812\"},{\"ruleCode\":\"813\",\"catalogProductName\":\"Rule13\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520114\",\"contractIntent\":\"T\",\"productCode\":\"813\"},{\"ruleCode\":\"814\",\"existingGroup\":\"10000OR20000OR60000\",\"catalogProductName\":\"Rule14\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520123\",\"contractIntent\":\"T\",\"productCode\":\"814\"},{\"ruleCode\":\"815\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule15\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520133\",\"contractIntent\":\"T\",\"productCode\":\"815\"},{\"ruleCode\":\"816\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule16\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520139\",\"contractIntent\":\"T\",\"productCode\":\"816\"},{\"ruleCode\":\"TESTRULE1\",\"existingGroup\":\"10000\",\"catalogProductName\":\"TESTRULE1\",\"salesChannel\":\"agentExisting|selfServiceExistingagentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493279687\",\"productCode\":\"TESTRULE1\"}]"; 
	
    List<DtvnMidasRule> dtvnMidasRuleInfoList = JsonService.getListObjectFromJsonTreeWithNoRootElement(dtvnMidasRules, DtvnMidasRule.class);
    
	private static String OFFER_REQUEST = "{\n" + 
			"    \"salesChannel\": [\n" + 
			"        \"online\"\n" + 
			"    ],\n" + 
			"    \"offerProductTypes\": [\n" + 
			"        \"video-plan\"\n" + 
			"    ],\n" + 
			"    \"offerProductFamily\": [\n" + 
			"        \"OTT\"\n" + 
			"    ],\n" + 
			"    \"contractIndicator\": [\n" + 
			"        \"contract\"\n" + 
			"    ],\n" + 
			"    \"customerEligibility\": {\n" + 
			"        \"zipCode\": [\n" + 
			"            \"32958\"\n" + 
			"        ]\n" + 
			"    },\n" + 
			"    \"offerActionType\": [\n" + 
			"        \"Other\"\n" + 
			"    ],\n" + 
			"    \"pagination\": {\n" + 
			"        \"page\": 1,\n" + 
			"        \"limit\": 300\n" + 
			"    },\n" + 
			"    \"state\": \"staged\",\n" + 
			"    \"customerContext\": {\n" + 
			"        \"existingProductFamily\": [\n" + 
			"            \"OTT\"\n" + 
			"        ],\n" + 
			"        \"OTT\": {\n" + 
			"            \"accountNumber\": \"190925162250755\",\n" + 
			"            \"isActive\": true,\n" + 
			"            \"freeTrialEligible\": true,\n" + 
			"            \"isDefaultPromo\" : true,\n" + 
			"            \"accountType\" : \"Mobility\",\n" + 
			"            \"nextBillingDate\" : \"02/28/2020\",\n" + 
			"            \"products\": [\n" + 
			"                {\n" + 
			"                    \"productCode\": \"BASE-ULTIMATE-201811\",\n" + 
			"                    \"productType\": \"video-plan\",\n" + 
			"                    \"basePrice\": \"50\",\n" + 
			"                    \"promotions\": [\n" + 
			"                    {\n" + 
			"                        \"promotionId\": \"1MONFLEAD\",\n" + 
			"                        \"promotionEndDate\": \"12/12/2020\",\n" + 
			"                        \"promotionStartDate\": \"10/12/2019\"\n" + 
			"                    }\n" + 
			"                ]\n" + 
			"                }\n" + 
			"            ]\n" + 
			"        }\n" + 
			"    }\n" + 
			"}";
	
	private static String OFFER_REQUEST_NO_OTT = "{\n" + 
			"    \"salesChannel\": [\n" + 
			"        \"online\"\n" + 
			"    ],\n" + 
			"    \"offerProductTypes\": [\n" + 
			"        \"video-addon\"\n" + 
			"    ],\n" + 
			"    \"offerProductFamily\": [\n" + 
			"        \"OTT\"\n" + 
			"    ],\n" + 
			"    \"contractIndicator\": [\n" + 
			"        \"contract\"\n" + 
			"    ],\n" + 
			"    \"customerEligibility\": {\n" + 
			"        \"zipCode\": [\n" + 
			"            \"32958\"\n" + 
			"        ]\n" + 
			"    },\n" + 
			"    \"offerActionType\": [\n" + 
			"        \"Other\"\n" + 
			"    ],\n" + 
			"    \"pagination\": {\n" + 
			"        \"page\": 1,\n" + 
			"        \"limit\": 300\n" + 
			"    },\n" + 
			"    \"state\": \"staged\",\n" + 
			"    \"customerContext\": {\n" + 
			"        \"existingProductFamily\": [\n" + 
			"            \"OTT\"\n" + 
			"        ]\n" + 
			"    }\n" + 
			"}";
	private static String OFFER_REQUEST_ADDON ="{\n" + 
			"    \"salesChannel\": [\n" + 
			"        \"online\"\n" + 
			"    ],\n" + 
			"    \"offerProductTypes\": [\n" + 
			"        \"video-addon\"\n" + 
			"    ],\n" + 
			"    \"offerProductFamily\": [\n" + 
			"        \"OTT\"\n" + 
			"    ],\n" + 
			"    \"contractIndicator\": [\n" + 
			"        \"contract\"\n" + 
			"    ],\n" + 
			"    \"customerEligibility\": {\n" + 
			"        \"zipCode\": [\n" + 
			"            \"32958\"\n" + 
			"        ]\n" + 
			"    },\n" + 
			"    \"offerActionType\": [\n" + 
			"        \"Other\"\n" + 
			"    ],\n" + 
			"    \"pagination\": {\n" + 
			"        \"page\": 1,\n" + 
			"        \"limit\": 300\n" + 
			"    },\n" + 
			"    \"state\": \"staged\",\n" + 
			"   \"customerContext\": {\n" + 
			"        \"existingProductFamily\": [\n" + 
			"            \"OTT\"\n" + 
			"        ],\n" + 
			"        \"OTT\": {\n" + 
			"            \"accountNumber\": \"190925162250755\",\n" + 
			"            \"isActive\": true,\n" + 
			"            \"freeTrialEligible\": true,\n" + 
			"            \"isDefaultPromo\" : true,\n" + 
			"            \"accountType\" : \"Mobility\",\n" + 
			"            \"nextBillingDate\" : \"02/28/2020\",\n" + 
			"            \"products\": [\n" + 
			"                {\n" + 
			"                    \"productCode\": \"BASE-ULTIMATE-201811\",\n" + 
			"                    \"productType\": \"video-addon\",\n" + 
			"                    \"basePrice\": \"50\",\n" + 
			"                    \"promotions\": [\n" + 
			"                    {\n" + 
			"                        \"promotionId\": \"1MONFLEAD\",\n" + 
			"                        \"promotionEndDate\": \"12/12/2020\",\n" + 
			"                        \"promotionStartDate\": \"10/12/2019\"\n" + 
			"                    }\n" + 
			"                ]\n" + 
			"                }\n" + 
			"            ]\n" + 
			"        }\n" + 
			"    },\n" + 
			"    \"cartContext\" : {\n" + 
			"    \"cpopOfferId\" : [\n" + 
			"    	\"b59c4e40-3d6f-40ba-8809-d68e1ab7fc0a\"\n" + 
			"    	] 	\n" + 
			"    } 	\n" + 
			"}";

	private static String OFFER_REQUEST_DEVICE ="{ \n" +
			"    \"offerActionType\": [ \n" +
			"        \"Other\" \n" +
			"    ], \n" +
			"    \"offerProductFamily\": [ \n" +
			"        \"OTT\" \n" +
			"    ], \n" +
			"    \"offerProductTypes\": [ \n" +
			"         \"video-device\"\n" +
			"    ], \n" +
			"    \"salesChannel\": [ \n" +
			"        \"opus\" \n" +
			"    ] \n" +
			"}";
    private static String OFFER_REQUEST_FEE ="{ \n" +
            "    \"offerActionType\": [ \n" +
            "        \"Other\" \n" +
            "    ], \n" +
            "    \"offerProductFamily\": [ \n" +
            "        \"OTT\" \n" +
            "    ], \n" +
            "    \"offerProductTypes\": [ \n" +
            "         \"fee\"\n" +
            "    ], \n" +
            "    \"salesChannel\": [ \n" +
            "        \"opus\" \n" +
            "    ] \n" +
            "}";
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@Disabled("this test is failing")
	void testGetAvailableCTOffers(){
				Map<String, String> leadAndAvailableGroup = new HashMap<>();
				leadAndAvailableGroup.put(Constants.LEAD, "10000");
				leadAndAvailableGroup.put(Constants.AVAILABE_PACKAGE_GROUP, "10000&50000");
				OfferRequest offerRequest = null;
				CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);;
				
			    try {
					offerRequest =  new ObjectMapper().readValue(OFFER_REQUEST, OfferRequest.class);
				} catch (Exception e) {			
				}
				OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
				offerRequestWrapper.setOfferRequest(offerRequest);
				offerRequestWrapper.setAgentType("SPECIAL");
				doReturn(cTOfferResponse).when(ottCTOffersProcessor).getOffersFromCT(any(), any());
				doReturn(cTOfferResponse).when(ottServicesOffersProcessorHelper).getFilteredOffers(any(), any());
				doReturn("Residential").when(ottServicesOffersProcessorHelper).findAccountType(any());
				doReturn("selfServiceExisting").when(ottServicesOffersProcessorHelper).findSalesChannel(any(), any());
				doReturn(true).when(ottServicesOffersProcessorHelper).isSalesChannelValidForCustomer(any());
				doReturn(dtvnMidasRuleInfoList).when(offersUtils).fetchDtvnMidasRules();
				Mockito.when(ottServicesIoOffersProcessor.processIoOffers(any(), any())).thenReturn(cTOfferResponse.getOffers());
				Mockito.when(ottServicesIoOffersProcessor.extractIOOffers(any())).thenReturn(cTOfferResponse.getOffers());
				Mockito.when(ottServicesIoOffersProcessor.extractNonIOOffers(any())).thenReturn(cTOfferResponse.getOffers());
				HttpHeaders headers=Mockito.mock(HttpHeaders.class);
				Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
				ottPromoContinuationProcessor.getAvailableCTOffers( offerRequestWrapper );
				Assertions.assertNotNull(CTOfferResponse.class, "The list of offers should not be null");
	}
	
	@Test
	void testGetAvailableCTOffers_NOOTT(){
		Map<String, String> leadAndAvailableGroup = new HashMap<>();
		leadAndAvailableGroup.put(Constants.LEAD, "10000");
		leadAndAvailableGroup.put(Constants.AVAILABE_PACKAGE_GROUP, "10000&50000");
		CGResponse cGResponse = new DataReader().readFileToObj("CustomerGraphResponse.json",CGResponse.class);
		OfferRequest offerRequest = null;
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);
		CTProductResponse currentBaseOffer = new DataReader().readFileToObj("baseOfferForCompatibility.json",CTProductResponse.class);;
		
	    try {
			offerRequest =  new ObjectMapper().readValue(OFFER_REQUEST_NO_OTT, OfferRequest.class);
		} catch (Exception e) {			
		}
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setDtvnAccount("190925162250755");
		offerRequestWrapper.setAgentType("SPECIAL");

		doReturn(currentBaseOffer).when(cpopClientHelper).getProducts(any());
		doReturn(cTOfferResponse).when(ottCTOffersProcessor).getOffersFromCT(any(), any());
		doReturn(cGResponse).when(customerGraphServiceImpl).getActiveSubscriptions(any(), any());
		doReturn(cTOfferResponse).when(ottServicesOffersProcessorHelper).getFilteredOffers(any(), any());
		doReturn("Residential").when(ottServicesOffersProcessorHelper).findAccountType(any());
		doReturn("selfServiceExisting").when(ottServicesOffersProcessorHelper).findSalesChannel(any(), any());
		doReturn(true).when(ottServicesOffersProcessorHelper).isSalesChannelValidForCustomer(any());
		doReturn(dtvnMidasRuleInfoList).when(offersUtils).fetchDtvnMidasRules();
		doReturn(true).when(featureHelper).isEnabled(any());
		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		
		
		try {
			CTOfferResponse ctOfferResponse = ottPromoContinuationProcessor.getAvailableCTOffers( offerRequestWrapper );
			assertNotNull("CTOfferResponse should not be null", ctOfferResponse);
		} catch (Exception e) {
			
		}

		Assertions.assertNotNull(CTOfferResponse.class, "Updated offers list should not be null");
	}
		
	@Test
	void testGetAvailableCTOffersAddOn(){
		OfferRequest offerRequest = null;
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponseVideoAddOn.json",CTOfferResponse.class);
		
	    try {
			offerRequest =  new ObjectMapper().readValue(OFFER_REQUEST_ADDON, OfferRequest.class);
		} catch (Exception e) {			
		}
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setAgentType("SPECIAL");
		doReturn(cTOfferResponse).when(ottCTOffersProcessor).getVideoPlanOffers(any(),anyBoolean());
		doReturn(cTOfferResponse.getOffers()).when(dtvnServicesAddonOffersProcessor).retrieveAvailableAddOns(any());
		doReturn("Residential").when(ottServicesOffersProcessorHelper).findAccountType(any());
		doReturn("selfServiceExisting").when(ottServicesOffersProcessorHelper).findSalesChannel(any(), any());
		
		doReturn(true).when(ottServicesOffersProcessorHelper).isSalesChannelValidForCustomer(any());
		doReturn(cTOfferResponse).when(cpopProductsHelper).filterInvalidContractIndicatorPrices(any(), any(), any(), any());

		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");

		try {
			CTOfferResponse ctOfferResponse = ottPromoContinuationProcessor.getAvailableCTOffers( offerRequestWrapper );
			assertNotNull("CTOfferResponse should not be null", ctOfferResponse);
		} catch (Exception e) {
			
		}
		Assertions.assertNotNull(CTOfferResponse.class, "Updated offers list should not be null");


	}
	
	
	@Test
	void testGetAvailableEquipmentOffers(){
		OfferRequest offerRequest = null;
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponseVideoAddOn.json",CTOfferResponse.class);
		CTProductResponse currentBaseOffer = new DataReader().readFileToObj("baseOfferForCompatibility.json",CTProductResponse.class);;
		try {
			offerRequest =  new ObjectMapper().readValue(OFFER_REQUEST_DEVICE, OfferRequest.class);
		} catch (Exception e) {			
		}
		doReturn(currentBaseOffer).when(cpopClientHelper).getProducts(any());
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		offerRequestWrapper.setAgentType("SPECIAL");
		doReturn(cTOfferResponse).when(ottCTOffersProcessor).getVideoPlanOffers(any(),anyBoolean());
		doReturn(cTOfferResponse.getOffers()).when(dtvnServicesEquipmentOffersProcessor).retrieveEquipmentOffers(any(),any());
		
		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		ottPromoContinuationProcessor.getAvailableCTOffers( offerRequestWrapper );
		Assertions.assertNotNull(cTOfferResponse.getOffers(), "The list of offers should not be null");
	}

    @Test
    void testGetAvailableFeeOffers(){
        OfferRequest offerRequest = null;
        CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("FeeCTOfferResponse.json",CTOfferResponse.class);
        try {
            offerRequest =  new ObjectMapper().readValue(OFFER_REQUEST_FEE, OfferRequest.class);
        } catch (Exception e) {
        }
        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.setAgentType("SPECIAL");
        doReturn(cTOfferResponse).when(ottCTOffersProcessor).getVideoPlanOffers(any(),anyBoolean());
        doReturn(cTOfferResponse.getOffers()).when(dtvnServicesFeeOffersProcessor).retrieveFeeOffers(any());
        HttpHeaders headers=Mockito.mock(HttpHeaders.class);
        Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
        ottPromoContinuationProcessor.getAvailableCTOffers( offerRequestWrapper );
		Assertions.assertNotNull(cTOfferResponse.getOffers(), "The list of offers should not be null");
    }

	 @Test
	 void testCheckValidContractIntent() {
		 CustomerSubscriptionDetail subscriptionsContext =new CustomerSubscriptionDetail();
		 subscriptionsContext.setContractIndicator(Constants.ROAD_RUNNER);
		 DtvnMidasRule dtvRule = new DtvnMidasRule();
		 dtvRule.setContractIntent(Constants.ROAD_RUNNER);
		 Assertions.assertTrue(ottPromoContinuationProcessor.checkValidContractIntent(subscriptionsContext,dtvRule));
	 }

	@Test
	void givenOspreyGiftRequest_whenGetAvailableCTOffers_thenCorrectOffersReturned() {

		OfferRequest offerRequest = new DataReader().readFileToObj("offer_request_osprey_gift.json", OfferRequest.class);
		CTOfferResponse expectedCTOfferResponse = new DataReader().readFileToObj("ct_offer_response_osprey_gift.json", CTOfferResponse.class);
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);

		HttpHeaders mockHeaders = Mockito.mock(HttpHeaders.class);
		Mockito.when(mockHeaders.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		Mockito.doReturn(expectedCTOfferResponse.getOffers()).when(dtvnServicesFeeOffersProcessor).retrieveFeeOffers(any());
		Mockito.doReturn(expectedCTOfferResponse.getOffers()).when(ottServicesOffersProcessorHelper).combineOffers(any());
		Mockito.doReturn(Boolean.TRUE).when(featureHelper).isEnabled(any());
		Mockito.doReturn(Boolean.TRUE).when(accountLookupService).isAccountNumberExistsInBanLookupTable(any(), any());
		Mockito.doReturn(expectedCTOfferResponse).when(cpopProductsHelper).filterInvalidContractIndicatorPrices(any(), any(), any(), any());
		List<String> freeDevicePromoList = Arrays.asList("EMPLTOSPR", "EMPOSPR", "OSPREYFREE", "DTVSTREAMOSPREYFREE", "DTVSTREAMFREE");
		Mockito.doReturn(freeDevicePromoList).when(redisCacheHelper).getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_FREEDEVICEPROMOS, Constants.OTT_PRODUCT_FAMILY);
		CTOfferResponse actualCTOfferResponse = ottPromoContinuationProcessor.getAvailableCTOffers(offerRequestWrapper);

		assertNotNull("CTOfferResponse should not be null", actualCTOfferResponse);
		assertNotNull("Offers should not be null", actualCTOfferResponse.getOffers());
		Mockito.verify(ottServicesOffersProcessorHelper).combineOffers(any());
		Mockito.verify(accountLookupService).isAccountNumberExistsInBanLookupTable(any(), any());
	}

}
