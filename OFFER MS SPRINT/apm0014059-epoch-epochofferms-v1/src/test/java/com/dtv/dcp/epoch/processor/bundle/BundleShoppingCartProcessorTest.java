package com.dtv.dcp.epoch.processor.bundle;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.Benefit;
import com.dtv.dcp.epoch.model.common.LineItem;
import com.dtv.dcp.epoch.model.common.Location;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.LOBDetails;
import com.dtv.dcp.epoch.model.common.request.Losgs;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPShoppingCartHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;

public class BundleShoppingCartProcessorTest {

    /**
     * The DTVN base offers processor.
     */
    @InjectMocks
    @Spy
    BundleShoppingCartProcessor bundleShoppingCartProcessor;

    /**
     * The DTVN base offers processor.
     */
    @Mock
    CPOPShoppingCartHelper offersHelper;

    /**
     * The CpopClient.
     */
    @Mock
    CpopClient cpopClient;


    @Mock
    FeatureManagerHelper featureManagerHelper;

    private static final String successResponse = "{\r\n  \"messages\": [\r\n    {\r\n      \"status\": \"success\",\r\n      \"type\": \"coupon\",\r\n      \"id\": \"123\",\r\n      \"conflicts\": [\r\n        {\r\n          \"type\": \"promotion\",\r\n          \"id\": \"201\"\r\n        }\r\n      ]\r\n    }\r\n  ],\r\n  \"context\": {},\r\n  \"dtvnowCart\": {\r\n    \"customerType\": \"consumer\",\r\n    \"losgs\": [\r\n      {\r\n        \"losgId\": \"losg_dtvnow_681490466\",\r\n        \"losgType\": \"NEW\",\r\n        \"lineItems\": [\r\n          {\r\n            \"lineItemId\": \"\",\r\n            \"offerId\": \"7492324434\",\r\n            \"productType\": \"PLAN\",\r\n            \"action\": \"ADD\",\r\n            \"billingCode\": \"BASE-XTRA-201811\",\r\n            \"productSKU\": \"7492324350\",\r\n            \"promotionReferences\": [\r\n              \"201\"\r\n            ]\r\n          },\r\n          {\r\n            \"lineItemId\": \"\",\r\n            \"offerId\": \"7456863852\",\r\n            \"productType\": \"ADDON\",\r\n            \"action\": \"ADD\",\r\n            \"billingCode\": \"BOLT-HBO-201610\",\r\n            \"productSKU\": \"7456863842\",\r\n            \"promotionReferences\": [\r\n              \"201\"\r\n            ]\r\n          },\r\n          {\r\n            \"lineItemId\": \"\",\r\n            \"offerId\": \"7456863849\",\r\n            \"productType\": \"ADDON\",\r\n            \"action\": \"ADD\",\r\n            \"billingCode\": \"BOLT-CDVRINCL-201710\",\r\n            \"productSKU\": \"7456863846\"\r\n          }\r\n        ]\r\n      }\r\n    ],\r\n    \"promotions\": [\r\n      {\r\n        \"id\": \"201\",\r\n        \"promotionBillingCode\": \"PLUS07\",\r\n        \"promoName\": \"15OFF12MONTHS\",\r\n        \"effectiveDate\": \"2019-04-01\",\r\n        \"promotionType\": \"PROMOTION\"\r\n      },\r\n      {\r\n        \"id\": \"202\",\r\n        \"promotionBillingCode\": \"DHBOF\",\r\n        \"promotionType\": \"PROMOTION\"\r\n      },\r\n      {\r\n        \"id\": \"203\",\r\n        \"promotionBillingCode\": \"ABCD\",\r\n        \"promotionType\": \"PROMOTION\",\r\n        \"displayName\": \"\"\r\n      }\r\n    ],\r\n    \"rewards\": [\r\n      {\r\n        \"id\": \"123\",\r\n        \"code\": \"CinemaxFree1Month\",\r\n        \"type\": \"multi-use\",\r\n        \"isQuotaBased\": false,\r\n        \"benefitId\": \"203\"\r\n      }\r\n    ]\r\n  }\r\n}";
    private static final String errorResponsewithev8349 = "{\r\n  \"error\": {\r\n    \"errorId\": \"eV8349\",\r\n    \"message\": \"Customer is ineligible for the reward\"\r\n  }\r\n}";
    private static final String errorResponsewithev8350 = "{\r\n  \"error\": {\r\n    \"errorId\": \"eV8350\",\r\n    \"message\": \"Customer is ineligible for the reward\"\r\n  }\r\n}";
    private static final String errorResponsewithev8351 = "{\r\n  \"error\": {\r\n    \"errorId\": \"eV8351\",\r\n    \"message\": \"Customer is ineligible for the reward\"\r\n  }\r\n}";
    private static final String errorResponsewithev8355 = "{\r\n  \"error\": {\r\n    \"errorId\": \"eV8355\",\r\n    \"message\": \"Customer is ineligible for the reward\"\r\n  }\r\n}";

    private static final String SAMPLE_REQUEST_REWARD_DTVN = "{\"context\":{\"channel\":\"Online\"},\"dtvnowCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_itvt_408442922\":{\"id\":\"losg_itvt_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
    private static final String SAMPLE_REQUEST_REWARD_DTVS = "{\"context\":{\"channel\":\"Opus\"},\"dtvSatelliteCart\":{\"lobType\":\"DTVS\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_dtvs_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
    private static final String SAMPLE_REQUEST_REWARD_BROADBAND = "{\"context\":{\"channel\":\"Opus\"},\"broadbandCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_broadband_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
    private static final String SAMPLE_REQUEST_REWARD_IPVT = "{\"context\":{\"channel\":\"Online\"},\"iptvCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_itvt_408442922\":{\"id\":\"losg_itvt_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";

    /**
     * Setup.
     */
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    public void testValidateShoppingCart() throws Exception {
    	
      CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(successResponse, CTShoppingCartResponse.class);
      //when(cpopClient.validateShoppingCart(Mockito.any(), false)).thenReturn(ctShoppingCartResponse);
      OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
      OfferValidationResponse offerValidationResponse =  bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
      assertNotNull(offerValidationResponse);
    	
    }
    
    @Test
    public void testCreateCTShoppingCartRequest() throws Exception {
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
    	CTShoppingCartRequest offerValidationResponse =  bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(offerValidationResponse);
    }
    
    @Test
    public void testValidateShoppingCartWithValidResponse() throws Exception {
    	
      CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(successResponse, CTShoppingCartResponse.class);
      when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
      OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
      OfferValidationResponse offerValidationResponse =  bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
      assertNotNull(offerValidationResponse);
    	
    }
      
    @Test
    public void testValidateCPOPVBBSateliteOfferSuccess() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(successResponse, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBBroadbandOfferSuccess() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(successResponse, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_BROADBAND);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBIPTVOfferSuccess() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(successResponse, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_IPVT);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBOfferErrorEV8349() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(errorResponsewithev8349, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVN);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBOfferErrorEV8350() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(errorResponsewithev8350, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVN);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBOfferErrorEV8351() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(errorResponsewithev8351, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVN);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    
    @Test
    public void testValidateCPOPVBBOfferErrorEV8355() throws Exception {
        CTShoppingCartResponse ctShoppingCartResponse = JsonService.getObjectFromJson(errorResponsewithev8355, CTShoppingCartResponse.class);
        when(cpopClient.validateShoppingCart(Mockito.any(), Mockito.anyBoolean())).thenReturn(ctShoppingCartResponse);
        OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVN);
        CTShoppingCartRequest ctShoppingCartRequest = bundleShoppingCartProcessor.createCTShoppingCartRequest(offerValidationRequest);
        assertNotNull(ctShoppingCartRequest);
        OfferValidationResponse offerValidationResponse = bundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, new OfferValidationResponse());
        assertNotNull(offerValidationResponse);
    }

    @Test
    public void testValidateCPOPVBBOfferExceptionBlock() throws Exception {

        ServiceException ex = catchThrowableOfType(() -> bundleShoppingCartProcessor.validateShoppingCart(Mockito.any(), Mockito.any()),
        		ServiceException.class);

        assertNotNull(ex);
    }

    private OfferValidationRequest buildOfferValidationRequest(String request) {
        return JsonService.getObjectFromJson(request, OfferValidationRequest.class);
    }
    
    @Test
	public void testConstructCTCoolOffPeriodResponse() {
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
		CartContext cartContext = new CartContext();
		cartContext.setSalesChannel("opus");
		cartContext.setValidateOnlyReward(false);
		cartContext.setValidateAvailableQuota(true);
		Location location = new Location();
		location.setZipCode("75093");
		location.setDma("sanantonio");
		location.setCity("sanantonio");
		location.setState("TX");
		location.setCounty("bexar");
		location.setRegion("central");
		cartContext.setLocation(location);
		List<LOBDetails> lobDetailsList = new ArrayList<LOBDetails>();
		LOBDetails lobDetails = new LOBDetails();
		lobDetails.setBusinessSegment("CRU");
		lobDetails.setCustomerSegement("Residential");
		lobDetails.setOfferActionType("Acquisition");
		lobDetails.setProductFamily("OTT");
		lobDetails.setLobType("OTT");
		List<Losgs> ctLosgsList = new ArrayList<Losgs>();
		List<Benefit> ctBenefits = new ArrayList<Benefit>();
		Losgs ctlosgs = new Losgs();
		List<LineItem> ctLineItems = new ArrayList<LineItem>();
		List<String> offerCodes = new ArrayList<String>();
		offerCodes.add("REWARD_OFFER");
		offerCodes.add("COOL_OFF_OFFER");
		LineItem item = new LineItem();
		item.setOfferCodes(offerCodes);
		item.setBillingProductCode("REWARD_OFFER_PR");
		item.setProductType("video-plan");
		Benefit ctBenefit = new Benefit();
		ctBenefit.setBillingBenefitId("23645");
		ctBenefit.setBillingBenefitCode("REWARD_BENEFIT");
		ctBenefit.setPromotionType("PROMOTION");
		ctBenefit.setOfferCode("REWARD_OFFER");
		ctBenefits.add(ctBenefit);
		item.setBenefits(ctBenefits);
		ctLineItems.add(item);
		ctlosgs.setLineItems(ctLineItems);
		ctLosgsList.add(ctlosgs);
		lobDetails.setLosgs(ctLosgsList);
		lobDetailsList.add(lobDetails);
		cartContext.setLobDetails(lobDetailsList);
		offerValidationRequest.setCartContext(cartContext);
		CustomerContext customerContext = new CustomerContext();
		List<String> existingAccounts = new ArrayList<String>();
		existingAccounts.add("DTVNOW");
		customerContext.setExistingAccounts(existingAccounts);
		CartProduct ott = new CartProduct();
		ott.setAccountNumber("123456789");
		ott.setProductFamily("OTT");
		ott.setIsContracted(Boolean.TRUE);
		List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
		ProductInfo info = new ProductInfo();
		info.setBillingProductCode("test_internetPlan456");
		info.setProductType("video-plan");
		info.setProductId("id1");
		info.setStreamType("two_stream");
		productInfos.add(info);
		ott.setProducts(productInfos);
		List<Benefit> benefits = new ArrayList<Benefit>();
		Benefit benefit = new Benefit();
		benefit.setBillingBenefitId("23645");
		benefit.setBillingBenefitCode("benefit1");
		benefit.setPromotionType("PROMOTION");
		benefit.setStartDate("2019-12-12");
		benefit.setEndDate("2020-5-5");
		benefits.add(benefit);
		ott.setBenefits(benefits);
		ott.setAccountStatus("active");
		customerContext.setOtt(ott);
		offerValidationRequest.setCustomerContext(customerContext);
		CTShoppingCartResponse shoppingCartResponse = new CTShoppingCartResponse();
		bundleShoppingCartProcessor.constructCTCoolOffPeriodResponse(offerValidationRequest, shoppingCartResponse);
	}
}
