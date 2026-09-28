package com.dtv.dcp.epoch.integration;


import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.JsonService;
import com.dtv.dcp.epoch.util.OffersUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

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
        if (CollectionUtils.isNotEmpty(requestedAttributesToFetch)){
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
}
