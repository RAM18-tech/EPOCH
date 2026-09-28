package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.DataReader;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.integration.EpochUCCPrimaryClient;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.CouponCartValidationHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ValidateCartCouponServiceImplTest {


    @InjectMocks
    ValidateCartCouponServiceImpl validateCartCouponService;
    @Mock
    CpopClient cpopClient;

    @Mock
    CpopUCCClientHelper cpopUCCClientHelper;

    @Mock
    CouponCartValidationHelper couponCartValidationHelper;

    @InjectMocks
    CouponCartValidationHelper couponCartValidationHelper1;

    @Mock
    EpochUCCPrimaryClient epochUCCPrimaryClient;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void validateCouponReconnect() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);

        Mockito.when(couponCartValidationHelper.isDateWithinEligibilityWindow(any())).thenReturn(true);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }

    @Test
    void validateReqAttribute() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        validateCartCouponService.validateRequestAttributes(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", new ArrayList<>());
    }

    @Test
    void validateReqAttributeFailed() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("TEST"));
        couponOffersRequest.setSalesChannel("TEST");
        couponOffersRequest.setOfferProductFamily("TEST");
        couponOffersRequest.setBusinessSegment("TEST");
        couponOffersRequest.setCustomerSegments("TEST");
        couponOffersRequest.setContractIndicator(Arrays.asList("TEST"));
        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        validateCartCouponService.validateRequestAttributes(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", new ArrayList<>());

        couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("UPSELL"));
        couponOffersRequest.setSalesChannel("directvOnline");
        couponOffersRequest.setOfferProductFamily("OTT");
        couponOffersRequest.setBusinessSegment("CONS");
        couponOffersRequest.setCustomerSegments("TEST");
        couponOffersRequest.setContractIndicator(Arrays.asList("TAZCONTRACT"));

        validateCartCouponService.validateRequestAttributes(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", new ArrayList<>());

    }


    @Test
    void validateCouponAcqRecon() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));
        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);
        epochClientCtRepo.getOffers().get(0).getAttributes().setCustomerTypes(Arrays.asList(Constants.ACQUISITION));

        Mockito.when(couponCartValidationHelper.isDateWithinEligibilityWindow(any())).thenReturn(false);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }

    @Test
    void invalidCartContext() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);

        couponOffersRequest.getCartContext().setCartOffers(new ArrayList<>());
        CartOffer cartOffer = new CartOffer();
        cartOffer.setOfferCode("123");

        couponOffersRequest.getCartContext().getCartOffers().add(cartOffer);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        ctOfferResponse.getOffers().removeIf(a -> a.getCode().equalsIgnoreCase("OF_BASE-CHOICE-201811_TAZBYOD_RECONN"));
        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT201", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }


    @Test
    void couponExpire() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);

        couponOffersRequest.getCartContext().setCartOffers(new ArrayList<>());

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        couponResponseData.getResults().get(0).setValidFrom("2022-07-10T18:30:00.000Z");
        couponResponseData.getResults().get(0).setValidUntil("2022-07-13T18:30:00.000Z");

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT201", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }


    @Test
    void couponInactive() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);

        couponOffersRequest.getCartContext().setCartOffers(new ArrayList<>());

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        couponResponseData.getResults().get(0).setValidFrom("2026-07-10T18:30:00.000Z");
        couponResponseData.getResults().get(0).setValidUntil("2026-07-13T18:30:00.000Z");

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT201", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }


    @Test
    void validateCouponAcq() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));
        couponOffersRequest.setReconnectCustomer(false);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);
        epochClientCtRepo.getOffers().get(0).getAttributes().setCustomerTypes(Arrays.asList(Constants.ACQUISITION));
        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }


    @Test
    void validateCouponQuotaReached() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("Acquisition"));
        couponOffersRequest.setReconnectCustomer(false);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);
        couponResponseData.getResults().get(0).getCustom().getFields().setCouponStatus("quotaReached");
        couponResponseData.getResults().get(0).setMaxApplications(0);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);

        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);

        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);
        epochClientCtRepo.getOffers().get(0).getAttributes().setCustomerTypes(Arrays.asList(Constants.ACQUISITION));
        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);

        assertNotNull(validateCouponList);
    }

    @Test
    void validateCouponUpsell() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("Upsell"));
        couponOffersRequest.setReconnectCustomer(false);
        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);
        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/discountCode.json", JsonNode.class);
        CTOfferResponse ctOfferResponse = new DataReader().readFileToObj("validatecoupon/CtResponseForCartCall.json", CTOfferResponse.class);
        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/ctBenefitsResponse.json", CTBenefitsResponse.class);
        CTOfferResponse epochClientCtRepo = new DataReader().readFileToObj("validatecoupon/epochClientOfferResponse.json", CTOfferResponse.class);
        epochClientCtRepo.getOffers().get(0).getAttributes().setCustomerTypes(Arrays.asList(Constants.UPSELL));
        List<ValidateCoupon> validateCouponList = new ArrayList<>();
        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", validateCouponList, "multi-use2", true, ctOfferResponse, epochClientCtRepo);
        assertNotNull(validateCouponList);
    }


    @Test
    void validateReqAttributeRRFaild() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequest.json", CouponOffersRequest.class);
        couponOffersRequest.setOfferActionType(Arrays.asList("TEST"));
        couponOffersRequest.setSalesChannel("TEST");
        couponOffersRequest.setOfferProductFamily("TEST");
        couponOffersRequest.setBusinessSegment("TEST");
        couponOffersRequest.setCustomerSegments("TEST");
        couponOffersRequest.setContractIndicator(Arrays.asList("RR"));
        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseValidateCoupon.json", CTCouponResponse.class);
        validateCartCouponService.validateRequestAttributes(couponOffersRequest, couponResponseData.getResults().get(0), "RECONNECT20", new ArrayList<>());
        assertNotNull(couponOffersRequest);
    }

    @Test
    void validateReqAttributeSatelliteFamily() {
        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequestSatellite.json", CouponOffersRequest.class);
        Coupon coupon = new Coupon();
        validateCartCouponService.validateRequestAttributes(couponOffersRequest, coupon, "DIRECTV100", new ArrayList<>());
        assertNotNull(couponOffersRequest);
    }

    @Test
    void testValidateCouponAcquisitionSatellite() {

        CouponOffersRequest couponOffersRequest = new DataReader().readFileToObj("validatecoupon/CouponOffersRequestSatellite.json", CouponOffersRequest.class);

        CTCouponResponse couponResponseData = new DataReader().readFileToObj("validatecoupon/CTCouponResponseSatellite.json", CTCouponResponse.class);

        JsonNode couponDetails = new DataReader().readFileToObj("validatecoupon/CTCouponResponseSatellite.json", JsonNode.class);

        CTOfferResponse cartOfferResponse = new DataReader().readFileToObj("validatecoupon/CTCartOfferResponseSatellite.json", CTOfferResponse.class);

        CTBenefitsResponse ctBenefitsResponse = new DataReader().readFileToObj("validatecoupon/CTBenefitsResponseSatellite.json", CTBenefitsResponse.class);

        CTOfferResponse rewardOfferResponse = new DataReader().readFileToObj("validatecoupon/CTRewardOfferResponseSatellite.json", CTOfferResponse.class);

        List<ValidateCoupon> validateCouponList = new ArrayList<>();

        validateCartCouponService.validateCoupon(couponOffersRequest, couponResponseData.getResults().get(0), "DIRECTV100", validateCouponList, "multi-use2", true, cartOfferResponse, rewardOfferResponse);

        assertNotNull(validateCouponList);
    }
}
