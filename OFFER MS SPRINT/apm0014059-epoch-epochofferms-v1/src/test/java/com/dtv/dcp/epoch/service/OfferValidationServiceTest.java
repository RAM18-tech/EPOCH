/**
 * 
 */
package com.dtv.dcp.epoch.service;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;

/**
 * @author dr000y
 *
 */
@ExtendWith(MockitoExtension.class)
public class OfferValidationServiceTest {

	@InjectMocks
	private OfferValidationServiceImpl offerValidationService;

	@Mock
    BundleOfferProcessor BundleOfferProcessor;

	@Mock
	CPOPAdditionalOfferHelper dtvNowCpopAdditionalOfferHelper;

	@Mock
	FeatureManagerHelper featureManagerHelper;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testOfferValidation() throws Exception {

		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);
	}

	@Test
	public void testRewardValidationOnline() throws Exception {

		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_ONLINE);
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		String channel = "Online";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		Mockito.lenient().when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		when(dtvNowCpopAdditionalOfferHelper.isRewardCapabilityEnabled(channel)).thenReturn(true);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);
	}

	@Test
	public void testRewardValidationOPUS() throws Exception {

		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_OPUS);
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		String channel = "Opus";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		Mockito.lenient().when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		when(dtvNowCpopAdditionalOfferHelper.isRewardCapabilityEnabled(channel)).thenReturn(true);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);

	}
	
	@Test
	public void testRewardValidationDTVSatelite() throws Exception {

		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		String channel = "Online";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		Mockito.lenient().when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		Mockito.lenient().when(dtvNowCpopAdditionalOfferHelper.isRewardCapabilityEnabled(channel)).thenReturn(true);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);
	}
	
	@Test
	public void testRewardValidationBroadband() throws Exception {

		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_BROADBAND);
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		String channel = "Online";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		Mockito.lenient().when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		Mockito.lenient().when(dtvNowCpopAdditionalOfferHelper.isRewardCapabilityEnabled(channel)).thenReturn(true);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);
	}
	@Test
	public void testRewardValidationIPTV() throws Exception {

		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_IPVT);
		OfferValidationResponse offerValidationRespons = mock(OfferValidationResponse.class);
		String sessionId = "123456";
		String channel = "Online";
		// mock Service
		Mockito.lenient().when(BundleOfferProcessor.offerValidation(offerValidationRequest)).thenReturn(offerValidationRespons);
		Mockito.lenient().when(featureManagerHelper.isEnabled(CpopConstants.EPOCH_REWARDS_ENABLED)).thenReturn(true);
		when(dtvNowCpopAdditionalOfferHelper.isRewardCapabilityEnabled(channel)).thenReturn(true);
		OfferValidationResponse response = offerValidationService.offerValidation(offerValidationRequest, sessionId);
		assertNotNull(response);
	}

	private OfferValidationRequest buildOfferValidationRequest(String request) {
		return JsonService.getObjectFromJson(request, OfferValidationRequest.class);
	}

	private static final String SAMPLE_REQUEST_REWARD_OPUS = "{\r\n    \"context\": {\r\n\t\t\"channel\": \"Opus\"\r\n\t},\r\n    \"dtvnowCart\": {\r\n        \"lobType\": \"DTVNOW\",\r\n        \"businessType\": \"consumer\",\r\n        \"losgs\": {\r\n            \"losg_dtvnow_408442922\": {\r\n                \"id\": \"losg_dtvnow_408442922\",\r\n                \"lineItems\": {\r\n                    \"138664750\": {\r\n                        \"itemType\": \"BASE\",\r\n                        \"id\": 138664750,\r\n                        \"productSKU\": 768336608,\r\n                        \"billingCode\": \"BASE-MAX-2018\",\r\n                        \"productType\": \"PLAN\",\r\n                        \"promotionReferences\": [\r\n                            \"PROMO_REWARD_0\"\r\n                        ]\r\n                    },\r\n                    \"138664751\": {\r\n                        \"itemType\": \"ADDON\",\r\n                        \"productSKU\": 718118360,\r\n                        \"billingCode\": \"BOLT-CDVRINCL-201710\",\r\n                        \"productType\": \"INCLUDED_FEATURE\",\r\n                        \"promotionReferences\": []\r\n                    }\r\n                }\r\n            }\r\n        },\r\n        \"promotions\": [\r\n            {\r\n                \"id\": \"PROMO_REWARD_0\",\r\n                \"promotionId\": \"15REWARD\",\r\n                \"promotionName\": \"15Reward\",\r\n                \"promotionType\": \"REWARD\",\r\n                \"promotionBillingCode\":\"15$OFFReward\",\r\n                \"offerId\":138664750\r\n            },\r\n            {\r\n                \"id\": \"PROMO_123\",\r\n                \"promotionId\": 123,\r\n                \"promotionName\": \"30 Off 2 Months\",\r\n                \"promotionType\": \"PROMOTION\"\r\n            }\r\n        ]\r\n    }\r\n}";
	private static final String SAMPLE_REQUEST_REWARD_ONLINE = "{\r\n    \"context\": {\r\n\t\t\"channel\": \"Online\"\r\n\t},\r\n    \"dtvnowCart\": {\r\n        \"lobType\": \"DTVNOW\",\r\n        \"businessType\": \"consumer\",\r\n        \"losgs\": {\r\n            \"losg_dtvnow_408442922\": {\r\n                \"id\": \"losg_dtvnow_408442922\",\r\n                \"lineItems\": {\r\n                    \"138664750\": {\r\n                        \"itemType\": \"BASE\",\r\n                        \"id\": 138664750,\r\n                        \"productSKU\": 768336608,\r\n                        \"billingCode\": \"BASE-MAX-2018\",\r\n                        \"productType\": \"PLAN\",\r\n                        \"promotionReferences\": [\r\n                            \"PROMO_REWARD_0\"\r\n                        ]\r\n                    },\r\n                    \"138664751\": {\r\n                        \"itemType\": \"ADDON\",\r\n                        \"productSKU\": 718118360,\r\n                        \"billingCode\": \"BOLT-CDVRINCL-201710\",\r\n                        \"productType\": \"INCLUDED_FEATURE\",\r\n                        \"promotionReferences\": []\r\n                    }\r\n                }\r\n            }\r\n        },\r\n        \"promotions\": [\r\n            {\r\n                \"id\": \"PROMO_REWARD_0\",\r\n                \"promotionId\": \"15REWARD\",\r\n                \"promotionName\": \"15Reward\",\r\n                \"promotionType\": \"REWARD\",\r\n                \"promotionBillingCode\":\"15$OFFReward\",\r\n                \"offerId\":138664750\r\n            },\r\n            {\r\n                \"id\": \"PROMO_123\",\r\n                \"promotionId\": 123,\r\n                \"promotionName\": \"30 Off 2 Months\",\r\n                \"promotionType\": \"PROMOTION\"\r\n            }\r\n        ]\r\n    }\r\n}";
    private static final String SAMPLE_REQUEST_REWARD_DTVS = "{\"context\":{\"channel\":\"Opus\"},\"dtvSatelliteCart\":{\"lobType\":\"DTVS\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_dtvs_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
    private static final String SAMPLE_REQUEST_REWARD_BROADBAND= "{\"context\":{\"channel\":\"Opus\"},\"broadbandCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_broadband_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";    
    private static final String SAMPLE_REQUEST_REWARD_IPVT = "{\"context\":{\"channel\":\"Online\"},\"iptvCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_itvt_408442922\":{\"id\":\"losg_itvt_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
}