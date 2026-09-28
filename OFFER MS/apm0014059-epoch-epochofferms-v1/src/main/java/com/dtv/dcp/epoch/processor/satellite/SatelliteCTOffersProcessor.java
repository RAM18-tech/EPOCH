package com.dtv.dcp.epoch.processor.satellite;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.ExistingPromotion;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.IneligibleOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.product.Constraints;
import com.dtv.dcp.epoch.model.ct.product.Price;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductObj;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Variant;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.ct.response.STMSBOMLineItem;
import com.dtv.dcp.epoch.processor.satellite.sales.SatelliteSegmentationProcessor;
import com.dtv.dcp.epoch.processor.satellite.services.SatelliteServicesOffersProcessor;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.dtv.dcp.epoch.util.OffersUtils;
import com.dtv.dcp.epoch.util.PnpGroupUtils;
import com.dtv.dcp.epoch.util.RedisCacheHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class SatelliteCTOffersProcessor {

    /**
     * The CpopClient.
     */
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CpopClientHelper cpopClientHelper;

    @Autowired
    OffersUtils offersUtils;

    @Autowired
    SatelliteServicesOffersProcessor satelliteServicesOffersProcessor;

    @Autowired
    SatelliteSegmentationProcessor satelliteSegmentationProcessor;

    @Autowired
    private DMALookUpService dmaLookUpService;

    @Autowired
    private PnpGroupUtils pnpGroupUtils;

    @Autowired
    private FeatureManagerHelper featureHelper;

    @Autowired
    private RedisCacheHelper redisCacheHelper;
    
    

    private static final Logger log = LoggerFactory.getLogger(SatelliteCTOffersProcessor.class);

    /**
     * Processes video addon offers for retention.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return CTOfferResponse containing video addon offers
     */
    public CTOfferResponse getVideoAddonOffers(OfferRequestWrapper offerRequestWrapper) {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        if (offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)) {

            ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
            ctOfferRequest.setOfferProductType(offerRequestWrapper.getOfferRequest().getOfferProductType());
            ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
            ctOfferRequest.setAddOnType(offerRequestWrapper.getOfferRequest().getAddOnType());
            ctOfferRequest.setQualifyingProducts(offerRequestWrapper.getOfferRequest().getQualifyingProducts());
            ctOfferRequest.setBillingProductCodes(offerRequestWrapper.getOfferRequest().getBillingProductCodes());
            ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
            ctOfferRequest.setCustomerEligibility(offerRequestWrapper.getOfferRequest().getCustomerEligibility());
            ctOfferRequest.setExpiredOffers(offerRequestWrapper.getOfferRequest().isExpiredOffers());


            // below filters yet to set in CT

            ctOfferRequest.setAccountTypes(offerRequestWrapper.getOfferRequest().getAccountTypes()); // Validate filter	in CT
            ctOfferRequest.setCustomerSegments(offerRequestWrapper.isMobility() ? Stream.of("Mobility").collect(Collectors.toList()) :
                    Stream.of("Residential").collect(Collectors.toList())); // Validate filter	in CT
            log.info("API_NAME:EPOCH_GETOFFERS CUSTOMERSEGMENT:{}", OffersUtils.sanitizeData(ctOfferRequest.getCustomerSegments()));
            ctOfferRequest.setPlanSubType(offerRequestWrapper.getOfferRequest().getPlanSubType()); // Validate filter in CT

        }
        ctOfferRequest.setFlow(offerRequestWrapper.getFlow());
        log.debug("\n Retention save offer request payLoad >>>>> {}", ctOfferRequest);

        return getOffersFromCT(ctOfferRequest);

    }

    /**
     * Processes video addon offers for acquisition.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffer     list of selected CT offers
     * @return CTOfferResponse containing video addon offers
     */
    public CTOfferResponse getVideoAddonOffersForAcquisition(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.VIDEO_ADDON)));

        String modifiedSalesChannel = OffersUtils.getModifiedSalesChannel(
                offerRequestWrapper.getOfferRequest().getSalesChannel(),
                offerRequestWrapper.getOfferRequest().getOfferActionType());

        offersUtils.processOffers(ctOfferResponse, modifiedSalesChannel);

        CTOfferResponse allOffers = objectMapper.convertValue(ctOfferResponse, CTOfferResponse.class);
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        filterVideoAddonOffers(ctOfferResponse, ctSelectedOffer, offerRequestWrapper);
        applyOrderModChanges(ctOfferResponse, offerRequestWrapper, allOffers, Constants.VIDEO_ADDON);
        return ctOfferResponse;
    }

    /**
     * Processes reward card offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffer     list of selected CT offers
     * @return CTOfferResponse containing reward card offers
     */
    public CTOfferResponse getSatelliteRewardCardOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.REWARD)));
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        filterRewardCardOffers(ctOfferResponse, ctSelectedOffer, offerRequestWrapper);
        return ctOfferResponse;
    }

    private void filterRewardCardOffers(CTOfferResponse ctOfferResponse, List<CTOffer> ctSelectedOffers, OfferRequestWrapper offerRequestWrapper) {
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
        List<String> incompatibleProductIds = getIncompatibleProductIds(ctSelectedOffers);
        ctOfferResponse.getOffers().removeIf(ctOffer -> (!isQualifyingOffer(ctOffer, ctSelectedOffers)
                || satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(incompatibleProductIds, ctOffer)
                || filterOffersBasedOnMarketingSourceCode(ctOffer, offerRequestWrapper)));

        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper)
                    || offersUtils.isNotValidBasedOnCommitmentDuration(ctOffer, ctSelectedOffers, offerRequestWrapper));
        }
        if (featureHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)) {
        	ctOfferResponse.getOffers().removeIf(ctOffer -> isInCompatibleOffer(ctOffer, ctSelectedOffers));
        }
        List<String> conflictingOfferIds = new ArrayList<>();
        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
            conflictingOfferIds.addAll(getConflictingOfferIds(ctOffer));
        });
        ctOfferResponse.getOffers().removeIf(ctOffer -> conflictingOfferIds.contains(ctOffer.getId()));
        applyFilterOnPriceforBillRefId(ctOfferResponse.getOffers(), serverDate, offerRequestWrapper);
    }

    /**
     * Processes closing offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffer     list of selected CT offers
     * @return CTOfferResponse containing closing offers
     */
    public CTOfferResponse getSatelliteClosingOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.CREDIT)));
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        filterClosingOffers(ctOfferResponse, ctSelectedOffer, offerRequestWrapper);
        return ctOfferResponse;
    }

    private void filterClosingOffers(CTOfferResponse ctOfferResponse, List<CTOffer> ctSelectedOffers, OfferRequestWrapper offerRequestWrapper) {
        List<String> incompatibleProductIds = getIncompatibleProductIds(ctSelectedOffers);
        ctOfferResponse.getOffers().removeIf(ctOffer -> (!isQualifyingOffer(ctOffer, ctSelectedOffers)
                || satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(incompatibleProductIds, ctOffer)
                || filterOffersBasedOnMarketingSourceCode(ctOffer, offerRequestWrapper)));

        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper)
                    || offersUtils.isNotValidBasedOnCommitmentDuration(ctOffer, ctSelectedOffers, offerRequestWrapper));
        }

        if (featureHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)) {
        	ctOfferResponse.getOffers().removeIf(ctOffer -> isInCompatibleOffer(ctOffer, ctSelectedOffers));
        }

        List<String> conflictingOfferIds = new ArrayList<>();
        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
            conflictingOfferIds.addAll(getConflictingOfferIds(ctOffer));
        });
        ctOfferResponse.getOffers().removeIf(ctOffer -> conflictingOfferIds.contains(ctOffer.getId()));
    }

    /**
     * Filters offers based on billing system.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctOfferResponse     the CT offer response
     */
    public void filterOffersBasedOnBillingSystem(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
        String billingSystem;
        if (StringUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBillingSystem())) {
            billingSystem = offerRequestWrapper.getOfferRequest().getBillingSystem();
        } else {
            billingSystem = Constants.ENABLER;
        }
        ctOfferResponse.getOffers().removeIf(ctOffer -> (Objects.nonNull(ctOffer.getAttributes().getBillingSystem())
                && !ctOffer.getAttributes().getBillingSystem().equalsIgnoreCase(billingSystem)));
    }

    /**
     * Filters offers based on order mod flag.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctOfferResponse     the CT offer response
     */
    private void filterOffersBasedOnOrderModFlag(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
        ctOfferResponse.getOffers().removeIf(ctOffer -> (Objects.nonNull(ctOffer.getAttributes().getForOrderMod())
                && ctOffer.getAttributes().getForOrderMod() == Boolean.TRUE
                && CollectionUtils.isEmpty(offerRequestWrapper.getOfferRequest().getOrderContext())));
    }

    public void filterOffersBasedOnDMA(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {

        CustomerEligibility custEligibility = offerRequestWrapper.getCtOfferRequest().getCustomerEligibility();

        if (Objects.nonNull(custEligibility)
                && CollectionUtils.isNotEmpty(custEligibility.getDma())) {

            List<String> requestDMA = custEligibility.getDma();

            ctOfferResponse.getOffers().removeIf(ctOffer -> (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                    && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                    && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0))
                    && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0).getDma())
                    && !ctOffer.getAttributes().getEligibility().getConstraints().get(0).getDma().stream().anyMatch(requestDMA::contains)));

        } else {
            ctOfferResponse.getOffers().removeIf(ctOffer -> (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                    && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                    && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0))
                    && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0).getDma())));
        }

    }

    public CTOfferResponse getSatelliteOffers(OfferRequestWrapper offerRequestWrapper, ArrayList<String> offerProductTypes) {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();
        ctOfferRequest.setOfferProductType(offerProductTypes);
        ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
        ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
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
        ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
        if (Objects.nonNull(offerRequestWrapper.getCtOfferRequest())) {
            ctOfferRequest.setChannelEligibility(offerRequestWrapper.getCtOfferRequest().getChannelEligibility());
        }
        ctOfferRequest.setExpiredOffers(true);
        ctOfferRequest.setFlow(offerRequestWrapper.getFlow());
        log.debug("\n Acquisition  request payLoad >>>>> {}", ctOfferRequest);

        CTOfferResponse ctOfferResponse = getOffersFromCT(ctOfferRequest);
        ctOfferResponse.getOffers().removeIf(
                ctOffer -> !OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(ctOffer.getStartDate()),
                        OffersUtils.getFormattedDate(ctOffer.getEndDate()), serverDate));
        return ctOfferResponse;
    }

    private List<String> getIncludedProductIds(List<CTOffer> ctSelectedOffers) {

        List<String> includedProductsIds = new ArrayList<>();
        if (Objects.nonNull(ctSelectedOffers)) {
            ctSelectedOffers.stream().filter(Objects::nonNull).forEach(ctSelectedOffer -> {
                if (Objects.nonNull(ctSelectedOffer.getAttributes())
                        && CollectionUtils.isNotEmpty(ctSelectedOffer.getAttributes().getAssociatedProducts())) {
                    ctSelectedOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                        if (Objects.nonNull(associatedProduct.getIncludedProducts())) {
                            associatedProduct.getIncludedProducts().stream().filter(Objects::nonNull).forEach(includedProduct -> {
                                if (Objects.nonNull(includedProduct.getProducts())) {
                                    includedProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                        if (Objects.nonNull(product.getId())) {
                                            includedProductsIds.add(product.getId());
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
            });

        }
        return includedProductsIds;
    }

    private void filterVideoAddonOffers(CTOfferResponse ctOfferResponse, List<CTOffer> ctSelectedOffers, OfferRequestWrapper offerRequestWrapper) {
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
        List<String> includedIdList = getIncludedProductIds(ctSelectedOffers);
        List<String> incompatibleProductIds = getIncompatibleProductIds(ctSelectedOffers);
        
        if (featureHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)) {
        	ctOfferResponse.getOffers().removeIf(ctOffer -> isInCompatibleOffer(ctOffer, ctSelectedOffers));
        }
        ctOfferResponse.getOffers().removeIf(ctOffer -> (!isQualifyingOffer(ctOffer, ctSelectedOffers)
                || hasIncludedProduct(ctOfferResponse, ctOffer, includedIdList)
                || satelliteServicesOffersProcessor.isNotValidBasedOnIncompatibleProd(incompatibleProductIds, ctOffer))
                || filterOffersBasedOnMarketingSourceCode(ctOffer, offerRequestWrapper)
                || (Objects.nonNull(offersUtils.isLocalChannelVideoAddon(ctOffer))
                && !offersUtils.isLocalChannelVideoAddon(ctOffer).equals(offerRequestWrapper.getOfferRequest().getHasLocalChannels())));

        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper)
                    || offersUtils.isNotValidBasedOnCommitmentDuration(ctOffer, ctSelectedOffers, offerRequestWrapper));
        }

        List<String> conflictingOfferIds = new ArrayList<>();
        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
            conflictingOfferIds.addAll(getConflictingOfferIds(ctOffer));
        });
        ctOfferResponse.getOffers().removeIf(ctOffer -> conflictingOfferIds.contains(ctOffer.getId()));
        applyFilterOnPriceforBillRefId(ctOfferResponse.getOffers(), serverDate, offerRequestWrapper);
    }

    private boolean filterOffersBasedOnMarketingSourceCode(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {

        if (Objects.nonNull(ctOffer.getAttributes().getEligibility())
                && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())
                && Objects.nonNull(ctOffer.getAttributes().getEligibility().getConstraints().get(0)) && Objects.nonNull(
                ctOffer.getAttributes().getEligibility().getConstraints().get(0).getMsc())) {
            List<String> marketingSourceCodes = ctOffer.getAttributes().getEligibility().getConstraints().get(0).getMsc();
            if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getMarketingSourceCode())
                    && Objects.nonNull(offerRequestWrapper.getOfferRequest().getMarketingSourceCode().get(0))) {
                return marketingSourceCodes.stream().noneMatch(offerRequestWrapper.getOfferRequest()
                        .getMarketingSourceCode().get(0)::equalsIgnoreCase);
            } else {
                return true;
            }
        }
        return false;
    }

    private List<String> getIncompatibleProductIds(List<CTOffer> ctSelectedOffers) {
        List<String> incompatibleProductIds = new ArrayList<>();
        if (Objects.nonNull(ctSelectedOffers)) {
            ctSelectedOffers.stream().filter(Objects::nonNull).forEach(ctSelectedOffer -> {
                incompatibleProductIds.addAll(satelliteServicesOffersProcessor.getIncompatibleProducts(offersUtils.getProductObjFromOffer(ctSelectedOffer)));
            });
        }
        return incompatibleProductIds;
    }

    private boolean hasIncludedProduct(CTOfferResponse ctOfferResponse, CTOffer ctOffer, List<String> includedIdList) {
        List<ProductObj> includedProducts = new ArrayList<>();
        if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
            ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                if (Objects.nonNull(associatedProduct.getBundleProducts())) {
                    associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                        if (Objects.nonNull(bundleProduct.getProducts())) {
                            bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                if (Objects.nonNull(includedIdList) && includedIdList.contains(product.getId())) {
                                	
                                	if (featureHelper.isEnabled(Constants.FEATURE_INCL_PRD_PRICE_FILTER_ENABLED) && Objects.nonNull(product.getObj())) {
                                		product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                            if (Objects.nonNull(variant.getPrices())) {                                                

                                                variant.getPrices().removeIf(price -> (Objects.nonNull(ctOffer.getAttributes().getBillingId())
                                                        && Objects.nonNull(price.getBillingReferenceId())
                                                        && !price.getBillingReferenceId().equals(ctOffer.getAttributes().getBillingId())));

                                                variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
                                                    if (Objects.nonNull(price.getSubCategory()) && Objects.nonNull(variant.getAttributes())) {
                                                        variant.getAttributes().setSubCategory(price.getSubCategory());
                                                    }
                                                });
                                            }
                                        });
                                    }
                                	
                                    includedProducts.add(product.getObj());
                                    includedIdList.remove(product.getId());
                                }
                            });
                        }

                    });
                }
            });
        }
        if (ctOfferResponse.getIncludedProducts() != null) {
            ctOfferResponse.getIncludedProducts().addAll(includedProducts);
        } else {
            ctOfferResponse.setIncludedProducts(includedProducts);
        }
        return CollectionUtils.isNotEmpty(includedProducts);
    }

    /**
     * Applies price filter for bill reference ID.
     *
     * @param offers              list of CT offers
     * @param serverDate          the server date
     * @param offerRequestWrapper the offer request wrapper
     */
    public void applyFilterOnPriceforBillRefId(List<CTOffer> offers, Date serverDate, OfferRequestWrapper offerRequestWrapper) {

        offers.stream().filter(Objects::nonNull).forEach(ctOffer -> {

            if (Objects.nonNull(ctOffer.getAttributes()) && Objects.nonNull(ctOffer.getAttributes().getAssociatedProducts())) {
                ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                    if (Objects.nonNull(associatedProduct.getBundleProducts())) {
                        associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                            if (Objects.nonNull(bundleProduct.getProducts())) {
                                bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                    ProductObj productObj = product.getObj();

                                    if (Objects.nonNull(productObj)) {
                                        productObj.getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                                            if (Objects.nonNull(variant.getPrices())) {

                                                if (Constants.VIDEO_PLAN.equalsIgnoreCase(ctOffer.getAttributes().getOfferProductType())) {

                                                    String pnpGroup = null;
                                                    if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                                                            Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()) &&
                                                            Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection())) {
                                                        PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection();
                                                        String nbcd = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getNextBillingDate();

                                                        pnpGroup = pnpGroupUtils.getPnpGroup(priceProtection, nbcd, offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
                                                    }

                                                    // Filter prices based on customer group for video-plan
                                                    filterInvalidPrices(offerRequestWrapper, variant, pnpGroup);
                                                }

                                                variant.getPrices().removeIf(price -> !isActivePrice(price, serverDate));
                                                variant.getPrices().removeIf(price -> (Objects.nonNull(ctOffer.getAttributes().getBillingId())
                                                        && Objects.nonNull(price.getBillingReferenceId())
                                                        && !price.getBillingReferenceId().equals(ctOffer.getAttributes().getBillingId())));

                                                variant.getPrices().stream().filter(Objects::nonNull).forEach(price -> {
                                                    if (Objects.nonNull(price.getSubCategory())) {
                                                        variant.getAttributes().setSubCategory(price.getSubCategory());
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
        });
    }

    /**
     * Gets conflicting offer IDs.
     *
     * @param ctOffer the CT offer
     * @return list of conflicting offer IDs
     */
    public List<String> getConflictingOfferIds(CTOffer ctOffer) {

        List<String> conflictingOfferIds = new ArrayList<>();

        if (Objects.nonNull(ctOffer) && Objects.nonNull(ctOffer.getAttributes())
                && Objects.nonNull(ctOffer.getAttributes().getConflictingOffers())
                && !ctOffer.getAttributes().getConflictingOffers().isEmpty()) {

            // Getting conflictingOffersList
            List<GenericTypeIdBase> conflictingOffersList = ctOffer.getAttributes().getConflictingOffers();

            if (CollectionUtils.isNotEmpty(conflictingOffersList)) {
                conflictingOffersList.stream().filter(Objects::nonNull).forEach(conflictingOffer -> {
                    if (Objects.nonNull(conflictingOffer.getId())) {
                        conflictingOfferIds.add(conflictingOffer.getId());
                    }
                });
            }
        }
        return conflictingOfferIds;
    }

    /**
     * This method return true if the offer has qualifying products list which matches with the selected offer details
     *
     * @param ctOffer          the CT offer
     * @param ctSelectedOffers list of selected CT offers
     * @return true if qualifying, false otherwise
     */
    public boolean isQualifyingOffer(CTOffer ctOffer, List<CTOffer> ctSelectedOffers) {
        Set<Boolean> hasQualifyingProduct = new HashSet<>();
        List<String> qualifyingProductIds = new ArrayList<>();
        if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
            ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                if (associatedProduct.getQualifyingProducts() != null) {
                    associatedProduct.getQualifyingProducts().stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
                        if (Objects.nonNull(qualifyingProduct.getProducts())) {
                            qualifyingProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                if (Objects.nonNull(ctSelectedOffers)) {
                                    ctSelectedOffers.stream().filter(Objects::nonNull).forEach(ctSelectedOffer -> {
                                        String productsId = offersUtils.getProductIdFromOffer(ctSelectedOffer);
                                        if (Objects.nonNull(productsId) && productsId.equals(product.getId())) {
                                            qualifyingProductIds.add(product.getId());
                                        }
                                    });
                                }
                            });
                            if (CollectionUtils.isEmpty(qualifyingProductIds)) {
                                hasQualifyingProduct.add(false);
                            }
                            qualifyingProductIds.clear();
                        }
                    });
                }
            });
        }
        return !hasQualifyingProduct.contains(false);
    }

    /**
     * Gets offers from CT.
     *
     * @param ctOfferRequest the CT offer request
     * @return CTOfferResponse from CT
     * @throws ServiceException if CT call fails
     */
    private CTOfferResponse getOffersFromCT(CTOfferRequest ctOfferRequest) {

        CTOfferResponse offerResponse = cpopClientHelper.getOffers(ctOfferRequest);
        if (offerResponse == null) {
            log.info(
                    "In Method SatelliteCTOffersProcessor.getOffersFromCT() :: Getting the null response from the CTMS");
            throw new ServiceException(ErrorMessages.CT_ERROR).addDetail(ErrorMessages.CT_ERROR_DETAILS);
        }
        log.debug("In Method SatelliteCTOffersProcessor.getOffersFromCT() :: Getting response from the CTMS");
        return offerResponse;

    }

    /**
     * Processes video plan offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return CTOfferResponse containing video plan offers
     */
    public CTOfferResponse getVideoPlanOffers(OfferRequestWrapper offerRequestWrapper) {
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.VIDEO_PLAN)));
        //CTOfferResponse allOffers = objectMapper.convertValue(ctOfferResponse, CTOfferResponse.class);
        CTOfferResponse allOffers = null;
        if (featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
        	allOffers = objectMapper.convertValue(ctOfferResponse, CTOfferResponse.class);
        }else {
        	allOffers = SerializationUtils.clone(ctOfferResponse);
        }
        ctOfferResponse.getOffers().removeIf(ctOffer -> ((!offersUtils.isRoadrunner(ctOffer) && !(offersUtils.isEligibleForServedMarket(ctOffer) == true
                ? Objects.nonNull(offerRequestWrapper.getOfferRequest().getHasLocalChannels()) && offerRequestWrapper
                .getOfferRequest().getHasLocalChannels().equals(offersUtils.isEligibleForServedMarket(ctOffer))
                : Objects.nonNull(offerRequestWrapper.getOfferRequest().getHasLocalChannels()) && offerRequestWrapper
                .getOfferRequest().getHasLocalChannels().equals(offersUtils.hasLocalChannels(ctOffer))))
                || (filterOffersBasedOnMarketingSourceCode(ctOffer, offerRequestWrapper))));

        if (featureHelper.isEnabled(Constants.FEATURE_ROADRUNNER_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> filterBasedOnContractIndicator(ctOffer, offerRequestWrapper));
        }

        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper));
        }

        List<String> conflictingOfferIds = new ArrayList<>();
        ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
            conflictingOfferIds.addAll(getConflictingOfferIds(ctOffer));
        });
        ctOfferResponse.getOffers().removeIf(ctOffer -> conflictingOfferIds.contains(ctOffer.getId()));
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        if (Constants.STMS.equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getBillingSystem())) {
            applyFilterOnPriceforBillRefId(ctOfferResponse.getOffers(), serverDate, offerRequestWrapper);
        } else {
            applyFilterOnPriceforActivePolicy(ctOfferResponse.getOffers(), serverDate, offerRequestWrapper);
        }
        applyOrderModChanges(ctOfferResponse, offerRequestWrapper, allOffers, Constants.VIDEO_PLAN);
        return ctOfferResponse;
    }

    /**
     * Return true, if contract indicator is not matching.
     *
     * @param ctOffer             the CTOffer to check
     * @param offerRequestWrapper the offer request wrapper containing contract indicators
     * @return true if contract indicator does not match or is missing, false otherwise
     */
    private boolean filterBasedOnContractIndicator(CTOffer ctOffer, OfferRequestWrapper offerRequestWrapper) {
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getContractIndicator())) {

            if (Objects.nonNull(ctOffer.getAttributes().getContractIndicator())) {
                return offerRequestWrapper.getOfferRequest().getContractIndicator().stream()
                        .noneMatch(ctOffer.getAttributes().getContractIndicator()::equalsIgnoreCase);
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * Processes video plan offers for retention.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return CTOfferResponse containing video plan offers
     */
    public CTOfferResponse getVideoPlanOffersForRetention(OfferRequestWrapper offerRequestWrapper) {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
        ctOfferRequest.setOfferProductType(offerRequestWrapper.getOfferRequest().getOfferProductType());
        ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
        ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
        ctOfferRequest.setAccountTypes(offerRequestWrapper.getOfferRequest().getAccountTypes());
        ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
        ctOfferRequest.setFlow(offerRequestWrapper.getFlow());

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

        log.debug("\n Retention dispute offer request payLoad >>>>> {}", ctOfferRequest);

        CTOfferResponse ctOfferResponse = getOffersFromCT(ctOfferRequest);

        filterRetentionDisputeOffers(offerRequestWrapper, ctOfferResponse);

        return ctOfferResponse;

    }

    /**
     * Apply eligibility and compatibility rules on retention dispute offers
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return CTOfferResponse containing video plan offers
     */
    private CTOfferResponse filterRetentionDisputeOffers(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
        //Remove inactive offers
        ctOfferResponse.getOffers().removeIf(
                ctOffer -> !OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(ctOffer.getStartDate()),
                        OffersUtils.getFormattedDate(ctOffer.getEndDate())));

        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getDynamicUpgrade())) {
            Integer dynamicUpgrade = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getDynamicUpgrade();

            //Remove offers if dynamicUpgrade value is not matching
            ctOfferResponse.getOffers().removeIf(ctOffer -> isNotValidBasedOnDynamicUpgrade(ctOffer, dynamicUpgrade));
        }

        applyBcodeExclusion(offerRequestWrapper, ctOfferResponse);

        return ctOfferResponse;
    }

    /**
     * Apply b-code exclusion logic
     *
     * @param offerRequestWrapper the offer request wrapper containing customer context
     * @param ctOfferResponse     the CTOfferResponse to filter
     */
    public void applyBcodeExclusion(OfferRequestWrapper offerRequestWrapper, CTOfferResponse ctOfferResponse) {
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts())) {
            List<ProductInfo> products = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getProducts();

            products.stream().filter(Objects::nonNull).forEach(product -> {

                //Remove offers if the offer is non-stackable with existing b-code
                if (Objects.nonNull(product.getBillingProductCode())) {
                    ctOfferResponse.getOffers().removeIf(ctOffer -> isNotValidBasedOnExistingCode(ctOffer,
                            product.getBillingProductCode(), product.getBillingReferenceID()));
                }
            });

        }

        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getExistingPromotions())) {
            List<ExistingPromotion> existingPromos = offerRequestWrapper.getOfferRequest().getCustomerContext()
                    .getSatellite().getExistingPromotions();

            existingPromos.stream().filter(Objects::nonNull).forEach(promo -> {

                // Remove offers if the offer is non-stackable with existing promotions
                if (Objects.nonNull(promo.getBenefitCode()) && OffersUtils.validateActiveDates(promo.getStartDate(), promo.getEndDate())) {
                    ctOfferResponse.getOffers().removeIf(
                            ctOffer -> isNotValidBasedOnExistingCode(ctOffer, promo.getBenefitCode(), null));
                }
            });
        }
    }

    /**
     * Checks if offer is not valid based on dynamic upgrade.
     *
     * @param ctOffer        the CT offer
     * @param dynamicUpgrade the dynamic upgrade value
     * @return true if not valid, false otherwise
     */
    public Boolean isNotValidBasedOnDynamicUpgrade(CTOffer ctOffer, Integer dynamicUpgrade) {
        return CollectionUtils.isNotEmpty(ctOffer.getAttributes().getDynamicUpgradeValues())
                && !ctOffer.getAttributes().getDynamicUpgradeValues()
                .contains(dynamicUpgrade);
    }

    /**
     * Checks if the CT offer is non-stackable with the given billing product code and reference ID.
     *
     * @param ctOffer         the CT offer to check
     * @param billingProdCode the billing product code to compare
     * @param billingRefId    the billing reference ID to compare
     * @return true if the offer is non-stackable with the provided code and reference ID, false otherwise
     */
    private Boolean isNotValidBasedOnExistingCode(CTOffer ctOffer, String billingProdCode, String billingRefId) {
        if (Objects.nonNull(ctOffer.getAttributes().getNonStackableCodes())) {
            List<String> nonStackables = new ArrayList<>();
            List<String> nonStackableCodes = Stream
                    .of(ctOffer.getAttributes().getNonStackableCodes().split(Pattern.quote(Constants.COMMA)))
                    .map(String::trim).collect(Collectors.toList());
            nonStackableCodes.stream().filter(Objects::nonNull).forEach(nonStackableCode -> {
                List<String> codes = Stream.of(nonStackableCode.split(Constants.SLASH)).map(String::trim)
                        .collect(Collectors.toList());
                if (codes.size() == 2) {
                    StringBuilder code = new StringBuilder(codes.get(0));
                    code.append(Constants.SLASH).append(codes.get(1).replaceFirst(Constants.BILLING_REFERENEID_PREFIX, Constants.EMPTYSTRING));
                    nonStackables.add(code.toString());
                } else {
                    nonStackables.add(codes.get(0));
                }
            });

            StringBuilder existingProduct = new StringBuilder(billingProdCode);
            if (Objects.nonNull(billingRefId)) {
                existingProduct.append(Constants.SLASH).append(billingRefId.replaceFirst(Constants.BILLING_REFERENEID_PREFIX, Constants.EMPTYSTRING));
            }

            return CollectionUtils.isNotEmpty(nonStackables)
                    && (nonStackables.contains(billingProdCode) || nonStackables.contains(existingProduct.toString()));
        }
        return false;
    }

    /**
     * This method is used to check if the product price is valid.
     *
     * @param price the price of the product
     * @return boolean if the price is active
     */
    private boolean isActivePrice(Price price, Date serverDate) {
        return price != null && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(price.getStartDate()),
                OffersUtils.getFormattedDate(price.getEndDate()), serverDate);
    }

    /**
     * Applies price filter for active policy.
     *
     * @param offers              list of CT offers
     * @param serverDate          the server date
     * @param offerRequestWrapper the offer request wrapper
     */
    public void applyFilterOnPriceforActivePolicy(List<CTOffer> offers, Date serverDate, OfferRequestWrapper offerRequestWrapper) {

        String pnpCustomerGroup = null;
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite()) &&
                Objects.nonNull(offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection())) {
            PriceProtection priceProtection = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getPriceProtection();
            String nbcd = offerRequestWrapper.getOfferRequest().getCustomerContext().getSatellite().getNextBillingDate();

            pnpCustomerGroup = pnpGroupUtils.getPnpGroup(priceProtection, nbcd, offerRequestWrapper.getOfferRequest().getOfferProductFamily().get(0));
        }
        String pnpGroup = Objects.nonNull(pnpCustomerGroup) ? pnpCustomerGroup : null;

        offers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
            if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                    if (associatedProduct.getBundleProducts() != null) {
                        associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                            if (Objects.nonNull(bundleProduct.getProducts())) {
                                bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                                    if (Objects.nonNull(product) && Objects.nonNull(product.getObj()) && Objects.nonNull(product.getObj().getVariants())) {
                                        product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {

                                            // Filter prices based on customer group for video-plan
                                            filterInvalidPrices(offerRequestWrapper, variant, pnpGroup);

                                            // Filter price based on valid start and end date
                                            variant.getPrices().removeIf(price -> !isActivePrice(price, serverDate));

                                            // Remove the price if its omsFreeSTBPolicyId is not matching with
                                            // policyname
                                            variant.getPrices().removeIf(price -> (Objects
                                                    .nonNull(ctOffer.getAttributes().getOmsFreeStbPolicy())
                                                    && !ctOffer.getAttributes().getOmsFreeStbPolicy().isEmpty()
                                                    && Objects.nonNull(ctOffer.getAttributes().getOmsFreeStbPolicy()
                                                    .get(0).getAttributes().getName())
                                                    && Objects.nonNull(price.getOmsFreeSTBPolicyId())
                                                    && !Arrays.asList(price.getOmsFreeSTBPolicyId().split(Constants.COMMA))
                                                    .contains(ctOffer.getAttributes().getOmsFreeStbPolicy()
                                                            .get(0).getAttributes().getName())));

                                            variant.getPrices().removeIf(price -> Objects.isNull(price.getOmsFreeSTBPolicyId()));
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

    public Variant filterInvalidPrices(OfferRequestWrapper offerRequestWrapper, Variant variant, String pnpGroup) {

        if (StringUtils.isNotBlank(pnpGroup)) {
            Optional<Price> prices = variant.getPrices().stream().filter(Objects::nonNull)
                    .filter(p -> pnpGroup.equalsIgnoreCase(p.getCustomerGroup())).findFirst();

            if (prices.isPresent()) {
                variant.getPrices().removeIf(price -> !pnpGroup.equalsIgnoreCase(price.getCustomerGroup()));
            } else {
                variant.getPrices().removeIf(price -> Objects.nonNull(price.getCustomerGroup()) && price.getCustomerGroup().contains("-PNP"));
            }

        } else {
            variant.getPrices().removeIf(price -> Objects.nonNull(price.getCustomerGroup()) && price.getCustomerGroup().contains("-PNP"));
        }
        return variant;
    }

    /**
     * Processes video device offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffers    list of selected CT offers
     * @return CTOfferResponse containing video device offers
     */
    public CTOfferResponse getVideoDeviceOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffers) {
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.VIDEO_DEVICE)));
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        if (featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)) {
            filterOffersBasedOnOrderModFlag(offerRequestWrapper, ctOfferResponse);
        }
        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper)
                    || offersUtils.isNotValidBasedOnCommitmentDuration(ctOffer, ctSelectedOffers, offerRequestWrapper));
        }
        if (featureHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)) {
        	ctOfferResponse.getOffers().removeIf(ctOffer -> isInCompatibleOffer(ctOffer, ctSelectedOffers));
        }
        ctOfferResponse.getOffers().removeIf(ctoffer -> filterOffersBasedOnMarketingSourceCode(ctoffer, offerRequestWrapper));
        return ctOfferResponse;
    }

    /**
     * Processes insurance offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffers    list of selected CT offers
     * @return CTOfferResponse containing insurance offers
     */
    public CTOfferResponse getInsuranceOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffers) {
        CTOfferResponse ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.INSURANCE)));
        //CTOfferResponse allOffers = objectMapper.convertValue(ctOfferResponse, CTOfferResponse.class);
        CTOfferResponse allOffers = null;
    	if (featureHelper.isEnabled(Constants.FEATURE_SVC_EPOCH_PERF_OPT_ENABLED)) {
    		allOffers = objectMapper.convertValue(ctOfferResponse, CTOfferResponse.class);
		} else {
			allOffers = SerializationUtils.clone(ctOfferResponse);
		}
        ctOfferResponse.getOffers().removeIf(ctoffer -> filterOffersBasedOnMarketingSourceCode(ctoffer, offerRequestWrapper));
        if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils
                    .isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper)
                    || offersUtils.isNotValidBasedOnCommitmentDuration(ctOffer, ctSelectedOffers, offerRequestWrapper));
        }
        if (featureHelper.isEnabled(Constants.FEATURE_QUALIFIER_INCOMP_FILTER_ENABLED)) {
        	ctOfferResponse.getOffers().removeIf(ctOffer -> isInCompatibleOffer(ctOffer, ctSelectedOffers));
        }
        filterOffersBasedOnBillingSystem(offerRequestWrapper, ctOfferResponse);
        applyOrderModChanges(ctOfferResponse, offerRequestWrapper, allOffers, Constants.INSURANCE);
        return ctOfferResponse;
    }

    /**
     * Processes satellite fee offers.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param ctSelectedOffer     list of selected CT offers
     * @param bomFlag             BOM flag
     * @return CTOfferResponse containing fee offers
     */
    public CTOfferResponse getSatelliteFeeOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> ctSelectedOffer) {

        List<CTOffer> feeOffers = new ArrayList<>();
        ctSelectedOffer.stream().filter(offer -> Constants.FEE.equalsIgnoreCase(offer.getAttributes().getOfferProductType())).forEach(feeOffers::add);
        ctSelectedOffer.removeIf(feeOffers::contains);
        CTOfferResponse ctOfferResponse = new CTOfferResponse();
        ctOfferResponse = getSatelliteOffers(offerRequestWrapper, new ArrayList<>(List.of(Constants.FEE)));
        ctOfferResponse.getOffers().removeIf(ctOffer -> (!isQualifyingOffer(ctOffer, ctSelectedOffer)));
        if (featureHelper.isEnabled(Constants.FEATURE_RR_FEE_FILTER_ENABLED)) {

            if (CollectionUtils.isNotEmpty(ctSelectedOffer)) {
                CTOffer ctBaseOffer = ctSelectedOffer.stream().filter(ctOffer -> ctOffer != null && ctOffer.getAttributes() != null
                        && Constants.VIDEO_PLAN.equals(ctOffer.getAttributes().getOfferProductType())).findFirst().orElse(null);

                String contractIndicator = ctBaseOffer.getAttributes().getContractIndicator();

                ctOfferResponse.getOffers().removeIf(ctOffer -> Objects.nonNull(ctOffer.getAttributes().getContractIndicator())
                        && !ctOffer.getAttributes().getContractIndicator().equalsIgnoreCase(contractIndicator));
            }

        }
        if (featureHelper.isEnabled(Constants.FEATURE_EPOCH_SERVICE_ACTIVATION_FEE_ENABLED)) {
            ctOfferResponse.getOffers().removeIf(ctOffer -> offersUtils.isNotValidBasedOnIneligibleSalesSubChannel(ctOffer, offerRequestWrapper));
        }
        return ctOfferResponse;
    }

    /**
     * Gets offers by IDs.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @param bomFlag             BOM flag
     * @param billerFlag          biller flag
     * @return CTOfferResponse containing offers by IDs
     */
    public CTOfferResponse getOfferByIds(OfferRequestWrapper offerRequestWrapper, boolean bomFlag, boolean billerFlag) {
        CTOfferRequest ctOfferRequest = new CTOfferRequest();

        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferIds())) {
            ctOfferRequest.setOfferIds(offerRequestWrapper.getOfferRequest().getOfferIds());
        }
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getCartContext())) {
            if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartContext().getCartOffers())) {
                ctOfferRequest.setOfferCodes(getOfferCodes(offerRequestWrapper));
            } else if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCartContext().getEpochOfferCodes())) {
                ctOfferRequest.setOfferCodes(offerRequestWrapper.getOfferRequest().getCartContext().getEpochOfferCodes());
            }
        } else if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferCodes())) {
            ctOfferRequest.setOfferCodes(offerRequestWrapper.getOfferRequest().getOfferCodes());
        }

        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferProductFamily())) {
            ctOfferRequest.setOfferProductFamily(offerRequestWrapper.getOfferRequest().getOfferProductFamily());
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferActionType())) {
            ctOfferRequest.setOfferActionType(offerRequestWrapper.getOfferRequest().getOfferActionType());
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBundleProductIds())) {
            ctOfferRequest.setBundleProducts(offerRequestWrapper.getOfferRequest().getBundleProductIds());
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getSalesChannel())) {
            ctOfferRequest.setSalesChannel(offerRequestWrapper.getOfferRequest().getSalesChannel());
        }
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getPagination())) {
            ctOfferRequest.setPagination(offerRequestWrapper.getOfferRequest().getPagination());
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getCustomerSegments())) {
            ctOfferRequest.setCustomerSegments(offerRequestWrapper.getOfferRequest().getCustomerSegments());
        }
        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getBusinessSegments())) {
            ctOfferRequest.setBusinessSegments(offerRequestWrapper.getOfferRequest().getBusinessSegments());
        }
        ctOfferRequest.setExpiredOffers(true);
        ctOfferRequest.setFlow(offerRequestWrapper.getFlow());
        return getOffersFromCT(ctOfferRequest);
    }

    /**
     * Gets offer codes from request wrapper.
     *
     * @param offerRequestWrapper the offer request wrapper
     * @return list of offer codes
     */
    public List<String> getOfferCodes(OfferRequestWrapper offerRequestWrapper) {

        List<String> epochOfferCodes = new ArrayList<>();

        if (Optional.ofNullable(offerRequestWrapper).isPresent()
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest()).isPresent()
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext()).isPresent()
                && Optional.ofNullable(offerRequestWrapper.getOfferRequest().getCartContext().getCartOffers()).isPresent()) {
            offerRequestWrapper.getOfferRequest().getCartContext().getCartOffers().stream().filter(Objects::nonNull).forEach(cartOffer -> {
                if (cartOffer.getOfferCode() != null) {
                    String offerCode = cartOffer.getOfferCode();
                    epochOfferCodes.add(offerCode);
                }
            });

        }
        return epochOfferCodes;
    }

    /**
     * Associates fee with device offers.
     *
     * @param videoDeviceOffers list of video device offers
     * @param feeOffers         fee offers response
     * @param ctSelectedOffer   list of selected CT offers
     * @return list of final offers
     */
    public List<CTOffer> associateFeeWithDevice(List<CTOffer> videoDeviceOffers, CTOfferResponse feeOffers, List<CTOffer> ctSelectedOffer) {
        List<CTOffer> finalOffers = null;
        // Check if the list of video device offers is not empty
        if (CollectionUtils.isNotEmpty(videoDeviceOffers)) {
            // Get the policy for free STB
            Product omsFreeStbPolicy = getOmsFreeStbPolicy(ctSelectedOffer);

            Boolean isRoadrunner = false;
            if (CollectionUtils.isNotEmpty(ctSelectedOffer)) {
                CTOffer ctBaseOffer = ctSelectedOffer.stream().filter(ctOffer -> ctOffer != null && ctOffer.getAttributes() != null
                        && Constants.VIDEO_PLAN.equals(ctOffer.getAttributes().getOfferProductType())).findFirst().orElse(null);

                isRoadrunner = offersUtils.isRoadrunner(ctBaseOffer);
            }

            // Filter the device offers by device type
            List<CTOffer> genieHdDvrOffers = offersUtils.filterDeviceOffersByDeviceType(videoDeviceOffers, Constants.GENIE_HD_DVR);
            List<CTOffer> genieMiniOffers = offersUtils.filterDeviceOffersByDeviceType(videoDeviceOffers, Constants.GENIE_MINI);
            List<CTOffer> genieServerOffers = offersUtils.filterDeviceOffersByDeviceType(videoDeviceOffers, Constants.GENIE_SERVER);
            List<CTOffer> genieMiniWirelessOffers = offersUtils.filterDeviceOffersByDeviceType(videoDeviceOffers, Constants.GENIE_MINI_WIRELESS);
            List<CTOffer> geminiOffers = offersUtils.filterDeviceOffersByDeviceType(videoDeviceOffers, Constants.GEMINI_WIRELESS);
            finalOffers = new ArrayList<>();

            // Check if the policy and its attributes and constraints are not null
            if (Objects.nonNull(omsFreeStbPolicy) && Objects.nonNull(omsFreeStbPolicy.getAttributes())
                    && Objects.nonNull(omsFreeStbPolicy.getAttributes().getConstraints())) {
                Constraints policy = omsFreeStbPolicy.getAttributes().getConstraints();

                // Process wired receivers
                // If there are included wired Genie receivers, filter them as included, otherwise as chargeable
                if (policy.getIncludedGenieWired() > 0) {
                    filterReceiver(feeOffers, genieHdDvrOffers, policy.getMaxTVFeesWaivedForWired(), Constants.INCLUDED, finalOffers, 0, 1, isRoadrunner);
                } else {
                    filterReceiver(feeOffers, genieHdDvrOffers, policy.getMaxTVFeesWaivedForWired(), Constants.CHARGEABLE, finalOffers, 0, 1, isRoadrunner);
                }

                // Process wired Genie Mini receivers
                // If there are included wired Genie Mini receivers, filter them as included, otherwise as chargeable
                if (policy.getIncludedGenieMiniWired() > 0) {
                    Integer maxSelected = policy.getIncludedGenieMiniWired();
                    filterReceiver(feeOffers, genieMiniOffers, policy.getMaxTVFeesWaivedForGenieMiniWired(), Constants.INCLUDED, finalOffers, 1, maxSelected, isRoadrunner);
                } else {
                    Integer maxSelected = policy.getMaxAddlTVsSelectedWired();
                    filterReceiver(feeOffers, genieMiniOffers, policy.getMaxTVFeesWaivedForGenieMiniWired(), Constants.CHARGEABLE, finalOffers, 1, maxSelected, isRoadrunner);
                }

                // Process discounted wired Genie Mini receivers
                if (policy.getDiscountedGenieMiniWired() > 0) {
                    Integer minSelected = policy.getIncludedGenieMiniWired() + 1;
                    Integer maxSelected = policy.getMaxAddlTVsSelectedWired();
                    filterReceiver(feeOffers, genieMiniOffers, null, Constants.DISCOUNTED, finalOffers, minSelected, maxSelected, isRoadrunner);
                }

                // Process wireless receivers
                // If there are included wireless Genie receivers, filter them as discounted and included, otherwise as chargeable
                if (policy.getIncludedGenieWireless() > 0) {
                    //Discounted Genie2 For STMS Online (Order mod only)
                    filterReceiver(feeOffers, genieServerOffers, policy.getMaxTVFeesWaivedForWireless(), Constants.DISCOUNTED, finalOffers, 0, 1, isRoadrunner);
                    //Included Genie2 For STMS/Enabler Online
                    filterReceiver(feeOffers, genieServerOffers, policy.getMaxTVFeesWaivedForWireless(), Constants.INCLUDED, finalOffers, 0, 1, isRoadrunner);
                } else {
                    filterReceiver(feeOffers, genieServerOffers, policy.getMaxTVFeesWaivedForWireless(), Constants.CHARGEABLE, finalOffers, 0, 1, isRoadrunner);
                }

                // Process wireless Genie Mini receivers
                // If there are included wireless Genie Mini receivers, filter them as included with server and included, otherwise as chargeable
                if (policy.getIncludedGenieMiniWireless() > 0) {
                    //Included With Server Genie Mini Wireless For STMS Online (Order mod only)
                    filterReceiver(feeOffers, genieMiniWirelessOffers, policy.getMaxTVFeesWaivedForGenieMiniWireless(), Constants.INCLUDED_WITH_SERVER, finalOffers, 1, 1, isRoadrunner);
                    //Included With Server GEMINI For STMS/Enabler Online
                    filterReceiver(feeOffers, geminiOffers, policy.getMaxTVFeesWaivedForGenieMiniWireless(), Constants.INCLUDED_WITH_SERVER, finalOffers, 1, 1, isRoadrunner);
                    Integer maxSelected = policy.getIncludedGenieMiniWireless();
                    //Included Genie Mini Wireless For STMS and Enabler Online (Order mod only)
                    filterReceiver(feeOffers, genieMiniWirelessOffers, null, Constants.INCLUDED, finalOffers, 2, maxSelected, isRoadrunner);
                    //Included GEMINI For STMS Online (GEM Whole home)
                    filterReceiver(feeOffers, geminiOffers, null, Constants.INCLUDED, finalOffers, 2, maxSelected, isRoadrunner);
                } else {
                    Integer maxSelected = policy.getMaxAddlTVsSelectedWireless();
                    filterReceiver(feeOffers, genieMiniWirelessOffers, null, Constants.CHARGEABLE, finalOffers, 1, maxSelected, isRoadrunner);
                }

                // Process discounted wireless Genie Mini receivers
                if (policy.getDiscountedGenieMiniWireless() > 0) {
                    Integer minSelected = policy.getIncludedGenieMiniWireless() + 1;
                    Integer maxSelected = policy.getMaxAddlTVsSelectedWireless();
                    //Discounted Genie Mini Wireless For STMS and Enabler Online (Order mod only)
                    filterReceiver(feeOffers, genieMiniWirelessOffers, null, Constants.DISCOUNTED, finalOffers, minSelected, maxSelected, isRoadrunner);
                    //Discounted GEMINI For STMS/Enabler Online
                    if (featureHelper.isEnabled(Constants.FEATURE_GEM_WHOLE_HOME_ENABLED)) {
                        //(GEM Whole home)
                        filterReceiver(feeOffers, geminiOffers, null, Constants.DISCOUNTED, finalOffers, minSelected, maxSelected, isRoadrunner);
                    } else {
                        filterReceiver(feeOffers, geminiOffers, null, Constants.DISCOUNTED, finalOffers, 2, maxSelected, isRoadrunner);
                    }
                }
            }
        }
        return finalOffers;
    }

    /**
     * Filters receiver offers by charge type and selection limits, and associates applicable fee offers.
     *
     * @param feeOffers       the CTOfferResponse containing fee offers
     * @param genieOffers     the list of Genie receiver offers to filter
     * @param maxTVFeesWaived the maximum number of TV fees to be waived
     * @param chargeType      the charge type (e\.g\., INCLUDED, CHARGEABLE, DISCOUNTED)
     * @param finalOffers     the list to collect final filtered offers
     * @param minSelected     the minimum number of receivers to select
     * @param maxSelected     the maximum number of receivers to select
     * @param isRoadrunner    flag indicating if Roadrunner is enabled
     */
    private void filterReceiver(CTOfferResponse feeOffers, List<CTOffer> genieOffers, Integer maxTVFeesWaived,
                                String chargeType, List<CTOffer> finalOffers, Integer minSelected, Integer maxSelected, Boolean isRoadrunner) {
        // Get the TV access fee offer
        CTOffer feeOffer = getTVAccessFee(feeOffers, maxTVFeesWaived, isRoadrunner);
        // Check if the list of genie offers is not empty
        if (CollectionUtils.isNotEmpty(genieOffers)) {
            // Stream through the genie offers
            genieOffers.stream().filter(Objects::nonNull).forEach(ctOffer -> {
                // Check if the offer attributes and charge type are not null and if the charge type matches the provided charge type
                if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getChargeType() != null
                        && ctOffer.getAttributes().getChargeType().equals(chargeType)) {
                    // If the fee offer is not null, set the associated fee of the offer to the code of the fee offer
                    if (feeOffer != null) {
                        ctOffer.getAttributes().setAssociatedFee(feeOffer.getCode());
                    }
                    // Set the minimum and maximum selected attributes of the offer
                    ctOffer.getAttributes().setMinSelected(minSelected);
                    ctOffer.getAttributes().setMaxSelected(maxSelected);
                    // Add the offer to the final offers list
                    finalOffers.add(ctOffer);
                }
            });
        }

    }

    /**
     * Retrieves the TV Access Fee offer from the given fee offers.
     * Returns an included fee offer if maxTVFeesWaived is greater than zero, otherwise returns a chargeable fee offer.
     *
     * @param feeOffers       the CTOfferResponse containing fee offers
     * @param maxTVFeesWaived the maximum number of TV fees to be waived
     * @param isRoadrunner    flag indicating if Roadrunner is enabled
     * @return the TV Access Fee CTOffer, or null if not found
     */
    private CTOffer getTVAccessFee(CTOfferResponse feeOffers, Integer maxTVFeesWaived, Boolean isRoadrunner) {
        if (Objects.nonNull(feeOffers) && CollectionUtils.isNotEmpty(feeOffers.getOffers())) {
            if (Objects.nonNull(maxTVFeesWaived) && maxTVFeesWaived > 0) {
                return getTVAccessFeeByChargeType(feeOffers, Constants.INCLUDED, isRoadrunner);
            } else {
                return getTVAccessFeeByChargeType(feeOffers, Constants.CHARGEABLE, isRoadrunner);
            }
        }
        return null;
    }

    /**
     * Retrieves the TV Access Fee offer from the given fee offers by charge type.
     * If Roadrunner is enabled, applies additional filtering based on billing parameters.
     *
     * @param feeOffers    the CTOfferResponse containing fee offers
     * @param chargeType   the charge type to filter (e\.g\., INCLUDED, CHARGEABLE)
     * @param isRoadrunner flag indicating if Roadrunner is enabled
     * @return the TV Access Fee CTOffer matching the charge type and Roadrunner flag, or null if not found
     */
    private CTOffer getTVAccessFeeByChargeType(CTOfferResponse feeOffers, String chargeType, Boolean isRoadrunner) {
        if (Objects.nonNull(feeOffers) && CollectionUtils.isNotEmpty(feeOffers.getOffers())) {
            if (isRoadrunner) {

                List<CTOffer> feeOffer = new ArrayList<>();
                feeOffers.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                    ProductObj productObj = offersUtils.getProductObjFromOffer(offer);
                    productObj.getVariants().stream().filter(Objects::nonNull)
                            .filter(variant -> Objects.nonNull(variant.getAttributes()) && CollectionUtils.isNotEmpty(variant.getAttributes().getBillingParams()))
                            .forEach(variant -> {
                                variant.getAttributes().getBillingParams().stream().filter(Objects::nonNull)
                                        .forEach(billingParam -> {

                                            if (Constants.FTV_COUNT.equalsIgnoreCase(billingParam.getName())
                                                    && "0".equalsIgnoreCase(billingParam.getValue())
                                                    && offer.getAttributes() != null && chargeType != null
                                                    && chargeType.equals(offer.getAttributes().getChargeType())) {
                                                feeOffer.add(offer);
                                            }
                                        });
                            });
                });
                if (CollectionUtils.isNotEmpty(feeOffer)) {
                    return feeOffer.get(0);
                } else {
                    return null;
                }
            }
            return feeOffers.getOffers().stream().filter(ctOffer -> ctOffer != null && ctOffer.getAttributes() != null
                    && chargeType != null && chargeType.equals(ctOffer.getAttributes().getChargeType())).findFirst().orElse(null);
        }
        return null;
    }

    /**
     * Retrieves the OMS Free STB policy product from the selected CT offers.
     * Returns the first OMS Free STB policy found in the base video plan offer.
     *
     * @param ctSelectedOffer the list of selected CT offers
     * @return the OMS Free STB policy Product, or null if not found
     */
    private Product getOmsFreeStbPolicy(List<CTOffer> ctSelectedOffer) {
        if (CollectionUtils.isNotEmpty(ctSelectedOffer)) {

            CTOffer ctBaseOffer = ctSelectedOffer.stream().filter(ctOffer -> ctOffer != null && ctOffer.getAttributes() != null
                    && Constants.VIDEO_PLAN.equals(ctOffer.getAttributes().getOfferProductType())).findFirst().orElse(null);

            if (ctBaseOffer != null && ctBaseOffer.getAttributes() != null && CollectionUtils.isNotEmpty(ctBaseOffer.getAttributes().getOmsFreeStbPolicy())) {
                return ctBaseOffer.getAttributes().getOmsFreeStbPolicy().get(0);
            }
        }
        return null;
    }

    /**
     * Calculate best price.
     *
     * @param offerList           the list of CT offers to process
     * @param offerRequestWrapper the offer request context
     * @param isSalesGetOffers    flag indicating if the flow is sales acquisition
     */
    public void calculateBestPrice(List<CTOffer> offerList, OfferRequestWrapper offerRequestWrapper, boolean isSalesGetOffers) {
        if (CollectionUtils.isNotEmpty(offerList)) {
            offerList.stream().filter(Objects::nonNull).forEach(offer -> {

                if (!isSalesGetOffers || !checkIfVideoDeviceAgentIndirect(offerRequestWrapper, offer)) {

                    OfferPrice offerPrice = new OfferPrice();
                    offerPrice.setDollarAmount(0);
                    offer.getAttributes().setOfferPrice(offerPrice);

                    OfferPrice contractPrice = new OfferPrice();
                    contractPrice.setDollarAmount(0);

                    boolean isVideoPlanAcquisition = isSalesGetOffers && checkIfVideoPlanAcquisition(offerRequestWrapper, offer);
                    List<Price> planPrice = new ArrayList<>();
                    // Fetch the baseprice and set the offerprice at offer level with the base price. If there is no benfit to subtract, this is the best price
                    if (Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())) {
                        AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
                        if (Objects.nonNull(associatedProduct) && CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
                            associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                if (CollectionUtils.isNotEmpty(bundleProduct.getProducts())) {
                                    bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                                        if (Objects.nonNull(prod.getObj()) && CollectionUtils.isNotEmpty(prod.getObj().getVariants()) &&
                                                CollectionUtils.isNotEmpty(prod.getObj().getVariants().get(0).getPrices())) {
                                            //Assuming there is one price under the product response
                                            Price price = prod.getObj().getVariants().get(0).getPrices().get(0);
                                            if (Objects.nonNull(price.getValue()) && Objects.nonNull(price.getValue().getDollarAmount())) {
                                                offerPrice.setDollarAmount(price.getValue().getDollarAmount());
                                            }
                                            //Now if there is a relevant benefit value, subtract it from the base price to arrive at BEST PRICE!
                                            getBestOfferPrice(offer, offerPrice, prod, false, offerRequestWrapper);

                                            if (isVideoPlanAcquisition) {
                                                if (Objects.nonNull(price.getValue()) && Objects.nonNull(price.getValue().getDollarAmount())) {
                                                    contractPrice.setDollarAmount(price.getValue().getDollarAmount());
                                                }
                                                //calculate contractPrice
                                                getBestOfferPrice(offer, contractPrice, prod, true, offerRequestWrapper);
                                            }
                                            planPrice.add(price);
                                        }
                                    });
                                }
                                if (CollectionUtils.isNotEmpty(bundleProduct.getProducts()) && bundleProduct.getProducts().size() > 1) {
                                    offerPrice.setDollarAmount(offer.getAttributes().getOfferPrice().getDollarAmount() + offerPrice.getDollarAmount());
                                    offer.getAttributes().setOfferPrice(offerPrice);
                                }
                            });
                        }
                    }
                    getFinalOfferPrice(offer, offerPrice, contractPrice, isVideoPlanAcquisition, offerRequestWrapper, planPrice);
                }
            });
        }
    }

    private void getFinalOfferPrice(CTOffer offer, OfferPrice offerPrice, OfferPrice contractPrice, boolean isVideoPlanAcquisition, OfferRequestWrapper offerRequestWrapper, List<Price> planPrice) {
        if (Objects.nonNull(offerPrice)) {
            BigDecimal bd = new BigDecimal(Double.toString(offerPrice.getDollarAmount()));
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            offerPrice.setDollarAmount(bd.doubleValue());
            offer.getAttributes().setOfferPrice(offerPrice);

            boolean isGetOffersSatellite = false;
            if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferActionType())
                    && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferProductType())) {
                isGetOffersSatellite = true;
            }

            if (isGetOffersSatellite && Objects.nonNull(offer) && offersUtils.isRoadrunner(offer)
                    && Boolean.TRUE.equals(offerRequestWrapper.getOfferRequest().getHasLocalChannels())) {
                List<String> localsBoltonPriceList = redisCacheHelper.getValues(Constants.LOCALS_BOLTON_PRICE,
                        Constants.SATELLITE_PRODUCT_FAMILY);
                if (CollectionUtils.isNotEmpty(localsBoltonPriceList)) {
                    Double localsBoltonPrice = Double.valueOf(localsBoltonPriceList.get(0));
                    updateOfferPriceWithLocals(offer, offerPrice, localsBoltonPrice);
                    updatePlanPriceWithLocals(offer, planPrice, localsBoltonPrice);
                }

            }

            if (isVideoPlanAcquisition) {
                offer.getAttributes().setSpecialPromoPrice(offerPrice);

                BigDecimal contract = new BigDecimal(Double.toString(contractPrice.getDollarAmount()));
                contract = contract.setScale(2, RoundingMode.HALF_UP);
                contractPrice.setDollarAmount(contract.doubleValue());

                offer.getAttributes().setContractPrice(contractPrice);
            }
        }
    }

    private boolean checkIfVideoPlanAcquisition(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        return Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferProductType())
                && offerRequestWrapper.getOfferRequest().getOfferProductType().contains(Constants.VIDEO_PLAN)
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOfferActionType())
                && offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION)
                && Objects.nonNull(offer) && Objects.nonNull(offer.getAttributes())
                && Constants.VIDEO_PLAN.equalsIgnoreCase(offer.getAttributes().getOfferProductType());
    }

    private boolean checkIfVideoDeviceAgentIndirect(OfferRequestWrapper offerRequestWrapper, CTOffer offer) {
        return Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getSalesChannel())
                && (offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.SALES_CRM)
                || offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.CCAP)
                || offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DPP)
                || offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.DIRECT_INTEGRATION_PARTNER)
                || offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.ASSISTED_SALES))
                && Objects.nonNull(offer) && Objects.nonNull(offer.getAttributes())
                && Constants.VIDEO_DEVICE.equalsIgnoreCase(offer.getAttributes().getOfferProductType());
    }

    private void getBestOfferPrice(CTOffer offer, OfferPrice offerPrice, Product prod, boolean isContractPrice, OfferRequestWrapper offerRequestWrapper) {

        if (Objects.nonNull(offer) && Objects.nonNull(prod) && Objects.nonNull(offer.getAttributes())
                && CollectionUtils.isNotEmpty(offer.getAttributes().getBenefits())) {

            filterBenefits(offer, offerRequestWrapper);

            if (CollectionUtils.isNotEmpty(offer.getAttributes().getBenefits())) {
                offer.getAttributes().getBenefits().sort(Comparator.comparing(Benefit::getPromoPriority));
                offer.getAttributes().getBenefits().stream().filter(Objects::nonNull).forEach(benefit -> {
                    List<String> applicableProductIds = getApplicableProductIds(benefit);
                    if (CollectionUtils.isNotEmpty(applicableProductIds) && applicableProductIds.contains(prod.getId())) {
                        if (!isContractPrice || (isContractPrice && (benefit.getStrikeThroughOffer() == null || Boolean.FALSE.equals(benefit.getStrikeThroughOffer())))) {
                            if (Objects.nonNull(benefit) && Objects.nonNull(benefit.getValue())
                                    && Objects.nonNull(benefit.getBenefitType()) && Constants.BENEFIT_FLAT_OFF.equalsIgnoreCase(benefit.getBenefitType())
                                    && Objects.nonNull(benefit.getValue().getDollarAmount())) {
                                offerPrice.setDollarAmount(offerPrice.getDollarAmount() - benefit.getValue().getDollarAmount());
                            } else if (Objects.nonNull(benefit) && Objects.nonNull(benefit.getValue())
                                    && Objects.nonNull(benefit.getBenefitType()) && Constants.BENEFIT_PERCENT_OFFER.equalsIgnoreCase(benefit.getBenefitType())
                                    && benefit.getValue().getPercentage() != null && benefit.getValue().getPercentage() != 0) {
                                Double percentageOff = ((offerPrice.getDollarAmount()) * (benefit.getValue().getPercentage().doubleValue() / 100));
                                offerPrice.setDollarAmount(offerPrice.getDollarAmount() - percentageOff);
                            } else if (Objects.nonNull(benefit) && Objects.nonNull(benefit.getValue())
                                    && Objects.nonNull(benefit.getBenefitType()) && Constants.BENEFIT_FLAT_RATE.equalsIgnoreCase(benefit.getBenefitType())) {
                                if (benefit.getValue().getDollarAmount() == null) {
                                    offerPrice.setDollarAmount(0);
                                } else {
                                    offerPrice.setDollarAmount(benefit.getValue().getDollarAmount());
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    public void filterBenefits(CTOffer offer, OfferRequestWrapper offerRequestWrapper) {
        Date serverDate = OffersUtils.getServerDateValue(offerRequestWrapper);

        offer.getAttributes().getBenefits().removeIf(benefit -> !OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(benefit.getStartDate()),
                OffersUtils.getFormattedDate(benefit.getEndDate()), serverDate));

        offer.getAttributes().getBenefits().removeIf(benefit -> CollectionUtils.isNotEmpty(benefit.getSalesChannels())
                && Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getSalesChannel())
                && !benefit.getSalesChannels().contains(offerRequestWrapper.getOfferRequest().getSalesChannel().get(0)));

        offer.getAttributes().getBenefits().removeIf(benefit -> CollectionUtils.isNotEmpty(benefit.getDealerCode())
                && Objects.nonNull(offerRequestWrapper) && Objects.nonNull(offerRequestWrapper.getOfferRequest())
                && Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility())
                && !benefit.getDealerCode().contains(offerRequestWrapper.getOfferRequest().getChannelEligibility().getDealerCode()));

        offer.getAttributes().getBenefits().removeIf(benefit -> isNotValidBasedOnBillingSystem(benefit, offerRequestWrapper));
    }

    public Boolean isNotValidBasedOnBillingSystem(Benefit benefit, OfferRequestWrapper offerRequestWrapper) {
        boolean isSTMSRequest = offersUtils.isSTMSRequest(offerRequestWrapper);

        if (isSTMSRequest) {
            return Objects.nonNull(benefit) && Constants.ENABLER.equalsIgnoreCase(benefit.getBillingSystem());
        } else {
            return Objects.nonNull(benefit) && Constants.STMS.equalsIgnoreCase(benefit.getBillingSystem());
        }
    }

    private List<String> getApplicableProductIds(Benefit benefit) {
        List<String> applicableProductIds = new ArrayList<>();
        if (Objects.nonNull(benefit) && CollectionUtils.isNotEmpty(benefit.getApplicableProducts())) {
            benefit.getApplicableProducts().stream().filter(Objects::nonNull).forEach(applicableProduct -> {
                applicableProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                    applicableProductIds.add(product.getId());
                });
            });
        }
        return applicableProductIds;
    }

    /**
     * Calculates and sets the discount amount for each offer in the given list.
     * For each offer, determines the discount based on associated products and updates the offer attributes.
     * The calculation excludes video device agent indirect offers.
     *
     * @param offerList           the list of CT offers to process
     * @param offerRequestWrapper the offer request context
     */
    public void calculateDiscountAmount(List<CTOffer> offerList, OfferRequestWrapper offerRequestWrapper) {
        if (CollectionUtils.isNotEmpty(offerList)) {
            offerList.stream().filter(Objects::nonNull).forEach(offer -> {

                if (!checkIfVideoDeviceAgentIndirect(offerRequestWrapper, offer)) {

                    OfferPrice discountAmount = new OfferPrice();
                    discountAmount.setDollarAmount(0);

                    if (Objects.nonNull(offer.getAttributes())
                            && CollectionUtils.isNotEmpty(offer.getAttributes().getAssociatedProducts())) {
                        AssociatedProduct associatedProduct = offer.getAttributes().getAssociatedProducts().get(0);
                        if (Objects.nonNull(associatedProduct)
                                && CollectionUtils.isNotEmpty(associatedProduct.getBundleProducts())) {
                            associatedProduct.getBundleProducts().stream().filter(Objects::nonNull)
                                    .forEach(bundleProduct -> {
                                        if (CollectionUtils.isNotEmpty(bundleProduct.getProducts())) {
                                            bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(prod -> {
                                                if (Objects.nonNull(prod.getObj())
                                                        && CollectionUtils.isNotEmpty(prod.getObj().getVariants())
                                                        && CollectionUtils.isNotEmpty(
                                                        prod.getObj().getVariants().get(0).getPrices())) {
                                                    // Assuming there is one price under the product response
                                                    Price price = prod.getObj().getVariants().get(0).getPrices().get(0);
                                                    if (Objects.nonNull(price.getValue())
                                                            && Objects.nonNull(price.getValue().getDollarAmount())) {
                                                        discountAmount.setDollarAmount(offersUtils
                                                                .getTwoDigitRoundOffValue(discountAmount.getDollarAmount()
                                                                        + price.getValue().getDollarAmount()));
                                                    }
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                    if (Objects.nonNull(offer.getAttributes()) && Objects.nonNull(offer.getAttributes().getOfferPrice())) {
                        discountAmount
                                .setDollarAmount(offersUtils.getTwoDigitRoundOffValue(discountAmount.getDollarAmount()
                                        - offer.getAttributes().getOfferPrice().getDollarAmount()));
                    }

                    offer.getAttributes().setDiscountAmount(discountAmount);
                }

            });
        }
    }

    /**
     * This method is used to fetch hardware offers for agent/indirect flow
     *
     * @param offerRequestWrapper the offer request context
     * @param videoDeviceOffers   the list of video device offers
     * @param selectedOffer       the list of selected CT offers
     * @return the list of video device offers with instant rebate processing applied
     */
    public List<CTOffer> getHardwareInstantRebateOffers(OfferRequestWrapper offerRequestWrapper, List<CTOffer> videoDeviceOffers, List<CTOffer> selectedOffer) {
        if (CollectionUtils.isNotEmpty(videoDeviceOffers)) {

            satelliteSegmentationProcessor.performSegmentation(offerRequestWrapper, videoDeviceOffers, selectedOffer, false, new HashMap<>());

            videoDeviceOffers.stream().filter(Objects::nonNull).forEach(offer -> {
                offer.getAttributes().setMinSelected(0);
                offer.getAttributes().getBenefits().sort(Comparator.comparing(Benefit::getRebatePriority));

                if (featureHelper.isEnabled(Constants.FEATURE_MDU_DTH_ENABLED)) {
                    ProductObj productObj = offersUtils.getProductObjFromOffer(offer);
                    Variant variant = productObj.getVariants().get(0);
                    if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getChannelEligibility()) && Constants.MDU_DTH
                            .equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getChannelEligibility().getSalesSubChannel())) {
                        if (Constants.GENIE_HD_DVR.equalsIgnoreCase(variant.getAttributes().getDeviceType())) {
                            offer.getAttributes().setLeadDeviceOffer(true);
                        }
                    } else {
                        if (Constants.GENIE_SERVER.equalsIgnoreCase(variant.getAttributes().getDeviceType())) {
                            offer.getAttributes().setLeadDeviceOffer(true);
                        }
                    }
                }

            });

        }
        return videoDeviceOffers;
    }

    /**
     * This method is used to get the product codes from order context
     *
     * @param offerRequestWrapper the offer request wrapper containing the order context
     * @param productType         the product type to filter by
     * @return a HashMap of product codes for the specified product type
     */
    private HashMap<String, String> getProductsFromOrderContext(OfferRequestWrapper offerRequestWrapper,
                                                                String productType) {
        HashMap<String, String> orderContextProducts = new HashMap<>();

        offerRequestWrapper.getOfferRequest().getOrderContext().stream().filter(Objects::nonNull).filter(lineItem -> Constants.ADD.equalsIgnoreCase(lineItem.getLineItem().getAction())
                || Constants.PRESERVE.equalsIgnoreCase(lineItem.getLineItem().getAction())).forEach(lineItem -> {
            if (productType.equalsIgnoreCase(lineItem.getLineItem().getCustomData().getProductType())) {

                String priceCode = getPriceCode(lineItem);

                if (Constants.VIDEO_ADDON.equalsIgnoreCase(productType)
                        && Constants.PREMIUM_PICK5_MASKS.containsKey(lineItem.getLineItem().getLineItemIdentifier().getProductId())) {
                    int pickValue = Constants.PREMIUM_PICK5_MASKS.get(lineItem.getLineItem().getLineItemIdentifier().getProductId());

                    String binaryPick = Integer.toBinaryString(pickValue);
                    binaryPick = StringUtils.leftPad(binaryPick, 5, "0");

                    char[] binaryPicks = binaryPick.toCharArray();

                    for (int i = 0, j = 4; i < 5; i++, j--) {
                        if (binaryPicks[i] == '1') {
                            Double individualPick = Math.pow(2, j);
                            orderContextProducts.put(Constants.PREMIUM_PICKS.get(individualPick.intValue()) + "/" + priceCode,
                                    lineItem.getLineItem().getLineItemIdentifier().getProductName());
                        }
                    }
                } else if (!Constants.BPC_4K_SERVICE.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.BPC_4K_VOD.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.SPORTS_PACK_SC1.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.SPORTS_PACK_SC2.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.BOLTON_NO_LOCALS_SAVINGS.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.TRACKING_CODE_12M.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.TRACKING_CODE_24M.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.TRACKING_CODE_M2M.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())
                        && !Constants.TRACKING_CODE_MEP.equals(lineItem.getLineItem().getLineItemIdentifier().getProductId())) {
                    orderContextProducts.put(lineItem.getLineItem().getLineItemIdentifier().getProductId() + "/" + priceCode,
                            lineItem.getLineItem().getLineItemIdentifier().getProductName());
                }

            }
        });

        return orderContextProducts;
    }

    /**
     * This method return the price code from lineItemDetail
     *
     * @param lineItem the STMSBOMLineItem to extract the price code from
     * @return the cleaned price code string
     */
    private String getPriceCode(STMSBOMLineItem lineItem) {
        String priceCode = Constants.EMPTYSTRING;
        if (Objects.nonNull(lineItem.getLineItem().getLineItemDetail().getProgramming())) {
            priceCode = lineItem.getLineItem().getLineItemDetail().getProgramming().getPriceCode();
        }
        if (Objects.nonNull(priceCode)) {
            priceCode = priceCode.replaceFirst(Constants.BILLING_REFERENEID_PREFIX, "");
        }
        return priceCode;
    }

    /**
     * Extracts offer IDs from the order context in the offer request wrapper.
     * Only line items with action ADD or PRESERVE and non-blank offer IDs are considered.
     *
     * @param offerRequestWrapper the offer request wrapper containing the order context
     * @return a list of offer IDs from the order context
     */
    private List<String> getOfferIdsFromOrderContext(OfferRequestWrapper offerRequestWrapper) {
        List<String> orderContextOfferIds = new ArrayList<>();

        offerRequestWrapper.getOfferRequest().getOrderContext().stream().filter(Objects::nonNull).filter(lineItem -> Constants.ADD.equalsIgnoreCase(lineItem.getLineItem().getAction())
                || Constants.PRESERVE.equalsIgnoreCase(lineItem.getLineItem().getAction())).forEach(lineItem -> {
            if (StringUtils.isNotBlank(lineItem.getLineItem().getCustomData().getOfferId())) {
                orderContextOfferIds.add(lineItem.getLineItem().getCustomData().getOfferId());
            }
        });

        return orderContextOfferIds;
    }

    /**
     * This method is used to get the bundle product codes from offer
     *
     * @param offer the CTOffer to extract bundle product codes from
     * @return a list of bundle product codes from the offer
     */
    private List<String> getBundleProductsFromOffer(CTOffer offer) {
        List<String> bundleProducts = new ArrayList<>();

        String priceCode = offer.getAttributes().getBillingId();

        offer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProd -> {
            associatedProd.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                bundleProduct.getProducts().stream().filter(Objects::nonNull).forEach(product -> {
                    product.getObj().getVariants().stream().filter(Objects::nonNull).forEach(variant -> {
                        if (Constants.VIDEO_ADDON.equalsIgnoreCase(offer.getAttributes().getOfferProductType())
                                && StringUtils.isNotBlank(variant.getAttributes().getPick5Mask())) {
                            int pickValue = Integer.parseInt(variant.getAttributes().getPick5Mask());

                            String binaryPick = Integer.toBinaryString(pickValue);
                            binaryPick = StringUtils.leftPad(binaryPick, 5, "0");

                            char[] binaryPicks = binaryPick.toCharArray();

                            for (int i = 0, j = 4; i < 5; i++, j--) {
                                if (binaryPicks[i] == '1') {
                                    Double individualPick = Math.pow(2, j);
                                    bundleProducts.add(Constants.PREMIUM_PICKS.get(individualPick.intValue()) + "/" + priceCode);
                                }
                            }

                        } else if (Constants.VIDEO_DEVICE.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                            bundleProducts.add(variant.getAttributes().getBillingProductCode() + "/");
                        } else {
                            bundleProducts.add(variant.getAttributes().getBillingProductCode() + "/" + priceCode);
                        }

                    });
                });
            });
        });

        return bundleProducts;
    }

    /**
     * This method converts the individual premium pick products to final pick
     *
     * @param bundleProducts list of bundle product codes
     * @return the final pick value as a string
     */
    private String convertToFinalPick(List<String> bundleProducts) {
        List<Integer> pickValues = new ArrayList<>();
        bundleProducts.forEach(product -> {
            if (Constants.PREMIUM_PICK5_MASKS.containsKey(product.substring(0, product.indexOf("/")))) {
                pickValues.add(Constants.PREMIUM_PICK5_MASKS.get(product.substring(0, product.indexOf("/"))));
            }
        });
        int finalPickValue = pickValues.stream().mapToInt(Integer::intValue).sum();
        if (Constants.PREMIUM_PICKS.get(finalPickValue) != null) {
            return Constants.PREMIUM_PICKS.get(finalPickValue) + "/1";
        }
        return null;
    }

    /**
     * This method is used to calculate the quantity based on offer id
     *
     * @param offerRequestWrapper the offer request wrapper containing the order context
     * @param offerId             the offer ID to count
     * @return the total quantity of the specified offer ID
     */
    private Integer getOfferQuantity(OfferRequestWrapper offerRequestWrapper, String offerId) {
        List<Integer> offerQty = new ArrayList<>();

        offerRequestWrapper.getOfferRequest().getOrderContext().stream().filter(Objects::nonNull).filter(lineItem -> Constants.ADD.equalsIgnoreCase(lineItem.getLineItem().getAction())
                || Constants.PRESERVE.equalsIgnoreCase(lineItem.getLineItem().getAction())).forEach(lineItem -> {
            if(offerRequestWrapper.getOfferRequest().getSalesChannel().contains(Constants.ONLINE)) {
              if (Constants.VIRTUAL_TYPE.equalsIgnoreCase(lineItem.getLineItem().getLineItemType()) 
                  && offerId.equalsIgnoreCase(lineItem.getLineItem().getCustomData().getOfferId())) {
                offerQty.add(1);
              }

            } else {
              if ((Constants.TYPE_RECEIVER.equalsIgnoreCase(lineItem.getLineItem().getLineItemType()) 
                  || Constants.TYPE_ACCESSORY.equalsIgnoreCase(lineItem.getLineItem().getLineItemType())) 
                  && offerId.equalsIgnoreCase(lineItem.getLineItem().getCustomData().getOfferId())) {
                offerQty.add(1);
              }						
            }
        });
        return offerQty.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * This method is used to calculate the quantity based on product id
     *
     * @param offerRequestWrapper the offer request wrapper containing the order context
     * @param productId           the list of product IDs to count
     * @return the total quantity of the specified product IDs
     */
    private Integer getProductQuantity(OfferRequestWrapper offerRequestWrapper, List<String> productId) {
        List<Integer> productQty = new ArrayList<>();

        offerRequestWrapper.getOfferRequest().getOrderContext().stream().filter(Objects::nonNull).filter(lineItem -> Constants.ADD.equalsIgnoreCase(lineItem.getLineItem().getAction())
                || Constants.PRESERVE.equalsIgnoreCase(lineItem.getLineItem().getAction())).forEach(lineItem -> {
            if ((Constants.TYPE_RECEIVER.equalsIgnoreCase(lineItem.getLineItem().getLineItemType())
                    || Constants.TYPE_ACCESSORY.equalsIgnoreCase(lineItem.getLineItem().getLineItemType()))
                    && productId.contains(lineItem.getLineItem().getLineItemIdentifier().getProductId() + "/")) {
                productQty.add(1);
            }
        });
        return productQty.stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * This method is used to perform order modification changes
     *
     * @param eligibleOffers      the CTOfferResponse containing eligible offers
     * @param offerRequestWrapper the offer request wrapper with order context
     * @param allOffers           the CTOfferResponse containing all offers
     * @param productType         the product type to process
     */
    public void applyOrderModChanges(CTOfferResponse eligibleOffers, OfferRequestWrapper offerRequestWrapper,
                                     CTOfferResponse allOffers, String productType) {

        if (CollectionUtils.isNotEmpty(offerRequestWrapper.getOfferRequest().getOrderContext())) {
            List<String> productsSelected = new ArrayList<>();
            List<String> offersSelected = new ArrayList<>();
            List<String> ineligibleProducts = new ArrayList<>();
            List<IneligibleOffer> ineligibleOffers = new ArrayList<>();
            List<String> orderContextOfferIds = getOfferIdsFromOrderContext(offerRequestWrapper);
            HashMap<String, String> orderContextProducts = getProductsFromOrderContext(offerRequestWrapper, productType);

            log.debug("orderContextOfferIds [{}] : ", orderContextOfferIds);
            log.debug("orderContextProducts [{}] : ", orderContextProducts);

            // Find matching offer for lineitems with benefits using offerId
            eligibleOffers.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {

                // For direct benefit association
                if (orderContextOfferIds.contains(offer.getCode())) {
                    offer.getAttributes().setIsSelected(true);
                    offersSelected.add(offer.getCode());
                    productsSelected.addAll(getBundleProductsFromOffer(offer));

                    if (Constants.VIDEO_DEVICE.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                        offer.getAttributes().setQtySelected(getOfferQuantity(offerRequestWrapper, offer.getCode()));
                    }
                }

            });

            // Remove offers that already matched
            orderContextOfferIds.removeIf(offersSelected::contains);

            // Remove products that already matched
            orderContextProducts.entrySet().removeIf(entry -> productsSelected.contains(entry.getKey()));

            // Find matching offer using productId
            eligibleOffers.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                if (Objects.isNull(offer.getAttributes().getIsSelected()) || Boolean.FALSE.equals(offer.getAttributes().getIsSelected())) {
                    List<String> bundleProducts = getBundleProductsFromOffer(offer);
                    if (orderContextProducts.keySet().containsAll(bundleProducts)) {
                        offer.getAttributes().setIsSelected(true);
                        productsSelected.addAll(bundleProducts);

                        if (Constants.VIDEO_DEVICE.equalsIgnoreCase(offer.getAttributes().getOfferProductType())) {
                            offer.getAttributes().setQtySelected(getProductQuantity(offerRequestWrapper, bundleProducts));
                        }
                    }
                }
            });

            // Find matching included product using productId
            if (CollectionUtils.isNotEmpty(eligibleOffers.getIncludedProducts())) {
                eligibleOffers.getIncludedProducts().stream().filter(Objects::nonNull).forEach(product -> {
                    product.getVariants().get(0).getPrices().stream().filter(Objects::nonNull).forEach(price -> {
                        if (Objects.nonNull(price.getBillingReferenceId())) {
                            String priceCode = price.getBillingReferenceId().replaceFirst(Constants.BILLING_REFERENEID_PREFIX, "");
                            if (Constants.EMBEDDED_SVOD.equalsIgnoreCase(product.getVariants().get(0).getAttributes().getSubCategory())
                                    && orderContextProducts.keySet().contains(product.getVariants().get(0).getAttributes().getBillingProductCode() + "/"
                                    + priceCode)) {
                                productsSelected.add(product.getVariants().get(0).getAttributes().getBillingProductCode()
                                        + "/" + priceCode);
                            }
                        }
                    });
                });
            }

            // Remove products that already matched
            orderContextProducts.entrySet().removeIf(entry -> productsSelected.contains(entry.getKey()));

            if (!orderContextProducts.isEmpty()) {

                // Find ineligible offer using productId
                allOffers.getOffers().stream().filter(Objects::nonNull).forEach(offer -> {
                    if (Objects.isNull(offer.getAttributes().getBillingSystem()) || Constants.STMS.equalsIgnoreCase(offer.getAttributes().getBillingSystem())) {
                        List<String> bundleProducts = getBundleProductsFromOffer(offer);
                        if (orderContextProducts.keySet().containsAll(bundleProducts) && !ineligibleProducts.containsAll(bundleProducts)) {
                            IneligibleOffer ineligibleOffer = new IneligibleOffer();
                            ineligibleOffer.setOfferId(offer.getCode());
                            if (Objects.nonNull(offer.getName())) {
                                ineligibleOffer.setProductName(offer.getName().getEn());
                            }
                            bundleProducts.forEach(product -> {
                                ineligibleOffer.setProductId(product.substring(0, product.indexOf("/")));
                                ineligibleProducts.add(product);
                                ineligibleOffers.add(ineligibleOffer);
                            });
                            productsSelected.addAll(bundleProducts);
                        }
                    }
                });
            }

            // Remove products that already matched
            orderContextProducts.entrySet().removeIf(entry -> productsSelected.contains(entry.getKey()));

            if (!orderContextProducts.isEmpty()) {

                String finalProduct = convertToFinalPick(new ArrayList<>(orderContextProducts.keySet()));

                if (StringUtils.isNotEmpty(finalProduct)) {
                    Optional<Entry<String, String>> matchingObject = orderContextProducts.entrySet().stream().filter(Objects::nonNull).
                            filter(p -> Constants.PREMIUM_PICK5_MASKS.containsKey(p.getKey().substring(0, p.getKey().indexOf("/")))).
                            findFirst();
                    orderContextProducts.put(finalProduct, matchingObject.get().getValue());
                }

                // Add any remaining products to ineligible offers
                orderContextProducts.forEach((productId, productName) -> {
                    if (!Constants.INDIVIDUAL_PREMIUM_PICKS.containsKey(productId.substring(0, productId.indexOf("/")))
                            || Constants.INDIVIDUAL_PREMIUM_PICKS.containsKey(finalProduct.substring(0, finalProduct.indexOf("/")))) {
                        if (!ineligibleProducts.contains(productId)) {
                            IneligibleOffer ineligibleOffer = new IneligibleOffer();
                            ineligibleOffer.setProductId(productId.substring(0, productId.indexOf("/")));
                            ineligibleOffer.setProductName(productName);
                            ineligibleProducts.add(productId);
                            ineligibleOffers.add(ineligibleOffer);
                        }
                    }
                });
            }

            eligibleOffers.setIneligibleOffers(ineligibleOffers);
        }

    }

    private void updateOfferPriceWithLocals(CTOffer offer, OfferPrice offerPrice, double localsBoltonPrice) {
        if (Objects.nonNull(offer) && Objects.nonNull(offerPrice) && Objects.nonNull(offerPrice.getDollarAmount())) {
            Double offerResult = Stream.of(offerPrice.getDollarAmount(), localsBoltonPrice)
                    .mapToDouble(Double::doubleValue).sum();
            offerResult = offersUtils.getTwoDigitRoundOffValue(offerResult);
            OfferPrice offerPriceWithLocals = new OfferPrice();
            offerPriceWithLocals.setDollarAmount(offerResult);
            offer.getAttributes().setOfferPricewithLocals(offerPriceWithLocals);
        }
    }

    private void updatePlanPriceWithLocals(CTOffer offer, List<Price> planPrice, double localsBoltonPrice) {
        if (Objects.nonNull(offer) && CollectionUtils.isNotEmpty(planPrice)
                && Objects.nonNull(planPrice.get(0)) && Objects.nonNull(planPrice.get(0).getValue())
                && Objects.nonNull(planPrice.get(0).getValue().getDollarAmount())) {
            Double priceResult = Stream.of(planPrice.get(0).getValue().getDollarAmount(), localsBoltonPrice)
                    .mapToDouble(Double::doubleValue).sum();
            priceResult = offersUtils.getTwoDigitRoundOffValue(priceResult);
            OfferPrice offerPlanPriceWithLocals = new OfferPrice();
            offerPlanPriceWithLocals.setDollarAmount(priceResult);
            offer.getAttributes().setPlanPricewithLocals(offerPlanPriceWithLocals);
        }
    }
    

    public boolean isInCompatibleOffer(CTOffer segmentOffer, List<CTOffer> selectedOffer) {
    	
		if(CollectionUtils.isNotEmpty(selectedOffer) && Objects.nonNull(segmentOffer) && Objects.nonNull(segmentOffer.getAttributes()) 
				&& CollectionUtils.isNotEmpty(segmentOffer.getAttributes().getAssociatedProducts())) {
			for(AssociatedProduct associatedProduct: segmentOffer.getAttributes().getAssociatedProducts()) {
				if(CollectionUtils.isNotEmpty(associatedProduct.getQualifyingProducts())) {
					for(ProductWrapper qualifyingProduct:associatedProduct.getQualifyingProducts()) {
						if(Objects.nonNull(qualifyingProduct.getConstraints()) 
								&& CollectionUtils.isNotEmpty(qualifyingProduct.getConstraints().getQualifierIncompatibleProducts())) {
							for(Product incompatibleProduct:qualifyingProduct.getConstraints().getQualifierIncompatibleProducts()) {
								for(CTOffer offer:selectedOffer) {
									ProductObj productObj=offersUtils.getProductObjFromOffer(offer);
									if(Objects.nonNull(productObj) && productObj.getCode().equalsIgnoreCase(incompatibleProduct.getKey())) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
    
}
 