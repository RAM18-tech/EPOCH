package com.dtv.dcp.epoch.util;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.service.ott.OttOffersService;

import javax.ws.rs.core.HttpHeaders;

class ValidateCartDroolsHelperTest {

    @InjectMocks
    ValidateCartDroolsHelper validateCartDroolsHelper;
    @Mock
    DroolsConfiguration droolsConfiguration;
    @Mock
    CpopClientHelper cpopClientHelper;
    @Mock
    CpopClient cpopClient;
    @Mock
    RedisCacheHelper redisCacheHelper;
    @Mock
    ValidateCartHelper validateCartHelper;
    @Mock
    CTOfferRequestHelper ctOfferRequestHelper;
    @Mock
    OttOffersService ottOffersService;
    @Mock
    FeatureManagerHelper featureManagerHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(validateCartHelper, "ctstate", "staged");
        ReflectionTestUtils.setField(validateCartHelper, "sportSpackLifetimePromo", "SPACKLTP");
        ReflectionTestUtils.setField(validateCartDroolsHelper, "ctstate", "staged");
        ReflectionTestUtils.setField(validateCartDroolsHelper, "sportSpack1Sku", "BOLT-SPACK-202222");
        ReflectionTestUtils.setField(validateCartDroolsHelper, "sportSpack2Sku", "BOLT-SPACK2-202209");
        ReflectionTestUtils.setField(validateCartDroolsHelper, "sportSpackLifetimePromo", "SPACKLTP");
        ReflectionTestUtils.setField(validateCartDroolsHelper, "localsBoltOnProductSKU", "BOLT-LOCALS-202409");

        // Arrange
        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource("ValidateCart.drl"));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
        when(droolsConfiguration.getKieContainerValidate()).thenReturn(kieContainer);

        KieServices kieServicesVC = KieServices.Factory.get();
        KieFileSystem kieFileSystemVC = kieServicesVC.newKieFileSystem();
        kieFileSystemVC.write(ResourceFactory.newClassPathResource("ValidateCartRules.drl"));
        KieBuilder kieBuilderVC = kieServicesVC.newKieBuilder(kieFileSystemVC);
        kieBuilderVC.buildAll();
        KieContainer kieContainerVC = kieServicesVC.newKieContainer(kieServicesVC.getRepository().getDefaultReleaseId());
        when(droolsConfiguration.getKieContainerForValidateCartRules()).thenReturn(kieContainerVC);

        List<String> compatiblePrdSku1 = new ArrayList<>();
        compatiblePrdSku1.add("BASE-ENTERTAINMENT-201811");
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1, Constants.OTT)).thenReturn(compatiblePrdSku1);
    }

    @Test
    void validateDroolScenariosTest() {

        CouponOffersRequest couponOffersRequest = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestGenre.json"), CouponOffersRequest.class);
        HttpHeaders headers = null;
        CTOfferResponse ctOfferResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTOfferResponseGenre.json"), CTOfferResponse.class);

        List<String> getOffersWithOfferCodesPreselectDesignation = new ArrayList<>();
        getOffersWithOfferCodesPreselectDesignation.add("OF_BOLT-FAST-202410_GENRE");
        getOffersWithOfferCodesPreselectDesignation.add("OF_UNLTDCDVR2021_GENRE");
        getOffersWithOfferCodesPreselectDesignation.add("OF_BASE-GENRE-202410_GENRE");

        List<String> getOffersWithOfferCodesDisplayTypeIncluded = new ArrayList<>();
        getOffersWithOfferCodesDisplayTypeIncluded.add("OF_BOLT-FAST-202410_GENRE");
        getOffersWithOfferCodesDisplayTypeIncluded.add("OF_BOLT-20STREAMS-202010_GENRE");
        getOffersWithOfferCodesDisplayTypeIncluded.add("OF_UNLTDCDVR2021_GENRE");

        CTProductResponse ctProductResponse = JsonService
                .getObjectFromJson(TestUtility.loadJson("/drools/CTProductResponseForGenreTazbyod.json"), CTProductResponse.class);
        // when(cpopClientHelper.getProducts(any())).thenReturn(ctProductResponse);

        ValidateCartAsyncResponses validateCartAsyncResponses = new ValidateCartAsyncResponses();
        validateCartAsyncResponses.setCtProductResponse(ctProductResponse);
        validateCartAsyncResponses.setCtCartCallOfferResponse(ctOfferResponse);

        List<String> genreBaseOfferCodes = new ArrayList<>();
        genreBaseOfferCodes.add("OF_BOLT-MYESPANOL-202410_GENRE");
        genreBaseOfferCodes.add("OF_BOLT-MYSPORTS-202410_GENRE");
        genreBaseOfferCodes.add("OF_BOLT-MYENTERTAINMENT-202410_GENRE");
        genreBaseOfferCodes.add("OF_BOLT-MYNEWS-202410_GENRE");
        when(validateCartHelper.getOfferCodeWithCategoryGenreBase(any())).thenReturn(genreBaseOfferCodes);

        Map<String, List<String>> svodProducts = new HashMap<>();
        svodProducts.put("OF_BOLT-MYENTERTAINMENT-202410_GENRE", Arrays.asList("OF_BOLT-DUOBASIC-202411_GENRE"));
        svodProducts.put("OF_BOLT-MYSPORTS-202410_GENRE", Arrays.asList("OF_BOLT-ESPNPLUS-202411_GENRE"));
        List<String> condition = Arrays.asList("BOLT-MYSPORTSEXTRA-202410:BOLT-MYSPORTS-202410; BOLT-MYHOME-202502:BOLT-MYSPORTS-202410;");
        when(redisCacheHelper.getValues(any(), any())).thenReturn(condition);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VALIDATE_CART_PRODUCT_GRP)).thenReturn(false);
        when(validateCartHelper.groupByProductGroup(ctProductResponse, couponOffersRequest)).thenReturn(svodProducts);


        when(validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES)).thenReturn(new ArrayList<>(Arrays.asList("GENRE", "RR")));
        when(validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES)).thenReturn(new ArrayList<>(Arrays.asList("GENRE")));
        when(validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_DEVICE_RULES)).thenReturn(new ArrayList<>(Arrays.asList("EDSP", "RR")));
        when(redisCacheHelper.getValidateRules(any(), any())).thenReturn(new ArrayList<>(Arrays.asList("BOLT-MYKIDS-202410+BOLT-MYENTERTAINMENT-202410:BOLT-MYENTERTAINMENT-202410|DISNEYDISCOUNT")));


        // Call the method under test
        HashMap<String, List> ruleExecutionResultMap = validateCartDroolsHelper.validateCartRulesExecutions(couponOffersRequest, ctOfferResponse,
                getOffersWithOfferCodesPreselectDesignation,validateCartAsyncResponses, "OF_BASE-GENRE-202410_GENRE", ctOfferResponse);
        // Assert the ruleExecutionResultMap is not null
        assertNotNull(ruleExecutionResultMap);
        assertEquals(3, ruleExecutionResultMap.size());
        assertTrue(ruleExecutionResultMap.containsKey("tempProductCodes"));
    }

    //@Test
    void validateSportPackDroolScenariosTestWithSpackNotActiveUpgradingToNonPremierBridge() {
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
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestWithSpackNotActiveUpgradingToNonPremierBridge.json"), CouponOffersRequest.class);
        // Call the method under test
        List<CartItems> cartItemsList = validateCartDroolsHelper.validateSportPackDroolScenarios(couponOffersRequest);
        // Assert that the cartItemsList is not null
        assertNotNull(cartItemsList, "Expected list of CartItems");
        assertEquals(2, cartItemsList.size());
        assertEquals("BRIDGE-CHOTOULT-202204", cartItemsList.get(0).getProductCode());
        assertEquals("Add", cartItemsList.get(0).getAction());
        assertNull(cartItemsList.get(0).getPromotions());
        assertEquals("BASE-CHOICE-201811", cartItemsList.get(1).getProductCode());
        assertEquals("Remove", cartItemsList.get(1).getAction());
        assertNull(cartItemsList.get(1).getPromotions());
    }

    //@Test
    void validateSportPackDroolScenariosTestWithActiveSpackWithoutPromoAndUpgradingToPremierBridge() {
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
        // Call the method under test
        List<CartItems> cartItemsList = validateCartDroolsHelper.validateSportPackDroolScenarios(couponOffersRequest);
        // Assert that the cartItemsList is not null
        assertNotNull(cartItemsList, "Expected list of CartItems");
        assertEquals(5, cartItemsList.size());
        assertEquals("BRIDGE-ENTTOPREM-202204", cartItemsList.get(0).getProductCode());
        assertEquals("Add", cartItemsList.get(0).getAction());
        assertNull(cartItemsList.get(0).getPromotions());
        assertEquals("BASE-ENTERTAINMENT-201811", cartItemsList.get(1).getProductCode());
        assertEquals("Remove", cartItemsList.get(1).getAction());
        assertNull(cartItemsList.get(1).getPromotions());
        assertEquals("BOLT-SPACK2-202209", cartItemsList.get(2).getProductCode());
        assertEquals("Add", cartItemsList.get(2).getAction());
        assertNotNull(cartItemsList.get(2).getPromotions());
        assertEquals("SPACKLTP", cartItemsList.get(2).getPromotions().get(0).getPromotionId());
        assertEquals("Add", cartItemsList.get(2).getPromotions().get(0).getAction());
        assertEquals("BOLT-SPACK-202222", cartItemsList.get(3).getProductCode());
        assertEquals("Remove", cartItemsList.get(3).getAction());
        assertNull(cartItemsList.get(3).getPromotions());
    }

    //@Test
    void validateSportPackDroolScenariosTestWithSpack1NotActiveUpgradingToPremierBridge() {
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
                .getObjectFromJson(TestUtility.loadJson("/drools/CouponOfferRequestWithSpackNotActiveUpgradingToPremierBridge.json"), CouponOffersRequest.class);
        // Call the method under test
        List<CartItems> cartItemsList = validateCartDroolsHelper.validateSportPackDroolScenarios(couponOffersRequest);
        // Assert that the cartItemsList is not null
        assertNotNull(cartItemsList, "Expected list of CartItems");
        assertEquals(4, cartItemsList.size());
        assertEquals("BOLT-SPACK2-202209", cartItemsList.get(0).getProductCode());
        assertEquals("Add", cartItemsList.get(0).getAction());
        assertNotNull(cartItemsList.get(0).getPromotions());
        assertEquals("SPACKLTP", cartItemsList.get(0).getPromotions().get(0).getPromotionId());
        assertEquals("BRIDGE-ENTTOPREM-202204", cartItemsList.get(1).getProductCode());
        assertEquals("Add", cartItemsList.get(1).getAction());
        assertNull(cartItemsList.get(1).getPromotions());
        assertEquals("BASE-ENTERTAINMENT-201811", cartItemsList.get(2).getProductCode());
        assertEquals("Remove", cartItemsList.get(2).getAction());
        assertNull(cartItemsList.get(2).getPromotions());
    }

}