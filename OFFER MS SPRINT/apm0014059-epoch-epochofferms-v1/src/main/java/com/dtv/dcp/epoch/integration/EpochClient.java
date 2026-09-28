package com.dtv.dcp.epoch.integration;


import com.commercetools.graphql.api.types.Product;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.IncludedProduct;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.apache.commons.collections.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("EpochClient")
public class EpochClient {

    /**
     * The log.
     */
    private static final Logger log = LoggerFactory.getLogger(EpochClient.class);

    @Autowired
    CpopBackUpClient epochBackUpClient;

    @Autowired
    CpopPrimaryClient epochPrimaryClient;

    @Autowired
    CpopPVTClient epochPVTClient;

    @Autowired
    CpopClientHelper epochClientHelper;
    @Autowired
    OffersUtils offersUtils;
    @Value("${dcp.config.env}")
    private String idpConfigEnv;
    @Autowired
    private FeatureManagerHelper featureManagerHelper;

    public CTOfferResponse getOffers(CTOfferRequest ctOfferRequest) {
        CTOfferResponse ctResponse = null;
        log.info("Call getOffersFromCache");
        ctResponse = getOffersFromCache(ctOfferRequest, null);
        return ctResponse;
    }

    public CTOfferResponse getOffersFromCache(CTOfferRequest ctOfferRequest, List<String> requestedAttributesToFetch) {
        CTOfferRequest newCTtOfferRequest = new CTOfferRequest();
        BeanUtils.copyProperties(ctOfferRequest, newCTtOfferRequest);

        boolean isGetOffersSatellite = isGetOffersSatellite(ctOfferRequest);
        boolean isRewardRequestWithZipNDMA = validateRewardRequestWithZipNDMA(ctOfferRequest);

        if (!isZipCodeAvailable(ctOfferRequest) && !isGetOffersSatellite && !isRewardRequestWithZipNDMA) {
            ctOfferRequest.setCustomerEligibility(null);
        }
        resetSuppressOfferFields(ctOfferRequest);

        long startTimeMillis = System.currentTimeMillis();
        CTOfferRequest newCTOfferRequest = epochClientHelper.cleanCTOfferRequest(ctOfferRequest);
        newCTOfferRequest.setBundleProducts(ctOfferRequest.getBundleProducts());

        processCartOffers(newCTOfferRequest);

        log.info("DEBUG ORIGINAL EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(newCTOfferRequest));
        String graphQlRequestString = getGraphQLRequestString(newCTOfferRequest, startTimeMillis);

        log.info("DEBUG GRAPHQL EPOCH_GETOFFERS_CT_REQUEST-[{}]", JsonService.getJsonFromObject(graphQlRequestString));
        if (CollectionUtils.isNotEmpty(requestedAttributesToFetch)) {
            return executeGraphQLQueryForRequestedAttributes(graphQlRequestString, requestedAttributesToFetch);
        }
        return executeGraphQLQuery(graphQlRequestString);
    }

    private boolean isGetOffersSatellite(CTOfferRequest ctOfferRequest) {
        return CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
                && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
                && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
                && ctOfferRequest.getOfferProductFamily().contains(Constants.SATELLITE_PRODUCT_FAMILY);
    }

    private void resetSuppressOfferFields(CTOfferRequest ctOfferRequest) {
        ctOfferRequest.setBenefitsCodesToSuppressOffer(null);
        ctOfferRequest.setBenefitCodesToSuppressTheOffer(null);
    }

    private String getGraphQLRequestString(CTOfferRequest newCTOfferRequest, long startTimeMillis) {
        long startTimeMillis3 = System.currentTimeMillis();
        String graphQlRequestString = epochClientHelper.getGraphQLRequest(newCTOfferRequest);
        log.info("TOTAL_TIME_TAKEN_to get graphQlRequestString-[{}]", System.currentTimeMillis() - startTimeMillis3);
        return graphQlRequestString;
    }

    private CTOfferResponse executeGraphQLQuery(String graphQlRequestString) {
        CTOfferResponse graphqlProduct = null;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT executeGraphQLQuery Call ");
            long startTimeMillispvt = System.currentTimeMillis();
            graphqlProduct = epochPVTClient.executeGraphQLQuery(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_to get graphqlProducts-[{}]", System.currentTimeMillis() - startTimeMillispvt);
        } else {
            graphqlProduct = executeGraphQLQueryBasedOnFeatureToggle(graphQlRequestString);
        }
        return graphqlProduct;
    }

    private CTOfferResponse executeGraphQLQueryBasedOnFeatureToggle(String graphQlRequestString) {
        CTOfferResponse graphqlProduct;
        if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
            log.info("BackUp executeGraphQLQuery Call ");
            long startTimeMillisbackup = System.currentTimeMillis();
            graphqlProduct = epochBackUpClient.executeGraphQLQuery(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisbackup);
        } else {
            log.info("Primary executeGraphQLQuery Call ");
            long startTimeMillisprimary = System.currentTimeMillis();
            graphqlProduct = epochPrimaryClient.executeGraphQLQuery(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisprimary);
        }
        return graphqlProduct;
    }

    private void processCartOffers(CTOfferRequest newCTOfferRequest) {
        if (CollectionUtils.isNotEmpty(newCTOfferRequest.getCartOffers())) {
            if (CollectionUtils.isNotEmpty(newCTOfferRequest.getOfferProductType()) && !newCTOfferRequest.getOfferProductType().contains("fee")) {
                newCTOfferRequest.getOfferProductType().add("fee");
            } else if (CollectionUtils.isEmpty(newCTOfferRequest.getOfferProductType())) {
                List<String> offerProductTypes = new ArrayList<>();
                offerProductTypes.add("fee");
                newCTOfferRequest.setOfferProductType(offerProductTypes);
            }
        }
    }

    private boolean validateRewardRequestWithZipNDMA(CTOfferRequest ctOfferRequest) {
        return CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
                && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType())
                && ctOfferRequest.getOfferProductType().contains(Constants.REWARD)
                && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductFamily())
                && ctOfferRequest.getOfferProductFamily().contains(Constants.OTT_PRODUCT_FAMILY)
                && Objects.nonNull(ctOfferRequest.getCustomerEligibility())
                && (CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getZipCode())
                || CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getDma()));
    }

    public CTProductResponse getProductsByTypeFromCache(String ctReqString) {
        CTProductResponse ctProductResponse = null;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT getProductsByTypeFromCache Call ");
            long startTimeMillis = System.currentTimeMillis();
            ctProductResponse = epochPVTClient.getProductsByType(ctReqString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.info("BackUp getProductsByTypeFromCache Call ");
                long startTimeMillis = System.currentTimeMillis();
                ctProductResponse = epochBackUpClient.getProductsByType(ctReqString, idpConfigEnv);
                log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
            } else {
                log.info("Primary getProductsByTypeFromCache Call ");
                long startTimeMillis = System.currentTimeMillis();
                ctProductResponse = epochPrimaryClient.getProductsByType(ctReqString, idpConfigEnv);
                log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
            }
        }
        return ctProductResponse;

    }

    public CTProductResponse getProductsByTypeFromCT(String productType) {
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT getProductsByType Call ");
            return epochPVTClient.getProductsByType(productType);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.info("BackUp getProductsByType Call ");
                return epochBackUpClient.getProductsByType(productType);
            } else {
                log.info("Primary getProductsByType Call ");
                return epochPrimaryClient.getProductsByType(productType);
            }
        }
    }

    public CTProductResponse getProductsByType(String productType) {
        CTProductResponse ctResponse = null;
        if (featureManagerHelper.isEnabled(Constants.SVC_PRODUCTS_CACHE_FLAG)) {
            log.info("Call getProductsByTypeFromCache");
            ctResponse = getProductsByTypeFromCache(productType);
        } else {
            log.info("Call getProductsByTypeFromCT");
            ctResponse = getProductsByTypeFromCT(productType);
        }
        return ctResponse;

    }

    /**
     * @param ctOfferRequest the request containing the offer details
     * @return a boolean indicating if the zip code is available
     */
    private boolean isZipCodeAvailable(CTOfferRequest ctOfferRequest) {
        boolean isEligible = false;
        List<String> zipCodeList = offersUtils.fetchCTZipcodes();

        if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_ZIP_SALES_OFFER) && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferActionType())
                && ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
                && CollectionUtils.isNotEmpty(ctOfferRequest.getSalesChannel()) && ctOfferRequest.getSalesChannel().stream().anyMatch(OffersUtils::checkOnlineRelatedChannel)
                && CollectionUtils.isNotEmpty(ctOfferRequest.getOfferProductType()) && ctOfferRequest.getOfferProductType().contains(Constants.VIDEO_PLAN)
                && Optional.ofNullable(ctOfferRequest.getCustomerEligibility()).isPresent()
                && CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerEligibility().getZipCode()) && CollectionUtils.isNotEmpty(zipCodeList)
                && new HashSet<>(zipCodeList).containsAll(ctOfferRequest.getCustomerEligibility().getZipCode())) {
            isEligible = true;
        }
        return isEligible;
    }

    public Map<String, String> loadProductTypeAttrs(String productType) {
        Map<String, String> productTypeAttrs;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("started epochPVTClient.loadProductTypeAttrs..");
            return epochPVTClient.loadProductTypeAttrs(productType, idpConfigEnv);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.debug("started epochBackUpClient.loadProductTypeAttrs...");
                productTypeAttrs = epochBackUpClient.loadProductTypeAttrs(productType, idpConfigEnv);
            } else {
                log.debug("started epochPrimaryClient.loadProductTypeAttrs..");
                productTypeAttrs = epochPrimaryClient.loadProductTypeAttrs(productType, idpConfigEnv);
            }
        }
        return productTypeAttrs;
    }

    private CTOfferResponse executeGraphQLQueryForRequestedAttributes(String graphQlRequestString, List<String> requestedAttributesToFetch) {
        CTOfferResponse graphqlProduct = null;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT executeGraphQLQueryForRequestedAttributes Call ");
            long startTimeMillispvt = System.currentTimeMillis();
            graphqlProduct = epochPVTClient.executeGraphQLQueryForRequestedAttributes(graphQlRequestString, requestedAttributesToFetch);
            log.info("TOTAL_TIME_TAKEN_to get graphqlProducts-[{}]", System.currentTimeMillis() - startTimeMillispvt);
        } else {
            graphqlProduct = executeGraphQLQueryBasedOnFeatureToggleForRequestedAttributes(graphQlRequestString, requestedAttributesToFetch);
        }
        return graphqlProduct;
    }

    private CTOfferResponse executeGraphQLQueryBasedOnFeatureToggleForRequestedAttributes(String graphQlRequestString, List<String> requestedAttributesToFetch) {
        CTOfferResponse graphqlProduct;
        if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
            log.info("BackUp executeGraphQLQueryBasedOnFeatureToggleForRequestedAttributes Call ");
            long startTimeMillisbackup = System.currentTimeMillis();
            graphqlProduct = epochBackUpClient.executeGraphQLQueryForRequestedAttributes(graphQlRequestString, requestedAttributesToFetch);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisbackup);
        } else {
            log.info("Primary executeGraphQLQueryBasedOnFeatureToggleForRequestedAttributes Call ");
            long startTimeMillisprimary = System.currentTimeMillis();
            graphqlProduct = epochPrimaryClient.executeGraphQLQueryForRequestedAttributes(graphQlRequestString, requestedAttributesToFetch);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisprimary);
        }
        return graphqlProduct;
    }

    public Map<String, CTOffer> populateOTTOffersFromCache(CTOfferRequest ctOfferRequest) {
        long startTimeMillis = System.currentTimeMillis();
        CTOfferRequest request = getCtOfferRequest(ctOfferRequest);
        String flowType = ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION) || ctOfferRequest.getOfferActionType().contains(Constants.UPSELL) ? "Sales" : "Services";
        String cacheKey = Constants.OTT_PRODUCT_FAMILY + "_" + flowType + "_" + ctOfferRequest.getSalesChannel().get(0) + "_" + ctOfferRequest.getContractIndicator().get(0);
        Map<String, CTOffer> ctOffersFromCache = null;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT Call populateOffersFromCache ");
            ctOffersFromCache = epochPVTClient.getOfferDataFromCache(request, idpConfigEnv, cacheKey);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PVT_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
        } else {
            if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                log.info("BackUp getOffersFromCache Call ");
                long startTimeMillisbackup = System.currentTimeMillis();
                ctOffersFromCache = epochBackUpClient.getOfferDataFromCache(request, idpConfigEnv, cacheKey);
                log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisbackup);
            } else {
                log.info("Primary getOffersFromCache Call ");
                long startTimeMillisprimary = System.currentTimeMillis();
                ctOffersFromCache = epochPrimaryClient.getOfferDataFromCache(request, idpConfigEnv, cacheKey);
                log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisprimary);
            }
        }
        log.info("TOTAL_TIME_TAKEN_getOfferDataFromCache-[{}]", System.currentTimeMillis() - startTimeMillis);
        return ctOffersFromCache;
    }

    @NotNull
    private static CTOfferRequest getCtOfferRequest(CTOfferRequest ctOfferRequest) {
        CTOfferRequest offerRequest = new CTOfferRequest();
        offerRequest.setOfferProductFamily(Arrays.asList(Constants.OTT_PRODUCT_FAMILY));
        offerRequest.setSalesChannel(ctOfferRequest.getSalesChannel());
        offerRequest.setOfferActionType(ctOfferRequest.getOfferActionType());
        offerRequest.setContractIndicator(ctOfferRequest.getContractIndicator());
        offerRequest.setExpandProductRefs(true);
        offerRequest.setExpiredOffers(true);
        Pagination pagination = new Pagination();
        pagination.setLimit(100);
        pagination.setPage(1);
        offerRequest.setPagination(pagination);
        return offerRequest;
    }

    private void populateOffers(CTOfferRequest ctOfferRequest, CTOfferResponse cpopOffersResponse,
                                List<String> offerIds) {
        Map<String, CTOffer> ctOffersFromCache = populateOTTOffersFromCache(ctOfferRequest);

        long startTimeMillis5 = System.currentTimeMillis();
        Set<CTOffer> ctOfferList = offerIds.stream()
                .map(ctOffersFromCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Find and print offerIds not present in the cache
        List<String> notPresentInCache = offerIds.stream()
                .filter(offerId -> !ctOffersFromCache.containsKey(offerId))
                .collect(Collectors.toList());
        logMissingOffers(notPresentInCache);

        Set<String> includedOffersIds = ctOfferList.stream()
                .filter(finalOffer -> finalOffer.getAttributes() != null && finalOffer.getAttributes().getIncludedOffers() != null
                        && CollectionUtils.isNotEmpty(ctOfferRequest.getCartOffers()) && ctOfferRequest.getCartOffers().contains(finalOffer.getCode()))
                .flatMap(finalOffer -> finalOffer.getAttributes().getIncludedOffers().stream())
                .map(IncludedProduct::getId)
                .collect(Collectors.toSet());
        Set<CTOffer> includedOffers = includedOffersIds.stream()
                .map(ctOffersFromCache::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        ctOfferList.addAll(includedOffers);

        // Find and print offerIds not present in the cache
        List<String> includedOffersIdsNotPresentInCache = includedOffersIds.stream()
                .filter(offerId -> !ctOffersFromCache.containsKey(offerId))
                .collect(Collectors.toList());
        logMissingOffers(includedOffersIdsNotPresentInCache);

        log.info("time taken for streaming ctOfferList-[{}]", System.currentTimeMillis() - startTimeMillis5);
        cpopOffersResponse.setOffers(new ArrayList<>(ctOfferList));
        cpopOffersResponse.setTotal(ctOfferList.size());

    }

    private void logMissingOffers(List<String> offers) {
        if (CollectionUtils.isEmpty(offers)) {
            log.info("Product not found at top level. PricePlanCode -[{}]", offers);
        }
    }

    private CTOfferResponse buildCTOfferResponse(CTOfferRequest ctOfferRequest, List<Product> graphqlProduct, long startTimeMillis) {
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        if (graphqlProduct != null) {
            long startTimeMillis1 = System.currentTimeMillis();
            List<String> offerIds = graphqlProduct.stream().map(Product::getId).collect(Collectors.toList());
            log.info("TOTAL_TIME_TAKEN_to get offerIds-[{}]", System.currentTimeMillis() - startTimeMillis1);
            populateOffers(ctOfferRequest, ctOfferResponse, offerIds);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_CACHE-[{}]", System.currentTimeMillis() - startTimeMillis);
        }
        return ctOfferResponse;
    }

    public CTOfferResponse getOffersFromCache(CTOfferRequest ctOfferRequest) {
        CTOfferRequest newCTtOfferRequest = new CTOfferRequest();
        BeanUtils.copyProperties(ctOfferRequest, newCTtOfferRequest);

        long startTimeMillis = System.currentTimeMillis();
        CTOfferRequest newCTOfferRequest = epochClientHelper.cleanCTOfferRequest(ctOfferRequest);

        processCartOffers(newCTOfferRequest);

        String graphQlRequestString = getGraphQLRequestString(newCTOfferRequest, startTimeMillis);
        List<Product> graphqlProduct = executeGraphQLQueryProductList(graphQlRequestString);

        return buildCTOfferResponse(ctOfferRequest, graphqlProduct, startTimeMillis);
    }

    private List<Product> executeGraphQLQueryProductList(String graphQlRequestString) {
        List<Product> graphqlProduct = null;
        if (epochClientHelper.isPVTEnabled()) {
            log.info("PVT getOffersFromCache Call ");
            long startTimeMillispvt = System.currentTimeMillis();
            graphqlProduct = epochPVTClient.executeGraphQLQueryProducts(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_to get graphqlProducts-[{}]", System.currentTimeMillis() - startTimeMillispvt);
        } else {
            graphqlProduct = executeGraphQLQueryProductsBasedOnFeatureToggle(graphQlRequestString);
        }
        return graphqlProduct;
    }

    private List<Product> executeGraphQLQueryProductsBasedOnFeatureToggle(String graphQlRequestString) {
        List<Product> graphqlProduct;
        if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
            log.info("BackUp getOffersFromCache Call ");
            long startTimeMillisbackup = System.currentTimeMillis();
            graphqlProduct = epochBackUpClient.executeGraphQLQueryProducts(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_BACKUP_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisbackup);
        } else {
            log.info("Primary getOffersFromCache Call ");
            long startTimeMillisprimary = System.currentTimeMillis();
            graphqlProduct = epochPrimaryClient.executeGraphQLQueryProducts(graphQlRequestString, idpConfigEnv);
            log.info("TOTAL_TIME_TAKEN_FROM_REDIS_PRIMARY_CACHE-[{}]", System.currentTimeMillis() - startTimeMillisprimary);
        }
        return graphqlProduct;

    }

    public Map<String, Object> loadEligibilityGlobalConfigRules() {
        log.info("loadEligibilityGlobalConfigRules started");
        Map<String, Object> eligibilityRules = new HashMap<>();
        try {
            long startTimeMillis = System.currentTimeMillis();
            if (epochClientHelper != null && epochClientHelper.isPVTEnabled()) {
                eligibilityRules = executeEligibilityQuery(epochPVTClient, "PVT");
            } else {
                eligibilityRules = getEligibilityGlobalConfigRules();
            }
            log.info("TOTAL_TIME_TAKEN_FOR_LOAD_ELIGIBILITY_GLOBAL_CONFIG-[{}] ms", System.currentTimeMillis() - startTimeMillis);
        } catch (Exception e) {
            log.error("Exception occurred in loadEligibilityGlobalConfigRules: {}", e.getMessage(), e);
        }
        return eligibilityRules.isEmpty() ? null : eligibilityRules;
    }

    public Map<String, Object> getEligibilityGlobalConfigRules() {
        log.info("getEligibilityGlobalConfigRules started");
        Map<String, Object> eligibilityRules = new HashMap<>();
        try {
            long startTimeMillis = System.currentTimeMillis();
            if (featureManagerHelper != null && featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP)) {
                eligibilityRules = executeEligibilityQuery(epochBackUpClient, "Backup");
            } else {
                eligibilityRules = executeEligibilityQuery(epochPrimaryClient, "Primary");
            }
            log.info("TOTAL_TIME_TAKEN_FOR_GET_ELIGIBILITY_GLOBAL_CONFIG-[{}] ms", System.currentTimeMillis() - startTimeMillis);
        } catch (Exception e) {
            log.error("Exception occurred in getEligibilityGlobalConfigRules: {}", e.getMessage(), e);
        }
        return eligibilityRules.isEmpty() ? null : eligibilityRules;
    }

    private Map<String, Object> executeEligibilityQuery(Object client, String clientType) {
        Map<String, Object> result = new HashMap<>();
        if (client != null) {
            try {
                long startTimeMillis = System.currentTimeMillis();
                log.info("Executing Eligibility query using {} client", clientType);
                if (client instanceof CpopPVTClient) {
                    result = ((CpopPVTClient) client).executeEligibilityGraphQLQuery();
                } else if (client instanceof CpopBackUpClient) {
                    result = ((CpopBackUpClient) client).executeEligibilityGraphQLQuery();
                } else if (client instanceof CpopPrimaryClient) {
                    result = ((CpopPrimaryClient) client).executeEligibilityGraphQLQuery();
                }
                log.info("TOTAL_TIME_TAKEN_BY_{}_CLIENT-[{}] ms", clientType.toUpperCase(), System.currentTimeMillis() - startTimeMillis);
            } catch (Exception e) {
                log.error("Exception occurred while executing Eligibility query using {} client: {}", clientType, e.getMessage(), e);
            }
        } else {
            log.warn("{} client is null, skipping query execution.", clientType);
        }
        return result;
    }

}
