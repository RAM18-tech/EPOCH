package com.dtv.dcp.epoch.processor.helper;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

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
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.util.JsonService;

public class CPOPShoppingCartHelperTest {

	@InjectMocks
	CPOPShoppingCartHelper cpopShoppingCartHelper;
	
	/**
	 * Setup.
	 */
	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}
	
	private static final String SAMPLE_REQUEST_REWARD_DTVN ="{\"context\":{\"channel\":\"Online\",\"location\":{\"zipCode\":\"75038\",\"dma\":\"500\",\"city\":\"Irving\",\"state\":\"TX\",\"county\":\"dallas\",\"region\":\"Central\"}},\"dtvnowCart\":{\"lobType\":\"OTT\",\"businessType\":\"consumer\",\"losgs\":{\"losg_itvt_408442922\":{\"id\":\"losg_itvt_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
	private static final String SAMPLE_REQUEST_REWARD_DTVS = "{\"context\":{\"channel\":\"Online\",\"location\":{\"zipCode\":\"75038\",\"dma\":\"500\",\"city\":\"Irving\",\"state\":\"TX\",\"county\":\"dallas\",\"region\":\"Central\"}},\"dtvSatelliteCart\":{\"lobType\":\"DTVS\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_dtvs_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
    private static final String SAMPLE_REQUEST_REWARD_BROADBAND= "{\"context\":{\"channel\":\"Online\",\"location\":{\"zipCode\":\"75038\",\"dma\":\"500\",\"city\":\"Irving\",\"state\":\"TX\",\"county\":\"dallas\",\"region\":\"Central\"}},\"broadbandCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_dtvnow_408442922\":{\"id\":\"losg_broadband_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";    
    private static final String SAMPLE_REQUEST_REWARD_IPVT = "{\"context\":{\"channel\":\"Online\",\"location\":{\"zipCode\":\"75038\",\"dma\":\"500\",\"city\":\"Irving\",\"state\":\"TX\",\"county\":\"dallas\",\"region\":\"Central\"}},\"iptvCart\":{\"lobType\":\"BROADBAND\",\"businessType\":\"consumer\",\"losgs\":{\"losg_itvt_408442922\":{\"id\":\"losg_itvt_408442922\",\"lineItems\":{\"138664750\":{\"itemType\":\"BASE\",\"id\":138664750,\"productSKU\":768336608,\"billingCode\":\"BASE-MAX-2018\",\"productType\":\"PLAN\",\"promotionReferences\":[\"PROMO_REWARD_0\"]},\"138664751\":{\"itemType\":\"ADDON\",\"productSKU\":718118360,\"billingCode\":\"BOLT-CDVRINCL-201710\",\"productType\":\"INCLUDED_FEATURE\",\"promotionReferences\":[]}}}},\"promotions\":[{\"id\":\"PROMO_REWARD_0\",\"promotionId\":\"15REWARD\",\"promotionName\":\"15Reward\",\"promotionType\":\"REWARD\",\"promotionBillingCode\":\"15$OFFReward\",\"offerId\":138664750},{\"id\":\"PROMO_123\",\"promotionId\":123,\"promotionName\":\"30 Off 2 Months\",\"promotionType\":\"PROMOTION\"}]}}";
	
    @Test
	public void testConstructCTDTVSatelliteRequest(){
		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVS);
		CTShoppingCartRequest ctShoppingCartRequest = cpopShoppingCartHelper.constructCTDTVSatelliteRequest(offerValidationRequest, new CTShoppingCartRequest());
		assertNotNull(ctShoppingCartRequest);
	}
	
	@Test
	public void testConstructCTDTVNowRequest(){
		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_DTVN);
		CTShoppingCartRequest ctShoppingCartRequest = cpopShoppingCartHelper.constructCTDTVNowRequest(offerValidationRequest, new CTShoppingCartRequest());
		assertNotNull(ctShoppingCartRequest);
	}
	
	@Test
	public void testConstructCTIptvRequest(){
		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_IPVT);
		CTShoppingCartRequest ctShoppingCartRequest = cpopShoppingCartHelper.constructCTIptvRequest(offerValidationRequest, new CTShoppingCartRequest());
		assertNotNull(ctShoppingCartRequest);
	}
	@Test
	public void testConstructCTBroadbandRequest(){
		OfferValidationRequest offerValidationRequest = buildOfferValidationRequest(SAMPLE_REQUEST_REWARD_BROADBAND);
		CTShoppingCartRequest ctShoppingCartRequest = cpopShoppingCartHelper.constructCTBroadbandRequest(offerValidationRequest, new CTShoppingCartRequest());
		assertNotNull(ctShoppingCartRequest);
	}
	
	private OfferValidationRequest buildOfferValidationRequest(String request) {
		return JsonService.getObjectFromJson(request,OfferValidationRequest.class);
	}
	
	
	@Test
	public void testconstructCTCoolOffPeriodRequest(){
		OfferValidationRequest offerValidationRequest = new OfferValidationRequest();
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
		offerValidationRequest.setCustomerContext(customerContext);
		customerContext.setOtt(ott);
		
		
		CartContext cartContext= new CartContext();
		cartContext.setCpopOfferCodes(Stream.of("OF_BASE-XTRA-201811_contract").collect(Collectors.toList()));
		cartContext.setSalesChannel("TEST");
		cartContext.setValidateOnlyReward(true);
		cartContext.setValidateAvailableQuota(true);

		Location location = new Location();
		location.setZipCode("256645");
		location.setDma("21576245");
		location.setCity("London");
		location.setState("California");
		location.setCounty("USA");
		location.setRegion("Central");
		cartContext.setLocation(location);
		
		List<LOBDetails> lobDetailsList = new ArrayList<LOBDetails>();

		LOBDetails lobDetails = new LOBDetails();
		lobDetails.setBusinessSegment("CONS");
		lobDetails.setCustomerSegement("Residential");
		lobDetails.setOfferActionType("Acquistion");
		lobDetails.setProductFamily("ATT&TV");
		lobDetails.setLobType("test");
		
		List<Losgs> losgsList = new ArrayList<Losgs>();
		
		Losgs losgs = new Losgs();
		
		List<LineItem> lineItems = new ArrayList<LineItem>();
		
		LineItem lineItem = new LineItem();
		
		lineItem.setOfferCodes(Stream.of("OF_BASE-XTRA-201811_contract").collect(Collectors.toList()));
		lineItem.setBillingProductCode("TEST_CODE");
		lineItem.setProductType("OTT");
		
		List<Benefit> benefits1 = new ArrayList<Benefit>();
		
		Benefit benefit1 = new Benefit();
		
		benefit.setBillingBenefitId("YDYLSGY");
		benefit.setBillingBenefitCode("TESTCODE");
		benefit.setPromotionType("free-promo");
		benefit.setOfferCode("OF_BASE-XTRA-201811_contract");
		
		benefits1.add(benefit1);
		
		lineItem.setBenefits(benefits1);
		
		lineItems.add(lineItem);
		
		losgs.setLineItems(lineItems);
		
		losgsList.add(losgs);
		
		lobDetails.setLosgs(losgsList);
		
		lobDetailsList.add(lobDetails);
		
		cartContext.setLobDetails(lobDetailsList);
		
		offerValidationRequest.setCartContext(cartContext);

		CTShoppingCartRequest shoppingCartRequest = new CTShoppingCartRequest();
		CTShoppingCartRequest ctShoppingCartRequest = cpopShoppingCartHelper.constructCTCoolOffPeriodRequest(offerValidationRequest, shoppingCartRequest);
		System.out.println(ctShoppingCartRequest);
		assertNotNull(ctShoppingCartRequest.getCustomerContext());
		assertNotNull(ctShoppingCartRequest.getCustomerContext().getOtt());
		assertNotNull(ctShoppingCartRequest.getCustomerContext().getOtt().getProducts());
		assertNotNull(ctShoppingCartRequest.getCustomerContext().getOtt().getBenefits());
	}
	
}
