package com.dtv.dcp.epoch.drools.helper;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.message.MessageDetail;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import org.apache.commons.collections.CollectionUtils;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ValidateCartSalesDroolHelper {
    private static final Logger log = LoggerFactory.getLogger(ValidateCartSalesDroolHelper.class);

    @Autowired
    ValidateCartHelpers validateCartHelpers;
    @Autowired
    DroolsConfiguration droolsConfiguration;

    public HashMap<String, List> validateCartRulesExecutions(CouponOffersRequest couponOffersRequest, ValidateCartAsyncResponses validateCartAsyncResponses, String ctOfferResponseOfferCode, CTOfferResponse ctOfferResponseOfferCodes) {
        log.info("validateCartRulesExecutions() - Start");
        List<MessageDetail> messageDetailsList = new ArrayList<>();
        List<CartItems> cartItemsList = new ArrayList<>();
        HashMap<String, List> ruleExecutionResultMap = new HashMap<>();
        List<String> requestCartOfferCodes = new ArrayList<>();
        Map<String, String> requestCartOfferActionMap = new HashMap<>();
        List<String> getOffersCartCallResponseCodes = new ArrayList<>();
        List<String> preSelectAndDispIncludedAndFeeTypeCodes = new ArrayList<>();
        try {
            Map<String, String> offerCodeWithNameMap = new HashMap<>();
            CTOfferResponse getOffersCartCallResponses = validateCartAsyncResponses.getCtCartCallOfferResponse();
            if (getOffersCartCallResponses == null || getOffersCartCallResponses.getOffers() == null) {
                log.warn("validateCartRulesExecutions() - getOffersCartCallResponses or offers is null, returning empty result");
                return ruleExecutionResultMap;
            }
            // getting the offer name for showing in the validation message
            getOffersCartCallResponses.getOffers().stream().filter(Objects::nonNull).filter(offer -> offer.getCode() != null)
                    .forEach(offer -> offerCodeWithNameMap.put(offer.getCode(), offer.getName().getEn()));
            validateCartHelpers.classifyOfferCodes(getOffersCartCallResponses.getOffers(), preSelectAndDispIncludedAndFeeTypeCodes);
            CTProductResponse ctProductResponse = validateCartAsyncResponses.getCtProductResponse();
            Map<String, List<String>> peacockProductsCondition = validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS);
            List<String> commonRuleCohortList = validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES);
            List<String> genreSpecificRuleCohortList = validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES);
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
            getOffersCartCallResponseCodes = Optional.ofNullable(getOffersCartCallResponses.getOffers())
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(CTOffer::getCode)
                    .collect(Collectors.toList());
            if (Objects.nonNull(ctOfferResponseOfferCodes) && CollectionUtils.isNotEmpty(ctOfferResponseOfferCodes.getOffers())) {
                ctOfferResponseOfferCodes.getOffers().forEach(offer -> {
                    offerCodeWithNameMap.put(offer.getCode(), offer.getName().getEn());
                });
                //Adding the offers from ctOfferResponseOfferCodes to getOffersCartCallResponse for svodMaps logic to process
                getOffersCartCallResponses.getOffers().addAll(ctOfferResponseOfferCodes.getOffers());
            }
            // SLS-IXP-FLAG changes
            if(validateCartHelpers.isVCSLSSalesFlowEnabled()
                    && validateCartHelpers.isAcquisitionWithUniversalCohortAndCustomerSubscriptionTypePresent(couponOffersRequest)) {
                couponOffersRequest.getContractIndicator().clear();
                couponOffersRequest.getContractIndicator().add(couponOffersRequest.getCustomerSubscriptionType());
            }
            List<String> genreBaseOfferCodes = validateCartHelpers.getOfferCodeWithCategoryGenreBase(getOffersCartCallResponses.getOffers());
            Map<String, List<String>> svodProductsMaps = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(getOffersCartCallResponses, couponOffersRequest, ctProductResponse, true, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> nonSvodProductMaps = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(getOffersCartCallResponses, couponOffersRequest, ctProductResponse, false, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> groupingByProductGrp = validateCartHelpers.groupByProductGroup(ctProductResponse, getOffersCartCallResponses, couponOffersRequest, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> peacockOffersCombinations = validateCartHelpers.groupByProductCodeOffersForPeacock(getOffersCartCallResponses, couponOffersRequest, peacockProductsCondition);
            Map<String, List<String>> offerOrProductToRemoveList = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(getOffersCartCallResponses, ctProductResponse, couponOffersRequest, validateCartAsyncResponses.getProductCodeToProductIdMap());
            ruleExecutionResultMap.put("cartItemsList", cartItemsList);
            ruleExecutionResultMap.put("messageDetailsList", messageDetailsList);
            try {
                log.info("validateCartRulesExecutions() - Drools Engine called");
                long startTimeInMillis = System.currentTimeMillis();
                KieSession kieSession = droolsConfiguration.getKieContainerForValidateCartSalesRules().newKieSession();
                kieSession.setGlobal("cartItems", cartItemsList);
                kieSession.setGlobal("messageDetails", messageDetailsList);
                kieSession.setGlobal("genreBaseOfferCodes", genreBaseOfferCodes);
                kieSession.setGlobal("contractIndicator", couponOffersRequest.getContractIndicator());
                kieSession.setGlobal("offerCodeWithNameMap", offerCodeWithNameMap);
                kieSession.setGlobal("svodProductsMaps", svodProductsMaps);
                kieSession.setGlobal("nonSvodProductMaps", nonSvodProductMaps);
                kieSession.setGlobal("requestCartOfferCodes", requestCartOfferCodes);
                kieSession.setGlobal("getOffersCartCallResponseCodes", getOffersCartCallResponseCodes);
                kieSession.setGlobal("commonRuleCohortList", commonRuleCohortList);
                kieSession.setGlobal("genreSpecificRuleCohortList", genreSpecificRuleCohortList);
                kieSession.setGlobal("ctOfferResponseOfferCode", ctOfferResponseOfferCode);
                kieSession.setGlobal("preSelectAndDispIncludedAndFeeTypeCodes", preSelectAndDispIncludedAndFeeTypeCodes);
                kieSession.setGlobal("getGroupingByProductGrpMap", groupingByProductGrp);
                kieSession.setGlobal("peacockOffersCombinations", peacockOffersCombinations);
                kieSession.setGlobal("basePlanChanged", new ArrayList<>());
                kieSession.setGlobal("offerOrProductToRemoveList", offerOrProductToRemoveList);
                kieSession.setGlobal("requestCartOfferActionMap", requestCartOfferActionMap);
                kieSession.setGlobal("log", log);
                kieSession.insert(couponOffersRequest);
                kieSession.fireAllRules();
                kieSession.dispose();
                log.info("validateCartRulesExecutions() - completed & time taken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
            } catch (Exception e) {
                log.error("Exception in validateCartRulesExecutions() : ", e);
                throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Exception in validateCartRulesExecutions() : ", e);
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException(ErrorMessages.EPOCH_VALIDATECART_SALES_EXECUTION_FAILURE, e.getMessage());
        }
        return ruleExecutionResultMap;
    }

}
