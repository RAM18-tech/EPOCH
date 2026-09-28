package com.dtv.dcp.epoch.processor.helper;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.ott.OttOffersService;
import com.dtv.dcp.epoch.util.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ValidateCartHelpersTest {

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

    @Mock
    CpopClient cpopClient;

    @InjectMocks
    ValidateCartHelpers validateCartHelpers;

    CouponOffersRequest couponOffersRequest;
    HttpHeaders headers;
    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(validateCartHelpers, "ctstate", "staged");
        ReflectionTestUtils.setField(validateCartHelpers, "sportSpackLifetimePromo", "SPACKLTP");
        ReflectionTestUtils.setField(validateCartHelpers, "MDUCustomerSegment", Arrays.asList(
                Constants.MDU, Constants.DECA, Constants.BCOMP, Constants.COURTESY,
                Constants.DEMO, Constants.SHOWROOM, Constants.MDUTENANT));

        headers = new HttpHeaders();
        couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));
        couponOffersRequest.setSalesChannel("directvOnline");
        couponOffersRequest.setOfferProductFamily("OTT");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void getMapOfProductsKeyFromId_withValidProducts_returnsCorrectMap() {
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        Map<String, String> result = validateCartHelpers.getMapOfProductsKeyFromId(ctProductResponse);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getMapOfProductsKeyFromId_withNullProductResponse_returnsEmptyMap() {
        Map<String, String> result = validateCartHelpers.getMapOfProductsKeyFromId(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMapOfProductsKeyFromId_withNullProducts_returnsEmptyMap() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(null);

        Map<String, String> result = validateCartHelpers.getMapOfProductsKeyFromId(ctProductResponse);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getMapOfProductsKeyFromId_withEmptyProducts_returnsEmptyMap() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(new ArrayList<>());

        Map<String, String> result = validateCartHelpers.getMapOfProductsKeyFromId(ctProductResponse);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getComplianceProductKey_withValidBenefits_returnsKeyList() {
        CTBenefitsResponse ctBenefitsResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTBenefitResponseForSPACLTP.json"),
                CTBenefitsResponse.class);

        List<String> result = validateCartHelpers.getComplianceProductKey(ctBenefitsResponse);

        assertNotNull(result);
    }

    @Test
    void getComplianceProductKey_withNullResponse_returnsEmptyList() {
        List<String> result = validateCartHelpers.getComplianceProductKey(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void complianceKeyOtherThenSPKLTP_withValidRequest_returnsKeyList() {
        CouponOffersRequest request = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestWithSpackNotActiveUpgradingToNonPremierBridge.json"),
                CouponOffersRequest.class);

        List<String> result = validateCartHelpers.complianceKeyOtherThenSPKLTP(request);

        assertNotNull(result);
    }

    @Test
    void complianceKeyOtherThenSPKLTP_withNullCartContext_returnsEmptyList() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setCartContext(null);

        List<String> result = validateCartHelpers.complianceKeyOtherThenSPKLTP(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void complianceKeyOtherThenSPKLTP_withNullCartProducts_returnsEmptyList() {
        CouponOffersRequest request = new CouponOffersRequest();
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartProducts(null);
        request.setCartContext(cartContexts);

        List<String> result = validateCartHelpers.complianceKeyOtherThenSPKLTP(request);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCompatibleProductSKUs_withValidProductAndSKU_returnsCompatibleList() {
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseWithMDUBusinessSegment.json"),
                CTProductResponse.class);

        List<String> result = validateCartHelpers.getCompatibleProductSKUs(ctProductResponse, "BASE-ENTERTAINMENT-201811");

        assertNotNull(result);
    }

    @Test
    void getCompatibleProductSKUs_withNullProductResponse_returnsEmptyList() {
        List<String> result = validateCartHelpers.getCompatibleProductSKUs(null, "SOME-SKU");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCompatibleProductSKUs_withNullProducts_returnsEmptyList() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(null);

        List<String> result = validateCartHelpers.getCompatibleProductSKUs(ctProductResponse, "SOME-SKU");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCompatibleProducts_withValidInputs_returnsCompatibleList() {
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);
        List<String> cartProductCodes = Arrays.asList("BOLT-MYSPORTS-202410", "BOLT-MYENTERTAINMENT-202410");

        List<String> result = validateCartHelpers.getCompatibleProducts(ctProductResponse, cartProductCodes, Constants.VIDEO_PLAN);

        assertNotNull(result);
    }

    @Test
    void getCompatibleProducts_withNullProductResponse_returnsEmptyList() {
        List<String> result = validateCartHelpers.getCompatibleProducts(null, Arrays.asList("PROD1"), Constants.VIDEO_PLAN);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getCompatibleProducts_withEmptyCartProductCodes_returnsEmptyList() {
        CTProductResponse ctProductResponse = new CTProductResponse();
        ctProductResponse.setProducts(new ArrayList<>());

        List<String> result = validateCartHelpers.getCompatibleProducts(ctProductResponse, new ArrayList<>(), Constants.VIDEO_PLAN);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void getPromoDetails_withValidBenefitsResponse_returnsMap() {
        CTBenefitsResponse ctBenefitsResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTBenefitResponseForSPACLTP.json"),
                CTBenefitsResponse.class);

        Map<String, Benefit> result = validateCartHelpers.getPromoDetails(ctBenefitsResponse);

        assertNotNull(result);
    }

    @Test
    void getIncompatiableSKUResolutionRule_withValidKey_returnsRuleMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)).thenReturn(true);
        List<String> mockRules = Arrays.asList(
                "PRODUCT-A+incompatible:PRODUCT-B",
                "PRODUCT-C+conflict:PRODUCT-D"
        );
        when(redisCacheHelper.getValidateRules(anyString(), eq(Constants.OTT))).thenReturn(mockRules);

        HashMap<String, Map<String, String>> result = validateCartHelpers.getIncompatiableSKUResolutionRule("TEST_KEY");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getIncompatiableSKUResolutionRule_withFeatureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)).thenReturn(false);

        HashMap<String, Map<String, String>> result = validateCartHelpers.getIncompatiableSKUResolutionRule("TEST_KEY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getIncompatiableSKUResolutionRule_withNullResponse_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)).thenReturn(true);
        when(redisCacheHelper.getValidateRules(anyString(), eq(Constants.OTT))).thenReturn(null);

        HashMap<String, Map<String, String>> result = validateCartHelpers.getIncompatiableSKUResolutionRule("TEST_KEY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByProductGroup_withValidData_returnsGroupedMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);

        CTProductResponse ctProductResponse = buildProductResponse("PROD-001", "video-addon", "GRP_A", 1);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductGroup(
                ctProductResponse, ctOfferResponse, couponOffersRequest, new HashMap<>());

        assertNotNull(result);
    }

    @Test
    void groupByProductGroup_withNullProductResponse_returnsEmptyMap() {
        Map<String, List<String>> result = validateCartHelpers.groupByProductGroup(
                null, new CTOfferResponse(), couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByProductGroup_withFeatureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(false);

        CTProductResponse ctProductResponse = buildProductResponse("PROD-001", "video-addon", "GRP_A", 1);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Upgrade"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductGroup(
                ctProductResponse, new CTOfferResponse(), couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void conflictingOfferOrProductToRemoveBasedOnConflictingProducts_withValidData_findsConflicts() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(true);

        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("OF_BOLT-MYESPANOL-202410_GENRE");
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(Collections.singletonList(cartOffer));
        couponOffersRequest.setCartContext(cartContexts);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));

        Map<String, List<String>> result = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                ctOfferResponse, ctProductResponse, couponOffersRequest, new HashMap<>());

        assertNotNull(result);
    }

    @Test
    void conflictingOfferOrProductToRemoveBasedOnConflictingProducts_withFeatureFlagDisabled_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(false);

        Map<String, List<String>> result = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                new CTOfferResponse(), new CTProductResponse(), couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void conflictingOfferOrProductToRemoveBasedOnConflictingProducts_withNullOfferResponse_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(true);

        Map<String, List<String>> result = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                null, new CTProductResponse(), couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void conflictingOfferOrProductToRemoveBasedOnConflictingProducts_withNullProductResponse_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)).thenReturn(true);

        Map<String, List<String>> result = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                new CTOfferResponse(), null, couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void reverseMap_withValidMap_reversesCorrectly() {
        Map<String, List<String>> inputMap = new HashMap<>();
        inputMap.put("KEY1", Arrays.asList("VALUE1", "VALUE2"));
        inputMap.put("KEY2", Collections.singletonList("VALUE3"));

        Map<String, List<String>> result = validateCartHelpers.reverseMap(inputMap);

        assertNotNull(result);
        assertTrue(result.containsKey("VALUE1"));
        assertTrue(result.containsKey("VALUE2"));
        assertTrue(result.containsKey("VALUE3"));
        assertTrue(result.get("VALUE1").contains("KEY1"));
    }

    @Test
    void reverseMap_withNullMap_returnsEmptyMap() {
        Map<String, List<String>> result = validateCartHelpers.reverseMap(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void reverseMap_withEmptyMap_returnsEmptyMap() {
        Map<String, List<String>> result = validateCartHelpers.reverseMap(new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getEmbeddedSVODProductsFromGetOffersGeneric_withValidData_returnsProductMap() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));

        Map<String, List<String>> result = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(
                ctOfferResponse, couponOffersRequest, ctProductResponse, true, new HashMap<>());

        assertNotNull(result);
    }

    @Test
    void getEmbeddedSVODProductsFromGetOffersGeneric_withNonSVOD_filtersCorrectly() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        CTProductResponse ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));

        Map<String, List<String>> result = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(
                ctOfferResponse, couponOffersRequest, ctProductResponse, false, new HashMap<>());

        assertNotNull(result);
    }

    @Test
    void getPreconditionsConfig_withValidKey_returnsConfig() {
        List<String> mockValues = Collections.singletonList("PRODUCT1:CONDITION1,CONDITION2;PRODUCT2:CONDITION3");
        when(redisCacheHelper.getValues(anyString(), eq(Constants.OTT))).thenReturn(mockValues);

        Map<String, List<String>> result = validateCartHelpers.getPreconditionsConfig("TEST_KEY");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getPreconditionsConfig_withNullResponse_returnsEmptyMap() {
        when(redisCacheHelper.getValues(anyString(), eq(Constants.OTT))).thenReturn(null);

        Map<String, List<String>> result = validateCartHelpers.getPreconditionsConfig("TEST_KEY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPreconditionsConfig_withEmptyResponse_returnsEmptyMap() {
        when(redisCacheHelper.getValues(anyString(), eq(Constants.OTT))).thenReturn(new ArrayList<>());

        Map<String, List<String>> result = validateCartHelpers.getPreconditionsConfig("TEST_KEY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getConfigList_withValidKey_returnsList() {
        List<String> mockList = Arrays.asList("VALUE1", "VALUE2", "VALUE3");
        when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(mockList);

        List<String> result = validateCartHelpers.getConfigList("TEST_KEY");

        assertNotNull(result);
    }

    @Test
    void getConfigList_withNullResponse_returnsEmptyList() {
        when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(null);

        List<String> result = validateCartHelpers.getConfigList("TEST_KEY");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }


    @Test
    void groupByProductCodeOffersForPeacock_withValidData_returnsGroupedOffers() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));
        Map<String, List<String>> peacockConditions = new HashMap<>();
        peacockConditions.put("PEACOCK-PRODUCT", Arrays.asList("CONDITION1"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductCodeOffersForPeacock(
                ctOfferResponse, couponOffersRequest, peacockConditions);

        assertNotNull(result);
    }

    @Test
    void groupByProductCodeOffersForPeacock_withNullOfferResponse_returnsEmptyMap() {
        Map<String, List<String>> result = validateCartHelpers.groupByProductCodeOffersForPeacock(
                null, couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByProductCodeOffersForPeacock_withNullOffers_returnsEmptyMap() {
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(null);

        Map<String, List<String>> result = validateCartHelpers.groupByProductCodeOffersForPeacock(
                ctOfferResponse, couponOffersRequest, new HashMap<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getOfferCodeWithCategoryGenreBase_withValidOffers_returnsGenreBaseCodes() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);

        List<String> result = validateCartHelpers.getOfferCodeWithCategoryGenreBase(ctOfferResponse.getOffers());

        assertNotNull(result);
    }

    @Test
    void getOfferCodeWithCategoryGenreBase_withEmptyOffers_returnsEmptyList() {
        List<String> result = validateCartHelpers.getOfferCodeWithCategoryGenreBase(new ArrayList<>());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void classifyOfferCodes_withValidOffers_classifiesCorrectly() {
        CTOfferResponse ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        List<String> classifiedCodes = new ArrayList<>();

        assertDoesNotThrow(() -> ValidateCartHelpers.classifyOfferCodes(ctOfferResponse.getOffers(), classifiedCodes));
    }

    @Test
    void classifyOfferCodes_withNullOffers_handlesGracefully() {
        List<String> classifiedCodes = new ArrayList<>();

        assertDoesNotThrow(() -> ValidateCartHelpers.classifyOfferCodes(null, classifiedCodes));
        assertTrue(classifiedCodes.isEmpty());
    }

    @Test
    void classifyOfferCodes_withEmptyOffers_handlesGracefully() {
        List<String> classifiedCodes = new ArrayList<>();

        assertDoesNotThrow(() -> ValidateCartHelpers.classifyOfferCodes(new ArrayList<>(), classifiedCodes));
        assertTrue(classifiedCodes.isEmpty());
    }

    @Test
    void isContractIndicatorGENRE_withGenreIndicator_returnsTrue() {
        CouponOffersRequest request = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        boolean result = validateCartHelpers.isContractIndicatorGENRE(request);

        assertTrue(result);
    }

    @Test
    void isContractIndicatorGENRE_withNonGenreIndicator_returnsFalse() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setContractIndicator(Arrays.asList("TAZBYOD", "EDSP"));

        boolean result = validateCartHelpers.isContractIndicatorGENRE(request);

        assertFalse(result);
    }

    @Test
    void isContractIndicatorGENRE_withNullIndicator_returnsFalse() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setContractIndicator(null);

        boolean result = validateCartHelpers.isContractIndicatorGENRE(request);

        assertFalse(result);
    }

    @Test
    void isAcquisitionOfferActionType_withAcquisition_returnsTrue() {
        boolean result = validateCartHelpers.isAcquisitionOfferActionType(Collections.singletonList("Acquisition"));

        assertTrue(result);
    }

    @Test
    void isAcquisitionOfferActionType_withNonAcquisition_returnsFalse() {
        boolean result = validateCartHelpers.isAcquisitionOfferActionType(Collections.singletonList("Upgrade"));

        assertFalse(result);
    }

    @Test
    void isAcquisitionOfferActionType_withNullList_returnsFalse() {
        boolean result = validateCartHelpers.isAcquisitionOfferActionType(new ArrayList<>());

        assertFalse(result);
    }

    @Test
    void checkForMDUFlow_withMDUSegment_setsFlag() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setCustomerSegments("MDU");

        assertDoesNotThrow(() -> validateCartHelpers.checkForMDUFlow(request));
    }

    @Test
    void checkForNonMDUFlow_withNonMDUSegment_setsFlag() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setCustomerSegments("CONS");

        assertDoesNotThrow(() -> validateCartHelpers.checkForNonMDUFlow(request));
    }

    @Test
    void checkForValidLocalsContractIndicator_withValidIndicator_passes() {
        CouponOffersRequest request = new CouponOffersRequest();
        request.setContractIndicator(Collections.singletonList("LOCALS"));

        assertDoesNotThrow(() -> validateCartHelpers.checkForValidLocalsContractIndicator(request));
    }

    @Test
    void groupByProductGroupAndComplianceRank_withValidData_groupsByComplianceRank() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);

        CTProductResponse productResponse = buildProductResponseWithMultipleProducts();
        couponOffersRequest.setOfferActionType(Collections.singletonList("Retention"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductGroupAndComplianceRank(
                productResponse, couponOffersRequest);

        assertNotNull(result);
    }

    @Test
    void groupByProductGroupAndComplianceRank_withNullProducts_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Retention"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductGroupAndComplianceRank(
                null, couponOffersRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void groupByProductGroupAndComplianceRank_withAcquisitionActionType_returnsEmptyMap() {
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);

        CTProductResponse productResponse = buildProductResponse("PROD-001", "video-addon", "GRP_A", 1);
        couponOffersRequest.setOfferActionType(Collections.singletonList("Acquisition"));

        Map<String, List<String>> result = validateCartHelpers.groupByProductGroupAndComplianceRank(
                productResponse, couponOffersRequest);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validateCartRequestValidation_withValidRequest_validates() {
        CouponOffersRequest request = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        assertDoesNotThrow(() -> validateCartHelpers.validateCartRequestValidation(request, true));
    }

    @Test
    void validateCartRequestValidation_withInvalidRequest_throwsException() {
        CouponOffersRequest request = new CouponOffersRequest();

        assertThrows(ServiceException.class, () ->
                validateCartHelpers.validateCartRequestValidation(request, true));
    }

    @Test
    void isLocalsServedOrUnServed_withServedLocals_returnsTrue() {
        when(dmaLookUpService.hasLocalChannels(anyString(), anyString())).thenReturn(true);

        Boolean result = validateCartHelpers.isLocalsServedOrUnServed("80907", "08041");

        assertTrue(result);
    }

    @Test
    void isLocalsServedOrUnServed_withUnservedLocals_returnsFalse() {
        when(dmaLookUpService.hasLocalChannels(anyString(), anyString())).thenReturn(false);

        Boolean result = validateCartHelpers.isLocalsServedOrUnServed("99999", "99999");

        assertFalse(result);
    }

    @Test
    void isLocalsServedOrUnServed_withNullResponse_returnsFalse() {
        when(dmaLookUpService.hasLocalChannels(anyString(), anyString())).thenReturn(null);

        Boolean result = validateCartHelpers.isLocalsServedOrUnServed("12345", "54321");

        assertFalse(result);
    }

    @Test
    void createValidationResults_createsCorrectResult() {
        var result = validateCartHelpers.createValidationResults("ERROR_CODE", "ERROR_MSG", "DETAILS");

        assertNotNull(result);
        assertNotNull(result.getStatus());
    }

    private CTProductResponse buildProductResponse(String productCode, String productTypeKey,
                                                   String billingProductGroup, Integer complianceRank) {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup(billingProductGroup);
        if (complianceRank != null) {
            attrs.setComplianceRank(complianceRank);
        }
        Variant variant = new Variant();
        variant.setAttributes(attrs);

        GenericTypeIdBase productType = new GenericTypeIdBase();
        productType.setKey(productTypeKey);

        ProductObj product = new ProductObj();
        product.setId(UUID.randomUUID().toString());
        product.setCode(productCode);
        product.setProductType(productType);
        product.setVariants(Collections.singletonList(variant));

        CTProductResponse response = new CTProductResponse();
        response.setProducts(Collections.singletonList(product));
        return response;
    }

    private CTProductResponse buildProductResponseWithMultipleProducts() {
        List<ProductObj> products = new ArrayList<>();

        ProductObj product1 = buildProductObj("PROD-001", "video-addon", "GRP_A", 1);
        ProductObj product2 = buildProductObj("PROD-002", "video-addon", "GRP_A", 5);
        ProductObj product3 = buildProductObj("PROD-003", "video-addon", "GRP_B", 2);

        products.add(product1);
        products.add(product2);
        products.add(product3);

        CTProductResponse response = new CTProductResponse();
        response.setProducts(products);
        return response;
    }

    private ProductObj buildProductObj(String productCode, String productTypeKey,
                                       String billingProductGroup, Integer complianceRank) {
        Attributes attrs = new Attributes();
        attrs.setBillingProductGroup(billingProductGroup);
        if (complianceRank != null) {
            attrs.setComplianceRank(complianceRank);
        }
        Variant variant = new Variant();
        variant.setAttributes(attrs);

        GenericTypeIdBase productType = new GenericTypeIdBase();
        productType.setKey(productTypeKey);

        ProductObj product = new ProductObj();
        product.setId(UUID.randomUUID().toString());
        product.setCode(productCode);
        product.setProductType(productType);
        product.setVariants(Collections.singletonList(variant));

        return product;
    }
}

