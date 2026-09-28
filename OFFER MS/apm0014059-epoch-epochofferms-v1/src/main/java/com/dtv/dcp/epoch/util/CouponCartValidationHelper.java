package com.dtv.dcp.epoch.util;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClientHelper;
import com.dtv.dcp.epoch.integration.EpochUCCPrimaryClient;
import com.dtv.dcp.epoch.model.common.request.Campaign;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.request.CustomerAddress;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.response.CampaignProducts;
import com.dtv.dcp.epoch.model.common.response.ucc.*;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.offer.AdditionalEligibility;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.offer.OfferAttributes;
import com.dtv.dcp.epoch.model.ct.offer.OnlinePartnerDetails;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.request.CTOfferRequest;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.model.customergraph.CustomerCoupons;
import com.dtv.dcp.epoch.service.CouponDetailsFromAccountService;
import com.dtv.dcp.epoch.service.DMALookUpService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CouponCartValidationHelper {

	private static final Logger log = LoggerFactory.getLogger(CouponCartValidationHelper.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	@Autowired
    private DMALookUpService dmaLookUpService;
	
	@Autowired
	EpochUCCPrimaryClient epochUCCPrimaryClient;

	@Value("${apiclient.rest.cpopofferms.reconnectEligibilityWindow}")
	private int reconnectEligibilityWindow;

	@Autowired
	RedisCacheHelper redisCacheHelper;

	@Autowired
	private CouponDetailsFromAccountService couponDetailsFromAccountService;

	@Autowired
	CpopClientHelper cpopClientHelper;

    @Autowired
    FeatureManagerHelper featureManagerHelper;

	public ValidateCoupon processValidateCouponResponse(CTOfferResponse offerResponse,
                                                        CouponOffersRequest couponOffersRequest, com.dtv.dcp.epoch.model.ct.coupon.Coupon ctCoupon) {

        log.info("ValidateCoupon processValidateCouponResponse - start");
		ValidateCoupon validateCoupon = new ValidateCoupon();
		ValidationResults validationResults = new ValidationResults();
        List<String> offersForRequestedCoupons = null;

        if (Objects.nonNull(offerResponse) && CollectionUtils.isNotEmpty(offerResponse.getOffers())) {
            offersForRequestedCoupons = offerResponse.getOffers().stream().filter(Objects::nonNull).map(CTOffer::getCode).collect(Collectors.toList());
            // DMA Filter Eligibility check
            if (Objects.nonNull(couponOffersRequest.getCustomerAddress())) {
                filterOfferBasedOnZipOrDMA(offerResponse, couponOffersRequest.getCustomerAddress());
            } else {
                filterOfferOnEmptyCustomerAddress(offerResponse);
		}
            // Additional Eligibility check
            offerResponse.getOffers().removeIf(offer -> additionalEligibilityCheck(couponOffersRequest, offer).contains(offer.getCode()));
        }
        log.debug("ValidateCoupon response mapping : [{}]", offerResponse);
        List<Offer> offers = processVaidateOfferResponse(offerResponse.getOffers(), couponOffersRequest);

        if (Objects.nonNull(ctCoupon)) {
            if (StringUtils.isNotEmpty(ctCoupon.getId())) {
				validateCoupon.setId(ctCoupon.getId());
			}
            if (StringUtils.isNotEmpty(ctCoupon.getCode())) {
				validateCoupon.setCode(ctCoupon.getCode());
			}
            if (Objects.nonNull(ctCoupon.getName())) {
				validateCoupon.setName(ctCoupon.getName().getEn());
			}
            if (Objects.nonNull(ctCoupon.getDescription())) {
				validateCoupon.setDescription(ctCoupon.getDescription().getEn());
			}

            if (CollectionUtils.isNotEmpty(offers)) {
                if (StringUtils.isNotEmpty(ctCoupon.getStatus())) {
				validateCoupon.setStatus(ctCoupon.getStatus());
			}
                validateCoupon.setOffersForCouponsRequested(offersForRequestedCoupons);
			validationResults.setStatus("Success");
			validateCoupon.setValidationResults(validationResults);
			validateCoupon.setOffers(offers);
            } else {
                validateCoupon.setOffersForCouponsRequested(offersForRequestedCoupons);
                validateCoupon.setValidationResults(createValidationResults(Constants.SEVERITY_ERROR, "COUPON_INELGIBLE_ERROR", Constants.COUPON_INELGIBLE_ERROR));
		}
        }
        log.info("ValidateCoupon processValidateCouponResponse : [{}]", validateCoupon);
		return validateCoupon;
	}
	
    public static List<String> additionalEligibilityCheck(CouponOffersRequest couponOffersRequest, CTOffer offer) {
    List<String> invalidOfferIds = new ArrayList<>();
    if (Objects.nonNull(offer.getAttributes()) && CollectionUtils.isNotEmpty(offer.getAttributes().getAdditionalEligibility())) {
       offer.getAttributes().getAdditionalEligibility().forEach(additionalEligibility -> {
                if (CollectionUtils.isNotEmpty(additionalEligibility.getOfferIds()) && StringUtils.isNotEmpty(additionalEligibility.getMinCount())
                        && StringUtils.isNotEmpty(additionalEligibility.getMaxCount())) {
                    List<CartOffer> cartOffers = getMatchingCartOfferCodes(additionalEligibility.getOfferIds(), couponOffersRequest);
                    if (CollectionUtils.isNotEmpty(cartOffers)) {
                        cartOffers.forEach(cartOffer -> {
                            if (null != cartOffer.getQuantity()) {
                if (!(cartOffer.getQuantity() >= Integer.parseInt(additionalEligibility.getMinCount())
                      && cartOffer.getQuantity() <= Integer.parseInt(additionalEligibility.getMaxCount()))) {
                   invalidOfferIds.add(offer.getCode());
                }
                            } else {
                                invalidOfferIds.add(offer.getCode());
             }
          });
                    } else {
                        invalidOfferIds.add(offer.getCode());
                    }
                }
       });
    }
    return invalidOfferIds;
    }

	private List<Offer> processVaidateOfferResponse(List<CTOffer> ctOffers, CouponOffersRequest couponOffersRequest) {

		log.info("ValidateCoupon number of offers to process : [{}]", ctOffers.size());
		List<Offer> offers = new ArrayList<>();    
		Boolean isRequestAttributeValid = true;

		for (CTOffer ctOffer : ctOffers) {
			log.info("ValidateCoupon offer process : [{}]", ctOffer);
			OfferAttributes offerAttrIbutes = ctOffer.getAttributes();
			if (Objects.nonNull(ctOffer) && offerAttrIbutes !=null && Objects.nonNull(offerAttrIbutes)
					&& (offerAttrIbutes.getOfferProductTypes().contains(Constants.CAMPAIGN)
							|| offerAttrIbutes.getOfferProductTypes().contains(Constants.REWARD))) {
				Offer offer = new Offer();

				// Validating request attributes, if any failure will not set any offers in
				// response.
				isRequestAttributeValid = validateRequestAttribute(couponOffersRequest, offerAttrIbutes);
				if (offerAttrIbutes != null && isRequestAttributeValid) {

					offer.setId(ctOffer.getId());
					offer.setCode(ctOffer.getCode());
					offer.setOfferProductType(offerAttrIbutes.getOfferProductType());
					offer.setDisclosureMessagesByKey(processDisclosureMessagesByKey(offerAttrIbutes));
					offer.setDescriptionsByKey(processDescriptionsByKey(offerAttrIbutes));
					offer.setDisplayNamesBykey(processDisplayNamesBykey(offerAttrIbutes));
					offer.setOfferPreselectDesignation(offerAttrIbutes.getOfferPreselectDesignation());
					offer.setSpecialoffer(offerAttrIbutes.isSpecialOffer());
					offer.setAssociatedProducts(processAssociatedProduct(ctOffer));
					offer.setAdditionalEligibility(offerAttrIbutes.getAdditionalEligibility());

					offer.setBenefits(processBenefits(ctOffer));
					log.info("ValidateCoupon offer : [{}]", offer);
					offers.add(offer);
					log.info("ValidateCoupon number of offers processed : [{}]", offers.size());
				}
			}
		}
		return offers;
	}

    public Boolean validateRequestAttribute(CouponOffersRequest couponOffersRequest, OfferAttributes offerAttrIbutes) {

		// validate OfferActionType
		if (Optional.ofNullable(couponOffersRequest.getOfferActionType()).isPresent()
				&& !couponOffersRequest.getOfferActionType().isEmpty()
				&& offerAttrIbutes.getOfferActionType() != null) {
			if (!((couponOffersRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.UPSELL))
					&& offerAttrIbutes.getOfferActionTypes().stream().anyMatch(couponOffersRequest.getOfferActionType()::contains))) {

				log.debug("Invalid OfferActionType: ", couponOffersRequest.getOfferActionType().get(0));
				return false;
			}
		}

		// SalesChannel validation
		if (Optional.ofNullable(couponOffersRequest.getSalesChannel()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getSalesChannel()) && offerAttrIbutes.getEligibility() != null
				&& offerAttrIbutes.getEligibility().getConstraints() != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0) != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0).getSalesChannel() != null) {

			if (!offerAttrIbutes.getEligibility().getConstraints().get(0).getSalesChannel()
					.contains(couponOffersRequest.getSalesChannel())) {

				log.debug("Invalid SalesChannel: ", couponOffersRequest.getSalesChannel());
				return false;
			}
		}

		// InEligible partner details validation
		if (offerAttrIbutes.getIneligiblePartners() != null && !offerAttrIbutes.getIneligiblePartners().isEmpty()) {
			if (Optional.ofNullable(couponOffersRequest.getSalesChannel()).isPresent()
					&& Objects.nonNull(couponOffersRequest.getSalesChannel())
					&& StringUtils.equalsIgnoreCase(Constants.DIRECTV_ONLINE, couponOffersRequest.getSalesChannel())) {

				if (offerAttrIbutes.getIneligiblePartners().contains(Constants.ALL)) {
					log.debug("IneligiblePartners{}: ", offerAttrIbutes.getIneligiblePartners().toString());
					return false;

				} else if (Optional.ofNullable(couponOffersRequest.getOnlinePartnerDetails()).isPresent()
						&& Objects.nonNull(couponOffersRequest.getOnlinePartnerDetails())) {

					for (String ineligiblePartnerName : offerAttrIbutes.getIneligiblePartners()) {
						if (StringUtils.equalsIgnoreCase(ineligiblePartnerName,
								couponOffersRequest.getOnlinePartnerDetails().getPartnerName())) {
							log.debug("OnlinePartnerDetails is not eligible for the offer. IneligiblePartners{}: ",
									ineligiblePartnerName);
							return false;
						}
					}
				}
			} else {
				log.debug("Invalid sales channel {}: ", couponOffersRequest.getSalesChannel());
				return false;
			}
		}

		// Online partner details validation
		if (offerAttrIbutes.getOnlinePartnerDetails() != null && !offerAttrIbutes.getOnlinePartnerDetails().isEmpty()) {
			Boolean isOnlinePartnerDetailsValid = true;
			for (OnlinePartnerDetails ctOnlinePartnerDetails : offerAttrIbutes.getOnlinePartnerDetails()) {
				if (ctOnlinePartnerDetails.getPartnerName() != null
						&& Optional.ofNullable(couponOffersRequest.getSalesChannel()).isPresent()
						&& Objects.nonNull(couponOffersRequest.getSalesChannel())) {

					if (Optional.ofNullable(couponOffersRequest.getOnlinePartnerDetails()).isPresent()
							&& Objects.nonNull(couponOffersRequest.getOnlinePartnerDetails())
							&& StringUtils.equalsIgnoreCase(Constants.DIRECTV_ONLINE,
									couponOffersRequest.getSalesChannel())) {

						if ((StringUtils.equalsIgnoreCase(ctOnlinePartnerDetails.getPartnerName(),
								couponOffersRequest.getOnlinePartnerDetails().getPartnerName()))) {

							List<String> dealerCode1 = ctOnlinePartnerDetails.getDealerCode1();
							if (dealerCode1 != null && !dealerCode1.isEmpty()) {
								if (couponOffersRequest.getOnlinePartnerDetails().getDealerCode1() != null
										&& !dealerCode1.contains(
												couponOffersRequest.getOnlinePartnerDetails().getDealerCode1())) {
									log.debug("Invalid Dealer Code1 [{}]: ",
											couponOffersRequest.getOnlinePartnerDetails().getDealerCode1());
									isOnlinePartnerDetailsValid = false;
								}
							} else {
								isOnlinePartnerDetailsValid = true;
								break;
							}
						} else {
							log.debug("Invalid partner name {}: ",
									couponOffersRequest.getOnlinePartnerDetails().getPartnerName());
							isOnlinePartnerDetailsValid = false;
						}

					} else {
						log.debug("OnlinePartnerDetails not present in the request or Invalid sales channel {}: ",
								couponOffersRequest.getSalesChannel());
						isOnlinePartnerDetailsValid = false;
					}
				}
			}
			if (!isOnlinePartnerDetailsValid)
				return isOnlinePartnerDetailsValid;
		}

		// dealerId1 and dealerId2 validations
		if (Optional.ofNullable(couponOffersRequest.getAgentDealerDetails()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getAgentDealerDetails())) {
			if (StringUtils.equalsIgnoreCase(Constants.OPUS, couponOffersRequest.getSalesChannel())) {

				if ((offerAttrIbutes.getOpusDealerID1() != null && !offerAttrIbutes.getOpusDealerID1().isEmpty())
						|| (offerAttrIbutes.getOpusDealerID2() != null
								&& !offerAttrIbutes.getOpusDealerID2().isEmpty())) {
					if (couponOffersRequest.getAgentDealerDetails().getDealerId1() != null
							&& !couponOffersRequest.getAgentDealerDetails().getDealerId1().isEmpty()
							&& !offerAttrIbutes.getOpusDealerID1()
									.contains(couponOffersRequest.getAgentDealerDetails().getDealerId1())) {
						log.debug("Invalid Agent Dealer Code1: [{}]",
								couponOffersRequest.getAgentDealerDetails().getDealerId1());
						return false;
					} else if (couponOffersRequest.getAgentDealerDetails().getDealerId2() != null
							&& !couponOffersRequest.getAgentDealerDetails().getDealerId2().isEmpty()
							&& !offerAttrIbutes.getOpusDealerID2()
									.contains(couponOffersRequest.getAgentDealerDetails().getDealerId2())) {
						log.debug("Invalid Agent Dealer Code2: [{}]",
								couponOffersRequest.getAgentDealerDetails().getDealerId2());
						return false;
					}
				}
			} else {
				log.debug("invalid sales channel for agent dealer details {}", couponOffersRequest.getSalesChannel());
				return false;
			}
		}

		// validating channel, subChannel and storeId
		if (Optional.ofNullable(couponOffersRequest.getAgentChannelDetails()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getAgentChannelDetails())
				&& (!offerAttrIbutes.getOpusChannels().isEmpty() || !offerAttrIbutes.getOpusSubChannels().isEmpty()
						|| !offerAttrIbutes.getOpusStoreIds().isEmpty())) {
			if (couponOffersRequest.getAgentChannelDetails().getChannel() != null && !offerAttrIbutes.getOpusChannels()
					.contains(couponOffersRequest.getAgentChannelDetails().getChannel())) {
				log.debug("Invalid Opus Channel: [{}]", couponOffersRequest.getAgentChannelDetails().getChannel());
				return false;
			} else if (couponOffersRequest.getAgentChannelDetails().getSubChannel() != null && !offerAttrIbutes
					.getOpusSubChannels().contains(couponOffersRequest.getAgentChannelDetails().getSubChannel())) {
				log.debug("Invalid Opus Sub-Channel: [{}]",
						couponOffersRequest.getAgentChannelDetails().getSubChannel());
				return false;
			} else if (couponOffersRequest.getAgentChannelDetails().getStoreId() != null && !offerAttrIbutes
					.getOpusStoreIds().contains(couponOffersRequest.getAgentChannelDetails().getStoreId())) {
				log.debug("Invalid Opus Store Id: [{}]", couponOffersRequest.getAgentChannelDetails().getStoreId());
				return false;
			}
		}

		// validate offerProductFamily
		if (Optional.ofNullable(couponOffersRequest.getOfferProductFamily()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getOfferProductFamily())
				&& offerAttrIbutes.getOfferProductFamily() != null) {

			if (!((StringUtils.equalsIgnoreCase(Constants.OTT_PRODUCT_FAMILY,
					couponOffersRequest.getOfferProductFamily())
					|| StringUtils.equalsIgnoreCase(Constants.SATELLITE_PRODUCT_FAMILY,
							couponOffersRequest.getOfferProductFamily()))
					&& StringUtils.equalsIgnoreCase(offerAttrIbutes.getOfferProductFamily(),
							couponOffersRequest.getOfferProductFamily()))) {

				log.debug("Invalid offerProductFamily: ", couponOffersRequest.getOfferProductFamily());
				return false;
			}
		}

		// validate BusinessSegment
		if (Optional.ofNullable(couponOffersRequest.getBusinessSegment()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getBusinessSegment()) && offerAttrIbutes.getEligibility() != null
				&& offerAttrIbutes.getEligibility().getConstraints() != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0) != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0).getBusinessSegment() != null) {

			if (!(StringUtils.equalsIgnoreCase("CONS", couponOffersRequest.getBusinessSegment())
					&& offerAttrIbutes.getEligibility().getConstraints().get(0).getBusinessSegment()
							.contains(couponOffersRequest.getBusinessSegment()))) {

				log.debug("Invalid BusinessSegment: ", couponOffersRequest.getBusinessSegment());
				return false;
			}
		}

		// validate customerSegments
		if (Optional.ofNullable(couponOffersRequest.getCustomerSegments()).isPresent()
				&& Objects.nonNull(couponOffersRequest.getCustomerSegments())
				&& offerAttrIbutes.getEligibility() != null && offerAttrIbutes.getEligibility().getConstraints() != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0) != null
				&& offerAttrIbutes.getEligibility().getConstraints().get(0).getCustomerSegments() != null) {

			if (!((StringUtils.equalsIgnoreCase(Constants.RESIDENTIAL, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.EMPLOYEE, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.MOBILITY, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.MDUTENANT, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.BCOMP, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.COURTESY, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.DEMO, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.DECA, couponOffersRequest.getCustomerSegments())
					|| StringUtils.equalsIgnoreCase(Constants.SHOWROOM, couponOffersRequest.getCustomerSegments()))
					&& offerAttrIbutes.getEligibility().getConstraints().get(0).getCustomerSegments()
							.contains(couponOffersRequest.getCustomerSegments()))) {

				log.debug("Invalid customerSegments: ", couponOffersRequest.getCustomerSegments());
				return false;
			}
		}

		// validate ContractIndicator
		if (Optional.ofNullable(couponOffersRequest.getContractIndicator()).isPresent()
				&& !couponOffersRequest.getContractIndicator().isEmpty()
				&& offerAttrIbutes.getContractIndicator() != null) {

			if (!((couponOffersRequest.getContractIndicator().contains(Constants.CONTRACT)
					|| couponOffersRequest.getContractIndicator().contains(Constants.NONCONTRACT)
					|| couponOffersRequest.getContractIndicator().contains(Constants.EDSP_STRING)
					|| couponOffersRequest.getContractIndicator().contains(Constants.TAZ_STRING)
					|| couponOffersRequest.getContractIndicator().contains(Constants.TAZBYOD_STRING)
					|| couponOffersRequest.getContractIndicator().contains(Constants.TAZCONTRACT_STRING)
                    || couponOffersRequest.getContractIndicator().contains(Constants.GENRE)
					|| couponOffersRequest.getContractIndicator().contains(Constants.ROAD_RUNNER))
					&& couponOffersRequest.getContractIndicator().contains(offerAttrIbutes.getContractIndicator()))) {

				log.debug("Invalid ContractIndicator: ", couponOffersRequest.getContractIndicator());
				return false;
			}
		}
		return true;
	}

	private DisplayNamesByKey processDisplayNamesBykey(OfferAttributes offerAttrIbutes) {
		DisplayNamesByKey displayNamesBykey = null;
		if (offerAttrIbutes.getDisplayNamesByKey() != null) {
			GenericByKey genericByKey =offerAttrIbutes.getDisplayNamesByKey();
			displayNamesBykey = new DisplayNamesByKey();
			displayNamesBykey.setOnlineDisplayName(genericByKey.getOnline() !=null ? genericByKey.getOnline() : null);
			displayNamesBykey.setOnlineDisplayNameForServices(genericByKey.getOnlineService() !=null ? genericByKey.getOnlineService() : null);
			displayNamesBykey.setOpusDisplayName(genericByKey.getOpus() !=null ? genericByKey.getOpus() : null);
			displayNamesBykey.setOpusDisplayNameForServices(genericByKey.getServices() !=null ? genericByKey.getServices() : null);
		}
		return displayNamesBykey;
	}

	private DescriptionByKey processDescriptionsByKey(OfferAttributes offerAttrIbutes) {

		DescriptionByKey descriptionByKey = null;
		if (offerAttrIbutes.getDescriptionsByKey() != null) {
			descriptionByKey = new DescriptionByKey();
			GenericByKey genericByKey = offerAttrIbutes.getDescriptionsByKey();
			descriptionByKey.setOnlineShortDescription(genericByKey.getOnline() != null ? genericByKey.getOnline() : null);
			descriptionByKey.setOnlineLongDescription(genericByKey.getLongDescription() != null ? genericByKey.getLongDescription() : null);

			descriptionByKey.setOnlineShortDescForServices(genericByKey.getOnline() != null ? genericByKey.getOnline() : null);
			descriptionByKey.setOnlineLongDescForServices(genericByKey.getCpcDescription() != null ? genericByKey.getCpcDescription() : null);

			descriptionByKey.setOpusLongDescription(genericByKey.getLongDescription() !=null ? genericByKey.getLongDescription() : null);
			descriptionByKey.setOpusShortDescription(genericByKey.getShortDescOpus() !=null ? genericByKey.getShortDescOpus() : null);

			descriptionByKey.setOpusShortDescForServices(genericByKey.getShortDescOpus() !=null ? genericByKey.getShortDescOpus() : null);
			descriptionByKey.setOpusLongDescForServices(genericByKey.getLongSescServices() !=null ? genericByKey.getLongSescServices() : null);
		}
		return descriptionByKey;
	}

	private DisclosureMessagesByKey processDisclosureMessagesByKey(OfferAttributes offerAttrIbutes) {

		DisclosureMessagesByKey disclosureMessagesByKey = null;
		if (offerAttrIbutes.getOpusDisclosureMessage() != null || offerAttrIbutes.getMyAttDisclosureMessage() != null) {
			disclosureMessagesByKey = new DisclosureMessagesByKey();
			disclosureMessagesByKey.setOpusDisclosure(offerAttrIbutes.getOpusDisclosureMessage());
			disclosureMessagesByKey.setOpusDisclosureForServices(offerAttrIbutes.getOpusDisclosureMessage());
			disclosureMessagesByKey.setOnlineDisclosure(offerAttrIbutes.getMyAttDisclosureMessage());
			disclosureMessagesByKey.setOnlineDisclosureForServices(offerAttrIbutes.getMyAttDisclosureMessage());
		}
		return disclosureMessagesByKey;
	}

	private List<Benefit> processBenefits(CTOffer ctOffer) {

		List<Benefit> benefits = new ArrayList<>();

		if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getBenefits() != null
				&& ctOffer.getAttributes().getBenefits().size() > 0) {
			log.info("ValidateCoupon benefits to process : [{}]", ctOffer.getAttributes().getBenefits().size());
            for (com.dtv.dcp.epoch.model.ct.benefit.Benefit ctBenefit : ctOffer.getAttributes().getBenefits()) {
				Benefit benefit = new Benefit();
				benefit.setId(ctBenefit.getId());
				benefit.setName(ctBenefit.getBenefitName().getEn());
				benefit.setBenefitBillingCode(ctBenefit.getBillingBenefitCode());
				benefit.setDescription(ctBenefit.getDescription().getEn());
				benefit.setBenefitType(ctBenefit.getBenefitType());
				benefit.setDuration(Integer.toString(ctBenefit.getDuration()));
				benefit.setPeriod(ctBenefit.getPeriod());

				if (ctBenefit.getValue() != null) {
					BenefitValue benefitValue = null;
					if (ctBenefit.getValue().getDollarAmount() != null
							|| ctBenefit.getValue().getPercentage() != null) {

						benefitValue = new BenefitValue();
						benefitValue.setDollarAmount(ctBenefit.getValue().getDollarAmount());
						if (ctBenefit.getValue().getPercentage() != null) {
							benefitValue.setPercentage(Integer.toString(ctBenefit.getValue().getPercentage()));
						}
					}
					benefit.setBenefitValue(benefitValue);
				}
				List<ApplicableProduct> applicableProducts = new ArrayList<>();
				if (ctBenefit.getApplicableProducts() != null && ctBenefit.getApplicableProducts().size() > 0) {

					for (ProductWrapper productWrapper : ctBenefit.getApplicableProducts()) {

						if (productWrapper.getProducts() != null && productWrapper.getProducts().size() > 0) {

                            for (com.dtv.dcp.epoch.model.ct.product.Product product : productWrapper
									.getProducts()) {
								ApplicableProduct applicableProduct = new ApplicableProduct();
								applicableProduct.setId(product.getId());
								applicableProduct.setKey(product.getKey());
								applicableProducts.add(applicableProduct);
							}
						}
					}
				}
				if (applicableProducts.size() > 0) {
					benefit.setApplicableProducts(applicableProducts);
				}
				benefit.setRtpIndicator(ctBenefit.isRtpIndicator());

				DisclosureMessagesByKey disclosureMessagesByKey = new DisclosureMessagesByKey();
				if (ctBenefit.getDisclosureMessagesByKey() != null) {
					disclosureMessagesByKey
							.setOpusDisclosure(ctBenefit.getDisclosureMessagesByKey().getOpusDisclosureMessage());
					disclosureMessagesByKey.setOpusDisclosureForServices(
							ctBenefit.getDisclosureMessagesByKey().getOpusDisclosureForServices());
					disclosureMessagesByKey
							.setOnlineDisclosure(ctBenefit.getDisclosureMessagesByKey().getMyAttDisclosureMessage());
					disclosureMessagesByKey.setOnlineDisclosureForServices(
							ctBenefit.getDisclosureMessagesByKey().getMyATTDisclosureForServices());
				}
				benefit.setDisclosureMessagesByKey(disclosureMessagesByKey);

				DescriptionByKey descriptionsByKey = new DescriptionByKey();
				if (ctBenefit.getLongDescription() != null) {
					descriptionsByKey.setOnlineLongDescription(ctBenefit.getLongDescription());
					descriptionsByKey.setOpusLongDescription(ctBenefit.getLongDescription());
				}
				if (ctBenefit.getLongDescriptionforServices() != null) {
					descriptionsByKey.setOnlineLongDescForServices(ctBenefit.getLongDescriptionforServices());
					descriptionsByKey.setOpusShortDescForServices(ctBenefit.getLongDescriptionforServices());
				}
				if (ctBenefit.getShortDescription() != null) {
					descriptionsByKey.setOnlineShortDescription(ctBenefit.getShortDescription());
				}
				if (ctBenefit.getShortDescriptionforServices() != null) {
					descriptionsByKey.setOnlineShortDescForServices(ctBenefit.getShortDescriptionforServices());
					descriptionsByKey.setOpusShortDescForServices(ctBenefit.getShortDescriptionforServices());
				}
				if (ctBenefit.getShortDescriptionforOpus() != null) {
					descriptionsByKey.setOpusShortDescription(ctBenefit.getShortDescriptionforOpus());
				}
				benefit.setDescriptionsByKey(descriptionsByKey);

				DisplayNamesByKey displayNamesByKey = new DisplayNamesByKey();
				if (ctBenefit.getDisplayNameforServices() != null) {
					displayNamesByKey.setOnlineDisplayNameForServices(ctBenefit.getDisplayNameforServices());
					displayNamesByKey.setOpusDisplayNameForServices(ctBenefit.getDisplayNameforServices());
				}
				if (ctBenefit.getBenefitDisplayName() != null && ctBenefit.getBenefitDisplayName().getEn() != null) {
					displayNamesByKey.setOnlineDisplayName(ctBenefit.getBenefitDisplayName().getEn());
					displayNamesByKey.setOpusDisplayName(ctBenefit.getBenefitDisplayName().getEn());
				}
				benefit.setDisplayNamesByKey(displayNamesByKey);

				benefits.add(benefit);
			}
		}
		log.info("ValidateCoupon benefits processed : [{}]", benefits.size());
		return benefits;
	}

	private AssociatedProduct processAssociatedProduct(CTOffer ctOffer) {
		AssociatedProduct associatedProduct = new AssociatedProduct();
		List<Product> qualifyingProducts = new ArrayList<>();
		List<Product> bundleProducts = new ArrayList<>();

		if (ctOffer.getAttributes() != null && ctOffer.getAttributes().getAssociatedProducts() != null
				&& ctOffer.getAttributes().getAssociatedProducts().size() > 0) {
			log.info("ValidateCoupon associated products to process : [{}]",
					ctOffer.getAttributes().getAssociatedProducts().size());
            for (com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct ctAssociatedProduct : ctOffer.getAttributes()
					.getAssociatedProducts()) {

				if (ctAssociatedProduct != null) {

					if (ctAssociatedProduct.getQualifyingProducts() != null
							&& ctAssociatedProduct.getQualifyingProducts().size() > 0) {

						for (ProductWrapper ctQualifyingproduct : ctAssociatedProduct.getQualifyingProducts()) {

							if (null != ctQualifyingproduct
									&& CollectionUtils.isNotEmpty(ctQualifyingproduct.getProducts())) {

                                for (com.dtv.dcp.epoch.model.ct.product.Product ctProduct : ctQualifyingproduct
										.getProducts()) {
									Product product = new Product();
									product.setId(ctProduct.getId());
									product.setCode(ctProduct.getObj().getCode());
									product.setProductType(ctProduct.getProductType());
									if (ctProduct.getObj() != null) {

										if (ctProduct.getObj().getVariants().get(0).getAttributes() != null
												&& ctProduct.getObj().getVariants() != null
												&& ctProduct.getObj().getVariants().get(0).getAttributes() != null) {

											product.setBillingProductCode(ctProduct.getObj().getVariants().get(0)
													.getAttributes().getBillingProductCode());
											product.setNumberOfChannels(ctProduct.getObj().getVariants().get(0)
													.getAttributes().getNumberOfChannels());
											product.setDisplayType(ctProduct.getObj().getVariants().get(0)
													.getAttributes().getDisplayType());

											ctProduct.getObj().getVariants().stream().filter(Objects::nonNull)
													.forEach(variant -> {
														if (Objects.nonNull(variant)
																&& Objects.nonNull(variant.getPrices()) 
																&& !Constants.SATELLITE_PRODUCT_FAMILY
																		.equalsIgnoreCase(ctOffer.getAttributes()
																				.getOfferProductFamily())) {
															variant.getPrices().stream().filter(Objects::nonNull)
																	.forEach(price -> {
																		if (price != null && price.getEndDate() != null
																				&& OffersUtils.validateActiveDates(
																						OffersUtils.getFormattedDate(
																								price.getStartDate()),
																						OffersUtils.getFormattedDate(
																								price.getEndDate()))
																				&& Optional.ofNullable(price.getValue())
																						.isPresent()
																				&& price.getContractIndicator()
																						.equalsIgnoreCase(ctOffer
																								.getAttributes()
																								.getContractIndicator())
																				&& Optional
																						.ofNullable(price.getValue()
																								.getDollarAmount())
																						.isPresent()) {
																			if (price.getValue()
																					.getDollarAmount() != null) {

																				product.setPrice(price.getValue()
																						.getDollarAmount());
																			}
																		}
																	});
														}
													});
										}
									}
									product.setDisclosureMessagesByKey(processDisclosureMessagesByKey(ctProduct));
									product.setDescriptionsByKey(processDescriptionsByKey(ctProduct));
									product.setDisplayNamesByKey(processDisplayNamesByKey(ctProduct));
									qualifyingProducts.add(product);
								}
							}
						}
					}
					if (ctAssociatedProduct.getBundleProducts() != null
							&& ctAssociatedProduct.getBundleProducts().size() > 0) {

						for (ProductWrapper ctBundleProducts : ctAssociatedProduct.getBundleProducts()) {

							if (null != ctBundleProducts
									&& CollectionUtils.isNotEmpty(ctBundleProducts.getProducts())) {

                                for (com.dtv.dcp.epoch.model.ct.product.Product ctProduct : ctBundleProducts
										.getProducts()) {
									Product product = new Product();
									product.setId(ctProduct.getId());
									product.setCode(ctProduct.getObj().getCode());
									product.setProductType(ctProduct.getProductType());
									if (ctProduct.getObj() != null) {
										product.setRewardValue(ctProduct.getObj().getVariants().get(0)
												.getAttributes().getRewardValue());
										product.setBillingProductCode(ctProduct.getObj().getVariants().get(0)
												.getAttributes().getBillingProductCode());
										product.setNumberOfChannels(ctProduct.getObj().getVariants().get(0)
												.getAttributes().getNumberOfChannels());
										product.setDisplayType(ctProduct.getObj().getVariants().get(0).getAttributes()
												.getDisplayType());

										ctProduct.getObj().getVariants().stream().filter(Objects::nonNull)
												.forEach(variant -> {
													if (Objects.nonNull(variant)
															&& Objects.nonNull(variant.getPrices())) {
														variant.getPrices().stream().filter(Objects::nonNull)
																.forEach(price -> {
																	if (price != null && price.getEndDate() != null
																			&& OffersUtils.validateActiveDates(
																					OffersUtils.getFormattedDate(
																							price.getStartDate()),
																					OffersUtils.getFormattedDate(
																							price.getEndDate()))
																			&& Optional.ofNullable(price.getValue())
																					.isPresent()
																			&& price.getContractIndicator()
																					.equalsIgnoreCase(ctOffer
																							.getAttributes()
																							.getContractIndicator())
																			&& Optional
																					.ofNullable(price.getValue()
																							.getDollarAmount())
																					.isPresent()) {

																		if (price.getValue()
																				.getDollarAmount() != null) {
																			product.setPrice(
																					price.getValue().getDollarAmount());
																		}
																	}
																});
													}
												});
									}
									product.setDisclosureMessagesByKey(processDisclosureMessagesByKey(ctProduct));
									product.setDescriptionsByKey(processDescriptionsByKey(ctProduct));
									product.setDisplayNamesByKey(processDisplayNamesByKey(ctProduct));
									bundleProducts.add(product);
								}
							}
						}
					}
				}
			}
		}

		if (ctOffer.getAttributes() != null && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getBenefits())) {
			ctOffer.getAttributes().getBenefits().removeIf(b -> b.getBenefitType().equalsIgnoreCase(Constants.REWARD));
		}

		if (qualifyingProducts.size() > 0) {
			associatedProduct.setQualifyingProducts(qualifyingProducts);
		}
		if (bundleProducts.size() > 0) {
			associatedProduct.setBundleProducts(bundleProducts);
		}
		log.info("ValidateCoupon qualifying products processed : [{}]", qualifyingProducts.size());
		log.info("ValidateCoupon bundled products processed : [{}]", bundleProducts.size());
		return associatedProduct;
	}

    private DisplayNamesByKey processDisplayNamesByKey(com.dtv.dcp.epoch.model.ct.product.Product ctProduct) {

		DisplayNamesByKey displayNamesByKey = null;
		if (ObjectUtils.allNotNull(ctProduct.getObj(), ctProduct.getObj().getVariants(),
				ctProduct.getObj().getVariants().get(0).getAttributes(),
				ctProduct.getObj().getVariants().get(0).getAttributes().getDisplayNamesByKey())) {
			GenericByKey genericByKey = ctProduct.getObj().getVariants().get(0).getAttributes().getDisplayNamesByKey();
			displayNamesByKey = new DisplayNamesByKey();
			displayNamesByKey.setOnlineDisplayName(genericByKey.getOnline() !=null ? genericByKey.getOnline() : null);
			displayNamesByKey.setOnlineDisplayNameForServices(genericByKey.getOnlineService() !=null ? genericByKey.getOnlineService() : null);
			displayNamesByKey.setOpusDisplayName(genericByKey.getOpus() !=null ? genericByKey.getOpus() : null);
			displayNamesByKey.setOpusDisplayNameForServices(genericByKey.getServices() !=null ? genericByKey.getServices() : null);
		}
		return displayNamesByKey;
	}

    private DescriptionByKey processDescriptionsByKey(com.dtv.dcp.epoch.model.ct.product.Product ctProduct) {
		DescriptionByKey descriptionByKey = null;
		if (ObjectUtils.allNotNull(ctProduct.getObj(), ctProduct.getObj().getVariants(),
				ctProduct.getObj().getVariants().get(0).getAttributes(),
				ctProduct.getObj().getVariants().get(0).getAttributes().getDescriptionsByKey())) {
			descriptionByKey = new DescriptionByKey();
				descriptionByKey.setOnlineShortDescription(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getShortDescMyatt());
				descriptionByKey.setOnlineLongDescription(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getCpcDescription());
				descriptionByKey.setOnlineShortDescForServices(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getShortDescMyatt());
				descriptionByKey.setOnlineLongDescForServices(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getCpcDescription());
				descriptionByKey.setOpusShortDescription(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getShortDescOpus());
				descriptionByKey.setOpusLongDescription(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getLongDescription());
				descriptionByKey.setOpusShortDescForServices(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getShortDescOpus());
				descriptionByKey.setOpusLongDescForServices(ctProduct.getObj().getVariants().get(0).getAttributes()
						.getDescriptionsByKey().getLongSescServices());
		}
		return descriptionByKey;
	}

	private DisclosureMessagesByKey processDisclosureMessagesByKey(
            com.dtv.dcp.epoch.model.ct.product.Product ctProduct) {

		DisclosureMessagesByKey disclosureMessagesByKey = null;
		if (ObjectUtils.allNotNull(ctProduct.getObj(), ctProduct.getObj().getVariants(),
				ctProduct.getObj().getVariants().get(0).getAttributes(),
				ctProduct.getObj().getVariants().get(0).getAttributes().getDisclosureMessagesByKey())) {
			disclosureMessagesByKey = new DisclosureMessagesByKey();
            com.dtv.dcp.epoch.model.ct.offer.DisclosureMessagesByKey messagesByKey = ctProduct.getObj()
					.getVariants().get(0).getAttributes().getDisclosureMessagesByKey();
			disclosureMessagesByKey.setOnlineDisclosure(messagesByKey.getOnlineDisclosure());
			disclosureMessagesByKey.setOpusDisclosure(messagesByKey.getOpusDisclosure());
			disclosureMessagesByKey.setOnlineDisclosureForServices(messagesByKey.getOnlineDisclosureForServices());
			disclosureMessagesByKey.setOpusDisclosureForServices(messagesByKey.getOpusDisclosureForServices());
		}
		return disclosureMessagesByKey;
	}

	public Campaign getCTCampaignDetails(JsonNode couponDetails) {

		JsonNode campaignRequestJSON = null;
		try {
			String campaignRequestString = "{\"productCodes\":["
					.concat(couponDetails.findValue("campaignCode").toString()).concat("]}");
			campaignRequestJSON = MAPPER.readTree(campaignRequestString);
		} catch (Exception e) {
			log.error("Exception while creating request JSON in CouponCartValidationHelper.getCTCampaignDetails", e);
			return null;

		}

		/*
		 * String campaignRequestJSONEncoded = ESAPI.encoder()
		 * .encodeForHTML(campaignRequestJSON != null ? campaignRequestJSON.asText() :
		 * ""); log.info("Campaign Details Request {}", campaignRequestJSONEncoded);
		 */

		Campaign campaignDetails = new Campaign();
		try {
			JsonNode products = epochUCCPrimaryClient.getCampaignDetails(campaignRequestJSON).at("/products");
			if (products != null && !products.isEmpty() && products.get(0) != null) {
				campaignDetails = MAPPER.readValue(products.get(0).toString(), Campaign.class);
			}
		} catch (Exception e) {
			log.error("Exception while getting campaign details in CouponCartValidationHelper.getCTCampaignDetails", e);
			return null;
		}
		return campaignDetails;
	}

	public ValidationResults createValidationResults(String status, String errorCode, String errorMessage) {
		ValidationResults validationResults = new ValidationResults();
		validationResults.setStatus(status);
		validationResults.setErrorCode(errorCode);
		validationResults.setErrorMessage(errorMessage);
		return validationResults;
	}

	public boolean isDateWithinEligibilityWindow(String serviceEndDate) {

		boolean isDateWithinEligibilityWindow = false;
		try {
			Date endDate = new SimpleDateFormat(Constants.DATE_FORMAT).parse(serviceEndDate);
			Calendar currentDateMinusREW = Calendar.getInstance();
			Integer reconnectEligibilityWindowRedis = null;
			List<String> reconnectWindowValue = redisCacheHelper.getValues(
					Constants.EPOCHOFFERSMS_GLOBAL_CONFIGURATIONS_RECONNECT_ELIGIBILITY_WINDOW, Constants.OTT);
			if (reconnectWindowValue != null && !reconnectWindowValue.isEmpty()
					&& reconnectWindowValue.get(0) != null) {
				reconnectEligibilityWindowRedis = Integer.parseInt(reconnectWindowValue.get(0));

				currentDateMinusREW.add(Calendar.MONTH, -(reconnectEligibilityWindowRedis));

			} else {
				currentDateMinusREW.add(Calendar.MONTH, -(reconnectEligibilityWindow));
			}
			if (endDate.after(currentDateMinusREW.getTime())) {
				isDateWithinEligibilityWindow = true;
			}

		} catch (ParseException e) {
			e.printStackTrace();
			isDateWithinEligibilityWindow = false;
		}
		return isDateWithinEligibilityWindow;
	}

	public boolean isValidFormat(CouponOffersRequest couponOffersRequest) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT);
			date = sdf.parse(couponOffersRequest.getServiceEndDate());
			if (!couponOffersRequest.getServiceEndDate().equals(sdf.format(date))) {
				date = null;
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return date != null;
	}

	public CTOfferRequest createCTOfferRequest(List<String> offerIds, List<String> offerCodes) {
		CTOfferRequest ctOfferRequest = new CTOfferRequest();
		ctOfferRequest.setOfferIds(offerIds);
		ctOfferRequest.setOfferCodes(offerCodes);
		ctOfferRequest.setState(ctstate);
		ctOfferRequest.setExpandProductRefs(true);
		ctOfferRequest.setExpiredOffers(true);
		return ctOfferRequest;
	}

	/**
	 * @param customerAddress
	 * @return
	 */
	public CustomerEligibility getCustomerEligibilityRequest(CustomerAddress customerAddress) {
		CustomerEligibility customerEligibility = new CustomerEligibility();
		List<String> zipCode = Arrays.asList(customerAddress.getZipCode());
		List<String> fipsCode = Arrays.asList(customerAddress.getFipsCode());
		customerEligibility.setZipCode(zipCode);
		customerEligibility.setFipsCode(fipsCode);

		if (CollectionUtils.isNotEmpty(zipCode) && CollectionUtils.isNotEmpty(fipsCode)) {
			List<String> dmaValueList = dmaLookUpService.getDMAValue(zipCode.get(0), fipsCode.get(0));
			if (!dmaValueList.isEmpty()) {
				customerEligibility.setDma(dmaValueList);
			}
		}
		return customerEligibility;
	}

	public void filterOfferBasedOnZipOrDMA(CTOfferResponse ctOfferResponse, CustomerAddress customerAddress) {
		List<String> dmaValueList = dmaLookUpService.getDMAValue(customerAddress.getZipCode(), customerAddress.getFipsCode());
		List<CTOffer> removeOfferList = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
			ctOfferResponse.getOffers().forEach(ctOffer -> {
				if (Objects.nonNull(ctOffer.getAttributes().getEligibility()) && CollectionUtils.isNotEmpty(ctOffer.getAttributes().getEligibility().getConstraints())) {
					boolean shouldRemove = ctOffer.getAttributes().getEligibility().getConstraints().stream().anyMatch(constraint -> {
						boolean zipNotMatch = CollectionUtils.isNotEmpty(constraint.getZip()) && !constraint.getZip().contains(customerAddress.getZipCode());
						boolean dmaNotMatch = CollectionUtils.isNotEmpty(constraint.getDma()) && !dmaValueList.isEmpty() && dmaValueList.stream().noneMatch(constraint.getDma()::contains);
						boolean invalidDMA = CollectionUtils.isNotEmpty(constraint.getDma()) && dmaValueList.isEmpty();
						return zipNotMatch || dmaNotMatch || invalidDMA;
					});
					if (shouldRemove) {
						removeOfferList.add(ctOffer);
					}
				}
			});
		}
		// Remove the offers that should be removed
		ctOfferResponse.getOffers().removeAll(removeOfferList);
	}

    public void filterOfferOnEmptyCustomerAddress(CTOfferResponse ctOfferResponse) {
        if (CollectionUtils.isNotEmpty(ctOfferResponse.getOffers())) {
            ctOfferResponse.getOffers().removeIf(offer ->
                    Objects.nonNull(offer.getAttributes().getEligibility()) &&
                            CollectionUtils.isNotEmpty(offer.getAttributes().getEligibility().getConstraints()) &&
                            offer.getAttributes().getEligibility().getConstraints().stream().anyMatch(constraint ->
                                    CollectionUtils.isNotEmpty(constraint.getZip()) || CollectionUtils.isNotEmpty(constraint.getDma())
                            )
            );
        }
    }

	/**
	 * This method checks for stackable rules and sets actions accordingly.
	 *
	 * @param couponOffersRequest The request object containing details about the coupon offers.
	 * @param coupons The list of ValidateCoupon objects to be processed.
	 */
	public void stackableRuleCheckAndSetActions(CouponOffersRequest couponOffersRequest, List<ValidateCoupon> coupons) {
		List<ValidateCoupon> invalidCoupons = new ArrayList<>();
		List<ValidateCoupon> couponsToAdd = new ArrayList<>();

		if (CollectionUtils.isNotEmpty(coupons)) {
			coupons.forEach(coupon -> {
				if (CollectionUtils.isNotEmpty(coupon.getOffers())) {
					for (Offer offer : coupon.getOffers()) {
						if (CollectionUtils.isNotEmpty(offer.getAdditionalEligibility())) {
							for (AdditionalEligibility additionalEligibility : offer.getAdditionalEligibility()) {
								if (additionalEligibility.getEligibilityType().equalsIgnoreCase(Constants.NON_STACKABLE)) {
									if (CollectionUtils.isNotEmpty(additionalEligibility.getOfferIds()) &&
											CollectionUtils.isNotEmpty(getMatchingOfferCodes(additionalEligibility.getOfferIds(), couponOffersRequest)) &&
											CollectionUtils.isEmpty(additionalEligibility.getPriorityOffers())) {
										List<String> offerCodes = getMatchingOfferCodes(additionalEligibility.getOfferIds(), couponOffersRequest);
										offerCodes.forEach(offerCode -> {
                                            if (!coupons.stream().filter(c -> CollectionUtils.isNotEmpty(c.getOffersForCouponsRequested()) && c.getOffersForCouponsRequested().contains(offerCode)).findAny().isPresent()) {
											Offer offer1 = new Offer();
											offer1.setAction(Constants.REMOVE_RCAPS);
											offer1.setCode(offerCode);
											offer1.setValidationMessage("Offer not stackable with a given coupon in the request");
											ValidateCoupon validateCoupon = new ValidateCoupon();
											validateCoupon.setOffers(Arrays.asList(offer1));
											couponsToAdd.add(validateCoupon);
                                            }
                                            ValidateCoupon invalidCoupon = coupons.stream()
                                                    .filter(coupon1 -> CollectionUtils.isNotEmpty(coupon1.getOffers()))
                                                    .filter(coupon1 -> coupon1.getOffers().stream()
                                                            .anyMatch(offer2 -> offer2.getCode().equalsIgnoreCase(offerCode)))
                                                    .findFirst().orElse(null);
                                            if (Objects.nonNull(invalidCoupon)) {
                                                invalidCoupons.add(invalidCoupon);
                                                ValidateCoupon vc = new ValidateCoupon();
                                                vc.setCode(invalidCoupon.getCode());
                                                vc.setId(invalidCoupon.getId());
                                                vc.setName(invalidCoupon.getName());
                                                vc.setDescription(invalidCoupon.getDescription());
                                                vc.setStatus(invalidCoupon.getStatus());
                                                List<Offer> offerList = new ArrayList<>();
                                                invalidCoupon.getOffers().forEach(offer1 -> {
                                                    Offer offers = new Offer();
                                                    offers.setCode(offer1.getCode());
                                                    offers.setAction(Constants.REMOVE_RCAPS);
                                                    offers.setValidationMessage("Offer not stackable with a given coupon in the request");
                                                    offerList.add(offers);
                                                });
                                                vc.setOffers(offerList);
                                                vc.setValidationResults(createValidationResults("Error", "COUPON_INELGIBLE_ERROR",
                                                        "Coupon is not valid/stackable for the cart items provided in the request"));
                                                couponsToAdd.add(vc);
                                            }
										});
                                        offer.setAction(Constants.ADD_ACAPS);
									} else if (CollectionUtils.isNotEmpty(additionalEligibility.getOfferIds()) &&
											CollectionUtils.isNotEmpty(getMatchingOfferCodes(additionalEligibility.getOfferIds(), couponOffersRequest)) &&
											CollectionUtils.isNotEmpty(additionalEligibility.getPriorityOffers()) &&
											CollectionUtils.isNotEmpty(getMatchingOfferCodes(additionalEligibility.getPriorityOffers(), couponOffersRequest))) {
                                        if (CollectionUtils.isEmpty(invalidCoupons) || (CollectionUtils.isNotEmpty(invalidCoupons) && !invalidCoupons.stream().filter(invalidCoupon -> invalidCoupon.getId().equalsIgnoreCase(coupon.getId())).findAny().isPresent())) {
										ValidateCoupon validateCoupon = new ValidateCoupon();
										validateCoupon.setCode(coupon.getCode());
										validateCoupon.setId(coupon.getId());
										validateCoupon.setName(coupon.getName());
										validateCoupon.setDescription(coupon.getDescription());
										validateCoupon.setStatus(coupon.getStatus());
										validateCoupon.setValidationResults(createValidationResults("Error", "COUPON_INELGIBLE_ERROR",
												"Coupon is not valid/stackable for the cart items provided in the request"));
										couponsToAdd.add(validateCoupon);
                                        }
                                        invalidCoupons.add(coupon);
									} else {
										offer.setAction(Constants.ADD_ACAPS);
									}
								}else {
									offer.setAction(Constants.ADD_ACAPS);
								}
							}
						}else{
							offer.setAction(Constants.ADD_ACAPS);
						}
					}
                } else if (CollectionUtils.isNotEmpty(coupon.getOffersForCouponsRequested()) && Objects.nonNull(coupon.getValidationResults())
                        && CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartOffers())) {
                    coupon.getOffersForCouponsRequested().forEach(offerCode -> {
                        if (couponOffersRequest.getCartContext().getCartOffers().stream().anyMatch(cartOffer -> cartOffer.getOfferCode().equalsIgnoreCase(offerCode))) {
                            if (StringUtils.isNotEmpty(coupon.getValidationResults().getErrorCode())) {
                                Offer offer1 = new Offer();
                                offer1.setAction(Constants.REMOVE_RCAPS);
                                offer1.setCode(offerCode);
                                offer1.setValidationMessage("Not valid for the cart items provided in the request");
                                coupon.setOffers(Arrays.asList(offer1));
                            }
                        }
                    });
				}
			});
		}

		if (CollectionUtils.isNotEmpty(invalidCoupons)) {
			coupons.removeAll(invalidCoupons);
		}
		coupons.addAll(couponsToAdd);
        changeActionIfOfferAlreadyInCart(couponOffersRequest, coupons);
	}
	private List<String> getMatchingOfferCodes(List<String> offerCodes, CouponOffersRequest couponOffersRequest) {
		return offerCodes.stream()
				.filter(offerCode -> couponOffersRequest.getCartContext().getCartOffers().stream()
						.anyMatch(cartOffer -> cartOffer.getOfferCode().equals(offerCode)))
				.collect(Collectors.toList());
	}

    private static List<CartOffer> getMatchingCartOfferCodes(List<String> offerCodes, CouponOffersRequest couponOffersRequest) {
        return couponOffersRequest.getCartContext().getCartOffers().stream()
                .filter(cartOffer -> offerCodes.contains(cartOffer.getOfferCode()))
                .collect(Collectors.toList());
    }
    private void changeActionIfOfferAlreadyInCart(CouponOffersRequest couponOffersRequest, List<ValidateCoupon> validateCoupons) {
        validateCoupons.forEach(validateCoupon -> {
            if (CollectionUtils.isNotEmpty(validateCoupon.getOffers()) && (Objects.isNull(validateCoupon.getValidationResults())
                    || Objects.nonNull(validateCoupon.getValidationResults()) && StringUtils.isEmpty(validateCoupon.getValidationResults().getErrorCode()))) {
                validateCoupon.getOffers().forEach(offer -> {
                    if (CollectionUtils.isNotEmpty(couponOffersRequest.getCartContext().getCartOffers())) {
                        CartOffer cartOffer = couponOffersRequest.getCartContext().getCartOffers().stream().filter(cartOffer1 -> cartOffer1.getOfferCode().equalsIgnoreCase(offer.getCode())).findAny().orElse(null);
                        if (Objects.nonNull(cartOffer) && cartOffer.getAction().equalsIgnoreCase(Constants.ADD_ACAPS) && offer.getAction() != null
                                && offer.getAction().equalsIgnoreCase(Constants.ADD_ACAPS)) {
                            offer.setAction(Constants.ACTIONTYPE_NOCHANGE);
                        }
                    }
                });
            }
        });
    }
	public Map<String, String> getAllCouponType(CampaignProducts campaignProducts){
		Map<String, String> couponTypeMap = new HashMap<>();
		if(Objects.nonNull(campaignProducts) && CollectionUtils.isNotEmpty(campaignProducts.getProducts())){
			campaignProducts.getProducts().forEach(campaignProduct -> {
				if(Objects.nonNull(campaignProduct)){
					couponTypeMap.put(campaignProduct.getCode(), campaignProduct.getVariants().get(0).getAttributes().getCouponType());
				}
			});
		}
		return couponTypeMap;
	}

	public Boolean isReconnectCustomerValid(CouponOffersRequest couponOffersRequest, String couponType,
											   List<ValidateCoupon> coupons, String couponCode, Coupon coupon, Boolean isCouponValid) {

		String accountNumber = null;
		List<CustomerCoupons> customerCoupons = new ArrayList<>();
		Boolean isCouponAvailableInAccount = false;
		if (couponOffersRequest.getCustomerContext() != null
				&& couponOffersRequest.getCustomerContext().getExistingProductFamily() != null && couponOffersRequest
				.getCustomerContext().getExistingProductFamily().get(0).getAccountNumber() != null) {

			log.debug("Scenario - ReconnectCustomer");
			accountNumber = couponOffersRequest.getCustomerContext().getExistingProductFamily().get(0)
					.getAccountNumber();
			log.debug("accountNumber: ", accountNumber);
			customerCoupons = couponDetailsFromAccountService.getCustomerCoupons(accountNumber);

			for (CustomerCoupons custCoupon : customerCoupons) {
				if (couponOffersRequest.getCouponCodes().contains(custCoupon.getCouponCode())) {
					isCouponAvailableInAccount = true;
					log.debug("isCouponAvailableInAccount: ", isCouponAvailableInAccount);
				}
			}
			if (!isCouponAvailableInAccount) {
				log.debug("Account is not eligible for the coupon.");
				coupons = processErrorResponse("COUPON_ACCOUNT_INELGIBLE_ERROR", Constants.COUPON_ACCOUNT_INELIGIBLE,
						coupons, coupon, couponCode, null);
				isCouponValid = false;
			}
		}
		return isCouponValid;
	}

	public List<ValidateCoupon> processErrorResponse(String errorCode, String errorMessage,
													 List<ValidateCoupon> coupons, Coupon coupon, String couponCode, List<String> offerCodes) {

		String status = "Error";
		ValidateCoupon validateCoupon = new ValidateCoupon();
		if (coupon != null){
			validateCoupon.setId(coupon.getId());
			validateCoupon.setCode(coupon.getCode());
			validateCoupon.setName(coupon.getName().getEn());
			validateCoupon.setDescription(coupon.getDescription().getEn());
			validateCoupon.setStatus(coupon.getStatus());
			validateCoupon.setOffersForCouponsRequested(offerCodes);
		}
		else {
			validateCoupon.setCode(couponCode);
			validateCoupon.setOffersForCouponsRequested(offerCodes);
		}

		validateCoupon.setValidationResults(createValidationResults(status, errorCode, errorMessage));
		coupons.add(validateCoupon);
		return coupons;
	}
}
