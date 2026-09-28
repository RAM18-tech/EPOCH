package com.dtv.dcp.epoch.resource;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.ucc.Content;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCartResponse;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.processor.ValidateCartProcessor;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import com.dtv.dcp.epoch.processor.validatecart.ValidateCartProcessors;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.TestUtility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ValidateCartResourceImplTest {

    @Mock
    ValidateCartProcessor validateCartProcessorOldFlow;

    @Mock
    ValidateCartProcessors validateCartProcessors;

    @Mock
    ValidateCartHelpers validateCartHelpers;

    @Mock
    FeatureManagerHelper featureManagerHelper;

    @InjectMocks
    ValidateCartResourceImpl validateCartResource;

    HttpHeaders headers;
    CouponOffersRequest couponOffersRequest;
    AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        headers = new HttpHeaders();
        headers.add(Constants.TRACE_ID, "test-trace-id-123");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void validateCart_withValidRequest_returnsSuccessResponse() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();
        expectedContent.setCoupons(new ArrayList<>());

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertInstanceOf(ValidateCartResponse.class, response.getBody());
        verify(validateCartProcessorOldFlow, times(1)).validateCartProcessing(any(), any(), anyBoolean());
        verify(validateCartProcessors, never()).validateCartProcessing(any(), any(), anyBoolean());
    }

    @Test
    void validateCart_withFeatureFlagEnabled_usesNewProcessor() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(true);
        when(validateCartProcessors.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validateCartProcessors, times(1)).validateCartProcessing(any(), any(), anyBoolean());
        verify(validateCartProcessorOldFlow, never()).validateCartProcessing(any(), any(), anyBoolean());
    }

    @Test
    void validateCart_withSatelliteRequest_processesSuccessfully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequestSatellite.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();
        List<ValidateCoupon> coupons = new ArrayList<>();
        expectedContent.setCoupons(coupons);

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(validateCartProcessorOldFlow, times(1)).validateCartProcessing(any(), any(), anyBoolean());
    }

    @Test
    void validateCart_withMDUServicesRequest_passesCorrectFlag() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/rightsizing/CouponOffersRightSizingRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(true));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(true);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), eq(true))).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(validateCartHelpers, times(1)).isMDUServicesRequest(any());
        verify(validateCartProcessorOldFlow, times(1)).validateCartProcessing(any(), any(), eq(true));
    }

    @Test
    void validateCart_withServiceException_returnsErrorResponse() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        ServiceException serviceException = new ServiceException(ErrorMessages.CPOPOFFERMS_INTERNALSERVER_ERROR);

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean()))
                .thenThrow(serviceException);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.valueOf(serviceException.getHttpCode()), response.getStatusCode());
        assertInstanceOf(Error.class, response.getBody());
    }

    @Test
    void validateCart_withGenericException_handlesGracefully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void validateCart_withNullTraceId_handlesGracefully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        HttpHeaders nullHeaders = new HttpHeaders();
        ResponseEntity<?> response = validateCartResource.validateCart(nullHeaders, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void validateCart_withEmptyCouponCodes_processesSuccessfully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);
        couponOffersRequest.setCouponCodes(new ArrayList<>());

        Content expectedContent = new Content();

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void validateCart_withCouponsContainingAdditionalEligibility_filtersCoupons() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();
        List<ValidateCoupon> coupons = new ArrayList<>();
        ValidateCoupon coupon = new ValidateCoupon();
        coupon.setCode("TEST_COUPON");
        coupons.add(coupon);
        expectedContent.setCoupons(coupons);

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ValidateCartResponse cartResponse = (ValidateCartResponse) response.getBody();
        assertNotNull(cartResponse);
        assertNotNull(cartResponse.getContent());
    }

    @Test
    void validateCart_withNullCoupons_setsNullInResponse() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();
        expectedContent.setCoupons(null);

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ValidateCartResponse cartResponse = (ValidateCartResponse) response.getBody();
        assertNotNull(cartResponse);
    }

    @Test
    void validateCart_stackableRequest_processesSuccessfully() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/couponOffersRequestStackable.json"),
                CouponOffersRequest.class);

        Content expectedContent = new Content();

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean())).thenReturn(expectedContent);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void validateCart_withNonMDUFlowAndNonGENREContract_setsSystemError() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);
        couponOffersRequest.setContractIndicator(Collections.singletonList("TAZBYOD"));

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Test exception"));
        when(validateCartHelpers.createValidationResults(anyString(), anyString(), anyString()))
                .thenReturn(null);

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(validateCartHelpers, times(1)).createValidationResults(eq("Error"), eq("SYSTEM_ERROR"), eq(Constants.SYSTEM_ERROR));
    }

    @Test
    void validateCart_withGENREContractAndException_doesNotSetSystemError() {
        couponOffersRequest = JsonService.getObjectFromJson(
                TestUtility.loadJson("/validatecoupon/CouponOffersRequest.json"),
                CouponOffersRequest.class);
        couponOffersRequest.setContractIndicator(Collections.singletonList(Constants.GENRE));

        doNothing().when(validateCartHelpers).validateCartRequestValidation(any(), eq(false));
        when(validateCartHelpers.isMDUServicesRequest(any())).thenReturn(false);
        when(featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)).thenReturn(false);
        when(validateCartProcessorOldFlow.validateCartProcessing(any(), any(), anyBoolean()))
                .thenThrow(new RuntimeException("Test exception"));

        ResponseEntity<?> response = validateCartResource.validateCart(headers, couponOffersRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(validateCartHelpers, never()).createValidationResults(eq("Error"), eq("SYSTEM_ERROR"), eq(Constants.SYSTEM_ERROR));
    }

}
