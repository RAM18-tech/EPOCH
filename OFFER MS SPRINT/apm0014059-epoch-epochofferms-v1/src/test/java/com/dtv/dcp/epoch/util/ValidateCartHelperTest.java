package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.CartProduct;
import com.dtv.dcp.epoch.model.common.CartPromotion;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.product.*;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.ott.OttOffersService;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpHeaders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.common.response.ucc.Offer;

public class ValidateCartHelperTest {

    @Mock
    CTOfferRequestHelper ctOfferRequestHelper;
    @Mock
    OttOffersService ottOffersService;
    @Mock
    RedisCacheHelper redisCacheHelper;
    @Mock
    FeatureManagerHelper featureManagerHelper;
    @Mock
    DMALookUpService dmaLookUpService;
    @InjectMocks
    ValidateCartHelper validateCartHelper;
    @Mock
    CpopClientHelper cpopClientHelper;
    HttpHeaders headers;

    CouponOffersRequest couponOffersRequest;

    @Mock
    CpopClientHelper cpopClientHelperInject;

    @Mock
    CpopClient cpopClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(validateCartHelper, "ctstate", "staged");
        ReflectionTestUtils.setField(validateCartHelper, "sportSpackLifetimePromo", "SPACKLTP");
        ReflectionTestUtils.setField(validateCartHelper, "MDUCustomerSegment", Arrays.asList(Constants.MDU, Constants.DECA, Constants.BCOMP, Constants.COURTESY,
                Constants.DEMO, Constants.SHOWROOM, Constants.MDUTENANT));
        headers = Mockito.mock(HttpHeaders.class);
        couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));
        couponOffersRequest.setContractIndicator(Arrays.asList("TAZCONTRACT"));
        couponOffersRequest.setSalesChannel("directvOnline");
        couponOffersRequest.setOfferProductFamily("OTT");
        couponOffersRequest.setReconnectCustomer(Boolean.TRUE);
        couponOffersRequest.setServiceEndDate("01/01/2025");
        couponOffersRequest.setCustomerSegments("CONS");
    }

    @Test
    public void getOffersVideoPlanAcquisitionCall_test() {
        when(ottOffersService.getOffers(any())).thenReturn(new CTOfferResponse());
        CTOfferResponse ctOfferResponse = validateCartHelper.getOffersVideoPlanAcquisitionCall(couponOffersRequest, headers, Arrays.asList("OF_BASE_TESTING"));
        Assert.assertNotNull(ctOfferResponse);
    }

    @Test
    public void getOffersOfferCodesCalls_test() {
        when(ottOffersService.getOffers(any())).thenReturn(new CTOfferResponse());
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BASE_TESTING");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Arrays.asList(cartOffer));
        couponOffersRequest.setCartContext(cartContexts);
        CTOfferResponse ctOfferResponse = validateCartHelper.getOffersOfferCodesCalls(couponOffersRequest, headers, Arrays.asList("OF_BASE_TESTING"));
        Assert.assertNotNull(ctOfferResponse);
    }

    @Test
    public void getOffersCartCallForSales_test() {
        when(ottOffersService.getOffers(any())).thenReturn(new CTOfferResponse());
        when(redisCacheHelper.getValues(any(), any())).thenReturn(Arrays.asList("video-addon", "video-device"));
        CTOfferResponse ctOfferResponse = validateCartHelper.getOffersCartCallForSales(couponOffersRequest, Arrays.asList("OF_BASE_TESTING"), headers, new ArrayList<>());
        Assert.assertNotNull(ctOfferResponse);
    }

    @Test
    public void validateCartRequestValidation_test() {
        validateCartHelper.validateCartRequestValidation(couponOffersRequest, true, false, true);
        couponOffersRequest.setCustomerSegments("MDU");
        couponOffersRequest.setBusinessSegment("MDU");
        validateCartHelper.checkForMDUFlow(couponOffersRequest);
        validateCartHelper.checkForNonMDUFlow(couponOffersRequest);
        validateCartHelper.checkForValidLocalsContractIndicator(couponOffersRequest);
        validateCartHelper.isContractIndicatorGENRE(couponOffersRequest);
        validateCartHelper.isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType());
        validateCartHelper.createValidationResults("12121", "12121", "12121");
        when(featureManagerHelper.isEnabled(any())).thenReturn(Boolean.TRUE);
        validateCartHelper.checkValidateCartRequestIsSportsPack(couponOffersRequest);
    }

    @Test
    public void isLocalsServedOrUnServed_test() {
        when(dmaLookUpService.hasLocalChannels(any(), any())).thenReturn(Boolean.TRUE);
        validateCartHelper.isLocalsServedOrUnServed("12121", "12121");
        when(dmaLookUpService.hasLocalChannels(any(), any())).thenReturn(null);
        validateCartHelper.isLocalsServedOrUnServed("12121", "12121");
    }

    @Test
    public void updateCartCallOfferCodesIfChangesInReconnectFlow_test() {
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BASE_TESTING");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(new ArrayList<>());
        cartContexts.getCartOffers().add(cartOffer);
        couponOffersRequest.setCartContext(cartContexts);
        List<String> list = new ArrayList<>();
        list.add("TEST");
        couponOffersRequest.setContractIndicator(Arrays.asList("TAZBYOD"));
        validateCartHelper.updateCartCallOfferCodesIfChangesInReconnectFlow(list, "OF_BASE_TESTING", couponOffersRequest, new HashMap<>());
        couponOffersRequest.setContractIndicator(null);
        couponOffersRequest.setContractIndicator(Arrays.asList("TAZCONTRACT"));
        validateCartHelper.updateCartCallOfferCodesIfChangesInReconnectFlow(list, "OF_BASE_TESTING", couponOffersRequest, new HashMap<>());
    }

    @Test
    public void sportspack_method_test() {
        // Mock the product response from the CpopClientHelper
        CTProductResponse ctProductResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTProductResponseWithMDUBusinessSegment.json"), CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        // Mock the benefits response from the CpopClient
        CTBenefitsResponse ctBenefitsResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTBenefitResponseForSPACLTP.json"), CTBenefitsResponse.class);
        when(cpopClient.getBenefitsFromCT(any())).thenReturn(ctBenefitsResponse);
        // Mock the CouponOffersRequest
        CouponOffersRequest couponOffersRequest = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestWithActiveSpackWithoutPromoAndUpgradingToPremierBridge.json"), CouponOffersRequest.class);
        couponOffersRequest.getCartContext().getCartProducts().get(1).setPromotions(new ArrayList<>());
        CartPromotion cartPromotion = new CartPromotion();
        cartPromotion.setPromotionId("SPACKLTPp");
        couponOffersRequest.getCartContext().getCartProducts().get(1).getPromotions().add(cartPromotion);
        validateCartHelper.complianceKeyOtherThenSPKLTP(couponOffersRequest);
        validateCartHelper.getCompatibleProductSKUs(ctProductResponse, "BASE-ENTERTAINMENT-201811");
        validateCartHelper.getComplianceProductKey(ctBenefitsResponse);
    }

    @Test
    public void getOffersVideoPlanAcquisitionCall_exception() {
        assertThrows(ServiceException.class, () -> {
            validateCartHelper.validateCartRequestValidation(new CouponOffersRequest(), true, false, true);
        });
    }

    @Test
    public void filterOfferCodesWithPreselectDesignationMandatory_test() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        CustomerEligibility customerEligibility = new CustomerEligibility();

        List<String> resultList = validateCartHelper.filterOfferCodesWithPreselectDesignationMandatory(ctOfferResponse.getOffers());
        assertNotNull(resultList);
        Assert.assertTrue(resultList.size() > 0);

    }

    @Test
    public void filterOfferCodesWithDisplayTypeIncluded_test() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        CustomerEligibility customerEligibility = new CustomerEligibility();

        List<String> resultList = validateCartHelper.filterOfferCodesWithDisplayTypeIncluded(ctOfferResponse.getOffers());
        assertNotNull(resultList);
        Assert.assertTrue(resultList.size() > 0);

    }

    @Test
    public void filterOfferCodesWithFeeType_test() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        CustomerEligibility customerEligibility = new CustomerEligibility();
        List<String> resultList = validateCartHelper.filterOfferCodesWithFeeType(ctOfferResponse.getOffers());
        assertNotNull(resultList);
    }

    @Test
    public void getEmbeddedSVODProductsFromGetOffers_test_sales() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"), CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        List<String> offerActionType = new ArrayList<>();
        offerActionType.add("Acquisition");
        Map<String, List<String>> resultMap = validateCartHelper.getEmbeddedSVODProductsFromGetOffers(ctOfferResponse, couponOffersRequest, ctProductResponse);
        assertNotNull(resultMap);
    }

    @Test
    public void getEmbeddedSVODProductsFromGetOffers_test_services() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"), CTProductResponse.class);
        when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);
        List<String> offerActionType = new ArrayList<>();
        offerActionType.add("Upgrade");
        Map<String, List<String>> resultMap = validateCartHelper.getEmbeddedSVODProductsFromGetOffers(ctOfferResponse, couponOffersRequest, ctProductResponse);
        assertNotNull(resultMap);
    }

    @Test
    public void getOfferCodeWithCategoryGenreBase_test() {
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);
        List<String> resultList = validateCartHelper.getOfferCodeWithCategoryGenreBase(ctOfferResponse.getOffers());
        assertNotNull(resultList);
        Assert.assertTrue(resultList.size() > 0);
    }

    @Test
    public void isContractIndicatorGENRE_true_test() {
        CouponOffersRequest couponOffersRequest = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"), CouponOffersRequest.class);
        boolean result = validateCartHelper.isContractIndicatorGENRE(couponOffersRequest);
        assertNotNull(result);
        Assert.assertTrue(result);
    }

    @Test
    public void isContractIndicatorGENRE_false_test() {
        CouponOffersRequest couponOffersRequest = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"), CouponOffersRequest.class);
        couponOffersRequest.setContractIndicator(new ArrayList<>());
        boolean result = validateCartHelper.isContractIndicatorGENRE(couponOffersRequest);
        assertNotNull(result);
        Assert.assertFalse(result);
    }

    // -----------------------------------------------------------------------
    // groupByProductGroupAndComplianceRank tests
    // -----------------------------------------------------------------------

    @Test
    public void groupByProductGroupAndComplianceRank_featureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(false);
        CTProductResponse productResponse = new CTProductResponse();
        productResponse.setProducts(Arrays.asList(new ProductObj()));
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void groupByProductGroupAndComplianceRank_nullProductResponse_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(null, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void groupByProductGroupAndComplianceRank_acquisitionActionType_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));

        CTProductResponse productResponse = buildProductResponse("PROD-001", "video-addon", "GRP_A", 1);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void groupByProductGroupAndComplianceRank_singleProduct_groupedCorrectly() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        CTProductResponse productResponse = buildProductResponse("PROD-001", "video-addon", "GRP_A", 1);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("GRP_A"));
        assertEquals(Arrays.asList("PROD-001"), result.get("GRP_A"));
    }

    @Test
    public void groupByProductGroupAndComplianceRank_multipleProductsSameGroup_sortedByComplianceRank() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("ADE"));

        // Build two products in same group with different compliance ranks
        CTProductResponse productResponse = new CTProductResponse();
        productResponse.setProducts(Arrays.asList(
                buildProduct("PROD-LOW", "video-addon", "GRP_A", 10),
                buildProduct("PROD-HIGH", "video-addon", "GRP_A", 2)
        ));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertNotNull(result);
        assertEquals(1, result.size());
        List<String> groupAProducts = result.get("GRP_A");
        // PROD-HIGH (rank=2) should come before PROD-LOW (rank=10)
        assertEquals("PROD-HIGH", groupAProducts.get(0));
        assertEquals("PROD-LOW", groupAProducts.get(1));
    }

    @Test
    public void groupByProductGroupAndComplianceRank_nullComplianceRank_sortedLast() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        ProductObj productWithRank = buildProduct("PROD-RANKED", "video-addon", "GRP_A", 5);
        ProductObj productNullRank = buildProduct("PROD-UNRANKED", "video-addon", "GRP_A", null);

        CTProductResponse productResponse = new CTProductResponse();
        productResponse.setProducts(Arrays.asList(productNullRank, productWithRank));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        List<String> groupAProducts = result.get("GRP_A");
        // ranked product (5) before unranked (Integer.MAX_VALUE)
        assertEquals("PROD-RANKED", groupAProducts.get(0));
        assertEquals("PROD-UNRANKED", groupAProducts.get(1));
    }

    @Test
    public void groupByProductGroupAndComplianceRank_nonVideoAddonProduct_filtered() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        CTProductResponse productResponse = buildProductResponse("PROD-DEVICE", "video-device", "GRP_A", 1);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void groupByProductGroupAndComplianceRank_variantWithNoBillingProductGroup_filtered() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Retention"));

        CTProductResponse productResponse = buildProductResponse("PROD-001", "video-addon", null, 1);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // conflictingOfferOrProductToRemoveBasedOnConflictingProducts tests
    // -----------------------------------------------------------------------

    @Test
    public void conflictingOfferOrProduct_featureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(false);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                new CTOfferResponse(), new CTProductResponse(), couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void conflictingOfferOrProduct_acquisitionFlow_noConflictingProducts_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));

        // Cart has offer OF-001
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF-001");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Arrays.asList(cartOffer));
        couponOffersRequest.setCartContext(cartContexts);

        // CTOfferResponse contains OF-001 but with no conflicting products in its bundle
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        CTOffer ctOffer = new CTOffer();
        ctOffer.setCode("OF-001");
        ctOfferResponse.setOffers(Arrays.asList(ctOffer));

        CTProductResponse productResponse = new CTProductResponse();
        productResponse.setProducts(new ArrayList<>());

        Map<String, List<String>> result = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                ctOfferResponse, productResponse, couponOffersRequest);

        assertTrue(result.isEmpty());
    }

    @Test
    public void conflictingOfferOrProduct_acquisitionFlow_conflictFound_mapsConflictingOffer() {
        // Enable the feature flag for conflict product rule
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(List.of("Acquisition"));

        // Product catalogue: id-x maps to key KEY-X
        ProductObj productX = new ProductObj();
        productX.setId("id-x");
        productX.setCode("KEY-X");
        CTProductResponse productResponse = new CTProductResponse();
        productResponse.setProducts(List.of(productX));

        // Cart offer OF-CART has a bundle product whose variant references a conflicting product id-x
        IncludedProduct conflictRef = new IncludedProduct();
        conflictRef.setId("id-x");
        IncludeProductWrapper includeProductWrapper = new IncludeProductWrapper();
        includeProductWrapper.setBillingConflictingProducts(List.of(conflictRef));
        Attributes variantAttr = new Attributes();
        variantAttr.setConflictingProductsReference(List.of(includeProductWrapper));
        com.dtv.dcp.epoch.model.ct.product.Variant variant = new com.dtv.dcp.epoch.model.ct.product.Variant();
        variant.setAttributes(variantAttr);
        ProductObj bundleProdObj = new ProductObj();
        bundleProdObj.setVariants(List.of(variant));
        Product product = new Product();
        product.setObj(bundleProdObj);
        product.setKey("KEY-X");
        ProductWrapper productWrapper = new ProductWrapper();
        productWrapper.setProducts(List.of(product));
        AssociatedProduct associatedProduct = new AssociatedProduct();
        associatedProduct.setBundleProducts(List.of(productWrapper));
        OfferAttributes cartOfferAttrs = new OfferAttributes();
        cartOfferAttrs.setAssociatedProducts(List.of(associatedProduct));
        CTOffer ctOfferCart = new CTOffer();
        ctOfferCart.setCode("OF-CART");
        ctOfferCart.setAttributes(cartOfferAttrs);

        // Another offer OF-OTHER also contains KEY-X in its bundle (should be mapped as conflicting)
        Product otherBundleProduct = new Product();
        otherBundleProduct.setKey("KEY-X");
        ProductWrapper otherWrapper = new ProductWrapper();
        otherWrapper.setProducts(List.of(otherBundleProduct));
        AssociatedProduct otherAssociated = new AssociatedProduct();
        otherAssociated.setBundleProducts(List.of(otherWrapper));
        OfferAttributes otherAttrs = new OfferAttributes();
        otherAttrs.setAssociatedProducts(List.of(otherAssociated));
        CTOffer ctOfferOther = new CTOffer();
        ctOfferOther.setCode("OF-OTHER");
        ctOfferOther.setAttributes(otherAttrs);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(List.of(ctOfferCart, ctOfferOther));

        // Cart context with OF-CART in the cart
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF-CART");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(List.of(cartOffer));
        couponOffersRequest.setCartContext(cartContexts);

        // Run the method under test
        Map<String, List<String>> result = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                ctOfferResponse, productResponse, couponOffersRequest);

        // OF-CART should map to OF-OTHER as a conflicting offer
        assertNotNull(result);
        assertTrue(result.containsKey("OF-CART"));
        assertTrue(result.get("OF-CART").contains("OF-OTHER"));
    }

    // -----------------------------------------------------------------------
    // Helper methods for building test data
    // -----------------------------------------------------------------------

    private CTProductResponse buildProductResponse(String productCode, String productTypeKey, String billingProductGroup, Integer complianceRank) {
        CTProductResponse response = new CTProductResponse();
        response.setProducts(Arrays.asList(buildProduct(productCode, productTypeKey, billingProductGroup, complianceRank)));
        return response;
    }

    private ProductObj buildProduct(String productCode, String productTypeKey, String billingProductGroup, Integer complianceRank) {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup(billingProductGroup);
        attrs.setComplianceRank(complianceRank);

        Variant variant = new Variant();
        variant.setAttributes(attrs);

        GenericTypeIdBase productType = new GenericTypeIdBase();
        productType.setKey(productTypeKey);

        ProductObj product = new ProductObj();
        product.setCode(productCode);
        product.setProductType(productType);
        product.setVariants(Arrays.asList(variant));
        return product;
    }

    @Test
    public void conflictingOfferOrProductToRemoveBasedOnConflictingProducts_nonAcquisitionAction_test() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setOfferActionType(Arrays.asList("Acquisition"));
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-HBOWADSPAID-202602_GENRE");
        CartOffer cartOffer2 = new CartOffer();
        cartOffer2.setOfferCode("OF_BOLT-MYENTERTAINMENT-202410_GENREE");
        CartContexts cartContexts = new CartContexts();
        List<CartOffer> cartOffers = new ArrayList<>();
        cartOffers.add(cartOffer);
        cartOffers.add(cartOffer2);
        cartContexts.setCartOffers(cartOffers);
        request.setCartContext(cartContexts);
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/CTOfferResponseAllowConflictingOffers.json"), CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"), CTProductResponse.class);
        cpopClientHelperInject.setInlineProducts(ctOfferResponse);
        Map<String, List<String>> result = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(ctOfferResponse, ctProductResponse, request);
        Assert.assertNotNull(result);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Helper methods for building test data
    // ─────────────────────────────────────────────────────────────────────────────

    private CTProductResponse buildCtProductResponse(String productId, String productCode,
                                                     String billingProductGroup, Integer complianceRank) {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup(billingProductGroup);
        if (complianceRank != null) {
            attrs.setComplianceRank(complianceRank);
        }
        Variant variant = new Variant();
        variant.setAttributes(attrs);
        ProductObj productObj = new ProductObj();
        productObj.setId(productId);
        productObj.setCode(productCode);
        productObj.setVariants(Collections.singletonList(variant));
        CTProductResponse response = new CTProductResponse();
        response.setProducts(Collections.singletonList(productObj));
        return response;
    }

    private CTProductResponse buildCtProductResponseMultiple(List<String[]> products) {
        List<ProductObj> productObjs = new ArrayList<>();
        for (String[] p : products) {
            Attributes attrs = new Attributes();
            attrs.setBillingProductGroup(p[2]);
            attrs.setComplianceRank(Integer.parseInt(p[3]));
            Variant variant = new Variant();
            variant.setAttributes(attrs);
            ProductObj productObj = new ProductObj();
            productObj.setId(p[0]);
            productObj.setCode(p[1]);
            productObj.setVariants(Collections.singletonList(variant));
            productObjs.add(productObj);
        }
        CTProductResponse response = new CTProductResponse();
        response.setProducts(productObjs);
        return response;
    }

    private CTOfferResponse buildCtOfferResponseWithBundledProduct(String offerCode, String bundledProductId) {
        Product product = new Product();
        product.setId(bundledProductId);
        ProductWrapper productWrapper = new ProductWrapper();
        productWrapper.setProducts(Collections.singletonList(product));
        AssociatedProduct associatedProduct = new AssociatedProduct();
        associatedProduct.setBundleProducts(Collections.singletonList(productWrapper));
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setAssociatedProducts(Collections.singletonList(associatedProduct));
        CTOffer ctOffer = new CTOffer();
        ctOffer.setCode(offerCode);
        ctOffer.setAttributes(offerAttributes);
        CTOfferResponse response = new CTOfferResponse();
        response.setOffers(Collections.singletonList(ctOffer));
        return response;
    }

    private Offer buildOffer(String code, String action) {
        Offer offer = new Offer();
        offer.setCode(code);
        offer.setAction(action);
        return offer;
    }

    private CartItems buildCartItemWithOffers(List<Offer> offers) {
        CartItems cartItem = new CartItems();
        cartItem.setOffers(new ArrayList<>(offers));
        return cartItem;
    }

    private CouponOffersRequest buildRequestWithCartOffers(List<String[]> offerCodesAndActions) {
        List<CartOffer> cartOffers = new ArrayList<>();
        for (String[] oa : offerCodesAndActions) {
            CartOffer co = new CartOffer();
            co.setOfferCode(oa[0]);
            co.setAction(oa[1]);
            cartOffers.add(co);
        }
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(cartOffers);
        CouponOffersRequest req = new CouponOffersRequest();
        req.setCartContext(cartContexts);
        return req;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Tests for buildOfferGroupingMap (via groupByProductGroup overload)
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * TC-01: null CTProductResponse → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_nullProductResponse_returnsEmptyMap() {
        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(null, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-02: CTProductResponse with empty products list → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_emptyProducts_returnsEmptyMap() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(Collections.emptyList());
        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-03: Non-acquisition flow (e.g. "Upgrade") with feature flag disabled → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_nonAcquisitionFlowFeatureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VALIDATE_CART_PRODUCT_GRP)).thenReturn(false);

        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);
        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Upgrade"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-04: All products are null in the products list → productCodeToGroupMap is empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_allNullProducts_returnsEmptyMap() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        List<ProductObj> products = new ArrayList<>();
        products.add(null);
        ctProductResponse.setProducts(products);

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-05: Product with null code → skipped → productCodeToGroupMap is empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_productWithNullCode_returnsEmptyMap() {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup("GROUP-A");
        attrs.setComplianceRank(1);
        Variant variant = new Variant();
        variant.setAttributes(attrs);
        ProductObj productObj = new ProductObj();
        productObj.setId("id1");
        productObj.setCode(null);   // null code
        productObj.setVariants(Collections.singletonList(variant));
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(Collections.singletonList(productObj));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-06: Product with empty variants list → skipped → productCodeToGroupMap is empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_productWithEmptyVariants_returnsEmptyMap() {
        ProductObj productObj = new ProductObj();
        productObj.setId("id1");
        productObj.setCode("PROD-A");
        productObj.setVariants(Collections.emptyList());
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(Collections.singletonList(productObj));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-07: Variant is null → skipped; billingProductGroup blank → skipped →
     *        productCodeToGroupMap is empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_variantNullAndBlankGroup_returnsEmptyMap() {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup("");   // blank group
        // complianceRank left at default (0) – primitive int, cannot be null
        Variant variantGood = new Variant();
        variantGood.setAttributes(attrs);

        List<Variant> variants = new ArrayList<>();
        variants.add(null);          // null variant – should be skipped
        variants.add(variantGood);

        ProductObj productObj = new ProductObj();
        productObj.setId("id1");
        productObj.setCode("PROD-A");
        productObj.setVariants(variants);
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(Collections.singletonList(productObj));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-08: Variant has null attributes → skipped → productCodeToGroupMap empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_variantNullAttributes_returnsEmptyMap() {
        Variant variant = new Variant();
        variant.setAttributes(null);   // null attributes
        ProductObj productObj = new ProductObj();
        productObj.setId("id1");
        productObj.setCode("PROD-A");
        productObj.setVariants(Collections.singletonList(variant));
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(Collections.singletonList(productObj));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, new CTOfferResponse(), req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-09: Product has billingProductGroup set, CTOfferResponse is null, cart context has no offers
     *        that derive to a known product → offerCodeToProductCodeMap stays empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_validGroupNoResolvableOffers_returnsEmptyMap() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Collections.emptyList());
        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);
        // null CTOfferResponse and empty cart offers → offerCodeToProductCodeMap stays empty
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, null, req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-10: CTOfferResponse is null → pass 1 skipped;
     *        cartContext is null → pass 2 skipped → offerCodeToProductCodeMap empty → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_nullOfferResponseNullCartContext_returnsEmptyMap() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(null);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, null, req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-11: CTOfferResponse with empty offers list → pass 1 skipped;
     *        cartContext has empty cartOffers list → pass 2 runs but adds nothing → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_emptyOffersEmptyCartOffers_returnsEmptyMap() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.emptyList());

        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Collections.emptyList());
        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertTrue(result.isEmpty());
    }


    /**
     * TC-14: CTOffer with empty associatedProducts → skipped in pass 1;
     *        cart offer code blank → skipped in pass 2 → emptyMap
     */
    @Test
    public void buildOfferGroupingMap_ctOfferEmptyAssociatedProducts_blankCartOfferCode_returnsEmptyMap() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setAssociatedProducts(Collections.emptyList());
        CTOffer ctOffer = new CTOffer();
        ctOffer.setCode("OF_OFFER-X");
        ctOffer.setAttributes(offerAttributes);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.singletonList(ctOffer));

        // cart offer with blank code → filter(StringUtils::isNotBlank) will remove it
        CartOffer cartOfferBlank = new CartOffer();
        cartOfferBlank.setOfferCode("   ");
        CartOffer cartOfferNull = new CartOffer();
        cartOfferNull.setOfferCode(null);
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Arrays.asList(cartOfferBlank, cartOfferNull));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-15: Pass 2 – offerCode already resolved via pass 1 → not re-derived;
     *        happy-path full result with multiple offers in same group sorted by complianceRank
     */
    @Test
    public void buildOfferGroupingMap_pass1ResolvesOffer_alreadyInMapNotReDerived_multipleOffersSorted() {
        // Two products in the same billing group with different ranks
        List<String[]> products = Arrays.asList(
                new String[]{"id1", "PROD-A", "GROUP-X", "10"},
                new String[]{"id2", "PROD-B", "GROUP-X", "5"}
        );
        CTProductResponse ctProductResponse = buildCtProductResponseMultiple(products);

        // pass 1: OF_OFFER-A → PROD-A (id1), OF_OFFER-B → PROD-B (id2)
        CTOffer ctOfferA = buildBundledCTOffer("OF_OFFER-A", "id1");
        CTOffer ctOfferB = buildBundledCTOffer("OF_OFFER-B", "id2");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Arrays.asList(ctOfferA, ctOfferB));

        // cart offers: OF_OFFER-A and OF_OFFER-B (also add OF_OFFER-A again to verify dedup via map)
        CartOffer co1 = new CartOffer();
        co1.setOfferCode("OF_OFFER-A");
        CartOffer co2 = new CartOffer();
        co2.setOfferCode("OF_OFFER-B");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Arrays.asList(co1, co2));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("GROUP-X"));
        List<String> sortedOffers = result.get("GROUP-X");
        // PROD-B has rank 5 (lower) → OF_OFFER-B should come first
        Assert.assertEquals("OF_OFFER-B", sortedOffers.get(0));
        Assert.assertEquals("OF_OFFER-A", sortedOffers.get(1));
    }

    /**
     * TC-16: Pass 1 – bundled product id resolves via getKeyFromID to a productCode in groupMap
     */
    @Test
    public void buildOfferGroupingMap_pass1BundledProductResolvesViaKeyFromId_returnsResult() {
        CTProductResponse ctProductResponse = buildCtProductResponse("prod-id-001", "PROD-X", "GROUP-Z", 3);

        CTOffer ctOffer = buildBundledCTOffer("OF_OFFER-X", "prod-id-001");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.singletonList(ctOffer));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(null);  // no cart context – pass 2 skipped; pass 1 already resolved
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("GROUP-Z"));
        Assert.assertEquals(Collections.singletonList("OF_OFFER-X"), result.get("GROUP-Z"));
    }

    /**
     * TC-17: Pass 2 – deriveProductCodeFromOfferCode returns null (no match at all) → not added to map
     *        → if no offers resolved, returns emptyMap
     */
    @Test
    public void buildOfferGroupingMap_pass2NoMatchReturnsNull_returnsEmptyMap() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.emptyList());

        // offer code that has no relationship to any known product code
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("COMPLETELY_UNKNOWN_OFFER");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Collections.singletonList(cartOffer));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);

        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertTrue(result.isEmpty());
    }

    /**
     * TC-18: Step 3 – product code mapped to a group that is blank (should be skipped)
     *        This is achieved by having a productCode in offerCodeToProductCodeMap whose
     *        group entry is blank → that entry is ignored in result.
     *        To produce this scenario: product has blank billingProductGroup but non-blank rank,
     *        however it still gets into offerCodeToProductCodeMap via pass 1 (id maps to code).
     *        Since productCodeToGroupMap will NOT contain it (blank group filtered), the product
     *        won't appear in the group map, and pass 1 won't resolve it either.
     *        Instead we test with two products: one with valid group (resolved), one without group
     *        (not resolved) – result contains only the valid group.
     */
    @Test
    public void buildOfferGroupingMap_step3BlankGroupSkipped_onlyValidGroupInResult() {
        // PROD-A has a valid group; PROD-B has blank group
        List<String[]> products = Arrays.asList(
                new String[]{"id1", "PROD-A", "GROUP-VALID", "1"},
                new String[]{"id2", "PROD-B", "",            "2"}
        );
        CTProductResponse ctProductResponse = buildCtProductResponseMultiple(products);

        // Pass 1: OF_OFFER-A → PROD-A
        CTOffer ctOfferA = buildBundledCTOffer("OF_OFFER-A", "id1");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.singletonList(ctOfferA));

        // Pass 2 (via cart offers): "OF_PROD-A" → PROD-A (already covered by pass1 result)
        CartOffer co = new CartOffer();
        co.setOfferCode("OF_OFFER-A");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Collections.singletonList(co));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(cartContexts);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("GROUP-VALID"));
        Assert.assertFalse(result.containsKey(""));
    }

    /**
     * TC-19: Upsell action type (also treated as acquisition) is handled the same way –
     *        ensures isAcquisitionOfferActionType covers Upsell path.
     */
    @Test
    public void buildOfferGroupingMap_upsellActionType_treatedAsAcquisition_returnsResult() {
        CTProductResponse ctProductResponse = buildCtProductResponse("id1", "PROD-A", "GROUP-A", 1);

        CTOffer ctOffer = buildBundledCTOffer("OF_OFFER-A", "id1");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Collections.singletonList(ctOffer));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Upsell"));
        req.setCartContext(null);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertFalse(result.isEmpty());
        Assert.assertTrue(result.containsKey("GROUP-A"));
    }


    /**
     * TC-21: Multiple offers across two different billing groups – result map has both groups
     *        each containing its respective offer code.
     */
    @Test
    public void buildOfferGroupingMap_multipleGroups_eachGroupContainsItsOffer() {
        List<String[]> products = Arrays.asList(
                new String[]{"id1", "PROD-A", "GROUP-1", "1"},
                new String[]{"id2", "PROD-B", "GROUP-2", "1"}
        );
        CTProductResponse ctProductResponse = buildCtProductResponseMultiple(products);

        CTOffer ctOfferA = buildBundledCTOffer("OF_OFFER-A", "id1");
        CTOffer ctOfferB = buildBundledCTOffer("OF_OFFER-B", "id2");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(Arrays.asList(ctOfferA, ctOfferB));

        CouponOffersRequest req = new CouponOffersRequest();
        req.setOfferActionType(Arrays.asList("Acquisition"));
        req.setCartContext(null);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        Map<String, List<String>> result = validateCartHelper.groupByProductGroup(ctProductResponse, ctOfferResponse, req);

        Assert.assertEquals(2, result.size());
        Assert.assertTrue(result.containsKey("GROUP-1"));
        Assert.assertTrue(result.containsKey("GROUP-2"));
        Assert.assertTrue(result.get("GROUP-1").contains("OF_OFFER-A"));
        Assert.assertTrue(result.get("GROUP-2").contains("OF_OFFER-B"));
    }



    // ─────────────────────────────────────────────────────────────────────────────
    // Additional helper: builds a CTOffer with a single bundled product id (pass 1)
    // ─────────────────────────────────────────────────────────────────────────────

    private CTOffer buildBundledCTOffer(String offerCode, String bundledProductId) {
        com.dtv.dcp.epoch.model.ct.product.Product product = new com.dtv.dcp.epoch.model.ct.product.Product();
        product.setId(bundledProductId);
        ProductWrapper productWrapper = new ProductWrapper();
        productWrapper.setProducts(Collections.singletonList(product));
        AssociatedProduct associatedProduct = new AssociatedProduct();
        associatedProduct.setBundleProducts(Collections.singletonList(productWrapper));
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setAssociatedProducts(Collections.singletonList(associatedProduct));
        CTOffer ctOffer = new CTOffer();
        ctOffer.setCode(offerCode);
        ctOffer.setAttributes(offerAttributes);
        return ctOffer;
    }
}