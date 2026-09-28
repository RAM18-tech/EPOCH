package com.dtv.dcp.epoch.processor.ott.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.model.common.DtvnMidasRule;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttLeadProductsProcessorTest {
	
	
	@InjectMocks
	OttLeadProductsProcessor ottLeadProductsProcessor;

	 @Mock
	 OffersUtils offersUtils;
	    
    @Mock
    OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;
	    
    String dtvnMidasRules ="[{\"ruleCode\":\"800\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule1\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493328673\",\"contractIntent\":\"F\",\"productCode\":\"800\"},{\"ruleCode\":\"801\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule2\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328682\",\"contractIntent\":\"F\",\"productCode\":\"801\"},{\"ruleCode\":\"802\",\"existingGroup\":\"70000\",\"catalogProductName\":\"Rule3\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328691\",\"contractIntent\":\"F\",\"productCode\":\"802\"},{\"ruleCode\":\"804\",\"catalogProductName\":\"Rule4\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493328705\",\"contractIntent\":\"F\",\"productCode\":\"804\"},{\"ruleCode\":\"805\",\"existingGroup\":\"10000OR20000OR70000OR50000OR60000\",\"catalogProductName\":\"Rule6\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000&70000\",\"ruleId\":\"7493328722\",\"contractIntent\":\"F\",\"productCode\":\"805\"},{\"ruleCode\":\"806\",\"catalogProductName\":\"Rule5\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493328714\",\"contractIntent\":\"F\",\"productCode\":\"806\"},{\"ruleCode\":\"807\",\"existingGroup\":\"50000\",\"catalogProductName\":\"Rule 7\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493823183\",\"contractIntent\":\"F\",\"productCode\":\"807\"},{\"ruleCode\":\"808\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 8\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7493823192\",\"contractIntent\":\"F\",\"productCode\":\"808\"},{\"ruleCode\":\"809\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 9\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493823202\",\"contractIntent\":\"F\",\"productCode\":\"809\"},{\"ruleCode\":\"810\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule10\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520084\",\"contractIntent\":\"T\",\"productCode\":\"810\"},{\"ruleCode\":\"811\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule11\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520094\",\"contractIntent\":\"T\",\"productCode\":\"811\"},{\"ruleCode\":\"812\",\"catalogProductName\":\"Rule12\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520104\",\"contractIntent\":\"T\",\"productCode\":\"812\"},{\"ruleCode\":\"813\",\"catalogProductName\":\"Rule13\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520114\",\"contractIntent\":\"T\",\"productCode\":\"813\"},{\"ruleCode\":\"814\",\"existingGroup\":\"10000OR20000OR60000\",\"catalogProductName\":\"Rule14\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520123\",\"contractIntent\":\"T\",\"productCode\":\"814\"},{\"ruleCode\":\"815\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule15\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7494520133\",\"contractIntent\":\"T\",\"productCode\":\"815\"},{\"ruleCode\":\"816\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule16\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7494520139\",\"contractIntent\":\"T\",\"productCode\":\"816\"},{\"ruleCode\":\"TESTRULE1\",\"existingGroup\":\"10000\",\"catalogProductName\":\"TESTRULE1\",\"salesChannel\":\"agentExisting|selfServiceExistingagentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000\",\"ruleId\":\"7493279687\",\"productCode\":\"TESTRULE1\"}]"; 
	
    List<DtvnMidasRule> dtvnMidasRuleInfoList = JsonService.getListObjectFromJsonTreeWithNoRootElement(dtvnMidasRules, DtvnMidasRule.class);
    

	private static String PRODUCT_REQUEST = "{ \n" +
			"    \"offerIds\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			"    \"offerCodes\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			"    \"offerActionType\": [ \n" +
			"        \"Acquisition\" \n" +
			"    ], \n" +
			"    \"salesChannel\": [ \n" +
			"        \"opus\", \n" +
			"        \"online\" \n" +
			"    ], \n" +
			"    \"heartValue\": \"3\", \n" +
			"    \"accountTypes\": [ \n" +
			"        \"CONS\" \n" +
			"    ], \n" +
			"    \"addOnType\": [ \n" +
			"        \"bolt-on\", \n" +
			"        \"programming-bolt-on\", \n" +
			"        \"standalone\" \n" +
			"    ], \n" +
			"    \"customerSegments\": [ \n" +
			"        \"Employee\", \n" +
			"        \"Residential\" \n" +
			"    ], \n" +
			"    \"offerProductTypes\": [ \n" +
			"        \"base\", \n" +
			"        \"video-plan\" \n" +
			"    ], \n" +
			"    \"offerTypes\": [ \n" +
			"        \"free-trial\", \n" +
			"        \"x-off\", \n" +
			"        \"percent-off\", \n" +
			"        \"prepay\", \n" +
			"        \"free-promo\", \n" +
			"        \"multi-benefit\" \n" +
			"    ], \n" +
			"    \"productFamily\": [ \n" +
			"        \"OTT\", \n" +
			"        \"Wireless\", \n" +
			"        \"IPTV\", \n" +
			"        \"BB\", \n" +
			"        \"VOIP\" \n" +
			"    ], \n" +
			"    \"offerStatus\": [ \n" +
			"        \"Active\", \n" +
			"        \"Grandfathered\", \n" +
			"        \"Closed\" \n" +
			"    ], \n" +
			"    \"bundleProductCodes\": [ \n" +
			"        \"abc-123\", \n" +
			"        \"def-456\" \n" +
			"    ], \n" +
			"    \"bundleProductIds\": [ \n" +
			"        \"def-456\" \n" +
			"    ], \n" +
			"    \"contractApplicable\": [ \n" +
			"        \"contract\", \n" +
			"        \"non-contract\" \n" +
			"    ], \n" +
			"    \"benefitCodes\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			"    \"customerEligibility\": { \n" +
			"        \"state\": [ \n" +
			"            \"TX\" \n" +
			"        ], \n" +
			"        \"dma\": [ \n" +
			"            \"TX\" \n" +
			"        ], \n" +
			"        \"county\": [ \n" +
			"            \"plano\" \n" +
			"        ], \n" +
			"        \"city\": [ \n" +
			"            \"plano\" \n" +
			"        ], \n" +
			"        \"zipCode\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"zipCode_4\": [ \n" +
			"            \"75202-4206\" \n" +
			"        ], \n" +
			"        \"customerCallIntent\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"custClassification\": [ \n" +
			"            \"existingCustomer\", \n" +
			"            \"newCustomer\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"channelEligibility\": { \n" +
			"        \"opusGroup\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"opusChannels\": \"Retail\", \n" +
			"        \"opusSubChannels\": \"Retail\", \n" +
			"        \"regions\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"market\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"opusState\": [ \n" +
			"            \"TX,\" \n" +
			"        ], \n" +
			"        \"storeIds\": \"string\", \n" +
			"        \"dealerIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"callCenterIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"kioskIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"repProfile\": [ \n" +
			"            \"manager,\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"cartContext\": { \n" +
			"        \"cpopProductIds\": [ \n" +
			"            \"12245abcd\" \n" +
			"        ], \n" +
			"        \"cpopOfferId\": [ \n" +
			"            \"455445abcd\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"userType\": \"agent\", \n" +
			"    \"account\": { \n" +
			"        \"accountType\": \"nonmobility\", \n" +
			"        \"contractIndicator\": true, \n" +
			"        \"contractVersion\": \"v1\", \n" +
			"        \"zipCode\": \"32957\" \n" +
			"    } \n" +
			"}";

	private static String PRODUCT_REQUEST_CUSTOMERCONTEXT = "{ \n" +
			"    \"offerIds\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			"    \"offerCodes\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			"    \"offerActionType\": [ \n" +
			"        \"Acquisition\" \n" +
			"    ], \n" +
			"    \"salesChannel\": [ \n" +
			"        \"online\" \n" +
			"    ], \n" +
			"    \"heartValue\": \"3\", \n" +
			"    \"accountTypes\": [ \n" +
			"        \"CONS\" \n" +
			"    ], \n" +
			"    \"addOnType\": [ \n" +
			"        \"bolt-on\", \n" +
			"        \"programming-bolt-on\", \n" +
			"        \"standalone\" \n" +
			"    ], \n" +
			"    \"customerSegments\": [ \n" +
			"        \"Employee\", \n" +
			"        \"Residential\" \n" +
			"    ], \n" +
			"    \"offerProductTypes\": [ \n" +
			"        \"base\", \n" +
			"        \"video-plan\" \n" +
			"    ], \n" +
			"    \"offerTypes\": [ \n" +
			"        \"free-trial\", \n" +
			"        \"x-off\", \n" +
			"        \"percent-off\", \n" +
			"        \"prepay\", \n" +
			"        \"free-promo\", \n" +
			"        \"multi-benefit\" \n" +
			"    ], \n" +
			"    \"productFamily\": [ \n" +
			"        \"OTT\", \n" +
			"        \"Wireless\", \n" +
			"        \"IPTV\", \n" +
			"        \"BB\", \n" +
			"        \"VOIP\" \n" +
			"    ], \n" +
			"    \"offerStatus\": [ \n" +
			"        \"Active\", \n" +
			"        \"Grandfathered\", \n" +
			"        \"Closed\" \n" +
			"    ], \n" +
			"    \"bundleProductCodes\": [ \n" +
			"        \"abc-123\", \n" +
			"        \"def-456\" \n" +
			"    ], \n" +
			"    \"bundleProductIds\": [ \n" +
			"        \"def-456\" \n" +
			"    ], \n" +
			"    \"contractApplicable\": [ \n" +
			"        \"contract\", \n" +
			"        \"non-contract\" \n" +
			"    ], \n" +
			"    \"benefitCodes\": [ \n" +
			"        \"string\" \n" +
			"    ], \n" +
			" \"customerContext\": {\n" +
			"    \"existingProductFamily\": [\n" +
			"      \"wireless\",\n" +
			"      \"IPTV\",\n" +
			"      \"satellite\",\n" +
			"      \"OTT\"\n" +
			"    ],\n" +
			"    \"IPTV\": {\n" +
			"      \"accountNumber\": \"9999\",\n" +
			"      \"isActive\":true,\n" +
			"      \"sunsetDate\": \"02012020\",\n" +
			"      \"products\": [\n" +
			"        {\n" +
			"          \"productCode\": \"u300\",\n" +
			"          \"productType\": \"video-plan\"\n" +
			"        }\n" +
			"      ]\n" +
			"    },\n" +
			"    \"satellite\": {\n" +
			"      \"accountNumber\": \"9999\",\n" +
			"      \"isActive\":true\n" +
			"    },\n" +
			"    \"wireless\": {\n" +
			"      \"accountNumber\": \"987665\",\n" +
			"      \"isActive\":true\n" +
			"\n" +
			"    },\n" +
			"    \"OTT\": {\n" +
			"      \"accountNumber\": \"9999\",\n" +
			"      \"isActive\":true,\n" +
			"      \"sunsetDate\": \"02012020\",\n" +
			"      \"products\": [\n" +
			"        {\n" +
			"          \"productCode\": \"u300\",\n" +
			"          \"productType\": \"video-plan\"\n" +
			"        }\n" +
			"      ]\n" +
			"    }\n" +
			"  },"+
			"    \"customerEligibility\": { \n" +
			"        \"state\": [ \n" +
			"            \"TX\" \n" +
			"        ], \n" +
			"        \"dma\": [ \n" +
			"            \"TX\" \n" +
			"        ], \n" +
			"        \"county\": [ \n" +
			"            \"plano\" \n" +
			"        ], \n" +
			"        \"city\": [ \n" +
			"            \"plano\" \n" +
			"        ], \n" +
			"        \"zipCode\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"zipCode_4\": [ \n" +
			"            \"75202-4206\" \n" +
			"        ], \n" +
			"        \"customerCallIntent\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"custClassification\": [ \n" +
			"            \"existingCustomer\", \n" +
			"            \"newCustomer\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"channelEligibility\": { \n" +
			"        \"opusGroup\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"opusChannels\": \"Retail\", \n" +
			"        \"opusSubChannels\": \"Retail\", \n" +
			"        \"regions\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"market\": [ \n" +
			"            \"Retail,\" \n" +
			"        ], \n" +
			"        \"opusState\": [ \n" +
			"            \"TX,\" \n" +
			"        ], \n" +
			"        \"storeIds\": \"string\", \n" +
			"        \"dealerIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"callCenterIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"kioskIds\": [ \n" +
			"            \"string\" \n" +
			"        ], \n" +
			"        \"repProfile\": [ \n" +
			"            \"manager,\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"cartContext\": { \n" +
			"        \"cpopProductIds\": [ \n" +
			"            \"12245abcd\" \n" +
			"        ], \n" +
			"        \"cpopOfferId\": [ \n" +
			"            \"455445abcd\" \n" +
			"        ] \n" +
			"    }, \n" +
			"    \"userType\": \"agent\", \n" +
			"    \"account\": { \n" +
			"        \"accountType\": \"nonmobility\", \n" +
			"        \"contractIndicator\": true, \n" +
			"        \"contractVersion\": \"v1\", \n" +
			"        \"zipCode\": \"32957\" \n" +
			"    } \n" +
			"}";
	private static String PRODUCT_RESPONSE = "{\n" +
//			"    \"content\": {\n" +
			"        \"limit\": 50,\n" +
			"        \"offset\": 0,\n" +
			"        \"count\": 40,\n" +
			"        \"total\": 40,\n" +
			"        \"products\": [\n" +
			"            {\n" +
			"                \"id\": \"a5269dc4-b6f5-4ab1-a71f-badfdceca1d3\",\n" +
			"                \"code\": \"VIDEOPLAN_TESTE2E\",\n" +
			"                \"productType\": {\n" +
			"                    \"typeId\": \"product-type\",\n" +
			"                    \"id\": \"eddcf245-874d-4cca-b069-8ada224b1d1a\",\n" +
			"                    \"key\": \"video-plan\"\n" +
			"                },\n" +
			"                \"name\": {\n" +
			"                    \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
			"                },\n" +
			"                \"description\": {\n" +
			"                    \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
			"                },\n" +
			"                \"categories\": [],\n" +
			"                \"variants\": [\n" +
			"                    {\n" +
			"                        \"id\": \"1\",\n" +
			"                        \"sku\": \"VIDEOPLAN_TESTE2E_SKU\",\n" +
			"                        \"key\": \"VIDEOPLAN_TESTE2E-1\",\n" +
			"                        \"prices\": [\n" +
			"                            {\n" +
			"                                \"value\": {\n" +
			"                                    \"centAmount\": 250000,\n" +
			"                                    \"currencyCode\": \"USD\",\n" +
			"                                    \"percentage\": 0,\n" +
			"                                    \"fractionDigits\": 2,\n" +
			"                                    \"dollarAmount\": 2500\n" +
			"                                },\n" +
			"                                \"id\": \"ecb370de-1eb0-4fb1-aa47-11c25c124df3\",\n" +
			"                                \"startDate\": \"2019-09-20T00:00:00.000Z\",\n" +
			"                                \"endDate\": \"9999-12-31T00:00:00.000Z\",\n" +
			"                                \"recurrenceIndicator\": true,\n" +
			"                                \"duration\": 0,\n" +
			"                                \"period\": \"day\",\n" +
			"                                \"proRateFrequency\": \"daily\",\n" +
			"                                \"contractIndicator\": \"contract\",\n" +
			"                                \"grandfathered\": false,\n" +
			"                                \"contracted\": false\n" +
			"                            }\n" +
			"                        ],\n" +
			"                        \"attributes\": {\n" +
			"                            \"displayName\": {\n" +
			"                                \"en\": \"VIDEOPLAN_TESTE2E\"\n" +
			"                            },\n" +
			"                            \"descriptionsByKey\": {\n" +
			"                                \"short-opus\": \"VIDEOPLAN_TESTE2E\"\n" +
			"                            },\n" +
			"                            \"billingProductCode\": \"VIDEOPLAN_TESTE2E\",\n" +
			"                            \"billingProductId\": \"32996\",\n" +
			"                            \"startDate\": \"2019-09-21T00:00:00.000Z\",\n" +
			"                            \"endDate\": \"2020-09-26T00:00:00.000Z\",\n" +
			"                            \"ranking\": 0,\n" +
			"                            \"productFamily\": \"OTT\",\n" +
			"                            \"capacityLimit\": 0,\n" +
			"                            \"videoBasePackageRequired\": false,\n" +
			"                            \"skipAdsIndicator\": false,\n" +
			"                            \"serviceFlag\": false,\n" +
			"                            \"concurrentStreamCount\": 0,\n" +
			"                            \"preOrder\": false,\n" +
			"                            \"thirdPartyOTT\": false,\n" +
			"                            \"dependentPromoIsFreeTrail\": false,\n" +
			"                            \"productStatus\": \"Active\",\n" +
			"                            \"contractApplicable\": \"contract\",\n" +
			"                            \"rsncapable\": false\n" +
			"                        }\n" +
			"                    }\n" +
			"                ]\n" +
			"            }\n" +
			"            ]\n" +
		//	"    }\n" +
			"}";

	private static String CUSTOMER_GRAPH = "{\n" +
			"  \"links\": null,\n" +
			// "  \"content\": {\n" +
			"    \"accountId\": \"190822152111954\",\n" +
			"    \"status\": \"current\",\n" +
			"    \"lastUpdatedTimeStamp\": \"2019-11-06T17:06:31.610+0000\",\n" +
			"    \"accountInfo\": {\n" +
			"      \"accountType\": \"Residential\",\n" +
			"      \"accountStatus\": \"Active\",\n" +
			"      \"verificationStatus\": false,\n" +
			"      \"spAccountId\": \"190822152111954\",\n" +
			"      \"accountCreationDate\": 1566488570000,\n" +
			"      \"cpCustomerId\": \"190822152111954\",\n" +
			"      \"isIOIndicator\": false,\n" +
			"      \"isPremiumCustomer\": true\n" +
			"    },\n" +
			"    \"contactInfo\": {\n" +
			"      \"firstName\": \"ATT\",\n" +
			"      \"lastName\": \"TEST\",\n" +
			"      \"alertNotificationEmail\": false,\n" +
			"      \"alertNotificationPush\": false,\n" +
			"      \"contactId\": \"4179595\",\n" +
			"      \"parentalControl\": false,\n" +
			"      \"isPrimaryContact\": true,\n" +
			"      \"email\": \"myatt.testing@att.com\",\n" +
			"      \"allowTracking\": false\n" +
			"    },\n" +
			"    \"billingInfo\": {\n" +
			"      \"nextBillingDateTime\": 1574510400000,\n" +
			"      \"nextBillingAmount\": 146.5\n" +
			"    },\n" +
			"    \"serviceInfo\": [\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Bolt-on\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"500 hours Cloud DVR\",\n" +
			"        \"serviceID\": \"BOLT-CDVR500FREE-201812\",\n" +
			"        \"startDate\": 1566432000000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"500 hours Cloud DVR\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4525511\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 0.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Bolt-on\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"CINEMAX\",\n" +
			"        \"serviceID\": \"BOLT-CINEMAX-201610\",\n" +
			"        \"startDate\": 1566432000000,\n" +
			"        \"validityTill\": 1574553600000,\n" +
			"        \"retailPrice\": 11.0,\n" +
			"        \"description\": \"Hit movies every week.  Bold, supercharged shows.##TV. Streaming. On Demand.||Thrilling movies every week||Action-packed shows||Every episode, of every season##Includes##Cinemax<sup>&reg;</sup> East||MAX GO<sup>&reg;</sup>||On Demand library\",\n" +
			"        \"status\": \"Final Bill\",\n" +
			"        \"promotion\": [\n" +
			"          {\n" +
			"            \"promotionName\": \"PMAX3MON\",\n" +
			"            \"promotionId\": \"PMAX3MON\",\n" +
			"            \"startDate\": 1566604800000,\n" +
			"            \"endDate\": 1574553600000,\n" +
			"            \"promotionalAmount\": 0.0,\n" +
			"            \"promotionType\": \"Free\",\n" +
			"            \"promotionAmount\": 0.0,\n" +
			"            \"isVoucher\": false,\n" +
			"            \"promoRanking\": 2,\n" +
			"            \"isDefaultPromo\": false\n" +
			"          }\n" +
			"        ],\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4525512\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 11.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Bolt-on\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"3 Streams\",\n" +
			"        \"serviceID\": \"BOLT-3RDSTREAMFREE-201804\",\n" +
			"        \"startDate\": 1566432000000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"3rd Stream Free\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4525516\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 0.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Bolt-on\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"EPIX\",\n" +
			"        \"serviceID\": \"BOLT-EPIX-201907\",\n" +
			"        \"startDate\": 1568160000000,\n" +
			"        \"retailPrice\": 6.0,\n" +
			"        \"description\": \"Watch exclusive, critically-acclaimed original series, Hollywood movies, and more.##1000s of Hollywood movies||All uncut, commercial-free||On TV + Online + On Demand + On the Go##Includes##EPIX<sup>&reg;</sup>||EPIX<sup>&reg;</sup> 2||EPIX<sup>&reg;</sup> Hits||EPIX<sup>&reg;</sup> On Demand\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4664143\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 6.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"RSN\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"Regional Sports Fee\",\n" +
			"        \"serviceID\": \"RSN-TIER1-201812\",\n" +
			"        \"startDate\": 1570665600000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"TIER1\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4900142\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 0.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Basic Service\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"ENTERTAINMENT\",\n" +
			"        \"serviceID\": \"BASE-ENTERTAINMENT-201811\",\n" +
			"        \"startDate\": 1571875200000,\n" +
			"        \"retailPrice\": 93.0,\n" +
			"        \"description\": \"Entertainment must-haves. ##65##+ channels\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"promotion\": [\n" +
			"          {\n" +
			"            \"promotionName\": \"BUNDLE10OF\",\n" +
			"            \"promotionId\": \"BUNDLE10OF\",\n" +
			"            \"startDate\": 1598227200000,\n" +
			"            \"endDate\": 1606176000000,\n" +
			"            \"promotionalAmount\": 83.0,\n" +
			"            \"promotionType\": \"Flat Off\",\n" +
			"            \"promotionAmount\": 10.0,\n" +
			"            \"isVoucher\": true,\n" +
			"            \"promoRanking\": 4,\n" +
			"            \"isDefaultPromo\": false\n" +
			"          }\n" +
			"        ],\n" +
			"        \"basicService\": true,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4900143\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 93.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"typeOfPlan\": \"Bolt-on\",\n" +
			"        \"supportedLanguages\": [\n" +
			"          \"English\"\n" +
			"        ],\n" +
			"        \"serviceName\": \"HBO\",\n" +
			"        \"serviceID\": \"BOLT-HBO-201610\",\n" +
			"        \"startDate\": 1571616000000,\n" +
			"        \"retailPrice\": 15.0,\n" +
			"        \"description\": \"24/7 access to a huge library of the best entertainment.##TV. Streaming. On Demand.||The latest, most addictive shows||New movies every week||Every episode of every season of your favorite classic and current shows##Includes##HBO<sup>&reg;</sup> East||HBO Family<sup>&reg;</sup>||HBO Latino<sup>&reg;</sup>||HBO GO<sup>&reg;</sup>||On Demand library\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"promotion\": [\n" +
			"          {\n" +
			"            \"promotionName\": \"50PERCENTOFF\",\n" +
			"            \"promotionId\": \"50PERCENTO\",\n" +
			"            \"startDate\": 1571875200000,\n" +
			"            \"endDate\": 1579824000000,\n" +
			"            \"promotionalAmount\": 7.5,\n" +
			"            \"promotionType\": \"Percentage Off\",\n" +
			"            \"promotionAmount\": 50.0,\n" +
			"            \"isVoucher\": true,\n" +
			"            \"promoRanking\": 6,\n" +
			"            \"isDefaultPromo\": false\n" +
			"          }\n" +
			"        ],\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4970072\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 15.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"installmentInfo\": {\n" +
			"          \"noOfInstallments\": 12,\n" +
			"          \"paidInstallments\": 2,\n" +
			"          \"version\": \"V2\",\n" +
			"          \"description\": \"AT&T TV Device Service Fee\",\n" +
			"          \"installmentID\": 5207,\n" +
			"          \"installmentAmount\": 10.0,\n" +
			"          \"status\": \"Active\"\n" +
			"        },\n" +
			"        \"serviceName\": \"AT&T TV Device(s)\",\n" +
			"        \"serviceID\": \"HARD-OSPREY-042019\",\n" +
			"        \"startDate\": 1568764800000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"AT&T TV Device(s)\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4712742\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 120.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"installmentInfo\": {\n" +
			"          \"noOfInstallments\": 12,\n" +
			"          \"paidInstallments\": 1,\n" +
			"          \"version\": \"V3\",\n" +
			"          \"description\": \"AT&T TV Installment plan \",\n" +
			"          \"installmentID\": 54240,\n" +
			"          \"installmentAmount\": 10.0,\n" +
			"          \"status\": \"Active\"\n" +
			"        },\n" +
			"        \"serviceName\": \"AT&T TV Device(s)\",\n" +
			"        \"serviceID\": \"HARD-OSPREY-042019\",\n" +
			"        \"startDate\": 1571702400000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"AT&T TV Device(s)\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4995943\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 120.0,\n" +
			"        \"quantity\": \"1\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      },\n" +
			"      {\n" +
			"        \"installmentInfo\": {\n" +
			"          \"noOfInstallments\": 12,\n" +
			"          \"paidInstallments\": 1,\n" +
			"          \"version\": \"V3\",\n" +
			"          \"description\": \"AT&T TV Installment plan \",\n" +
			"          \"installmentID\": 54240,\n" +
			"          \"installmentAmount\": 20.0,\n" +
			"          \"status\": \"Active\"\n" +
			"        },\n" +
			"        \"serviceName\": \"AT&T TV Device(s)\",\n" +
			"        \"serviceID\": \"HARD-OSPREY-042019\",\n" +
			"        \"startDate\": 1571702400000,\n" +
			"        \"retailPrice\": 0,\n" +
			"        \"description\": \"AT&T TV Device(s)\",\n" +
			"        \"status\": \"Active\",\n" +
			"        \"basicService\": false,\n" +
			"        \"cancellable\": false,\n" +
			"        \"hasAction\": false,\n" +
			"        \"ordProdId\": \"4997143\",\n" +
			"        \"rateType\": \"Contract Selling Price\",\n" +
			"        \"contractPrice\": 120.0,\n" +
			"        \"quantity\": \"2\",\n" +
			"        \"allowedRefundDate\": -1\n" +
			"      }\n" +
			"    ],\n" +
			"    \"isProjectedBillDate\": false\n" +
			// "  }\n" +
			"}";
	
	private static final String PRODUCTS_RESPONSE_TWO = "{\"limit\":300,\"count\":3,\"total\":3,\"products\":[{\"id\":\"3fb5a61e-ed27-4fcb-b144-bee80edb01a1\",\"code\":\"BASE-PLUSWOHBO-2020\",\"productType\":{\"typeId\":\"product-type\",\"id\":\"9b5b3be3-7ad2-4bca-ade3-010bac00e7f0\",\"key\":\"video-plan\"},\"name\":{\"en\":\"PLUS WO HBO\"},\"description\":{\"en\":\"####45##+ Channels ##Get popular channels including ABC  MTV  CNN  and FOX. Enjoy your favorite shows  plus new movies every week.####See all channels##Get 1 week FREE. After free trial  service renews at then-prevailing rate (currently min. $55/mo.) unless you cancel.## New subscribers only.## Cancel anytime. Restrictions apply.##See details. ##<b>AT&T TV NOW:</b> Compatible device & browser req'd. Residential customers only. Avail. in the U.S. only (excludes Puerto Rico and U.S. Virgin Islands). <b>Free Trial:</b> Cancel before end of trial or service renews monthly (currently min. $55/mo.)  billed to your payment method on file. <b>Pricing  channels  features  and terms subject to change & may be modified or discontinued at any time without notice. Regional sports & local channels:</b> Limited availability. Channels vary by package & billing region. Device may need to be in billing region in order to view. Cancellation: View  modify or cancel at any time at atttvnow.com. Once canceled you can access AT&T TV NOW through the remaining monthly period. <b>No refunds or credits for any partial-month periods or unwatched content. GENERAL:</b> Limit 3 concurrent streams per account with min. $55/mo. package. 3rd stream is $5/mo. with other packages. Programming subject to blackout restrictions. Taxes may apply. See details at atttvnow.com.##CBS||MSNBC||FOXNEWS||ESPN||BRAVO||DISNEYJUNIOR##\"},\"categories\":[],\"variants\":[{\"id\":\"1\",\"prices\":[{\"value\":{\"centAmount\":5500,\"currencyCode\":\"USD\",\"fractionDigits\":2,\"dollarAmount\":55},\"id\":\"cc6819d9-3c57-47f8-9e3e-041c6562f2dc\",\"startDate\":\"2020-03-09T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"recurrenceIndicator\":true,\"duration\":0,\"period\":\"month\",\"proRateFrequency\":\"daily\",\"contractIndicator\":\"non-contract\",\"billingReferenceId\":\"49632\",\"gfVersion\":0,\"contracted\":false,\"grandfathered\":false}],\"attributes\":{\"displayName\":{\"en\":\"PLUS\"},\"displayNamesByKey\":{\"services\":\"PLUS\",\"name-cpc\":\"PLUS\"},\"descriptionsByKey\":{\"short-opus\":\"45+ channels\",\"long-services\":\"45+ channels\",\"short-services\":\"45+\",\"description-cpc\":\"####45##+ Channels ##Get popular channels including ABC  MTV  CNN  and FOX. Enjoy your favorite shows  plus new movies every week.####See all channels##Get 1 week FREE. After free trial  service renews at then-prevailing rate (currently min. $55/mo.) unless you cancel.## New subscribers only.## Cancel anytime. Restrictions apply.##See details. ##<b>AT&T TV NOW:</b> Compatible device & browser req'd. Residential customers only. Avail. in the U.S. only (excludes Puerto Rico and U.S. Virgin Islands). <b>Free Trial:</b> Cancel before end of trial or service renews monthly (currently min. $55/mo.)  billed to your payment method on file. <b>Pricing  channels  features\\tand terms subject to change & may be modified or discontinued at any time without notice. Regional sports & local channels:</b> Limited availability. Channels vary by package & billing region. Device may need to be in billing region in order to view. Cancellation: View  modify or cancel at any time at atttvnow.com. Once canceled you can access AT&T TV NOW through the remaining monthly period. <b>No refunds or credits for any partial-month periods or unwatched content. GENERAL:</b> Limit 3 concurrent streams per account with min. $55/mo. package. 3rd stream is $5/mo. with other packages. Programming subject to blackout restrictions. Taxes may apply. See details at atttvnow.com.##CBS||MSNBC||FOXNEWS||ESPN||BRAVO||DISNEYJUNIOR##\"},\"billingProductCode\":\"BASE-PLUSWOHBO-2020\",\"billingProductId\":\"40899\",\"startDate\":\"2020-03-09T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"productFamily\":\"OTT\",\"packageGroup\":\"10000\",\"removalRule\":{\"channel\":[],\"accountType\":[\"ALL\"]},\"numberOfChannels\":\"45+ live channels\",\"language\":\"English\",\"serviceFlag\":true,\"secondaryBillingProductId\":\"7519024896\",\"compatibleProducts\":[{\"constraints\":{\"anchor\":false,\"dependent\":false},\"products\":[{\"id\":\"69087b0f-46ea-4ea1-a726-700e7040c137\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-LINEARHBO-202003\"},{\"id\":\"6851067e-6f06-4e89-9dff-c3928afd98cb\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLTNOW-HBO-201810\"},{\"id\":\"2a4d6ace-4210-4fab-8f6d-ddd63ec12b8b\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-ENESPANOL-201802\"},{\"id\":\"7a296b61-ef13-4efc-b6aa-e1f1537c971b\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-CINEMAX-201610\"},{\"id\":\"6042e194-d71d-44f3-a820-fc883a55513c\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-SHOWTIME-201707\"},{\"id\":\"efea6573-cc11-4699-8b6c-9003e8e8ca09\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLTNOW-MAX-201810\"},{\"id\":\"993bc6ac-2990-4d8b-9d0d-55cee05f0801\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLTNOW-SHOW-201810\"},{\"id\":\"71de8390-4979-4a59-9ac4-3f9c02412d62\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-STARZ-201610\"},{\"id\":\"9a0f0b45-590a-4787-851a-83a83eb26c2c\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-EPIX-201907\"},{\"id\":\"e59af3f6-cf1e-41ea-9e84-65d801b0abde\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLTNOW-STZ-201810\"},{\"id\":\"599dc3c8-0f3f-4d83-9e00-393d35f37894\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-2STREAMS-201710\"},{\"id\":\"07b18be4-8bb6-438c-bf50-26e6f3ce07c6\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-3RDSTREAMFREE-201804\"},{\"id\":\"307a01b7-d35a-48af-895f-989557abf903\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"STANDNOW-JUPSTZ-201804\"},{\"id\":\"43750a18-5e4b-4f84-9b71-bc349d76b624\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-DEPORTES-201802\"},{\"id\":\"61ce6e50-cfee-4da8-9ea5-5f56e538eaa5\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-HBO-201610\"},{\"id\":\"6c2cf0ca-dadf-40e1-8622-a4e783c59aab\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-CDVRTIER1FREE-201711\"},{\"id\":\"d911b432-9e0f-4bee-bae7-19ae2acb6ba6\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-MVPX-201910\"},{\"id\":\"2795c1cb-3c59-4279-85be-529c6c8ed1af\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"STANDNOW-JUPHBO-201804\"},{\"id\":\"9bd236a4-b81f-4c66-818b-6714f660d784\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"STANDNOW-JUPSHO-201804\"},{\"id\":\"98b62bae-ab77-44aa-bc0f-2a8bb6eacdbd\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"STANDNOW-JUPMAX-201804\"},{\"id\":\"d78c41fd-9d01-4ba6-80a1-f5a81aa309bb\",\"typeId\":\"product\",\"productType\":\"video-addon\",\"key\":\"BOLT-NBALP-201912\"}]}],\"contractApplicable\":\"noncontract\",\"evergentProdCategory\":\"Subscription\"}}]},{\"id\":\"07b18be4-8bb6-438c-bf50-26e6f3ce07c6\",\"code\":\"BOLT-3RDSTREAMFREE-201804\",\"productType\":{\"typeId\":\"product-type\",\"id\":\"fe3c5d80-b7d4-4f2d-bcbf-6e049e03dcc7\",\"key\":\"video-addon\"},\"name\":{\"en\":\"3rd Stream Free\"},\"description\":{\"en\":\"3rd Stream Free\"},\"categories\":[],\"variants\":[{\"id\":\"1\",\"prices\":[{\"value\":{\"centAmount\":0,\"currencyCode\":\"USD\",\"fractionDigits\":2,\"dollarAmount\":0},\"id\":\"9aa11e17-a1e9-4433-a303-d26f2159339e\",\"startDate\":\"2017-01-01T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"recurrenceIndicator\":true,\"duration\":0,\"contractIndicator\":\"non-contract\",\"billingReferenceId\":\"2800\",\"gfVersion\":0,\"contracted\":false,\"grandfathered\":false}],\"attributes\":{\"displayName\":{\"en\":\"3 Streams\"},\"displayNamesByKey\":{\"services\":\"3 streams\",\"name-cpc\":\"3 Streams\"},\"descriptionsByKey\":{\"short-opus\":\"Stream your favorites anytime, anywhere with the AT&amp;T TV App\",\"short-myatt\":\"Stream live and DVR content on 3 devices at once.\",\"long-services\":\"Watch TV on any 3 devices at the same time.\",\"long-myatt\":\"Available only in the U.S. (excl. Puerto Rico &amp; U.S.V.I.). Req's compatible device &amp; data connection. Limited to 3 concurrent streams.\",\"description-cpc\":\"Vietnamese Bolton\"},\"billingProductCode\":\"BOLT-3RDSTREAMFREE-201804\",\"billingProductId\":\"2804\",\"startDate\":\"2019-01-01T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"productFamily\":\"OTT\",\"removalRule\":{\"channel\":[],\"accountType\":[\"ALL\"]},\"addOnType\":\"Bolt-on\",\"planSubType\":\"Streams\",\"displayType\":\"included\",\"language\":\"English\",\"concurrentStreamCount\":1,\"secondaryBillingProductId\":\"7517907511\",\"contractApplicable\":\"all\",\"evergentProdCategory\":\"Concurrent Streams\"},\"contentImages\":[{\"url\":\"/salescms/dam/att/2017/idp/dtvnow/3rd-stream-bg-sm.png\",\"name\":\"3 streams\",\"size\":\"small\"},{\"url\":\"/salescms/dam/att/2017/idp/dtvnow/3rd-stream-bg-lg.png\",\"name\":\"3 streams\",\"size\":\"large\"},{\"url\":\"/salescms/dam/att/2017/idp/dtvnow/3rd-stream-bg-md.png\",\"name\":\"3 streams\",\"size\":\"medium\"}],\"productImages\":[]}]},{\"id\":\"6c2cf0ca-dadf-40e1-8622-a4e783c59aab\",\"code\":\"BOLT-CDVRTIER1FREE-201711\",\"productType\":{\"typeId\":\"product-type\",\"id\":\"fe3c5d80-b7d4-4f2d-bcbf-6e049e03dcc7\",\"key\":\"video-addon\"},\"name\":{\"en\":\"cDVRTier1Free\"},\"description\":{\"en\":\"##Record 500 hours||Keep recordings 90 days||Mark recordings to keep until you delete\"},\"categories\":[],\"variants\":[{\"id\":\"1\",\"prices\":[{\"value\":{\"centAmount\":0,\"currencyCode\":\"USD\",\"fractionDigits\":2,\"dollarAmount\":0},\"id\":\"f868b68c-a495-4a73-afd5-2b13c7144175\",\"startDate\":\"2017-01-01T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"recurrenceIndicator\":true,\"duration\":0,\"contractIndicator\":\"non-contract\",\"billingReferenceId\":\"2340\",\"gfVersion\":0,\"contracted\":false,\"grandfathered\":false}],\"attributes\":{\"displayName\":{\"en\":\"True Cloud DVR (500 hours)\"},\"displayNamesByKey\":{\"services\":\"True Cloud DVR (500 hrs.)\",\"name-cpc\":\"True Cloud DVR (500 hours)\"},\"descriptionsByKey\":{\"long-services\":\"Save your recordings for 90 days.\",\"short-services\":\"500 hrs. of storage\",\"description-cpc\":\"##Record 500 hours||Keep recordings 90 days||Mark recordings to keep until you delete\"},\"billingProductCode\":\"BOLT-CDVRTIER1FREE-201711\",\"billingProductId\":\"2340\",\"startDate\":\"2016-11-18T00:00:00.000Z\",\"endDate\":\"9999-12-31T00:00:00.000Z\",\"productFamily\":\"OTT\",\"removalRule\":{\"channel\":[],\"accountType\":[\"NONE\"]},\"addOnType\":\"Bolt-on\",\"planSubType\":\"cDVR\",\"capacityLimit\":500,\"displayType\":\"included\",\"language\":\"English\",\"secondaryBillingProductId\":\"7517906785\",\"contractApplicable\":\"noncontract\",\"evergentProdCategory\":\"DVR\"}}]}]}";
	private static final String DTVN_MIDAS_RULES ="[{\"ruleCode\":\"808\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 8\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7517907043\",\"contractIntent\":\"F\",\"productCode\":\"808\"},{\"ruleCode\":\"809\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule 9\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7517907044\",\"contractIntent\":\"F\",\"productCode\":\"809\"},{\"ruleCode\":\"810\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule10\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000&80000\",\"ruleId\":\"7517907045\",\"contractIntent\":\"T\",\"productCode\":\"810\"},{\"ruleCode\":\"800\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule1\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7517906668\",\"contractIntent\":\"F\",\"productCode\":\"800\"},{\"ruleCode\":\"811\",\"existingGroup\":\"20000\",\"catalogProductName\":\"Rule11\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7517907046\",\"contractIntent\":\"T\",\"productCode\":\"811\"},{\"ruleCode\":\"812\",\"catalogProductName\":\"Rule12\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000&80000\",\"ruleId\":\"7517907047\",\"contractIntent\":\"T\",\"productCode\":\"812\"},{\"ruleCode\":\"801\",\"existingGroup\":\"10000\",\"catalogProductName\":\"Rule2\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7517906669\",\"contractIntent\":\"F\",\"productCode\":\"801\"},{\"ruleCode\":\"802\",\"existingGroup\":\"70000\",\"catalogProductName\":\"Rule3\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7517906670\",\"contractIntent\":\"F\",\"productCode\":\"802\"},{\"ruleCode\":\"813\",\"catalogProductName\":\"Rule13\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7517907048\",\"contractIntent\":\"T\",\"productCode\":\"813\"},{\"ruleCode\":\"814\",\"existingGroup\":\"10000OR20000OR60000OR80000\",\"catalogProductName\":\"Rule14\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7517907049\",\"contractIntent\":\"T\",\"productCode\":\"814\"},{\"ruleCode\":\"815\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule15\",\"salesChannel\":\"selfServiceExisting|selfServiceNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000&80000\",\"ruleId\":\"7517907050\",\"contractIntent\":\"T\",\"productCode\":\"815\"},{\"ruleCode\":\"804\",\"catalogProductName\":\"Rule4\",\"salesChannel\":\"selfServiceNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7517906671\",\"contractIntent\":\"F\",\"productCode\":\"804\"},{\"ruleCode\":\"805\",\"existingGroup\":\"10000OR20000OR70000OR50000OR60000OR80000\",\"catalogProductName\":\"Rule6\",\"salesChannel\":\"agentExisting\",\"agentType\":\"Special\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000&20000&70000&80000\",\"ruleId\":\"7517906673\",\"contractIntent\":\"F\",\"productCode\":\"805\"},{\"ruleCode\":\"816\",\"existingGroup\":\"60000\",\"catalogProductName\":\"Rule16\",\"salesChannel\":\"agentExisting|agentNew\",\"leadGroup\":\"20000\",\"availableGroup\":\"20000\",\"ruleId\":\"7517907051\",\"contractIntent\":\"T\",\"productCode\":\"816\"},{\"ruleCode\":\"817\",\"existingGroup\":\"80000\",\"catalogProductName\":\"Rule17\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7519172955\",\"contractIntent\":\"F\",\"productCode\":\"817\"},{\"ruleCode\":\"806\",\"catalogProductName\":\"Rule5\",\"salesChannel\":\"agentNew\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7517906672\",\"contractIntent\":\"F\",\"productCode\":\"806\"},{\"ruleCode\":\"818\",\"existingGroup\":\"80000\",\"catalogProductName\":\"Rule18\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"20000\",\"availableGroup\":\"10000&20000\",\"ruleId\":\"7519172966\",\"contractIntent\":\"T\",\"productCode\":\"818\"},{\"ruleCode\":\"807\",\"existingGroup\":\"50000\",\"catalogProductName\":\"Rule 7\",\"salesChannel\":\"agentExisting|selfServiceExisting\",\"leadGroup\":\"10000\",\"availableGroup\":\"10000\",\"ruleId\":\"7517907042\",\"contractIntent\":\"F\",\"productCode\":\"807\"}]";
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testPopulateLeadAttribute_nonLeadOffer() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		try {
			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setDtvnAccount("191024212421755");
		when(offersUtils.fetchDtvnMidasRules()).thenReturn(JsonService
                .getListObjectFromJsonTreeWithNoRootElement(DTVN_MIDAS_RULES, DtvnMidasRule.class));
		when(ottServicesOffersProcessorHelper.findSalesChannel(Mockito.anyList(),Mockito.anyString())).thenReturn("agentExisting|selfServiceExisting");
		ottLeadProductsProcessor.populateLeadAttribute(ctProductResponse.getProducts(), productRequestWrapper);
		assertTrue(!ctProductResponse.getProducts().get(0).isLeadOffer());
	}
	
	@Test
	public void testProcessCTProduct() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		try {
			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
			ctProductResponse =  objectMapperRes.readValue(PRODUCTS_RESPONSE_TWO, CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setDtvnAccount("191024212421755");
		when(offersUtils.fetchDtvnMidasRules()).thenReturn(JsonService
                .getListObjectFromJsonTreeWithNoRootElement(DTVN_MIDAS_RULES, DtvnMidasRule.class));
		ottLeadProductsProcessor.processCTProduct(ctProductResponse.getProducts().get(0), "contract", "agentExisting|selfServiceExisting");
		assertTrue(!ctProductResponse.getProducts().get(0).isLeadOffer());
	}

}