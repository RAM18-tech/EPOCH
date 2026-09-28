package com.dtv.dcp.epoch.processor.satellite.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CustomerContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.ExistingPromotion;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.coupon.PurchaseDetails;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.product.Attributes;
import com.dtv.dcp.epoch.model.ct.product.InCompatibleProductValue;
import com.dtv.dcp.epoch.model.ct.product.IncompatibleProducts;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.BenefitCodesToSuppressTheOffer;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CTProductRequest;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.request.Product;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.CTProductResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.model.customergraph.CustomerCouponsResults;
import com.dtv.dcp.epoch.processor.satellite.SatelliteCTOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphClientService;
import com.dtv.dcp.epoch.service.customergraph.CustomerGraphService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.PnpGroupUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author ap778g
 * @author ka6579
 */
@Component
public class SatelliteServicesOffersProcessor {

    /**
     * The CpopUCCClientHelper
     */
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CpopUCCClientHelper cpopUCCClientHelper;

    /**
     * The CustomerGraphService
     */
    @Autowired
    private CustomerGraphService customerGraphService;

    /**
     * The FeatureManagerHelper
     */
    @Autowired
    private FeatureManagerHelper featureManagerHelper;

    /**
     * The CustomerGraphClientService
     */
    @Autowired
    private CustomerGraphClientService customerGraphClientService;

    /**
     * The SatelliteServicesOffersProcessorUCCHelper
     */
    @Autowired
    private SatelliteServicesOffersProcessorUCCHelper satelliteUCCHelper;

    @Autowired
    private OffersUtils util;

    @Autowired
    private CpopClientHelper cpopClientHelper;

    @Autowired
    private SatelliteCTOffersProcessor satelliteCTOffersProcessor;

    @Autowired
    OffersUtils utils;

    @Autowired
    private DMALookUpService dmaLookUpService;

    @Autowired
    private PnpGroupUtils pnpGroupUtils;

    @Autowired
    RedisCacheHelper redisCacheHelper;

    Map<String, String> couponStatusList = new HashMap<>();


    /**
     * The log.
     */
    static Logger log = LoggerFactory.getLogger(SatelliteServicesOffersProcessor.class);

    /**
     * /**
     *
     * @param offerRequestWrapper the wrapper object containing all details of the offer request
     * @return CTOfferResponse the response object containing the list of offers and related attributes
     * returned from the offer processing logic
     */
    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) {
        log.info("Start of Satellite GET OFFER method getOffers non retention");

        List<String> offerTypes = Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferTypes()).orElse(Collections.emptyList());

        if (offerTypes.contains("coupon")) {
            return getCouponOffers(offerRequestWrapper);
        } else {
            return getSecModOffers(offerRequestWrapper);
        }
    }

    private CTOfferResponse getCouponOffers(OfferRequestWrapper offerRequestWrapper) {
        if (featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_ENABLED)) {
            log.info("Satellite GET OFFER getoffer flow Ucc flag enabled ");
            CTOfferRequest ctOfferRequest = offerRequestWrapper.getCtOfferRequest();
            OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
            if (Optional.ofNullable(offerRequest.getOfferTypes()).isPresent() &&
                    Optional.ofNullable(offerRequest.getOfferProductType()).isPresent() &&
                    isCouponOfferrequested(offerRequest.getOfferTypes(), offerRequest.getOfferProductType())) {
                updateCouponRequest(offerRequestWrapper, offerRequest);
                Map<String, PurchaseDetails> purchaseDetailsList = offerRequestWrapper.getPurchaseDetailsList();
                ctOfferRequest.setOfferProductFamily(offerRequest.getOfferProductFamily());
                ctOfferRequest.setOfferProductType(offerRequest.getOfferProductType());
                ctOfferRequest.setSalesChannel(offerRequest.getSalesChannel());
                ctOfferRequest.setOfferType(offerRequest.getOfferTypes());
                if (Optional.ofNullable(offerRequestWrapper.getCtOfferRequest()).isPresent() &&
                        Optional.ofNullable(offerRequestWrapper.getCtOfferRequest().getCouponCodes()).isPresent() &&
                        !offerRequestWrapper.getCtOfferRequest().getCouponCodes().isEmpty()) {
                    CTOfferResponse offerResponse = getOffersFromCT(offerRequestWrapper.getCtOfferRequest());
                    satelliteUCCHelper.updateUCCCTOfferResponse(this, offerRequestWrapper, purchaseDetailsList, offerResponse);
                    log.info("End of Satellite GET OFFER method getOffers non retention Call CT for RESPONSE:valid COUPON");
                    satelliteUCCHelper.updateTitlesResponse(offerResponse);
                    return offerResponse;
                } else {
                    CTOfferResponse offerResponse = satelliteUCCHelper.getEmptyCTResponse();
                    log.info("End of Satellite GET OFFER method getOffers non retention EMPTY RESPONSE: NO valid COUPON");
                    return offerResponse;
                }
            }
        }

        log.info("End of Satellite GET OFFER method getOffers non retention null response UCC flag false");
        return satelliteUCCHelper.getEmptyCTResponse();
    }


    /**
     * @param ctOfferRequest the request object containing offer details for CT
     * @return CTOfferResponse the response containing offers from CT
     */
    public CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest) {
        log.info("Start In Method SatelliteCTOffersProcessor.getOffersFromCT() :: Getting response from the CTMS");
        CTOfferResponse offerResponse = cpopUCCClientHelper.getOffers(ctOfferRequest);
        if (offerResponse == null) {
            log.info(
                    "In Method SatelliteCTOffersProcessor.getOffersFromCT() :: Getting the null response from the CTMS");
            throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
        }
        log.debug("End In Method SatelliteCTOffersProcessor.getOffersFromCT() :: Getting response from the CTMS");
        return offerResponse;

    }

    /**
     * @param offerProductType A list of product types associated with the offer.
     * @param offerTypes       A list of offer types to evaluate.
     * @return boolean Returns `true` if the `offerTypes` contains `Constants.COUPON`
     * and the `offerProductType` contains `Constants.CAMPAIGN`, otherwise `false`.
     */
    private boolean isCouponOfferrequested(List<String> offerTypes, List<String> offerProductType) {
        return ((offerTypes != null && offerTypes.stream()
                .anyMatch(Predicate.isEqual(Constants.COUPON)))
                && (offerProductType.stream()
                .anyMatch(Predicate.isEqual(Constants.CAMPAIGN)))
        );
    }

    /**
     * @param offerRequestWrapper the wrapper object containing offer request details
     * @param offersRequest       the offer request data
     * @return OfferRequestWrapper the updated offer request wrapper
     */
    private OfferRequestWrapper updateCouponRequest(OfferRequestWrapper offerRequestWrapper, OfferRequest offersRequest) {
        log.info("Start of Satellite GET OFFER method updateCouponRequest");
        CustomerCouponsResults customerCouponsResults = null;
        String accountId = offersRequest.getCustomerContext().getSatellite().getAccountNumber();
        String accountType = Constants.UVERSE_CG_ACCOUNT_TYPE;
        String customerId = "1";
        if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                .anyMatch(s -> s.equalsIgnoreCase(Constants.ONLINE))) {
            customerId = "1";

        } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                .anyMatch(s -> s.equalsIgnoreCase(Constants.OPUS))) {
            customerId = "1w";
        }
        try {
            log.debug("Satellite GET OFFER method getUverseCustomerCoupons call");
            customerCouponsResults = customerGraphService.getUverseCustomerCoupons(customerId, accountId, accountType, CustomerCouponsResults.class, featureManagerHelper.isEnabled(Constants.FEATURE_TOGGLE_DTVN_EPOCHENABLED_BACKUP) ? "backup" : "primary");
        } catch (ServiceException e) {
            log.error(String.format("Exception while getUverseAccountCoupon %s", e.getMessage()));
        }

        requestUpdateCouponCode(offerRequestWrapper, offersRequest, customerCouponsResults);
        log.debug("End of Satellite GET OFFER method updateCouponRequest");
        return offerRequestWrapper;
    }

    /**
     * @param offerRequestWrapper    the wrapper object containing offer request details
     * @param offersRequest          the offer request data
     * @param customerCouponsResults the results containing customer coupon information
     */
    private void requestUpdateCouponCode(OfferRequestWrapper offerRequestWrapper, OfferRequest offersRequest,
                                         CustomerCouponsResults customerCouponsResults) {
        if (customerCouponsResults != null) {
            Map<String, PurchaseDetails> purchaseDetailsList = new HashMap<>();
            List<CustomerCoupons> couponList = customerCouponsResults.getCoupons();
            CTOfferRequest ctOfferRequest = offerRequestWrapper.getCtOfferRequest();
            List<String> couponCodes = new ArrayList<>();
            String accountId = offersRequest.getCustomerContext().getSatellite().getAccountNumber();
            couponList.forEach(coupon -> {
                if (Optional.ofNullable(coupon.getStatusDescription()).isPresent()
                        && coupon.getStatusDescription().equalsIgnoreCase(Constants.USED_COUPON)) {
                    updateCouponStatusToCG(accountId, coupon);
                }

                if (Optional.ofNullable(offersRequest.getCouponStatus()).isPresent()) {
                    offersRequest.getCouponStatus().forEach(status -> {
                        if (status.equalsIgnoreCase(coupon.getStatusDescription())) {
                            if (!(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)
                                    && Constants.MULTI_USE_TYPE_2_COUPON.equalsIgnoreCase(coupon.getCouponType())
                                    && Constants.INVALID_STATUS.equalsIgnoreCase(coupon.getStatusDescription()))) {
                                couponCodes.add(coupon.getCouponCode());
                                couponCodePurchaseDetailsListMapping(purchaseDetailsList, coupon);
                                couponStatusMapping(couponStatusList, coupon);
                            }
                        }
                    });
                } else {
                    if (!(featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)
                            && Constants.MULTI_USE_TYPE_2_COUPON.equalsIgnoreCase(coupon.getCouponType())
                            && Constants.INVALID_STATUS.equalsIgnoreCase(coupon.getStatusDescription()))) {
                        couponCodes.add(coupon.getCouponCode());
                        couponCodePurchaseDetailsListMapping(purchaseDetailsList, coupon);
                        couponStatusMapping(couponStatusList, coupon);
                    }
                }
            });
            ctOfferRequest.setCouponStatus(offersRequest.getCouponStatus());
            ctOfferRequest.setCouponCodes(couponCodes);
            offerRequestWrapper.setCtOfferRequest(ctOfferRequest);
            offerRequestWrapper.setPurchaseDetailsList(purchaseDetailsList);
        }
    }

    private void updateCouponStatusToCG(String accountId, CustomerCoupons coupon) {
        if (Optional.ofNullable(coupon.getLockDuration()).isPresent() && Optional.ofNullable(coupon.getUsedDate()).isPresent()) {
            log.info("Used Date.....{}", coupon.getUsedDate());
            log.info("LockDuration.....{}", coupon.getLockDuration());
            String differenceDateTime = satelliteUCCHelper.getDifferenceTime(coupon.getUsedDate());
            log.info("differenceDateTime.....{}", differenceDateTime);
            int lockDuration = Integer.parseInt(coupon.getLockDuration());
            int diffDateTime = Integer.parseInt(differenceDateTime);
            if (diffDateTime >= lockDuration) {
                log.info("Change in CG Used to avaible call publishUpdateCouponMessage()");
                customerGraphClientService.publishUpdateCouponMessage(coupon, accountId);
                log.info("Change in CG Used to avaible call publishUpdateCouponMessage() : sucess");
                coupon.setStatus(Constants.AVAILABLE);
                coupon.setStatusDescription(Constants.AVAILABLE_COUPON);
                if (featureManagerHelper.isEnabled(Constants.SVC_EPOCH_UCC_B_ENABLED)
                        && Constants.MULTI_USE_TYPE_2_COUPON.equalsIgnoreCase(coupon.getCouponType())) {
                    coupon.setStatusDescription(Constants.INVALID_STATUS);
                }
            }
            log.info("No Change in CG Used to AVAILABLE, no call to publishUpdateCouponMessage()");
        }
    }

    /**
     * @param couponStatusList the map containing coupon codes and their status descriptions
     * @param coupon           the CustomerCoupons object to extract status information from
     */
    private void couponStatusMapping(Map<String, String> couponStatusList,
                                     CustomerCoupons coupon) {
        couponStatusList.put(coupon.getCouponCode(), coupon.getStatusDescription());
    }

    /**
     * @param purchaseDetailsList the map to store coupon code and purchase details
     * @param coupon              the CustomerCoupons object containing coupon information
     */
    private void couponCodePurchaseDetailsListMapping(Map<String, PurchaseDetails> purchaseDetailsList,
                                                      CustomerCoupons coupon) {
        if (Optional.ofNullable(coupon.getTitlePurchased()).isPresent()
                && Optional.ofNullable(coupon.getStatusDescription()).isPresent()
                && !coupon.getStatusDescription().equalsIgnoreCase(Constants.AVAILABLE_COUPON)) {
            PurchaseDetails purchaseDetailsObj = satelliteUCCHelper.getCouponPurchaseDetails(coupon);
            purchaseDetailsList.put(coupon.getCouponCode(), purchaseDetailsObj);
        }
    }

    public List<BenefitCodesToSuppressTheOffer> convertToBenefitCodesToSuppressTheOffer(List<ExistingPromotion> existingPromotions) {
        List<BenefitCodesToSuppressTheOffer> result = new ArrayList<>();
        existingPromotions.forEach(promotion -> {
            BenefitCodesToSuppressTheOffer obj = new BenefitCodesToSuppressTheOffer();
            if (Objects.nonNull(promotion.getBenefitCode())) {
                obj.setBenefitCode(promotion.getBenefitCode());
                obj.setStartDate(promotion.getStartDate());
                obj.setEndDate(promotion.getEndDate());
            } else if (Objects.nonNull(promotion.getAgreementId())) {
                obj.setBenefitCode(promotion.getAgreementId() + "/" + promotion.getAgreementPriceCode());
                obj.setStartDate(promotion.getAgreementStartDate());
                obj.setEndDate(promotion.getAgreementEndDate());
            }
            result.add(obj);
        });
        return result;
    }

    private CTOfferResponse getSecModOffers(OfferRequestWrapper offerRequestWrapper) {
        log.info("Start of Satellite getOffers services flow");
        CTOfferRequest ctOfferRequest = offerRequestWrapper.getCtOfferRequest();
        CTOfferResponse ctOfferResponse;
        CTOfferResponse offerResponse = new CTOfferResponse();
        CTOfferResponse cartOfferResponse = null;
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
        if (ctOfferRequest != null) {
            OfferRequest offerRequest = offerRequestWrapper.getOfferRequest();
            ctOfferRequest.setOfferProductFamily(offerRequest.getOfferProductFamily());
            ctOfferRequest.setOfferProductType(offerRequest.getOfferProductType());
            ctOfferRequest.setSalesChannel(offerRequest.getSalesChannel());
            ctOfferRequest.setOfferType(offerRequest.getOfferTypes());
            ctOfferRequest.setHasLocalChannels(offerRequest.getHasLocalChannels());
            ctOfferRequest.setOfferActionType(offerRequest.getOfferActionType());
            ctOfferRequest.setFlow(offerRequestWrapper.getFlow());

            // coolOfPeriod start
            if (!isADEPerfFlow(ctOfferRequest)) {
                if (Optional.ofNullable(offerRequest.getCustomerContext().getSatellite().getExistingPromotions())
                        .isPresent()) {
                    List<ExistingPromotion> existingPromotions = offerRequest.getCustomerContext().getSatellite()
                            .getExistingPromotions();
                    List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(
                            existingPromotions);
                    if (Optional.ofNullable(benefitCodesToSuppressTheOffer).isPresent()) {
                        ctOfferRequest.setBenefitCodesToSuppressTheOffer(benefitCodesToSuppressTheOffer);
                    }
                }
            }

            // coolOfPeriod end

            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerEligibility())) {

                CustomerEligibility custEligibility = offerRequestWrapper.getOfferRequest().getCustomerEligibility();
                List<String> zipCode = custEligibility.getZipCode();
                List<String> county = custEligibility.getCounty();

                if (CollectionUtils.isNotEmpty(zipCode) && CollectionUtils.isNotEmpty(county)) {
                    List<String> dmaValue = dmaLookUpService.getDMAValue(zipCode.get(0), county.get(0));

                    if (CollectionUtils.isNotEmpty(dmaValue)) {
                        custEligibility.setDma(dmaValue);
                    }
                    ctOfferRequest.setCustomerEligibility(custEligibility);
                }
            }

            //PSO request
            if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBillingProductCodes())) {
                ctOfferRequest.setBillingProductCodes(offerRequestWrapper.getOfferRequest().getBillingProductCodes());
            }

            if (isADEPerfFlow(ctOfferRequest)) {
            	
            	long startTimeInMillis = System.currentTimeMillis();
            	
                List<CTCustomerContext> customerContext = ctOfferRequest.getCustomerContext();
                CustomerEligibility customerEligibility = ctOfferRequest.getCustomerEligibility();
                Boolean hasLocalChannels = ctOfferRequest.getHasLocalChannels();
                ctOfferRequest.setCustomerEligibility(null);
                ctOfferRequest.setCustomerContext(null);
                ctOfferRequest.setHasLocalChannels(null);
                ctOfferRequest.setAdeRequest(true);

                if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_ASYNC_CALLS_ENABLED)) {

					ctOfferResponse = getOffersFromCTAsync(ctOfferRequest);
					
					long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
					log.info("EPOCH_ADE_EXECUTION_TIME using Async MW calls -[{}]", (endTimeMillis));

				} else {
					ctOfferResponse = getOffersFromCT(ctOfferRequest);
					
					long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
					log.info("EPOCH_ADE_EXECUTION_TIME using existing code flow -[{}]", (endTimeMillis));
				}

                ctOfferRequest.setCustomerContext(customerContext);
                ctOfferRequest.setCustomerEligibility(customerEligibility);
                ctOfferRequest.setHasLocalChannels(hasLocalChannels);

            } else {
                ctOfferResponse = getOffersFromCT(ctOfferRequest);
            }

            String modifiedSalesChannel = OffersUtils.getModifiedSalesChannel(
                    offerRequestWrapper.getOfferRequest().getSalesChannel(),
                    offerRequestWrapper.getOfferRequest().getOfferActionType());

            util.processOffers(ctOfferResponse, modifiedSalesChannel);

            if (isADEPerfFlow(ctOfferRequest) && Optional.ofNullable(offerRequest.getCustomerContext().getSatellite().getExistingPromotions())
                    .isPresent() && null != ctOfferResponse.getOffers()) {
                List<ExistingPromotion> existingPromotions = offerRequest.getCustomerContext().getSatellite()
                        .getExistingPromotions();
                List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer = convertToBenefitCodesToSuppressTheOffer(
                        existingPromotions);

                List<CTOffer> offers = ctOfferResponse.getOffers();
                List<CTOffer> removeOffers = new ArrayList<>();
                if (Optional.ofNullable(offers).isPresent()) {
                    offers.stream().filter(Objects::nonNull).forEach(offer -> {
                        if (CollectionUtils.isNotEmpty(benefitCodesToSuppressTheOffer) && offer != null
                                && CollectionUtils.isNotEmpty(offer.getAttributes().getBenefitCodes())) {
                            for (BenefitCodesToSuppressTheOffer inputSupressBenefitCode : benefitCodesToSuppressTheOffer) {
                                if (processBenefitCodetoSupress(offer, inputSupressBenefitCode)) {
                                    removeOffers.add(offer);
                                }
                            }
                        }
                    });
                }

                if (CollectionUtils.isNotEmpty(removeOffers) && offers != null) {
                    offers.removeAll(removeOffers);
                    ctOfferResponse.setOffers(offers);
                }
            }

            List<String> insuranceProductCodes = new ArrayList<>();
            if (util.isSTMSRequest(offerRequestWrapper)) {
                insuranceProductCodes = redisCacheHelper.getValues("insuranceProductCodes", Constants.SATELLITE_PRODUCT_FAMILY);
            }

            List<String> existingProductIds = new ArrayList<>();
            List<String> incompatibleProductIds = new ArrayList<>();
            Map<String, String> compatibleReceiversMap = new HashMap<>();
            List<String> componentCode = new ArrayList<>();
            if (Objects.nonNull(offerRequest.getCartContext()) &&
                    CollectionUtils.isNotEmpty(offerRequest.getCartContext().getCartOffers())) {
                cartOfferResponse = satelliteCTOffersProcessor.getOfferByIds(offerRequestWrapper, false, false);
            }
            ctOfferResponse.getOffers().removeIf(ctOffer -> util.isNotValidBasedOnBillingSystem(ctOffer, offerRequestWrapper));
            populateExistingProductIdsAndIncompatibleIds(ctOfferResponse.getProducts(), offerRequestWrapper, cartOfferResponse, existingProductIds, incompatibleProductIds, compatibleReceiversMap, componentCode, insuranceProductCodes);
            applyEligibilityFiltersInPrice(ctOfferRequest.getCustomerContext(), ctOfferResponse.getOffers(), serverDate, offerRequestWrapper);
            List<CTOffer> cartOffersList = new ArrayList<>();
            if (Objects.nonNull(cartOfferResponse)) {
                cartOffersList.addAll(cartOfferResponse.getOffers());
            }
            List<String> existingProducts = populateExistingProducts(offerRequestWrapper, cartOfferResponse);

            removeBPOffersWhenLCC(ctOfferResponse, existingProducts);

            ctOfferResponse.getOffers().removeIf(ctOffer -> isNotValidOffer(ctOffer, existingProductIds, incompatibleProductIds, ctOfferRequest, componentCode, cartOffersList, offerRequestWrapper, existingProducts));
            applyVideoDeviceFeeFilters(ctOfferResponse, offerRequestWrapper, compatibleReceiversMap);

            retentionDisputeChanges(offerRequestWrapper, ctOfferResponse);

            if (isADEPerfFlow(ctOfferRequest)) {
                satelliteCTOffersProcessor.filterOffersBasedOnDMA(offerRequestWrapper, ctOfferResponse);
            }

            satelliteCTOffersProcessor.applyBcodeExclusion(offerRequestWrapper, ctOfferResponse);

            List<String> conflictingOfferIds = new ArrayList<>();
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                conflictingOfferIds.addAll(satelliteCTOffersProcessor.getConflictingOfferIds(ctOffer));
            });
            ctOfferResponse.getOffers().removeIf(ctOffer -> conflictingOfferIds.contains(ctOffer.getId()));

            satelliteCTOffersProcessor.calculateBestPrice(ctOfferResponse.getOffers(), offerRequestWrapper, false);

            if (offerRequest.getSalesChannel().contains(Constants.STB)
                    && Objects.nonNull(offerRequestWrapper.getOfferRequest().getAdeOffersLite())
                    && offerRequestWrapper.getOfferRequest().getAdeOffersLite()) {

                List<CTOffer> offerList = getOffersWithADEOffersLite(ctOfferResponse);
                offerResponse.setOffers(offerList);

            } else {
                offerResponse.setOffers(ctOfferResponse.getOffers());
            }
            offerResponse.setTotal(ctOfferResponse.getOffers().size());
            offerResponse.setCount(ctOfferResponse.getOffers().size());
        }

        return offerResponse;
    }
    
    public CTOfferResponse getOffersFromCTAsync(CTOfferRequest ctOfferRequest) {
    	
        log.info("In Method SatelliteCTOffersProcessor.getOffersFromCTAsync() Getting response from the CTMS.");

        if (CollectionUtils.isEmpty(ctOfferRequest.getOfferProductType())) {
            throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
        }
        
        List<String> offerProductTypes = ctOfferRequest.getOfferProductType();

        try {
            
        	// Iterate and make asynchronous calls
			List<CompletableFuture<CTOfferResponse>> futures = offerProductTypes.stream()
					.map(offerProductType -> CompletableFuture.supplyAsync(() -> {
						try {
							CTOfferRequest ctOfferRequestAsync = new CTOfferRequest();
							if (Objects.nonNull(ctOfferRequest)) {
								BeanUtils.copyProperties(ctOfferRequest, ctOfferRequestAsync);
							}
							ctOfferRequestAsync.setOfferProductType(Collections.singletonList(offerProductType));
							log.info("Processing offerProductType: " + offerProductType);
							return cpopUCCClientHelper.getOffers(ctOfferRequestAsync);
						} catch (Exception e) {
							log.error("Error processing offerType: " + offerProductType, e);
							return null;
						}
					})).collect(Collectors.toList());
            
            // Wait for all async calls to complete and collect results
            List<CTOfferResponse> responses = futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull) 
                .collect(Collectors.toList());
            
            if (responses.isEmpty()) {
                log.error("No successful responses received from async calls :: Getting the null response from the CTMS");
                throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
            }

            log.info("All asynchronous calls completed. Successful responses: " + responses.size());
            
            // Combine the responses as needed
            CTOfferResponse offerResponse = combineResponses(responses);

            if (offerResponse == null) {
                log.info("In Method SatelliteCTOffersProcessor.getOffersFromCTAsync() :: Getting the null response from the CTMS");
                throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
            }
            log.debug("End In Method SatelliteCTOffersProcessor.getOffersFromCTAsync() :: Getting response from the CTMS");

            return offerResponse;

        } catch (Exception e) {
            log.error("In Method SatelliteCTOffersProcessor.getOffersFromCTAsync()", e);
            throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
        }
    }

    private CTOfferResponse combineResponses(List<CTOfferResponse> responses) {
    	
		List<CTOffer> offers = new ArrayList<>();
		List<ProductObj> productObjs = new ArrayList<>();
		CTOfferResponse combinedResponse = null;

		if (responses != null && !responses.isEmpty()) {
			combinedResponse = new CTOfferResponse();

			responses.forEach(response -> {
				offers.addAll(response.getOffers());
				productObjs.addAll(response.getProducts());
			});
            
			combinedResponse.setOffers(offers);
			combinedResponse.setProducts(productObjs);
		}

		return combinedResponse;
    }
    
    private boolean processBenefitCodetoSupress(CTOffer offer,
                                                BenefitCodesToSuppressTheOffer inputSupressBenefitCode) {
        List<CTOffer> removeOffers = new ArrayList<>();
        if (null != offer.getAttributes().getBenefitCodes() && offer.getAttributes().getBenefitCodes()
                .contains(inputSupressBenefitCode.getBenefitCode())) {
            populateInvalidOffers(offer, inputSupressBenefitCode, removeOffers);
        }


        return CollectionUtils.isNotEmpty(removeOffers);
    }

    private void populateInvalidOffers(CTOffer offer, BenefitCodesToSuppressTheOffer inputSupressBenefitCode,
                                       List<CTOffer> removeOffers) {
        if (checkInvalidOffers(inputSupressBenefitCode, offer.getAttributes().getCoolOffPeriod())) {
            removeOffers.add(offer);
        } else {
            int diffDays = OffersUtils
                    .returnNumberOfDays(inputSupressBenefitCode.getEndDate());
            if (diffDays < offer.getAttributes().getCoolOffPeriod()) {
                removeOffers.add(offer);
            }
        }
    }

    private boolean checkInvalidOffers(BenefitCodesToSuppressTheOffer inputSupressBenefitCode,
                                       int coolOffPeriod) {
        return coolOffPeriod == 9999
                || null == inputSupressBenefitCode.getEndDate() || OffersUtils
                .isEndDateIsGreater(inputSupressBenefitCode.getEndDate());
    }

    private boolean isADEPerfFlow(CTOfferRequest ctOfferRequest) {
        return featureManagerHelper.isEnabled("svc-epoch-ade-cache-perf-change")
                && ((Optional.ofNullable(ctOfferRequest.getOfferActionType()).isPresent()
                && !ctOfferRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE))
                && (Optional.ofNullable(ctOfferRequest.getSalesChannel()).isPresent()
                && ctOfferRequest.getSalesChannel().contains("online"))
                && (Optional.ofNullable(ctOfferRequest.getOfferProductFamily()).isPresent()
                && ctOfferRequest.getOfferProductFamily().contains("satellite")));
    }

    /**
     * Applies retention dispute changes to the offer response.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctOfferResponse     the CT offer response
     */
    private void retentionDisputeChanges(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {

        if (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
                && CollectionUtils.isEmpty(offerRequestWrapper.getOfferRequest().getBillingProductCodes())) {
            //Remove PSO offers from other Retention flows
            ctOfferResponse.getOffers().removeIf(ctOffer -> Objects.nonNull(ctOffer.getAttributes().getRetentionOfferSubType())
                    && ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PREMIUM));
        }

        if (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBillingProductCodes())) {
            //Remove other Retention offers from PSO flow
            ctOfferResponse.getOffers().removeIf(ctOffer -> Objects.isNull(ctOffer.getAttributes().getRetentionOfferSubType())
                    || !ctOffer.getAttributes().getRetentionOfferSubType().contains(Constants.RETENTION_OFFER_SUBTYPE_PREMIUM));
        }

        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getDynamicUpgrade())) {
            Integer dynamicUpgrade = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getDynamicUpgrade();

            //Remove offers if dynamicUpgrade value is not matching
            ctOfferResponse.getOffers().removeIf(ctOffer -> satelliteCTOffersProcessor.isNotValidBasedOnDynamicUpgrade(ctOffer, dynamicUpgrade));
        }
    }

    private Boolean isNotValidOffer(CTOffer ctOffer, List<String> existingProductIds, List<String> incompatibleProductIds,
                                    CTOfferRequest ctOfferRequest, List<String> componentCode, List<CTOffer> cartOffersList, OfferRequestWrapper offerRequestWrapper, List<String> existingProducts) {
        return Objects.nonNull(ctOffer) && (isNotValidBasedOnOfferDate(ctOffer, offerRequestWrapper) || isNotValidBasedOnExistingProd(existingProductIds, ctOffer) ||
                isNotValidBasedOnIncompatibleProd(incompatibleProductIds, ctOffer) ||
                (Objects.nonNull(ctOffer.getAttributes()) && StringUtils.isNotEmpty(ctOffer.getAttributes().getOfferProductType())
                        && !Constants.CREDIT.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType()) && getPriceSize(ctOffer) == 0) ||
                !isQualifyingOffer(ctOffer, existingProductIds) ||
                isNotValidOfferBasedOnProductType(ctOffer, ctOfferRequest, componentCode, cartOffersList, offerRequestWrapper) ||
                isNotValidBasedOnAccountEligibilities(ctOffer, offerRequestWrapper) || isNotValidSecondChanceOffer(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnRequiredProduct(ctOffer, existingProducts) || isNotValidBasedOnCCIDAndSalesChannel(ctOffer, offerRequestWrapper)
                || (!util.isSTMSRequest(offerRequestWrapper) && isNotValidBasedOnTenureRule(ctOffer, offerRequestWrapper))
                || (featureManagerHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED) 
        				&& isInCompatibleOffer(ctOffer, existingProductIds)));
    }
    
    public boolean isInCompatibleOffer(CTOffer offer, List<String> existingProductIds) {
    	
    	List<Boolean> isInCompatibleOffer = new ArrayList<Boolean>();
		if(Objects.nonNull(offer) && Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())) {
			offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
				if(CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
					associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
						if(Objects.nonNull(qualifyingProduct.getConstraints()) 
								&& CollectionUtils.isNotEmpty(qualifyingProduct.getConstraints().getQualifierIncompatibleProducts())) {
							qualifyingProduct.getConstraints().getQualifierIncompatibleProducts().stream().filter(Objects::nonNull).forEach(incompatibleProduct -> {
								if(Objects.nonNull(offer) && CollectionUtils.isNotEmpty(existingProductIds)
						                && existingProductIds.contains(incompatibleProduct.getId())) {
									isInCompatibleOffer.add(Boolean.TRUE);
								}
							});
						}
					});
				}
			});
		}
		return CollectionUtils.isNotEmpty(isInCompatibleOffer);
    }

    private Boolean isNotValidBasedOnOfferDate(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
        return Objects.nonNull(ctOffer) && !OffersUtils.validateActiveDates(
                OffersUtils.getFormattedDate(ctOffer.getStartDate()), OffersUtils.getFormattedDate(ctOffer.getEndDate()),
                serverDate);
    }

    private Boolean isNotValidBasedOnExistingProd(List<String> existingProductIds, CTOffer ctOffer) {
        return Objects.nonNull(ctOffer) && CollectionUtils.isNotEmpty(existingProductIds)
                && existingProductIds.contains(util.getProductIdFromOffer(ctOffer));
    }

    public Boolean isNotValidBasedOnIncompatibleProd(List<String> incompatibleProductIds, CTOffer ctOffer) {
        return Objects.nonNull(ctOffer) && CollectionUtils.isNotEmpty(incompatibleProductIds)
                && incompatibleProductIds.contains(util.getProductIdFromOffer(ctOffer));
    }

    private Boolean isNotValidOfferBasedOnProductType(CTOffer ctOffer, CTOfferRequest ctOfferRequest, List<String> componentCode,
                                                      List<CTOffer> cartOffersList, OfferRequestWrapper offerRequestWrapper) {
        return (Objects.nonNull(ctOffer.getAttributes()) && StringUtils.isNotEmpty(ctOffer.getAttributes().getOfferProductType())
                && ((Constants.VIDEO_PLAN.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                && !util.isRoadrunner(ctOffer) && !(util.isEligibleForServedMarket(ctOffer)
                ? Objects.nonNull(ctOfferRequest.getHasLocalChannels()) && ctOfferRequest
                .getHasLocalChannels().equals(util.isEligibleForServedMarket(ctOffer))
                : Objects.nonNull(ctOfferRequest.getHasLocalChannels())
                && ctOfferRequest.getHasLocalChannels().equals(util.hasLocalChannels(ctOffer))))
                || (Constants.VIDEO_ADDON.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                && (Objects.nonNull(util.isLocalChannelVideoAddon(ctOffer))
                && !util.isLocalChannelVideoAddon(ctOffer).equals(ctOfferRequest.getHasLocalChannels())))
                || (Constants.VIDEO_ACCESSORY.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                && ((!componentCode.isEmpty() && !isCompatibleAccessoryOffer(cartOffersList, offerRequestWrapper, ctOffer))
                || isNotValidBasedOnReplacementType(ctOfferRequest, util.getProductObjFromOffer(ctOffer))))));
    }

    private int getPriceSize(CTOffer ctOffer) {
        if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices()))
            return ctOffer.getAttributes().getAssociatedProducts().get(0).getBundleProducts().get(0).getProducts().get(0).getObj().getVariants().get(0).getPrices().size();
        return 0;
    }

    private boolean isQualifyingOffer(CTOffer ctOffer, List<String> existingProductIds) {
        List<String> qualifyingProductIds = new ArrayList<>();
        if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts())
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts())) {
            ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts().get(0).getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                if (CollectionUtils.isNotEmpty(existingProductIds) && existingProductIds.contains(product.getId())) {
                    qualifyingProductIds.add(product.getId());
                }
            });
            return CollectionUtils.isNotEmpty(qualifyingProductIds);
        }
        return true;
    }

    /**
     * Checks if required product is not present.
     *
     * @param ctOffer          the offer object
     * @param existingProducts list of existing products
     * @return boolean true if not present
     */
    public boolean isNotValidBasedOnRequiredProduct(CTOffer ctOffer, List<String> existingProducts) {
        List<String> eligibleProducts = new ArrayList<>();
        if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getRequiredProducts())) {
            List<String> reqdProducts = getListFromString(ctOffer.getAttributes().getRequiredProducts());
            reqdProducts.stream().filter(Objects::nonNull).forEach(product -> {
                if (CollectionUtils.isNotEmpty(existingProducts) && existingProducts.contains(product)) {
                    eligibleProducts.add(product);
                }
            });
            return CollectionUtils.isEmpty(eligibleProducts);
        }
        return false;
    }

    /**
     * Return true, if account eligibility is not met (STMS).
     * Return false, if account eligibility is met (STMS).
     * Return false, if Enabler request.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not met
     */
    private boolean isNotValidBasedOnAccountEligibilities(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        return util.isSTMSRequest(offerRequestWrapper) && (isNotValidBasedOnAccountStatus(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnHeartValue(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnPBEnrollment(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnABPEnrollment(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnCommissionDate(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnActivationDate(ctOffer, offerRequestWrapper) ||
                isNotValidBasedOnAccountType(ctOffer, offerRequestWrapper));
    }

    /**
     * Return true, if account status is not matching.
     * Return false, if eligibleAccountStatus is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not matching
     */
    private boolean isNotValidBasedOnAccountStatus(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)) && Objects.nonNull(
                ctOffer.getAttributes().getEligibility().getConstraints().get(0).getEligibleAccountStatus())) {
            List<String> statusList = getListFromString(
                    ctOffer.getAttributes().getEligibility().getConstraints().get(0).getEligibleAccountStatus());

            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()
                    .getSatellite().getAccountStatus())) {
                return statusList.stream().noneMatch(offerRequestWrapper.getOfferRequest().getCustomerContext()
                        .getSatellite().getAccountStatus()::equalsIgnoreCase);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true, if heart value is not matching.
     * Return false, if heart value is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not matching
     */
    private boolean isNotValidBasedOnHeartValue(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0).getHeartValue())) {
            List<String> heartValues = ctOffer.getAttributes().getEligibility().getConstraints().get(0).getHeartValue()
                    .stream().map(value -> value.substring(0, value.indexOf("-"))).collect(Collectors.toList());
            return !heartValues.contains(
                    offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getCustomerValue());
        }
        return false;
    }

    /**
     * Return true, if PB is not matching. Return false, if PB is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not matching
     */
    private boolean isNotValidBasedOnPBEnrollment(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)
                .getAcceptPaperlessBillEnrollment())) {
            return !ctOffer.getAttributes().getEligibility().getConstraints().get(0).getAcceptPaperlessBillEnrollment()
                    .equals(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
                            .getAcceptPaperlessBillEnrollment());
        }
        return false;
    }

    /**
     * Return true, if ABP is not matching. Return false, if ABP is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not matching
     */
    private boolean isNotValidBasedOnABPEnrollment(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0))
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)
                .getAcceptAutoBillPayEnrollment())) {
            return !ctOffer.getAttributes().getEligibility().getConstraints().get(0).getAcceptAutoBillPayEnrollment()
                    .equals(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
                            .getAcceptAutoBillPayEnrollment());
        }
        return false;
    }

    /**
     * Return true, if tenure rule is not met based on commission date.
     * Return false, if tenure rule based on commission date is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not met
     */
    private boolean isNotValidBasedOnCommissionDate(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)) && Objects.nonNull(
                ctOffer.getAttributes().getEligibility().getConstraints().get(0).getCommissionStartDate())) {
            String commStartDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
                    .getCommissionStartDate();

            if (StringUtils.isNotBlank(commStartDate)) {
                LocalDate commissionStartDate = OffersUtils.convertToLocalDate(commStartDate);
                LocalDate today = LocalDate.now();
                long diff = ChronoUnit.DAYS.between(commissionStartDate, today);
                return diff < ctOffer.getAttributes().getEligibility().getConstraints().get(0).getCommissionStartDate();
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true, if tenure rule is not met based on activation date.
     * Return false, if tenure rule based on activation date is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not met
     */
    private boolean isNotValidBasedOnActivationDate(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)) && Objects.nonNull(
                ctOffer.getAttributes().getEligibility().getConstraints().get(0).getActivationDate())) {
            String actDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
                    .getActivationDate();

            if (StringUtils.isNotBlank(actDate)) {
                LocalDate activationDate = OffersUtils.convertToLocalDate(actDate);
                LocalDate today = LocalDate.now();
                long diff = ChronoUnit.DAYS.between(activationDate, today);
                return diff < ctOffer.getAttributes().getEligibility().getConstraints().get(0).getActivationDate();
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * Return true, if account type is not matching.
     * Return false, if eligibleAccountTypes is empty in CT.
     *
     * @param ctOffer             the offer object
     * @param offerRequestWrapper the offer request wrapper
     * @return boolean true if not matching
     */
    private boolean isNotValidBasedOnAccountType(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)) && Objects.nonNull(
                ctOffer.getAttributes().getEligibility().getConstraints().get(0).getEligibleAccountTypes())) {
            List<String> acctTypeList = getListFromString(
                    ctOffer.getAttributes().getEligibility().getConstraints().get(0).getEligibleAccountTypes());

            String businessSegment = getBusinessSegment(offerRequestWrapper.getCtOfferRequest().getCustomerContext().get(0));
            if (Objects.nonNull(businessSegment)) {
                return acctTypeList.stream().noneMatch(businessSegment::equalsIgnoreCase);
            } else {
                return true;
            }
        }
        return false;
    }

    private void populateExistingProductIdsAndIncompatibleIds(List<ProductObj> products, OfferRequestWrapper offerRequestWrapper,
                                                              CTOfferResponse cartOfferResponse, List<String> existingProductIds, List<String> incompatibleProductIds
            , Map<String, String> compatibleReceiversMap, List<String> componentCode, List<String> insuranceProductCodes) {

        if (Objects.nonNull(cartOfferResponse) && CollectionUtils.isNotEmpty(cartOfferResponse.getOffers())) {
            Map<String, ProductObj> productMap = CollectionUtils.isEmpty(cartOfferResponse.getProducts())
                    ? Collections.emptyMap()
                    : cartOfferResponse.getProducts().stream()
                            .filter(Objects::nonNull)
                            .filter(p -> StringUtils.isNotEmpty(p.getId()))
                            .collect(Collectors.toMap(ProductObj::getId, p -> p, (a, b) -> a));
            cartOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                String productId = util.getProductIdFromOffer(offer);
                ProductObj productObj = cpopUCCClientHelper.getProductObjByid(productMap, productId);
                if (Objects.nonNull(productObj) && Objects.nonNull(productObj.getProductType())
                        && Constants.VIDEO_DEVICE.equalsIgnoreCase(productObj.getProductType().getKey())) {
                    //Getting compatibleReceivers of video-device in cartOffers
                    compatibleReceiversMap.putAll(getCompatibleProducts(productObj));
                } else {
                    existingProductIds.add(productId);
                    incompatibleProductIds.addAll(getIncompatibleProducts(productObj));
                }
            });
        }

        List<CTCustomerContext> customerContexts = offerRequestWrapper.getCtOfferRequest().getCustomerContext();
        // Filter offer response based on the request attribute 'CustomerContext.Products.billingProductCode'
        if (CollectionUtils.isNotEmpty(customerContexts) && Objects.nonNull(customerContexts.get(0))
                && Objects.nonNull(customerContexts.get(0).getProducts())) {
            customerContexts.get(0).getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                if (StringUtils.isNotEmpty(product.getComponentCode())) {
                    componentCode.add(product.getComponentCode());
                }
                boolean isProductInCart = isProductInCart(product, offerRequestWrapper);

                if (!isProductInCart) {

                    //Getting all existing package information based the request attribute 'CustomerContext.Products'
                    ProductObj productObj = getProductObjInCustomerContext(product, products, insuranceProductCodes);

                    if (Objects.nonNull(productObj) && Objects.nonNull(productObj.getProductType())
                            && Constants.VIDEO_DEVICE.equalsIgnoreCase(productObj.getProductType().getKey())) {
                        //Getting compatibleReceivers of products in 'CustomerContext.Products'
                        compatibleReceiversMap.putAll(getCompatibleProducts(productObj));
                    } else if (Objects.nonNull(productObj)) {
                        existingProductIds.add(productObj.getId());
                        //Getting incompatibleProductsIds of products in 'CustomerContext.Products'
                        incompatibleProductIds.addAll(getIncompatibleProducts(productObj));
                    }
                }
            });
        }
    }

    /**
     * Populates a list of existing products from the cart offer response.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param cartOfferResponse   the cart offer response
     * @return List<String> list of existing product IDs
     */
    public List<String> populateExistingProducts(OfferRequestWrapper offerRequestWrapper,
                                                 CTOfferResponse cartOfferResponse) {
        List<String> existingProducts = new ArrayList<>();

        if (Objects.nonNull(cartOfferResponse) && CollectionUtils.isNotEmpty(cartOfferResponse.getOffers())) {
            cartOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                ProductObj productObj = util.getProductObjFromOffer(offer);
                if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                        && Objects.nonNull(productObj.getVariants().get(0))
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes().getBillingProductCode())) {
                    existingProducts.add(productObj.getVariants().get(0).getAttributes().getBillingProductCode());
                }
            });
        }

        CustomerContext customerContext = offerRequestWrapper.getOfferRequest().getCustomerContext();
        if (Objects.nonNull(customerContext) && Objects.nonNull(customerContext.getSatellite())
                && CollectionUtils.isNotEmpty(customerContext.getSatellite().getProducts())) {
            customerContext.getSatellite().getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                if (Objects.nonNull(product.getBillingProductCode())) {
                    existingProducts.add(product.getBillingProductCode());
                }
            });
        }

        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts())) {
            offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts().stream().filter(Objects::nonNull).forEach(product -> {
                existingProducts.removeIf(p -> Objects.nonNull(p) && p.equalsIgnoreCase(product.getBillingProductCode()));
            });
        }
        return existingProducts;
    }

    /**
     * Finds a matching ProductObj in the customer context.
     *
     * @param product               the product to match
     * @param products              the list of product objects
     * @param insuranceProductCodes the list of insurance product codes
     * @return ProductObj the matching product object
     */
    private ProductObj getProductObjInCustomerContext(Product product, List<ProductObj> products, List<String> insuranceProductCodes) {
        ProductObj productObj = null;
        if (StringUtils.isNotBlank(product.getBillingProductCode())) {
            //Getting the product object on billing reference id and billing product code if product type is insurance
            if (StringUtils.isNotEmpty(product.getBillingReferenceID())
                    && Objects.nonNull(insuranceProductCodes) && insuranceProductCodes.contains(product.getBillingProductCode())) {
                productObj = getProductObjByBillingReferenceId(products, product.getBillingReferenceID(), product.getBillingProductCode());
            } else {
                productObj = getProductObjByBillingProductCode(products, product.getBillingProductCode());
            }
        } else if (StringUtils.isNotBlank(product.getPricePlanCode()) && !Constants.VIDEO_DEVICE.equals(product.getProductType())) {
            //Getting the product object on price plan code and pick code if product type is addon
            if (StringUtils.isNotBlank(product.getPickCode()) && Constants.VIDEO_ADDON.equals(product.getProductType())) {
                productObj = getProductObjByPickCode(products, product.getPricePlanCode(), product.getPickCode());
            } else {
                productObj = getProductObjByPricePlanCode(products, product.getPricePlanCode());
            }
        } else if (StringUtils.isNotBlank(product.getComponentCode()) && Constants.VIDEO_DEVICE.equals(product.getProductType())) {
            //Getting the product object on component code if product type is video-device
            productObj = getProductObjByComponentCode(products, product.getComponentCode());
        } else if (StringUtils.isNotBlank(product.getManufacturer()) && StringUtils.isNotBlank(product.getModelNumber())) {
            //Getting the product object on manufacturer-model number if product type is video-device
            productObj = getProductObjByManufacturerModel(products, product.getManufacturer(), product.getModelNumber());
        }
        return productObj;
    }

    /**
     * This method is used to get the incompatible products of a specific product.
     *
     * @param productObj the product object
     * @return List<String> list of incompatible product IDs
     */
    public List<String> getIncompatibleProducts(ProductObj productObj) {

        List<String> incompatibleProductsIds = new ArrayList<>();

        if (Objects.nonNull(productObj)
                && CollectionUtils.isNotEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes().getIncompatibleProducts())) {

            // Getting incompatibleProductsList
            List<IncompatibleProducts> incompatibleProductsList = productObj.getVariants().get(0).getAttributes().getIncompatibleProducts().get(0);

            if (null != incompatibleProductsList && !incompatibleProductsList.isEmpty()) {
                incompatibleProductsList.stream().filter(Objects::nonNull).forEach(incompatibleProducts -> {

                    if (Constants.PRODUCTS.equalsIgnoreCase(incompatibleProducts.getName())) {
                        List<InCompatibleProductValue> inCompatibleProductValueList = incompatibleProducts.getValue();

                        inCompatibleProductValueList.stream().filter(Objects::nonNull).forEach(inCompatibleProductValue -> {
                            if (Objects.nonNull(inCompatibleProductValue.getId())) {
                                incompatibleProductsIds.add(inCompatibleProductValue.getId());
                            }
                        });
                    }

                });
            }
        }
        return incompatibleProductsIds;
    }

    private ProductObj getProductObjByPickCode(List<ProductObj> products, String pricePlanCode, String pickCode) {
        if (Objects.nonNull(products)) {
            Optional<ProductObj> matchingObject = products.stream().filter(Objects::nonNull).
                    filter(p -> Objects.nonNull(p.getVariants().get(0).getAttributes().getEnablerPricePlanCode())
                            && p.getVariants().get(0).getAttributes().getEnablerPricePlanCode().equalsIgnoreCase(pricePlanCode)
                            && getBillingParamValue(p.getVariants().get(0).getAttributes().getBillingParams(), Constants.PICK_CODE).equalsIgnoreCase(pickCode)).
                    findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            }
        }
        return null;
    }

    private String getBillingParamValue(List<GenericNameValueBase> billingParams, String name) {
        if (Objects.nonNull(billingParams)) {
            Optional<GenericNameValueBase> matchingObject = billingParams.stream().filter(Objects::nonNull).
                    filter(billingParam -> Objects.nonNull(billingParam.getName()) && billingParam.getName().equalsIgnoreCase(name)).
                    findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get().getValue();
            }
        }
        return null;
    }

    /**
     * This method is used to find the product information based on the billingProductCode and billing referenceId.
     *
     * @param products           list of product objects
     * @param billingReferenceId the billing reference ID
     * @param billingProductcode the billing product code
     * @return ProductObj the matching product object
     */
    private ProductObj getProductObjByBillingReferenceId(List<ProductObj> products, String billingReferenceId, String billingProductcode) {
        if (CollectionUtils.isNotEmpty(products)) {
            for (ProductObj productObj : products) {
                if (Objects.nonNull(productObj) && Objects.nonNull(productObj.getVariants()) && Objects.nonNull(productObj.getVariants().get(0))
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                        && (Objects.nonNull(productObj.getVariants().get(0).getAttributes().getBillingProductCode()))
                        && CollectionUtils.isNotEmpty(productObj.getVariants().get(0).getPrices())
                        && productObj.getVariants().get(0).getAttributes().getBillingProductCode().equalsIgnoreCase(billingProductcode)) {
                    List<Price> pricesList = productObj.getVariants().get(0).getPrices();
                    for (Price price : pricesList) {
                        if (Objects.nonNull(price.getBillingReferenceId())
                                && price.getBillingReferenceId().equalsIgnoreCase(billingReferenceId.replaceFirst(Constants.BILLING_REFERENEID_PREFIX, ""))) {
                            return productObj;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method is used to find the product information based on the billingProductCode.
     *
     * @param products           list of product objects
     * @param billingProductCode the billing product code
     * @return ProductObj the matching product object
     */
    private ProductObj getProductObjByBillingProductCode(List<ProductObj> products, String billingProductCode) {

        if (Objects.nonNull(products)) {
            Optional<ProductObj> matchingObject = products.stream().filter(Objects::nonNull)
                    .filter(p -> Objects.nonNull(p.getVariants().get(0).getAttributes().getBillingProductCode())
                            && p.getVariants().get(0).getAttributes().getBillingProductCode().equalsIgnoreCase(billingProductCode))
                    .findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            }
        }
        return null;
    }

    private ProductObj getProductObjByPricePlanCode(List<ProductObj> products, String pricePlanCode) {

        if (Objects.nonNull(products)) {
            Optional<ProductObj> matchingObject = products.stream().filter(Objects::nonNull)
                    .filter(p -> Objects.nonNull(p.getVariants().get(0).getAttributes().getEnablerPricePlanCode())
                            && p.getVariants().get(0).getAttributes().getEnablerPricePlanCode().equalsIgnoreCase(pricePlanCode))
                    .findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            }
        }
        return null;
    }

    private void applyEligibilityFiltersInPrice(List<CTCustomerContext> customerContexts, List<CTOffer> ctOffers, Date serverDate, OfferRequestWrapper offerRequestWrapper) {
        String pnpCustomerGroup = null;
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection())) {
            PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection();
            String nbcd = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getNextBillingDate();

            pnpCustomerGroup = pnpGroupUtils.getPnpGroup(priceProtection, nbcd, offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
        }
        String pnpGroup = Objects.nonNull(pnpCustomerGroup) ? pnpCustomerGroup : null;

        if (Objects.nonNull(customerContexts) && Objects.nonNull(ctOffers)) {
            ctOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
                    ctOffer.getAttributes().getAssociatedProducts().forEach(associatedProduct -> {
                        if (Objects.nonNull(associatedProduct.getBundleProducts())) {
                            associatedProduct.getBundleProducts().forEach(bundleProduct -> {
                                if (Objects.nonNull(bundleProduct.getProducts())) {
                                    bundleProduct.getProducts().forEach(product -> {
                                        //ProductObj productObj = objectMapper.convertValue(product.getObj(), ProductObj.class);
                                    	ProductObj productObj = null;
                                    	if (featureManagerHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
                                			productObj = objectMapper.convertValue(product.getObj(), ProductObj.class);
                                		} else {
                                			productObj = SerializationUtils.clone(product.getObj());
                                		}
                                        applyFiltersInPrice(customerContexts.get(0), productObj, ctOffer, serverDate, offerRequestWrapper, pnpGroup);
                                        product.setObj(productObj);
                                    });
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    private void applyFiltersInPrice(CTCustomerContext customerContext, ProductObj productObj, CTOffer ctOffer, Date serverDate, OfferRequestWrapper offerRequestWrapper, String pnpGroup) {

        if (Objects.nonNull(customerContext) && Objects.nonNull(productObj)) {

            boolean isRoadrunner = String.valueOf(Constants.TWO).equalsIgnoreCase(customerContext.getDtvSwimlane());

            String businessSegment = getBusinessSegment(customerContext);
            String employeeSegment = customerContext.getEmployeeSegment();
            String nftvFlag = customerContext.getNftvFlag();
            String ftvCount = customerContext.getFtvCount();
            String policy = isRoadrunner ? Constants.DTV_SWIMLANE : customerContext.getPolicy();

            // Iterating each variants to apply filter conditions. Remove all price information which is not matching request attributes.
            productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                if (Objects.nonNull(variant.getPrices())) {

                    variant.getPrices().removeIf(price -> !OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()),
                            OffersUtils.getFormattedDate(price.getEndDate()), serverDate)
                            || (Objects.nonNull(ctOffer.getAttributes().getBillingId())
                            && Objects.nonNull(price.getBillingReferenceId())
                            && !price.getBillingReferenceId().equals(ctOffer.getAttributes().getBillingId())));

                    if (StringUtils.isNotBlank(businessSegment)) {
                        if (StringUtils.isNotBlank(employeeSegment)) {
                            variant.getPrices().removeIf(price -> (Objects.isNull(price.getBusinessSegment())
                                    || Objects.isNull(price.getEmployeeSegment())
                                    || !(getListFromString(price.getBusinessSegment()).stream().anyMatch(businessSegment::equalsIgnoreCase)
                                    && getListFromString(price.getEmployeeSegment()).stream().anyMatch(employeeSegment::equalsIgnoreCase))));
                        } else {
                            variant.getPrices().removeIf(price -> (Objects.isNull(price.getBusinessSegment())
                                    || !getListFromString(price.getBusinessSegment()).stream().anyMatch(businessSegment::equalsIgnoreCase)));
                        }
                    }

                    // nftvFlag and ftvCount filters will be applied only for video-plan
                    if (Constants.VIDEO_PLAN.equalsIgnoreCase(productObj.getProductType().getKey())) {

                        // Filter prices based on customer group for video-plan
                        satelliteCTOffersProcessor.filterInvalidPrices(offerRequestWrapper, variant, pnpGroup);

                        applyPriceFilterForVideoPlan(variant, nftvFlag, ftvCount, policy);
                    }

                    variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
                        if (Objects.nonNull(price.getSubCategory())) {
                            variant.getAttributes().setSubCategory(price.getSubCategory());
                        }
                    });
                }
            });
        }
    }

    private String getBusinessSegment(CTCustomerContext customerContext) {
        if (Constants.BUSINESS_SEGMENT_PTR.equalsIgnoreCase(customerContext.getBusinessSegment())
                && StringUtils.isNotEmpty(customerContext.getPartnerName())
                && StringUtils.isEmpty(customerContext.getEmployeeSegment())) {
            return customerContext.getPartnerName();
        }
        return customerContext.getBusinessSegment();
    }

    private void applyPriceFilterForVideoPlan(Variant variant, String nftvFlag, String ftvCount, String policy) {
        List<Price> prices = variant.getPrices();
        if (Objects.nonNull(prices)) {
            if (StringUtils.isNotBlank(nftvFlag)) {
                if (StringUtils.isNotBlank(ftvCount)) {
                    prices.removeIf(price -> Objects.isNull(price.getNftvFlag()) || Objects.isNull(price.getFtvCount())
                            || !(getListFromString(price.getNftvFlag()).contains(nftvFlag)
                            && getListFromString(price.getFtvCount()).contains(ftvCount)));
                } else {
                    prices.removeIf(price -> (Objects.isNull(price.getNftvFlag()))
                            || !getListFromString(price.getNftvFlag()).contains(nftvFlag));
                }
            }

            if (StringUtils.isNotEmpty(policy)) {
                Integer gfVersion;
                if (Objects.nonNull(variant.getAttributes().getGrandFatherInfo())) {
                    gfVersion = variant.getAttributes().getGrandFatherInfo().stream().filter(Objects::nonNull).filter(grandFatherInfo ->
                                    OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(grandFatherInfo.getSalesEffectiveDate()),
                                            OffersUtils.getFormattedDate(grandFatherInfo.getSalesExpirationDate())))
                            .map(grandFatherInfo -> grandFatherInfo.getVersion())
                            .findFirst()
                            .orElse(null);

                } else {
                    gfVersion = null;
                }
                if (Constants.NA.equalsIgnoreCase(policy)) {
                    prices.removeIf(price -> Objects.isNull(price.getOmsFreeSTBPolicyId())
                            || (Objects.nonNull(price.getOmsFreeSTBPolicyId())
                            && (!(Arrays.asList(price.getOmsFreeSTBPolicyId().split(",")).contains(Constants.DVANA1)
                            || Arrays.asList(price.getOmsFreeSTBPolicyId().split(",")).contains(Constants.DVANA2))
                            || (Objects.nonNull(gfVersion) && Objects.nonNull(price.getGrandfatherVersion())
                            && !price.getGrandfatherVersion().toString().equals(gfVersion.toString())))));
                } else {
                    prices.removeIf(price -> Objects.isNull(price.getOmsFreeSTBPolicyId())
                            || (Objects.nonNull(price.getOmsFreeSTBPolicyId())
                            && !Arrays.asList(price.getOmsFreeSTBPolicyId().split(",")).contains(policy)
                            || (Objects.nonNull(gfVersion) && Objects.nonNull(price.getGrandfatherVersion())
                            && !price.getGrandfatherVersion().toString().equals(gfVersion.toString()))));
                }

            }
        }
    }

    public List<String> getListFromString(String inputString) {
        List<String> convertedList = new ArrayList<>();

        if (StringUtils.isNotBlank(inputString)) {
            if (inputString.indexOf(",") != -1) {
                convertedList = Stream.of(inputString.split(",", -1)).map(String::trim).collect(Collectors.toList());
            } else {
                convertedList.add(inputString.trim());
            }
        }
        return convertedList;
    }

    public CTProductResponse getProductResponsefromCT(List<String> products) {
        CTProductRequest ctProductRequest = new CTProductRequest();
        CTProductResponse ctProductResponse = new CTProductResponse();
        if (CollectionUtils.isNotEmpty(products)) {
            ctProductRequest.setProductIds(products);
            ctProductResponse = cpopClientHelper.getProducts(ctProductRequest);
        }
        return ctProductResponse;

    }

    private List<ProductObj> getProductObjectFromResponse(CTProductResponse ctProductResponse, com.dtv.dcp.epoch.model.ct.product.Product prod) {
        List<ProductObj> productObj = new ArrayList<>();

        if (Objects.nonNull(ctProductResponse) && CollectionUtils.isNotEmpty(ctProductResponse.getProducts())) {
            productObj = ctProductResponse.getProducts().stream().filter(Objects::nonNull)
                    .filter(product ->
                            StringUtils.isNotEmpty(product.getId())
                                    && StringUtils.isNotEmpty(prod.getId())
                                    && product.getId().equalsIgnoreCase(prod.getId()))
                    .collect(Collectors.toList());
        }
        return productObj;
    }

    private boolean isCompatibleAccessoryOffer(List<CTOffer> cartOffersList, OfferRequestWrapper offerRequestWrapper, CTOffer offer) {

        long compatibleRemoteOfferCount = 0;
        if (Objects.nonNull(offer) && Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getAssociatedProducts())) {
            compatibleRemoteOfferCount = offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull)
                    .filter(associatedProduct -> CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts()))
                    .flatMap(associatedProduct -> associatedProduct.getBundleProducts().stream()).filter(Objects::nonNull)
                    .filter(bundleproduct -> CollectionUtils.isNotEmpty(bundleproduct.getProducts()))
                    .flatMap(bundleproduct -> bundleproduct.getProducts().stream()).filter(Objects::nonNull)
                    .filter(product -> Objects.nonNull(product.getObj()) && CollectionUtils.isNotEmpty(product.getObj().getVariants()))
                    .flatMap(product -> product.getObj().getVariants().stream()).filter(Objects::nonNull)
                    .flatMap(variant -> {
                        if (Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getCompatibleProducts())) {

                            List<String> productIds = variant.getAttributes().getCompatibleProducts().stream().filter(Objects::nonNull)
                                    .filter(compatProd -> CollectionUtils.isNotEmpty(compatProd.getProducts()))
                                    .flatMap(compatProd -> compatProd.getProducts().stream()).filter(Objects::nonNull)
                                    .map(prod -> prod.getId())
                                    .collect(Collectors.toList());

                            CTProductResponse ctProductResponse = getProductResponsefromCT(productIds);

                            return getCompatibleRemoteProducts(cartOffersList, offerRequestWrapper, ctProductResponse, offer, variant).stream();
                        } else {
                            return Stream.of(offer);
                        }
                    })
                    .count();

        }
        return compatibleRemoteOfferCount == 1;
    }

    private Set<CTOffer> getCompatibleRemoteProducts(List<CTOffer> cartOffersList, OfferRequestWrapper offerRequestWrapper, CTProductResponse ctProductResponse, CTOffer ctOffer, Variant variant) {
        Set<CTOffer> ctOffers = new HashSet<>();
        if (Objects.nonNull(variant)
                && Objects.nonNull(variant.getAttributes())
                && CollectionUtils.isNotEmpty(variant.getAttributes().getCompatibleProducts())) {
            variant.getAttributes().getCompatibleProducts().stream().filter(Objects::nonNull)
                    .filter(compatProd -> CollectionUtils.isNotEmpty(compatProd.getProducts()))
                    .forEach(compatProd -> compatProd.getProducts().stream().filter(Objects::nonNull)
                            .map(prod -> getProductObjectFromResponse(ctProductResponse, prod))
                            .filter(productObj ->
                                    CollectionUtils.isNotEmpty(productObj)
                                            && Objects.nonNull(productObj.get(0))
                                            && CollectionUtils.isNotEmpty(productObj.get(0).getVariants()))
                            .flatMap(productObj -> productObj.get(0).getVariants().stream())
                            .filter(Objects::nonNull)
                            .filter(prodVariant -> CollectionUtils.isNotEmpty(prodVariant.getAttributes().getBillingParams()))
                            .flatMap(prodVariant -> prodVariant.getAttributes().getBillingParams().stream().filter(Objects::nonNull))
                            .forEach(billingParam -> {
                                if (StringUtils.isNotEmpty(billingParam.getValue())) {
                                    if (Objects.isNull(offerRequestWrapper.getOfferRequest().getCartContext())) {
                                        offerRequestWrapper.getCtOfferRequest().getCustomerContext().get(0).getProducts().stream().filter(Objects::nonNull)
                                                .forEach(reqProduct -> ctOffers.addAll(getCompatibleRemoteOnCustContext(reqProduct, compatProd, ctOffer, billingParam)));
                                    } else {
                                        offerRequestWrapper.getCtOfferRequest().getCustomerContext().get(0).getProducts().stream().filter(Objects::nonNull)
                                                .forEach(reqProduct -> ctOffers.addAll(getCompatibleRemoteOnCustContext(reqProduct, compatProd, ctOffer, billingParam)));
                                        ctOffers.addAll(getCompatibleRemoteOnCartOffer(cartOffersList, ctOffer, billingParam));
                                    }
                                }
                            })
                    );
        }
        return ctOffers;
    }

    private Set<CTOffer> getCompatibleRemoteOnCustContext(Product reqProduct, ProductWrapper compatProd, CTOffer ctOffer, GenericNameValueBase billingParam) {
        Set<CTOffer> ctOffers = new HashSet<>();
        if (StringUtils.isNotEmpty(reqProduct.getComponentCode()) && reqProduct.getComponentCode().equalsIgnoreCase(billingParam.getValue())) {
            if (StringUtils.isNotEmpty(compatProd.getConstraints().getModelNos())) {
                List<String> modelNos = Arrays.asList(compatProd.getConstraints().getModelNos().split(",", -1));
                if (CollectionUtils.isNotEmpty(modelNos)) {
                    modelNos.forEach(number -> {
                        if (number.equalsIgnoreCase(reqProduct.getModelNumber())) {
                            ctOffers.add(ctOffer);
                        }
                    });
                }
            } else {
                ctOffers.add(ctOffer);
            }
        }
        return ctOffers;
    }

    private Set<CTOffer> getCompatibleRemoteOnCartOffer(List<CTOffer> cartOffersList, CTOffer offer, GenericNameValueBase billingParam) {
        Set<CTOffer> ctOffers = new HashSet<>();
        if (CollectionUtils.isNotEmpty(cartOffersList)) {
            cartOffersList.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                ProductObj productObj = util.getProductObjFromOffer(ctOffer);
                if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())) {
                    productObj.getVariants().stream().filter(Objects::nonNull)
                            .filter(variant -> CollectionUtils.isNotEmpty(variant.getAttributes().getBillingParams()))
                            .flatMap(variant -> variant.getAttributes().getBillingParams().stream().filter(Objects::nonNull))
                            .forEach(compCode -> {
                                if (StringUtils.isNotEmpty(compCode.getValue())
                                        && compCode.getValue().equalsIgnoreCase(billingParam.getValue())) {
                                    ctOffers.add(offer);
                                }
                            });
                }
            });
        }
        return ctOffers;
    }

    /**
     * Filter accessories based on remoteReplacementType in customerContext
     *
     * @param ctOfferRequest the CT offer request
     * @param productObj     the product object
     * @return boolean true if not valid
     */
    private boolean isNotValidBasedOnReplacementType(CTOfferRequest ctOfferRequest, ProductObj productObj) {
        if (Objects.nonNull(ctOfferRequest) && CollectionUtils.isNotEmpty(ctOfferRequest.getCustomerContext())
                && Objects.nonNull(ctOfferRequest.getCustomerContext().get(0))
                && StringUtils.isNotBlank(ctOfferRequest.getCustomerContext().get(0).getRemoteReplacementType())) {
            // If remoteReplacementType present in request
            if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                    && Objects.nonNull(productObj.getVariants().get(0))
                    && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                    && !ctOfferRequest.getCustomerContext().get(0).getRemoteReplacementType().equalsIgnoreCase(
                    productObj.getVariants().get(0).getAttributes().getRemoteReplacementType())) {
                // Remove offers with non-matching remoteReplacementType
                return true;
            }

        } else {
            // If remoteReplacementType not present in request
            if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                    && Objects.nonNull(productObj.getVariants().get(0))
                    && Objects.nonNull(productObj.getVariants().get(0).getAttributes()) && StringUtils.isNotBlank(
                    productObj.getVariants().get(0).getAttributes().getRemoteReplacementType())) {
                // Remove offers with non-empty remoteReplacementType
                return Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                        && Objects.nonNull(productObj.getVariants().get(0))
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes()) && StringUtils.isNotBlank(
                        productObj.getVariants().get(0).getAttributes().getRemoteReplacementType());
            }
        }
        return false;
    }

    private boolean isProductInCart(Product product, OfferRequestWrapper offerRequestWrapper) {
        Optional<Product> matchingObject = Optional.empty();
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext()) &&
                CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts())
                && Objects.nonNull(product)) {
            if (StringUtils.isNotBlank(product.getBillingProductCode())) {
                matchingObject = offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts().stream().filter(Objects::nonNull).
                        filter(p -> p.getBillingProductCode().equalsIgnoreCase(product.getBillingProductCode())).
                        findFirst();
            }
            if (StringUtils.isNotBlank(product.getPricePlanCode())) {
                if (StringUtils.isNotBlank(product.getPickCode())) {
                    matchingObject = offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts().stream().filter(Objects::nonNull).
                            filter(p -> p.getPricePlanCode().equalsIgnoreCase(product.getPricePlanCode()) &&
                                    p.getPickCode().equalsIgnoreCase(product.getPickCode())).
                            findFirst();
                } else {
                    matchingObject = offerRequestWrapper.getOfferRequest().getCartContext().getCartProducts().stream().filter(Objects::nonNull).
                            filter(p -> p.getPricePlanCode().equalsIgnoreCase(product.getPricePlanCode())).
                            findFirst();
                }
            }
        }
        return matchingObject.isPresent();

    }

    /**
     * This method applies filters on devices and fees
     *
     * @param ctOfferResponse        the CT offer response
     * @param offerRequestWrapper    the offer request wrapper
     * @param compatibleReceiversMap map of compatible receivers
     */
    private void applyVideoDeviceFeeFilters(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper, Map<String, String> compatibleReceiversMap) {

        if (offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_DEVICE)
                && Objects.nonNull(compatibleReceiversMap) && !compatibleReceiversMap.isEmpty()) {

            List<CTOffer> finalOffers = new ArrayList<>();
            List<CTOffer> videoDeviceOffers = util.filterOffersByProductType(ctOfferResponse.getOffers(), Constants.VIDEO_DEVICE);
            List<CTOffer> hdAccessFeeOffers = util.filterFeeOffersByFeeType(ctOfferResponse.getOffers(), Constants.HD_ACCESS_FEE);
            List<String> compatibleReceiversInFee = new ArrayList<>();

            //get the tv access fee associated based on the service order date from request
            CTOffer associatedFee = getFeeOfferBasedOnServiceDate(hdAccessFeeOffers, offerRequestWrapper);

            if (Objects.nonNull(associatedFee) || !util.isSTMSRequest(offerRequestWrapper)) {
                //get the compatible products of fee
                compatibleReceiversInFee.addAll(getCompatibleProducts(util.getProductObjFromOffer(associatedFee)).keySet());

                //filter the compatibleReceiversMap based on compatible products of fee
                compatibleReceiversMap.entrySet().removeIf(entry -> !compatibleReceiversInFee.contains(entry.getKey()));
            }

            //filter the video-device offers based on final compatibleReceiversMap
            videoDeviceOffers.removeIf(ctOffer -> Objects.nonNull(ctOffer)
                    && !compatibleReceiversMap.keySet().contains(util.getProductIdFromOffer(ctOffer)));

            //associate the tv access fee and allowed action to the video-device offer
            videoDeviceOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(associatedFee)) {
                    ctOffer.getAttributes().setAssociatedFee(associatedFee.getCode());
                }
                setAllowedAction(ctOffer, compatibleReceiversMap, offerRequestWrapper);
            });
            finalOffers.addAll(videoDeviceOffers);

            if (Objects.nonNull(associatedFee)) {
                finalOffers.add(associatedFee);
            }

            ctOfferResponse.getOffers().removeIf(ctOffer -> Objects.nonNull(ctOffer)
                    && Objects.nonNull(ctOffer.getAttributes())
                    && (Constants.VIDEO_DEVICE.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())
                    || Constants.FEE.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())));
            ctOfferResponse.getOffers().addAll(finalOffers);
        }
    }

    /**
     * This method sets the allowed action
     *
     * @param ctOffer                the CT offer
     * @param compatibleReceiversMap map of compatible receivers
     * @param offerRequestWrapper    the offer request wrapper
     */
    private void setAllowedAction(CTOffer ctOffer, Map<String, String> compatibleReceiversMap, OfferRequestWrapper offerRequestWrapper) {
        ProductObj productObj = util.getProductObjFromOffer(ctOffer);

        if (Objects.nonNull(productObj) && Objects.nonNull(compatibleReceiversMap) && !compatibleReceiversMap.isEmpty()) {
            //setting the allowed action
            ctOffer.getAttributes().setAllowedAction(compatibleReceiversMap.get(productObj.getId()));
        }

        if (Objects.nonNull(productObj)
                && CollectionUtils.isNotEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes())) {

            int maxOrderLimit = productObj.getVariants().get(0).getAttributes().getMaxOrderLimit();
            int maxAccountLimit = productObj.getVariants().get(0).getAttributes().getMaxAccountLimit();

            ctOffer.getAttributes().setReceiverOrderLimit(productObj.getVariants().get(0).getAttributes().getMaxOrderLimit());
            ctOffer.getAttributes().setReceiverAccountLimit(productObj.getVariants().get(0).getAttributes().getMaxAccountLimit());

            Integer cartQty = getCartQuantity(ctOffer, offerRequestWrapper);
            Integer onAccountQty = getOnAccountQuantity(productObj, offerRequestWrapper);
            Integer sum = cartQty + onAccountQty;

            if (cartQty >= maxOrderLimit || onAccountQty >= maxAccountLimit || sum >= maxAccountLimit) {
                //overriding the allowed action to callToOrder if :-
                //offer is in cart and quantity reached the maxOrderLimit
                //on account product count reached the maxAccountLimit
                //sum of cart offer quantity and on account product count reached the maxAccountLimit
                ctOffer.getAttributes().setAllowedAction(Constants.CALL_TO_ORDER);
            }

        }
    }

    /**
     * This method gets the cart offer quantity
     *
     * @param ctOffer             the CT offer
     * @param offerRequestWrapper the offer request wrapper
     * @return Integer the cart quantity
     */
    private Integer getCartQuantity(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        List<Integer> cartQty = new ArrayList<>();
        if (Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartContext().getCartOffers())) {
            offerRequestWrapper.getOfferRequest().getCartContext().getCartOffers().stream().filter(Objects::nonNull).forEach(cartOffer -> {
                if (cartOffer.getOfferCode().equalsIgnoreCase(ctOffer.getCode())) {
                    cartQty.add(cartOffer.getQuantity());
                }
            });
        }
        return cartQty.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * This method gets the on account quantity
     *
     * @param productObj          the product object
     * @param offerRequestWrapper the offer request wrapper
     * @return Integer the on account quantity
     */
    private Integer getOnAccountQuantity(ProductObj productObj, OfferRequestWrapper offerRequestWrapper) {
        Integer onAccountQty = 0;
        if (Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts())) {
            List<ProductInfo> products = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts();

            onAccountQty = (int) products.stream().filter(Objects::nonNull)
                    .filter(product -> (StringUtils.isNotBlank(product.getComponentCode()) && Constants.VIDEO_DEVICE.equals(product.getProductType())
                            && product.getComponentCode().equalsIgnoreCase(getBillingParamValue(productObj.getVariants().get(0).getAttributes().getBillingParams(), Constants.STB_NAME)))
                            || (StringUtils.isNotBlank(product.getManufacturer()) && StringUtils.isNotBlank(product.getModelNumber())
                            && MapUtils.isNotEmpty(getModels(productObj))
                            && getModels(productObj).keySet().contains(product.getManufacturer())
                            && getModels(productObj).get(product.getManufacturer()).contains(product.getModelNumber())))
                    .count();

        }
        return onAccountQty;
    }

    /**
     * This method gets the tv access fee or tivo fee based on service order date in request
     *
     * @param hdAccessFeeOffers   list of fee offers
     * @param offerRequestWrapper the offer request wrapper
     * @return CTOffer the fee offer
     */
    public CTOffer getFeeOfferBasedOnServiceDate(List<CTOffer> hdAccessFeeOffers, OfferRequestWrapper offerRequestWrapper) {
        List<CTOffer> ctOffers = new ArrayList<>();
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite())
                && StringUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getServiceOrderDate())
                && CollectionUtils.isNotEmpty(hdAccessFeeOffers)) {
            hdAccessFeeOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                ProductObj productObj = util.getProductObjFromOffer(ctOffer);
                if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                        && Objects.nonNull(productObj.getVariants().get(0))
                        && Objects.nonNull(productObj.getVariants().get(0).getAttributes())
                        && ((Objects.nonNull(productObj.getVariants().get(0).getAttributes().getReceiverLineType())
                        && Constants.LINE_TYPES.contains(productObj.getVariants().get(0).getAttributes().getReceiverLineType())
                        && Constants.HD_ACCESS_FEE.equalsIgnoreCase(productObj.getVariants().get(0).getAttributes().getFeeType()))
                        || Constants.TIVO_FEE.equalsIgnoreCase(productObj.getVariants().get(0).getAttributes().getFeeType()))) {
                    Attributes attributes = productObj.getVariants().get(0).getAttributes();
                    String fromDate = OffersUtils.getFormattedDate(attributes.getOrderValidFromDate());
                    String toDate = OffersUtils.getFormattedDate(attributes.getOrderValidToDate());
                    String serviceOrderDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getServiceOrderDate();

                    Map<String, String> serviceOrderDateString = new HashMap<>();
                    serviceOrderDateString.put(Constants.PROMO_START_DATE, serviceOrderDate);
                    Map<String, Date> serviceDate = OffersUtils.validateDateFormat(serviceOrderDateString);

                    if (OffersUtils.validateActiveDates(fromDate, toDate, serviceDate.get(Constants.PROMO_START_DATE))) {
                        ctOffers.add(ctOffer);
                    }
                }
            });
        }
        if (CollectionUtils.isNotEmpty(ctOffers)) {
            return ctOffers.get(0);
        }
        return null;
    }

    /**
     * This method is to get the product obj based on component code
     *
     * @param products      list of product objects
     * @param componentCode the component code
     * @return ProductObj the matching product object
     */
    private ProductObj getProductObjByComponentCode(List<ProductObj> products, String componentCode) {
        if (Objects.nonNull(products)) {
            Optional<ProductObj> matchingObject = products.stream().filter(Objects::nonNull).
                    filter(p -> componentCode.equalsIgnoreCase(getBillingParamValue(p.getVariants().get(0).getAttributes().getBillingParams(), Constants.STB_NAME))).
                    findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            }
        }
        return null;
    }

    /**
     * This method is to get the product obj based on manufacturer and model number
     *
     * @param products     list of product objects
     * @param manufacturer the manufacturer
     * @param model        the model number
     * @return ProductObj the matching product object
     */
    private ProductObj getProductObjByManufacturerModel(List<ProductObj> products, String manufacturer, String model) {
        if (Objects.nonNull(products)) {
            Optional<ProductObj> matchingObject = products.stream().filter(Objects::nonNull).
                    filter(product -> MapUtils.isNotEmpty(getModels(product))
                            && getModels(product).keySet().contains(manufacturer)
                            && getModels(product).get(manufacturer).contains(model)).
                    findFirst();
            if (matchingObject.isPresent()) {
                return matchingObject.get();
            }
        }
        return null;
    }

    /**
     * This method returns the map of manufacturer-model numbers of a product
     *
     * @param productObj the product object
     * @return Map<String, List < String>> map of model numbers
     */
    private Map<String, List<String>> getModels(ProductObj productObj) {
        Map<String, List<String>> models = new HashMap<>();
        GenericLocaleBase modelNumbers = productObj.getVariants().get(0).getAttributes().getModelNumbers();
        if (Objects.nonNull(modelNumbers) && Objects.nonNull(modelNumbers.getEn())) {
            List<String> allModels = Stream.of(modelNumbers.getEn().split(Pattern.quote("||"))).map(String::trim).collect(Collectors.toList());
            allModels.stream().filter(Objects::nonNull).forEach(model -> {
                List<String> manModel = Stream.of(model.split(":")).map(String::trim).collect(Collectors.toList());
                List<String> modelNos = Stream.of(manModel.get(1).split(",")).map(String::trim).collect(Collectors.toList());
                models.put(manModel.get(0), modelNos);
            });
        }
        return models;
    }

    /**
     * This method is used to get the compatible products of a specific product.
     *
     * @param productObj the product object
     * @return Map<String, String> map of compatible products
     */
    private Map<String, String> getCompatibleProducts(ProductObj productObj) {

        Map<String, String> compatibleProductsIds = new HashMap<>();

        if (Objects.nonNull(productObj) && CollectionUtils.isNotEmpty(productObj.getVariants())
                && Objects.nonNull(productObj.getVariants().get(0))
                && Objects.nonNull(productObj.getVariants().get(0).getAttributes()) &&
                CollectionUtils.isNotEmpty(productObj.getVariants().get(0).getAttributes().getCompatibleProducts())) {

            // Getting compatibleProductsList
            List<ProductWrapper> compatibleProductsList = productObj.getVariants().get(0).getAttributes()
                    .getCompatibleProducts();

            compatibleProductsList.stream().filter(Objects::nonNull).forEach(compatibleProducts -> {

                if (CollectionUtils.isNotEmpty(compatibleProducts.getProducts()) && Objects.nonNull(compatibleProducts.getConstraints())) {
                    compatibleProducts.getProducts().stream().filter(Objects::nonNull)
                            .forEach(compatibleProduct -> {
                                if (Objects.nonNull(compatibleProduct.getId())) {
                                    compatibleProductsIds.put(compatibleProduct.getId(), compatibleProducts.getConstraints().getAllowedAction());
                                }
                            });
                }

            });
        }
        return compatibleProductsIds;
    }

    private Boolean isNotValidSecondChanceOffer(CTOffer ctoffer, OfferRequestWrapper offerRequestWrapper) {
        String activationDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getActivationDate();
        String eligibilityTenure = ctoffer.getAttributes().getMaxCustomerEligibilityTenureInDays();
        boolean isValidSecondChanceOffer = false;

        if (Objects.nonNull(eligibilityTenure) && StringUtils.isNumeric(eligibilityTenure)) {
            if (StringUtils.isNotBlank(activationDate)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT);
                LocalDate accountActiveDate = LocalDate.parse(activationDate, formatter);
                LocalDate today = LocalDate.now();
                long totalDays = ChronoUnit.DAYS.between(accountActiveDate, today);
                isValidSecondChanceOffer = (totalDays > Integer.parseInt(eligibilityTenure));
            } else {
                return true;
            }
        }
        return isValidSecondChanceOffer;
    }

    /**
     * Return true, if tenure rule is not met.
     * Return false, if tenure rule is empty in CT.
     *
     * @param ctoffer             the CT offer
     * @param offerRequestWrapper the offer request wrapper
     * @return Boolean true if not valid
     */
    private Boolean isNotValidBasedOnTenureRule(CTOffer ctoffer, OfferRequestWrapper offerRequestWrapper) {
        String eligibilityTenure = ctoffer.getAttributes().getMinCustomerEligibilityTenureInDays();

        if (Objects.nonNull(eligibilityTenure) && StringUtils.isNumeric(eligibilityTenure)) {
            String activationDate = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()
                    .getActivationDate();

            if (StringUtils.isNotBlank(activationDate)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DTVN_CPC_DATE_FORMAT);
                LocalDate actDate = LocalDate.parse(activationDate, formatter);
                LocalDate today = LocalDate.now();
                long diff = ChronoUnit.DAYS.between(actDate, today);
                return diff < Integer.parseInt(eligibilityTenure);
            } else {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove all BP offers from the response if LCC Bcode is on account
     *
     * @param ctOfferResponse  the CT offer response
     * @param existingProducts list of existing products
     */
    private void removeBPOffersWhenLCC(CTOfferResponse ctOfferResponse, List<String> existingProducts) {
        if (existingProducts.stream().anyMatch(Constants.LCC_BCODE::contains)) {
            ctOfferResponse.getOffers().removeIf(
                    offer -> Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType()));
        }
    }

    private Boolean isNotValidBasedOnCCIDAndSalesChannel(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {

        boolean isNotValidOffer = false;
        if (offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.IPOR)) {
            if (StringUtils.isEmpty(offerRequestWrapper.getOfferRequest().getContentChannelId())) {
                isNotValidOffer = true;
            } else {
                ProductObj obj = util.getProductObjFromOffer(ctOffer);
                isNotValidOffer = !offerRequestWrapper.getOfferRequest().getContentChannelId()
                        .equalsIgnoreCase(obj.getVariants().get(0).getAttributes().getContentChannelId());
            }
        }
        return isNotValidOffer;
    }

    private List<CTOffer> getOffersWithADEOffersLite(CTOfferResponse ctOfferResponse) {

        List<CTOffer> offersList = new ArrayList<>();
        ctOfferResponse.getOffers().forEach(offer -> {
            CTOffer ctOffer = new CTOffer();
            ctOffer.setCode(offer.getCode());
            ctOffer.setDescription(offer.getDescription());
            OfferAttributes attr = new OfferAttributes();
            attr.setDisplayName(offer.getAttributes().getDisplayName());
            attr.setEnablerSalesOfferId(offer.getAttributes().getEnablerSalesOfferId());
            attr.setOfferPrice(offer.getAttributes().getOfferPrice());
            ctOffer.setAttributes(attr);
            offersList.add(ctOffer);

        });

        return offersList;

    }
}
