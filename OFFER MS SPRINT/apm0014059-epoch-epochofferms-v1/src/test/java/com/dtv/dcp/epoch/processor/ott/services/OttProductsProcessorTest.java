package com.dtv.dcp.epoch.processor.ott.services;



import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.DeviceInfo;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttProductsProcessor;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttProductsProcessorTest {



	@InjectMocks
	private OttProductsProcessor ottProductsProcessor;

	@Mock
	CpopClientHelper cpopClient;

	@Mock
	CustomerGraphService customerGraphService;

	@Mock
	CPOPProductsHelper cpopProductsHelper;

	@Mock
	OffersUtils offersUtils;
	
	@Mock
	FeatureManagerHelper featureManagerHelper;

	@Mock
	OttLeadProductsProcessor ottLeadProductsProcessor;
	
	@Mock
	OttProductsServicesProcessor ottProductsServicesProcessor;


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

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);

	}


	@Test
	public void testGetProducts(){

		// Temp  need to update late once logic updated

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCGRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		CGResponse cgResponse = new CGResponse();
		try {

			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);

			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
			cgResponse = objectMapperCGRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
		}


		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		// ctProduct.set(attributes);
		// responseList.add(ctOffer);
		// ctProductResponse.setOffers(responseList);
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setDtvnAccount("191024212421755");
		doReturn(ctProductResponse).when(cpopClient).getProducts(ctProductRequest);

		//doReturn(ctProductResponse).when(ottProductsProcessor).getProducts(productRequestWrapper);


		// ctProductResponse = cpopClient.getProducts(ctProductRequest);
		when(cpopClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		when(customerGraphService.getActiveSubscriptions("","")).thenReturn(cgResponse);
		// doReturn(cgResponse).when(customerGraphService.getActiveSubscriptions("191024212421755"));
		// when(ottProductsProcessor.getCustomerAccount(Mockito.any())).thenReturn(cgResponse);
		doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.CONTRACT,null,null);
		doReturn(cgResponse).when(customerGraphService).getActiveSubscriptions("","");
		// doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.NONCONTRACT,null,null);
		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.NONCONTRACT,null,null);
		CTProductResponse productResponse = ottProductsProcessor.getProducts(productRequestWrapper);


		assertNotNull(productResponse);
	}

	@Test
	public void testGetProductsCustomerContext(){

		// Temp  need to update late once logic updated

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCGRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		CGResponse cgResponse = new CGResponse();
		try {

			productRequest =  objectMapper.readValue(PRODUCT_REQUEST_CUSTOMERCONTEXT, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);

			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
			cgResponse = objectMapperCGRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
		}

//		ctProductRequest.setOfferProductSubType(offerRequest.getOfferProductTypes());
//		ctProductRequest.setState("staged");

	
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		// ctProduct.set(attributes);
		// responseList.add(ctOffer);
		// ctProductResponse.setOffers(responseList);
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setDtvnAccount("191024212421755");
		doReturn(ctProductResponse).when(cpopClient).getProducts(ctProductRequest);

		//doReturn(ctProductResponse).when(ottProductsProcessor).getProducts(productRequestWrapper);


		// ctProductResponse = cpopClient.getProducts(ctProductRequest);
		when(cpopClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		when(customerGraphService.getActiveSubscriptions("","")).thenReturn(cgResponse);
		when(ottProductsServicesProcessor.processProductsServices(any(), any())).thenReturn(ctProductResponse);
		// doReturn(cgResponse).when(customerGraphService.getActiveSubscriptions("191024212421755"));
		// when(ottProductsProcessor.getCustomerAccount(Mockito.any())).thenReturn(cgResponse);
		doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.CONTRACT,null,null);
		doReturn(cgResponse).when(customerGraphService).getActiveSubscriptions("","");
		// doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.NONCONTRACT,null,null);
		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.NONCONTRACT,null,null);
		CTProductResponse productResponse = ottProductsProcessor.getProducts(productRequestWrapper);


		assertNotNull(productResponse);
	}

	//@Test(expected = ServiceException.class)
	@Test
	public void testGetProductsException(){

		// Temp  need to update late once logic updated

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCGRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		
		try {
		
			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);

			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
			CGResponse cgResponse = objectMapperCGRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
			assertNotNull(cgResponse);
		} catch(Exception e) {
			//do nothing
		}


		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);

		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setDtvnAccount("191024212421755");
		doReturn(ctProductResponse).when(cpopClient).getProducts(ctProductRequest);

		//doReturn(ctProductResponse).when(ottProductsProcessor).getProducts(productRequestWrapper);


		// ctProductResponse = cpopClient.getProducts(ctProductRequest);
		when(cpopClient.getProducts(Mockito.any())).thenReturn(ctProductResponse);

		// doReturn(cgResponse).when(customerGraphService.getActiveSubscriptions("191024212421755"));
		// when(ottProductsProcessor.getCustomerAccount(Mockito.any())).thenReturn(cgResponse);
		doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.CONTRACT,null,null);
		// doReturn(cgResponse).when(customerGraphService).getActiveSubscriptions(any());
		// doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		when(customerGraphService.getActiveSubscriptions("","")).thenThrow(new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001));
//		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
//		cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);
		ottProductsProcessor.getCustomerAccount("3342432432");


		//assert(productResponse);
	}
	@Test
	public void testValidateRequest(){

		// Temp  need to update late once logic updated

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

	
		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		doReturn(ctProductResponse).when(cpopClient).getProducts(ctProductRequest);

		List<String> list = new ArrayList<>(Arrays.asList("32957","32961","32964"));
		when(offersUtils.fetchCTZipcodes()).thenReturn(list);
		//ottProductsProcessor.validateProductsRequest(productRequestWrapper);


		
	}
	@Test//(expected = ServiceException.class)
	public void testValidateRequestZipCodeNotValid(){

		// Temp  need to update late once logic updated

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		try {

			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
			// productRequest.getAccount().setZipCode("4646");
			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}


		OfferAttributes attributes=new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		productRequestWrapper.setProductRequest(productRequest);
		doReturn(ctProductResponse).when(cpopClient).getProducts(ctProductRequest);

		List<String> list = new ArrayList<>(Arrays.asList("32961","32964"));
		when(offersUtils.fetchCTZipcodes()).thenReturn(list);
		//ottProductsProcessor.validateProductsRequest(productRequestWrapper);


	
	}
	@SuppressWarnings("unused")
	@Test
	public void testValidRequest(){



		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		try {

			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
			// productRequest.getAccount().setZipCode("4646");
			ctProductResponse =  objectMapperRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}

		productRequest.setProductCodes(Arrays.asList("BASE-PLUS-2018"));
		productRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
		List<DeviceInfo> devices = new ArrayList<>();
		productRequest.setDevices(devices);
		ottProductsProcessor.isValidDevicesRequest(productRequest);
		ottProductsProcessor.isValidProductRequest(productRequest);



	}
	
	@Test
	@SuppressWarnings("unused")
	public void testNotValidRequest(){



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

		productRequest.setProductCodes(null);
		productRequest.setProductFamily(null);

		productRequest.setDevices(null);
		ottProductsProcessor.isValidDevicesRequest(productRequest);
		ottProductsProcessor.isValidProductRequest(productRequest);



	}
}
