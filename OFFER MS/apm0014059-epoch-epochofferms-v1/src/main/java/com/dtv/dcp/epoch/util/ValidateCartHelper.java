package com.dtv.dcp.epoch.util;


import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dtv.dcp.epoch.model.ct.product.Product;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CartContext;
import com.dtv.dcp.epoch.model.common.request.CartContexts;
import com.dtv.dcp.epoch.model.common.request.CartProduct;
import com.dtv.dcp.epoch.model.common.request.ChannelEligibility;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.CustomerPromotion;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidationResults;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTBenefitsRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTBenefitsResponse;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.ott.OttOffersService;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.product.Attributes;

@Component
public class ValidateCartHelper {
    @Autowired
    CTOfferRequestHelper ctOfferRequestHelper;
    @Autowired
    OttOffersService ottOffersService;
    @Autowired
    RedisCacheHelper redisCacheHelper;
    @Autowired
    CpopClient cpopClient;
    @Autowired
    CpopClientHelper cpopClientHelper;
    @Autowired
    FeatureManagerHelper featureManagerHelper;
    @Autowired
    private DMALookUpService dmaLookUpService;
    @Value("${apiclient.rest.cpopofferms.ctstate}")
    private String ctstate;
    @Value("${sportspack.lifetime.promo}")
    private String sportSpackLifetimePromo;
    List<String> MDUCustomerSegment = Arrays.asList(Constants.MDU, Constants.DECA, Constants.BCOMP, Constants.COURTESY,
            Constants.DEMO, Constants.SHOWROOM, Constants.MDUTENANT);

    private static Logger log = LoggerFactory.getLogger(ValidateCartHelper.class);

    public CTOfferResponse getOffersVideoPlanAcquisitionCall(CouponOffersRequest couponOffersRequest, HttpHeaders headers, List<String> requestCartVideoPlanOffers) {
        log.debug("getOffersVideoPlanAcquisitionCall() start");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            OfferRequest offersRequest = new OfferRequest();
            offersRequest.setOfferActionType(couponOffersRequest.getOfferActionType());
            offersRequest.setSalesChannel(Arrays.asList(couponOffersRequest.getSalesChannel()));
            offersRequest.setOfferProductFamily(Arrays.asList(couponOffersRequest.getOfferProductFamily()));
            offersRequest.setContractIndicator(couponOffersRequest.getContractIndicator());
            offersRequest.setMigrationIndicator(couponOffersRequest.isMigrationIndicator());
            offersRequest.setMigrationServiceType(couponOffersRequest.getMigrationServiceType());
            //Pass only video-plan product type. Do not change
            offersRequest.setOfferProductType(Arrays.asList(Constants.VIDEO_PLAN));
            if (null != couponOffersRequest.getReconnectCustomer()) {
                offersRequest.setReconnectCustomer(couponOffersRequest.getReconnectCustomer());
            }
            if (null != couponOffersRequest.getServiceEndDate()) {
                offersRequest.setServiceEndDate(couponOffersRequest.getServiceEndDate());
            }
            setCustomerContexts(couponOffersRequest, offersRequest);
            getChannelEligibility(couponOffersRequest, offersRequest, new ChannelEligibility());
            log.info("EPOCH_VALIDATE_CART_INTERNAL_GET_OFFERS_VIDEO_PLAN_ACQUISITION_CALL_REQUEST-[{}]", JsonService.getJsonFromObject(offersRequest));
            OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offersRequest);
            long startTimeInMillis = System.currentTimeMillis();
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VC_PERFORMANCE_ISSUE_FLAG))
            {
                offerRequestWrapper.setFlow("validateCart");
            }
            ctOfferResponse = ottOffersService.getOffers(offerRequestWrapper);
            long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
            log.info("ValidateCartHelper - getOffersVideoPlanAcquisitionCall() Time Taken for getOffers response -[{}]", (endTimeMillis));
        } catch (Exception e) {
            log.error("Exception occurred while calling getOffersVideoPlanAcquisitionCall(): ", e);
        }
        log.debug("getOffersVideoPlanAcquisitionCall() end");
        return ctOfferResponse;
    }

    public CTOfferResponse getOffersOfferCodesCalls(CouponOffersRequest couponOffersRequest, HttpHeaders headers, List<String> requestCartVideoPlanOffers) {
        log.debug("getOffersOfferCodesCalls() start");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            OfferRequest offersRequest = new OfferRequest();
            offersRequest.setCustomerSegments(Arrays.asList(couponOffersRequest.getCustomerSegments()));
            offersRequest.setOfferProductFamily(Arrays.asList(couponOffersRequest.getOfferProductFamily()));
            offersRequest.setOfferCodes(requestCartVideoPlanOffers);
            offersRequest.setSalesChannel(Arrays.asList(couponOffersRequest.getSalesChannel()));
            offersRequest.setContractIndicator(couponOffersRequest.getContractIndicator());
            offersRequest.setCartOffers(couponOffersRequest.getCartContext().getCartOffers());
            offersRequest.setMigrationIndicator(couponOffersRequest.isMigrationIndicator());
            offersRequest.setMigrationServiceType(couponOffersRequest.getMigrationServiceType());
            if (null != couponOffersRequest.getOnlinePartnerDetails()) {
                offersRequest.setOnlinePartnerDetails(couponOffersRequest.getOnlinePartnerDetails());
            }
            if (null != couponOffersRequest.getReconnectCustomer()) {
                offersRequest.setReconnectCustomer(couponOffersRequest.getReconnectCustomer());
            }
            if (null != couponOffersRequest.getServiceEndDate()) {
                offersRequest.setServiceEndDate(couponOffersRequest.getServiceEndDate());
            }
            setCustomerContexts(couponOffersRequest, offersRequest);
            getChannelEligibility(couponOffersRequest, offersRequest, new ChannelEligibility());
            log.info("EPOCH_VALIDATE_CART_INTERNAL_GET_OFFERS_OFFER_CODES_CALL_REQUEST-[{}]", JsonService.getJsonFromObject(offersRequest));
            OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offersRequest);
            long startTimeInMillis = System.currentTimeMillis();
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_DELAY_PROVISIONING_ENABLED)) {
            	// Skip the GraphQL call for Delay Provisioning flow.
            	 ctOfferResponse = ottOffersService.getOffers(offerRequestWrapper);
            }else {
            	 if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VC_PERFORMANCE_ISSUE_FLAG))
                 {
                     offerRequestWrapper.setFlow("validateCart");
                 }
            	 ctOfferResponse = ottOffersService.getOffers(offerRequestWrapper);
            }
           
            long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
            log.info("ValidateCartHelper - getOffersOfferCodesCalls() Time Taken for getOffers response -[{}]", (endTimeMillis));
        } catch (Exception e) {
            log.error("Exception occurred while calling getOffersOfferCodesCalls(): ", e);
        }
        log.debug("getOffersOfferCodesCalls() end");
        return ctOfferResponse;
    }

    public CTOfferResponse getOffersCartCallForSales(CouponOffersRequest couponOffersRequest, List<String> cpopOfferCodes, HttpHeaders headers, List<String> bundeProductIds) {
        log.debug("getOffersCartCallForSales() start");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try {
            OfferRequest offersRequest = new OfferRequest();
            CartContext cartContext = new CartContext();
            offersRequest.setOfferActionType(couponOffersRequest.getOfferActionType());
            offersRequest.setSalesChannel(Arrays.asList(couponOffersRequest.getSalesChannel()));
            offersRequest.setOfferProductFamily(Arrays.asList(couponOffersRequest.getOfferProductFamily()));
            offersRequest.setContractIndicator(couponOffersRequest.getContractIndicator());
            offersRequest.setMarketingSourceCode(couponOffersRequest.getMarketingSourceCode());
            offersRequest.setMigrationIndicator(couponOffersRequest.isMigrationIndicator());
            offersRequest.setMigrationServiceType(couponOffersRequest.getMigrationServiceType());
            if (null != couponOffersRequest.getOnlinePartnerDetails()) {
                offersRequest.setOnlinePartnerDetails(couponOffersRequest.getOnlinePartnerDetails());
            }
            List<String> validateCartOfferProductTypes = new ArrayList<>();
            if (couponOffersRequest.getContractIndicator().stream().anyMatch(indicator -> indicator.equalsIgnoreCase(Constants.GENRE))) {
                validateCartOfferProductTypes = Optional.ofNullable(redisCacheHelper.getValues(Constants.REDIS_CACHE_GENRE_VALIDATE_CART_OFFER_PRODUCT_TYPES, Constants.OTT))
                        .filter(values -> !values.isEmpty())
                        .map(values -> values.get(0))
                        .map(value -> value.split("\\s*,\\s*"))
                        .map(Arrays::asList)
                        .orElseGet(() -> {
                            return List.of();
                        });
            } else if (couponOffersRequest.getContractIndicator().stream().anyMatch(indicator -> indicator.equalsIgnoreCase(Constants.TAZBYOD_STRING))) {
                //validateCartOfferProductTypes = Arrays.asList("video-addon", "protection-plan", "campaign");
                validateCartOfferProductTypes = Optional.ofNullable(redisCacheHelper.getValues(Constants.REDIS_CACHE_TAZBYOD_VALIDATE_CART_OFFER_PRODUCT_TYPES, Constants.OTT))
                        .filter(values -> !values.isEmpty())
                        .map(values -> values.get(0))
                        .map(value -> value.split("\\s*,\\s*"))
                        .map(Arrays::asList)
                        .orElseGet(() -> {
                            return List.of();
                        });
                } else {
                    validateCartOfferProductTypes = Optional.ofNullable(redisCacheHelper.getValues(Constants.REDIS_CACHE_VALIDATE_CART_OFFER_PRODUCT_TYPES, Constants.OTT))
                            .filter(values -> !values.isEmpty())
                            .map(values -> values.get(0))
                            .map(value -> value.split("\\s*,\\s*"))
                            .map(Arrays::asList)
                            .orElseGet(() -> {
                                return List.of();
                            });
                }
            offersRequest.setBundleProductIds(bundeProductIds);
            offersRequest.setOfferProductType(validateCartOfferProductTypes);
            cartContext.setCpopOfferCodes(cpopOfferCodes);
            offersRequest.setCartContext(cartContext);
            offersRequest.setServiceEndDate(couponOffersRequest.getServiceEndDate());
            if (null != couponOffersRequest.getReconnectCustomer()) {
                offersRequest.setReconnectCustomer(couponOffersRequest.getReconnectCustomer());
            }
            offersRequest.setBusinessSegment(Arrays.asList(couponOffersRequest.getBusinessSegment()));
            if(StringUtils.isNotEmpty(couponOffersRequest.getCustomerSegments()) && Constants.EMPLOYEE.equalsIgnoreCase(couponOffersRequest.getCustomerSegments()) ){
                offersRequest.setCustomerSegments(Arrays.asList(couponOffersRequest.getCustomerSegments()));
            }
            getChannelEligibility(couponOffersRequest, offersRequest, new ChannelEligibility());
            getCustomerEligibility(couponOffersRequest, offersRequest, new CustomerEligibility());
            setCustomerContexts(couponOffersRequest, offersRequest);
            offersRequest.setCartOffers(couponOffersRequest.getCartContext().getCartOffers());
            Optional.ofNullable(couponOffersRequest.getTreatmentcode()).ifPresent(offersRequest::setTreatmentCode);
            Optional.ofNullable(couponOffersRequest.getCreditRisk()).ifPresent(offersRequest::setCreditRisk);
            log.info("EPOCH_VALIDATE_CART_INTERNAL_GET_OFFERS_CART_CALL_REQUEST-[{}]", JsonService.getJsonFromObject(offersRequest));
            OfferRequestWrapper offerRequestWrapper = ctOfferRequestHelper.preProcessOfferRequest(headers, offersRequest);
            long startTimeInMillis = System.currentTimeMillis();
            if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VC_PERFORMANCE_ISSUE_FLAG))
            {
                offerRequestWrapper.setFlow("validateCart");
            }
            ctOfferResponse = ottOffersService.getOffers(offerRequestWrapper);
            long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
            log.info("ValidateCartHelper - getOffersCartCallForSales() Time Taken for getOffers response -[{}]", (endTimeMillis));
        } catch (Exception e) {
            log.error("Exception occurred while calling getOffersCartCallForSales: ", e);
        }
        log.debug("getOffersCartCallForSales() end");
        return ctOfferResponse;
    }
    private static void getCustomerEligibility(CouponOffersRequest couponOffersRequest, OfferRequest offersRequest, CustomerEligibility customerEligibility) {
        log.debug("getCustomerEligibility() start");
        try{
            if(Optional.ofNullable(couponOffersRequest.getCustomerAddress()).isPresent() && Optional.ofNullable(couponOffersRequest.getCustomerAddress().getZipCode()).isPresent()){
                customerEligibility.setZipCode(Arrays.asList(couponOffersRequest.getCustomerAddress().getZipCode()));
            }
            if(Optional.ofNullable(couponOffersRequest.getCustomerAddress()).isPresent() && Optional.ofNullable(couponOffersRequest.getCustomerAddress().getFipsCode()).isPresent()){
                customerEligibility.setFipsCode(Arrays.asList(couponOffersRequest.getCustomerAddress().getFipsCode()));
            }
            if(Objects.nonNull(customerEligibility)){
                offersRequest.setCustomerEligibility(customerEligibility);
            }
        }catch (Exception e){
            log.error("Exception occurred while calling getCustomerEligibility: ", e);
        }
        log.debug("getCustomerEligibility() end");
    }

    private static void getChannelEligibility(CouponOffersRequest couponOffersRequest, OfferRequest offersRequest, ChannelEligibility channelEligibility) {
        log.debug("getChannelEligibility() start");
        Optional.ofNullable(couponOffersRequest.getChannelEligibility()).ifPresent(channelEligibilityRequest -> {
            Optional.ofNullable(channelEligibilityRequest.getOpusChannel()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setOpusChannel);
            Optional.ofNullable(channelEligibilityRequest.getOpusSubChannel()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setOpusSubChannel);
            Optional.ofNullable(channelEligibilityRequest.getOpusStoreId()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setOpusStoreId);
            Optional.ofNullable(channelEligibilityRequest.getSalesChannel()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setSalesChannel);
            Optional.ofNullable(channelEligibilityRequest.getSalesSubChannel()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setSalesSubChannel);
            Optional.ofNullable(channelEligibilityRequest.getLocationId()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setLocationId);
            Optional.ofNullable(channelEligibilityRequest.getLocationTypeId()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setLocationTypeId);
            Optional.ofNullable(channelEligibilityRequest.getDealerCode()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setDealerCode);
            Optional.ofNullable(channelEligibilityRequest.getDirectIntegrationPartnerName()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setDirectIntegrationPartnerName);
            Optional.ofNullable(channelEligibilityRequest.getIsFulfillmentDealer()).ifPresent(channelEligibility::setIsFulfillmentDealer);
            Optional.ofNullable(channelEligibilityRequest.getMasterDealerId()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setMasterDealerId);
            Optional.ofNullable(channelEligibilityRequest.getDealerId()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setDealerId);
            Optional.ofNullable(channelEligibilityRequest.getSpecialPage()).filter(StringUtils::isNotEmpty).ifPresent(channelEligibility::setSpecialPage);
            offersRequest.setChannelEligibility(channelEligibility);
        });
        log.debug("getChannelEligibility() end");
    }

    @NotNull
    public static List<String> filterOfferCodesWithPreselectDesignationMandatory(List<CTOffer> offers) {
        return offers.stream().filter(Objects::nonNull).filter(offer -> StringUtils.isNotEmpty(offer.getAttributes().getOfferPreselectDesignation())).
                filter(offer -> offer.getAttributes().getOfferPreselectDesignation().equalsIgnoreCase(Constants.MANDATORY))
                .map(CTOffer::getCode).collect(Collectors.toList());
    }

    public static List<String> filterOfferCodesWithDisplayTypeIncluded(@NotNull List<CTOffer> offers) {
        log.debug("inside filterOfferCodesWithDisplayTypeIncluded()");
        return offers.stream()
                .filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .filter(offer -> offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                        .anyMatch(associatedProduct -> CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) &&
                                associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                        .anyMatch(bundleProduct -> CollectionUtils.isNotEmpty(bundleProduct.getProducts()) &&
                                                bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                        .anyMatch(product -> Objects.nonNull(product.getObj()) &&
                                                                CollectionUtils.isNotEmpty(product.getObj().getVariants()) &&
                                                                product.getObj().getVariants().stream().filter(Objects::nonNull)
                                                                        .anyMatch(variant -> Objects.nonNull(variant.getAttributes()) &&
                                                                                StringUtils.isNotEmpty(variant.getAttributes().getDisplayType()) &&
                                                                                StringUtils.equalsIgnoreCase(Constants.INCLUDED, variant.getAttributes().getDisplayType()))))))
                .map(CTOffer::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> filterOfferCodesWithFeeType(List<CTOffer> offers) {
        log.debug("inside filterOfferCodesWithFeeType() start");
        return offers.stream()
                .filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .filter(offer -> offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                        .anyMatch(associatedProduct -> CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) &&
                                associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                        .anyMatch(bundleProduct -> CollectionUtils.isNotEmpty(bundleProduct.getProducts()) &&
                                                bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                        .anyMatch(product -> Objects.nonNull(product.getObj()) &&
                                                                CollectionUtils.isNotEmpty(product.getObj().getVariants()) &&
                                                                product.getObj().getVariants().stream().filter(Objects::nonNull)
                                                                        .anyMatch(variant -> Objects.nonNull(variant.getAttributes()) &&
                                                                                StringUtils.isNotEmpty(variant.getAttributes().getFeeType()) &&
                                                                                (StringUtils.equalsIgnoreCase(Constants.ARS_FEE, variant.getAttributes().getFeeType()))
                                                                                || StringUtils.equalsIgnoreCase(Constants.RSNFEE, variant.getAttributes().getFeeType())
                                                                                || StringUtils.equalsIgnoreCase(Constants.DO_FEE, variant.getAttributes().getFeeType()))))))
                .map(CTOffer::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public static List<String> addCreditOfferIfPresent(List<CTOffer> offers) {
        return Optional.ofNullable(offers).orElseGet(Collections::emptyList).stream()
                .filter(Objects::nonNull)
                .filter(offer -> CollectionUtils.isNotEmpty(offer.getAttributes().getOfferProductTypes()) && offer.getAttributes().getOfferProductTypes().stream()
                        .anyMatch(productType -> StringUtils.equalsIgnoreCase(productType, Constants.CREDIT)))
                .map(CTOffer::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean isAcquisitionOfferActionType(List<String> offerActionType) {
        return offerActionType.stream()
                .anyMatch(type -> type.equalsIgnoreCase(Constants.ACQUISITION) || type.equalsIgnoreCase(Constants.UPSELL_ACTION_TYPE));
    }

    public List<String> complianceKeyOtherThenSPKLTP(CouponOffersRequest couponOffersRequest) {
        String promoId = null;
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartProducts())) {
            promoId = couponOffersRequest.getCartContext().getCartProducts().stream().
                    filter(Objects::nonNull)
                    .filter(cartProduct -> CollectionUtils.isNotEmpty(cartProduct.getPromotions()))
                    .flatMap(cartProduct -> cartProduct.getPromotions().stream()
                            .filter(Objects::nonNull)
                            .filter(promotion -> promotion.getPromotionId() != null)
                            .filter(promotion -> !promotion.getPromotionId().contains(sportSpackLifetimePromo))
                    ).map(promotion -> promotion.getPromotionId()).findFirst().orElse(null);


            if (org.apache.commons.lang.StringUtils.isNotEmpty(promoId)) {
                CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
                ctBenefitsRequest.setBenefitCodes(Arrays.asList(promoId));
                ctBenefitsRequest.setState(ctstate);
                CTBenefitsResponse ctBenefitsResponse = cpopClient.getBenefitsFromCT(ctBenefitsRequest);
                return getComplianceProductKey(ctBenefitsResponse);
            }
        }
        return null;
    }

    public List<String> getCompatibleProductSKUs(CTProductResponse productResponse, String SKU) {
        return productResponse.getProducts().stream()
                .filter(product -> product.getVariants().stream().filter(Objects::nonNull)
                        .filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getCompatibleProducts()))
                        .anyMatch(variant -> variant.getAttributes().getCompatibleProducts().stream().filter(Objects::nonNull)
                                .anyMatch(compatibleProduct -> compatibleProduct.getProducts().stream().filter(Objects::nonNull)
                                        .anyMatch(product1 -> StringUtils.isNotEmpty(product1.getKey()) && product1.getKey().contains(SKU)))))
                .map(product -> product.getCode())
                .collect(Collectors.toList());
    }

    public List<String> getComplianceProductKey(CTBenefitsResponse ctBenefitsResponse) {
        return ctBenefitsResponse.getBenefits().stream()
                .filter(Objects::nonNull)
                .flatMap(b -> b.getCompliance() != null ? b.getCompliance().getConstraints().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .flatMap(constraint -> constraint.getComplianceProducts() != null ? constraint.getComplianceProducts().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .flatMap(complianceProduct -> complianceProduct.getProducts() != null ? complianceProduct.getProducts().stream() : Stream.empty())
                .filter(Objects::nonNull)
                .map(product -> product.getKey() != null ? product.getKey() : null)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a map of embedded SVOD products from the given offer response based on the specified offer action types.
     *
     * @param getOfferResponse the response containing the offers
     * @param couponOffersRequest
     * @return a map where the key is the offer code and the value is the first embedded SVOD product key
     */
    public  Map<String, List<String>> getEmbeddedSVODProductsFromGetOffers(CTOfferResponse getOfferResponse, CouponOffersRequest couponOffersRequest,CTProductResponse productResponse) {
        log.info("getEmbeddedSVODProductsFromGetOffers() - Started");

        Map<String, List<String>> map = new HashMap<>();
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> getKeyFromID = getMapOfProductsKeyFromId(productResponse);
        Map<String, List<String>> resultMaps = new HashMap<>();

        // Check if offerActionType contains "Upgrade", "Downgrade", "Cross-sell", or "Other"
        if(isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())){
            getOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                List<String> productKeyList = new ArrayList<>(); // Create a new list for each offer
                offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(
                        bundledProduct -> {
                            bundledProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                product.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                                            if (null != prod.getObj() && null != prod.getObj().getVariants()) {
                                                prod.getObj().getVariants().stream().filter(Objects::nonNull).forEach(mm -> {
                                                    if (null != mm.getAttributes().getIncludedProducts() && CollectionUtils.isNotEmpty(mm.getAttributes().getContractIndicator())
                                                            && mm.getAttributes().getContractIndicator().contains(couponOffersRequest.getContractIndicator().get(0))) {
                                                        mm.getAttributes().getIncludedProducts().stream().filter(Objects::nonNull).forEach(inc -> {
                                                                    inc.getProducts().stream().forEach(ff -> {
                                                                        productKeyList.add(getKeyFromID.get(ff.getId()));
                                                                    });
                                                                }
                                                        );
                                                        map.put(offer.getCode(), productKeyList);
                                                    }
                                                });
                                            }
                                        }
                                );
                            });
                        });
            });
        } else {
            productResponse.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                        List<String> productKeyList = new ArrayList<>();
                        prod.getVariants().stream().filter(Objects::nonNull).forEach(mm -> {
                            if (null != mm.getAttributes().getIncludedProducts() && CollectionUtils.isNotEmpty(mm.getAttributes().getContractIndicator())
                                    && mm.getAttributes().getContractIndicator().contains(couponOffersRequest.getContractIndicator().get(0))) {
                                mm.getAttributes().getIncludedProducts().stream().filter(Objects::nonNull).forEach(inc -> {
                                            inc.getProducts().stream().forEach(ff -> {
                                                productKeyList.add(getKeyFromID.get(ff.getId()));
                                            });
                                        }
                                );
                                map.put(prod.getCode(), productKeyList);
                            }
                        });
                    }
            );
        }

        List<String> embeddedSVODProductList = new ArrayList<>();

        productResponse.getProducts().stream()
                .filter(Objects::nonNull)
                .forEach(product -> product.getVariants().forEach(variant -> {
                    if ("EmbeddedSVOD".equalsIgnoreCase(variant.getAttributes().getSubCategory())) {
                        embeddedSVODProductList.add(product.getCode());
                    }
                }));

        log.debug("Embedded SVOD product list: {}", embeddedSVODProductList);

        if (!embeddedSVODProductList.isEmpty() && isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            // Filter offers that have a bundled product equal to a product in embeddedSVODProductList
            /* Compare base offer (OF_BASE-SPORTSMVP-202408_TAZBYOD_15OFF_3MO_FT) with addon offer(OF_BOLT-ESPNPLUS-202411_TAZBYOD)
            by matching embeddedSVODProductList BOLT-ESPNPLUS-202411. Check if both offers are coming in getOffers response */
            Map<String, List<String>> filteredMap = getOfferResponse.getOffers().stream()
                    .filter(offer -> offer.getAttributes().getAssociatedProducts().stream()
                            .flatMap(bundledProduct -> bundledProduct.getBundleProducts().stream())
                            .flatMap(product -> product.getProducts().stream())
                            .anyMatch(prod -> embeddedSVODProductList.contains(getKeyFromID.get(prod.getId()))))
                    .collect(Collectors.toMap(CTOffer::getCode, offer -> offer.getAttributes().getAssociatedProducts().stream()
                            .flatMap(bundledProduct -> bundledProduct.getBundleProducts().stream())
                            .flatMap(product -> product.getProducts().stream())
                            .map(prod -> getKeyFromID.get(prod.getId()))
                            .collect(Collectors.toList())));

            // Map filtered offers to resultMap
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                List<String> includedOffers = filteredMap.keySet().stream()
                        .filter(filteredKey -> filteredMap.get(filteredKey).stream().anyMatch(value::contains))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(includedOffers)) {
                    resultMaps.put(key, includedOffers);
                }
            }
        } else if (!embeddedSVODProductList.isEmpty()) {
            // Remove entries from map that do not contain any product in embeddedSVODProductList
            map.entrySet().removeIf(entry ->
                    entry.getValue().stream().noneMatch(embeddedSVODProductList::contains)
            );
            resultMaps = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        log.info("getEmbeddedSVODProductsFromGetOffers() - Completed with resultMap: {}", resultMap);
        return resultMaps;
    }

    public List<String> getOfferCodeWithCategoryGenreBase(@NotNull List<CTOffer> offers) {
        log.debug("inside getOfferCodeWithCategoryGenreBase()");
        return offers.stream()
                .filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .filter(offer -> offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                        .anyMatch(associatedProduct -> CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) &&
                                associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                        .anyMatch(bundleProduct -> CollectionUtils.isNotEmpty(bundleProduct.getProducts()) &&
                                                bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                        .anyMatch(product -> Objects.nonNull(product.getObj()) &&
                                                                CollectionUtils.isNotEmpty(product.getObj().getVariants()) &&
                                                                product.getObj().getVariants().stream().filter(Objects::nonNull)
                                                                        .anyMatch(variant -> Objects.nonNull(variant.getAttributes()) &&
                                                                                StringUtils.isNotEmpty(variant.getAttributes().getCategory()) &&
                                                                                StringUtils.equalsIgnoreCase(Constants.GENREBASE, variant.getAttributes().getCategory()))))))
                .map(CTOffer::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public void validateCartRequestValidation(CouponOffersRequest couponOffersRequest, boolean localsEnabledFlag, boolean isLocalsValidateCartRequest, boolean isSportsPackValidateCartRequest) {

        List<String> missingAttributes = new ArrayList<>();
        if (CollectionUtils.isEmpty(couponOffersRequest.getOfferActionType()) ||
                (CollectionUtils.isNotEmpty(couponOffersRequest.getOfferActionType())
                        && StringUtils.isEmpty(couponOffersRequest.getOfferActionType().get(0)))) {
            missingAttributes.add("OfferActionType");
        }

        if (StringUtils.isEmpty(couponOffersRequest.getSalesChannel())) {
            missingAttributes.add("SalesChannel");
        }

        if (StringUtils.isEmpty(couponOffersRequest.getOfferProductFamily())) {
            missingAttributes.add("OfferProductFamily");
        }

        if (StringUtils.isEmpty(couponOffersRequest.getCustomerSegments())) {
            missingAttributes.add("CustomerSegments");
        }

        if (CollectionUtils.isEmpty(couponOffersRequest.getContractIndicator())
                || (CollectionUtils.isNotEmpty(couponOffersRequest.getContractIndicator())
                && StringUtils.isEmpty(couponOffersRequest.getContractIndicator().get(0)))) {
            missingAttributes.add("ContractIndicator");
        }

        if(Objects.nonNull(couponOffersRequest.getCustomerContext()) && CollectionUtils.isEmpty(couponOffersRequest.getCustomerContext().getExistingProductFamily())){
            missingAttributes.add("ExistingProductFamily");
        }
        //cart request validation for locals flow missingAttributes
        List<String> validateCartVideoPlanOffers = Optional.ofNullable(couponOffersRequest.getCartContext())
                .map(CartContexts::getCartOffers)
                .orElse(Collections.emptyList())
                .stream()
                .filter(Objects::nonNull)
                .filter(cartOffer -> cartOffer.getOfferCode().contains("OF_BASE-"))
                //.filter(cartOffer -> Constants.VIDEO_PLAN.equalsIgnoreCase(cartOffer.getOfferProductType()))
                .map(CartOffer::getOfferCode)
                .collect(Collectors.toList());
        if (!isSportsPackValidateCartRequest && isLocalsValidateCartRequest) {
            if (couponOffersRequest.getCustomerAddress() == null) {
                missingAttributes.add("Customer Address");
            } else if (couponOffersRequest.getCustomerAddress().getZipCode() == null || couponOffersRequest.getCustomerAddress().getFipsCode() == null) {
                missingAttributes.add("Customer Address is missing Zipcode and/or FipsCode");
            } else if (couponOffersRequest.getPreviousCustomerAddress() != null &&
                    (couponOffersRequest.getPreviousCustomerAddress().getZipCode() == null || couponOffersRequest.getPreviousCustomerAddress().getFipsCode() == null)) {
                missingAttributes.add("Previous Customer Address is missing Zipcode and/or FipsCode");
            } else if ((CollectionUtils.isNotEmpty(couponOffersRequest.getOfferActionType())
                    && StringUtils.isNotEmpty(couponOffersRequest.getOfferActionType().get(0)))
                    && isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())
                    && CollectionUtils.isEmpty(validateCartVideoPlanOffers)) {
                missingAttributes.add("Base Package is missing");
            }
        } else if (!isSportsPackValidateCartRequest && (CollectionUtils.isNotEmpty(couponOffersRequest.getContractIndicator())
                && StringUtils.isNotEmpty(couponOffersRequest.getContractIndicator().get(0)))
                && !isContractIndicatorGENRE(couponOffersRequest)) {
            if (isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())
                    && CollectionUtils.isEmpty(validateCartVideoPlanOffers)) {
                missingAttributes.add("Base Package is missing");
            }
        }

        if (CollectionUtils.isNotEmpty(missingAttributes)) {
            throw new ServiceException(ErrorMessages.CTLG_VALISATECART_INVALID_REQUEST, missingAttributes.toString());
        }

    }

    public boolean checkForMDUFlow(CouponOffersRequest couponOffersRequest) {
        return StringUtils.isNotEmpty(couponOffersRequest.getCustomerSegments()) && StringUtils.isNotEmpty(couponOffersRequest.getBusinessSegment())
                && StringUtils.equalsIgnoreCase(Constants.MDU, couponOffersRequest.getBusinessSegment()) && MDUCustomerSegment.contains(couponOffersRequest.getCustomerSegments());
    }

    public boolean checkForValidLocalsContractIndicator(CouponOffersRequest couponOffersRequest) {
        return Optional.ofNullable(couponOffersRequest.getContractIndicator()).isPresent()
                && couponOffersRequest.getContractIndicator().stream()
                .noneMatch(indicator -> indicator.equalsIgnoreCase(Constants.GENRE) || indicator.equalsIgnoreCase(Constants.FRONTPORCH_STRING) || indicator.equalsIgnoreCase(Constants.TAZBYOD_STRING));
    }

    public boolean checkForNonMDUFlow(CouponOffersRequest couponOffersRequest) {
        return Optional.ofNullable(couponOffersRequest.getBusinessSegment()).isEmpty()
                || (!couponOffersRequest.getBusinessSegment().isEmpty() && !couponOffersRequest.getBusinessSegment().equalsIgnoreCase(Constants.MDU));
    }

    public ValidationResults createValidationResults(String status, String errorCode, String errorMessage) {
        ValidationResults validationResults = new ValidationResults();
        validationResults.setStatus(status);
        validationResults.setErrorCode(errorCode);
        validationResults.setErrorMessage(errorMessage);
        return validationResults;
    }

    public boolean checkValidateCartRequestIsSportsPack(CouponOffersRequest couponOffersRequest) {
        return Optional.ofNullable(couponOffersRequest)
                .filter(this::checkForMDUFlow)
                .filter(request -> !request.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
                .map(CouponOffersRequest::getCartContext)
                .filter(Objects::nonNull)
                .map(CartContexts::getCartProducts)
                .filter(CollectionUtils::isNotEmpty)
                .isPresent();
    }

    public boolean isContractIndicatorGENRE(CouponOffersRequest couponOffersRequest) {
        return Optional.ofNullable(couponOffersRequest)
                .map(CouponOffersRequest::getContractIndicator)
                .filter(CollectionUtils::isNotEmpty)
                .map(indicators -> indicators.stream()
                        .anyMatch(indicator -> Constants.GENRE.equalsIgnoreCase(indicator)))
                .orElse(false);
    }

    public boolean localsEnabled() {
        List<String> localFlagFromConfigs = redisCacheHelper.getValues(Constants.REDIS_CACHE_LOCALS_ENABLED_FLAG, Constants.OTT);
        return localFlagFromConfigs != null && !localFlagFromConfigs.isEmpty() && Boolean.parseBoolean(localFlagFromConfigs.get(0));
    }

    public boolean isValidSalesChannel(List<CTOffer> ctOffers, String salesChannelRequest) {
        if (ctOffers == null) {
            return false;
        }
        if (featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_VALIDATE_GRAPHQL)) {
            for (CTOffer offer : ctOffers) {
                if (null != offer.getAttributes() && null != offer.getAttributes().getEligibility()
                        && CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints())
                        && offer.getAttributes().getEligibility().getConstraints().stream().filter(Objects::nonNull)
                        .filter(constraint -> CollectionUtils.isNotEmpty(constraint.getSalesChannel()))
                        .flatMap(constraint -> constraint.getSalesChannel().stream())
                        .anyMatch(salesChannel -> salesChannel.equalsIgnoreCase(salesChannelRequest))) {
                    return true;
                }
            }
            return false;
        } else {
            return ctOffers.stream().filter(Objects::nonNull)
                    .filter(ctOffer -> ObjectUtils.allNotNull(ctOffer.getAttributes(),
                            ctOffer.getAttributes().getEligibility()))
                    .filter(ctOffer -> CollectionUtils
                            .isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints()))
                    .anyMatch(ctOffer -> ctOffer.getAttributes().getEligibility().getConstraints().stream()
                            .filter(Objects::nonNull)
                            .filter(constraint -> CollectionUtils.isNotEmpty(constraint.getSalesChannel()))
                            .flatMap(constraint -> constraint.getSalesChannel().stream())
                            .anyMatch(salesChannel -> salesChannel.equalsIgnoreCase(salesChannelRequest)));
        }

    }


    public Boolean isLocalsServedOrUnServed(String previousZipCode, String previousFipsCode) {
        Boolean isLocalsServed = dmaLookUpService.hasLocalChannels(previousZipCode, previousFipsCode);
        if(null!= isLocalsServed){
            return isLocalsServed;
        }else{
            return false;
        }
    }

    private Map<String, String> getMapOfProductsKeyFromId(CTProductResponse ctProductResponse) {
        if (ctProductResponse == null || ctProductResponse.getProducts() == null) {
            return new HashMap<>();
        }
        return ctProductResponse.getProducts().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ProductObj::getId, ProductObj::getCode));
    }
    

    public List<String> getOfferCodeWithBundleProductSubCategoryLocals(@NotNull List<CTOffer> offers) {
        log.debug("inside getOfferCodeWithBundleProductSubCategoryLocals()");
        return offers.stream()
                .filter(Objects::nonNull)
                .filter(offer -> Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                .filter(offer -> offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                        .anyMatch(associatedProduct -> CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) &&
                                associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                        .anyMatch(bundleProduct -> CollectionUtils.isNotEmpty(bundleProduct.getProducts()) &&
                                                bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                        .anyMatch(product -> Objects.nonNull(product.getObj()) &&
                                                                CollectionUtils.isNotEmpty(product.getObj().getVariants()) &&
                                                                product.getObj().getVariants().stream().filter(Objects::nonNull)
                                                                        .anyMatch(variant -> Objects.nonNull(variant.getAttributes()) &&
                                                                                StringUtils.isNotEmpty(variant.getAttributes().getSubCategory()) &&
                                                                                StringUtils.equalsIgnoreCase(Constants.LOCALS, variant.getAttributes().getSubCategory()))))))
                .map(CTOffer::getCode)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getConfigList(String key) {
        if (CollectionUtils.isNotEmpty(redisCacheHelper.getValues(key, Constants.OTT))) {
            return new ArrayList<>(Arrays.asList(
                    Optional.ofNullable(redisCacheHelper.getValues(key, Constants.OTT).get(0))
                            .orElse("")
                            .split("\\s*,\\s*")));
        }
        return null;
    }

    public  Map<String, List<String>> getEmbeddedSVODProductsFromGetOffersGeneric(CTOfferResponse getOfferResponse, CouponOffersRequest couponOffersRequest,CTProductResponse productResponse, boolean isEmbeddedSVOD) {
        log.info("getEmbeddedSVODProductsFromGetOffersGeneric() - Started");

        Map<String, List<String>> map = new HashMap<>();
        Map<String, String> resultMap = new HashMap<>();
        Map<String, String> getKeyFromID = getMapOfProductsKeyFromId(productResponse);
        Map<String, List<String>> resultMaps = new HashMap<>();

        // Check if offerActionType contains "Upgrade", "Downgrade", "Cross-sell", or "Other"
        if (isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            getOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                List<String> productKeyList = new ArrayList<>(); // Create a new list for each offer
                offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(
                        bundledProduct -> {
                            bundledProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                product.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                                            if (null != prod.getObj() && null != prod.getObj().getVariants()) {
                                                prod.getObj().getVariants().stream().filter(Objects::nonNull).forEach(mm -> {
                                                    if (CollectionUtils.isNotEmpty(mm.getAttributes().getIncludedProducts()) && CollectionUtils.isNotEmpty(mm.getAttributes().getContractIndicator())
                                                            && mm.getAttributes().getContractIndicator().contains(couponOffersRequest.getContractIndicator().get(0))) {
                                                        mm.getAttributes().getIncludedProducts().stream().filter(Objects::nonNull).forEach(inc -> {
                                                                    if (CollectionUtils.isNotEmpty(inc.getProducts())) {
                                                                        inc.getProducts().stream().forEach(ff -> {
                                                                            productKeyList.add(getKeyFromID.get(ff.getId()));
                                                                        });
                                                                    }
                                                                }
                                                        );
                                                        map.put(offer.getCode(), productKeyList);
                                                    }
                                                });
                                            }
                                        }
                                );
                            });
                        });
            });
        } else {
            productResponse.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                        List<String> productKeyList = new ArrayList<>();
                        prod.getVariants().stream().filter(Objects::nonNull).forEach(mm -> {
                            if (CollectionUtils.isNotEmpty(mm.getAttributes().getIncludedProducts()) && CollectionUtils.isNotEmpty(mm.getAttributes().getContractIndicator())
                                    && mm.getAttributes().getContractIndicator().contains(couponOffersRequest.getContractIndicator().get(0))) {
                                mm.getAttributes().getIncludedProducts().stream().filter(Objects::nonNull).forEach(inc -> {
                                            if (CollectionUtils.isNotEmpty(inc.getProducts())) {
                                                inc.getProducts().stream().forEach(ff -> {
                                                    productKeyList.add(getKeyFromID.get(ff.getId()));
                                                });
                                            }
                                        }
                                );
                                map.put(prod.getCode(), productKeyList);
                            }
                        });
                    }
            );
        }

        List<String> embeddedSVODProductList = new ArrayList<>();

        productResponse.getProducts().stream()
                .filter(Objects::nonNull)
                .forEach(product -> product.getVariants().stream()
                        .filter(Objects::nonNull)
                        .filter(variant -> isEmbeddedSVOD
                                ? Constants.EMBEDDED_SVOD.equalsIgnoreCase(variant.getAttributes().getSubCategory())
                                : !Constants.EMBEDDED_SVOD.equalsIgnoreCase(variant.getAttributes().getSubCategory()))
                        .forEach(variant -> embeddedSVODProductList.add(product.getCode())));

        log.debug("Embedded SVOD product list: {}", embeddedSVODProductList);

        if (!embeddedSVODProductList.isEmpty() && isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            // Filter offers that have a bundled product equal to a product in embeddedSVODProductList
            /* Compare base offer (OF_BASE-SPORTSMVP-202408_TAZBYOD_15OFF_3MO_FT) with addon offer(OF_BOLT-ESPNPLUS-202411_TAZBYOD)
            by matching embeddedSVODProductList BOLT-ESPNPLUS-202411. Check if both offers are coming in getOffers response */
            Map<String, List<String>> filteredMap = getOfferResponse.getOffers().stream()
                    .filter(offer -> offer.getAttributes().getAssociatedProducts().stream()
                            .flatMap(bundledProduct -> bundledProduct.getBundleProducts().stream())
                            .flatMap(product -> product.getProducts().stream())
                            .anyMatch(prod -> embeddedSVODProductList.contains(getKeyFromID.get(prod.getId()))))
                    .collect(Collectors.toMap(CTOffer::getCode, offer -> offer.getAttributes().getAssociatedProducts().stream()
                            .flatMap(bundledProduct -> bundledProduct.getBundleProducts().stream())
                            .flatMap(product -> product.getProducts().stream())
                            .map(prod -> getKeyFromID.get(prod.getId()))
                            .collect(Collectors.toList())));

            // Map filtered offers to resultMap
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                List<String> includedOffers = filteredMap.keySet().stream()
                        .filter(filteredKey -> filteredMap.get(filteredKey).stream().anyMatch(value::contains))
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(includedOffers)) {
                    resultMaps.put(key, includedOffers);
                }
            }
        } else if (!embeddedSVODProductList.isEmpty()) {
            // Remove entries from map that do not contain any product in embeddedSVODProductList
            map.entrySet().removeIf(entry ->
                    entry.getValue().stream().noneMatch(embeddedSVODProductList::contains)
            );
            if (!map.isEmpty()) {
                map.entrySet().forEach(mapset -> {
                    mapset.getValue().removeIf(value -> !embeddedSVODProductList.contains(value));
                });
            }
            resultMaps = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        log.info("getEmbeddedSVODProductsFromGetOffersGeneric() - Completed with resultMap: {}", resultMap);
        return resultMaps;
    }

    private List<String> getCompatibleProducts(ProductObj productObj) {

        List<String> compatibleProductsIds = new ArrayList<>();

        if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes()) &&
                CollectionUtils.isNotEmpty(productObj.getVariants().get(0).getAttributes().getCompatibleProducts())) {

            // Getting compatibleProductsList
            List<ProductWrapper> compatibleProductsList = productObj.getVariants().get(0).getAttributes()
                    .getCompatibleProducts();

            compatibleProductsList.stream().filter(Objects::nonNull).forEach(compatibleProducts -> {

                if (CollectionUtils.isNotEmpty(compatibleProducts.getProducts())) {
                    compatibleProducts.getProducts().stream().filter(Objects::nonNull)
                            .forEach(compatibleProduct -> {
                                if (Objects.nonNull(compatibleProduct.getId())) {
                                    compatibleProductsIds.add(compatibleProduct.getId());
                                }
                            });
                }

            });
        }
        return compatibleProductsIds;
    }

    public void updateCartCallOfferCodesIfChangesInReconnectFlow(List<String> requestCartVideoPlanOffers, String cartCallOfferCode, CouponOffersRequest couponOffersRequest, HashMap<String, String> reconnectOfferCodeNActionMap) {
        log.debug("inside updateCartCallOfferCodesIfChangesInReconnectFlow() start");
        //if contractIndicator is TAZBYOD or GENRE we are updating request cartOffer
        if(CollectionUtils.containsAny(couponOffersRequest.getContractIndicator(), Arrays.asList(Constants.TAZBYOD_STRING, Constants.GENRE))
                && !featureManagerHelper.isEnabled(Constants.FEATURE_VALIDATECART_GENERIC_FLOW)) {
            if (!requestCartVideoPlanOffers.contains(cartCallOfferCode)) {
                couponOffersRequest.getCartContext().getCartOffers().stream()
                        .filter(m -> requestCartVideoPlanOffers.contains(m.getOfferCode()))
                        .forEach(m -> {
                            m.setAction("Remove");
                            reconnectOfferCodeNActionMap.put(m.getOfferCode(), "Remove");
                        });
                requestCartVideoPlanOffers.clear();
                requestCartVideoPlanOffers.add(cartCallOfferCode);
                CartOffer cartOffer = new CartOffer();
                cartOffer.setOfferCode(cartCallOfferCode);
                cartOffer.setAction("Add");
                reconnectOfferCodeNActionMap.put(cartCallOfferCode, "Add");
                couponOffersRequest.getCartContext().getCartOffers().add(cartOffer);
            }
        }else{
            if (!requestCartVideoPlanOffers.contains(cartCallOfferCode)) {
                requestCartVideoPlanOffers.clear();
                requestCartVideoPlanOffers.add(cartCallOfferCode);
            }
        }
        log.debug("inside updateCartCallOfferCodesIfChangesInReconnectFlow() end");
    }

    public List<String> getBundleProductIdsFromCTOfferResponse(CTOfferResponse ctOfferResponseOfferCodes) {
        List<String> bundleProductIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ctOfferResponseOfferCodes.getOffers())) {
            ctOfferResponseOfferCodes.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
                    ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                            .forEach(associatedProduct -> {
                                if (Objects.nonNull(associatedProduct.getBundleProducts())) {
                                    associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                            .forEach(bundleProduct -> {
                                                if (Objects.nonNull(bundleProduct.getProducts())) {
                                                    bundleProduct.getProducts().stream().filter(Objects::nonNull)
                                                            .forEach(product -> {
                                                                bundleProductIds.addAll(getCompatibleProducts(product.getObj()));
                                                            });
                                                }
                                            });
                                }
                            });
                }
            });
        }
        return bundleProductIds;
    }

    public List<CartItems> removeDuplicateFromCartItems(List<CartItems> cartItems) {
        Set<String> uniqueProductCodes = new HashSet<>();
        List<CartItems> newCartItems = new ArrayList<>();
        for (CartItems cartItem : cartItems) {
            if (uniqueProductCodes.add(cartItem.getProductCode())) {
                newCartItems.add(cartItem);
            }
        }
        return newCartItems;
    }

    public Map<String, List<String>> groupByProductGroup(CTProductResponse ctProductResponse, CouponOffersRequest couponOffersRequest) {
        if (Objects.isNull(ctProductResponse) || CollectionUtils.isEmpty(ctProductResponse.getProducts()) || !featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VALIDATE_CART_PRODUCT_GRP)) {
            return new HashMap<>();
        }else if (featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)){
            return groupByProductGroupAndComplianceRank(ctProductResponse, couponOffersRequest);
        }
        if (!isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            List<ProductObj> products = ctProductResponse.getProducts();
            return products.stream()
                    .filter(Objects::nonNull)
                    .filter(product -> Objects.nonNull(product.getProductType()) && StringUtils.equalsIgnoreCase(product.getProductType().getKey(), Constants.VIDEO_ADDON))
                    .filter(product -> Objects.nonNull(product.getVariants()) && CollectionUtils.isNotEmpty(product.getVariants()))
                    .flatMap(product -> product.getVariants().stream()
                            .filter(Objects::nonNull).filter(variant -> StringUtils.isNotEmpty(variant.getAttributes().getProductGroup()))
                            .map(variant -> Map.entry(variant.getAttributes().getProductGroup(), product.getCode())))
                    .filter(entry -> entry.getKey() != null) // Ensure productGroup is not null
                    .collect(Collectors.groupingBy(Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        }
        return new HashMap<>();
    }
    
    /**
     * Overloaded version of groupByProductGroup for the acquisition/sales flow.
     * For the services (non-acquisition) flow it delegates to the existing method which
     * returns {@code billingProductGroup -> [productCode sorted by complianceRank]}.
     * For the acquisition/sales flow it returns
     * {@code billingProductGroup -> [offerCode sorted by complianceRank]} so that
     * the Drools rule can deduplicate at the offer level inside {@code CartItems.offers}.
     *
     * Offer-code resolution uses a two-pass strategy identical to the one that was
     * previously inside {@code applyBillingProductGroupDeduplication}:
     * <ol>
     *   <li>Pass 1 – look up the offer's bundled product via {@code ctOfferResponse}.</li>
     *   <li>Pass 2 – derive the product code from the offer code by stripping the
     *       {@code OF_} prefix and trailing {@code _<SEGMENT>} parts.</li>
     * </ol>
     */
    public Map<String, List<String>> groupByProductGroup(CTProductResponse ctProductResponse,
                                                          CTOfferResponse ctOfferResponse,
                                                          CouponOffersRequest couponOffersRequest) {
        if (Objects.isNull(ctProductResponse) || CollectionUtils.isEmpty(ctProductResponse.getProducts())) {
            return Collections.emptyMap();
        }
        if (!isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            // Services flow – feature flag guards existing product-level deduplication
            if (!featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_VALIDATE_CART_PRODUCT_GRP)) {
                return Collections.emptyMap();
            }
            return groupByProductGroup(ctProductResponse, couponOffersRequest);
        } else {
            // Sales / acquisition flow – build offer-level grouping map
        	if (!featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC))
        		return Collections.emptyMap();
        	else
        		return buildOfferGroupingMap(ctProductResponse, ctOfferResponse, couponOffersRequest);
        }
    }

    /**
     * Builds {@code billingProductGroup -> [offerCode sorted by complianceRank]} for the
     * sales/acquisition flow.  Mirrors the logic previously in
     * {@code applyBillingProductGroupDeduplication}.
     */
    private Map<String, List<String>> buildOfferGroupingMap(CTProductResponse ctProductResponse,
                                                             CTOfferResponse ctOfferResponse,
                                                             CouponOffersRequest couponOffersRequest) {
        // Step 1: productCode -> billingProductGroup and productCode -> complianceRank
        Map<String, String>  productCodeToGroupMap = new HashMap<>();
        Map<String, Integer> productCodeToRankMap  = new HashMap<>();

        for (ProductObj productObj : ctProductResponse.getProducts()) {
            if (productObj == null || productObj.getCode() == null
                    || CollectionUtils.isEmpty(productObj.getVariants())) continue;
            for (Variant variant : productObj.getVariants()) {
                if (variant == null || variant.getAttributes() == null) continue;
                Attributes attrs = variant.getAttributes();
                String group = attrs.getBillingProductGroup();
                Integer rank = attrs.getComplianceRank();
                if (StringUtils.isNotBlank(group)) {
                    productCodeToGroupMap.put(productObj.getCode(), group);
                }
                if (rank != null) {
                    productCodeToRankMap.put(productObj.getCode(), rank);
                }
                if (productCodeToGroupMap.containsKey(productObj.getCode())
                        && productCodeToRankMap.containsKey(productObj.getCode())) break;
            }
        }

        if (productCodeToGroupMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // Step 2: offerCode -> productCode (pass 1 via CTOfferResponse bundle products,
        //         pass 2 via offer code derivation)
        Map<String, String> getKeyFromID = getMapOfProductsKeyFromId(ctProductResponse);
        Map<String, String> offerCodeToProductCodeMap = new HashMap<>();

        if (Objects.nonNull(ctOfferResponse) && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
            for (CTOffer ctOffer : ctOfferResponse.getOffers()) {
                if (ctOffer == null || ctOffer.getCode() == null) continue;
                if (Objects.isNull(ctOffer.getAttributes())
                        || CollectionUtils.isEmpty(ctOffer.getAttributes().getAssociatedProducts())) continue;
                String productCode = ctOffer.getAttributes().getAssociatedProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(ap -> CollectionUtils.isNotEmpty(ap.getBundleProducts()))
                        .flatMap(ap -> ap.getBundleProducts().stream())
                        .filter(Objects::nonNull)
                        .filter(bp -> CollectionUtils.isNotEmpty(bp.getProducts()))
                        .flatMap(bp -> bp.getProducts().stream())
                        .filter(Objects::nonNull)
                        .map(p -> getKeyFromID.get(p.getId()))
                        .filter(StringUtils::isNotBlank)
                        .filter(productCodeToGroupMap::containsKey)
                        .findFirst().orElse(null);
                if (productCode != null) {
                    offerCodeToProductCodeMap.put(ctOffer.getCode(), productCode);
                }
            }
        }

        if (offerCodeToProductCodeMap.isEmpty()) {
            return Collections.emptyMap();
        }

        // Step 3: build billingProductGroup -> [offerCode sorted by complianceRank]
        Map<String, List<Map.Entry<String, Integer>>> groupToOfferRankList = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : offerCodeToProductCodeMap.entrySet()) {
            String offerCode   = entry.getKey();
            String productCode = entry.getValue();
            String group = productCodeToGroupMap.get(productCode);
            if (StringUtils.isBlank(group)) continue;
            int rank = productCodeToRankMap.getOrDefault(productCode, Integer.MAX_VALUE);
            groupToOfferRankList.computeIfAbsent(group, k -> new ArrayList<>())
                    .add(new AbstractMap.SimpleEntry<>(offerCode, rank));
        }

        Map<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map.Entry<String, Integer>>> entry : groupToOfferRankList.entrySet()) {
            List<String> sortedOfferCodes = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(Map.Entry::getValue))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), sortedOfferCodes);
        }
        return result;
    }

    /**
     * Derives a productCode from an offerCode by stripping the "OF_" prefix and then
     * progressively removing trailing "_&lt;SEGMENT&gt;" parts until we find a match in
     * the given productCodeSet.
     */
    private String deriveProductCodeFromOfferCode(String offerCode, Map<String, String> productCodeToGroupMap) {
        if (StringUtils.isBlank(offerCode)) return null;
        String stripped = offerCode.startsWith("OF_") ? offerCode.substring(3) : offerCode;
        if (productCodeToGroupMap.containsKey(stripped)) return stripped;
        while (stripped.contains("_")) {
            int lastUnderscore = stripped.lastIndexOf('_');
            stripped = stripped.substring(0, lastUnderscore);
            if (productCodeToGroupMap.containsKey(stripped)) return stripped;
        }
        return null;
    }


    public Map<String, List<String>> groupByProductGroupAndComplianceRank(CTProductResponse ctProductResponse, CouponOffersRequest couponOffersRequest) {
        if (Objects.isNull(ctProductResponse) || CollectionUtils.isEmpty(ctProductResponse.getProducts()) || !featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_COMPLIANCE_RANK_BILLING_PROD_GRP_VC)) {
            return new HashMap<>();
        }
        if (!isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType())) {
            List<ProductObj> products = ctProductResponse.getProducts();
            return products.stream()
                    .filter(Objects::nonNull)
                    .filter(product -> Objects.nonNull(product.getProductType()) && StringUtils.equalsIgnoreCase(product.getProductType().getKey(), Constants.VIDEO_ADDON))
                    .filter(product -> Objects.nonNull(product.getVariants()) && CollectionUtils.isNotEmpty(product.getVariants()))
                    .flatMap(product -> product.getVariants().stream()
                            .filter(Objects::nonNull)
                            .filter(variant -> Objects.nonNull(variant.getAttributes()) && StringUtils.isNotEmpty(variant.getAttributes().getBillingProductGroup()))
                            .map(variant -> Map.entry(variant.getAttributes().getBillingProductGroup(),
                                    Map.entry(product.getCode(),
                                            Optional.ofNullable(variant.getAttributes().getComplianceRank()).orElse(Integer.MAX_VALUE)))))
                    .filter(entry -> entry.getKey() != null)
                    .collect(Collectors.groupingBy(Map.Entry::getKey,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    list -> list.stream()
                                            .sorted(Comparator.comparingInt(e -> e.getValue().getValue()))
                                            .map(e -> e.getValue().getKey())
                                            .collect(Collectors.toList()))));
        }
        return new HashMap<>();
    }

    public Map<String, List<String>> groupByProductCodeOffersForPeacock(CTOfferResponse ctOfferResponse, CouponOffersRequest couponOffersRequest, Map<String, List<String>> productMaps) {
        if (Objects.isNull(ctOfferResponse) || CollectionUtils.isEmpty(ctOfferResponse.getOffers()) || productMaps.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> finalMap = new HashMap<>();
        if (isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType()) && !productMaps.isEmpty()) {
            Map<String, List<String>> offersMap = ctOfferResponse.getOffers().stream()
                    .filter(Objects::nonNull)
                    .filter(offer -> Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts()))
                    .flatMap(offer -> offer.getAttributes().getAssociatedProducts().stream()
                            .filter(Objects::nonNull)
                            .flatMap(associatedProduct -> associatedProduct.getBundleProducts().stream())
                            .filter(Objects::nonNull)
                            .flatMap(bundleProduct -> bundleProduct.getProducts().stream())
                            .filter(Objects::nonNull)
                            .filter(product -> product.getObj() != null)
                            .map(product -> Map.entry(product.getObj().getCode(), offer.getCode())))
                    .collect(Collectors.groupingBy(Map.Entry::getKey,
                            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

            productMaps.entrySet().forEach(k -> {
                if (offersMap.containsKey(k.getKey())) {
                    List<String> combinedList = new ArrayList<>();
                    for (String offer : k.getValue()) {
                        if (offersMap.containsKey(offer)) {
                            combinedList.addAll(offersMap.get(offer));
                        }
                    }
                    finalMap.put(offersMap.get(k.getKey()).get(0), combinedList);
                }
            });
        }
        return finalMap;
    }

    private void setCustomerContexts(CouponOffersRequest couponOffersRequest, OfferRequest offersRequest) {
        if (Objects.nonNull(couponOffersRequest.getCustomerContext())) {
            offersRequest.setCustomerContext(new CustomerContext());
            List<String> existingProductFamily = couponOffersRequest.getCustomerContext().getExistingProductFamily().stream()
                    .map(productFamily -> productFamily.getProductFamily())
                    .collect(Collectors.toList());
            offersRequest.getCustomerContext().setExistingProductFamily(existingProductFamily);
            if (existingProductFamily.contains(Constants.OTT_PRODUCT_FAMILY)) {
                offersRequest.getCustomerContext().setOtt(couponOffersRequest.getCustomerContext().getOtt());
            } else if (existingProductFamily.contains(Constants.IPTV_PRODUCT_FAMILY)) {
                offersRequest.getCustomerContext().setIptv(new CartProduct());
            } else if (existingProductFamily.contains(Constants.SATELLITE_PRODUCT_FAMILY)) {
                offersRequest.getCustomerContext().setSatellite(new CartProduct());
            }
        }
    }

    public Map<String, List<String>> getConfigMap(String key) {
        Map<String, List<String>> configMap = new HashMap<>();
        if (StringUtils.isNotEmpty(key)) {
            // Split the repackagedChannels string by the ';' delimiter
            String[] entries = key.split("\\s*;\\s*");
            for (String entry : entries) {
                // Split each entry into key and value by the ':' delimiter
                String[] keyValue = entry.split("\\s*:\\s*");
                if (keyValue.length == 2 && StringUtils.isNotEmpty(keyValue[0]) && StringUtils.isNotEmpty(keyValue[1])) {
                    String mapKey = keyValue[0];
                    // Split the values by ',' and collect non-null, non-empty values into a list
                    List<String> mapValues = Arrays.stream(keyValue[1].split("\\s*,\\s*"))
                            .filter(StringUtils::isNotEmpty)
                            .collect(Collectors.toList());
                    if (!mapValues.isEmpty()) {
                        configMap.put(mapKey, mapValues);
                    }
                }
            }
        }
        return configMap;
    }

    public void updatingProductTypeInCartProducts(CouponOffersRequest request, CTProductResponse productResponse) {
        if ((request == null || request.getCartContext() == null || productResponse == null)
                || CollectionUtils.isEmpty(request.getCartContext().getCartProducts())
                || CollectionUtils.isEmpty(productResponse.getProducts())) {
            return;
        }
        request.getCartContext().getCartProducts().forEach(cartProduct -> {
            if (StringUtils.isEmpty(cartProduct.getProductType()) && StringUtils.isNotEmpty(cartProduct.getProductCode())) {
                String productType = productResponse.getProducts().stream()
                        .filter(Objects::nonNull)
                        .filter(product -> StringUtils.equalsIgnoreCase(product.getCode(), cartProduct.getProductCode()))
                        .map(ProductObj::getProductType)
                        .filter(Objects::nonNull)
                        .map(GenericTypeIdBase::getKey)
                        .findFirst()
                        .orElse(null);
                cartProduct.setProductType(productType);
            }
        });
    }
    
    public Map<String, List<String>> getCustomerContextPromotions(CouponOffersRequest couponOffersRequest) {
        Map<String, List<String>> promotionsByProduct = new HashMap<>();
        if (couponOffersRequest == null || couponOffersRequest.getCustomerContext() == null) {
            return promotionsByProduct;
        }
        CartProduct ottContext = couponOffersRequest.getCustomerContext().getOtt();
        if (ottContext == null || CollectionUtils.isEmpty(ottContext.getProducts())) {
            return promotionsByProduct;
        }
        ottContext.getProducts().stream()
                .filter(Objects::nonNull)
                .filter(product -> StringUtils.isNotBlank(product.getProductCode()))
                .forEach(product -> {
                    if (CollectionUtils.isEmpty(product.getPromotions())) {
                        return;
                    }
                    List<String> promotionIds = promotionsByProduct.computeIfAbsent(product.getProductCode(), key -> new ArrayList<>());
                    product.getPromotions().stream()
                            .filter(Objects::nonNull)
                            .map(CustomerPromotion::getPromotionId)
                            .filter(StringUtils::isNotBlank)
                            .forEach(promoId -> {
                                if (!promotionIds.contains(promoId)) {
                                    promotionIds.add(promoId);
                                }
                            });
                });
        return promotionsByProduct;
	}

    public Map<String, Benefit> getPromoDetails(CouponOffersRequest couponOffersRequest) {
		 Map<String, Benefit> promoDetails = new HashMap<>();
        Set<String> promotionIds = new HashSet<>();
        
        // Global Rules for ValidateCart
        List<String> validateCartRules = redisCacheHelper.getValidateRules(Constants.DISCOUNTRULES, Constants.OTT);
        if(CollectionUtils.isNotEmpty(validateCartRules)) {
            for (String ruleString : validateCartRules) {
                String[] parts = ruleString.split(":");
                if (parts.length == 2) {
                    String[] productAndDiscount = parts[1].split("\\|");
                    String discount = productAndDiscount[1].trim();
                    promotionIds.add(discount);
                }
            }
        }
		if(CollectionUtils.isNotEmpty(promotionIds)) {
			CTBenefitsRequest ctBenefitsRequest = new CTBenefitsRequest();
	        ctBenefitsRequest.setBenefitCodes(new ArrayList<>(promotionIds));
			CTBenefitsResponse ctBenefitsResponse = cpopClient != null ? cpopClient.getBenefitsFromGraphlQL(ctBenefitsRequest, Arrays.asList("saleschannels","eligibleIAPPartners")) : null;
			if (ctBenefitsResponse != null && CollectionUtils.isNotEmpty(ctBenefitsResponse.getBenefits())) {
				for(Benefit benefit: ctBenefitsResponse.getBenefits()) {
					promoDetails.put(benefit.getCode(), benefit);
				}
			}
		}
		return promoDetails;
	}

    public String getIapPartnerAccountType(CouponOffersRequest couponOffersRequest) {
        if (couponOffersRequest != null && couponOffersRequest.getCustomerContext() != null
                && couponOffersRequest.getCustomerContext().getOtt() != null) {
            return couponOffersRequest.getCustomerContext().getOtt().getIapPartnerAccountType();
        }
        return null;
    }
    public List<String> getCompatibleProducts(CTProductResponse productResponse, List<String> cartProductCodes, String productTypeKey) {
        if (productResponse == null
                || CollectionUtils.isEmpty(productResponse.getProducts())
                || CollectionUtils.isEmpty(cartProductCodes)
                || StringUtils.isBlank(productTypeKey)) {
            return new ArrayList<>();
        }
        return productResponse.getProducts().stream()
                .filter(Objects::nonNull)
                .filter(productObj -> productObj.getCode() != null && cartProductCodes.contains(productObj.getCode()))
                .filter(productObj -> productObj.getProductType() != null
                        && productObj.getProductType().getKey() != null
                        && productObj.getProductType().getKey().equalsIgnoreCase(productTypeKey))
                .flatMap(productObj -> {
                    if (CollectionUtils.isEmpty(productObj.getVariants())) return Stream.empty();
                    return productObj.getVariants().stream()
                            .filter(Objects::nonNull)
                            .flatMap(variant -> {
                                if (variant.getAttributes() == null
                                        || CollectionUtils.isEmpty(variant.getAttributes().getCompatibleProducts())) return Stream.empty();
                                return variant.getAttributes().getCompatibleProducts().stream()
                                        .filter(Objects::nonNull)
                                        .flatMap(productWrapper -> {
                                            if (CollectionUtils.isEmpty(productWrapper.getProducts())) return Stream.empty();
                                            return productWrapper.getProducts().stream()
                                                    .filter(Objects::nonNull)
                                                    .map(Product::getKey)
                                                    .filter(Objects::nonNull);
                                        });
                            });
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    public Map<String, List<String>> conflictingOfferOrProductToRemoveBasedOnConflictingProducts(CTOfferResponse ctOfferResponse, CTProductResponse productResponse, CouponOffersRequest couponOffersRequest) {
        if (!featureManagerHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICT_PRODUCT_RULE_VC)) {
            return new HashMap<>();
        }
        Map<String, List<String>> offerOrProductToRemove = new HashMap<>();
        Map<String, List<String>> conflictingProductKeyMap = new HashMap<>();
        Map<String, String> getKeyFromID = getMapOfProductsKeyFromId(productResponse);
        if (isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType()) && Objects.nonNull(ctOfferResponse) && CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())
                && Objects.nonNull(couponOffersRequest.getCartContext()) && CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartOffers())) {
            List<String> cartOffers = couponOffersRequest.getCartContext().getCartOffers().stream().filter(Objects::nonNull).map(CartOffer::getOfferCode).collect(Collectors.toList());
            List<CTOffer> filteredOffers = ctOfferResponse.getOffers().stream().filter(ctOffer -> cartOffers.contains(ctOffer.getCode())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(filteredOffers)) {
                filteredOffers.forEach(ctOffer -> {
                    if (Objects.nonNull(ctOffer.getAttributes()) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getAssociatedProducts())) {
                        ctOffer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
                            if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) && Objects.nonNull(associatedProduct.getBundleProducts().get(0)) &&
                                    CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts().get(0).getProducts())) {
                                associatedProduct.getBundleProducts().get(0).getProducts().stream().forEach(product -> {
                                    if (Objects.nonNull(product.getObj()) && CollectionUtils.isNotEmpty(product.getObj().getVariants())) {
                                        product.getObj().getVariants().stream().forEach(variant -> {
                                            if (Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getConflictingProductsReference())) {
                                                variant.getAttributes().getConflictingProductsReference().forEach(conflictingPrd -> {
                                                    if (CollectionUtils.isNotEmpty(conflictingPrd.getBillingConflictingProducts())) {
                                                        conflictingPrd.getBillingConflictingProducts().forEach(billingPrdRef -> {
                                                            conflictingProductKeyMap.computeIfAbsent(ctOffer.getCode(), key -> new ArrayList<>()).add(getKeyFromID.get(billingPrdRef.getId()));
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
            if (!conflictingProductKeyMap.isEmpty()) {
                ctOfferResponse.getOffers().forEach(ctOffer -> {
                    if (Objects.nonNull(ctOffer.getAttributes()) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getAssociatedProducts())) {
                        ctOffer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
                            if (CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()) && Objects.nonNull(associatedProduct.getBundleProducts().get(0)) &&
                                    CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts().get(0).getProducts())) {
                                associatedProduct.getBundleProducts().get(0).getProducts().stream().forEach(product -> {
                                    conflictingProductKeyMap.forEach((offerCode, conflictingProductKeys) -> {
                                        if (conflictingProductKeys.contains(product.getKey())) {
                                            offerOrProductToRemove.computeIfAbsent(offerCode, key -> new ArrayList<>()).add(ctOffer.getCode());
                                        }
                                    });
                                });
                            }
                        });
                    }
                });
            }
        } else if (!isAcquisitionOfferActionType(couponOffersRequest.getOfferActionType()) && Objects.nonNull(productResponse) && CollectionUtils.isNotEmpty(productResponse.getProducts())
                && Objects.nonNull(couponOffersRequest.getCartContext()) && CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartProducts())) {
            //service flow logic here
            List<String> cartProductCodes = couponOffersRequest.getCartContext().getCartProducts().stream().filter(Objects::nonNull).map(com.dtv.dcp.epoch.model.common.CartProduct::getProductCode).collect(Collectors.toList());
            productResponse.getProducts().stream().filter(product -> cartProductCodes.contains(product.getCode())).forEach(product -> {
                if (Objects.nonNull(product.getVariants()) && CollectionUtils.isNotEmpty(product.getVariants())) {
                    product.getVariants().stream().filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getConflictingProductsReference()))
                            .forEach(variant -> variant.getAttributes().getConflictingProductsReference().forEach(conflictingPrd -> {
                                if (CollectionUtils.isNotEmpty(conflictingPrd.getBillingConflictingProducts())) {
                                    conflictingPrd.getBillingConflictingProducts().forEach(billingPrdRef -> {
                                        conflictingProductKeyMap.computeIfAbsent(product.getCode(), key -> new ArrayList<>()).add(billingPrdRef.getKey());
                                    });
                                }
                            }));
                }
            });
            return conflictingProductKeyMap;

        }
        return offerOrProductToRemove;
    }
}
