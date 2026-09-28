package com.dtv.dcp.epoch.integration.common;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.BuildMiddlewareCTFilters;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.request.BenefitCodesToSuppressTheOffer;
import com.dtv.dcp.epoch.model.ct.request.CTChannelEligibility;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

public class BuildMiddlewareCTFiltersTest {

	@InjectMocks
	BuildMiddlewareCTFilters buildMiddlewareCTFilters;
	
	@Mock
	private SatelliteCTOffersProcessor satelliteCTOffersProcessor;
	
	@Mock
	private FeatureManagerHelper featureManagerHelper;
	@Mock
	private OffersUtils offersUtils;

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
	            "    \"creditRisk\": \"UNKNOWN\"\n" +
	            "}";

	 /**
	     * Setup.
	     */
	    @BeforeEach
	    public void setup() {
	        MockitoAnnotations.openMocks(this);
	    }
	    
	/**
	 * Setup.
	 */
	/*
	 * @BeforeEach public void init() throws Exception { restTemplateBeanFactory =
	 * Mockito.mock(RestTemplateBeanFactory.class); restTemplate =
	 * Mockito.mock(RestTemplate.class);
	 * Mockito.when(restTemplateBeanFactory.getObject(ArgumentMatchers.anyString()))
	 * .thenReturn(restTemplate);
	 * 
	 * MockitoAnnotations.openMocks(this); //
	 * ReflectionTestUtils.setField(cpopBackupClient, "pageLimit", 300); try { //
	 * FieldUtils.writeField(cpopBackupClient, "cpcBaseURL", "http://localhost:80",
	 * // true); ReflectionTestUtils.setField(cpopBackupClient, "pageLimit", 10); }
	 * catch (Exception e) { } }
	 */
	
    //@Test
    public void testFilters() {
    	
    	CTOfferRequest ctOfferRequest = new CTOfferRequest();
		ctOfferRequest.setOfferActionType(Arrays.asList(Constants.ACQUISITION));
		ctOfferRequest.setCreditRisk("UNKNOWN");
    	CTOffer ctOffer = new CTOffer();
		OfferAttributes attributes = new OfferAttributes();
		attributes.setOfferProductSubtype(Constants.BASE);
		ctOffer.setAttributes(attributes);
		List<CartOffer> cartOffers = new ArrayList<>();
        List<Product> cartProducts = new ArrayList<>();
        
		CartContext cartContext = new CartContext();
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BASE-ULTIMATE-ALL-INCLUDED-NO-LOCALS_satellite");
        cartOffer.setQuantity(1);
        cartOffers.add(cartOffer);
        Product cartProduct = new Product();
        
        cartProduct.setProductType(Constants.VIDEO_PLAN);
        cartProducts.add(cartProduct);
        cartContext.setCartOffers(cartOffers);
      
		
		CTOfferResponse ctSelectedOffersResponse=new DataReader().readFileToObj("enabler/premiumPick_response.json", CTOfferResponse.class);
		List<String> qualifyingProductBillingCodes = new ArrayList<>();
		List<String> deleteSpecialOffer = new ArrayList<>();
		Product prod = new Product();
		List<InstallmentInfo> installmentList = new ArrayList<>();
		InstallmentInfo installmentInfo = new InstallmentInfo();

		List<InCompatibleProductValue> creditRisk = new ArrayList<>();
		InCompatibleProductValue value = new InCompatibleProductValue();
		value.setKey("UNKNOWN");
		value.setLabel("UNKNOWN");
		installmentInfo.setCreditRiskEligibility(creditRisk);
		List<String> contract = new ArrayList<String>();
		contract.add("EDSP");
		installmentInfo.setContractApplicable(contract);
		installmentList.add(installmentInfo);
		ctSelectedOffersResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts()
				.get(0).getProducts().get(0).getObj().getVariants().get(0).getAttributes()
				.setInstallmentList(installmentList);
		 buildMiddlewareCTFilters.populateInstallmentCreditRisk(ctOfferRequest,ctSelectedOffersResponse.getOffers().get(0));
        
        List<String> offerProductFamily = new ArrayList<>();
        offerProductFamily.add("OTT");
        ctOfferRequest.setOfferProductFamily(offerProductFamily);        
        ctOfferRequest.setContractIndicator(contract);
        buildMiddlewareCTFilters.populateInstallmentAndMinMaxQuantityEDSP(ctOfferRequest,ctSelectedOffersResponse.getOffers().get(0));
        
        List<String> salesChannel = new ArrayList<>();
        salesChannel.add("directIntegrationPartner");        
        ctOfferRequest.setSalesChannel(salesChannel);
        CTChannelEligibility channelEligibility = new CTChannelEligibility();
        channelEligibility.setSalesChannel("indirectSales");
        channelEligibility.setSalesSubChannel("DTVCALLCTR");
        channelEligibility.setLocationId("118724");
        channelEligibility.setLocationTypeId("4");
        channelEligibility.setDealerId("1");
        channelEligibility.setDealerCode("OKW7J");
        channelEligibility.setMasterDealerId("M100");
        channelEligibility.setDirectIntegrationPartnerName("CHUZO");
		ctOfferRequest.setChannelEligibility(channelEligibility);
		List<String> data = new ArrayList<>();
		data.add(channelEligibility.getDirectIntegrationPartnerName());
		ctSelectedOffersResponse.getOffers().get(0).getAttributes().setDirectIntegrationPartnerName(data);
		ctSelectedOffersResponse.getOffers().get(1).getAttributes().setDirectIntegrationPartnerName(data);
		ctSelectedOffersResponse.getOffers().get(2).getAttributes().setDirectIntegrationPartnerName(data);
		ctSelectedOffersResponse.getOffers().get(3).getAttributes().setDirectIntegrationPartnerName(data);
		data = new ArrayList<>();
		data.add(channelEligibility.getDealerCode());
		ctSelectedOffersResponse.getOffers().get(0).getAttributes().setDealerCode(data);
		data = new ArrayList<>();
		data.add(channelEligibility.getDealerId());
		ctSelectedOffersResponse.getOffers().get(1).getAttributes().setDealerIds(data);
		
		data = new ArrayList<>();
		data.add(channelEligibility.getMasterDealerId());
		ctSelectedOffersResponse.getOffers().get(2).getAttributes().setMasterDealerId(data);
		
		data = new ArrayList<>();
		data.add(channelEligibility.getSalesSubChannel());
		ctSelectedOffersResponse.getOffers().get(3).getAttributes().setSalesSubChannel(data);
		
        buildMiddlewareCTFilters.filterOffersForDirectIntegrationPartners(ctSelectedOffersResponse,ctOfferRequest);
        
        offerProductFamily = new ArrayList<>();
        offerProductFamily.add("satellite");
        ctOfferRequest.setOfferProductFamily(offerProductFamily);  
        buildMiddlewareCTFilters.filterOffersForDirectIntegrationPartners(ctSelectedOffersResponse,ctOfferRequest);
        
        
   
        buildMiddlewareCTFilters.filterChannelEligibilityOffersForSatellite(ctSelectedOffersResponse,ctOfferRequest);
        
        buildMiddlewareCTFilters.filterCorrectPrice(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.filterDeviceOffersBasedOnMigrationIndicator(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.applyCartContextFilter(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.applySatelliteCartOffersFilter(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.deliveryMethodByChannelFilterOffer(ctSelectedOffersResponse,ctOfferRequest);
        
        List<BenefitCodesToSuppressTheOffer> benefitSupress = new ArrayList<>();
        BenefitCodesToSuppressTheOffer benefit = new BenefitCodesToSuppressTheOffer();
        benefit.setBenefitCode("TEST");
        benefit.setEndDate("01-01-2020");
        benefit.setStartDate("01-01-2023");
		benefitSupress.add(benefit );
		ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitSupress);
		List<String> benefitCodes =  new ArrayList<>();
		benefitCodes.add("TEST");
		ctSelectedOffersResponse.getOffers().get(0).getAttributes().setBenefitCodes(benefitCodes);
		ctSelectedOffersResponse.getOffers().get(0).getAttributes().setCoolOffPeriod(-1);
        buildMiddlewareCTFilters.evaluateBenefitsCodestoSuppressTheOffer(ctSelectedOffersResponse,ctOfferRequest);
        
        buildMiddlewareCTFilters.populateReconnectOffers(ctSelectedOffersResponse,ctOfferRequest,qualifyingProductBillingCodes,ctSelectedOffersResponse.getOffers().get(0));
        buildMiddlewareCTFilters.populateDeleteSpecialOffer(qualifyingProductBillingCodes,ctSelectedOffersResponse.getOffers().get(0),deleteSpecialOffer);
        buildMiddlewareCTFilters.filterSpecialOfferForReconnect(ctSelectedOffersResponse,ctSelectedOffersResponse.getOffers().get(0));
        buildMiddlewareCTFilters.filterSpecialOffer(ctSelectedOffersResponse,deleteSpecialOffer);
        buildMiddlewareCTFilters.extractOfferWithBusinessSegment(ctOfferRequest,prod);
        
        ctOfferRequest.setAdditionalOfferType("reward");
        buildMiddlewareCTFilters.evaluateRewardCards(ctSelectedOffersResponse,ctOfferRequest);
        
        buildMiddlewareCTFilters.extractBusinessSegment(ctSelectedOffersResponse,ctOfferRequest);
        
        salesChannel.add("oemIAPROKUTV");        
        ctOfferRequest.setSalesChannel(salesChannel);
        CTOffer offer = ctSelectedOffersResponse.getOffers().get(0);
        GenericByKey desc = new GenericByKey();
        desc.setOemLongDescriptionRokuTv("rokuLongDesc");
        desc.setOemShortDescriptionRokuTv("rokuShortDesc");
        
        desc.setOemLongDescriptionFireTv("rokuLongDesc");
        desc.setOemShortDescriptionFireTv("rokuShortDesc");
		offer.getAttributes().setDescriptionsByKey(desc);
        buildMiddlewareCTFilters.populateOEMDescriptionsByKey(ctOfferRequest,ctSelectedOffersResponse.getOffers().get(0));
        
        buildMiddlewareCTFilters.returnValidOpusStoreOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.filterOffersForChannelEligibilityByKey(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.returnValidAgentOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.returnValidPartnerDealersOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.returnValidOnlinePartnerOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.removeIneligiblePartnerOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.removeIneligibleAccountStatus(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.filterDeviceOffersBasedOnMigrationIndicator(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.removeComplatibleProductsFromResponse(ctOfferRequest,ctSelectedOffersResponse.getOffers().get(0));
        
        buildMiddlewareCTFilters.removeinstallmentListForEmployee(ctOfferRequest,ctSelectedOffersResponse.getOffers().get(0));
        buildMiddlewareCTFilters.returnValidPartnerTypeOffers(ctSelectedOffersResponse,ctOfferRequest);
        buildMiddlewareCTFilters.productsOnAccountToSupressOffer(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.applyCartContextFilter(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.applySatelliteCartOffersFilter(ctSelectedOffersResponse, ctOfferRequest);
        
        List<ProductInfo> cartProductsReq = new ArrayList<>();
		ctOfferRequest.setCartProducts(cartProductsReq);
        buildMiddlewareCTFilters.filterOffersByPolicy(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.filterOffersByPPCBillingRefId(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.filterOffersByBillingRefId(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.filterOffersByComponentCode(ctSelectedOffersResponse, ctOfferRequest);
        buildMiddlewareCTFilters.filterOffersByModelNumber(ctSelectedOffersResponse, ctOfferRequest);
       
        buildMiddlewareCTFilters.getComponentCodeFromBillingParams(new Attributes());
        buildMiddlewareCTFilters.getOEMchannel(ctOfferRequest);
        
        buildMiddlewareCTFilters.priceProtect(ctSelectedOffersResponse, ctOfferRequest);
       
        
        List<Price> filteredPrices = new ArrayList<Price>();
        Price price = new Price();
        price.setStartDate("01/02/2021");
        price.setEndDate("01/02/2025");
        price.setContractIndicator(OFFER_REQUEST);
        
        price.setContractIndicator("EDSP");
        filteredPrices.add(price);
        prod = ctSelectedOffersResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getBundleProducts()
		.get(0).getProducts().get(0);
        prod.getObj().getVariants().get(0).setPrices(filteredPrices);
        
        PriceProtection protectionPlan = new PriceProtection();
        protectionPlan.setContractIndicator(contract);
        protectionPlan.setCurrentDate("01/02/2022");
        protectionPlan.setEndDate("01/02/2025");
        protectionPlan.setNextBillingDate("01/02/2024");
        protectionPlan.setStartDate("01/02/2021");
		ctOfferRequest.setPriceProtection(protectionPlan);
		buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, ctOffer, prod, filteredPrices);
        
        buildMiddlewareCTFilters.filterCorrectPrice(ctSelectedOffersResponse, ctOfferRequest);
        

        buildMiddlewareCTFilters.deliveryMethodByChannelFilterOffer(ctSelectedOffersResponse,ctOfferRequest);
        
        
    }

    @Test
	public void testGetOfferSalesEligibleDMA() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	ctOfferRequest = new DataReader().readFileToObj("stms/getoffers-sales-dma-eligible.json", CTOfferRequest.class);
        	
            ctOfferResponse = new DataReader().readFileToObj("stms/ct-addon-response.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnDMA(any(), any());
        
        CTOfferResponse offers = buildMiddlewareCTFilters.applyMiddlewareFilters(ctOfferResponse, ctOfferRequest);
        assertNotNull(offers);
        assertEquals(1, offers.getOffers().size());
        Optional<CTOffer> matchingObject = offers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-A-AND-E-CRIME-CENTRAL_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
	}
    
    @Test
	public void testGetOfferSalesIneligibleDMA() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	ctOfferRequest = new DataReader().readFileToObj("stms/getoffers-sales-dma-ineligible.json", CTOfferRequest.class);
        	
            ctOfferResponse = new DataReader().readFileToObj("stms/ct-addon-response.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnDMA(any(), any());
        
        CTOfferResponse offers = buildMiddlewareCTFilters.applyMiddlewareFilters(ctOfferResponse, ctOfferRequest);
        assertNotNull(offers);
        assertEquals(0, offers.getOffers().size());
        Optional<CTOffer> matchingObject = offers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-A-AND-E-CRIME-CENTRAL_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(matchingObject.isPresent());
	}
    
    @Test
	public void testGetOfferServicesNonADEEligibleDMA() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	ctOfferRequest = new DataReader().readFileToObj("stms/getoffers-services-non-ade-dma-eligible.json", CTOfferRequest.class);
        	
            ctOfferResponse = new DataReader().readFileToObj("stms/ct-addon-response.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnDMA(any(), any());
        
        CTOfferResponse offers = buildMiddlewareCTFilters.applyMiddlewareFilters(ctOfferResponse, ctOfferRequest);
        assertNotNull(offers);
        assertEquals(1, offers.getOffers().size());
        Optional<CTOffer> matchingObject = offers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-A-AND-E-CRIME-CENTRAL_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertTrue(matchingObject.isPresent());
	}
    
    @Test
	public void testGetOfferServicesNonADEIneligibleDMA() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
        	ctOfferRequest = new DataReader().readFileToObj("stms/getoffers-services-non-ade-dma-ineligible.json", CTOfferRequest.class);
        	
            ctOfferResponse = new DataReader().readFileToObj("stms/ct-addon-response.json", CTOfferResponse.class);
            
        } catch(Exception e) {
            //do nothing
        }
        
        Mockito.doCallRealMethod().when(satelliteCTOffersProcessor).filterOffersBasedOnDMA(any(), any());
        
        CTOfferResponse offers = buildMiddlewareCTFilters.applyMiddlewareFilters(ctOfferResponse, ctOfferRequest);
        assertNotNull(offers);
        assertEquals(0, offers.getOffers().size());
        Optional<CTOffer> matchingObject = offers.getOffers().stream()
				.filter(Objects::nonNull).filter(p -> "OF_BOLTON-A-AND-E-CRIME-CENTRAL_satellite".equalsIgnoreCase(p.getCode()))
				.findFirst();
        assertFalse(matchingObject.isPresent());
	}

	@Test
	public void testFilterIncludedProductsFromOffer() {

		CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("getoffers-mdu-videoplan-response.json", CTOfferResponse.class);

		Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_MDU_INCLUDED_PRODUCT_EXCLUSIONS)).thenReturn(true);

		List<String> excludedProductsMock = List.of("BOLT-ESPNPLUS-202411");
		Mockito.when(offersUtils.getConfigList(Constants.MDU_INCLUDED_PRODUCT_EXCLUSIONS)).thenReturn(excludedProductsMock);

		Assertions.assertEquals(excludedProductsMock, offersUtils.getConfigList(Constants.MDU_INCLUDED_PRODUCT_EXCLUSIONS),
				"Mocked getConfigList should return the expected excluded products list");

        Assertions.assertFalse(ctOfferResponse.getOffers().isEmpty(), "Test data should contain offers");
        Assertions.assertFalse(ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().isEmpty(), "Offers should have associated products");

		buildMiddlewareCTFilters.filterIncludedProductsFromOffer(ctOfferResponse);

		Assertions.assertAll("Validations",
				() -> Assertions.assertNotNull(ctOfferResponse, "Filtered response should not be null"),
				() -> Assertions.assertFalse(ctOfferResponse.getOffers().isEmpty(), "Filtered response should contain offers")
		);

		ctOfferResponse.getOffers().forEach(offer -> {
			Assertions.assertNotNull(offer.getAttributes(), "Offer attributes should not be null");
			offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
				associatedProduct.getBundleProducts().forEach(bundleProduct ->
						Assertions.assertFalse(excludedProductsMock.contains(bundleProduct.getProducts()),
								"Excluded bundle product should not be present in the response")
				);
				associatedProduct.getQualifyingProducts().forEach(qualifyingProduct ->
						Assertions.assertFalse(excludedProductsMock.contains(qualifyingProduct.getProducts()),
								"Excluded qualifying product should not be present in the response")
				);
			});
		});
	}
	
	@Test
	public void testUpdateMessagesByKey() {
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("getoffers-delayedmessages-videoplan-response.json", CTOfferResponse.class);
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setSalesChannel(List.of("directIntegrationPartner"));
        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
        	buildMiddlewareCTFilters.updateMessagesByKey(offer, ctOfferRequest);
        });
        assertNotNull(ctOfferResponse.getOffers());

       // Check delayProvisioningMessagesByKey for each product in bundleProducts
        ctOfferResponse.getOffers().stream()
            .filter(Objects::nonNull)
            .filter(offer -> "OF_BASE-CHOICE-201811_RR_INTRO".equalsIgnoreCase(offer.getCode()))
            .forEach(offer -> {
                Assertions.assertNotNull(offer.getAttributes(), "Offer attributes should not be null");
                offer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
                    associatedProduct.getBundleProducts().forEach(bundleProduct -> {
                        bundleProduct.getProducts().forEach(prod -> {
                            Assertions.assertNotNull(prod.getObj().getVariants().get(0).getAttributes().getDelayProvisioningMessagesByKey(),
                                "delayProvisioningMessagesByKey should not be null due to free trial");
                        });
                    });
                });
            });
    }

    @Test
    public void testPriceProtectQualAndBundleProductsSwimlaneGateFiltersHogAndPnp() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSwimlaneSwitchEligible(true);
        ctOfferRequest.setOfferRequest(offerRequest);

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(List.of(Constants.SWIMLANE));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");
        hogPrice.setCustomerGroup(null);

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>());

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(1, finalPrices.size());
        assertEquals("TAZCONTRACT", finalPrices.get(0).getChannel());
        assertTrue(finalPrices.get(0).getCustomerGroup() == null || finalPrices.get(0).getCustomerGroup().isEmpty());
    }

    @Test
    public void testPriceProtectQualAndBundleProductsSwimlaneGateNotAppliedWhenNotEligible() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSwimlaneSwitchEligible(false);
        ctOfferRequest.setOfferRequest(offerRequest);

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(List.of(Constants.SWIMLANE));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");
        hogPrice.setCustomerGroup(null);

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>());

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(2, finalPrices.size());
        assertTrue(finalPrices.stream().anyMatch(price -> "2024-PNP-1".equals(price.getCustomerGroup())));
    }

    @Test
    public void testPriceProtectQualAndBundleProductsSwimlaneGateFlowIntentCaseInsensitive() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSwimlaneSwitchEligible(true);
        ctOfferRequest.setOfferRequest(offerRequest);

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(List.of("sWiMlAnE"));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>());

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(1, finalPrices.size());
        assertTrue(finalPrices.get(0).getCustomerGroup() == null || finalPrices.get(0).getCustomerGroup().isEmpty());
    }

    @Test
    public void testPriceProtectQualAndBundleProductsSwimlaneGateNullOfferRequestNoNpe() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(List.of(Constants.SWIMLANE));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        Assertions.assertDoesNotThrow(() ->
                buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>()));

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(2, finalPrices.size());
        assertTrue(finalPrices.stream().anyMatch(price -> "2024-PNP-1".equals(price.getCustomerGroup())));
    }

    @Test
    public void testPriceProtectQualAndBundleProductsExplicitInlaneShouldNotGateEvenWhenSubscriptionMatches() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSwimlaneSwitchEligible(true);
        offerRequest.setSlsEligibleSubscriptionTypes(List.of("GENRE"));
        ctOfferRequest.setOfferRequest(offerRequest);

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(List.of("inlane"));
        attributes.setServiceSubscriptionType(List.of("GENRE"));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>());

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(2, finalPrices.size());
        assertTrue(finalPrices.stream().anyMatch(price -> "2024-PNP-1".equals(price.getCustomerGroup())));
    }

    @Test
    public void testPriceProtectQualAndBundleProductsSubscriptionFallbackShouldGateWhenFlowIntentAbsent() {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        OfferRequest offerRequest = new OfferRequest();
        offerRequest.setSwimlaneSwitchEligible(true);
        offerRequest.setSlsEligibleSubscriptionTypes(List.of("GENRE"));
        ctOfferRequest.setOfferRequest(offerRequest);

        CTOffer offer = new CTOffer();
        OfferAttributes attributes = new OfferAttributes();
        attributes.setFlowIntents(null);
        attributes.setServiceSubscriptionType(List.of("GENRE"));
        offer.setAttributes(attributes);

        Price hogPrice = new Price();
        hogPrice.setChannel("PP-TAZ");

        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setCustomerGroup("2024-PNP-1");

        Price standardPrice = new Price();
        standardPrice.setChannel("TAZCONTRACT");
        standardPrice.setCustomerGroup(null);

        Variant variant = new Variant();
        variant.setPrices(new ArrayList<>(Arrays.asList(hogPrice, pnpPrice, standardPrice)));
        ProductObj productObj = new ProductObj();
        productObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(productObj);

        buildMiddlewareCTFilters.priceProtectQualAndBundleProducts(ctOfferRequest, offer, product, new ArrayList<>());

        List<Price> finalPrices = product.getObj().getVariants().get(0).getPrices();
        assertEquals(1, finalPrices.size());
        assertTrue(finalPrices.get(0).getCustomerGroup() == null || finalPrices.get(0).getCustomerGroup().isEmpty());
    }
}
