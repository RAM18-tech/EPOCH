package com.dtv.dcp.epoch.resource;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.ucc.Content;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCartResponse;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.processor.ValidateCartProcessor;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import com.dtv.dcp.epoch.processor.validatecart.ValidateCartProcessors;
import com.dtv.dcp.epoch.representation.Error;
import com.dtv.dcp.epoch.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Controller
public class ValidateCartResourceImpl implements ValidateCartResource {

    private static final Logger log = LoggerFactory.getLogger(ValidateCartResourceImpl.class);
    private static final String ENDPOINT = "API_NAME:EPOCH_VALIDATECART";
    @Autowired
    ValidateCartProcessor validateCartProcessorOldFlow;
    @Autowired
    ValidateCartProcessors validateCartProcessors;
    @Autowired
    ValidateCartHelpers validateCartHelpers;
    @Autowired
    FeatureManagerHelper featureManagerHelper;

    @Override
    public ResponseEntity validateCart(HttpHeaders headers, CouponOffersRequest couponOffersRequest) {
        MDC.put(Constants.TRACE_ID, null != headers ? headers.getFirst(Constants.TRACE_ID) : "");
        long startTimeInMillies = System.currentTimeMillis();
        log.info("{} CLIENT_REQUEST:[{}] TRACE_ID:[{}]", ENDPOINT, JsonService.getJsonFromObject(couponOffersRequest),
                headers.getFirst(Constants.TRACE_ID));

        ValidateCartResponse response = new ValidateCartResponse();
        Content content = new Content();
        List<ValidateCoupon> coupons = new ArrayList<>();
        try {
            log.debug("ValidateCartResourceImpl - start");
            FeatureManagerHelper.httpHeaders = headers;
            boolean isMDUServicesRequest = validateCartHelpers.isMDUServicesRequest(couponOffersRequest);
            //validate cart request and throw error if not valid
            validateCartHelpers.validateCartRequestValidation(couponOffersRequest, isMDUServicesRequest);

            if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SEPARATE_FLOW)) {
                content = validateCartProcessors.validateCartProcessing(couponOffersRequest, headers, isMDUServicesRequest);
            } else {
                content = validateCartProcessorOldFlow.validateCartProcessing(couponOffersRequest, headers, isMDUServicesRequest);
            }

            if (CollectionUtils.isEmpty(couponOffersRequest.getCouponCodes())) {
                if (Objects.isNull(content.getValidationResults()) && Objects.nonNull(couponOffersRequest.getCartContext()) && CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartProducts())
                        && !validateCartHelpers.checkForMDUFlow(couponOffersRequest)
                        && !CollectionUtils.containsAny(couponOffersRequest.getOfferActionType(), Arrays.asList(Constants.ACQUISITION_ACTION_TYPE, Constants.CLOSING_ACTION_TYPE))
                        && Objects.isNull(content.getCartItems()) && Objects.isNull(content.getMessages())) {
                    content.setValidationResults(validateCartHelpers.createValidationResults("Error", "SYSTEM_ERROR", "Incorrect Request: Right sizing is currently for MDU customers only"));
                    log.error("Incorrect Request: Right sizing is currently for MDU customers only");
                }
            }
        } catch (ServiceException serviceException) {
            log.error("Exception occurred while processing validation of cart: ", serviceException);
            Error error = new Error(serviceException.getError());
            return new ResponseEntity<>(error, HttpStatus.valueOf(serviceException.getHttpCode()));
            //throw serviceEx;
        } catch (Exception e) {
            log.error("Exception occurred while processing validation of cart: ", e);
            if (Objects.nonNull(couponOffersRequest.getContractIndicator())
                    && CollectionUtils.isNotEmpty(couponOffersRequest.getContractIndicator())
                    && !couponOffersRequest.getContractIndicator().contains(Constants.GENRE)
            ) {
                content.setValidationResults(validateCartHelpers.createValidationResults("Error", "SYSTEM_ERROR", Constants.SYSTEM_ERROR));
            }
        }
        coupons = JsonFilterService.filterAttributesAndGetObjectFromJson(content.getCoupons(), List.class, Stream.of("additionalEligibility").toArray(String[]::new));
        content.setCoupons(Objects.nonNull(coupons) && coupons.size() > 0 ? coupons : null);
        response.setContent(content);
        log.info("{} EPOCH_VALIDATECART_EXECUTION_TIME-[{}] FOR COUPON_CODE-[{}] ", ENDPOINT,
                (System.currentTimeMillis() - startTimeInMillies), OffersUtils.sanitizeData(couponOffersRequest.getCouponCodes()));
        log.info("{} RESPONSE:[{}] TRACE_ID:[{}]", ENDPOINT, JsonService.getJsonFromObject(response),
                headers.getFirst(Constants.TRACE_ID));
        log.debug("End of ValidateCartResourceImpl");
        return ResponseEntity.ok(response);
    }

}
