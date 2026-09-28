package com.dtv.dcp.epoch.processor.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.model.common.request.OfferRequestWrapper;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.OffersUtils;

@Component
public class CPOPBenefitsHelper {

	private static final String ONLINE="online";

    public List<Benefit> filterByDate(List<Benefit> benefitList) {

        List<Benefit> list = new ArrayList<>();
        if (benefitList != null) {
            benefitList.stream().filter(Objects::nonNull).forEach(benefit -> {
                if (benefit.getStartDate() != null && benefit.getEndDate() != null && OffersUtils.validateActiveDates(OffersUtils.getFormattedDate(benefit.getStartDate()),
                        OffersUtils.getFormattedDate(benefit.getEndDate()))) {
                    list.add(benefit);
                }
            });
        }
        return list;
    }

    /**
     * @param ctOfferResponse
     * @throws ServiceException
     */
    public CTOfferResponse filterInvalidBenefits(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) throws ServiceException {

        String channel = "";
        List<String> offerActionType = Stream.of(Constants.ACQUISITION).collect(Collectors.toList());
        if(Objects.nonNull(offerRequestWrapper.getOfferRequest().getOfferActionType())){
            offerActionType = offerRequestWrapper.getOfferRequest().getOfferActionType();
        }
        //BYODOPC-4674 remove usage of dotcomAgent
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getSalesChannel())) {
            if(offerActionType.contains(Constants.ACQUISITION) || offerActionType.contains(Constants.UPSELL)
            || offerActionType.contains(Constants.CLOSING_ACTION_TYPE))
            {
                if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> s.equalsIgnoreCase("opus") || s.equalsIgnoreCase(Constants.ASSISTED_SALES))) {
                    channel = Constants.AGENT_NEW;

                } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
                    channel = Constants.SELF_SERVICE_NEW;
                }
            }else{
                //BYODOPC-4674 remove usage of dotcomAgent.
                if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> s.equalsIgnoreCase("opus")
                                || s.equalsIgnoreCase(Constants.ASSISTED_SALES)
                                        || s.equalsIgnoreCase(Constants.DTV360)
                                        || s.equalsIgnoreCase(Constants.UVC)
                                //|| s.equalsIgnoreCase(Constants.DOT_COM_AGENT)
                            )) {
                    channel = Constants.AGENT_EXISTING;

                } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
                    channel = Constants.SELF_SERVICE_EXISTING;
                }
            }

        }

        String salesChannel = channel;


        if (Objects.nonNull(ctOfferResponse.getOffers())) {
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                List<Benefit> finalBenefitList = new ArrayList<>();

              if(ctOffer.getAttributes() !=null && ctOffer.getAttributes().isMigratedOffer()) {
                final String[] billingProductCode = {""};
                if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null) {
                    ctOffer.getAttributes().getAssociatedProducts().stream().filter(Objects::nonNull).forEach(associatedProduct -> {
                        if (associatedProduct.getBundleProducts() != null) {
                            associatedProduct.getBundleProducts().stream().filter(Objects::nonNull).forEach(bundleProduct -> {
                                if (Objects.nonNull(bundleProduct.getProducts())) {
										bundleProduct.getProducts().stream().filter(Objects::nonNull)
												.forEach(product -> {
													if (null != product.getObj()) {
														billingProductCode[0] = product.getObj().getCode();
													}
												}
                                    );
                                }

                            });
                        }
                    });
                }
                List<Benefit> filteredBenefitsList = new ArrayList<>();
                List<Benefit> benefitList = ctOffer.getAttributes().getBenefits();
                if (Optional.ofNullable(ctOffer.getAttributes().getOfferPromos()).isPresent() && !ctOffer.getAttributes().getOfferPromos().isEmpty()) {
                    ctOffer.getAttributes().getOfferPromos().stream().filter(Objects::nonNull).forEach(benefit -> {
                        Promotion promo = null;
                        if (Optional.ofNullable(benefit).isPresent() && !benefit.isEmpty()) {
                            promo = new Promotion();
                            for (GenericNameValueBase offerPromo : benefit) {
                                if (Optional.ofNullable(offerPromo.getName()).isPresent() && offerPromo.getName().equalsIgnoreCase("promoId")) {
                                    promo.setPromoId(offerPromo.getValue());
                                }
                                if (ctOffer.getAttributes().isMigratedOffer()) {
                                    if (Optional.ofNullable(offerPromo.getName()).isPresent() && offerPromo.getName().equalsIgnoreCase("startDateStr")) {
                                        promo.setStartDate(offerPromo.getValue());
                                    }
                                    if (Optional.ofNullable(offerPromo.getName()).isPresent() && offerPromo.getName().equalsIgnoreCase("endDateStr")) {
                                        promo.setEndDate(offerPromo.getValue());
                                    }
                                } else {
                                    promo.setStartDate(ctOffer.getStartDate());
                                    promo.setEndDate(ctOffer.getEndDate());
                                }
                            }
                        }
                       if(null != promo) {
	                      boolean validateActiveDate = OffersUtils.validateActiveDates(promo.getStartDate(), promo.getEndDate());
	                        if (Optional.ofNullable(promo).isPresent() && Optional.ofNullable(promo.getPromoId()).isPresent()
	                                && validateActiveDate  &&   (Optional.ofNullable(benefitList).isPresent() && !benefitList.isEmpty())) {
	
	                          
	                                final Promotion promoObj = promo;
	
	                                // Filter Promotions salesPromo validity
	                                benefitList.stream().filter(Objects::nonNull).forEach(offerBenefit -> {
	                                    if (offerBenefit.getBillingBenefitCode().equalsIgnoreCase(promoObj.getPromoId()) && (!filteredBenefitsList.contains(offerBenefit)) ) {
	                                        
	                                            filteredBenefitsList.add(offerBenefit);
	                                        }
	                              
	                                });
	                            
	                        }
                       }
                    });
                }
                // Filter Promotions based on sales channel and contract indicator
                filteredBenefitsList.stream().filter(Objects::nonNull)
                        .forEach(promotion -> {
                        	
                            List<String> SalesChannel = getSalesChannels(promotion.getSalesChannel());
                            if (SalesChannel.stream().anyMatch(s -> s.equalsIgnoreCase(salesChannel) || s.equalsIgnoreCase("All")) &&
                                    (promotion.getContractIndicator() !=null && (promotion.getContractIndicator().equalsIgnoreCase(ctOffer.getAttributes().getContractIndicator()) || 
                                    		"All".equalsIgnoreCase(promotion.getContractIndicator())))) {

                                String promotionSourceSku = promotion.getPromoSourceSku();

                                if (Optional.ofNullable(promotionSourceSku).isPresent() && !promotionSourceSku.isEmpty()) {

                                    List<String> promoSourceSku = Arrays.asList(promotionSourceSku.split(","));
                                    promoSourceSku = promoSourceSku.stream().filter(Objects::nonNull).map(String::trim)
                                            .collect(Collectors.toList());

                                    if (Optional.ofNullable(promoSourceSku).isPresent() && !promoSourceSku.isEmpty()) {
                                        promoSourceSku.stream().filter(Objects::nonNull).forEach(promoSku -> {
                                            if (billingProductCode[0].equalsIgnoreCase(promoSku) && (!finalBenefitList.contains(promotion))) {
                                                    finalBenefitList.add(promotion);
                                            }
                                        });
                                    }
                                } else {
                                    if (!finalBenefitList.contains(promotion)) {
                                        finalBenefitList.add(promotion);
                                    }
                                }

                            }

                        });
                }else {
                  if(null != ctOffer.getAttributes().getBenefits())
                  {
                      finalBenefitList.addAll(ctOffer.getAttributes().getBenefits());
                  }
                }
          
                    ctOffer.getAttributes().setBenefits(finalBenefitList);
               });
        }
        return ctOfferResponse;
    }
    public CTOfferResponse filterOffersBySalesChannelOfBenefits(CTOfferResponse ctOfferResponse, OfferRequestWrapper offerRequestWrapper) throws ServiceException {

        String channel = "";
        List<CTOffer> listOfCtOffers = new ArrayList<>();
         //BYODOPC-4674 remove usage of dotcomAgent
        if (Objects.nonNull(offerRequestWrapper.getOfferRequest().getSalesChannel())) {
            if(offerRequestWrapper.getOfferRequest().getOfferActionType().contains(Constants.ACQUISITION))
            {
                if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> s.equalsIgnoreCase("opus") || s.equalsIgnoreCase(Constants.ASSISTED_SALES))) {
                    channel = Constants.AGENT_NEW;

                } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
                    channel = Constants.SELF_SERVICE_NEW;
                }
            }else{
                //BYODOPC-4674 remove usage of dotcomAgent.
                if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> s.equalsIgnoreCase("opus")
                                || s.equalsIgnoreCase(Constants.ASSISTED_SALES)
                                        || s.equalsIgnoreCase(Constants.DTV360)
                                        || s.equalsIgnoreCase(Constants.UVC)
                                //|| s.equalsIgnoreCase(Constants.DOT_COM_AGENT)
                            )) {
                    channel = Constants.AGENT_EXISTING;

                } else if (offerRequestWrapper.getOfferRequest().getSalesChannel().stream()
                        .anyMatch(s -> OffersUtils.checkOnlineRelatedChannel(s))) {
                    channel = Constants.SELF_SERVICE_EXISTING;
                }
            }

        }

        String salesChannel = channel;


        if (Objects.nonNull(ctOfferResponse.getOffers())) {
            ctOfferResponse.getOffers().stream().filter(Objects::nonNull).forEach(ctOffer -> {
                 List<String> listOfOfferIds = new ArrayList<>();
                if(ctOffer.getAttributes() !=null && ctOffer.getAttributes().isMigratedOffer()) {


                    // Filter Promotions based on sales channel and contract indicator
                    ctOffer.getAttributes().getBenefits().stream().filter(Objects::nonNull)
                            .forEach(promotion -> {
                                List<String> SalesChannel = getSalesChannels(promotion.getSalesChannel());
                                if (SalesChannel.stream().anyMatch(s -> s.equalsIgnoreCase(salesChannel) || s.equalsIgnoreCase("All")) &&
                                          (promotion.getContractIndicator() != null && promotion.getContractIndicator().equalsIgnoreCase(offerRequestWrapper.getOfferRequest().getContractIndicator().get(0)) ||
                                                "All".equalsIgnoreCase(promotion.getContractIndicator())) && (!listOfOfferIds.contains(ctOffer.getId()))) {
                                    
                                        listOfOfferIds.add(ctOffer.getId());
                                        listOfCtOffers.add(ctOffer);
                                    }
                                

                            });
                }else {

                    List<String> tmpSalesChannel = null;
                    String incomingSalesChannel = offerRequestWrapper.getOfferRequest().getSalesChannel().get(0);
                        if (ctOffer.getAttributes().getEligibility() !=null &&
                                ctOffer.getAttributes().getEligibility().getConstraints() !=null &&
                                ctOffer.getAttributes().getEligibility().getConstraints().get(0) != null && ctOffer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel() != null) {
                            tmpSalesChannel = ctOffer.getAttributes().getEligibility().getConstraints().get(0).getSalesChannel();
                            if (!tmpSalesChannel.isEmpty() && (tmpSalesChannel.contains(incomingSalesChannel))) {

                                    listOfCtOffers.add(ctOffer);
                                
                            }
                        }

                }

            });
        }
        ctOfferResponse.setOffers(listOfCtOffers);
        return ctOfferResponse;
    }

    /**
     * '
     *
     * @param salesChannel
     * @return
     */
    public List<String> getSalesChannels(String salesChannel) {
        List<String> salesChannels = new ArrayList<>();
        if (Optional.ofNullable(salesChannel).isPresent()) {
            salesChannels = Arrays.asList(salesChannel.split(Constants.PIPE));
            salesChannels = salesChannels.stream().map(String::trim).collect(Collectors.toList());
        }
        return salesChannels;
    }


}
