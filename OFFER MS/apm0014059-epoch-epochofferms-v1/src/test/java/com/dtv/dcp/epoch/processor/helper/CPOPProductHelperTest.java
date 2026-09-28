package com.dtv.dcp.epoch.processor.helper;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CPOPProductHelperTest {

	@InjectMocks
	CPOPProductsHelper cpopProductsHelper;

	@Mock
	CpopClientHelper cpopClientHelper;
	
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

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
	
	private String CT_OFFERS_RESPONSE_BENIFITS = TestUtility.loadJson("/CTOfferResponseBenefits.json");
    @Test
	public void testFilters(){
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperOfferRes = new ObjectMapper();
		ObjectMapper objectMapperCGRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponse_benefits = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTProductResponse ctProductResponseTechProtect = new CTProductResponse();
		CGResponse cgResponse = new CGResponse();
		try {

			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);

			ctProductResponse =  objectMapperOfferRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
			ctOfferResponse =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			ctOfferResponse_benefits =  objectMapperRes.readValue(CT_OFFERS_RESPONSE_BENIFITS, CTOfferResponse.class);
			cgResponse = objectMapperCGRes.readValue(CUSTOMER_GRAPH, CGResponse.class);

			ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);
			ctProductResponseTechProtect = new DataReader().readFileToObj("CTProductsTechProtection.json",CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}


		List<ProductObj> list = cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.CONTRACT,null,null);
		List<ProductObj> list1 = cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(), Constants.NONCONTRACT,null,null);
		List<ProductObj> list2 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), true, false);// Mobility
		List<ProductObj> list3 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), false, false);//NonMobility
		List<ProductObj> list4 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), false, true); //"Employee"
		

		List<ProductObj> listOfferedPromo = cpopProductsHelper.filterByProductsObj(ctProductResponse.getProducts(), Arrays.asList(new String[]{"VIDEOPLAN_TESTE2E"}));

		List<Product> listOfferedPromo1 = cpopProductsHelper.filterByProducts(ctOfferResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), Arrays.asList(new String[]{"VIDEOPLAN_TESTE2E"}));
		List<Product> listOfferedPromo2 = cpopProductsHelper.filterByProductsNotInList(ctOfferResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), Arrays.asList(new String[]{"VIDEOPLAN_TESTE2E"}));
		List<Product> listOfferedPromo21 = cpopProductsHelper.filterByProductsInList(ctOfferResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), Arrays.asList(new String[]{"VIDEOPLAN_TESTE2E"}));
		CTOfferResponse listOfferedPromo3 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, "Mobility"); //cgResponse
		CTOfferResponse listOfferedPromo31 = cpopProductsHelper.filterProductsByRetIOApplicableProducts(ctOfferResponse); //cgResponse
		CTOfferResponse listOfferedPromo3_io = cpopProductsHelper.filterProductsByRetIOApplicableProducts(ctOfferResponse_benefits); //cgResponse_io
		CTOfferResponse listOfferedPromo4 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, "Residential"); //cgResponse
		CTOfferResponse listOfferedPromo5 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, "Employee"); //cgResponse
		CTOfferResponse listOfferedPromo71 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse_benefits, "Mobility"); //cgResponse
		CTOfferResponse listOfferedPromo72 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse_benefits, "Residential"); //cgResponse
		CTOfferResponse listOfferedPromo73= cpopProductsHelper.filterProductsByAccountType(ctOfferResponse_benefits, "Employee"); //cgResponse
		
		CTOfferResponse listOfferedPromo6 = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponseProcessed, java.util.Arrays.asList("TV 1"), false, "", "opus", "contract");
		CTOfferResponse listOfferedPromo8 = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponseProcessed, java.util.Arrays.asList("TV 1"), false, "", "online", "contract");
		List<Product> listOfferedPromo7 = cpopProductsHelper.filterProductsByCustomerSegment(ctOfferResponseProcessed.getOffers().get(3).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), "Residential");
		List<Product> listOfferedPromo7_1 = cpopProductsHelper.filterProductsByCustomerSegment(ctOfferResponse_benefits.getOffers().get(3).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), "Residential");
		
		cpopProductsHelper.filterNonIAPRemovalRules(ctProductResponseTechProtect.getProducts(), "Residential", null, productRequest);
		cpopProductsHelper.filterNonIAPRemovalRules(ctProductResponseTechProtect.getProducts(), "Residential", null, productRequest);
		CTOfferResponse ctOfferResponse1 = cpopProductsHelper.filterInvalidPrices(ctOfferResponseProcessed);
		CTOfferResponse ctOfferResponse2 = cpopProductsHelper.filterInvalidPrices(ctOfferResponse_benefits);
		cpopProductsHelper.removeExpiredPricesFromList(ctOfferResponseProcessed.getOffers().get(3).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts(), ctOfferResponseProcessed.getOffers().get(3).getAttributes().getContractIndicator(),null,null);
		assertNotNull(list);
		assertNotNull(list1);
		assertNotNull(list2);
		assertNotNull(list3);
	}

	@Test
	public void testFiltersWithError() throws JsonMappingException, JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperOfferRes = new ObjectMapper();
		ObjectMapper objectMapperCGRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductResponse ctProductResponse = new CTProductResponse();
		CTProductRequest ctProductRequest = new CTProductRequest();
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CGResponse cgResponse = new CGResponse();
		productRequest = objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
		BeanUtils.copyProperties(productRequest, ctProductRequest);

		ctProductResponse = objectMapperOfferRes.readValue(PRODUCT_RESPONSE, CTProductResponse.class);
		ctOfferResponse = objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
		cgResponse = objectMapperCGRes.readValue(CUSTOMER_GRAPH, CGResponse.class);

		List<ProductObj> list = cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(),
				Constants.CONTRACT, null, null);
		List<ProductObj> list1 = cpopProductsHelper.filterPriceByContractIndicator(ctProductResponse.getProducts(),
				Constants.NONCONTRACT, null, null);
		List<ProductObj> list2 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), true,
				false); // "Mobility"
		List<ProductObj> list3 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), false,
				false); // "NonMobility"
		List<ProductObj> list4 = cpopProductsHelper.filterByCompatibleProducts(ctProductResponse.getProducts(), false,
				true); // "Employee"
		CTOfferResponse ctOfferResponse1 = cpopProductsHelper.filterInvalidPrices(ctOfferResponse);

		ServiceException ex = catchThrowableOfType(
				() -> cpopProductsHelper.filterByProductsObj(null, Arrays.asList(new String[] { "VIDEOPLAN_TESTE2E" })),
				ServiceException.class);

		List<Product> listOfferedPromo1 = cpopProductsHelper.filterByProducts(
				ctOfferResponse.getOffers().get(1).getAttributes().getAssociatedProducts().get(0)
						.getQualifyingProducts().get(0).getProducts(),
				Arrays.asList(new String[] { "VIDEOPLAN_TESTE2E" }));
		List<Product> listOfferedPromo2 = cpopProductsHelper.filterByProductsNotInList(null,
				Arrays.asList(new String[] { "VIDEOPLAN_TESTE2E" }));
//		CTOfferResponse listOfferedPromo3 = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, cgResponse);

		// ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes().getCompatibleProducts().get(0).getProducts().get(0).setObj(null);

		List<Product> listOfferedPromo5 = cpopProductsHelper.filterProductsByCustomerSegment(null, "Residential");

		// assertNull(listOfferedPromo2);
		assertNotNull(listOfferedPromo5);
		ctOfferResponse.getOffers().get(0).setAttributes(null);
		CTOfferResponse ctOfferResponse2 = objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
		NullPointerException npe = catchThrowableOfType(
				() -> cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse2, java.util.Arrays.asList("TV 1"),
						true, "", "online", "contract"),
				NullPointerException.class);
	}

	@Test
	public void testFilterInstallments() {
		CTProductResponse ctProductResponse = new CTProductResponse();

		try {
			ctProductResponse = new DataReader().readFileToObj("/ProductsWithInstallments.json",CTProductResponse.class);
		} catch(Exception e) {
			//do nothing
		}
    	cpopProductsHelper.filterExpiredInstallmentOptions(ctProductResponse.getProducts());
	}
	@Test
	public void testGetVideoAddons() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductRequest ctProductRequest = new CTProductRequest();

		try {
			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			ctProductResponse = new DataReader().readFileToObj("/ProductsWithInstallments.json",CTProductResponse.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
		} catch(Exception e) {
			//do nothing
		}
		when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setCtProductRequest(ctProductRequest);
		cpopProductsHelper.getVideoAddonProducts(productRequestWrapper);
	}

	@Test
	public void testGetVideoAddonProducts() {
		CTProductResponse ctProductResponse = new CTProductResponse();
		ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ProductRequest productRequest = null;
		CTProductRequest ctProductRequest = new CTProductRequest();

		try {
			productRequest =  objectMapper.readValue(PRODUCT_REQUEST, ProductRequest.class);
			ctProductResponse = new DataReader().readFileToObj("/ProductsWithInstallments.json",CTProductResponse.class);
			BeanUtils.copyProperties(productRequest, ctProductRequest);
		} catch(Exception e) {
			//do nothing
		}
		when(cpopClientHelper.getProducts(Mockito.any())).thenReturn(ctProductResponse);
		productRequest.setProductTypes(java.util.Arrays.asList(Constants.VIDEO_DEVICE, Constants.VIDEO_ACCESSORY));
		productRequestWrapper.setProductRequest(productRequest);
		productRequestWrapper.setCtProductRequest(ctProductRequest);
		cpopProductsHelper.getEquipmentsProducts(productRequestWrapper);
	}
	
	@Test
	public void testFilterByProductsError() {
		
		Product product1 = Mockito.mock(Product.class);
		List<Product> products = new ArrayList<Product>();
		List<String> listOfProducts = new ArrayList<String>();
		listOfProducts.add("TEST");
		ArrayIndexOutOfBoundsException arrayException = new ArrayIndexOutOfBoundsException();
		Mockito.when(product1.getKey()).thenThrow(arrayException);
		products.add(product1);
		cpopProductsHelper.filterByProducts(products,listOfProducts);

	}
	
	@Test
	public void testFilterByProductsNotInListError() {
		Product product1 = Mockito.mock(Product.class);
		List<Product> products = new ArrayList<Product>();
		List<String> listOfProducts = new ArrayList<String>();
		listOfProducts.add("TEST");
		ArrayIndexOutOfBoundsException arrayException = new ArrayIndexOutOfBoundsException();
		Mockito.when(product1.getKey()).thenThrow(arrayException);
		products.add(product1);
		cpopProductsHelper.filterByProductsNotInList(products,listOfProducts);
	}
	
	@Test
	public void testFilterByProductsInListError() {
		Product product1 = Mockito.mock(Product.class);
		List<Product> products = new ArrayList<Product>();
		List<String> listOfProducts = new ArrayList<String>();
		listOfProducts.add("TEST");
		ArrayIndexOutOfBoundsException arrayException = new ArrayIndexOutOfBoundsException();
		Mockito.when(product1.getKey()).thenThrow(arrayException);
		products.add(product1);
		cpopProductsHelper.filterByProductsInList(products,listOfProducts);
	}
	
	//@Test
	public void testFilterProductsByCustomerSegment() throws JsonParseException, JsonMappingException, IOException {
		
		String PRODUCT_STRING = " {\n                                        \"typeId\": \"product\",\n                                        \"id\": \"2067b3eb-ffc9-4865-82bf-a76f2bc7cbe2\",\n                                        \"productType\": \"video-device\",\n                                        \"key\": \"HARD-OSPREY-042019\",\n                                        \"obj\": {\n                                            \"id\": \"2067b3eb-ffc9-4865-82bf-a76f2bc7cbe2\",\n                                            \"code\": \"HARD-OSPREY-042019\",\n                                            \"name\": {\n                                                \"en\": \"AT&T TV Device\"\n                                            },\n                                            \"description\": {\n                                                \"en\": \"AT&T TV Device(s)\"\n                                            },\n                                            \"version\": 359,\n                                            \"createdAt\": \"2019-10-14T21:58:43.652Z\",\n                                            \"createdBy\": {\n                                                \"clientId\": \"LH-YOABCHzQ9IZix5qtjCF5n\",\n                                                \"isPlatformClient\": false\n                                            },\n                                            \"lastModifiedAt\": \"2020-10-21T23:10:25.152Z\",\n                                            \"lastModifiedBy\": {\n                                                \"isPlatformClient\": true,\n                                                \"user\": {\n                                                    \"typeId\": \"user\",\n                                                    \"id\": \"6584dcf8-2ca2-4fa7-a21b-ab848b3cad6f\"\n                                                }\n                                            },\n                                            \"status\": {\n                                                \"typeId\": \"state\",\n                                                \"id\": \"8fb20b3c-fde9-4073-ba6c-1c1c0211c577\"\n                                            },\n                                            \"productType\": {\n                                                \"typeId\": \"product-type\",\n                                                \"id\": \"81078d42-227b-444c-b225-d1ab9226acd6\",\n                                                \"key\": \"video-device\"\n                                            },\n                                            \"categories\": [],\n                                            \"variants\": [\n                                                {\n                                                    \"id\": 1,\n                                                    \"attributes\": {\n                                                        \"productFamily\": \"OTT\",\n                                                        \"displayNamesByKey\": {\n                                                            \"name-cpc\": \"AT&T TV Device(s)\"\n                                                        },\n                                                        \"descriptionsByKey\": {\n                                                            \"short-myatt\": \"AT&amp;T TV device &amp; voice remote with the Google Assistant.  Google login required.\",\n                                                            \"short-opus\": \"AT&amp;T TV device &amp; voice remote with the Google Assistant.  Google login required.\",\n                                                            \"long-myatt\": \"Rep should inform customer that device has a Limited 2-year Warranty; term details can be found at att.com/atttvwarranty.\",\n                                                            \"description-cpc\": \"AT&T TV Device(s)\"\n                                                        },\n                                                        \"opusDisclosureMessage\": \"<p>I have informed the customer of the following: Device has a Limited 2-year Warranty; term details can be found at att.com/atttvwarranty<br><sup>1</sup> Purchasing a device on an installment plan requires an installment agreement and AT&#38;T TV service. Taxes are due at time of sale. If service is cancelled, the outstanding balance is charged. <br>If service is canceled within the first 14 days of purchase, the AT&#38;TV device included with service must be returned within 14 days of purchase to avoid a non-return fee of $120.</p>\",\n                                                        \"secondaryBillingProductId\": \"7523830999\",\n                                                        \"clearRejections\": false,\n                                                        \"discountSku\": \"7088A\",\n                                                        \"equipmentSku\": \"60075\",\n                                                        \"installmentList\": [\n                                                            {\n                                                                \"installmentId\": 5207,\n                                                                \"installmentDescription\": \"AT&T TV Device Service Fee\",\n                                                                \"installmentPlanVersion\": \"V2\",\n                                                                \"installmentStartDate\": \"2019-05-31T18:30:00.000Z\",\n                                                                \"installmentEndDate\": \"2019-06-04T12:29:00.000Z\",\n                                                                \"numberOfMonthlyInstallments\": 12,\n                                                                \"creditRiskEligibility\": [\n                                                                    {\n                                                                        \"key\": \"Low\",\n                                                                        \"label\": \"Low\"\n                                                                    },\n                                                                    {\n                                                                        \"key\": \"Medium\",\n                                                                        \"label\": \"Medium\"\n                                                                    }\n                                                                ],\n                                                                \"contractApplicable\": [\n                                                                    \"contract\"\n                                                                ]\n                                                            },\n                                                            {\n                                                                \"installmentId\": 54240,\n                                                                \"installmentDescription\": \"AT&T TV Installment plan\",\n                                                                \"installmentPlanVersion\": \"V3\",\n                                                                \"installmentStartDate\": \"2019-07-18T18:30:00.000Z\",\n                                                                \"installmentEndDate\": \"9999-12-30T12:29:00.000Z\",\n                                                                \"numberOfMonthlyInstallments\": 12,\n                                                                \"creditRiskEligibility\": [\n                                                                    {\n                                                                        \"key\": \"Low\",\n                                                                        \"label\": \"Low\"\n                                                                    },\n                                                                    {\n                                                                        \"key\": \"Medium\",\n                                                                        \"label\": \"Medium\"\n                                                                    }\n                                                                ],\n                                                                \"contractApplicable\": [\n                                                                    \"contract\"\n                                                                ]\n                                                            }\n                                                        ],\n                                                        \"contractApplicable\": \"contract\",\n                                                        \"migratedProduct\": true,\n                                                        \"displayName\": {\n                                                            \"en\": \"AT&T TV Device(s)\"\n                                                        },\n                                                        \"billingProductCode\": \"HARD-OSPREY-042019\",\n                                                        \"billingProductId\": \"20899\",\n                                                        \"startDate\": \"2019-04-16T18:30:00.000Z\",\n                                                        \"endDate\": \"9999-12-30T18:30:00.000Z\",\n                                                        \"removalRule\": {\n                                                            \"channel\": [],\n                                                            \"customerSegments\": [\n                                                                \"None\"\n                                                            ],\n                                                            \"accountType\": [\n                                                                \"NONE\"\n                                                            ]\n                                                        },\n                                                        \"evergentProdCategory\": \"Hardware\",\n                                                        \"deliverymethod\": [\n                                                            \"DF\",\n                                                            \"Enjoy\"\n                                                        ],\n                                                        \"firstPublishedAt\": \"2020-03-02T20:58:45.838Z\",\n                                                        \"lastUpdatedState\": \"live\",\n                                                        \"myAttDisclosureMessage\": \"<strong>AT&amp;T TV:</strong> New U.S. customers only. Must prepay first 2 months of service at full price. Online orders will be shipped in 1 to 3 days by FedEx Ground<sup>&#174;</sup> to the address provided. <strong>Non-refundable.</strong> Offer limited to 1 per AT&amp;T TV account; 2 per household. Not combinable with select offers.\",\n                                                        \"minQuantity\": 1,\n                                                        \"salesChannel\": [\n                                                            \"onlineSales\",\n                                                            \"onlineServices\",\n                                                            \"opusSales\",\n                                                            \"opusServices\",\n                                                            \"partnerSales\",\n                                                            \"partnerServices\"\n                                                        ],\n                                                        \"customerSegments\": [\n                                                            \"Employee\"\n                                                        ],\n                                                        \"businessSegment\": [\n                                                            \"CONS\"\n                                                        ],\n                                                        \"minMaxQuantity\": [\n                                                            {\n                                                                \"minQuantity\": 0,\n                                                                \"maxQuantity\": 1,\n                                                                \"contractApplicable\": [\n                                                                    \"EDSP\"\n                                                                ]\n                                                            },\n                                                            {\n                                                                \"contractApplicable\": [\n                                                                    \"contract\"\n                                                                ],\n                                                                \"minQuantity\": 1,\n                                                                \"maxQuantity\": 6\n                                                            }\n                                                        ],\n                                                        \"deliveryMethodByChannel\": {\n                                                            \"deliveryMethod\": [\n                                                                {\n                                                                    \"salesChannel\": \"online\",\n                                                                    \"customerSegments\": \"Employee\",\n                                                                    \"deliverymethod\": \"Enjoy\"\n                                                                }\n                                                            ]\n                                                        },\n                                                        \"contractIndicator1\": [\n                                                            \"contract\"\n                                                        ],\n                                                        \"maxQuantity\": 6,\n                                                        \"partnerKeys\": []\n                                                    },\n                                                    \"prices\": [\n                                                        {\n                                                            \"value\": {\n                                                                \"type\": \"centPrecision\",\n                                                                \"currencyCode\": \"USD\",\n                                                                \"centAmount\": 9600,\n                                                                \"fractionDigits\": 2,\n                                                                \"dollarAmount\": \"96.00\"\n                                                            },\n                                                            \"id\": \"29ebc47e-f08f-405c-a142-34bbedd06e2e\",\n                                                            \"startDate\": \"2019-03-31T18:30:00.000Z\",\n                                                            \"endDate\": \"2019-06-22T18:30:00.000Z\",\n                                                            \"channel\": \"contracted\",\n                                                            \"recurrenceIndicator\": false,\n                                                            \"contractIndicator\": \"contract\",\n                                                            \"isGrandfathered\": false,\n                                                            \"billingReferenceId\": \"21831\"\n                                                        },\n                                                        {\n                                                            \"value\": {\n                                                                \"type\": \"centPrecision\",\n                                                                \"currencyCode\": \"USD\",\n                                                                \"centAmount\": 0,\n                                                                \"fractionDigits\": 2,\n                                                                \"dollarAmount\": \"0.00\"\n                                                            },\n                                                            \"id\": \"3ce0be09-fe8a-4398-b737-7ce7e76c3822\",\n                                                            \"startDate\": \"2019-03-31T18:30:00.000Z\",\n                                                            \"endDate\": \"2019-07-30T18:30:00.000Z\",\n                                                            \"channel\": \"non-contracted\",\n                                                            \"recurrenceIndicator\": false,\n                                                            \"contractIndicator\": \"non-contract\",\n                                                            \"isGrandfathered\": false,\n                                                            \"billingReferenceId\": \"21832\"\n                                                        },\n                                                        {\n                                                            \"value\": {\n                                                                \"type\": \"centPrecision\",\n                                                                \"currencyCode\": \"USD\",\n                                                                \"centAmount\": 12000,\n                                                                \"fractionDigits\": 2,\n                                                                \"dollarAmount\": \"120.00\"\n                                                            },\n                                                            \"id\": \"46533662-938c-4c71-91c7-ff2cc75e0f8c\",\n                                                            \"startDate\": \"2019-06-23T18:30:00.000Z\",\n                                                            \"endDate\": \"9999-12-30T18:30:00.000Z\",\n                                                            \"channel\": \"contracted\",\n                                                            \"recurrenceIndicator\": false,\n                                                            \"contractIndicator\": \"contract\",\n                                                            \"isGrandfathered\": false,\n                                                            \"billingReferenceId\": \"23232\"\n                                                        }\n                                                    ]\n                                                }\n                                            ]\n                                        }\n                                    }";
		ObjectMapper objectMapper = new ObjectMapper();
		Product product1 =  objectMapper.readValue(PRODUCT_STRING, Product.class);

		List<Product> products = new ArrayList<Product>();
		products.add(product1);
		String customerSegments = "Employee";

		cpopProductsHelper.filterProductsByCustomerSegment(products,customerSegments);
	}
	
	@Test
	public void testGetSegmentDescription() {
		String SegmentDescription = "TEST | TEST";
		cpopProductsHelper.getSegmentDescription(SegmentDescription);
	}
	
	@Test
	public void testFilterProductsByAccountType()
	{
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		List<CTOffer> offers= new ArrayList<CTOffer>();
		CTOffer offer = Mockito.mock(CTOffer.class);
		NullPointerException exception = new NullPointerException();
		Mockito.when(offer.getAttributes()).thenThrow(exception);
		offers.add(offer);
		ctOfferResponse.setOffers(offers);
		String accountType = "TEST";
		ServiceException npe = catchThrowableOfType(
				() -> cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, accountType),
				ServiceException.class);
		assertNotNull(npe);
	}

}
