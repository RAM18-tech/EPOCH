package com.dtv.dcp.epoch.processor.ott.sales;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.common.CpopConstants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.RxJavaHelper;
import com.dtv.dcp.epoch.util.SalesVideoAddonProcessor;

@Component
public class OttSalesOffersProcessor {

    private static Logger log = LoggerFactory.getLogger(OttSalesOffersProcessor.class);

    @Autowired
    SalesVideoPlanProcessor salesVideoPlanProcessor;

    @Autowired
    SalesVideoAddonProcessor salesVideoAddonProcessor;

    @Autowired
    SalesEquipmentProcessor salesEquipmentProcessor;

    @Autowired
    OttSalesOffersProcessorHelper ottSalesOffersProcessorHelper;

    @Autowired
    CPOPProductsHelper cpopProductsHelper;

    @Autowired
    CPOPBenefitsHelper cpopBenefitsHelper;

    @Autowired
    DtvnSalesRSNFeeOffersProcessor dtvnSalesRSNFeeOffersProcessor;

    @Autowired
    ProtectionPlanProcessor protectionPlanProcessor;
    @Autowired
    private FeatureManagerHelper featureHelper;
    @Autowired
    OffersUtils offersUtils;
    @Autowired
    RewardProcessor rewardProcessor;
    @Autowired
    CreditProcessor  creditProcessor;

    public CTOfferResponse getOffers(OfferRequestWrapper offerRequestWrapper) throws ServiceException {

        CTOfferResponse videoPlanOffers = null;
        CTOfferResponse videoAddonStandaloneOffer = null;
        CTOfferResponse videoAddonOffer = null;
        CTOfferResponse videoDeviceAndAccessoryOffer = null;
        CTOfferResponse feeOffer = null;
        CTOfferResponse protectionPlanOffer = null;
        CTOfferResponse rewardOffer = null;
        CTOfferResponse closingOffer = null;
        Integer count = 0;
        Integer total = 0;
        List<CTOffer> finalOfferList = new ArrayList<>();
        CTOfferResponse finalOfferResponse = new CTOfferResponse();
        Map<String, CTOfferResponse> offersMap = null;

        /**
         * pass the request as it is to CT and get the response . based on the offerProductTypes CPOPOFFERMS will perform the enterprise rules.
         *    Basically  bases of the offerProductTypes of response this class will call the respective  classes
         *    "video-addon","video-device","video-plan","video-accessory","fee" and will construct  the final response of offers
         *
         */

        offersMap = getOffersAsynchronously(offerRequestWrapper );
        long startTimes = System.currentTimeMillis();
        videoPlanOffers = offersMap.get(Constants.VIDEO_PLAN);
        videoAddonStandaloneOffer = offersMap.get(Constants.STANDALONE);
        videoAddonOffer = offersMap.get(Constants.VIDEO_ADDON);
        protectionPlanOffer = offersMap.get(Constants.PROTECTION_PLAN);
        rewardOffer = offersMap.get(Constants.REWARD);
        feeOffer = offersMap.get(Constants.FEE);
        videoDeviceAndAccessoryOffer = offersMap.get(Constants.VIDEO_DEVICE);
        closingOffer= offersMap.get(Constants.CREDIT);
       
        if (videoPlanOffers != null && CollectionUtils.isNotEmpty(videoPlanOffers.getOffers())) {
            dtvnSalesRSNFeeOffersProcessor.populateRSNFeeDetails(offerRequestWrapper, videoPlanOffers.getOffers(),
                    feeOffer != null && CollectionUtils.isNotEmpty(feeOffer.getOffers()) ? feeOffer.getOffers() : null);
            log.info("Number of Video Plans : {}", videoPlanOffers.getOffers().size());
        }

        //Suppressing all the offers if no video plans found for Acquisition non-cart mode call
        List<String> salesChannelList = offersUtils.getConfigList(Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_SALES_CHANNEL_TO_SUPPRESS_NON_BP);
        if (salesChannelList != null && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getSalesChannel()).isPresent()
                && CollectionUtils.containsAny(salesChannelList, offerRequestWrapper.getOfferRequest().getSalesChannel()) 
        		&& Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext()).isEmpty()
                && offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)) {
                if (videoPlanOffers == null || CollectionUtils.isEmpty(videoPlanOffers.getOffers())) {
                    finalOfferResponse.setOffers(new ArrayList<>());
                    log.info("Non-Cart Mode: No video-plan offers found. Returning an empty response list.");
                    finalOfferResponse.setCount(0);
                    finalOfferResponse.setTotal(0);
                    return finalOfferResponse;
                }
        }
        
        // final offerResponse
        if (videoPlanOffers != null && CollectionUtils.isNotEmpty(videoPlanOffers.getOffers())) {
            finalOfferList.addAll(videoPlanOffers.getOffers());

            // Run DTVN rules for base  offer only
            // finalOfferList = ottSalesOffersProcessorHelper.dtvNowRulesProcessing(finalOfferList, offerRequestWrapper);
            finalOfferList = ottSalesOffersProcessorHelper.filterMaxPlusForOpus(finalOfferList, offerRequestWrapper);
            ottSalesOffersProcessorHelper.populateCompatibleProducts(finalOfferList);
        }
        if (videoAddonStandaloneOffer != null && CollectionUtils.isNotEmpty(videoAddonStandaloneOffer.getOffers())) {
            finalOfferList.addAll(videoAddonStandaloneOffer.getOffers());
            // Run DTVN rules for standalone base  offer only
            finalOfferList = ottSalesOffersProcessorHelper.dtvNowRulesProcessing(finalOfferList, offerRequestWrapper);
            ottSalesOffersProcessorHelper.populateCompatibleProducts(finalOfferList);
        }

        if (videoAddonOffer != null && CollectionUtils.isNotEmpty(videoAddonOffer.getOffers())) {
            List<CTOffer> filteredOffers = videoAddonOffer.getOffers();
            if (featureHelper.isEnabled(Constants.FEATURE_FLAG_CONFLICTING_OFFER_NEW_LOGIC)) {
                filteredOffers = offersUtils.filterConflictingOfferWhenAllowConflictingOffersPresent(filteredOffers, offerRequestWrapper);
            }
            ottSalesOffersProcessorHelper.filterVirtualQualifierPromos(videoAddonOffer.getOffers());

            if (offerRequestWrapper.getOfferRequest().isMigrationIndicator() || featureHelper.isEnabled(CpopConstants.EPOCH_MIGRATION_OFFER_ENABLED)) {
                //Excluding Virtual offers from response if IPTV Migration in context
                filteredOffers = ottSalesOffersProcessorHelper.excludeVirtualOffers(videoAddonOffer.getOffers());
            }
            filteredOffers = ottSalesOffersProcessorHelper.handleIneligibleOffersAndDelayedMessages(filteredOffers, offerRequestWrapper);
            finalOfferList.addAll(filteredOffers);
        }


        if (videoDeviceAndAccessoryOffer != null && CollectionUtils.isNotEmpty(videoDeviceAndAccessoryOffer.getOffers())) {
            ottSalesOffersProcessorHelper.filterExpiredInstallmentOptions(videoDeviceAndAccessoryOffer.getOffers());
            finalOfferList.addAll(videoDeviceAndAccessoryOffer.getOffers());
        }
        if (feeOffer != null && CollectionUtils.isNotEmpty(feeOffer.getOffers())
                && offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.FEE)) {
            finalOfferList.addAll(feeOffer.getOffers());
        }

        if (protectionPlanOffer != null && CollectionUtils.isNotEmpty(protectionPlanOffer.getOffers())) {
            finalOfferList.addAll(protectionPlanOffer.getOffers());
        }

        if (rewardOffer != null && CollectionUtils.isNotEmpty(rewardOffer.getOffers())) {
            finalOfferList.addAll(rewardOffer.getOffers());
        }
        if (closingOffer != null && CollectionUtils.isNotEmpty(closingOffer.getOffers())) {
            finalOfferList.addAll(closingOffer.getOffers());
        }

        //Filter non-contracted offers if ATT TV NOW flag is OFF
        if (featureHelper.isEnabled(CpopConstants.EPOCH_OFFERSMS_DTVNOW_SHUTDOWN)) {
            // Filter out offers that have a contract indicator other than the specified values
            finalOfferList = finalOfferList.stream().filter(offer -> (Optional
                            .ofNullable(offer.getAttributes().getContractIndicator()).isPresent()
                            && (offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.CONTRACT) ||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.EDSP_STRING) ||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZ_STRING) ||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZBYOD_STRING) ||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.TAZCONTRACT_STRING)||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.ROAD_RUNNER)||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.FRONTPORCH_STRING)||
                            offer.getAttributes().getContractIndicator().equalsIgnoreCase(Constants.GENRE))))
                    .collect(Collectors.toList());
        }
        offerRequestWrapper.setNoDeviceFrameworkEnabled(featureHelper.isEnabled(Constants.FEATURE_FLAG_NO_DEVICE_FRAMEWORK_ENABLED));
        if(offerRequestWrapper.isNoDeviceFrameworkEnabled()
                && offersUtils.isBYODFlow(offerRequestWrapper)
                && CollectionUtils.isNotEmpty(finalOfferList)) {
            offersUtils.updateBenefitsBasedOnConditions(offerRequestWrapper, finalOfferList);
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartOffers())) {
            finalOfferList = ottSalesOffersProcessorHelper.filterOfferBasedOnCartOfferQuantity(finalOfferList, offerRequestWrapper);
        }
        offersUtils.filterOfferBasedOnCartContextWithConflictingOffers(finalOfferList, offerRequestWrapper);
        //NonStackable Functionality in Acquisition flow with ixp flag
        if (featureHelper.isEnabled(Constants.FEATURE_NON_STACKABLE_RULE_ENABLED)) {
            offersUtils.filterOffersBasedOnNonStackableCartOffers(finalOfferList, offerRequestWrapper);
        }
        offersUtils.filterOfferBasedOnZipOrDMA(finalOfferList, offerRequestWrapper);
        count = count + finalOfferList.size();
        total = total + finalOfferList.size();

        finalOfferResponse.setOffers(finalOfferList);
        finalOfferResponse.setCount(count);
        finalOfferResponse.setTotal(total);

        if(null != offerRequestWrapper.getFlow() && !offerRequestWrapper.getFlow().equals("validateCart")){
            finalOfferResponse = cpopProductsHelper.filterInvalidPrices(finalOfferResponse, offerRequestWrapper);
            finalOfferResponse = cpopBenefitsHelper.filterInvalidBenefits(finalOfferResponse, offerRequestWrapper);
            calculateOfferPriceOnIXPFlag(offerRequestWrapper, finalOfferList);
        }

        // setting the additional in case it is part of response
        // paging not setting for sales

        if (videoPlanOffers != null && videoPlanOffers.getAdditionals() != null) {
            finalOfferResponse.setAdditionals(videoPlanOffers.getAdditionals());
        } else {
            if (videoAddonStandaloneOffer != null && videoAddonStandaloneOffer.getAdditionals() != null) {
                finalOfferResponse.setAdditionals(videoAddonStandaloneOffer.getAdditionals());
            }
        }

        log.info("TOTAL_TIME_FOR_POST_PROCESSING_RESPONSE-[{}]",System.currentTimeMillis()- startTimes );
        return finalOfferResponse;
    }


    private void calculateOfferPriceOnIXPFlag(OfferRequestWrapper offerRequestWrapper, List<CTOffer> finalOfferList) {
//		if (featureHelper.isEnabled(Constants.FEATURE_TOGGLE_IXP_OFFERPRICE_ENABLED)) {
//			if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream().anyMatch(
//					s -> Constants.OEM_IAPFIRETV.equalsIgnoreCase(s) || Constants.OEM_IAPROKUTV.equalsIgnoreCase(s))) {
//				log.info("Get offers Based on the IXP flag is TRUE and sales channel is Fire TV or Roku TV");
//				OffersUtils.calculateBestPrice(offerRequestWrapper, finalOfferList);
//			} else {
//				log.info("Get offers Based on the IXP flag is TRUE and Sales channel expect opus");
//				if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
//						.anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
//					ottSalesOffersProcessorHelper.calculateBestPrice(offerRequestWrapper, finalOfferList);
//				}
//			}
//		} else {
        if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                .anyMatch(s -> Constants.DIRECT_INTEGRATION_PARTNER.equalsIgnoreCase(s)
                        && featureHelper.isEnabled(Constants.FEATURE_TOGGLE_DIP_OFFER_PRICE_ENABLED))) {
            log.info("Get offers Based on the IXP flag is FALSE Which returns new Offer prices");
            OffersUtils.calculateBestPrice(offerRequestWrapper, finalOfferList);
        } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                .anyMatch(s -> Constants.INDIRECT_PARTNER.equalsIgnoreCase(s) || Constants.ONLINE.equalsIgnoreCase(s)
                        || (Constants.DIRECT_INTEGRATION_PARTNER.equalsIgnoreCase(s)
                        && !featureHelper.isEnabled(Constants.FEATURE_TOGGLE_DIP_OFFER_PRICE_ENABLED)))) {
            ottSalesOffersProcessorHelper.calculateBestPrice(offerRequestWrapper, finalOfferList);
        } else {
            log.info("Get offers Based on the IXP flag is FALSE Which returns new Offer prices");
            OffersUtils.calculateBestPrice(offerRequestWrapper, finalOfferList);
        }
//		}
    }

    /**
     *
     * @param offerRequestWrapper
     * @return
     */
    public Map<String, CTOfferResponse> getOffersAsynchronously(OfferRequestWrapper offerRequestWrapper) {


        boolean stanaloneOnlyReq = isStandaloneOffersOnlyRequest(offerRequestWrapper);

        log.info("Start of getOffersAsynchronously method..");

        List<Callable<?>> callableObjList = new ArrayList<>();
        // Capture MDC context
        Map<String, String> capturedMdcContext = MDC.getCopyOfContextMap();

        OfferRequestWrapper asyncCpopOffersRequest1 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest2 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest3 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest4 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest5 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest6 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest7 = new OfferRequestWrapper();
        OfferRequestWrapper asyncCpopOffersRequest8 = new OfferRequestWrapper();

        if (Objects.nonNull(offerRequestWrapper)) {
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest1);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest2);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest3);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest4);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest5);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest6);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest7);
            BeanUtils.copyProperties(offerRequestWrapper, asyncCpopOffersRequest8);
        }

        // Scenario 1- Base Offer(video plan) scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest1).isPresent() && Optional.ofNullable(asyncCpopOffersRequest1.getOfferRequest()).isPresent() && ((asyncCpopOffersRequest1.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_PLAN))) {
            log.info("Async Video Plan..");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> salesVideoPlanProcessor.getVideoPlanOffers(asyncCpopOffersRequest1)));
            //callableObjList.add(() ->   salesVideoPlanProcessor.getVideoPlanOffers(asyncCpopOffersRequest1));
        } else {
            callableObjList.add(() ->   new CTOfferResponse());
        }

        // Scenario 2- StandAlone Offer(video Addon ) scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest2).isPresent() && Optional.ofNullable(asyncCpopOffersRequest2.getOfferRequest()).isPresent() &&
                ((((asyncCpopOffersRequest2.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_ADDON)) &&
                        (Optional.ofNullable(asyncCpopOffersRequest2.getOfferRequest().getAddOnType()).isPresent() && asyncCpopOffersRequest2.getOfferRequest().getAddOnType().contains(Constants.STANDALONE_CAPS)) &&
                        (Optional.ofNullable(asyncCpopOffersRequest2.getOfferRequest().getPlanSubType()).isPresent() && asyncCpopOffersRequest2.getOfferRequest().getPlanSubType().contains(Constants.INTERNATIONAL)))
                        || isAllVideoAddOnsRequest(asyncCpopOffersRequest2))) {

            //Remove "International" from planSubType list so that its not called in Scenario 3 again.
            asyncCpopOffersRequest2.getOfferRequest().setPlanSubType(asyncCpopOffersRequest2.getOfferRequest().getPlanSubType().stream().filter(planSubType -> !planSubType.equalsIgnoreCase("International")).collect(Collectors.toList()));
            asyncCpopOffersRequest3.getOfferRequest().setPlanSubType(asyncCpopOffersRequest3.getOfferRequest().getPlanSubType().stream().filter(planSubType -> !planSubType.equalsIgnoreCase("International")).collect(Collectors.toList()));
            // for sales standalone offers call is made, which is not needed for contracted & EDSP flows now
            if (Optional.ofNullable(asyncCpopOffersRequest2.getOfferRequest().getContractIndicator()).isPresent() &&
                    CollectionUtils.isNotEmpty(asyncCpopOffersRequest2.getOfferRequest().getContractIndicator()) &&
                    asyncCpopOffersRequest2.getOfferRequest().getContractIndicator().stream().anyMatch(Predicate.isEqual(Constants.NONCONTRACT))) {
                log.info("Async Standalone..");
                callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                        () -> salesVideoAddonProcessor.getVideoAddonStandalonOffers(asyncCpopOffersRequest2)));
                
                //callableObjList.add(() ->   salesVideoAddonProcessor.getVideoAddonStandalonOffers(asyncCpopOffersRequest2));
            } else {
                callableObjList.add(() ->   new CTOfferResponse());
            }
        } else {
            callableObjList.add(() ->   new CTOfferResponse());
        }

        // Scenario 3- Addon Offer(video Addon ) scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest3).isPresent() && Optional.ofNullable(asyncCpopOffersRequest3.getOfferRequest()).isPresent() &&
                ((asyncCpopOffersRequest3.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_ADDON) && !stanaloneOnlyReq)) {
            log.info("Async video-addon..");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> salesVideoAddonProcessor.getVideoAddonOffers(asyncCpopOffersRequest3)));
            //callableObjList.add(() ->   salesVideoAddonProcessor.getVideoAddonOffers(asyncCpopOffersRequest3));
        } else {
            callableObjList.add(() ->   new CTOfferResponse());
        }

        // Scenario 4- Equipment Offer(video Device and video-accessory  scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest4).isPresent() && Optional.ofNullable(asyncCpopOffersRequest4.getOfferRequest()).isPresent() &&
                (((asyncCpopOffersRequest4.getOfferRequest().getOfferProductType()).contains(Constants.VIDEO_DEVICE)) || asyncCpopOffersRequest4.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ACCESSORY))) {
            log.info("Async video-device..");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> salesEquipmentProcessor.getEquipmentOffers(asyncCpopOffersRequest4)));
            //callableObjList.add(() ->   salesEquipmentProcessor.getEquipmentOffers(asyncCpopOffersRequest4));
        } else {
            callableObjList.add(() ->   new CTOfferResponse());
        }


        // Scenario 5-Fee Offer(fee type)
        if (Optional.ofNullable(asyncCpopOffersRequest5).isPresent() && Optional.ofNullable(asyncCpopOffersRequest5.getOfferRequest()).isPresent() &&
                (asyncCpopOffersRequest5.getOfferRequest().getOfferProductType().contains(Constants.FEE)) ||
                (asyncCpopOffersRequest5.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_PLAN) &&
                        (asyncCpopOffersRequest5.getOfferRequest().getContractIndicator().contains(Constants.TAZ_STRING) ||
                                asyncCpopOffersRequest5.getOfferRequest().getContractIndicator().contains(Constants.TAZCONTRACT_STRING)))){
            log.info("Async video-fee started.");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> salesEquipmentProcessor.getFeeOffers(asyncCpopOffersRequest5)));
            //callableObjList.add(() ->   salesEquipmentProcessor.getFeeOffers(asyncCpopOffersRequest5));
            log.info("Async video-fee..");
        } else {
            callableObjList.add(() ->   new CTOfferResponse());
        }

        // Scenario 6- Protection-Plan offer scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest6).isPresent()
                && Optional.ofNullable(asyncCpopOffersRequest6.getOfferRequest()).isPresent() && asyncCpopOffersRequest6
                .getOfferRequest().getOfferProductType().contains(Constants.PROTECTION_PLAN)) {
            log.info("Async Protection-Plan started.");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> protectionPlanProcessor.getProtectionPlanOffers(asyncCpopOffersRequest6)));
            //callableObjList.add(() -> protectionPlanProcessor.getProtectionPlanOffers(asyncCpopOffersRequest6));
            log.info("Async Protection-Plan..");
        } else {
            callableObjList.add(() -> new CTOfferResponse());
        }
        // Scenario 7- Reward offer scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest7).isPresent()
                && Optional.ofNullable(asyncCpopOffersRequest7.getOfferRequest()).isPresent()
                && asyncCpopOffersRequest7.getOfferRequest().getOfferProductType().contains(Constants.REWARD)) {
            log.info("Async Reward started.");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> rewardProcessor.getRewardOffers(asyncCpopOffersRequest7)));
            //callableObjList.add(() -> rewardProcessor.getRewardOffers(asyncCpopOffersRequest7));
            log.info("Async Reward..");
        } else {
            callableObjList.add(() -> new CTOfferResponse());
        }

        // Scenario 8- Credit offer scenarios
        if (Optional.ofNullable(asyncCpopOffersRequest8).isPresent()
                && Optional.ofNullable(asyncCpopOffersRequest8.getOfferRequest()).isPresent()
                && asyncCpopOffersRequest8.getOfferRequest().getOfferProductType().contains(Constants.CREDIT)) {
            log.info("Async Credit started.");
            callableObjList.add(() -> OffersUtils.executeWithMdcContext(capturedMdcContext, 
                    () -> creditProcessor.getClosingOffers(asyncCpopOffersRequest8)));
            //callableObjList.add(() -> creditProcessor.getClosingOffers(asyncCpopOffersRequest8));
            log.info("Async Credit..");
        } else {
            callableObjList.add(() -> new CTOfferResponse());
        }

        Object[] responses = null;
        Map<String, CTOfferResponse> objMap = new HashMap<>();
        long startTimeInMillis = System.currentTimeMillis();
        responses = RxJavaHelper.callConcurrentlyGetResult(callableObjList.toArray(new Callable[callableObjList.size()]));
        long endTimeMillis = System.currentTimeMillis() - startTimeInMillis;
        log.info("EPOCH_OTT_SALES_GET_OFFERS_ASYNC-[{}]", (endTimeMillis));
        // log.info("Getting Async response: {}", responses);

        log.info("End of getOffersAsynchronously method..");
        return handleAsyncResponse(responses, offerRequestWrapper);
    }
    
   

    public Map<String, CTOfferResponse> handleAsyncResponse(Object[] responses, OfferRequestWrapper offerRequestWrapper) throws ServiceException {
        Map<String, CTOfferResponse> objMap = new HashMap<>();
        ServiceException serviceException = null;
        boolean anyFailure = false;
        try {

            if( responses[0] instanceof CTOfferResponse) {
                objMap.put(Constants.VIDEO_PLAN, (CTOfferResponse) responses[0]);
            } else {
                serviceException = (ServiceException) responses[0];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_PLAN, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[1] instanceof CTOfferResponse) {
                objMap.put(Constants.STANDALONE, (CTOfferResponse) responses[1]);
            } else {
                serviceException = (ServiceException) responses[1];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.STANDALONE, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[2] instanceof CTOfferResponse) {
                objMap.put(Constants.VIDEO_ADDON, (CTOfferResponse) responses[2]);
            } else {
                serviceException = (ServiceException) responses[2];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_ADDON, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[3] instanceof CTOfferResponse) {
                objMap.put(Constants.VIDEO_DEVICE, (CTOfferResponse) responses[3]);
            } else {
                serviceException = (ServiceException) responses[3];
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.VIDEO_DEVICE, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[4] instanceof CTOfferResponse) {
                objMap.put(Constants.FEE, (CTOfferResponse) responses[4]);
            } else {
                serviceException = (ServiceException) responses[4];
                log.error("serviceException {}", serviceException);
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.FEE, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }

            if( responses[5] instanceof CTOfferResponse) {
                objMap.put(Constants.PROTECTION_PLAN, (CTOfferResponse) responses[5]);
            } else {
                serviceException = (ServiceException) responses[5];
                log.error("serviceException {}", serviceException);
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.PROTECTION_PLAN, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[6] instanceof CTOfferResponse) {
                objMap.put(Constants.REWARD, (CTOfferResponse) responses[6]);
            } else {
                serviceException = (ServiceException) responses[6];
                log.error("serviceException {}", serviceException);
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.REWARD, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if( responses[7] instanceof CTOfferResponse) {
                objMap.put(Constants.CREDIT, (CTOfferResponse) responses[7]);
            } else {
                serviceException = (ServiceException) responses[7];
                log.error("serviceException {}", serviceException);
                log.error("handleAsyncResponse: Scenario {} Failure Description {} {}", Constants.CREDIT, serviceException.getMessage(), serviceException.getError());
                anyFailure = true;
            }
            if(anyFailure) {
                if(serviceException != null
                        && Objects.nonNull(serviceException.getError())
                        &&(Constants.MIG_OFFERS_R001.equalsIgnoreCase(serviceException.getError().getErrorId())
                        || Constants.EDSP_OFFER_CT_ERROR_CODE.equalsIgnoreCase(serviceException.getError().getErrorId()))) {
                    log.error("migration offers error handler...");
                    throw serviceException;
                } else {
                    log.error("Some method failed::::::" + serviceException);
                    log.error("Some method failed with getError::::::" + serviceException.getError());
                    if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
                            && !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
                            && !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
                            && offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                        throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001))
                                .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "handleAsyncResponse()"));
                    } else {
                        throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001))
                                .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002,  "handleAsyncResponse()"));
                    }
                }
            }

        } catch (ServiceException ex) {
            if (ex != null && Objects.nonNull(serviceException.getError())
                    && (Constants.MIG_OFFERS_R001.equalsIgnoreCase(ex.getError().getErrorId())
                    || Constants.EDSP_OFFER_CT_ERROR_CODE.equalsIgnoreCase(ex.getError().getErrorId()))) {
                throw serviceException;
            } else {
                if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
                        && !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
                        && !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
                        && offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                    throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001))
                            .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "handleAsyncResponse()"));
                } else {
                    throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                            .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002, "handleAsyncResponse()"));
                }
            }
        }
        return objMap;
    }

    /**
     * Checks if is standalone offers only request.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return true, if is standalone offers only request
     */
    private boolean isStandaloneOffersOnlyRequest(OfferRequestWrapper offerRequestWrapper) {
        boolean stanaloneOffersReq = false;
        if( Optional.ofNullable(offerRequestWrapper).isPresent() &&
                Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent() &&
                Optional.ofNullable(offerRequestWrapper.getOfferRequest().getAddOnType()).isPresent() &&
                offerRequestWrapper.getOfferRequest().getAddOnType().size() == Constants.ONE &&
                offerRequestWrapper.getOfferRequest().getAddOnType().contains(Constants.STANDALONE_CAPS) &&
                Optional.ofNullable(offerRequestWrapper.getOfferRequest().getPlanSubType()).isPresent() &&
                offerRequestWrapper.getOfferRequest().getPlanSubType().contains(Constants.INTERNATIONAL) &&
                offerRequestWrapper.getOfferRequest().getPlanSubType().size() == Constants.ONE){
            stanaloneOffersReq = true;
        }
        return stanaloneOffersReq;
    }
    /**
     * Checks if is all video add ons request.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return true, if is all video add ons request
     */
    private boolean isAllVideoAddOnsRequest(OfferRequestWrapper offerRequestWrapper) {
        boolean allVideoAddons = false;
        if( Optional.ofNullable(offerRequestWrapper).isPresent() &&
                Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent() &&
                !Optional.ofNullable(offerRequestWrapper.getOfferRequest().getAddOnType()).isPresent() &&
                !Optional.ofNullable(offerRequestWrapper.getOfferRequest().getPlanSubType()).isPresent() &&
                offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_ADDON)) {
            allVideoAddons = true;
            // populating AddOnType and PlanSubType for All Video-AddOns scenario
            populateAddOnTypeAndPlanSubType(offerRequestWrapper);
        }
        return allVideoAddons;
    }
    /**
     * Populate add on type and plan sub type.
     * @param offerRequestWrapper the offer request wrapper
     */
    private void populateAddOnTypeAndPlanSubType(OfferRequestWrapper offerRequestWrapper) {
        offerRequestWrapper.getOfferRequest().setAddOnType(Stream.of(Constants.PROGRAMMING_BOLT_ON, Constants.BOLT_ON, Constants.STANDALONE_CAPS, Constants.SUBSCRIPTION)
                .collect(Collectors.toList()));
        offerRequestWrapper.getOfferRequest()
                .setPlanSubType(Stream.of(Constants.C_DVR, Constants.CROSS_PRODUCT_MOBILITY, Constants.STREAMS,Constants.CROSS_PRODUCT_WATCH_TV, Constants.CROSS_PRODUCT)
                        .collect(Collectors.toList()));
    }

    /**
     *
     * @param offers
     * @param offerRequestWrapper
     */
    public void processOffersFetchedBySearchIds(CTOfferResponse offers, OfferRequestWrapper offerRequestWrapper) {

        if (Objects.nonNull(offers) && Objects.nonNull(offers.getOffers()) && !offers.getOffers().isEmpty()) {
            offers.getOffers().stream().forEach(offer -> {
                if (offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_PLAN)) {
                    ottSalesOffersProcessorHelper.setLeadOffers(Stream.of(offer).collect(Collectors.toList()), offerRequestWrapper );
                    ottSalesOffersProcessorHelper.populateCompatibleProducts(Stream.of(offer).collect(Collectors.toList()));
                }
                if (offer.getAttributes().getOfferProductType().equalsIgnoreCase(Constants.VIDEO_DEVICE)) {
                    ottSalesOffersProcessorHelper.filterExpiredInstallmentOptions(Stream.of(offer).collect(Collectors.toList()));
                }
            });

            cpopProductsHelper.filterInvalidPrices(offers, offerRequestWrapper);
            cpopBenefitsHelper.filterInvalidBenefits(offers, offerRequestWrapper);
            calculateOfferPriceOnIXPFlag(offerRequestWrapper,offers.getOffers());
        }
    }
}
