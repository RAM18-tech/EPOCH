package com.dtv.dcp.epoch.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dtv.dcp.epoch.model.common.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import org.apache.commons.collections.CollectionUtils;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.message.MessageDetail;


@Component
public class ValidateCartDroolsHelper {

    private static final Logger log = LoggerFactory.getLogger(ValidateCartDroolsHelper.class);
    @Autowired
    CpopClient cpopClient;
    @Autowired
    DroolsConfiguration droolsConfiguration;
    @Autowired
    CpopClientHelper cpopClientHelper;
    @Autowired
    RedisCacheHelper redisCacheHelper;
    @Autowired
    ValidateCartHelper validateCartHelper;
    @Autowired
    FeatureManagerHelper featureManagerHelper;
    @Value("${sportspack1.sku}")
    private String sportSpack1Sku;
    @Value("${sportspack2.sku}")
    private String sportSpack2Sku;
    @Value("${apiclient.rest.cpopofferms.ctstate}")
    private String ctstate;
    @Value("${sportspack.lifetime.promo}")
    private String sportSpackLifetimePromo;
    @Value("${localsBoltOnProduct.sku}")
    private String localsBoltOnProductSKU;
    @Value("${vcRules.repackagedChannels}")
    private String repackagedChannels;

    public List<CartItems> validateSportPackDroolScenarios(Object obj) {
        List<CartItems> cartItemsList = null;
        try {
            log.info("validateSportPackDroolScenarios() called");

            CTProductRequest ctProductRequest = new CTProductRequest();
            ctProductRequest.setProductTypes(Arrays.asList(Constants.VIDEO_PLAN, Constants.VIDEO_ADDON));
            ctProductRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
            ctProductRequest.setBusinessSegment(Arrays.asList(Constants.MDU));
            CTProductResponse productResponse = cpopClientHelper.getProducts(ctProductRequest);
            Map<String, String> productCodeWithNameMap = new HashMap<>();
            if (Objects.nonNull(productResponse) && Objects.nonNull(productResponse.getProducts()) && !productResponse.getProducts().isEmpty()) {
                productCodeWithNameMap = productResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductObj::getCode, product -> product.getName().getEn()));
            }
            CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
            ctBenefitsRequest.setBenefitCodes(Arrays.asList(sportSpackLifetimePromo));
            ctBenefitsRequest.setState(ctstate);
            CTBenefitsResponse ctBenefitsResponse = cpopClient.getBenefitsFromCT(ctBenefitsRequest);
            List<String> compliancePrdKey = validateCartHelper.getComplianceProductKey(ctBenefitsResponse);
            List<String> compliancePrdKeyForPromoInRequest = validateCartHelper.complianceKeyOtherThenSPKLTP((CouponOffersRequest) obj);
            //List<String> compatiblePrdSku1 = getCompatibleProductSKUs(productResponse, sportSpack1Sku);
            List<String> compatiblePrdSku1 = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1, Constants.OTT);
            List<String> compatiblePrdSku2 = validateCartHelper.getCompatibleProductSKUs(productResponse, sportSpack2Sku);
            Map<String, List<String>> offerOrProductToRemoveList = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(null, productResponse, (CouponOffersRequest) obj);
            Map<String, List<String>> groupingByProductGrp = validateCartHelper.groupByProductGroupAndComplianceRank(productResponse, (CouponOffersRequest) obj);
            cartItemsList = new ArrayList<>();
            log.info("validateSportPackDroolScenarios() - Drools Engine called");
            long startTimeInMillis = System.currentTimeMillis();
            KieSession kieSession = droolsConfiguration.getKieContainerValidate().newKieSession();
            kieSession.setGlobal("cartItems", cartItemsList);
            kieSession.setGlobal("compatiblePrdSku1", compatiblePrdSku1);
            kieSession.setGlobal("compatiblePrdSku2", compatiblePrdSku2);
            kieSession.setGlobal("compliancePrdKey", compliancePrdKey);
            kieSession.setGlobal("compliancePrdKeyForPromoInRequest", compliancePrdKeyForPromoInRequest);
            kieSession.setGlobal("SPACK1SKU", sportSpack1Sku);
            kieSession.setGlobal("SPACK2SKU", sportSpack2Sku);
            kieSession.setGlobal("SPACKLTPPROMO", sportSpackLifetimePromo);
            kieSession.setGlobal("getGroupingByProductGrpMap", groupingByProductGrp);
            kieSession.setGlobal("productCodeWithNameMap", productCodeWithNameMap);
            kieSession.setGlobal("offerOrProductToRemoveList", offerOrProductToRemoveList);
            kieSession.setGlobal("log", log);

            kieSession.insert(obj);
            kieSession.fireAllRules();
            kieSession.dispose();
            log.info("validateSportPackDroolScenarios() completed & timetaken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
        } catch (Exception e) {
            log.error("Exception in validateSportPackDroolScenarios() : ", e);
            throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
        }
        return cartItemsList;
    }

    public HashMap<String, List> validateLocalsDroolScenarios(Object obj, String ctOfferResponseOfferCode, List<String> requestCartVideoPlanOffers, CTOfferResponse getOffersCartCallResponse, List<String> getOffersWithOfferCodesPreselectDesignation, List<String> getOffersWithOfferCodesDisplayTypeIncluded, List<String> getOffersWithOfferCodesFeeType, boolean genericRulesExecutionFlag) {
        log.info("validateLocalsDroolScenarios() start");
        CouponOffersRequest couponOffersRequest = (CouponOffersRequest) obj;
        List<CartItems> cartItemsList = new ArrayList<>();
        List<MessageDetail> messageDetailsList = new ArrayList<>();
        HashMap<String, List> ruleExecutionResultMap = new HashMap<>();
        List<String> requestCartOfferCodes = new ArrayList<>();
        List<String> getOfferCodeWithBundleProductSubCategoryLocals = new ArrayList<>();
        List<String> requestCustomerContextOTTProducts = new ArrayList<>();
        List<String> requestCartProductCodes = new ArrayList<>();
        List<String> preSelectAndDispIncludedAndFeeTypeCodes = new ArrayList<>();
        String filteredLocalBoltOn = null;
        List<String> cartCallOfferCodes = new ArrayList<>();
        try {
            String currentZipCode = Optional.ofNullable(couponOffersRequest.getCustomerAddress())
                    .map(address -> address.getZipCode()).orElse(null);
            String currentFipsCode = Optional.ofNullable(couponOffersRequest.getCustomerAddress())
                    .map(address -> address.getFipsCode()).orElse(null);
            String previousZipCode = Optional.ofNullable(couponOffersRequest.getPreviousCustomerAddress())
                    .map(address -> address.getZipCode()).orElse(null);
            String previousFipsCode = Optional.ofNullable(couponOffersRequest.getPreviousCustomerAddress())
                    .map(address -> address.getFipsCode()).orElse(null);
            boolean currentServedMarket = false;
            boolean previousServedMarket = false;
            if (currentZipCode != null && currentFipsCode != null) {
                Boolean isLocalsServed = validateCartHelper.isLocalsServedOrUnServed(currentZipCode, currentFipsCode);
                if (null != isLocalsServed) {
                    currentServedMarket = isLocalsServed;
                } else {
                    currentServedMarket = false;
                }
                if (previousZipCode != null && previousFipsCode != null) {
                    previousServedMarket = previousZipCode.equals(currentZipCode) && previousFipsCode.equals(currentFipsCode)
                            ? currentServedMarket
                            : validateCartHelper.isLocalsServedOrUnServed(previousZipCode, previousFipsCode);
                }
            }
            boolean localsZeroDollarBoltOnFlag = false;
            List<String> values = redisCacheHelper.getValues(Constants.REDIS_CACHE_LOCALS_ZERO_DOLLAR_BOLT_ON, Constants.OTT);
            if (values != null && !values.isEmpty()) {
                localsZeroDollarBoltOnFlag = Boolean.parseBoolean(values.get(0));
            }
            boolean isAcquisitionOfferActionType = validateCartHelper.isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType());
            //Locals sales flow
            if (isAcquisitionOfferActionType) {
                CTOfferResponse ctOfferResponse = getOffersCartCallResponse;
                cartCallOfferCodes = ctOfferResponse.getOffers().stream().filter(Objects::nonNull).map(CTOffer::getCode).collect(Collectors.toList());
                preSelectAndDispIncludedAndFeeTypeCodes = new ArrayList<>(getOffersWithOfferCodesPreselectDesignation);
                preSelectAndDispIncludedAndFeeTypeCodes.addAll(getOffersWithOfferCodesDisplayTypeIncluded);
                preSelectAndDispIncludedAndFeeTypeCodes.addAll(getOffersWithOfferCodesFeeType);
                preSelectAndDispIncludedAndFeeTypeCodes = preSelectAndDispIncludedAndFeeTypeCodes.stream().distinct().collect(Collectors.toList());
                List<CTOffer> ctOffers = new ArrayList<>(ctOfferResponse.getOffers());
                getOfferCodeWithBundleProductSubCategoryLocals = validateCartHelper.getOfferCodeWithBundleProductSubCategoryLocals(ctOffers);
                if (CollectionUtils.isNotEmpty(getOfferCodeWithBundleProductSubCategoryLocals)) {
                    filteredLocalBoltOn = getOfferCodeWithBundleProductSubCategoryLocals.get(0);
                }
                //filteredLocalBoltOn = "OF_" + filteredLocalBoltOn + "_" + couponOffersRequest.getContractIndicator().get(0);
                //Add request base cart offers
                getOfferCodeWithBundleProductSubCategoryLocals.addAll(requestCartVideoPlanOffers);
                requestCartOfferCodes = Optional.ofNullable(couponOffersRequest.getCartContext())
                        .map(CartContexts::getCartOffers)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(CartOffer::getOfferCode)
                        .collect(Collectors.toList());
            } else {//Locals services flow
                filteredLocalBoltOn = localsBoltOnProductSKU;
                requestCustomerContextOTTProducts = Optional.ofNullable(couponOffersRequest.getCustomerContext())
                        .map(CustomerContext::getOtt)
                        .map(CartProduct::getProducts)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(ProductInfo::getProductCode)
                        .collect(Collectors.toList());
                requestCartProductCodes = Optional.ofNullable(couponOffersRequest.getCartContext())
                        .map(CartContexts::getCartProducts)
                        .orElse(Collections.emptyList())
                        .stream()
                        .filter(Objects::nonNull)
                        .map(com.dtv.dcp.epoch.model.common.CartProduct::getProductCode)
                        .collect(Collectors.toList());
            }
            log.info("validateLocalsDroolScenarios() - Drools Engine called");
            long startTimeInMillis = System.currentTimeMillis();
            KieSession kieSession = droolsConfiguration.getKieContainerForLocalsValidateCart().newKieSession();
            ruleExecutionResultMap.put("cartItemsList", cartItemsList);
            ruleExecutionResultMap.put("messageDetailsList", messageDetailsList);
            // Set global variables
            kieSession.setGlobal("cartItems", cartItemsList);
            kieSession.setGlobal("messageDetails", messageDetailsList);
            kieSession.setGlobal("requestCartOfferCodes", requestCartOfferCodes);
            kieSession.setGlobal("currentServedMarket", currentServedMarket);
            kieSession.setGlobal("previousServedMarket", previousServedMarket);
            kieSession.setGlobal("localsZeroDollarBoltOnFlag", localsZeroDollarBoltOnFlag);
            kieSession.setGlobal("isAcquisitionOfferActionType", isAcquisitionOfferActionType);
            kieSession.setGlobal("localsBoltOnProductSKU", localsBoltOnProductSKU);
            kieSession.setGlobal("responseLocalsOfferCodes", getOfferCodeWithBundleProductSubCategoryLocals);
            kieSession.setGlobal("filteredLocalBoltOn", filteredLocalBoltOn);
            kieSession.setGlobal("ctOfferResponseOfferCode", ctOfferResponseOfferCode);
            kieSession.setGlobal("preSelectAndDispIncludedAndFeeTypeCodes", preSelectAndDispIncludedAndFeeTypeCodes);
            kieSession.setGlobal("genericRulesExecutionFlag", genericRulesExecutionFlag);
            kieSession.setGlobal("cartCallOfferCodes", cartCallOfferCodes);
            kieSession.setGlobal("requestCustomerContextOTTProducts", requestCustomerContextOTTProducts);
            kieSession.setGlobal("requestCartProductCodes", requestCartProductCodes);
            kieSession.setGlobal("log", log);
            kieSession.insert(couponOffersRequest);
            kieSession.fireAllRules();
            kieSession.dispose();
            log.info("validateLocalsDroolScenarios() - completed & timetaken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
        } catch (Exception e) {
            log.error("Exception in validateLocalsDroolScenarios() : ", e);
            throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
        }
        log.info("validateLocalsDroolScenarios() end");
        return ruleExecutionResultMap;
    }


    public HashMap<String, List> validateCartRulesExecutions(Object obj, CTOfferResponse getOffersCartCallResponse, List<String> preSelectAndDispIncludedAndFeeTypeCodes, ValidateCartAsyncResponses validateCartAsyncResponses, String ctOfferResponseOfferCode, CTOfferResponse ctOfferResponseOfferCodes) {
        log.info("validateCartRulesExecutions() - Start");
        CouponOffersRequest couponOffersRequest = (CouponOffersRequest) obj;
        List<MessageDetail> messageDetailsList = new ArrayList<>();
        List<CartItems> cartItemsList = new ArrayList<>();
        HashMap<String, List> ruleExecutionResultMap = new HashMap<>();
        List<CTOffer> offers = new ArrayList<>();
        List<String> cartProductCodes = new ArrayList<>();
        List<String> requestCartOfferCodes = new ArrayList<>();
        Map<String, String> requestCartOfferActionMap = new HashMap<>();
        List<String> getOffersCartCallResponseCodes = new ArrayList<>();
        List<String> compatibleProductSKUForBaseProduct = null;
        List<String> commonRuleCohortList = null;
        List<String> genreSpecificRuleCohortList = null;
        List<String> deviceBasedRulesCohortList = null;
        List<String> slsUniversalCohortsList = null;
        try {
            Map<String, String> offerCodeWithNameMap = new HashMap<>();
            Map<String, String> productCodeWithNameMap = new HashMap<>();
            Map<String, List<String>> productConditions = getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_GENRE_PRODUCTCONDITIONS);
            Map<String, List<String>> productDeviceConditions = getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_DEVICE_PRODUCTCONDITIONS);
            Map<String, List<String>> peacockProductsCondition = getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS);
            Map<String, List<String>> mvpxSkipCombinationsMap  = getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVPX_SKIP_COMBINATION);
            commonRuleCohortList = validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES);
            genreSpecificRuleCohortList = validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES);
            deviceBasedRulesCohortList = validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_DEVICE_RULES);
            if (Objects.nonNull(validateCartAsyncResponses.getCtProductResponse()) && Objects.nonNull(validateCartAsyncResponses.getCtProductResponse().getProducts()) && !validateCartAsyncResponses.getCtProductResponse().getProducts().isEmpty()) {
                productCodeWithNameMap = validateCartAsyncResponses.getCtProductResponse().getProducts().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductObj::getCode, product -> product.getName().getEn()));
            }
            List<String> genreBaseProductCodes = validateCartAsyncResponses.getCtProductResponse().getProducts().stream()
                    .filter(Objects::nonNull)
                    .filter(product -> product.getVariants().stream()
                            .anyMatch(variant -> Constants.GENREBASE.equalsIgnoreCase(variant.getAttributes().getCategory())))
                    .map(ProductObj::getCode)
                    .collect(Collectors.toList());

            List<String> baseProductCodes = validateCartAsyncResponses.getCtProductResponse().getProducts().stream()
                    .filter(Objects::nonNull)
                    .filter(product -> product.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_PLAN))
                    .filter(product -> product.getVariants().stream()
                            .anyMatch(variant -> !Constants.GENREBASE.equalsIgnoreCase(variant.getAttributes().getCategory())))
                    .map(ProductObj::getCode)
                    .collect(Collectors.toList());

            List<String> standaloneAddonsProductCodes = validateCartAsyncResponses.getCtProductResponse().getProducts().stream()
                    .filter(Objects::nonNull)
                    .filter(product -> product.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_ADDON))
                    .filter(product -> product.getVariants().stream()
                            .anyMatch(variant -> Constants.STANDALONE.equalsIgnoreCase(variant.getAttributes().getAddOnType())))
                    .map(ProductObj::getCode)
                    .collect(Collectors.toList());

            requestCartOfferCodes = Optional.ofNullable(couponOffersRequest.getCartContext())
                    .map(CartContexts::getCartOffers)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .map(CartOffer::getOfferCode)
                    .collect(Collectors.toList());
         // Build offerCode -> original request action map for restoring retained offer's action after deduplication
            requestCartOfferActionMap = Optional.ofNullable(couponOffersRequest.getCartContext())
                    .map(CartContexts::getCartOffers)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(co -> co.getOfferCode() != null && co.getAction() != null)
                    .collect(Collectors.toMap(CartOffer::getOfferCode, CartOffer::getAction, (a, b) -> a));
            getOffersCartCallResponseCodes = Optional.ofNullable(getOffersCartCallResponse.getOffers())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(CTOffer::getCode)
                    .collect(Collectors.toList());
            if (Objects.nonNull(couponOffersRequest.getCartContext()) && Objects.nonNull(couponOffersRequest.getCartContext().getCartProducts())) {
                cartProductCodes = couponOffersRequest.getCartContext().getCartProducts().stream().filter(Objects::nonNull).map(com.dtv.dcp.epoch.model.common.CartProduct::getProductCode).collect(Collectors.toList());
                compatibleProductSKUForBaseProduct = validateCartHelper.getCompatibleProducts(validateCartAsyncResponses.getCtProductResponse(), cartProductCodes, Constants.VIDEO_PLAN);
            }
            slsUniversalCohortsList = redisCacheHelper.getSwimlaneRules(Constants.UNIVERSAL_COHORTS, Constants.OTT);
            // getting the offer name for showing in the validation message
            if (validateCartHelper.isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
                offers = getOffersCartCallResponse.getOffers();
                offers.forEach(offer -> {
                    offerCodeWithNameMap.put(offer.getCode(), offer.getName().getEn());
                });
                if (Objects.nonNull(ctOfferResponseOfferCodes) && CollectionUtils.isNotEmpty(ctOfferResponseOfferCodes.getOffers())) {
                    ctOfferResponseOfferCodes.getOffers().forEach(offer -> {
                        offerCodeWithNameMap.put(offer.getCode(), offer.getName().getEn());
                    });
                    //Adding the offers from ctOfferResponseOfferCodes to getOffersCartCallResponse for svodMaps logic to process
                    getOffersCartCallResponse.getOffers().addAll(ctOfferResponseOfferCodes.getOffers());
                }
                preSelectAndDispIncludedAndFeeTypeCodes = preSelectAndDispIncludedAndFeeTypeCodes.stream().distinct().collect(Collectors.toList());
                // SLS-IXP-FLAG changes
                if(validateCartHelper.isVCSLSSalesFlowEnabled()
                    && validateCartHelper.isAcquisitionWithUniversalCohortAndCustomerSubscriptionTypePresent(couponOffersRequest)) {
                        couponOffersRequest.getContractIndicator().clear();
                        couponOffersRequest.getContractIndicator().add(couponOffersRequest.getCustomerSubscriptionType());
                }
            // SLS-IXP-FLAG changes
            }else if (validateCartHelper.isVCSLSServicesFlowEnabled()){
                //swimlaneSwitchEligible true
                if(Objects.nonNull(couponOffersRequest.getSwimlaneSwitchEligible()) && couponOffersRequest.getSwimlaneSwitchEligible()){
                    validateCartHelper.UpdatingDeviceInCartFromCCForSLS(couponOffersRequest, validateCartAsyncResponses.getProductCodeToTypeMap());
                    //clearing the contract indicator for swimlane switch eligible request as new contract indicator will be the same as the slsEligibleSubscriptionType
                    couponOffersRequest.getContractIndicator().clear();
                    couponOffersRequest.getContractIndicator().add(couponOffersRequest.getSlsEligibleSubscriptionType());
                } //swimlaneSwitchEligible false
                else if (Objects.nonNull(couponOffersRequest.getSwimlaneSwitchEligible())
                        && !couponOffersRequest.getSwimlaneSwitchEligible()
                        && CollectionUtils.isNotEmpty(slsUniversalCohortsList)) {
                    boolean isUniversalCohortMatch = slsUniversalCohortsList.stream()
                            .filter(Objects::nonNull)
                            .anyMatch(slsUniversalCohorts -> couponOffersRequest.getContractIndicator().stream()
                                    .filter(Objects::nonNull)
                                    .anyMatch(contractIndicator -> contractIndicator.equalsIgnoreCase(slsUniversalCohorts)));
                    if (isUniversalCohortMatch && Objects.nonNull(couponOffersRequest.getCustomerSubscriptionType())) {
                        couponOffersRequest.getContractIndicator().clear();
                        couponOffersRequest.getContractIndicator().add(couponOffersRequest.getCustomerSubscriptionType());
                    }
                }
            }
            List<String> genreBaseOfferCodes = validateCartHelper.getOfferCodeWithCategoryGenreBase(offers);
            String currentBasePlan = null;
            if (Objects.nonNull(couponOffersRequest.getCustomerContext()) && Objects.nonNull(couponOffersRequest.getCustomerContext().getOtt()) && Objects.nonNull(couponOffersRequest.getCustomerContext().getOtt().getProducts())) {
                //Adding the product type in cart products for the request cart products as not there in AID
                validateCartHelper.updatingProductTypeInCartProducts(couponOffersRequest, validateCartAsyncResponses.getProductCodeToTypeMap());
                currentBasePlan = couponOffersRequest.getCustomerContext().getOtt().getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> product.getProductCode() != null && product.getProductType() != null)
                        .filter(product -> product.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN))
                        .map(ProductInfo::getProductCode)
                        .findFirst()
                        .orElse(null);
            }
            Map<String, List<String>>  offerOrProductToRemoveList = validateCartHelper.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(getOffersCartCallResponse, validateCartAsyncResponses.getCtProductResponse(), couponOffersRequest);
            Map<String, List<String>> svodProductsMaps = validateCartHelper.getEmbeddedSVODProductsFromGetOffersGeneric(getOffersCartCallResponse, couponOffersRequest, validateCartAsyncResponses.getCtProductResponse(), true);
            Map<String, List<String>> nonSvodProductMaps = validateCartHelper.getEmbeddedSVODProductsFromGetOffersGeneric(getOffersCartCallResponse, couponOffersRequest, validateCartAsyncResponses.getCtProductResponse(), false);
            Map<String, List<String>> reverseMapForSvod = reverseMap(svodProductsMaps);
            // Use overloaded groupByProductGroup: for services returns productCode-level map,
            // for acquisition/sales returns offerCode-level map for billing product group deduplication
            Map<String, List<String>> groupingByProductGrp = validateCartHelper.groupByProductGroup(
                    validateCartAsyncResponses.getCtProductResponse(), getOffersCartCallResponse, couponOffersRequest);
            Map<String, List<String>> peacockOffersCombinations = validateCartHelper.groupByProductCodeOffersForPeacock(getOffersCartCallResponse, couponOffersRequest, peacockProductsCondition);
            Map<String, List<String>> repackagedChannelsMap = validateCartHelper.getConfigMap(repackagedChannels);
            Map<String, List<String>> customerContextPromotions = validateCartHelper.getCustomerContextPromotions(couponOffersRequest);
            Map<String, Benefit> promotionsCTAttributes = validateCartHelper.getPromoDetails(couponOffersRequest);
            HashMap<String, Map<String, String>> incompatibleSKURules=getIncompatiableSKUResolutionRule(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_INCOMPATIBLE_SKU_RULES);
            ruleExecutionResultMap.put("cartItemsList", cartItemsList);
            ruleExecutionResultMap.put("messageDetailsList", messageDetailsList);
            log.info("validateCartRulesExecutions() - Drools Engine called");
            long startTimeInMillis = System.currentTimeMillis();
            ArrayList<String> tempProductCodes = new ArrayList<>();
            KieSession kieSession = droolsConfiguration.getKieContainerForValidateCartRules().newKieSession();
            kieSession.setGlobal("cartItems", cartItemsList);
            kieSession.setGlobal("messageDetails", messageDetailsList);
            kieSession.setGlobal("genreBaseProductCodes", genreBaseProductCodes);
            kieSession.setGlobal("baseProductCodes", baseProductCodes);
            kieSession.setGlobal("genreBaseOfferCodes", genreBaseOfferCodes);
            kieSession.setGlobal("contractIndicator", couponOffersRequest.getContractIndicator());
            kieSession.setGlobal("offerCodeWithNameMap", offerCodeWithNameMap);
            kieSession.setGlobal("svodProductsMaps", svodProductsMaps);
            kieSession.setGlobal("nonSvodProductMaps", nonSvodProductMaps);
            kieSession.setGlobal("productCodeWithNameMap", productCodeWithNameMap);
            kieSession.setGlobal("cartProductCodes", cartProductCodes);
            kieSession.setGlobal("requestCartOfferCodes", requestCartOfferCodes);
            kieSession.setGlobal("getOffersCartCallResponseCodes", getOffersCartCallResponseCodes);
            kieSession.setGlobal("productConditionsConfig", productConditions);
            kieSession.setGlobal("commonRuleCohortList", commonRuleCohortList);
            kieSession.setGlobal("genreSpecificRuleCohortList", genreSpecificRuleCohortList);
            kieSession.setGlobal("ctOfferResponseOfferCode", ctOfferResponseOfferCode);
            kieSession.setGlobal("preSelectAndDispIncludedAndFeeTypeCodes", preSelectAndDispIncludedAndFeeTypeCodes);
            kieSession.setGlobal("deviceDependentProductConditions", productDeviceConditions);
            kieSession.setGlobal("deviceBasedRulesCohortList", deviceBasedRulesCohortList);
            kieSession.setGlobal("standaloneAddonsProductCodes", standaloneAddonsProductCodes);
            kieSession.setGlobal("reverseMapForSvod", reverseMapForSvod);
            kieSession.setGlobal("getGroupingByProductGrpMap", groupingByProductGrp);
            kieSession.setGlobal("peacockProductsCondition", peacockProductsCondition);
            kieSession.setGlobal("peacockOffersCombinations", peacockOffersCombinations);
            kieSession.setGlobal("mvpxSkipCombinationsMap", mvpxSkipCombinationsMap);
            kieSession.setGlobal("tempProductCodes", tempProductCodes);

            kieSession.setGlobal("optimoMasRepackagingEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_OPTIMOMAS_REPACKAGING_ENABLED));
            kieSession.setGlobal("basePlanChanged", new ArrayList<>());
            kieSession.setGlobal("repackagedChannelsMap", repackagedChannelsMap);
            kieSession.setGlobal("offerOrProductToRemoveList", offerOrProductToRemoveList);
            kieSession.setGlobal("requestCartOfferActionMap", requestCartOfferActionMap);
            kieSession.setGlobal("compRankingBillingEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC));
            kieSession.setGlobal("log", log);
            // Global Rules for ValidateCart
            List<String> validateCartRules = redisCacheHelper.getValidateRules(Constants.DISCOUNTRULES, Constants.OTT);
            kieSession.setGlobal("globalValidateCartEvaluator", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_GLOBAL_VALIDATE_CART_EVALUATOR));
            kieSession.setGlobal("validateCartRules", validateCartRules);
            kieSession.setGlobal("customerContextPromotions", customerContextPromotions);
            kieSession.setGlobal("promotionsCTAttributes", promotionsCTAttributes);
            kieSession.setGlobal("currentBasePlan", currentBasePlan);
            kieSession.setGlobal("peacockEmbeddedEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_PEACOCK_VALIDATE_CART_RULES));
            kieSession.setGlobal("compatibleProductSKUForBaseProduct", compatibleProductSKUForBaseProduct);
            kieSession.setGlobal("incompatibleSKURules", incompatibleSKURules);
            kieSession.setGlobal("peacockPhaseVCrulesEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS));
            kieSession.setGlobal("promoSuppressionEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_PROMO_SUPPRESSION_IN_VC));
            kieSession.setGlobal("productCodewithTypeMap", validateCartAsyncResponses.getProductCodeToProductTypeMap());
            kieSession.setGlobal("suppressingVideoAddon", featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VALIDATECART_SUPPRESS_VIDEOADDON));
            kieSession.setGlobal("discountDuoSVODEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_VC_DISCOUNT_DUO));
            kieSession.insert(couponOffersRequest);
            kieSession.fireAllRules();
            kieSession.dispose();
            ruleExecutionResultMap.put("tempProductCodes", tempProductCodes);
            log.info("validateCartRulesExecutions() - completed & timetaken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
        } catch (Exception e) {
            log.error("Exception in validateCartRulesExecutions() : ", e);
            throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
        }
        return ruleExecutionResultMap;
    }

    // Note in global config we should config like below for preconditions
    // BOLT-MYSPORTSEXTRA-202410:BOLT-MYSPORTS-202410; BOLT-MYHOME-202502:BOLT-MYSPORTS-202410;
    // BOLT-MYCINEMA-202410:BOLT-MYNEWS-202410,BOLT-MYSPORTS-202410,BOLT-MYENTERTAINMENT-202410
    private Map<String, List<String>> getPreconditionsConfig(String key) {
        Map<String, List<String>> productConditions = new HashMap<>();
        List<String> preconditionConfigs = redisCacheHelper.getValues(key, Constants.OTT);
        if (CollectionUtils.isNotEmpty(preconditionConfigs)) {
            preconditionConfigs = Arrays.asList(preconditionConfigs.get(0).split("\\s*;\\s*"));
            for (String preconditionConfig : preconditionConfigs) {
                String[] preconditionConfigArray = preconditionConfig.split("\\s*:\\s*");
                if (preconditionConfigArray.length == 2) {
                    productConditions.put(preconditionConfigArray[0], Arrays.asList(preconditionConfigArray[1].split("\\s*,\\s*")));
                }
            }
        }
        return productConditions;
    }

    private Map<String, List<String>> reverseMap(Map<String, List<String>> svodProductsMaps) {
        Map<String, List<String>> reversedMap = new HashMap<>();
        if (svodProductsMaps == null || svodProductsMaps.isEmpty()) {
            return reversedMap;
        }
        // Reverse the map to have values as keys and original keys as values
        for (Map.Entry<String, List<String>> entry : svodProductsMaps.entrySet()) {
            String originalKey = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null) {
                for (String value : values) {
                    reversedMap.computeIfAbsent(value, k -> new ArrayList<>()).add(originalKey);
                }
            }
        }
        return reversedMap;
    }


    // Note in global config we should config like below for preconditions
    // BASE-ULTIMATE-201811+BOLT-BOLT-PEACOCKPRE-202208:BOLT-PEACOCKEMBED-202506
    private HashMap<String, Map<String, String>> getIncompatiableSKUResolutionRule(String key) {
        if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_PRODUCT_FILTERS)) {
            List<String> inCompatiableSKURules = redisCacheHelper.getValidateRules(key, Constants.OTT);

            return inCompatiableSKURules == null ? new HashMap<>() :
                    inCompatiableSKURules.stream()
                            .map(rule -> rule.split("\\s*:\\s*"))
                            .filter(parts -> parts.length == 2)
                            .map(parts -> new String[]{parts[0], parts[1]})
                            .map(preRes -> {
                                String[] preconditionParts = preRes[0].split("\\s*\\+\\s*");
                                return preconditionParts.length == 2 ? new String[]{preconditionParts[0], preconditionParts[1], preRes[1]} : null;
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.groupingBy(
                                    arr -> arr[0],
                                    HashMap::new,
                                    Collectors.toMap(arr -> arr[1], arr -> arr[2], (v1, v2) -> v1, HashMap::new)
                            ));
        }
        return new HashMap<>();
    }

    /**
     * Executes the SLS-specific Drools rules (ValidateCartSLSRules.drl) against the
     * already-computed cartItems. Called from the processor after the main rules execution
     * when swimlaneSwitchEligible == true.
     *
     * @param couponOffersRequest  the incoming request (must have swimlaneSwitchEligible == true)
     * @param cartItems            the cartItems list produced by the main rules execution (modified in-place)
     * @param tempProductCodes     the tempProductCodes list from the main rules execution
     * @param validateCartAsyncResponses async responses containing CT product/benefits data
     */
    public void validateSLSDroolScenarios(CouponOffersRequest couponOffersRequest,
                                          List<CartItems> cartItems,
                                          List<String> tempProductCodes,
                                          ValidateCartAsyncResponses validateCartAsyncResponses) {
        log.info("validateSLSDroolScenarios() - Start");
        try {
            CTProductResponse ctProductResponse = validateCartAsyncResponses != null
                    ? validateCartAsyncResponses.getCtProductResponse() : null;
            List<String> slsCommonFreeProductCodes = null;
            Map<String, List<String>> slsCommonProductsMap = null;
            // SLS globals
            List<String> customerContextProductCodes = Optional.ofNullable(couponOffersRequest.getCustomerContext())
                    .map(CustomerContext::getOtt)
                    .map(CartProduct::getProducts)
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(productInfo -> productInfo.getProductType() != null
                            && !productInfo.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN))
                    .map(ProductInfo::getProductCode)
                    .collect(Collectors.toList());

            Map<String, List<String>> benefitsSwimlaneIndicatorMap = validateCartHelper.getBenefitsSwimlaneIndicator(
                    validateCartAsyncResponses != null ? validateCartAsyncResponses.getCtBenefitsResponse() : null);

            slsCommonFreeProductCodes = redisCacheHelper.getSwimlaneRules(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_SLS_COMMON_PRODUCT_SKUS, Constants.OTT);
            slsCommonProductsMap = validateCartHelper.getPreconditionsSLSCommonConfig(slsCommonFreeProductCodes, couponOffersRequest.getSlsEligibleSubscriptionType());


            Map<String, String> promoToProductMapCC = validateCartHelper.promoToProductMap(couponOffersRequest);

            List<String> productToSuppressForSLS = redisCacheHelper.getSwimlaneRules(
                    Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_SLS_PRODUCT_TO_SUPPRESS, Constants.OTT);

            List<String> cartProductCodes = Optional.ofNullable(couponOffersRequest.getCartContext())
                    .map(CartContexts::getCartProducts)
                    .orElse(Collections.emptyList())
                    .stream().filter(Objects::nonNull)
                    .map(com.dtv.dcp.epoch.model.common.CartProduct::getProductCode)
                    .collect(Collectors.toList());

            List<String> compatibleProductSKUForBaseProduct = validateCartHelper.getCompatibleProducts(
                    ctProductResponse, cartProductCodes, Constants.VIDEO_PLAN);

            Map<String, String> productCodeWithNameMap = new HashMap<>();
            if (ctProductResponse != null && ctProductResponse.getProducts() != null) {
                productCodeWithNameMap = ctProductResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductObj::getCode, p -> p.getName().getEn()));
            }

            Map<String, List<String>> svodProductsMaps = validateCartHelper.getEmbeddedSVODProductsFromGetOffersGeneric(
                    null, couponOffersRequest, ctProductResponse, true);

            Map<String, List<String>> groupingByProductGrp = validateCartHelper.groupByProductGroup(
                    ctProductResponse, null, couponOffersRequest);

            List<String> commonRuleCohortList = validateCartHelper.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES);

            log.info("validateSLSDroolScenarios() - Drools SLS Engine called");
            long startTimeInMillis = System.currentTimeMillis();
            KieSession kieSession = droolsConfiguration.getKieContainerForValidateCartSLSRules().newKieSession();
            kieSession.setGlobal("cartItems", cartItems);
            kieSession.setGlobal("benefitsSwimlaneIndicatorMap", benefitsSwimlaneIndicatorMap);
            kieSession.setGlobal("compatibleProductSKUForBaseProduct",
                    compatibleProductSKUForBaseProduct != null ? new ArrayList<>(compatibleProductSKUForBaseProduct) : new ArrayList<>());
            kieSession.setGlobal("contractIndicator", couponOffersRequest.getContractIndicator());
            kieSession.setGlobal("slsCommonProductsMap", slsCommonProductsMap);
            kieSession.setGlobal("promoToProductMapCC", promoToProductMapCC != null ? promoToProductMapCC : new HashMap<>());
            kieSession.setGlobal("customerContextProductCodes",
                    customerContextProductCodes != null ? new ArrayList<>(customerContextProductCodes) : new ArrayList<>());
            kieSession.setGlobal("productToSuppressForSLS",
                    productToSuppressForSLS != null ? new ArrayList<>(productToSuppressForSLS) : new ArrayList<>());
            kieSession.setGlobal("svodProductsMaps", svodProductsMaps != null ? svodProductsMaps : new HashMap<>());
            kieSession.setGlobal("productCodeWithNameMap", productCodeWithNameMap);
            kieSession.setGlobal("tempProductCodes",
                    tempProductCodes != null ? new ArrayList<>(tempProductCodes) : new ArrayList<>());
            kieSession.setGlobal("getGroupingByProductGrpMap", groupingByProductGrp != null ? groupingByProductGrp : new HashMap<>());
            kieSession.setGlobal("commonRuleCohortList",
                    commonRuleCohortList != null ? commonRuleCohortList : new ArrayList<>());
            kieSession.setGlobal("log", log);

            kieSession.insert(couponOffersRequest);
            kieSession.fireAllRules();
            kieSession.dispose();
            log.info("validateSLSDroolScenarios() - completed & time taken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
        } catch (Exception e) {
            log.error("Exception in validateSLSDroolScenarios() : ", e);
            throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
        }
        log.info("validateSLSDroolScenarios() - End");
    }
}
