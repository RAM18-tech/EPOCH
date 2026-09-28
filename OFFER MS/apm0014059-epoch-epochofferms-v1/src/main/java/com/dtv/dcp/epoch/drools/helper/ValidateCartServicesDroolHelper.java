package com.dtv.dcp.epoch.drools.helper;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.config.DroolsConfiguration;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.ValidateCartAsyncResponses;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.message.MessageDetail;
import com.dtv.dcp.epoch.processor.helper.ValidateCartHelpers;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ValidateCartServicesDroolHelper {
    private static final Logger log = LoggerFactory.getLogger(ValidateCartServicesDroolHelper.class);
    @Autowired
    ValidateCartHelpers validateCartHelpers;
    @Autowired
    DroolsConfiguration droolsConfiguration;
    @Autowired
    RedisCacheHelper redisCacheHelper;
    @Autowired
    FeatureManagerHelper featureManagerHelper;
    @Value("${vcRules.repackagedChannels}")
    private String repackagedChannels;
    @Value("${sportspack1.sku}")
    private String sportSpack1Sku;
    @Value("${sportspack2.sku}")
    private String sportSpack2Sku;
    @Value("${apiclient.rest.cpopofferms.ctstate}")
    private String ctstate;
    @Value("${sportspack.lifetime.promo}")
    private String sportSpackLifetimePromo;
    @Autowired
    CpopClientHelper cpopClientHelper;
    @Autowired
    CpopClient cpopClient;

    public List<CartItems> validateSportPackDroolScenarios(Object obj) {
        List<CartItems> cartItemsList = null;
        try {
            log.info("validateSportPackDroolScenarios() called");

            CTProductRequest ctProductRequest = new CTProductRequest();
            ctProductRequest.setProductTypes(Arrays.asList(Constants.VIDEO_PLAN, Constants.VIDEO_ADDON));
            ctProductRequest.setProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
            ctProductRequest.setBusinessSegment(Arrays.asList(Constants.MDU));
            CTProductResponse productResponse = cpopClientHelper.getProducts(ctProductRequest);

            CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
            ctBenefitsRequest.setBenefitCodes(Arrays.asList(sportSpackLifetimePromo));
            ctBenefitsRequest.setState(ctstate);
            CTBenefitsResponse ctBenefitsResponse = cpopClient.getBenefits(ctBenefitsRequest);
            Map<String, String> productCodeWithNameMap = new HashMap<>();
            if (Objects.nonNull(productResponse) && Objects.nonNull(productResponse.getProducts()) && !productResponse.getProducts().isEmpty()) {
                productCodeWithNameMap = productResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductObj::getCode, product -> product.getName().getEn()));
            }
            Map<String, String> productIdToMap = validateCartHelpers.getMapOfProductsKeyFromId(productResponse);
            List<String> compliancePrdKey = validateCartHelpers.getComplianceProductKey(ctBenefitsResponse);
            List<String> compliancePrdKeyForPromoInRequest = validateCartHelpers.complianceKeyOtherThenSPKLTP((CouponOffersRequest) obj);
            //List<String> compatiblePrdSku1 = getCompatibleProductSKUs(productResponse, sportSpack1Sku);
            List<String> compatiblePrdSku1 = redisCacheHelper.getValues(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_COMPATIBLESPK_SKU1, Constants.OTT);
            List<String> compatiblePrdSku2 = validateCartHelpers.getCompatibleProductSKUs(productResponse, sportSpack2Sku);
            Map<String, List<String>> offerOrProductToRemoveList = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(null, productResponse, (CouponOffersRequest) obj, productIdToMap);
            Map<String, List<String>> groupingByProductGrp = validateCartHelpers.groupByProductGroupAndComplianceRank(productResponse, (CouponOffersRequest) obj);
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


    public HashMap<String, List> validateCartRulesExecutions(CouponOffersRequest couponOffersRequest, ValidateCartAsyncResponses validateCartAsyncResponses) {
        log.info("validateCartRulesExecutions() - Start");
        List<MessageDetail> messageDetailsList = new ArrayList<>();
        List<CartItems> cartItemsList = new ArrayList<>();
        HashMap<String, List> ruleExecutionResultMap = new HashMap<>();
        List<String> cartProductCodes = new ArrayList<>();
        List<String> compatibleProductSKUForBaseProduct = null;
        List<String> genreBaseProductCodes = new ArrayList<>();
        List<String> baseProductCodes = new ArrayList<>();
        List<String> standaloneAddonsProductCodes = new ArrayList<>();
        try {
            CTProductResponse ctProductResponse = validateCartAsyncResponses.getCtProductResponse();
            Map<String, String> productCodeWithNameMap = new HashMap<>();
            Map<String, List<String>> productConditions = validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_GENRE_PRODUCTCONDITIONS);
            Map<String, List<String>> productDeviceConditions = validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_DEVICE_PRODUCTCONDITIONS);
            Map<String, List<String>> peacockProductsCondition = validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVP_PEACOCK_PRODUCTCONDITIONS);
            Map<String, List<String>> mvpxSkipCombinationsMap = validateCartHelpers.getPreconditionsConfig(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_MVPX_SKIP_COMBINATION);
            List<String> commonRuleCohortList = validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_COMMON_RULES);
            List<String> genreSpecificRuleCohortList = validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_GENRE_RULES);
            List<String> deviceBasedRulesCohortList = validateCartHelpers.getConfigList(Constants.REDIS_CACHE_COHORTS_FOR_VC_DEVICE_RULES);
            if (Objects.nonNull(ctProductResponse) && Objects.nonNull(ctProductResponse.getProducts()) && !ctProductResponse.getProducts().isEmpty()) {
                productCodeWithNameMap = ctProductResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(ProductObj::getCode, product -> product.getName().getEn()));
                genreBaseProductCodes = ctProductResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> product.getVariants().stream()
                                .anyMatch(variant -> Constants.GENREBASE.equalsIgnoreCase(variant.getAttributes().getCategory())))
                        .map(ProductObj::getCode)
                        .collect(Collectors.toList());
                baseProductCodes = ctProductResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> product.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_PLAN))
                        .filter(product -> product.getVariants().stream()
                                .anyMatch(variant -> !Constants.GENREBASE.equalsIgnoreCase(variant.getAttributes().getCategory())))
                        .map(ProductObj::getCode)
                        .collect(Collectors.toList());
                standaloneAddonsProductCodes = ctProductResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> product.getProductType().getKey().equalsIgnoreCase(Constants.VIDEO_ADDON))
                        .filter(product -> product.getVariants().stream()
                                .anyMatch(variant -> Constants.STANDALONE.equalsIgnoreCase(variant.getAttributes().getAddOnType())))
                        .map(ProductObj::getCode)
                        .collect(Collectors.toList());
            }

            if (Objects.nonNull(couponOffersRequest.getCartContext()) && Objects.nonNull(couponOffersRequest.getCartContext().getCartProducts())) {
                cartProductCodes = couponOffersRequest.getCartContext().getCartProducts().stream().filter(Objects::nonNull).map(com.dtv.dcp.epoch.model.common.CartProduct::getProductCode).collect(Collectors.toList());
                compatibleProductSKUForBaseProduct = validateCartHelpers.getCompatibleProducts(ctProductResponse, cartProductCodes, Constants.VIDEO_PLAN);
            }

            String currentBasePlan = null;
            if (Objects.nonNull(couponOffersRequest.getCustomerContext()) && Objects.nonNull(couponOffersRequest.getCustomerContext().getOtt()) && Objects.nonNull(couponOffersRequest.getCustomerContext().getOtt().getProducts())) {
                //Adding the product type in cart products for the request cart products as not there in AID
                validateCartHelpers.updatingProductTypeInCartProducts(couponOffersRequest, validateCartAsyncResponses.getProductCodeToProductTypeMap());
                currentBasePlan = couponOffersRequest.getCustomerContext().getOtt().getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> product.getProductCode() != null && product.getProductType() != null)
                        .filter(product -> product.getProductType().equalsIgnoreCase(Constants.VIDEO_PLAN))
                        .map(ProductInfo::getProductCode)
                        .findFirst()
                        .orElse(null);
            }
            Map<String, List<String>> svodProductsMaps = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(null, couponOffersRequest, ctProductResponse, true, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> nonSvodProductMaps = validateCartHelpers.getEmbeddedSVODProductsFromGetOffersGeneric(null, couponOffersRequest, ctProductResponse, false, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> reverseMapForSvod = validateCartHelpers.reverseMap(svodProductsMaps);
            Map<String, List<String>> groupingByProductGrp = validateCartHelpers.groupByProductGroup(ctProductResponse, null, couponOffersRequest, validateCartAsyncResponses.getProductCodeToProductIdMap());
            Map<String, List<String>> repackagedChannelsMap = validateCartHelpers.getConfigMap(repackagedChannels);
            Map<String, List<String>> customerContextPromotions = validateCartHelpers.getCustomerContextPromotions(couponOffersRequest);
            Map<String, Benefit> promotionsCTAttributes = validateCartHelpers.getPromoDetails(validateCartAsyncResponses.getCtBenefitsResponse());
            HashMap<String, Map<String, String>> incompatibleSKURules = validateCartHelpers.getIncompatiableSKUResolutionRule(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_INCOMPATIBLE_SKU_RULES);
            Map<String, List<String>> offerOrProductToRemoveList = validateCartHelpers.conflictingOfferOrProductToRemoveBasedOnConflictingProducts(null, ctProductResponse, couponOffersRequest, validateCartAsyncResponses.getProductCodeToProductIdMap());
            ruleExecutionResultMap.put("cartItemsList", cartItemsList);
            ruleExecutionResultMap.put("messageDetailsList", messageDetailsList);
            try {
                log.info("validateCartRulesExecutions() - Drools Engine called");
                long startTimeInMillis = System.currentTimeMillis();
                KieSession kieSession = droolsConfiguration.getKieContainerForValidateCartServicesRules().newKieSession();
                kieSession.setGlobal("cartItems", cartItemsList);
                kieSession.setGlobal("messageDetails", messageDetailsList);
                kieSession.setGlobal("genreBaseProductCodes", genreBaseProductCodes);
                kieSession.setGlobal("baseProductCodes", baseProductCodes);
                kieSession.setGlobal("contractIndicator", couponOffersRequest.getContractIndicator());
                kieSession.setGlobal("svodProductsMaps", svodProductsMaps);
                kieSession.setGlobal("nonSvodProductMaps", nonSvodProductMaps);
                kieSession.setGlobal("productCodeWithNameMap", productCodeWithNameMap);
                kieSession.setGlobal("cartProductCodes", cartProductCodes);
                kieSession.setGlobal("productConditionsConfig", productConditions);
                kieSession.setGlobal("commonRuleCohortList", commonRuleCohortList);
                kieSession.setGlobal("genreSpecificRuleCohortList", genreSpecificRuleCohortList);
                kieSession.setGlobal("deviceDependentProductConditions", productDeviceConditions);
                kieSession.setGlobal("deviceBasedRulesCohortList", deviceBasedRulesCohortList);
                kieSession.setGlobal("standaloneAddonsProductCodes", standaloneAddonsProductCodes);
                kieSession.setGlobal("reverseMapForSvod", reverseMapForSvod);
                kieSession.setGlobal("getGroupingByProductGrpMap", groupingByProductGrp);
                kieSession.setGlobal("peacockProductsCondition", peacockProductsCondition);
                kieSession.setGlobal("mvpxSkipCombinationsMap", mvpxSkipCombinationsMap);
                kieSession.setGlobal("tempProductCodes", new ArrayList<>());
                kieSession.setGlobal("optimoMasRepackagingEnabled", featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_OPTIMOMAS_REPACKAGING_ENABLED));
                kieSession.setGlobal("repackagedChannelsMap", repackagedChannelsMap);
                kieSession.setGlobal("offerOrProductToRemoveList", offerOrProductToRemoveList);
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
                kieSession.insert(couponOffersRequest);
                kieSession.fireAllRules();
                kieSession.dispose();
                log.info("validateCartRulesExecutions() - completed & timetaken [{}] : {}", System.currentTimeMillis() - startTimeInMillis);
            } catch (Exception e) {
                log.error("Exception in validateCartRulesExecutions() : ", e);
                throw new ServiceException(ErrorMessages.EXCEPTION_IN_DROOLS_ENGINE, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Exception in validateCartRulesExecutions() : ", e);
            if (e instanceof ServiceException) {
                throw (ServiceException) e;
            }
            throw new ServiceException(ErrorMessages.EPOCH_VALIDATECART_SERVICES_EXECUTION_FAILURE, e.getMessage());
        }
        return ruleExecutionResultMap;
    }


}
