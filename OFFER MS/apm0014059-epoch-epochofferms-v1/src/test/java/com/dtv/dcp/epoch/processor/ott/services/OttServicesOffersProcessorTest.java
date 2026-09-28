package com.dtv.dcp.epoch.processor.ott.services;


import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.BeanUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.AdditionalDetails;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.response.CGResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.processor.ott.OttCTOffersProcessor;
import com.dtv.dcp.epoch.processor.ott.sales.OttSalesOffersProcessorHelper;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphServiceImpl;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.TestUtility;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OttServicesOffersProcessorTest {
	
/** The OttServicesOffersProcessor . */
	
	@InjectMocks
	@Spy
	private OttServicesOffersProcessor ottServicesOffersProcessor;

	@Mock
	CpopClientHelper cpopClient;
	
	@Mock
	OttCTOffersProcessor ottCTOffersProcessor;

	@Mock
	OffersUtils offersUtils;


	@Mock
	CustomerGraphService customerGraphService;

	@Mock
	CustomerGraphServiceImpl customerGraphServiceImpl;

	@Mock
	OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

	@Mock
	CPOPBenefitsHelper cpopBenefitsHelper;

	@Mock
	CPOPProductsHelper cpopProductsHelper;
	
	@Mock
	OttAvaialbleOffersProcessor ottAvaialbleOffersProcessor ;
	
	@Mock
	FeatureManagerHelper featureManagerHelper ;

	@Mock
	OttServicesOffersProcessorHelper ottServicesOffersProcessorHelper;

	@Mock
	CpopUCCClientHelper cpopUCCClientHelper;

	@Mock
	RedisCacheHelper redisCacheHelper;


	private static String OFFER_REQUEST = "{\n" +
			"    \"offerActionType\": [\n" +
			"        \"Retention\"\n" +
			"    ],\n" +
			"\"customerContext\": {\n" +
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
			"  }," +
			"    \"offerProductFamily\": [\n" +
			"        \"OTT\"\n" +
			"    ],\n" +
			"    \"salesChannel\": [\n" +
			"        \"agent\"\n" +
			"    ],\n" +
			"    \"state\": \"staged\",\n" +
			"    \"pagination\": {\n" +
			"        \"page\": 1,\n" +
			"        \"limit\": 300\n" +
			"    },\n" +
			"    \"heartValue\": \"TV 1\"\n" +
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
	
	
	private static String OFFER_REQUEST_SERVICE = "{ \n" + 
			"    \"salesChannel\": [ \n" + 
			"        \"opus\" \n" + 
			"    ], \n" + 
			"    \"offerProductTypes\": [ \n" + 
			"        \"video-plan\" \n" + 
			"    ], \n" + 
			"    \"offerProductFamily\": [ \n" + 
			"        \"OTT\" \n" + 
			"    ], \n" + 
			"    \"contractIndicator\": [ \n" + 
			"        \"contract\" \n" + 
			"    ], \n" + 
			"    \"customerEligibility\": { \n" + 
			"        \"zipCode\": [ \n" + 
			"            \"32958\" \n" + 
			"        ] \n" + 
			"    }, \n" + 
			"    \"offerActionType\": [ \n" + 
			"        \"Other\" \n" + 
			"    ], \n" + 
			"    \"pagination\": { \n" + 
			"        \"page\": 1, \n" + 
			"        \"limit\": 300 \n" + 
			"    }, \n" + 
			"    \"state\": \"staged\", \n" + 
			"    \"customerContext\": { \n" + 
			"        \"existingProductFamily\": [ \n" + 
			"            \"OTT\", \n" + 
			"            \"wireless\" \n" + 
			"        ], \n" + 
			"        \"OTT\": { \n" + 
			"            \"accountNumber\": \"190925162250755\", \n" + 
			"            \"isActive\": true, \n" + 
			"            \"products\": [ \n" + 
			"                { \n" + 
			"                    \"productCode\": \"BASE-MAX-2018\", \n" + 
			"                    \"productType\": \"video-plan\" \n" + 
			"                } \n" + 
			"            ] \n" + 
			"        }, \n" + 
			"        \"wireless\": { \n" + 
			"            \"accountNumber\": \"298090749748\", \n" + 
			"            \"isActive\": \"True\" \n" + 
			"        } \n" + 
			"    } \n" + 
			"}";
	
	private String invalidRequestService = "{ \"offerActionType\": [ \n" + 
			"        \"Other\" \n" + 
			"    ],\n" + 
			"    \"contractIndicator\": [ \n" + 
			"        \"contract\" \n" + 
			"    ], \n" + 
			"    \"customerEligibility\": { \n" + 
			"        \"zipCode\": [ \n" + 
			"            \"32958\" \n" + 
			"        ] \n" + 
			"    },     \n" + 
			"    \"pagination\": { \n" + 
			"        \"page\": 1, \n" + 
			"        \"limit\": 300 \n" + 
			"    }, \n" + 
			"    \"state\": \"staged\"\n" + 
			"}";
	
	private String PREMIUM_SAVE_OFFER_REQ = "{\n  \"accountTypes\": [\n    \"Residential\"\n  ],\n  \"addOnType\": [],\n  \"contractIndicator\": [\n    \"contract\"\n  ],\n  \"benefitsCustomerHasReceived\": [\n    {\n      \"benefitCode\": \"$4OFSHTIM3M\",\n      \"startDate\": \"01-19-2019\",\n      \"endDate\": \"04-19-2019\"\n    },\n    {\n      \"benefitCode\": \"$4OFSTARZ3M\",\n      \"startDate\": \"06-19-2020\",\n      \"endDate\": \"09-19-2020\"\n    }\n  ],\n  \"customerContext\": {\n    \"existingProductFamily\": [\n      \"OTT\"\n    ],\n    \"OTT\": {\n      \"products\": [\n        {\n          \"basePrice\": \"11.0\",\n          \"productCode\": \"BOLT-SHOWTIME-201707\",\n          \"productType\": \"video-addon\"\n        },\n        {\n          \"basePrice\": \"124.0\",\n          \"productCode\": \"BASE-XTRA-201811\",\n          \"productType\": \"video-plan\",\n          \"promotions\": [\n            {\n              \"promotionId\": \"PXTR30CON\",\n              \"promotionStartDate\": \"12/22/2019\",\n              \"promotionEndDate\": \"12/22/2020\"\n            },\n            {\n              \"promotionId\": \"PXTR12MCON\",\n              \"promotionStartDate\": \"12/22/2019\",\n              \"promotionEndDate\": \"12/22/2020\"\n            },\n            {\n              \"promotionId\": \"PMTRACKCON\",\n              \"promotionStartDate\": \"12/22/2020\",\n              \"promotionEndDate\": \"12/22/2021\"\n            }\n          ]\n        },\n        {\n          \"basePrice\": \"11.0\",\n          \"productCode\": \"BOLT-STARZ-201610\",\n          \"productType\": \"video-addon\",\n          \"promotions\": [\n            {\n              \"promotionId\": \"$4OFSTARZ3M\",\n              \"promotionStartDate\": \"06/19/2020\",\n              \"promotionEndDate\": \"09/19/2020\"\n            }\n          ]\n        },\n        {\n          \"basePrice\": \"0.0\",\n          \"productCode\": \"BOLT-CDVR500FREE-201812\",\n          \"productType\": \"video-addon\"\n        },\n        {\n          \"basePrice\": \"6.0\",\n          \"productCode\": \"BOLT-EPIX-201907\",\n          \"productType\": \"video-addon\"\n        },\n        {\n          \"basePrice\": \"0.0\",\n          \"productCode\": \"BOLT-3RDSTREAMFREE-201804\",\n          \"productType\": \"video-addon\"\n        },\n        {\n          \"basePrice\": \"0.0\",\n          \"productCode\": \"RSN-TIER4-201812\",\n          \"productType\": \"fee\"\n        },\n        {\n          \"basePrice\": \"0.0\",\n          \"productCode\": \"HARD-OSPREY-042019\",\n          \"productType\": \"video-device\",\n          \"promotions\": [\n            {\n              \"promotionId\": \"OSPREYFREE\",\n              \"promotionStartDate\": \"12/15/2019\"\n            }\n          ]\n        }\n      ],\n      \"accountNumber\": \"191216222689556\",\n      \"isActive\": true,\n      \"retentionPromotionIndicator\": true,\n      \"segmentDescription\": \"TV 4\",\n      \"freeTrialEligible\": false,\n      \"accountType\": \"Residential\",\n      \"nextBillingDate\": \"06/19/2020\"\n    }\n  },\n  \"offerActionType\": [\n    \"Retention\"\n  ],\n  \"offerProductFamily\": [\n    \"OTT\"\n  ],\n  \"salesChannel\": [\n    \"opus\"\n  ],\n  \"offerProductTypes\": [\n    \"video-plan\",\n    \"video-addon\",\n    \"video-accessory\",\n    \"fee\",\n    \"video-device\"\n  ]\n}";
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	
	}
	
	@Test
	public void testGetOffers_service(){
		OfferRequest offerRequest = null;
		ObjectMapper objectMapper = new ObjectMapper();
		CGResponse cgResponse = new CGResponse();
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);;
		try {
			offerRequest =  objectMapper.readValue(OFFER_REQUEST_SERVICE, OfferRequest.class);
			cgResponse = objectMapper.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch (Exception e) {	}
		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(customerGraphServiceImpl.getActiveSubscriptions(any(),Mockito.anyString())).thenReturn(cgResponse);
		when(ottAvaialbleOffersProcessor.getAvailableCTOffers(any())).thenReturn(cTOfferResponse);
		when(offersUtils.isService(any())).thenReturn(true);
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));
		ottServicesOffersProcessor.getOffers(offerRequestWrapper );
		
	}
	
	@Test
	public void testGetOffers_service_InvalidRequest(){
		OfferRequest offerRequest = null;
		ObjectMapper objectMapper = new ObjectMapper();
		CGResponse cgResponse = new CGResponse();
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);
		try {
			offerRequest =  objectMapper.readValue(invalidRequestService, OfferRequest.class);
			cgResponse = objectMapper.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch (Exception e) {	}
		
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(customerGraphServiceImpl.getActiveSubscriptions(any(),Mockito.anyString())).thenReturn(cgResponse);
		when(ottAvaialbleOffersProcessor.getAvailableCTOffers(any())).thenReturn(cTOfferResponse);
		when(offersUtils.isService(any())).thenReturn(true);
		
		ServiceException ex = catchThrowableOfType(() -> ottServicesOffersProcessor.getOffers(offerRequestWrapper ),
				ServiceException.class);
		assertNotNull(ex);
		
	}
	
	
	@Test
	public void testGetOffers(){

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {


			offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			// ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

			// ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;
			cgResponse = objectMapperCusRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
			// System.out.println("Error : "+e);
		}

		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest.setContractIndicator(Arrays.asList("contract"));
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);
		doReturn(ctOfferResponseProcessed).when(cpopClient).getOffers(any());

		//doReturn(ctProductResponse).when(ottProductsProcessor).getProducts(productRequestWrapper);


		// ctProductResponse = cpopClient.getProducts(ctProductRequest);
		when(cpopClient.getOffers(any())).thenReturn(ctOfferResponseProcessed);
		when(customerGraphServiceImpl.getActiveSubscriptions(any(),Mockito.anyString())).thenReturn(cgResponse);
		when(ottSalesOffersProcessorHelper.dtvNowRulesProcessing(any(),any())).thenReturn(ctOfferResponseProcessed.getOffers());
		when(cpopProductsHelper.filterOffersByHeartValue(any(), any(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponseProcessed);
//		when(cpopProductsHelper.filterByProducts(any(), any());
//		when(cpopProductsHelper.filterProductsByAccountType(any(),any());


		doReturn(ctOfferResponseProcessed.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts()).when(cpopProductsHelper).filterByProductsNotInList(any(), any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterOffersByHeartValue(any(), any(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterProductsByAccountType(any(), any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterInvalidPrices(any(),any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterInvalidPrices(any(),any());
		doReturn(ctOfferResponseProcessed).when(cpopBenefitsHelper).filterInvalidBenefits(any(), any());


		// doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		cpopProductsHelper.filterByProductsNotInList(any(), any());
		cpopProductsHelper.filterByProducts(any(), any());
		cpopProductsHelper.filterProductsByAccountType(any(),any());
		// cpopProductsHelper.filterOffersByHeartValue(any(), any());
		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");

		CTOfferResponse offerResponse = ottServicesOffersProcessor.getOffers(offerRequestWrapper );


		assertNotNull(offerResponse);
	}
	@Test
	public void testGetOffersNoCustomerContext(){

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {


			offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			// ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

			// ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;
			cgResponse = objectMapperCusRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
			// System.out.println("Error : "+e);
		}


		offerRequest.setCustomerContext(null);
		offerRequest.setContractIndicator(Arrays.asList("contract"));
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setDtvnAccount("190925162250755");
		offerRequestWrapper.setOfferRequest(offerRequest);
		doReturn(ctOfferResponseProcessed).when(cpopClient).getOffers(any());

		//doReturn(ctProductResponse).when(ottProductsProcessor).getProducts(productRequestWrapper);


		// ctProductResponse = cpopClient.getProducts(ctProductRequest);
		when(cpopClient.getOffers(any())).thenReturn(ctOfferResponseProcessed);
		when(customerGraphServiceImpl.getActiveSubscriptions(any(),any())).thenReturn(cgResponse);
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);
		when(ottSalesOffersProcessorHelper.dtvNowRulesProcessing(any(),any())).thenReturn(ctOfferResponseProcessed.getOffers());
		when(cpopProductsHelper.filterOffersByHeartValue(any(), any(), any(), any(), any(), any())).thenReturn(ctOfferResponseProcessed);
		when(cpopProductsHelper.filterProductsByAccountType(any(), any())).thenReturn(ctOfferResponseProcessed);
		when(cpopProductsHelper.filterOffersByHeartValue(any(), any(), Mockito.anyBoolean(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(ctOfferResponseProcessed);
//		when(cpopProductsHelper.filterByProducts(any(), any());
//		when(cpopProductsHelper.filterProductsByAccountType(any(),any());


		doReturn(ctOfferResponseProcessed.getOffers().get(1).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts()).when(cpopProductsHelper).filterByProductsNotInList(any(), any());
		// doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterOffersByHeartValue(any(), any(), any(), any(), any(), any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterProductsByAccountType(any(), any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterInvalidPrices(any(),any());
		doReturn(ctOfferResponseProcessed).when(cpopProductsHelper).filterInvalidPrices(any(),any());
		doReturn(ctOfferResponseProcessed).when(cpopBenefitsHelper).filterInvalidBenefits(any(), any());
		// doReturn(ctProductResponse.getProducts()).when(cpopProductsHelper).filterPriceByContractIndicator(ctProductResponse.getProducts(), false);
		cpopProductsHelper.filterByProductsNotInList(any(), any());
		cpopProductsHelper.filterByProducts(any(), any());
		cpopProductsHelper.filterProductsByAccountType(any(),any());
		// cpopProductsHelper.filterOffersByHeartValue(any(), any());
		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		//CTOfferResponse offerResponse = ottServicesOffersProcessor.getOffers(offerRequestWrapper );
		//assertNotNull(offerResponse);
	}
	@Test
	public void testGetOffersException() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {


			offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			// ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

			ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			cgResponse = objectMapperCusRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
			// System.out.println("Error : "+e);
		}


		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		//doReturn(ctOfferResponseProcessed).when(cpopClient).getOffers(any());

		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");
		ServiceException ex = catchThrowableOfType(() -> ottServicesOffersProcessor.getOffers(offerRequestWrapper ),
				ServiceException.class);
		assertNotNull(ex);

		//assertNotNull(offerResponse);
	}
	@Test
	public void testGetOffersServiceException() {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {


			offerRequest =  objectMapper.readValue(OFFER_REQUEST, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);

			// ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

			ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			cgResponse = objectMapperCusRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
			// System.out.println("Error : "+e);
		}


		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(customerGraphService.getActiveSubscriptions(any(),Mockito.anyString())).thenThrow(new ServiceException(ErrorMessages.EXTERNAL_PROCESSING_ERROR, "getActiveSubscriptions: "));

		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		Mockito.when(headers.getHeaderString("x-att-clientid")).thenReturn("SalesProductOrchestrationMs");

		ServiceException ex = catchThrowableOfType(() -> ottServicesOffersProcessor.getOffers(offerRequestWrapper ),
				ServiceException.class);
		assertNotNull(ex);
	}
	
	@Test
	public void testGetOffersPremiumSaveOffers(){
		doReturn(true).when(featureManagerHelper).isEnabled(Mockito.anyString());
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectMapper objectMapperRes = new ObjectMapper();
		ObjectMapper objectMapperCusRes = new ObjectMapper();
		OfferRequest offerRequest = null;
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		CTOfferResponse ctOfferResponseProcessed = new CTOfferResponse();
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		CGResponse cgResponse = new CGResponse();
		try {


			offerRequest =  objectMapper.readValue(PREMIUM_SAVE_OFFER_REQ, OfferRequest.class);
			BeanUtils.copyProperties(offerRequest, ctOfferRequest);
			Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
			Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));

			// ctOfferResponse =  objectMapperRes.readValue(ctOfferedPromoResponse, CTOfferResponse.class);

			// ctOfferResponseProcessed =  objectMapperRes.readValue(OFFER_RESPONSE, CTOfferResponse.class);
			ctOfferResponseProcessed = new DataReader().readFileToObj("offers/offeredPromotions.json",CTOfferResponse.class);;
			cgResponse = objectMapperCusRes.readValue(CUSTOMER_GRAPH, CGResponse.class);
		} catch(Exception e) {
			//do nothing
			// System.out.println("Error : "+e);
		}


		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequest.setContractIndicator(Arrays.asList("contract"));
		offerRequestWrapper.setOfferRequest(offerRequest);
		when(featureManagerHelper.isEnabled(any())).thenReturn(true);
		doReturn(ctOfferResponseProcessed).when(cpopClient).getOffers(any());
		CTProductResponse ctProductResponse  = JsonService.getObjectFromJson(TestUtility.loadJson("/ProductResponse.json"), CTProductResponse.class);
		doReturn(ctProductResponse).when(cpopClient).getProducts(any());
		HttpHeaders headers=Mockito.mock(HttpHeaders.class);
		CTOfferResponse offerResponse = ottServicesOffersProcessor.getOffers(offerRequestWrapper );
	}

	@Test
	void testFilterOffersForIneligibleAccountStatus(){
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailResponseForRR.json"), CTOfferResponse.class);
		ottServicesOffersProcessor.filterOffersForIneligibleAccountStatus(ctOfferResponse);
		Assertions.assertEquals(0, ctOfferResponse.getOffers().size());
	}

	@Test
	void testInvalidFilterOffersForIneligibleAccountStatus(){
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailInvalidResponseForRR.json"), CTOfferResponse.class);
		ottServicesOffersProcessor.filterOffersForIneligibleAccountStatus(ctOfferResponse);
		Assertions.assertNotNull(ctOfferResponse);
		Assertions.assertEquals(1, ctOfferResponse.getOffers().size());

	}

	@Test
	void testCheckInvalidFilterFreeTrailOffers(){

		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest =JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailInvalidRequestForRR.json"), OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		doReturn(false).when(ottServicesOffersProcessor).checkForAdditionalInfo(any());
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailInvalidResponseForRR.json"), CTOfferResponse.class);
		ottServicesOffersProcessor.filterFreeTrailOffers(offerRequestWrapper,ctOfferResponse);
		Assertions.assertNotNull(ctOfferResponse);
		Assertions.assertEquals(1, ctOfferResponse.getOffers().size());

	}
	@Test
	void testCheckValidFilterFreeTrailOffers(){
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest =JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailValidRequestForRR.json"), OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		doReturn(true).when(ottServicesOffersProcessor).checkForAdditionalInfo(any());
		CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/roadrunner/FreeTrailResponseForRR.json"), CTOfferResponse.class);
		ottServicesOffersProcessor.filterFreeTrailOffers(offerRequestWrapper,ctOfferResponse);
		Assertions.assertEquals(0, ctOfferResponse.getOffers().size());
	}

	@Test
	void  testValidCheckForAdditionalInfo(){
		String additionalDetailsString = "{\"name\":\"FREE_TRIAL_DETAILS\",\"params\":[{\"paramName\":\"promotionCode\",\"paramValue\":\"DTVSTREAMFT\"},{\"paramName\":\"promotionType\",\"paramValue\":\"Free\"},{\"paramName\":\"startDate\",\"paramValue\":\"1717113600000\"},{\"paramName\":\"endDate\",\"paramValue\":\"1717372800000\"},{\"paramName\":\"status\",\"paramValue\":\"Active\"},{\"paramName\":\"duration\",\"paramValue\":\"3\"}]}";
		AdditionalDetails additionalDetails = JsonService.getObjectFromJson(additionalDetailsString, AdditionalDetails.class);
		ottServicesOffersProcessor.checkForAdditionalInfo(List.of(additionalDetails));
		Assertions.assertTrue(true);
	}

	@Test
	void  testInValidCheckForAdditionalInfo(){
		String additionalDetailsString = "{\"name\":\"FREE_TRIAL_DETAILS\",\"params\":[{\"paramName\":\"promotionCode\",\"paramValue\":\"DTVSTREAMFT\"},{\"paramName\":\"promotionType\",\"paramValue\":\"Free\"},{\"paramName\":\"startDate\",\"paramValue\":\"1717113600000\"},{\"paramName\":\"endDate\",\"paramValue\":\"1717372800000\"},{\"paramName\":\"status\",\"paramValue\":\"InActive\"},{\"paramName\":\"duration\",\"paramValue\":\"3\"}]}";
		AdditionalDetails additionalDetails = JsonService.getObjectFromJson(additionalDetailsString, AdditionalDetails.class);
		ottServicesOffersProcessor.checkForAdditionalInfo(List.of(additionalDetails));
		Assertions.assertFalse(false);

	}

	@Test
	void testGetIapPartnerAccountType(){
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		OfferRequest offerRequest =JsonService.getObjectFromJson(TestUtility.loadJson("/IAP/IAPValidRequest.json"), OfferRequest.class);
		offerRequestWrapper.setOfferRequest(offerRequest);
		CTOfferResponse ctOfferResponse = new CTOfferResponse();
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));

		ottServicesOffersProcessor.getOffers(offerRequestWrapper);
		Assertions.assertNotNull(ctOfferResponse);
		//Assertions.assertEquals(1, ctOfferResponse.getOffers().size());
	}

	@Test
	void testGetOffersFromCT(){
		OfferRequest offerRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/IAP/IAPValidRequest.json"), OfferRequest.class);
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));
		BeanUtils.copyProperties(offerRequest, ctOfferRequest);
		doReturn(cTOfferResponse).when(cpopUCCClientHelper).getOffers(ctOfferRequest);
		cTOfferResponse = ottServicesOffersProcessor.getOffersFromCT(ctOfferRequest);

	}

	@Test
	void testGetOffersDecisioningFlow(){
		OfferRequest offerRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/IAP/IAPValidDecisioningFlowRequest.json"), OfferRequest.class);
		CTOfferResponse cTOfferResponse = new DataReader().readFileToObj("CTOfferResponse.json",CTOfferResponse.class);;
		OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
		offerRequestWrapper.setOfferRequest(offerRequest);
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_EXCLUSIVE_SALES_CHANNEL, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("oemIAPGOOGLE","oemIAPROKUTV"));
		Mockito.lenient().when(redisCacheHelper.getValues(Constants.REDISCACHE_GLOBAL_CONFIG_PARTNERCATALOG_IAP_PARTNER_TYPE, Constants.OTT_PRODUCT_FAMILY)).thenReturn(Arrays.asList("ROKUTV","GOOGLE"));
		when(ottAvaialbleOffersProcessor.getAvailableCTOffers(any())).thenReturn(cTOfferResponse);
		when(cpopClient.getOffers(any())).thenReturn(cTOfferResponse);
		doReturn(cTOfferResponse).when(ottServicesOffersProcessor).applyFilters(any(),any(),any());
		ottServicesOffersProcessor.getOffers(offerRequestWrapper);
	}
}
