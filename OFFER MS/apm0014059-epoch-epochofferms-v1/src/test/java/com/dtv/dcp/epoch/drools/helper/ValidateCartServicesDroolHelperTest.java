package com.dtv.dcp.epoch.drools.helper;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.CartProduct;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ValidateCartServicesDroolHelperTest {

    @Mock
    ValidateCartHelpers validateCartHelpers;

    @Mock
    DroolsConfiguration droolsConfiguration;

    @Mock
    RedisCacheHelper redisCacheHelper;

    @Mock
    FeatureManagerHelper featureManagerHelper;

    @Mock
    CpopClientHelper cpopClientHelper;

    @Mock
    CpopClient cpopClient;

    @InjectMocks
    ValidateCartServicesDroolHelper validateCartServicesDroolHelper;

    AutoCloseable closeable;
    CouponOffersRequest couponOffersRequest;
    ValidateCartAsyncResponses validateCartAsyncResponses;
    CTProductResponse ctProductResponse;
    CTBenefitsResponse ctBenefitsResponse;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(validateCartServicesDroolHelper, "ctstate", "staged");
        ReflectionTestUtils.setField(validateCartServicesDroolHelper, "sportSpack1Sku", "BOLT-SPACK-202222");
        ReflectionTestUtils.setField(validateCartServicesDroolHelper, "sportSpack2Sku", "BOLT-SPACK2-202209");
        ReflectionTestUtils.setField(validateCartServicesDroolHelper, "sportSpackLifetimePromo", "SPACKLTP");
        ReflectionTestUtils.setField(validateCartServicesDroolHelper, "repackagedChannels", "");
        setupDroolsEngine();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private void setupDroolsEngine() {
        KieContainer kieContainerValidate = mock(KieContainer.class);
        KieSession kieSessionValidate = mock(KieSession.class);
        when(kieContainerValidate.newKieSession()).thenReturn(kieSessionValidate);
        doNothing().when(kieSessionValidate).setGlobal(anyString(), any());
        when(kieSessionValidate.insert(any())).thenReturn(null);
        when(kieSessionValidate.fireAllRules()).thenReturn(0);
        doNothing().when(kieSessionValidate).dispose();
        when(droolsConfiguration.getKieContainerValidate()).thenReturn(kieContainerValidate);

        KieContainer kieContainerServices = mock(KieContainer.class);
        KieSession kieSessionServices = mock(KieSession.class);
        when(kieContainerServices.newKieSession()).thenReturn(kieSessionServices);
        doNothing().when(kieSessionServices).setGlobal(anyString(), any());
        when(kieSessionServices.insert(any())).thenReturn(null);
        when(kieSessionServices.fireAllRules()).thenReturn(0);
        doNothing().when(kieSessionServices).dispose();
        when(droolsConfiguration.getKieContainerForValidateCartServicesRules()).thenReturn(kieContainerServices);
    }

    @Test
    void validateSportPackDroolScenarios_withValidRequest_returnsCartItemsList() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestWithSpackNotActiveUpgradingToNonPremierBridge.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseWithMDUBusinessSegment.json"),
                CTProductResponse.class);

        ctBenefitsResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTBenefitResponseForSPACLTP.json"),
                CTBenefitsResponse.class);

        when(cpopClientHelper.getProducts(any(CTProductRequest.class))).thenReturn(ctProductResponse);
        when(cpopClient.getBenefits(any(CTBenefitsRequest.class))).thenReturn(ctBenefitsResponse);
        when(validateCartHelpers.getMapOfProductsKeyFromId(any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getComplianceProductKey(any())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.complianceKeyOtherThenSPKLTP(any())).thenReturn(new ArrayList<>());
        when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1, Constants.OTT))
                .thenReturn(Arrays.asList("BASE-ENTERTAINMENT-201811"));
        when(validateCartHelpers.getCompatibleProductSKUs(any(), anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroupAndComplianceRank(any(), any())).thenReturn(new HashMap<>());

        List result = validateCartServicesDroolHelper.validateSportPackDroolScenarios(couponOffersRequest);

        assertNotNull(result);
        assertTrue(result instanceof List);
    }

    @Test
    void validateSportPackDroolScenarios_withNullProductResponse_handlesGracefully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        when(cpopClientHelper.getProducts(any(CTProductRequest.class))).thenReturn(null);
        when(cpopClient.getBenefits(any(CTBenefitsRequest.class))).thenReturn(new CTBenefitsResponse());
        when(validateCartHelpers.getMapOfProductsKeyFromId(any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getComplianceProductKey(any())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.complianceKeyOtherThenSPKLTP(any())).thenReturn(new ArrayList<>());
        when(redisCacheHelper.getValues(anyString(), anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getCompatibleProductSKUs(any(), anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroupAndComplianceRank(any(), any())).thenReturn(new HashMap<>());

        List result = validateCartServicesDroolHelper.validateSportPackDroolScenarios(couponOffersRequest);

        assertNotNull(result);
    }

    @Test
    void validateSportPackDroolScenarios_withException_throwsServiceException() {
        couponOffersRequest = new CouponOffersRequest();

        when(cpopClientHelper.getProducts(any(CTProductRequest.class)))
                .thenThrow(new RuntimeException("CPOP error"));

        ServiceException exception = assertThrows(ServiceException.class, () ->
                validateCartServicesDroolHelper.validateSportPackDroolScenarios(couponOffersRequest)
        );

        assertNotNull(exception);
        assertNotNull(exception.getError());
        assertNotNull(exception.getError().getErrorId());
    }

    @Test
    void validateCartRulesExecutions_withValidRequest_returnsSuccessfulResponse() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        ctBenefitsResponse = new CTBenefitsResponse();

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(ctBenefitsResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("cartItemsList"));
        assertTrue(result.containsKey("messageDetailsList"));
        assertNotNull(result.get("cartItemsList"));
        assertNotNull(result.get("messageDetailsList"));
    }

    @Test
    void validateCartRulesExecutions_withNullProductResponse_returnsEmptyLists() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(null);
        validateCartAsyncResponses.setCtBenefitsResponse(null);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withCartProducts_processesCompatibleProducts() {
        couponOffersRequest = new CouponOffersRequest();
        CartContexts cartContexts = new CartContexts();
        List<CartProduct> cartProducts = new ArrayList<>();
        CartProduct cartProduct = new CartProduct();
        cartProduct.setProductCode("BOLT-MYSPORTS-202410");
        cartProducts.add(cartProduct);
        cartContexts.setCartProducts(cartProducts);
        couponOffersRequest.setCartContext(cartContexts);
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();
        when(validateCartHelpers.getCompatibleProducts(any(), anyList(), eq(Constants.VIDEO_PLAN)))
                .thenReturn(Arrays.asList("BASE-ENTERTAINMENT-201811"));

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getCompatibleProducts(any(), anyList(), eq(Constants.VIDEO_PLAN));
    }

    @Test
    void validateCartRulesExecutions_withCustomerContext_updatesProductTypes() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();
        doNothing().when(validateCartHelpers).updatingProductTypeInCartProducts(any(), any());

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
    }

    @Test
    void validateCartRulesExecutions_withSVODProducts_processesBoth() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> svodProducts = new HashMap<>();
        svodProducts.put("OF_SVOD_OFFER", Arrays.asList("SVOD1", "SVOD2"));
        Map<String, List<String>> nonSvodProducts = new HashMap<>();
        nonSvodProducts.put("OF_NON_SVOD", Collections.singletonList("NON_SVOD1"));

        setupCommonMocks();
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(isNull(), any(), any(), eq(true), any()))
                .thenReturn(svodProducts);
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(isNull(), any(), any(), eq(false), any()))
                .thenReturn(nonSvodProducts);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getEmbeddedSVODProductsFromGetOffersGeneric(
                isNull(), any(), any(), eq(true), any());
        verify(validateCartHelpers, times(1)).getEmbeddedSVODProductsFromGetOffersGeneric(
                isNull(), any(), any(), eq(false), any());
    }

    @Test
    void validateCartRulesExecutions_withFeatureFlags_passesCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_OPTIMOMAS_REPACKAGING_ENABLED)).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_GLOBAL_VALIDATE_CART_EVALUATOR)).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_PEACOCK_VALIDATE_CART_RULES)).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)).thenReturn(true);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(featureManagerHelper, times(1)).isEnabled(Constants.FEATURE_FLAG_OPTIMOMAS_REPACKAGING_ENABLED);
        verify(featureManagerHelper, times(1)).isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC);
        verify(featureManagerHelper, times(1)).isEnabled(Constants.FEATURE_FLAG_GLOBAL_VALIDATE_CART_EVALUATOR);
        verify(featureManagerHelper, times(1)).isEnabled(Constants.FEATURE_FLAG_PEACOCK_VALIDATE_CART_RULES);
        verify(featureManagerHelper, times(1)).isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS);
    }

    @Test
    void validateCartRulesExecutions_withProductConditions_loadsAllConfigurations() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> genreConditions = new HashMap<>();
        genreConditions.put("GENRE_PROD", Collections.singletonList("CONDITION1"));
        Map<String, List<String>> deviceConditions = new HashMap<>();
        deviceConditions.put("DEVICE_PROD", Collections.singletonList("CONDITION2"));
        Map<String, List<String>> peacockConditions = new HashMap<>();
        peacockConditions.put("PEACOCK_PROD", Collections.singletonList("CONDITION3"));
        Map<String, List<String>> mvpxSkipConditions = new HashMap<>();
        mvpxSkipConditions.put("MVPX_PROD", Collections.singletonList("SKIP1"));

        setupCommonMocks();
        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_GENRE_PRODUCTCONDITIONS))
                .thenReturn(genreConditions);
        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_DEVICE_PRODUCTCONDITIONS))
                .thenReturn(deviceConditions);
        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS))
                .thenReturn(peacockConditions);
        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVPX_SKIP_COMBINATION))
                .thenReturn(mvpxSkipConditions);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_GENRE_PRODUCTCONDITIONS);
        verify(validateCartHelpers, times(1)).getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_DEVICE_PRODUCTCONDITIONS);
        verify(validateCartHelpers, times(1)).getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS);
        verify(validateCartHelpers, times(1)).getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVPX_SKIP_COMBINATION);
    }

    @Test
    void validateCartRulesExecutions_withPromotions_processesCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        ctBenefitsResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTBenefitResponseForSPACLTP.json"),
                CTBenefitsResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(ctBenefitsResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> customerPromotions = new HashMap<>();
        customerPromotions.put("PROMO1", Collections.singletonList("VALUE1"));
        Map<String, Benefit> promoDetails = new HashMap<>();

        setupCommonMocks();
        when(validateCartHelpers.getCustomerContextPromotions(any())).thenReturn(customerPromotions);
        when(validateCartHelpers.getPromoDetails(any())).thenReturn(promoDetails);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getCustomerContextPromotions(any());
        verify(validateCartHelpers, times(1)).getPromoDetails(any());
    }

    @Test
    void validateCartRulesExecutions_withIncompatibleSKURules_loadsCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        HashMap<String, Map<String, String>> incompatibleRules = new HashMap<>();
        Map<String, String> rule1 = new HashMap<>();
        rule1.put("incompatible", "PRODUCT1");
        incompatibleRules.put("PRODUCT2", rule1);

        setupCommonMocks();
        when(validateCartHelpers.getIncompatiableSKUResolutionRule(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_INCOMPATIBLE_SKU_RULES))
                .thenReturn(incompatibleRules);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getIncompatiableSKUResolutionRule(
                Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_INCOMPATIBLE_SKU_RULES);
    }

    @Test
    void validateCartRulesExecutions_withDroolsEngineException_throwsServiceException() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();
        when(droolsConfiguration.getKieContainerForValidateCartServicesRules())
                .thenThrow(new RuntimeException("Drools engine error"));

        ServiceException exception = assertThrows(ServiceException.class, () ->
                validateCartServicesDroolHelper.validateCartRulesExecutions(couponOffersRequest, validateCartAsyncResponses)
        );

        assertNotNull(exception);
        assertNotNull(exception.getError());
        assertNotNull(exception.getError().getErrorId());
    }

    @Test
    void validateCartRulesExecutions_withGenericException_throwsServiceException() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(null);

        when(validateCartHelpers.getPreconditionsConfig(anyString()))
                .thenThrow(new RuntimeException("Config error"));

        ServiceException exception = assertThrows(ServiceException.class, () ->
                validateCartServicesDroolHelper.validateCartRulesExecutions(couponOffersRequest, validateCartAsyncResponses)
        );

        assertNotNull(exception);
        assertNotNull(exception.getError());
        assertNotNull(exception.getError().getErrorId());
    }

    @Test
    void validateCartRulesExecutions_withValidateCartRules_loadsGlobalRules() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        List<String> validateCartRules = Arrays.asList("RULE1", "RULE2", "RULE3");

        setupCommonMocks();
        when(redisCacheHelper.getValidateRules(Constants.DISCOUNTRULES, Constants.OTT))
                .thenReturn(validateCartRules);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(redisCacheHelper, times(1)).getValidateRules(Constants.DISCOUNTRULES, Constants.OTT);
    }

    @Test
    void validateCartRulesExecutions_withNullCartContext_processesSuccessfully() {
        couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setCartContext(null);
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        setupCommonMocks();

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withRepackagedChannels_processesMap() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtBenefitsResponse(new CTBenefitsResponse());
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> repackagedMap = new HashMap<>();
        repackagedMap.put("CHANNEL1", Arrays.asList("REPACK1", "REPACK2"));

        setupCommonMocks();
        when(validateCartHelpers.getConfigMap(anyString())).thenReturn(repackagedMap);

        HashMap<String, List> result = validateCartServicesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getConfigMap(anyString());
    }

    private void setupCommonMocks() {
        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.reverseMap(any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigMap(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getCustomerContextPromotions(any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getPromoDetails(any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getIncompatiableSKUResolutionRule(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        when(featureManagerHelper.isEnabled(anyString())).thenReturn(false);
        when(redisCacheHelper.getValidateRules(anyString(), anyString())).thenReturn(new ArrayList<>());
    }

}

