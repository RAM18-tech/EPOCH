package com.dtv.dcp.epoch.processor.validatecart;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.dtv.dcp.epoch.drools.helper.ValidateCartSalesDroolHelper;
import com.dtv.dcp.epoch.drools.helper.ValidateCartServicesDroolHelper;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.EpochUCCPrimaryClient;
import com.dtv.dcp.epoch.model.common.CartProduct;
import com.dtv.dcp.epoch.model.common.CartPromotion;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerPromotion;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.request.ProductRequest;
import com.dtv.dcp.epoch.model.common.response.CampaignProducts;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.common.response.ucc.Content;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidationResults;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTCouponResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.ct.response.ParentOffers;
import com.dtv.dcp.epoch.model.message.MessageDetail;
import com.dtv.dcp.epoch.service.ValidateCartCouponService;
import com.dtv.dcp.epoch.util.CouponCartValidationHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.dtv.dcp.epoch.util.RxJavaHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ValidateCartProcessors {

    private static final Logger log = LoggerFactory.getLogger(ValidateCartProcessors.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static boolean isCouponsFlowExecuted = false;
    @Autowired
    EpochUCCPrimaryClient epochUCCPrimaryClient;
    @Autowired
    FeatureManagerHelper featureManagerHelper;
    @Autowired
    ValidateCartHelpers validateCartHelpers;
    @Autowired
    CouponCartValidationHelper couponCartValidationHelper;
    @Autowired
    ValidateCartCouponService validateCartCouponService;
    @Autowired
    ValidateCartSalesDroolHelper validateCartSalesDroolHelper;
    @Autowired
    ValidateCartServicesDroolHelper validateCartServicesDroolHelper;
    @Autowired
    CpopClient cpopClient;
    @Autowired
    CpopClientHelper cpopClientHelper;
    @Value("${apiclient.rest.cpopofferms.ctstate}")
    String ctstate;

    @Autowired
    RedisCacheHelper redisCacheHelper;

    public Content validateCartProcessing(CouponOffersRequest couponOffersRequest, HttpHeaders headers, boolean isMDUServiceRequest) {
        log.info("validateCartProcessing start");
        Content content = new Content();
        List<CartItems> cartItems = null;
        HashMap<String, List> ruleExecutionResultMap = null;
        List<MessageDetail> genreMessageDetailList = null;
        List<String> requestCartVideoPlanOffers = new ArrayList<>();
        CTOfferResponse ctOfferResponseOfferCodes = null;
        String ctOfferResponseOfferCode = null;
        ValidationResults validationResults = new ValidationResults();
        boolean skipDroolsExecution = false;
        ValidateCartAsyncResponses validateCartAsyncResponses = null;
        boolean isAcquisitionOfferActionType = false;
        boolean isCouponsAvailableInValidateCartRequest = CollectionUtils.isNotEmpty(couponOffersRequest.getCouponCodes());
        HashMap<String, String> reconnectOfferCodeNActionMap = new HashMap<>();
        //not sports pack request
        if (!isMDUServiceRequest) {
            isAcquisitionOfferActionType = validateCartHelpers.isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType());
            //getOffers-offerCode call
            if (isAcquisitionOfferActionType) {
                requestCartVideoPlanOffers = Optional.ofNullable(couponOffersRequest.getCartContext())
                        .map(CartContexts::getCartOffers)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(cartOffer -> cartOffer.getOfferCode().contains("OF_BASE-"))
                        //.filter(cartOffer -> Constants.VIDEO_PLAN.equalsIgnoreCase(cartOffer.getOfferProductType()))
                        .map(CartOffer::getOfferCode)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(requestCartVideoPlanOffers)) {
                    if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
                        requestCartVideoPlanOffers = Optional.ofNullable(couponOffersRequest.getCartContext())
                                .map(CartContexts::getCartOffers)
                                .orElse(Collections.emptyList())
                                .stream()
                                .filter(Objects::nonNull)
                                .map(CartOffer::getOfferCode)
                                .collect(Collectors.toList());
                    }
                    log.debug("validateCartProcessing - before OfferCodes Call");
                    ctOfferResponseOfferCodes = validateCartHelpers.getOffersOfferCodesCalls(couponOffersRequest, headers, requestCartVideoPlanOffers);
                    log.debug("validateCartProcessing - after OfferCodes Call");
                    if (ctOfferResponseOfferCodes != null && ctOfferResponseOfferCodes.getOffers() != null) {
                        //ctOfferResponseOfferCode = ctOfferResponseOfferCodes.getOffers().stream().filter(Objects::nonNull).map(CTOffer::getCode).findFirst().orElse(null);
                        ctOfferResponseOfferCode = ctOfferResponseOfferCodes.getOffers().stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> offer.getAttributes() != null && Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType()))
                                .map(CTOffer::getCode)
                                .findFirst()
                                .orElse(null);
                    } else {
                        ctOfferResponseOfferCode = null;
                    }
                    if (StringUtils.isEmpty(ctOfferResponseOfferCode) && validateCartHelpers.isContractIndicatorGENRE(couponOffersRequest)) {
                        log.error("No GENRE offers found for the given offer codes in the request");
                        validationResults.setStatus("Error");
                        validationResults.setErrorCode("VALIDATION_ERROR");
                        validationResults.setErrorMessage("GENRE Offers are not supported for the given sales channel");
                        content.setValidationResults(validationResults);
                        skipDroolsExecution = true;
                    } else if (StringUtils.isEmpty(ctOfferResponseOfferCode) && !validateCartHelpers.isContractIndicatorGENRE(couponOffersRequest)) {
                        log.error("No offers found for the given offer codes in the request");
                        validationResults.setStatus("Error");
                        validationResults.setErrorCode("VALIDATION_ERROR");
                        validationResults.setErrorMessage("No offers found for the given video-plan offer codes in the request");
                        content.setValidationResults(validationResults);
                        skipDroolsExecution = true;
                    } else if (null != couponOffersRequest.getReconnectCustomer() && null != couponOffersRequest.getServiceEndDate()) {
                        //if request is reconnect flow, then below method will update correct video-plan
                        validateCartHelpers.updateCartCallOfferCodesIfChangesInReconnectFlow(requestCartVideoPlanOffers, ctOfferResponseOfferCode, couponOffersRequest, reconnectOfferCodeNActionMap);
                    }
                } else if (validateCartHelpers.isContractIndicatorGENRE(couponOffersRequest)) {
                    //for GENRE - cart request not have video-plan Offers
                    log.debug("validateCartProcessing - before video-plan acquisition call");
                    ctOfferResponseOfferCodes = validateCartHelpers.getOffersVideoPlanAcquisitionCall(couponOffersRequest, headers, requestCartVideoPlanOffers);
                    log.debug("validateCartProcessing - after video-plan acquisition call");

                    if (ctOfferResponseOfferCodes != null && ctOfferResponseOfferCodes.getOffers() != null) {
                        //ctOfferResponseOfferCode = ctOfferResponseOfferCodes.getOffers().stream().filter(Objects::nonNull).map(CTOffer::getCode).findFirst().orElse(null);
                        ctOfferResponseOfferCode = ctOfferResponseOfferCodes.getOffers().stream()
                                .filter(Objects::nonNull)
                                .filter(offer -> offer.getAttributes() != null && Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType()))
                                .map(CTOffer::getCode)
                                .findFirst()
                                .orElse(null);
                        requestCartVideoPlanOffers.add(ctOfferResponseOfferCode);

                        if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
                            requestCartVideoPlanOffers = Optional.ofNullable(couponOffersRequest.getCartContext())
                                    .map(CartContexts::getCartOffers)
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .filter(Objects::nonNull)
                                    .map(CartOffer::getOfferCode)
                                    .collect(Collectors.toList());
                            requestCartVideoPlanOffers.add(ctOfferResponseOfferCode);
                            log.debug("validateCartProcessing - before OfferCodes Call");
                            ctOfferResponseOfferCodes = validateCartHelpers.getOffersOfferCodesCalls(couponOffersRequest, headers, requestCartVideoPlanOffers);
                            log.debug("validateCartProcessing - after OfferCodes Call");
                            List<CTOffer> offers = ctOfferResponseOfferCodes.getOffers().stream().filter(Objects::nonNull)
                                    .filter(offer -> offer.getCode().contains("OF_BASE-"))
                                    .collect(Collectors.toList());
                            // Resetting ctOfferResponseOfferCodes and requestCartVideoPlanOffers
                            ctOfferResponseOfferCodes.setOffers(offers);
                            ctOfferResponseOfferCodes.setCount(offers.size());
                            ctOfferResponseOfferCodes.setTotal(offers.size());
                            requestCartVideoPlanOffers = requestCartVideoPlanOffers.stream().filter(Objects::nonNull)
                                    .filter(requestCartVideoPlanOffer -> requestCartVideoPlanOffer.contains("OF_BASE-"))
                                    .collect(Collectors.toList());
                        }

                        if (couponOffersRequest.getContractIndicator().contains(Constants.GENRE) && !validateCartHelpers.isValidSalesChannel(ctOfferResponseOfferCodes.getOffers(), couponOffersRequest.getSalesChannel())) {
                            content.setValidationResults(validateCartHelpers.createValidationResults("Error", "VALIDATION_ERROR", "GENRE Offers not supported for the given sales channel"));
                            skipDroolsExecution = true;
                        }

                    } else {
                        ctOfferResponseOfferCode = null;
                        skipDroolsExecution = true;
                    }
                }
            }
            //parallel calls for getOffers-cart call & coupons validations
            if ((CollectionUtils.isNotEmpty(couponOffersRequest.getCouponCodes()) || !skipDroolsExecution) && (isAcquisitionOfferActionType && CollectionUtils.isNotEmpty(requestCartVideoPlanOffers))) {
                if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)) {
                    if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
                        requestCartVideoPlanOffers = requestCartVideoPlanOffers.stream().filter(Objects::nonNull)
                                .filter(requestCartVideoPlanOffer -> requestCartVideoPlanOffer.contains("OF_BASE-"))
                                .collect(Collectors.toList());
                        List<CTOffer> offers = ctOfferResponseOfferCodes.getOffers().stream().filter(Objects::nonNull)
                                .filter(offer -> offer.getCode().contains("OF_BASE-"))
                                .collect(Collectors.toList());
                        ctOfferResponseOfferCodes.setOffers(offers);
                        ctOfferResponseOfferCodes.setCount(offers.size());
                        ctOfferResponseOfferCodes.setTotal(offers.size());
                    }
                    List<String> bundleProductIds = validateCartHelpers.getBundleProductIdsFromCTOfferResponse(ctOfferResponseOfferCodes);
                    validateCartAsyncResponses = invokeAsync(couponOffersRequest, requestCartVideoPlanOffers, headers, isAcquisitionOfferActionType, bundleProductIds);
                } else {
                    validateCartAsyncResponses = invokeAsync(couponOffersRequest, requestCartVideoPlanOffers, headers, isAcquisitionOfferActionType, new ArrayList<>());
                }

                if (Objects.nonNull(validateCartAsyncResponses)) {
                    if (CollectionUtils.isNotEmpty(validateCartAsyncResponses.getCoupons())) {
                        content.setCoupons(validateCartAsyncResponses.getCoupons());
                    }
                }
            } else {
                //service flow
                validateCartAsyncResponses = invokeAsync(couponOffersRequest, new ArrayList<>(), headers, isAcquisitionOfferActionType, new ArrayList<>());
                if (Objects.nonNull(validateCartAsyncResponses)) {
                    if (Objects.nonNull(validateCartAsyncResponses.getCoupons()) && CollectionUtils.isNotEmpty(validateCartAsyncResponses.getCoupons())) {
                        content.setCoupons(validateCartAsyncResponses.getCoupons());
                    }
                }
            }
        }
        //validate cart for sports pack
        if (isMDUServiceRequest) {
            log.info("validateCartProcessing - before validate cart for sports pack");
            cartItems = validateCartServicesDroolHelper.validateSportPackDroolScenarios(couponOffersRequest);
            log.info("validateCartProcessing - after validate cart for sports pack");
        }

        if (!skipDroolsExecution && Objects.nonNull(couponOffersRequest.getCartContext()) && validateCartHelpers.checkForNonMDUFlow(couponOffersRequest) && featureManagerHelper.isEnabled(Constants.FEATURE_VALIDATECART_GENERIC_FLOW)) {
            log.info("validateCartProcessing - before validate cart for FEATURE_VALIDATECART_GENERIC_FLOW");
            if (isAcquisitionOfferActionType) {
                ruleExecutionResultMap = validateCartSalesDroolHelper.validateCartRulesExecutions(couponOffersRequest, validateCartAsyncResponses, ctOfferResponseOfferCode, ctOfferResponseOfferCodes);
            } else {
                ruleExecutionResultMap = validateCartServicesDroolHelper.validateCartRulesExecutions(couponOffersRequest, validateCartAsyncResponses);
            }
            cartItems = ruleExecutionResultMap.get("cartItemsList");
            genreMessageDetailList = ruleExecutionResultMap.get("messageDetailsList");
            if (genreMessageDetailList != null && genreMessageDetailList.size() > 0) {
                content.setMessages(genreMessageDetailList);
            }
            if (couponOffersRequest.getContractIndicator().contains(Constants.NONCONTRACT) && !isAcquisitionOfferActionType) {
                cartItems = validateCartHelpers.removeDuplicateFromCartItems(cartItems);
            }
        }

        //after executing all the drools, setting the cartItems
        if (CollectionUtils.isNotEmpty(cartItems)) {
            content.setCartItems(cartItems);
        }

        //check coupons flow executed or not
        if (isCouponsAvailableInValidateCartRequest && !isCouponsFlowExecuted) {
            content.setCoupons(invokeCouponFlow(couponOffersRequest));
        }

        // Process delay provisioning logic
        if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
            processDelayProvisioning(isAcquisitionOfferActionType, ctOfferResponseOfferCodes, validateCartAsyncResponses, content, couponOffersRequest);
        }
        log.info("validateCartProcessing end");
        return content;
    }

    private ValidateCartAsyncResponses invokeAsync(CouponOffersRequest couponOffersRequest, List<String> offerCodes, HttpHeaders headers, boolean isAcquisitionOfferActionType, List<String> bundleProductIds) {
        log.info("invokeAsync start");
        List<Callable<Object>> callableObjList = new ArrayList<>();
        // Capture MDC context
        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();
        //coupons call
        if (CollectionUtils.isNotEmpty(couponOffersRequest.getCouponCodes())) {
            log.debug("invokeAsync - before coupons Call");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> invokeCouponFlow(couponOffersRequest)));
            isCouponsFlowExecuted = true;
            log.debug("invokeAsync - after coupons Call");
        } else {
            callableObjList.add(() -> new ArrayList<>());
        }
        //getOffers - cart call - sales flow
        if (isAcquisitionOfferActionType && CollectionUtils.isNotEmpty(offerCodes)) {
            log.debug("invokeAsync - before sales cart Call");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> validateCartHelpers.getOffersCartCallForSales(couponOffersRequest, offerCodes, headers, bundleProductIds)));
            log.debug("invokeAsync - after sales cart Call");
        } else {
            callableObjList.add(() -> new CTOfferResponse());
        }

        if (CollectionUtils.containsAny(couponOffersRequest.getContractIndicator(), validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES))) {
            CTProductRequest ctProductRequest = new CTProductRequest();
            ctProductRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
            ctProductRequest.setProductTypes(Arrays.asList(Constants.VIDEO_ADDON, Constants.VIDEO_PLAN, Constants.PROTECTION_PLAN, Constants.VIDEO_DEVICE));
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> cpopClientHelper.getProducts(ctProductRequest)));
        } else {
            callableObjList.add(() -> new CTProductResponse());
        }

        Set<String> benefitCodesFromGlobalConfig = validateCartHelpers.getBenefitCodeFromGlobalConfigs();
        if (!benefitCodesFromGlobalConfig.isEmpty() && !isAcquisitionOfferActionType) {
            CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
            ctBenefitsRequest.setBenefitCodes(new ArrayList<>(benefitCodesFromGlobalConfig));
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> cpopClient.getBenefitsFromGraphlQL(ctBenefitsRequest,  Arrays.asList("saleschannels", "eligibleIAPPartners"))));
        } else {
            callableObjList.add(() -> new ArrayList<>());
        }

        Object[] responses = null;
        long startTimeInMillis = System.currentTimeMillis();
        responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
        long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
        log.info("ValidateCartAsyncResponses() - invokeAsync() Time Taken -[{}]", (endTimeMillis));

        for (Object obj : responses) {
            if (obj instanceof ServiceException) {
                throw (ServiceException) obj;
            }
        }
        ValidateCartAsyncResponses validateCartAsyncResponses = new ValidateCartAsyncResponses();

        if (responses[0] instanceof List<?>) {
            validateCartAsyncResponses.setCoupons((List) responses[0]);
        }
        if (responses[1] instanceof CTOfferResponse) {
            validateCartAsyncResponses.setCtCartCallOfferResponse((CTOfferResponse) responses[1]);
        }
        if (responses[2] instanceof CTProductResponse) {
            validateCartAsyncResponses.setCtProductResponse((CTProductResponse) responses[2]);
            validateCartAsyncResponses.setProductCodeToProductIdMap(validateCartHelpers.getMapOfProductsKeyFromId((CTProductResponse) responses[2]));
            validateCartAsyncResponses.setProductCodeToProductTypeMap(validateCartHelpers.getProductTypeMaps((CTProductResponse) responses[2]));
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_INCLUDE_PRODUCT_FILTERS)) {
                OffersUtils.filterIncludedProductsBasedOnSalesChannelOrExcludedPartner(validateCartAsyncResponses.getCtProductResponse(), Arrays.asList(couponOffersRequest.getSalesChannel()), validateCartHelpers.getIapPartnerAccountType(couponOffersRequest));
            }
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)) {
                OffersUtils.filterConflictingProductsBasedOnSalesChannelOrExcludedPartner(validateCartAsyncResponses.getCtProductResponse(), Arrays.asList(couponOffersRequest.getSalesChannel()), validateCartHelpers.getIapPartnerAccountType(couponOffersRequest));
            }
        }
        if (responses[3] instanceof CTBenefitsResponse) {
            validateCartAsyncResponses.setCtBenefitsResponse((CTBenefitsResponse) responses[3]);
        }

        log.info("invokeAsync end");
        return validateCartAsyncResponses;
    }

    private List<ValidateCoupon> invokeCouponFlow(CouponOffersRequest couponOffersRequest) {
        Map<String, Coupon> couponMap = new HashMap<>();
        Boolean isCouponValid = true;
        CTCouponResponse cTCouponResponse = null;
        CTOfferResponse cTOfferResponse = null;
        CTOfferResponse cartContextCTOfferResponse = null;
        CampaignProducts campaignProducts = null;
        List<Callable<Object>> callableObjList = new ArrayList<>();
        List<ValidateCoupon> validateCoupons = new ArrayList<>();
        Map<String, List<String>> offerIdsAssociatedToBenefit = new HashMap<>();
        List<String> offerIdsToInvoke = new ArrayList<>();
        List<String> couponNotExist = null;
        Map<String, String> getAllCouponTypes = null;
        // Capture MDC context
        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();

        JsonNode couponDetails = epochUCCPrimaryClient.getCouponDetailsWithMultipleCoupons(couponOffersRequest.getCouponCodes().stream().distinct().collect(Collectors.toList()));
        if (Objects.nonNull(couponDetails)) {
            cTCouponResponse = MAPPER.convertValue(couponDetails, new TypeReference<CTCouponResponse>() {
            });
        }

        if (Objects.nonNull(cTCouponResponse) && CollectionUtils.isNotEmpty(cTCouponResponse.getResults())) {
            cTCouponResponse.getResults().forEach(coupon -> couponMap.put(coupon.getCode(), coupon));
            couponNotExist = couponOffersRequest.getCouponCodes().stream().filter(coupon -> !couponMap.containsKey(coupon)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(couponNotExist)) {
                validateCoupons = couponNotExist.stream().map(coupon -> {
                    ValidateCoupon validateCoupon = new ValidateCoupon();
                    validateCoupon.setCode(coupon);
                    validateCoupon.setValidationResults(validateCartHelpers.createValidationResults("Error", "COUPON_INVALID_ERROR", Constants.COUPON_NOT_EXISTS));
                    return validateCoupon;
                }).collect(Collectors.toList());
            }
        } else {
            validateCoupons = couponOffersRequest.getCouponCodes().stream().map(coupon -> {
                ValidateCoupon validateCoupon = new ValidateCoupon();
                validateCoupon.setCode(coupon);
                validateCoupon.setValidationResults(validateCartHelpers.createValidationResults("Error", "COUPON_INVALID_ERROR", Constants.COUPON_NOT_EXISTS));
                return validateCoupon;
            }).collect(Collectors.toList());
            return validateCoupons;
        }
        Map<String, String> benefitsMapIdReference = new HashMap<>();
        Map<String, Benefit> benefitsMap = new HashMap<>();
        if (!couponMap.isEmpty()) {
            List<String> benefitIds = new ArrayList<>();
            couponOffersRequest.getCouponCodes().forEach(couponCode -> {
                Coupon coupon = couponMap.get(couponCode);
                if (Objects.nonNull(coupon) && CollectionUtils.isNotEmpty(coupon.getCartDiscounts())) {
                    String id = coupon.getCartDiscounts().get(0).getId();
                    benefitIds.add(id);
                    benefitsMapIdReference.put(id, couponCode);
                }
            });
            CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
            ctBenefitsRequest.setBenefitIds(benefitIds);
            ctBenefitsRequest.setState(ctstate);
            CTBenefitsResponse ctBenefitsResponse = epochUCCPrimaryClient.getBenefits(ctBenefitsRequest);
            if (Objects.nonNull(ctBenefitsResponse) && CollectionUtils.isNotEmpty(ctBenefitsResponse.getBenefits())) {
                ctBenefitsResponse.getBenefits().forEach(benefit -> {
                    if (benefitsMapIdReference.containsKey(benefit.getId())) {
                        benefitsMap.put(benefitsMapIdReference.get(benefit.getId()), benefit);
                    }
                });
            }
        }

        if (!benefitsMap.isEmpty()) {
            benefitsMap.forEach((s, benefit) -> {
                List<String> offerIds = new ArrayList<>();
                for (ParentOffers parentOffers : benefit.getParentOffers()) {
                    offerIds.add(parentOffers.getId());
                    offerIdsToInvoke.add(parentOffers.getId());
                }
                offerIdsAssociatedToBenefit.put(s, offerIds);
            });
        }

        List<String> offerCodes = Optional.ofNullable(couponOffersRequest.getCartContext())
                .map(CartContexts::getCartOffers)
                .orElse(Collections.emptyList())
                .stream()
                .map(CartOffer::getOfferCode)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(offerCodes)) {
            CTOfferRequest cartContextCTOfferRequest = couponCartValidationHelper.createCTOfferRequest(null, offerCodes);
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> cpopClient.getOffers(cartContextCTOfferRequest)));
            // callableObjList.add(() -> cpopClient.getOffers(cartContextCTOfferRequest));
        }
        if (CollectionUtils.isNotEmpty(offerIdsToInvoke)) {
            CTOfferRequest ctOfferRequest = couponCartValidationHelper.createCTOfferRequest(offerIdsToInvoke, null);
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                    () -> epochUCCPrimaryClient.getOffers(ctOfferRequest)));
            // callableObjList.add(() -> epochUCCPrimaryClient.getOffers(ctOfferRequest));
        }
        if (Objects.nonNull(cTCouponResponse)) {
            List<String> campaignCodes = Optional.ofNullable(cTCouponResponse).map(CTCouponResponse::getResults).orElse(Collections.emptyList()).stream()
                    .map(coupon -> coupon.getCustom().getFields().getCampaignCode()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(campaignCodes)) {
                ProductRequest productRequest = new ProductRequest();
                productRequest.setProductCodes(campaignCodes);
                callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext,
                        () -> epochUCCPrimaryClient.getCampaignDetailsList(productRequest)));
                //callableObjList.add(() -> epochUCCPrimaryClient.getCampaignDetailsList(productRequest));
            }
        }

        Object[] responses = null;
        long startTimeInMillis = System.currentTimeMillis();
        responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
        long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
        log.info("invokeAsync() Time Taken -[{}]", (endTimeMillis));

        for (Object obj : responses) {
            if (obj instanceof ServiceException) {
                throw (ServiceException) obj;
            }
        }

        if (responses[0] instanceof CTOfferResponse) {
            cartContextCTOfferResponse = (CTOfferResponse) responses[0];
        }
        if (responses[1] instanceof CTOfferResponse) {
            cTOfferResponse = (CTOfferResponse) responses[1];
        }
        if (responses[2] instanceof CampaignProducts) {
            campaignProducts = (CampaignProducts) responses[2];
            getAllCouponTypes = couponCartValidationHelper.getAllCouponType(campaignProducts);
        }

        Map<String, CTOfferResponse> offerSortingWithBenefit = new HashMap<>();
        CTOfferResponse finalCTOfferResponse = cTOfferResponse;
        if (!offerIdsAssociatedToBenefit.isEmpty() && finalCTOfferResponse != null && finalCTOfferResponse.getOffers() != null) {
            offerIdsAssociatedToBenefit.forEach((s, offerIds) -> {
                List<CTOffer> cTOffers = finalCTOfferResponse.getOffers().stream().filter(cTOffer -> offerIds.contains(cTOffer.getId())).collect(Collectors.toList());
                CTOfferResponse ctOfferResponse = new CTOfferResponse();
                ctOfferResponse.setOffers(cTOffers);
                ctOfferResponse.setCount(cTOffers.size());
                ctOfferResponse.setTotal(cTOffers.size());
                ctOfferResponse.setProducts(finalCTOfferResponse.getProducts());
                offerSortingWithBenefit.put(s, ctOfferResponse);
            });
        }
        for (String coupon : couponOffersRequest.getCouponCodes()) {
            if (!couponNotExist.contains(coupon) && offerSortingWithBenefit.containsKey(coupon)) {
                String couponsType = getAllCouponTypes.get(coupon);
                Boolean isRequestValid = validateCartCouponService.validateRequestAttributes(couponOffersRequest, couponMap.get(coupon), coupon, validateCoupons);
                if (!isRequestValid) {
                    break;
                }
                isCouponValid = validateCartCouponService.validateCoupon(couponOffersRequest, couponMap.get(coupon), coupon, validateCoupons, couponsType, true, cartContextCTOfferResponse, offerSortingWithBenefit.get(coupon));
            }
        }
        couponCartValidationHelper.stackableRuleCheckAndSetActions(couponOffersRequest, validateCoupons);
        return validateCoupons;
    }


    /**
     * Processes delay provisioning logic for both acquisition and service flows.
     * For acquisition flows, checks for free trial offers and sets delay provisioning.
     * For service flows, validates customer benefits against exception lists.
     */
    private void processDelayProvisioning(boolean isAcquisitionOfferActionType,
                                          CTOfferResponse ctOfferResponseOfferCodes,
                                          ValidateCartAsyncResponses validateCartAsyncResponses,
                                          Content content,
                                          CouponOffersRequest couponOffersRequest) {
        if (isAcquisitionOfferActionType) {
            final List<String> benefitsExtPromotTypes = new ArrayList<>();
            processDelayProvisioningReason(ctOfferResponseOfferCodes, benefitsExtPromotTypes);
            boolean hasValidExtPromotTypes = org.apache.commons.collections4.CollectionUtils.emptyIfNull(benefitsExtPromotTypes)
                    .stream()
                    .anyMatch(s -> s != null && !s.trim().isEmpty());
            if (hasValidExtPromotTypes) {
                setDelayProvisioningForCartOffers(validateCartAsyncResponses, benefitsExtPromotTypes, content);
            }

        } else {
            HashMap<String, List<String>> productsBenefitsMap = extractCartProductBenefit(couponOffersRequest);
            List<String> validateCartRules = redisCacheHelper != null ? redisCacheHelper.getValidateRules(Constants.DELAYPROVISIONINGBENEFITEXCEPTIONS, Constants.OTT) : Collections.emptyList();
            HashMap<String, List<String>> delayProvisioningBenefitExceptionsMap = validateCartRules != null ? validateCartRules.stream()
                    .filter(Objects::nonNull)
                    .map(rule -> rule.split(":"))
                    .filter(parts -> parts.length == 2 && parts[0] != null && parts[1] != null)
                    .collect(Collectors.toMap(
                            parts -> parts[0],
                            parts -> Arrays.asList(parts[1].split(",")),
                            (a, b) -> b,
                            HashMap::new
                    )) : new HashMap<>();

            List<ProductInfo> ottProducts = Optional.ofNullable(couponOffersRequest)
                    .map(CouponOffersRequest::getCustomerContext)
                    .map(ctx -> ctx.getOtt())
                    .map(ott -> ott.getProducts())
                    .orElse(Collections.emptyList());

            List<String> benefitsCustomerHasReceivedList = ottProducts.stream()
                    .filter(Objects::nonNull)
                    .filter(productInfo -> "video-plan".equalsIgnoreCase(productInfo.getProductType()) && CollectionUtils.isNotEmpty(productInfo.getPromotions()))
                    .flatMap(productInfo -> Optional.ofNullable(productInfo.getPromotions()).orElse(Collections.emptyList()).stream())
                    .filter(Objects::nonNull)
                    .map(CustomerPromotion::getPromotionId)
                    .collect(Collectors.toList());
            final List<String> customerBenefitExtPromoTypes = new ArrayList<String>();
            if (CollectionUtils.isNotEmpty(benefitsCustomerHasReceivedList)) {
                CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
                ctBenefitsRequest.setBenefitCodes(benefitsCustomerHasReceivedList);
                CTBenefitsResponse ctBenefitsResponse = cpopClient != null ? cpopClient.getBenefitsFromGraphlQL(ctBenefitsRequest, Arrays.asList("extPromoType")) : null;
                if (ctBenefitsResponse != null && ctBenefitsResponse.getBenefits() != null) {
                    customerBenefitExtPromoTypes.addAll(ctBenefitsResponse.getBenefits().stream()
                            .filter(Objects::nonNull)
                            .map(Benefit::getExtPromoType)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                }
            }
            List<CartItems> cartItemsList = Optional.ofNullable(content).map(Content::getCartItems).orElse(Collections.emptyList());
            CTProductResponse ctCartCallProducts = validateCartAsyncResponses != null ? validateCartAsyncResponses.getCtProductResponse() : null;
            List<ProductObj> productObjs = ctCartCallProducts != null && ctCartCallProducts.getProducts() != null ? ctCartCallProducts.getProducts() : Collections.emptyList();

            // Collect product codes from cartItemsList
            Set<String> cartProductCodes = cartItemsList.stream()
                    .filter(Objects::nonNull)
                    .map(CartItems::getProductCode)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            // Filter productObjs to only those matching cartProductCodes
            Map<String, ProductObj> productObjMap = productObjs.stream()
                    .filter(Objects::nonNull)
                    .filter(productObj -> cartProductCodes.contains(productObj.getCode()))
                    .collect(Collectors.toMap(ProductObj::getCode, p -> p, (a, b) -> a));

            // Use a Set for allowed actions for O(1) lookup
            Set<String> delayedProvisioningAllowedActions = new java.util.HashSet<>(Arrays.asList("ADD", "CHANGE"));

            cartItemsList.stream()
                    .filter(Objects::nonNull)
                    .filter(cartItem -> {
                        String action = cartItem.getAction();
                        return action != null && delayedProvisioningAllowedActions.contains(action.toUpperCase());
                    })
                    .forEach(cartItem -> {
                        String productCode = cartItem.getProductCode();
                        if (productCode == null) return;

                        List<String> benefitOnProductCustomerAccount = productsBenefitsMap.getOrDefault(productCode, Collections.emptyList());
                        List<String> delayProvisioningExceptionBenefits = delayProvisioningBenefitExceptionsMap.getOrDefault(productCode, Collections.emptyList());

                        if (Collections.disjoint(benefitOnProductCustomerAccount, delayProvisioningExceptionBenefits)) {
                            ProductObj productObj = productObjMap.get(productCode);
                            if (productObj != null && CollectionUtils.isNotEmpty(customerBenefitExtPromoTypes)) {
                                List<Variant> variants = productObj.getVariants();
                                if (variants != null && variants.stream()
                                        .filter(Objects::nonNull)
                                        .map(Variant::getAttributes)
                                        .filter(Objects::nonNull)
                                        .anyMatch(variant -> null != variant.isDelayProvisioning() && variant.isDelayProvisioning() && CollectionUtils.containsAny(variant.getDelayProvisioningReasons(), customerBenefitExtPromoTypes))) {
                                    cartItem.setDelayProvisioning(true);
                                }
                            }
                        }
                    });

        }
    }

    private HashMap<String, List<String>> extractCartProductBenefit(
            CouponOffersRequest couponOffersRequest) {
        CartContexts cartContext = (couponOffersRequest != null) ? couponOffersRequest.getCartContext() : null;
        HashMap<String, List<String>> productsBenefitsMap = new HashMap<String, List<String>>();
        if (cartContext == null || cartContext.getCartProducts() == null) {
            return productsBenefitsMap;
        }

        for (CartProduct product : cartContext.getCartProducts()) {
            List<String> benefitsList = new ArrayList<>();
            if (null != product.getPromotions()) {
                for (CartPromotion customerPromotion : product.getPromotions()) {
                    benefitsList.add(customerPromotion.getPromotionId());
                }
                productsBenefitsMap.put(product.getProductCode(), benefitsList);
            }
        }
        return productsBenefitsMap;
    }

    /**
     * Extracts and sets free trial and delay provisioning reason flags.
     */
    private void processDelayProvisioningReason(CTOfferResponse ctOfferResponseOfferCodes, List<String> benefitsExtPromotTypes) {
        if (Objects.nonNull(ctOfferResponseOfferCodes) && Objects.nonNull(ctOfferResponseOfferCodes.getOffers()) && !ctOfferResponseOfferCodes.getOffers().isEmpty()) {
            CTOffer ctOffer = ctOfferResponseOfferCodes.getOffers().stream()
                    .filter(Objects::nonNull)
                    .filter(offer -> offer.getCode() != null && offer.getCode().contains("OF_BASE-"))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes())
                    && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits())) {
                populateBenefitsExtPromotTypes(ctOffer.getAttributes().getBenefits(), benefitsExtPromotTypes);
            }
        }
    }

    /**
     * Sets delayProvisioning flag for offers in cart if free trial is detected and delay provisioning is required.
     *
     * @param benefitsExtPromotTypes
     */
    private void setDelayProvisioningForCartOffers(ValidateCartAsyncResponses validateCartAsyncResponses, List<String> benefitsExtPromotTypes, Content content) {
        List<CTOffer> ctCartCallOffers = getCartCallOffers(validateCartAsyncResponses);
        List<CartItems> cartItemsList = Optional.ofNullable(content.getCartItems()).orElse(Collections.emptyList());
        ctCartCallOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
            if (isDirectDelayProvisioning(ctOffer)) {
                if (CollectionUtils.containsAny(ctOffer.getAttributes().getDelayProvisioningReasons(), benefitsExtPromotTypes)) {
                    setDelayProvisioningOnMatchingOffers(cartItemsList, ctOffer.getCode());
                }
            } else if (Objects.isNull(ctOffer.getAttributes().isDelayProvisioning()) && hasBundleProducts(ctOffer)) {
                processBundleProductsForDelayProvisioning(cartItemsList, benefitsExtPromotTypes, ctOffer);
            }
        });
    }

    private List<CTOffer> getCartCallOffers(ValidateCartAsyncResponses validateCartAsyncResponses) {
        return Optional.ofNullable(validateCartAsyncResponses)
                .map(ValidateCartAsyncResponses::getCtCartCallOfferResponse)
                .map(CTOfferResponse::getOffers)
                .orElse(Collections.emptyList());
    }

    private boolean isDirectDelayProvisioning(CTOffer ctOffer) {
        return ctOffer.getAttributes() != null && Boolean.TRUE.equals(ctOffer.getAttributes().isDelayProvisioning());
    }

    private boolean hasBundleProducts(CTOffer ctOffer) {
        return ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null
                && !ctOffer.getAttributes().getAssociatedProducts().isEmpty()
                && ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts() != null;
    }

    private void setDelayProvisioningOnMatchingOffers(List<CartItems> cartItemsList, String offerCode) {
        cartItemsList.stream()
                .filter(Objects::nonNull)
                .flatMap(cartItem -> Optional.ofNullable(cartItem.getOffers()).orElse(Collections.emptyList()).stream())
                .filter(Objects::nonNull)
                .filter(offer -> offerCode.equalsIgnoreCase(offer.getCode()))
                .forEach(offer -> offer.setDelayProvisioning(true));
    }

    private void processBundleProductsForDelayProvisioning(List<CartItems> cartItemsList, List<String> benefitsExtPromotTypes, CTOffer ctOffer) {
        List<ProductWrapper> bundleProducts = ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts();
        if (bundleProducts != null && !bundleProducts.isEmpty()) {
            bundleProducts.stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                List<Product> products = bundleProduct.getProducts();
                if (products != null && !products.isEmpty()) {
                    products.stream().filter(Objects::nonNull).forEach(product -> {
                        if (product.getObj() == null || CollectionUtils.isEmpty(product.getObj().getVariants())) return;
                        product.getObj().getVariants().stream().filter(Objects::nonNull)
                                .filter(variant -> variant.getAttributes() != null
                                        && Boolean.TRUE.equals(variant.getAttributes().isDelayProvisioning())
                                        && CollectionUtils.isNotEmpty(variant.getAttributes().getDelayProvisioningReasons())
                                        && CollectionUtils.containsAny(variant.getAttributes().getDelayProvisioningReasons(), benefitsExtPromotTypes))
                                .forEach(variant -> setDelayProvisioningOnMatchingOffers(cartItemsList, ctOffer.getCode()));
                    });
                }
            });
        }
    }

    private boolean populateBenefitsExtPromotTypes(List<Benefit> benefits, List<String> benefitsExtPromotTypes) {
        if (CollectionUtils.isEmpty(benefits)) return false;
        for (Benefit benefit : benefits) {
            if (benefit != null && benefit.getExtPromoType() != null) {
                benefitsExtPromotTypes.add(benefit.getExtPromoType());
            }
        }
        return false;
    }

}

