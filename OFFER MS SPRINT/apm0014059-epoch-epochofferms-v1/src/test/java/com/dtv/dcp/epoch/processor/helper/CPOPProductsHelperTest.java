package com.dtv.dcp.epoch.processor.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.ChannelEligibility;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.request.ProductRequestWrapper;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.product.ActionRule;
import com.dtv.dcp.epoch.model.ct.product.ActionRuleAttributes;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.IncludeProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;

class CPOPProductsHelperTest {

    @InjectMocks
    CPOPProductsHelper cpopProductsHelper;
    @Mock
    CpopClientHelper cpopClientHelper;
    @Mock
    FeatureManagerHelper featureManagerHelper;
    @Mock
    OffersUtils offersUtils;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        doReturn(true).when(featureManagerHelper).isEnabled(Mockito.anyString());
    }

    @Test
    void filterInvalidPricesWithRequestTreatmentCodeTest() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTSales.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTSales_D001.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidPrices(ctOfferResponse, offerRequestWrapper);
        assertNotNull(response);
        assertNotNull(response.getOffers());
        assertEquals(12, response.getOffers().size());
    }

    @Test
    void filterInvalidContractIndicatorPricesTest() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServices.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, false, null);
        assertNotNull(response);
        assertNotNull(response.getOffers());
    }

    @Test
    void filterInvalidContractIndicatorPricesTestIsBulk() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsBulk.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, false, null);
        assertNotNull(response);
        assertNotNull(response.getOffers());
    }

    @Test
    void filterInvalidContractIndicatorPricesTestVideo_plan() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");
        assertNotNull(response);
        assertNotNull(response.getOffers());
    }
    @Test
    void filterInvalidContractIndicatorPricesTestVideo_planBulk() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideoplanBulk.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");
        assertNotNull(response);
        assertNotNull(response.getOffers());
    }

    /**
     * When swimlaneSwitchEligible=true AND offer has flowIntents=["swimlane"],
     * the PNP branch should be SKIPPED and fall through to removeInvalidContractIndicatorPricesFromList().
     * Prices with a customerGroup (PNP prices) must NOT survive the default filter (which requires customerGroup=null).
     */
    @Test
    void filterInvalidContractIndicatorPrices_swimlaneEligible_shouldSkipPNPFilter() {
        // Load video-plan offer response and set flowIntents=["swimlane"] on each offer
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        ctOfferResponse.getOffers().forEach(offer ->
                offer.getAttributes().setFlowIntents(Arrays.asList(Constants.SWIMLANE))
        );

        // Inject a synthetic PNP-marked price so this test fails if PNP branch is incorrectly applied
        String contractIndicator = ctOfferResponse.getOffers().get(0).getAttributes().getContractIndicator();
        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setContractIndicator(contractIndicator);
        pnpPrice.setCustomerGroup("PNP_2024_1");
        pnpPrice.setStartDate("2020-01-01T00:00:00.000Z");
        pnpPrice.setEndDate("2050-01-01T00:00:00.000Z");
        ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0)
                .getProducts().get(0).getObj().getVariants().get(0).getPrices().add(pnpPrice);

        // Load wrapper and override swimlaneSwitchEligible to true
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        offerRequestWrapper.getOfferRequest().setSwimlaneSwitchEligible(true);

        // isPNP=true, but isSwimlaneOffer=true → PNP branch should be skipped
        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");

        assertNotNull(response);
        assertNotNull(response.getOffers());

        // Verify PNP prices are removed (PNP branch skipped; default path excludes customerGroup)
        boolean hasPnpPrice = response.getOffers().stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(ap -> ap.getQualifyingProducts() != null)
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(qp -> qp.getProducts() != null)
                .flatMap(qp -> qp.getProducts().stream())
                .flatMap(product -> product.getObj().getVariants().stream())
                .flatMap(variant -> variant.getPrices().stream())
                .anyMatch(price -> "PNP_2024_1".equalsIgnoreCase(price.getCustomerGroup()));
        assertFalse("PNP price should not survive when swimlane gate is active",hasPnpPrice);
    }

    /**
     * When swimlaneSwitchEligible=false but offer has flowIntents=["swimlane"],
     * isSwimlaneOffer evaluates to false, so the PNP branch STILL applies as normal.
     */
    @Test
    void filterInvalidContractIndicatorPrices_swimlaneNotEligible_shouldApplyPNPFilter() {
        // Load video-plan offer response and set flowIntents=["swimlane"] on each offer
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        ctOfferResponse.getOffers().forEach(offer ->
                offer.getAttributes().setFlowIntents(Arrays.asList(Constants.SWIMLANE))
        );

        // Inject a synthetic PNP-marked price so this test validates that PNP branch is applied
        String contractIndicator = ctOfferResponse.getOffers().get(0).getAttributes().getContractIndicator();
        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setContractIndicator(contractIndicator);
        pnpPrice.setCustomerGroup("PNP_2024_1");
        pnpPrice.setStartDate("2020-01-01T00:00:00.000Z");
        pnpPrice.setEndDate("2050-01-01T00:00:00.000Z");
        ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0)
                .getProducts().get(0).getObj().getVariants().get(0).getPrices().add(pnpPrice);

        // swimlaneSwitchEligible=false → isSwimlaneOffer=false → PNP branch applies
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        offerRequestWrapper.getOfferRequest().setSwimlaneSwitchEligible(false);

        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");

        assertNotNull(response);
        assertNotNull(response.getOffers());

        boolean hasPnpPrice = response.getOffers().stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(ap -> ap.getQualifyingProducts() != null)
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(qp -> qp.getProducts() != null)
                .flatMap(qp -> qp.getProducts().stream())
                .flatMap(product -> product.getObj().getVariants().stream())
                .flatMap(variant -> variant.getPrices().stream())
                .anyMatch(price -> "PNP_2024_1".equalsIgnoreCase(price.getCustomerGroup()));
        assertTrue(hasPnpPrice, "PNP price should be retained when swimlane gate is not active");
    }

    @Test
    void filterInvalidContractIndicatorPrices_swimlaneSwitchEligibleNull_shouldNotThrow() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        ctOfferResponse.getOffers().forEach(offer ->
                offer.getAttributes().setFlowIntents(Arrays.asList(Constants.SWIMLANE))
        );

        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        offerRequestWrapper.getOfferRequest().setSwimlaneSwitchEligible(null);

        Assertions.assertDoesNotThrow(() -> {
            CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");
            assertNotNull(response);
            assertNotNull(response.getOffers());
        });
    }

    @Test
    void filterInvalidContractIndicatorPrices_explicitInlaneShouldNotGateEvenWhenSubscriptionMatches() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        ctOfferResponse.getOffers().forEach(offer -> {
            offer.getAttributes().setFlowIntents(Arrays.asList("inlane"));
            offer.getAttributes().setServiceSubscriptionType(Arrays.asList("GENRE"));
        });

        String contractIndicator = ctOfferResponse.getOffers().get(0).getAttributes().getContractIndicator();
        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setContractIndicator(contractIndicator);
        pnpPrice.setCustomerGroup("PNP_2024_1");
        pnpPrice.setStartDate("2020-01-01T00:00:00.000Z");
        pnpPrice.setEndDate("2050-01-01T00:00:00.000Z");
        ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0)
                .getProducts().get(0).getObj().getVariants().get(0).getPrices().add(pnpPrice);

        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        offerRequestWrapper.getOfferRequest().setSwimlaneSwitchEligible(true);
        offerRequestWrapper.getOfferRequest().setSlsEligibleSubscriptionTypes(Arrays.asList("GENRE"));

        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");

        assertNotNull(response);
        assertNotNull(response.getOffers());

        boolean hasPnpPrice = response.getOffers().stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(ap -> ap.getQualifyingProducts() != null)
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(qp -> qp.getProducts() != null)
                .flatMap(qp -> qp.getProducts().stream())
                .flatMap(product -> product.getObj().getVariants().stream())
                .flatMap(variant -> variant.getPrices().stream())
                .anyMatch(price -> "PNP_2024_1".equalsIgnoreCase(price.getCustomerGroup()));
        assertTrue(hasPnpPrice, "PNP price should be retained when explicit flowIntent is inlane");
    }

    @Test
    void filterInvalidContractIndicatorPrices_subscriptionFallbackShouldGateWhenFlowIntentAbsent() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTServicesIsVideo_plan.json"), CTOfferResponse.class);
        ctOfferResponse.getOffers().forEach(offer -> {
            offer.getAttributes().setFlowIntents(null);
            offer.getAttributes().setServiceSubscriptionType(Arrays.asList("GENRE"));
        });

        String contractIndicator = ctOfferResponse.getOffers().get(0).getAttributes().getContractIndicator();
        Price pnpPrice = new Price();
        pnpPrice.setChannel("TAZCONTRACT");
        pnpPrice.setContractIndicator(contractIndicator);
        pnpPrice.setCustomerGroup("PNP_2024_1");
        pnpPrice.setStartDate("2020-01-01T00:00:00.000Z");
        pnpPrice.setEndDate("2050-01-01T00:00:00.000Z");
        ctOfferResponse.getOffers().get(0).getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0)
                .getProducts().get(0).getObj().getVariants().get(0).getPrices().add(pnpPrice);

        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTServices.json"), OfferRequestWrapper.class);
        offerRequestWrapper.getOfferRequest().setSwimlaneSwitchEligible(true);
        offerRequestWrapper.getOfferRequest().setSlsEligibleSubscriptionTypes(Arrays.asList("GENRE"));

        CTOfferResponse response = cpopProductsHelper.filterInvalidContractIndicatorPrices(ctOfferResponse, offerRequestWrapper, true, "PNP_2024_1");

        assertNotNull(response);
        assertNotNull(response.getOffers());

        boolean hasPnpPrice = response.getOffers().stream()
                .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream())
                .filter(ap -> ap.getQualifyingProducts() != null)
                .flatMap(ap -> ap.getQualifyingProducts().stream())
                .filter(qp -> qp.getProducts() != null)
                .flatMap(qp -> qp.getProducts().stream())
                .flatMap(product -> product.getObj().getVariants().stream())
                .flatMap(variant -> variant.getPrices().stream())
                .anyMatch(price -> "PNP_2024_1".equalsIgnoreCase(price.getCustomerGroup()));
        assertFalse("PNP price should not survive when swimlane fallback gate is active", hasPnpPrice);
    }

    @Test
    void testFilterPriceByContractIndicator() {
        // Setup test data
        List<String> teatmentCode = Arrays.asList("D001", "D002");
        List<String> creditRisk = Arrays.asList("Low", "High");
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj = new ProductObj();
        Variant variant = new Variant();
        Price price1 = new Price();
        price1.setContractIndicator("CONTRACT");
        price1.setStartDate("2022-05-03T00:00:00.000Z");
        price1.setEndDate("2050-05-03T00:00:00.000Z");
        price1.setTreatmentCode(Collections.singletonList(teatmentCode.get(0)));
        price1.setCreditRisk(Collections.singletonList(creditRisk.get(0)));

        Price price2 = new Price();
        price2.setContractIndicator("NONCONTRACT");
        price2.setStartDate("2022-05-03T00:00:00.000Z");
        price2.setEndDate("2050-05-03T00:00:00.000Z");
        price2.setTreatmentCode(Collections.singletonList(teatmentCode.get(1)));
        price2.setCreditRisk(Collections.singletonList(creditRisk.get(1)));

        variant.setPrices(Arrays.asList(price1, price2));
        productObj.setVariants(Collections.singletonList(variant));
        products.add(productObj);

        // Invoke method
        List<ProductObj> result = cpopProductsHelper.filterPriceByContractIndicator(products, "CONTRACT", "D001", "Low");

        // Assertions
        Assertions.assertEquals(1, result.size());
        assertEquals(1, result.get(0).getVariants().get(0).getPrices().size());
        assertEquals("CONTRACT", result.get(0).getVariants().get(0).getPrices().get(0).getContractIndicator());
        assertNotNull(teatmentCode.get(0), result.get(0).getVariants().get(0).getPrices().get(0).getTreatmentCode());
        assertNotNull(creditRisk.get(0), result.get(0).getVariants().get(0).getPrices().get(0).getCreditRisk());

    }

    @Test
    void testFilterNBCDPriceByContractIndicator() {
        List<String> customerGroup = Arrays.asList("2024-PNP-1", "2024-PNP-2");
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj = new ProductObj();
        GenericTypeIdBase productType = new GenericTypeIdBase();
        productType.setKey("video-plan");
        productObj.setProductType(productType);
        Variant variant = new Variant();
        Price price1 = new Price();
        price1.setContractIndicator("CONTRACT");
        price1.setStartDate("2024-05-03T00:00:00.000Z");
        price1.setEndDate("2050-05-03T00:00:00.000Z");
        price1.setTreatmentCode(Arrays.asList("D001"));
        price1.setCreditRisk(Arrays.asList("Low"));
        price1.setCustomerGroup(customerGroup.get(0));

        Price price2 = new Price();
        price2.setContractIndicator("NONCONTRACT");
        price2.setStartDate("2024-05-03T00:00:00.000Z");
        price2.setEndDate("2050-05-03T00:00:00.000Z");
        price2.setTreatmentCode(Arrays.asList("D002"));
        price2.setCreditRisk(Arrays.asList("High"));
        price2.setCustomerGroup(customerGroup.get(1));

        variant.setPrices(Arrays.asList(price1, price2));
        productObj.setVariants(Collections.singletonList(variant));
        products.add(productObj);

        // Invoke method
        List<ProductObj> result = cpopProductsHelper.filterNBCDPriceByContractIndicator(products, "CONTRACT", "05/03/2050T00:00:00.000Z", "D001", "Low", true, "2024-PNP-1");

        // Assertions
        Assertions.assertEquals(1, result.size());
        assertEquals(1, result.get(0).getVariants().get(0).getPrices().size());
        assertEquals("CONTRACT", result.get(0).getVariants().get(0).getPrices().get(0).getContractIndicator());
        assertTrue(result.get(0).getVariants().get(0).getPrices().get(0).getTreatmentCode().contains("D001"));
        assertTrue(result.get(0).getVariants().get(0).getPrices().get(0).getCreditRisk().contains("Low"));
    }

    @Test
    void testFilterByCompatibleProducts() {
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        CTProductResponse ctProductResponse = new CTProductResponse();
        Variant variant = new Variant();
        Attributes attributes = new Attributes();
        ctProductResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/GETProductResponse.json"), CTProductResponse.class);
        attributes = ctProductResponse.getProducts().get(0).getVariants().get(0).getAttributes();
        List<ProductWrapper> compatibleMobilityProducts = attributes.getCompatibleMobilityProducts();
        List<ProductWrapper> compatibleEmployeeProducts = attributes.getCompatibleEmployeeProducts();
        List<IncludeProductWrapper> includedMobilityProducts = attributes.getIncludedMobilityProducts();
        attributes.setCompatibleMobilityProducts(attributes.getCompatibleMobilityProducts());
        attributes.setCompatibleEmployeeProducts(attributes.getCompatibleEmployeeProducts());
        attributes.setIncludedMobilityProducts(attributes.getIncludedMobilityProducts());
        variant.setAttributes(attributes);
        products.add(ctProductResponse.getProducts().get(0));
        // Invoke method with mobility = true
        List<ProductObj> result = cpopProductsHelper.filterByCompatibleProducts(products, true, false);

        // Assertions for mobility = true
        assertEquals(compatibleMobilityProducts, result.get(0).getVariants().get(0).getAttributes().getCompatibleProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleMobilityProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleEmployeeProducts());
        assertEquals(includedMobilityProducts, result.get(0).getVariants().get(0).getAttributes().getIncludedProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getIncludedMobilityProducts());

        // Invoke method with isEmployeeAccount = true
        result = cpopProductsHelper.filterByCompatibleProducts(products, false, true);

        // Assertions for isEmployeeAccount = true
        assertEquals(compatibleEmployeeProducts, result.get(0).getVariants().get(0).getAttributes().getCompatibleProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleMobilityProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleEmployeeProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getIncludedMobilityProducts());

        // Invoke method with both flags false
        result = cpopProductsHelper.filterByCompatibleProducts(products, false, false);

        // Assertions for both flags false
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleMobilityProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getCompatibleEmployeeProducts());
        assertNull(result.get(0).getVariants().get(0).getAttributes().getIncludedMobilityProducts());
    }

    @Test
    void testFilterByProductsObj() {
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj1 = new ProductObj();
        Variant variant1 = new Variant();
        Attributes attributes1 = new Attributes();
        attributes1.setBillingProductCode("BASE-ENTERTAINMENT-201811");
        variant1.setAttributes(attributes1);
        productObj1.setVariants(Collections.singletonList(variant1));
        products.add(productObj1);

        ProductObj productObj2 = new ProductObj();
        Variant variant2 = new Variant();
        Attributes attributes2 = new Attributes();
        attributes2.setBillingProductCode("BASE-CHOICE-201811");
        variant2.setAttributes(attributes2);
        productObj2.setVariants(Collections.singletonList(variant2));
        products.add(productObj2);

        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<ProductObj> result = cpopProductsHelper.filterByProductsObj(products, listOfProducts);

        // Assertions
        assertEquals(2, result.size());
        assertEquals("BASE-ENTERTAINMENT-201811", result.get(0).getVariants().get(0).getAttributes().getBillingProductCode());
    }

    @Test
    void testRemoveExpiredPricesFromList() {
        // Setup test data
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        Variant variant = new Variant();
        Price price1 = new Price();
        price1.setStartDate("2022-05-03T00:00:00.000Z");
        price1.setEndDate("2050-05-03T00:00:00.000Z");
        price1.setContractIndicator("CONTRACT");
        price1.setTreatmentCode(Collections.singletonList("T001"));
        price1.setCreditRisk(Collections.singletonList("Low"));

        Price price2 = new Price();
        price2.setStartDate("2022-05-03T00:00:00.000Z");
        price2.setEndDate("2050-05-03T00:00:00.000Z");
        price2.setContractIndicator("NONCONTRACT");
        price2.setTreatmentCode(Collections.singletonList("T002"));
        price2.setCreditRisk(Collections.singletonList("High"));

        variant.setPrices(new ArrayList<>(List.of(price1, price2)));
        product.setObj(new ProductObj());
        product.getObj().setVariants(Collections.singletonList(variant));
        products.add(product);

        String contractIndicator = "CONTRACT";
        List<String> treatmentCode = Collections.singletonList("T001");
        List<String> creditRisk = Collections.singletonList("Low");

        // Invoke method
        List<Product> result = cpopProductsHelper.removeExpiredPricesFromList(products, contractIndicator, treatmentCode, creditRisk);

        // Assertions
        assertEquals(1, result.get(0).getObj().getVariants().get(0).getPrices().size());
        assertEquals("CONTRACT", result.get(0).getObj().getVariants().get(0).getPrices().get(0).getContractIndicator());
        assertTrue(result.get(0).getObj().getVariants().get(0).getPrices().get(0).getTreatmentCode().contains("T001"));
        assertTrue(result.get(0).getObj().getVariants().get(0).getPrices().get(0).getCreditRisk().contains("Low"));
    }


    @Test
    void testFilterNonIAPRemovalRules() {
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj = new ProductObj();
        Variant variant = new Variant();
        Attributes attributes = new Attributes();
        ActionRule removalRuleByChannel = new ActionRule();
        ActionRuleAttributes actionRule1 = new ActionRuleAttributes();
        actionRule1.setCustomerSegments("Employee");
        actionRule1.setSalesChannel("Online");
        ActionRuleAttributes actionRule2 = new ActionRuleAttributes();
        actionRule2.setSalesChannel("Online");
        actionRule2.setIneligibleAccountStatuses(Collections.emptyList());
        actionRule2.setIapPartnerAccountType("");
        actionRule2.setCustomerSegments("Residential");
        removalRuleByChannel.setActionRule(new ArrayList<>(List.of(actionRule1, actionRule2)));
        attributes.setRemovalRuleByChannel(removalRuleByChannel);
        variant.setAttributes(attributes);
        productObj.setVariants(Collections.singletonList(variant));
        products.add(productObj);

        ProductRequest productRequest = new ProductRequest();
        productRequest.setSalesChannel(Collections.singletonList("Online"));

        // Invoke method
        cpopProductsHelper.filterNonIAPRemovalRules(products, "Employee", "", productRequest);

        // Assertions
        assertEquals(1, products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().size());
        assertEquals("Employee", products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().get(0).getCustomerSegments());

        // Invoke method with non-employee account type
        cpopProductsHelper.filterNonIAPRemovalRules(products, "Residential", "IAP", productRequest);

        // Assertions
        assertEquals(1, products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().size());
        assertEquals("Online", products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().get(0).getSalesChannel());
    }

    @Test
    void testFilterActionRuleByIneligibleAccountStatuses() {
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj = new ProductObj();

        Variant variant = new Variant();
        Attributes attributes = new Attributes();
        ActionRule removalRuleByChannel = new ActionRule();
        ActionRuleAttributes actionRule1 = new ActionRuleAttributes();
        actionRule1.setCustomerSegments("Employee");
        actionRule1.setSalesChannel("Online");
        actionRule1.setIneligibleAccountStatuses(Collections.singletonList("Inactive"));
        ActionRuleAttributes actionRule2 = new ActionRuleAttributes();
        actionRule2.setCustomerSegments("Residential");
        actionRule2.setSalesChannel("Online");
        actionRule2.setIneligibleAccountStatuses(Collections.singletonList("Active"));
        removalRuleByChannel.setActionRule(new ArrayList<>(List.of(actionRule1, actionRule2)));
        attributes.setRemovalRuleByChannel(removalRuleByChannel);
        variant.setAttributes(attributes);
        productObj.setVariants(Collections.singletonList(variant));
        products.add(productObj);

        ProductRequest productRequest = new ProductRequest();
        productRequest.setSalesChannel(Collections.singletonList("Online"));

        // Invoke method
        cpopProductsHelper.filterActionRuleByIneligibleAccountStatuses(products, productRequest, "IAP", "Employee", true, false,false);

        // Assertions
        assertEquals(1, products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().size());
        assertEquals("Employee", products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().get(0).getCustomerSegments());

        // Invoke method with non-employee account type
        cpopProductsHelper.filterActionRuleByIneligibleAccountStatuses(products, productRequest, "IAP", "Employee", false, false,false);

        // Assertions
        assertEquals(1, products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().size());
        assertEquals("Employee", products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().get(0).getCustomerSegments());

        // Invoke method with null iapPartnerAccountType
        cpopProductsHelper.filterActionRuleByIneligibleAccountStatuses(products, productRequest, null, "Residential", false, false,false);

        // Assertions
        assertNull(products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel());
    }

    @Test
    void testFilterActionRuleForOPUS() {
        // Setup test data
        ActionRuleAttributes actionRuleAttributes = new ActionRuleAttributes();
        actionRuleAttributes.setSalesChannel("opus");
        actionRuleAttributes.setOpusStoreIds(Collections.singletonList("7S9RV"));
        actionRuleAttributes.setOpusSubChannels(Collections.singletonList("DMDR"));
        actionRuleAttributes.setOpusChannels(Collections.singletonList("DMDR"));

        ProductRequest productRequest = new ProductRequest();
        productRequest.setSalesChannel(Collections.singletonList("opus"));
        ChannelEligibility channelEligibility = new ChannelEligibility();
        channelEligibility.setOpusStoreId("7S9RV");
        channelEligibility.setOpusSubChannel("DMDR");
        channelEligibility.setOpusChannel("DMDR");
        productRequest.setChannelEligibility(channelEligibility);

        // Invoke method and assert
        assertTrue(cpopProductsHelper.filterActionRuleForOPUS(actionRuleAttributes, productRequest));


        channelEligibility.setOpusStoreId("");
        productRequest.setChannelEligibility(channelEligibility);
        assertTrue(cpopProductsHelper.filterActionRuleForOPUS(actionRuleAttributes, productRequest));

        channelEligibility.setOpusStoreId("");
        channelEligibility.setOpusSubChannel("");
        productRequest.setChannelEligibility(channelEligibility);
        assertTrue(cpopProductsHelper.filterActionRuleForOPUS(actionRuleAttributes, productRequest));

    }

    @Test
    void testFilterRemovalRuleBySalesChannel() {
        // Setup test data
        List<ProductObj> products = new ArrayList<>();
        ProductObj productObj = new ProductObj();
        Variant variant = new Variant();
        Attributes attributes = new Attributes();
        ActionRule removalRuleByChannel = new ActionRule();
        ActionRuleAttributes actionRule1 = new ActionRuleAttributes();
        actionRule1.setSalesChannel("Online");
        actionRule1.setCustomerSegments("Employee");
        actionRule1.setIapPartnerAccountType("IAP");

        ActionRuleAttributes actionRule2 = new ActionRuleAttributes();
        actionRule2.setSalesChannel("opus");
        actionRule2.setCustomerSegments("Residential");
        actionRule2.setIapPartnerAccountType("NONIAP");

        removalRuleByChannel.setActionRule(new ArrayList<>(List.of(actionRule1, actionRule2)));
        attributes.setRemovalRuleByChannel(removalRuleByChannel);
        variant.setAttributes(attributes);
        productObj.setVariants(Collections.singletonList(variant));
        products.add(productObj);

        ProductRequest productRequest = new ProductRequest();
        productRequest.setSalesChannel(Collections.singletonList("Online"));

        // Invoke method
        cpopProductsHelper.filterRemovalRuleBySalesChannel(products, "IAP", "Employee", productRequest);

        // Assertions
        assertEquals(1, products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().size());
        assertEquals("Online", products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel().getActionRule().get(0).getSalesChannel());

        // Invoke method with non-matching account type
        cpopProductsHelper.filterRemovalRuleBySalesChannel(products, "NONIAP", "Residential", productRequest);

        // Assertions
        assertNull(products.get(0).getVariants().get(0).getAttributes().getRemovalRuleByChannel());
    }

    @Test
    void testFilterByProducts() {
        // Setup test data
        Product product1 = new Product();
        product1.setKey("BASE-ENTERTAINMENT-201811");
        Product product2 = new Product();
        product2.setKey("BASE-CHOICE-201811");
        List<Product> products = Arrays.asList(product1, product2);
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterByProducts(products, listOfProducts);

        // Assertions
        assertEquals(1, actualResponse.size());
        assertEquals("BASE-ENTERTAINMENT-201811", actualResponse.get(0).getKey());
    }

    @Test
    void testFilterByProductsNotInList() {
        // Setup test data
        Product product1 = new Product();
        product1.setKey("BASE-ENTERTAINMENT-201811");
        Product product2 = new Product();
        product2.setKey("BASE-CHOICE-201811");
        List<Product> products = Arrays.asList(product1, product2);
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterByProductsNotInList(products, listOfProducts);

        // Assertions
        assertEquals(1, actualResponse.size());
        assertEquals("BASE-CHOICE-201811", actualResponse.get(0).getKey());
    }

    @Test
    void testFilterByProductsNotInList_EmptyList() {
        // Setup test data
        List<Product> products = Collections.emptyList();
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterByProductsNotInList(products, listOfProducts);

        // Assertions
        assertTrue(actualResponse.isEmpty());
    }

    @Test
    void testFilterByProductsNotInList_Exception() {
        // Setup test data
        List<Product> products = null;
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        try {
            // Invoke method
            List<Product> actualResponse = cpopProductsHelper.filterByProductsNotInList(products, listOfProducts);
        } catch (Exception e) {
            // Assertions
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    void testFilterByProductsInList() {
        // Setup test data
        Product product1 = new Product();
        product1.setKey("BASE-ENTERTAINMENT-201811");
        Product product2 = new Product();
        product2.setKey("BASE-CHOICE-201811");
        List<Product> products = Arrays.asList(product1, product2);
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterByProductsInList(products, listOfProducts);

        // Assertions
        assertEquals(1, actualResponse.size());
        assertEquals("BASE-ENTERTAINMENT-201811", actualResponse.get(0).getKey());
    }

    @Test
    void testFilterByProductsInList_EmptyList() {
        // Setup test data
        List<Product> products = Collections.emptyList();
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterByProductsInList(products, listOfProducts);

        // Assertions
        assertTrue(actualResponse.isEmpty());
    }

    @Test
    void testFilterByProductsInList_Exception() {
        // Setup test data
        List<Product> products = null;
        List<String> listOfProducts = Collections.singletonList("BASE-ENTERTAINMENT-201811");

        try {
            // Invoke method
            List<Product> actualResponse = cpopProductsHelper.filterByProductsInList(products, listOfProducts);
        } catch (Exception e) {
            // Assertions
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    void testFilterProductsByCustomerSegment() {
        // Setup test data
        Product product1 = new Product();
        Variant variant1 = new Variant();
        Attributes attributes1 = new Attributes();
        attributes1.setCustomerSegments(Collections.singletonList("Residential"));
        variant1.setAttributes(attributes1);
        product1.setObj(new ProductObj());
        product1.getObj().setVariants(Collections.singletonList(variant1));

        Product product2 = new Product();
        Variant variant2 = new Variant();
        Attributes attributes2 = new Attributes();
        attributes2.setCustomerSegments(Collections.singletonList("Employee"));
        variant2.setAttributes(attributes2);
        product2.setObj(new ProductObj());
        product2.getObj().setVariants(Collections.singletonList(variant2));

        List<Product> products = Arrays.asList(product1, product2);

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterProductsByCustomerSegment(products, "Residential");

        // Assertions
        assertEquals(1, actualResponse.size());
        assertEquals("Residential", actualResponse.get(0).getObj().getVariants().get(0).getAttributes().getCustomerSegments().get(0));
    }

    @Test
    void testFilterProductsByCustomerSegment_EmptyList() {
        // Setup test data
        List<Product> products = Collections.emptyList();

        // Invoke method
        List<Product> actualResponse = cpopProductsHelper.filterProductsByCustomerSegment(products, "Residential");

        // Assertions
        assertTrue(actualResponse.isEmpty());
    }

    @Test
    void testFilterProductsByCustomerSegment_Exception() {
        // Setup test data
        List<Product> products = null;

        try {
            // Invoke method
            List<Product> actualResponse = cpopProductsHelper.filterProductsByCustomerSegment(products, "Residential");
        } catch (Exception e) {
            // Assertions
            assertTrue(e instanceof NullPointerException);
        }
    }

    @Test
    void testGetSegmentDescription() {
        // Test case 1: Valid segment descriptions
        String segmentDescriptions = "Residential|Employee|Mobility";
        List<String> actualResponse = cpopProductsHelper.getSegmentDescription(segmentDescriptions);
        assertEquals(3, actualResponse.size());
        assertEquals("Residential", actualResponse.get(0));
        assertEquals("Employee", actualResponse.get(1));
        assertEquals("Mobility", actualResponse.get(2));

        // Test case 2: Null segment descriptions
        segmentDescriptions = null;
        actualResponse = cpopProductsHelper.getSegmentDescription(segmentDescriptions);
        assertTrue(actualResponse.isEmpty());
    }


    @Test
    void testIsValidPromoBasedOnSegementDescriptionForCustomer() {
        // Test case 1: False retentionPromoIndicator and empty segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = Collections.emptyList();
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = false;
            String coolOffPeriod = "30";

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForCustomer(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator, coolOffPeriod);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Test case 2: True retentionPromoIndicator and non-empty segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = Collections.singletonList("O-HEART");
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = true;
            String coolOffPeriod = "";

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForCustomer(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator, coolOffPeriod);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Test case 3: True retentionPromoIndicator and empty segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = Collections.emptyList();
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = true;
            String coolOffPeriod = "";

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForCustomer(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator, coolOffPeriod);
            assertFalse(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

    }

    @Test
    void testIsValidPromoBasedOnSegementDescriptionForAgent() {
        // Test case 1: False retentionPromoIndicator
        try {
            List<String> segmentDescriptionFromCT = Collections.singletonList("O-HEART");
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = false;

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForAgent(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Test case 2: True retentionPromoIndicator and matching segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = Collections.singletonList("O-HEART");
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = true;

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForAgent(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Test case 3: True retentionPromoIndicator and non-matching segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = Collections.singletonList("O-HEART");
            List<String> listHeartValue = Collections.singletonList("O-OTHER");
            Boolean retentionPromoIndicator = true;

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForAgent(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator);
            assertFalse(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        // Test case 4: Null segmentDescriptionFromCT
        try {
            List<String> segmentDescriptionFromCT = null;
            List<String> listHeartValue = Collections.singletonList("O-HEART");
            Boolean retentionPromoIndicator = true;

            boolean result = cpopProductsHelper.isValidPromoBasedOnSegementDescriptionForAgent(segmentDescriptionFromCT, listHeartValue, retentionPromoIndicator);
            assertFalse(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testIsValidTreatmentCode() {
        // Test case 1: Valid treatment code
        try {
            Price price = new Price();
            price.setTreatmentCode(Collections.singletonList("D001"));
            OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
            offerRequestWrapper.setOfferRequest(new OfferRequest());
            offerRequestWrapper.getOfferRequest().setTreatmentCode("D001");

            boolean result = cpopProductsHelper.isValidTreatmentCode(price, offerRequestWrapper);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
    }

    @Test
    void testIsValidCreditRisk() {
        // Test case 1: Valid credit risk
        try {
            Price price = new Price();
            price.setCreditRisk(Collections.singletonList("Low"));
            OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
            offerRequestWrapper.setOfferRequest(new OfferRequest());
            offerRequestWrapper.getOfferRequest().setCreditRisk("Low");

            boolean result = cpopProductsHelper.isValidCreditRisk(price, offerRequestWrapper);
            assertTrue(result);
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

    }

    @Test
    void testRemoveExpiredPricesFromList_ValidData() {
        // Setup test data
        List<Product> products = new ArrayList<>();
        Product product = new Product();
        Variant variant = new Variant();
        Attributes attributes = new Attributes();
        variant.setAttributes(attributes);
        Price price1 = new Price();
        price1.setStartDate("2022-05-03T00:00:00.000Z");
        price1.setEndDate("2050-05-03T00:00:00.000Z");
        price1.setContractIndicator("CONTRACT");
        price1.setTreatmentCode(Collections.singletonList("T001"));
        price1.setCreditRisk(Collections.singletonList("Low"));

        Price price2 = new Price();
        price2.setStartDate("2022-05-03T00:00:00.000Z");
        price2.setEndDate("2024-05-03T00:00:00.000Z");
        price2.setContractIndicator("NONCONTRACT");
        price2.setTreatmentCode(Collections.singletonList("T002"));
        price2.setCreditRisk(Collections.singletonList("High"));

        variant.setPrices(new ArrayList<>(List.of(price1, price2)));
        product.setObj(new ProductObj());
        product.getObj().setVariants(Collections.singletonList(variant));
        products.add(product);

        OfferRequestWrapper offerRequestWrapper = new OfferRequestWrapper();
        OfferRequest offerRequest = new OfferRequest();
        offerRequestWrapper.setOfferRequest(offerRequest);
        offerRequestWrapper.getOfferRequest().setCreditRisk("Low");
        offerRequestWrapper.getOfferRequest().setTreatmentCode("T001");
        CustomerContext customerContext = new CustomerContext();
        customerContext.setOtt(new CartProduct());
        offerRequestWrapper.getOfferRequest().setCustomerContext(customerContext);

        String contractIndicator = "CONTRACT";
        List<String> treatmentCode = Collections.singletonList("T001");
        List<String> creditRisk = Collections.singletonList("Low");

        // Invoke method
        List<Product> result = cpopProductsHelper.removeExpiredPricesFromList(products, contractIndicator, offerRequestWrapper, treatmentCode, creditRisk);

        // Assertions - only price1 survives: price2 has NONCONTRACT indicator and expired endDate (2024)
        assertEquals(1, result.get(0).getObj().getVariants().get(0).getPrices().size());
        assertEquals("CONTRACT", result.get(0).getObj().getVariants().get(0).getPrices().get(0).getContractIndicator());
        assertTrue(result.get(0).getObj().getVariants().get(0).getPrices().get(0).getTreatmentCode().contains("T001"));
        assertTrue(result.get(0).getObj().getVariants().get(0).getPrices().get(0).getCreditRisk().contains("Low"));
    }
    @Test
    void testFilterProductsByAccountType() throws ServiceException {
        // Setup test data
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/FilterHeartValuesReasonponse.json"), CTOfferResponse.class);
         // Test Mobility account type
        CTOfferResponse resultMobility = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, CpopConstants.MOBILITY_WITH_CAPITAL_M);
        assertNotNull(resultMobility);
        assertEquals(1, resultMobility.getOffers().size());
        // Test Residential account type
        CTOfferResponse resultResidential = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, CpopConstants.RESIDENTIAL);
        assertNotNull(resultResidential);
        assertEquals(1, resultResidential.getOffers().size());

        // Test Employee account type
        CTOfferResponse resultEmployee = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, CpopConstants.EMPLOYEE);
        assertNotNull(resultEmployee);
        assertEquals(1, resultEmployee.getOffers().size());

        // Test Null account type
        CTOfferResponse resultNull = cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, null);
        assertNotNull(resultNull);
        assertEquals(1, resultNull.getOffers().size());


        cpopProductsHelper.filterProductsByAccountType(ctOfferResponse, CpopConstants.MOBILITY_WITH_CAPITAL_M);

    }
    @Test
    void testFilterProductsByRetIOApplicableProducts(){
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/FilterHeartValuesReasonponse.json"), CTOfferResponse.class);
        ctOfferResponse =cpopProductsHelper.filterProductsByRetIOApplicableProducts(ctOfferResponse);
        assertNotNull(ctOfferResponse);
        assertEquals(1, ctOfferResponse.getOffers().size());
    }
    @Test
    void filterInvalidPrices() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSCTOfferResponseTAZCONTRACTSales.json"), CTOfferResponse.class);
        OfferRequestWrapper offerRequestWrapper = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/ACAPSOfferRequestWrapperTAZCONTRACTSales_D001.json"), OfferRequestWrapper.class);
        CTOfferResponse response = cpopProductsHelper.filterInvalidPrices(ctOfferResponse);
        assertNotNull(response);
        assertNotNull(response.getOffers());
        assertEquals(12, response.getOffers().size());
    }

    @Test
    void testFilterExpiredInstallmentOptions(){
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse   = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/GETProductResponse.json"), CTProductResponse.class);
        List<ProductObj> products = ctProductResponse.getProducts();
        cpopProductsHelper.filterExpiredInstallmentOptions(products);
        assertEquals(5, products.size());
    }

     @Test
     void testGetVideoAddonProducts(){
        CTProductResponse ctProductResponse  = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/GETProductResponse.json"), CTProductResponse.class);
        ProductRequest productRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getProductRequest.json"), ProductRequest.class);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
       productRequestWrapper.setProductRequest(productRequest);
       when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
       ctProductResponse =cpopProductsHelper.getVideoAddonProducts(productRequestWrapper);
       assertNotNull(ctProductResponse);
       assertNotNull(ctProductResponse.getProducts());
     }
    @Test
    void testGetProtecionPlanProducts(){
        CTProductResponse ctProductResponse  = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/GETProductResponse.json"), CTProductResponse.class);
        ProductRequest productRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getProductRequest.json"), ProductRequest.class);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        ctProductResponse =cpopProductsHelper.getProtecionPlanProducts(productRequestWrapper);
        assertNotNull(ctProductResponse);
        assertNotNull(ctProductResponse.getProducts());

    }
    @Test
    void testGetEquipmentsProducts(){
        CTProductResponse ctProductResponse  = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/GETProductResponse.json"), CTProductResponse.class);
        ProductRequest productRequest = JsonService.getObjectFromJson(TestUtility.loadJson("/getProductRequest.json"), ProductRequest.class);
        ProductRequestWrapper productRequestWrapper = new ProductRequestWrapper();
        productRequestWrapper.setProductRequest(productRequest);
        productRequestWrapper.getProductRequest().setProductTypes(Collections.singletonList(Constants.VIDEO_ACCESSORY));
        doReturn(ctProductResponse).when(cpopClientHelper).getProducts(any());
        ctProductResponse =cpopProductsHelper.getEquipmentsProducts(productRequestWrapper);
        assertNotNull(ctProductResponse);
        assertNotNull(ctProductResponse.getProducts());
    }

    @Test
    void testFilterOffersByHeartValue() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(TestUtility.loadJson("/acaps/FilterHeartValuesReasonponse.json"), CTOfferResponse.class);
        List<String> listHeartValue = Collections.singletonList(null);
        Boolean retentionPromoIndicator = false;
        String coolOffPeriod = "30";
        String salesChannel = "online";
        String contractIndicator = "Contract";

        try {

            ctOfferResponse =cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, listHeartValue, retentionPromoIndicator, coolOffPeriod, salesChannel, contractIndicator);
            // Assertions
            assertTrue(ctOfferResponse.getOffers().isEmpty());
        } catch (Exception e) {
            // Assertions
            assertTrue(e instanceof NullPointerException);
        }

        // Test case 2: Null offers
        ctOfferResponse.setOffers(null);
        try {
            CTOfferResponse actualResponse = cpopProductsHelper.filterOffersByHeartValue(ctOfferResponse, listHeartValue, retentionPromoIndicator, coolOffPeriod, salesChannel, contractIndicator);
            assertTrue(actualResponse.getOffers().isEmpty());
        } catch (Exception e) {
            assertTrue(e instanceof NullPointerException);
        }
    }

}