package com.dtv.dcp.epoch.util;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.model.common.request.AgentChannelDetails;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerAddress;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.ct.eligibility.Constraint;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.offer.OnlinePartnerDetails;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;

class CouponCartValidationHelperTest {

    @InjectMocks
    CouponCartValidationHelper couponCartValidationHelper;

    @Mock
    DroolsConfiguration droolsConfiguration;

    @Mock
    KieSession kieSession;

    @Mock
    CpopClientHelper cpopClientHelper;

    @Mock
    CpopClient cpopClient;

    @Mock
    FeatureManagerHelper featureManagerHelper;
    @Mock
    RedisCacheHelper redisCacheHelper;
    @Mock
    DMALookUpService dmaLookUpService;
    @Mock
    ValidateCartHelper validateCartHelper;
    @Mock
    ValidateCartDroolsHelper validateCartDroolsHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(couponCartValidationHelper, "ctstate", "staged");
        ReflectionTestUtils.setField(couponCartValidationHelper, "reconnectEligibilityWindow", 6);
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

        List<String> compatiblePrdSku1 = new ArrayList<>();
        compatiblePrdSku1.add("BASE-ENTERTAINMENT-201811");
        Mockito.when(redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1, Constants.OTT)).thenReturn(compatiblePrdSku1);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_REWARD_CARD_GEO_LOCATION_ENABLED)).thenReturn(true);
        Mockito.when(featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_LOCALS_VALIDATE_CART_ENABLED_FLAG)).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)).thenReturn(false);
    }

    @Test
    void testProcessValidateCouponResponseSatellite() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequestSatellite.json", CouponOffersRequest.class);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseSatellite.json", CTCouponResponse.class);

        CTOfferResponse rewardOfferResponse = new DataReader().readFileToObj("validatecoupon/CTRewardOfferResponseSatellite.json", CTOfferResponse.class);

        ValidateCoupon validateCoupon = couponCartValidationHelper.processValidateCouponResponse(rewardOfferResponse, couponOffersRequest, couponResponseData.getResults().get(0));

        assertNotNull(validateCoupon);
        assertNull(validateCoupon.getOffers().get(0).getAssociatedProducts().getQualifyingProducts().get(0).getPrice());
        assertNotNull(validateCoupon.getOffers().get(0).getAssociatedProducts().getBundleProducts().get(0).getPrice());

    }

    @Test
    void testValidateRequestAttributeInValid() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setOfferActionType(Collections.singletonList("INVALID_ACTION_TYPE"));
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setOfferActionType(Constants.ACQUISITION_ACTION_TYPE);
        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        assertFalse(result);
    }

    @Test
    void testValidateRequestAttribute_ValidSalesChannel() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setSalesChannel(Constants.DIRECTV_ONLINE);
        OfferAttributes offerAttributes = new OfferAttributes();
        Eligibility eligibility = new Eligibility();
        Constraint constraint = new Constraint();
        constraint.setSalesChannel(Collections.singletonList(Constants.DIRECTV_ONLINE));
        eligibility.setConstraints(Collections.singletonList(constraint));
        offerAttributes.setEligibility(eligibility);
        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        assertTrue(result);
    }

    @Test
    void testValidateRequestAttribute1() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setOfferActionType(Collections.singletonList(Constants.INCLUDED));
        couponOffersRequest.setSalesChannel("DIRECTVONLINE_TEST");
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setOfferActionType(Constants.ACQUISITION_ACTION_TYPE);
        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        assertFalse(result);
        offerAttributes.setOfferActionType(null);
        Eligibility eligibility = new Eligibility();
        Constraint constraint = new Constraint();
        constraint.setSalesChannel(Collections.singletonList(Constants.DIRECTV_ONLINE));
        eligibility.setConstraints(Collections.singletonList(constraint));
        offerAttributes.setEligibility(eligibility);
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        assertFalse(result);
        couponOffersRequest.setSalesChannel(Constants.DIRECTV_ONLINE);
        offerAttributes.setIneligiblePartners(new ArrayList<>());
        offerAttributes.getIneligiblePartners().add("ALL");
        offerAttributes.setOnlinePartnerDetails(new ArrayList<>());
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        offerAttributes.setIneligiblePartners(new ArrayList<>());
        offerAttributes.getIneligiblePartners().add("DIRECTVONLINE_TEST");
        OnlinePartnerDetails onlinePartnerDetails = new OnlinePartnerDetails();
        onlinePartnerDetails.setPartnerName("DIRECTVONLINE_TEST");

        com.dtv.dcp.epoch.model.common.request.OnlinePartnerDetails onlinePartnerDetails1 = new com.dtv.dcp.epoch.model.common.request.OnlinePartnerDetails();
        onlinePartnerDetails1.setPartnerName("DIRECTVONLINE_TEST");
        couponOffersRequest.setOnlinePartnerDetails(onlinePartnerDetails1);
        offerAttributes.getOnlinePartnerDetails().add(onlinePartnerDetails);
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        couponOffersRequest.setSalesChannel("DIRECTVONLINE_TEST");
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
    }

    @Test
    void testValidateRequestAttribute2() {
        OfferAttributes offerAttributes = new OfferAttributes();
        OnlinePartnerDetails onlinePartnerDetails = new OnlinePartnerDetails();
        onlinePartnerDetails.setPartnerName("DIRECTVONLINE_TEST");
        onlinePartnerDetails.setDealerCode1(Collections.singletonList("TEST1"));
        offerAttributes.setOnlinePartnerDetails(Arrays.asList(onlinePartnerDetails));
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setSalesChannel(Constants.DIRECTV_ONLINE);
        com.dtv.dcp.epoch.model.common.request.OnlinePartnerDetails onlinePartnerDetails1 = new com.dtv.dcp.epoch.model.common.request.OnlinePartnerDetails();
        onlinePartnerDetails1.setPartnerName("DIRECTVONLINE_TEST");
        onlinePartnerDetails1.setDealerCode1("TEST");
        couponOffersRequest.setOnlinePartnerDetails(onlinePartnerDetails1);
        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        onlinePartnerDetails.setPartnerName("DIRECTVONLINE_TEST");
        onlinePartnerDetails.setDealerCode1(null);
        offerAttributes.setOnlinePartnerDetails(Arrays.asList(onlinePartnerDetails));
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        onlinePartnerDetails1.setPartnerName("DIRECTVONLINE_INVALID");
        couponOffersRequest.setOnlinePartnerDetails(onlinePartnerDetails1);
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);

    }

    @Test
    void testValidationAttribute3() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setSalesChannel(Constants.OPUS);
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setOpusDealerID1(Collections.singletonList("TEST"));

        couponOffersRequest.setAgentDealerDetails(new com.dtv.dcp.epoch.model.common.request.AgentDealerDetails());
        couponOffersRequest.getAgentDealerDetails().setDealerId1("TEST1");

        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);

        couponOffersRequest.getAgentDealerDetails().setDealerId1(null);
        couponOffersRequest.getAgentDealerDetails().setDealerId2("TEST2");
        offerAttributes.setOpusDealerID1(null);
        offerAttributes.setOpusDealerID2(Collections.singletonList("TEST2"));
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);

    }

    @Test
    void testValidationAttribute4() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setSalesChannel(Constants.OPUS);
        OfferAttributes offerAttributes = new OfferAttributes();
        offerAttributes.setOpusChannels(Collections.singletonList("TEST"));

        offerAttributes.setOpusStoreIds(Collections.singletonList("TEST"));

        couponOffersRequest.setAgentChannelDetails(new AgentChannelDetails());
        couponOffersRequest.getAgentChannelDetails().setChannel("TEST1");
        boolean result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        offerAttributes.setOpusSubChannels(Collections.singletonList("TEST"));
        couponOffersRequest.getAgentChannelDetails().setChannel(null);
        couponOffersRequest.getAgentChannelDetails().setSubChannel("TEST1");
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);
        offerAttributes.setOpusSubChannels(Collections.singletonList("TEST1323"));
        offerAttributes.setOpusStoreIds(Collections.singletonList("TEST"));
        couponOffersRequest.getAgentChannelDetails().setStoreId("TEST1");
        couponOffersRequest.getAgentChannelDetails().setSubChannel(null);
        result = couponCartValidationHelper.validateRequestAttribute(couponOffersRequest, offerAttributes);

    }

    @Test
    void validateCartRequestValidationTest() {
        CouponOffersRequest couponOffersRequest = new CouponOffersRequest();
        couponOffersRequest.setOfferActionType(new ArrayList<>());
        couponOffersRequest.getOfferActionType().add(null);
        couponOffersRequest.setContractIndicator(new ArrayList<>());
        couponOffersRequest.getContractIndicator().add(null);
        ServiceException ex = catchThrowableOfType(() -> validateCartHelper.validateCartRequestValidation(couponOffersRequest, false, false, false),
                ServiceException.class);

    }

    @Test
    void testProcessValidateCouponResponse() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));
        couponOffersRequest.setReconnectCustomer(false);
        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);
        epochClientCtRepo.getOffers().get(0).getAttributes().setCustomerTypes(Arrays.asList("Acquisition"));

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);
        CpopUCCClientHelper cpopUCCClientHelper = new CpopUCCClientHelper();
        ReflectionTestUtils.setField(cpopUCCClientHelper, "featureManagerHelper", featureManagerHelper);
        cpopUCCClientHelper.setInlineProducts(epochClientCtRepo);
        couponCartValidationHelper.processValidateCouponResponse(epochClientCtRepo, couponOffersRequest, couponResponseData.getResults().get(0));

    }

    @Test
    void teststackableRuleCheckAndSetActions() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        ValidateCoupon validateCoupon = new DataReader().readFileToObj("validatecoupon/validateCoupon1.json", ValidateCoupon.class);
        List<ValidateCoupon> validateCouponList = new ArrayList<>();
        validateCouponList.add(validateCoupon);
        couponCartValidationHelper.stackableRuleCheckAndSetActions(couponOffersRequest, validateCouponList);

        ValidateCoupon validateCoupon2 = new DataReader().readFileToObj("validatecoupon/validateCoupon2.json", ValidateCoupon.class);
        List<ValidateCoupon> validateCouponList2 = new ArrayList<>();
        validateCouponList2.add(validateCoupon2);
        couponCartValidationHelper.stackableRuleCheckAndSetActions(couponOffersRequest, validateCouponList2);
        couponCartValidationHelper.isValidFormat(couponOffersRequest);

    }

    @Test
    void filterOfferBasedOnZipOrDMATest() {
        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochOfferResponseDMA.json", CTOfferResponse.class);
        when(dmaLookUpService.getDMAValue(any(), any())).thenReturn(Collections.singletonList("512"));
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setZipCode("75001");
        customerAddress.setFipsCode("12345");
        couponCartValidationHelper.filterOfferBasedOnZipOrDMA(epochClientCtRepo, customerAddress);
        couponCartValidationHelper.getCustomerEligibilityRequest(customerAddress);
        couponCartValidationHelper.filterOfferOnEmptyCustomerAddress(epochClientCtRepo);

    }

    @Test
    void createCTOfferRequest() {
        couponCartValidationHelper.createCTOfferRequest(Arrays.asList("123", "456"), Arrays.asList("123", "456"));
    }

    @Test
    void isDateWithinEligibilityWindowTest() {
        when(redisCacheHelper.getValues(any(), any())).thenReturn(Collections.singletonList("2"));
        couponCartValidationHelper.isDateWithinEligibilityWindow("05/01/2024");
    }

    @Test
    void additionalEligibilityCheckTest(){
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochOfferResponseTestDevice.json", CTOfferResponse.class);
        couponCartValidationHelper.additionalEligibilityCheck(couponOffersRequest, epochClientCtRepo.getOffers().get(0));
    }
}