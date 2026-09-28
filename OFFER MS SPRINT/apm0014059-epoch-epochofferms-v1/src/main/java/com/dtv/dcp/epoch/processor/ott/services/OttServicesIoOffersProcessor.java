package com.dtv.dcp.epoch.processor.ott.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.processor.helper.CPOPBenefitsHelper;
import com.dtv.dcp.epoch.processor.helper.CPOPProductsHelper;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class OttServicesIoOffersProcessor {

    private static final Logger log = LoggerFactory.getLogger(com.dtv.dcp.epoch.processor.ott.services.OttServicesIoOffersProcessor.class);

    @Autowired
    CPOPBenefitsHelper cpopBenefitsHelper;

    @Autowired
    CPOPProductsHelper cpopProductsHelper;



    /**
     * @param offerRequestWrapper
     * @param offers
     * @return
     */
    public List<CTOffer> processIoOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> offers) {
        log.info("START processIoOffers");
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        try{
            List<String> listOfServiceId = new ArrayList<>();
            List<CTOffer> listIoOffers = extractIOOffers(offers);
            ctOfferResponse.setOffers(listIoOffers);

            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getSalesChannel()) &&
                    StringUtils.equalsIgnoreCase(Constants.OPUS, offerRequestWrapper.getOfferRequest().getSalesChannel().get(0)) &&
                        Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext()).isPresent() &&
                            Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {

                //Getting the list from eligible products
                if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt()).isPresent()) {
                    for (ProductInfo productInfo : offerRequestWrapper.getOfferRequest().getCustomerContext().getOtt().getProducts()) {
                        listOfServiceId.add(productInfo.getProductCode());
                    }
                }

                //Filtering Benefits for applicable products  customerContext

                List<CTOffer> listOfOffers = filterOffersBasedOnServiceIds(ctOfferResponse, listOfServiceId, offerRequestWrapper);
                log.info("filterOffersBasedOnServiceIds Done");
                ctOfferResponse.setOffers(listOfOffers);
                //Filters conflicting products by account type.
                ctOfferResponse = cpopProductsHelper.filterProductsByRetIOApplicableProducts(ctOfferResponse);
                log.info("filterProductsByRetIOApplicableProducts Done");
                ctOfferResponse = cpopBenefitsHelper.filterInvalidBenefits(ctOfferResponse, offerRequestWrapper);
                log.info("filterInvalidBenefits Done");
                ctOfferResponse = cpopProductsHelper.filterInvalidPrices(ctOfferResponse, offerRequestWrapper);
                log.info("filterInvalidPrices Done");
            }

        } catch (Exception ex) {
            log.error("Exception processIoOffers...{}", ex);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex))
                        .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "processIoOffers()"));
            } else {
                throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                        .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002,  "processIoOffers()"));
            }
        }


        log.info("END processIoOffers");
        return ctOfferResponse.getOffers();
    }

    public List<CTOffer> filterOffersBasedOnServiceIds(CTOfferResponse ctOfferResponse, List<String> listOfServiceId, OfferRequestWrapper offerRequestWrapper) {
        log.info("START filterOffersBasedOnServiceIds");
        List<CTOffer> listOfOffers = new ArrayList<>();
        try{
            if (ctOfferResponse != null && ctOfferResponse.getOffers() != null) {
                ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {

                    if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getBenefits() != null) {
                        AtomicBoolean offerAdded = new AtomicBoolean(false);
                        ctOffer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                            if (benefit.getApplicableProducts() != null && !benefit.getApplicableProducts().isEmpty()) {

                                benefit.getApplicableProducts().stream().forEach(productWrapper -> {

                                    if (!offerAdded.get() && Objects.nonNull(productWrapper.getProducts()) && CollectionUtils.isNotEmpty(productWrapper.getProducts())) {
                                        List<String> list = productWrapper.getProducts().stream().map(product -> product.getKey()).collect(Collectors.toList());
                                        if (CollectionUtils.containsAny(list, listOfServiceId) && benefit.getStartDate() != null && benefit.getEndDate() != null &&
                                                OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(benefit.getStartDate()), OffersUtils.getFormattedDate(benefit.getEndDate()))
                                        ) {
                                            listOfOffers.add(ctOffer);
                                            offerAdded.set(true);
                                        } else if (CollectionUtils.containsAny(list, listOfServiceId) && benefit.getStartDate() != null && benefit.getEndDate() == null &&
                                                OffersUtils.isDateActive(OffersUtils.getFormattedDate(benefit.getStartDate()))) {
                                            listOfOffers.add(ctOffer);
                                            offerAdded.set(true);
                                        }
                                    }

                                });

                            }
                        });
                    }
                });
            }
        } catch (Exception ex) {
            log.error("Exception filterOffersBasedOnServiceIds...{}", ex);
            if (Optional.ofNullable(offerRequestWrapper.getOfferRequest().getOfferProductFamily()).isPresent()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().isEmpty()
					&& !offerRequestWrapper.getOfferRequest().getOfferProductFamily().contains(null)
					&& offerRequestWrapper.getOfferRequest().getOfferProductFamily().stream().allMatch(Predicate.isEqual(Constants.OTT_PRODUCT_FAMILY))) {
                throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10001, ex))
                        .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_OTT_GETEOFFER_10002,  "filterOffersBasedOnServiceIds()"));
            } else {
                throw ((new ServiceException(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10001, ex))
                        .addDetail(ErrorMessages.CPOP_OFFER_ERROR_ON_GETEOFFER_10002,  "filterOffersBasedOnServiceIds()"));
            }
        }
        log.info("END filterOffersBasedOnServiceIds");
        return listOfOffers;
    }

    public List<CTOffer> extractIOOffers(List<CTOffer> ctOfferList) {
        log.info("START extractIOOffers");
        List<CTOffer> list = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(ctOfferList)) {
            ctOfferList.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                if (ctOffer.getAttributes() != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits()) && ctOffer.getAttributes().getBenefits().get(0) != null &&
                        (ctOffer.getAttributes().getBenefits().get(0).isIoOffer())) {
                    log.debug(":::::::::IOOFFER: " + OffersUtils.sanitizeData(ctOffer.getCode()));
                    list.add(ctOffer);
                }
            });
        }
        log.info("END extractIOOffers");
        return list;
    }

    public List<CTOffer> extractNonIOOffers(List<CTOffer> ctOfferList) {
        log.info("START extractNonIOOffers");
        List<CTOffer> list = new ArrayList<>();
        ctOfferList.stream().filter(Objects::nonNull).forEach(ctOffer -> {
            if (ctOffer.getAttributes() != null && org.apache.commons.collections.CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits()) && ctOffer.getAttributes().getBenefits().get(0) != null &&
                    (ctOffer.getAttributes().getBenefits().get(0).isIoOffer())) {
                // nothing
            } else {
                log.debug(":::::::::NON:::IOOFFER: " + OffersUtils.sanitizeData(ctOffer.getCode()));
                list.add(ctOffer);
            }
        });
        log.info("END extractNonIOOffers");
        return list;
    }
}
