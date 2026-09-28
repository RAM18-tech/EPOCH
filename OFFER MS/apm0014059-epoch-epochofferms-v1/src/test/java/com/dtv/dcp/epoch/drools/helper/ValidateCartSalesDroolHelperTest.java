package com.dtv.dcp.epoch.drools.helper;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ValidateCartSalesDroolHelperTest {

    @Mock
    ValidateCartHelpers validateCartHelpers;

    @Mock
    DroolsConfiguration droolsConfiguration;

    @InjectMocks
    ValidateCartSalesDroolHelper validateCartSalesDroolHelper;

    AutoCloseable closeable;
    CouponOffersRequest couponOffersRequest;
    ValidateCartAsyncResponses validateCartAsyncResponses;
    CTOfferResponse ctOfferResponse;
    CTProductResponse ctProductResponse;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        setupDroolsEngine();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    private void setupDroolsEngine() {
        KieContainer kieContainer = mock(KieContainer.class);
        KieSession kieSession = mock(KieSession.class);
        when(kieContainer.newKieSession()).thenReturn(kieSession);
        doNothing().when(kieSession).setGlobal(anyString(), any());
        when(kieSession.insert(any())).thenReturn(null);
        when(kieSession.fireAllRules()).thenReturn(0);
        doNothing().when(kieSession).dispose();
        when(droolsConfiguration.getKieContainerForValidateCartSalesRules()).thenReturn(kieContainer);
    }

    @Test
    void validateCartRulesExecutions_withValidRequest_returnsSuccessfulResponse() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        List<String> genreBaseOfferCodes = Arrays.asList("OF_BOLT-MYESPANOL-202410_GENRE", "OF_BOLT-MYSPORTS-202410_GENRE");
        Map<String, List<String>> svodProducts = new HashMap<>();
        svodProducts.put("OF_BOLT-MYENTERTAINMENT-202410_GENRE", Collections.singletonList("OF_BOLT-DUOBASIC-202411_GENRE"));
        Map<String, List<String>> peacockProductsCondition = new HashMap<>();
        List<String> commonRuleCohortList = Arrays.asList("GENRE", "RR");
        List<String> genreSpecificRuleCohortList = Collections.singletonList("GENRE");

        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS))
                .thenReturn(peacockProductsCondition);
        when(validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES))
                .thenReturn(commonRuleCohortList);
        when(validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES))
                .thenReturn(genreSpecificRuleCohortList);
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(genreBaseOfferCodes);
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), eq(true), any()))
                .thenReturn(svodProducts);
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), eq(false), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", ctOfferResponse);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("cartItemsList"));
        assertTrue(result.containsKey("messageDetailsList"));
        assertNotNull(result.get("cartItemsList"));
        assertNotNull(result.get("messageDetailsList"));
    }

    @Test
    void validateCartRulesExecutions_withNullCartCallResponse_returnsEmptyResult() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(null);
        validateCartAsyncResponses.setCtProductResponse(null);

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void validateCartRulesExecutions_withNullOffers_returnsEmptyResult() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = new CTOfferResponse();
        ctOfferResponse.setOffers(null);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void validateCartRulesExecutions_withEmptyCartOffers_processesSuccessfully() {
        couponOffersRequest = new CouponOffersRequest();
        CartContexts cartContexts = new CartContexts();
        cartContexts.setCartOffers(new ArrayList<>());
        couponOffersRequest.setCartContext(cartContexts);
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));

        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withConflictingProducts_processesCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> conflictingProducts = new HashMap<>();
        conflictingProducts.put("OF_CONFLICTING_OFFER", Collections.singletonList("PRODUCT_TO_REMOVE"));

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(conflictingProducts);
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).conflictingOfferOrProductToRemoveBasedOnConflictingProducts(
                any(), any(), any(), any());
    }

    @Test
    void validateCartRulesExecutions_withPeacockProductConditions_appliesCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> peacockProductsCondition = new HashMap<>();
        peacockProductsCondition.put("PEACOCK_PRODUCT", Arrays.asList("CONDITION1", "CONDITION2"));
        Map<String, List<String>> peacockOffersCombinations = new HashMap<>();
        peacockOffersCombinations.put("OF_PEACOCK_OFFER", Arrays.asList("PRODUCT1", "PRODUCT2"));

        when(validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS))
                .thenReturn(peacockProductsCondition);
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any()))
                .thenReturn(peacockOffersCombinations);
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).groupByProductCodeOffersForPeacock(any(), any(), any());
    }

    @Test
    void validateCartRulesExecutions_withProductGrouping_groupsCorrectly() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        Map<String, String> productCodeToIdMap = new HashMap<>();
        productCodeToIdMap.put("PRODUCT1", "ID1");
        validateCartAsyncResponses.setProductCodeToProductIdMap(productCodeToIdMap);

        Map<String, List<String>> groupingByProductGrp = new HashMap<>();
        groupingByProductGrp.put("GROUP1", Arrays.asList("PRODUCT1", "PRODUCT2"));

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any()))
                .thenReturn(groupingByProductGrp);
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).groupByProductGroup(any(), any(), any(), eq(productCodeToIdMap));
    }

    @Test
    void validateCartRulesExecutions_withDroolsException_throwsServiceException() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        
        when(droolsConfiguration.getKieContainerForValidateCartSalesRules()).thenThrow(new RuntimeException("Drools error"));

        ServiceException exception = assertThrows(ServiceException.class, () ->
                validateCartSalesDroolHelper.validateCartRulesExecutions(
                        couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null)
        );

        assertNotNull(exception);
        assertNotNull(exception.getError());
        assertNotNull(exception.getError().getErrorId());
    }

    @Test
    void validateCartRulesExecutions_withNullCartContext_handlesGracefully() {
        couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setCartContext(null);
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));

        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withMultipleContractIndicators_processesAll() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        couponOffersRequest.setContractIndicator(Arrays.asList("GENRE", "TAZBYOD", "EDSP"));

        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withSVODAndNonSVODProducts_processesBoth() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);
        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        Map<String, List<String>> svodProducts = new HashMap<>();
        svodProducts.put("OF_SVOD_OFFER", Arrays.asList("SVOD_PRODUCT1", "SVOD_PRODUCT2"));
        Map<String, List<String>> nonSvodProducts = new HashMap<>();
        nonSvodProducts.put("OF_NON_SVOD_OFFER", Collections.singletonList("NON_SVOD_PRODUCT1"));

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), eq(true), any()))
                .thenReturn(svodProducts);
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), eq(false), any()))
                .thenReturn(nonSvodProducts);
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        verify(validateCartHelpers, times(1)).getEmbeddedSVODProductsFromGetOffersGeneric(
                any(), any(), any(), eq(true), any());
        verify(validateCartHelpers, times(1)).getEmbeddedSVODProductsFromGetOffersGeneric(
                any(), any(), any(), eq(false), any());
    }

    @Test
    void validateCartRulesExecutions_withRequestCartOfferActionMap_preservesActions() {
        couponOffersRequest = new CouponOffersRequest();
        CartContexts cartContexts = new CartContexts();
        List<CartOffer> cartOffers = new ArrayList<>();
        CartOffer offer1 = new CartOffer();
        offer1.setOfferCode("OFFER1");
        offer1.setAction("ADD");
        CartOffer offer2 = new CartOffer();
        offer2.setOfferCode("OFFER2");
        offer2.setAction("REMOVE");
        cartOffers.add(offer1);
        cartOffers.add(offer2);
        cartContexts.setCartOffers(cartOffers);
        couponOffersRequest.setCartContext(cartContexts);
        couponOffersRequest.setContractIndicator(Collections.singletonList("GENRE"));

        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);
        ctProductResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"),
                CTProductResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setProductCodeToProductIdMap(new HashMap<>());

        when(validateCartHelpers.getPreconditionsConfig(anyString())).thenReturn(new HashMap<>());
        when(validateCartHelpers.getConfigList(anyString())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getOfferCodeWithCategoryGenreBase(anyList())).thenReturn(new ArrayList<>());
        when(validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(any(), any(), any(), anyBoolean(), any()))
                .thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductGroup(any(), any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.groupByProductCodeOffersForPeacock(any(), any(), any())).thenReturn(new HashMap<>());
        when(validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(any(), any(), any(), any()))
                .thenReturn(new HashMap<>());
        

        HashMap<String, List> result = validateCartSalesDroolHelper.validateCartRulesExecutions(
                couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void validateCartRulesExecutions_withGenericException_throwsServiceException() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"),
                CouponOffersRequest.class);

        ctOfferResponse = JsonService.getObjectFromJson(
                TestUtility.loadJson("/drools/CTOfferResponseGenre.json"),
                CTOfferResponse.class);

        validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);
        validateCartAsyncResponses.setCtProductResponse(null);

        when(validateCartHelpers.getPreconditionsConfig(anyString()))
                .thenThrow(new RuntimeException("Config error"));

        ServiceException exception = assertThrows(ServiceException.class, () ->
                validateCartSalesDroolHelper.validateCartRulesExecutions(
                        couponOffersRequest, validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", null)
        );

        assertNotNull(exception);
        assertNotNull(exception.getError());
        assertNotNull(exception.getError().getErrorId());
    }

}

