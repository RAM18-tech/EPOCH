package com.dtv.dcp.epoch.service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.integration.CpopUCCClientHelper;
import com.dtv.dcp.epoch.integration.EpochUCCPrimaryClient;
import com.dtv.dcp.epoch.model.common.request.Campaign;
import com.dtv.dcp.epoch.model.common.request.CouponOffersRequest;
import com.dtv.dcp.epoch.model.common.response.ucc.ValidateCoupon;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.coupon.Coupon;
import com.dtv.dcp.epoch.model.ct.offer.CTOffer;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.response.CTOfferResponse;
import com.dtv.dcp.epoch.util.CouponCartValidationHelper;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ValidateCartCouponServiceImpl implements ValidateCartCouponService {

	private static final Logger log = LoggerFactory.getLogger(ValidateCartCouponServiceImpl.class);

	@Value("${apiclient.rest.cpopofferms.ctstate}")
	private String ctstate;

	@Autowired
	private CpopClient cpopClient;

	@Autowired
	private CpopUCCClientHelper cpopUCCClientHelper;

	@Autowired
	CouponCartValidationHelper couponCartValidationHelper;

	@Autowired
	private EpochUCCPrimaryClient epochUCCPrimaryClient;
	@Autowired
	FeatureManagerHelper featureHelper;

	private List<String> customerSegment = Arrays.asList(Constants.RESIDENTIAL, Constants.EMPLOYEE, Constants.MOBILITY, Constants.MDUTENANT, Constants.BCOMP,
			Constants.COURTESY, Constants.DEMO, Constants.DECA, Constants.SHOWROOM);

	private List<String> salesChannels = Arrays.asList(Constants.OPUS, Constants.ONLINE, Constants.INDIRECT_PARTNER,
			Constants.DIRECTV_ONLINE, Constants.OEM_IAPFIRETV, Constants.OEM_IAPROKUTV,
			Constants.DIRECTV_STREAM_ONLINE);

	@Override
	public Boolean validateRequestAttributes(CouponOffersRequest couponOffersRequest, Coupon coupon,
												String couponCode, List<ValidateCoupon> coupons) {

		String errorCode = "INVALID_REQUEST_ERROR";
		String errorMessage = null;
		Boolean isCouponValid = true;
		StringBuilder errorMessages = new StringBuilder();
		errorMessages.append("Invalid request attributes: ");

		log.debug("Validation of request - START");
		// validate OfferActionType
		if (Optional.ofNullable(couponOffersRequest.getOfferActionType()).isPresent()
				&& !couponOffersRequest.getOfferActionType().isEmpty()) {

			if (!(couponOffersRequest.getOfferActionType().contains(Constants.ACQUISITION_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.RETENTION_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.OTHER_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.UPGRADE_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.DOWNGRADE_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.CROSS_SELL_ACTION_TYPE)
					|| couponOffersRequest.getOfferActionType().contains(Constants.UPSELL)
					|| couponOffersRequest.getOfferActionType().contains(Constants.CLOSING_ACTION_TYPE))
			) {

				log.error("Invalid OfferActionType: ", couponOffersRequest.getOfferActionType());
				errorMessages.append("offerActionType, ");
				isCouponValid = false;
			}
		}

		// SalesChannel validation
		if (Optional.ofNullable(couponOffersRequest.getSalesChannel()).isPresent()
				&& !couponOffersRequest.getSalesChannel().isEmpty()
				&& !salesChannels.contains(couponOffersRequest.getSalesChannel())) {
			log.error("Invalid SalesChannel: ", couponOffersRequest.getSalesChannel());
			errorMessages.append("salesChannel, ");
			isCouponValid = false;
		}

		// validate offerProductFamily
		if (Optional.ofNullable(couponOffersRequest.getOfferProductFamily()).isPresent()
				&& !couponOffersRequest.getOfferProductFamily().isEmpty()) {

			if (!StringUtils.equalsIgnoreCase(Constants.OTT_PRODUCT_FAMILY,
					couponOffersRequest.getOfferProductFamily())
					&& !StringUtils.equalsIgnoreCase(Constants.SATELLITE_PRODUCT_FAMILY,
					couponOffersRequest.getOfferProductFamily())) {

				log.error("Invalid offerProductFamily: ", couponOffersRequest.getOfferProductFamily());
				errorMessages.append("offerProductFamily, ");
				isCouponValid = false;
			}
		}

		// validate BusinessSegment
		if (Optional.ofNullable(couponOffersRequest.getBusinessSegment()).isPresent()
				&& !couponOffersRequest.getBusinessSegment().isEmpty()) {

			if (!StringUtils.equalsIgnoreCase("CONS", couponOffersRequest.getBusinessSegment())) {

				log.error("Invalid BusinessSegment: ", couponOffersRequest.getBusinessSegment());
				errorMessages.append("businessSegment, ");
				isCouponValid = false;
			}
		}

		// validate customerSegments
		if (Optional.ofNullable(couponOffersRequest.getCustomerSegments()).isPresent()
				&& !couponOffersRequest.getCustomerSegments().isEmpty()
				&& !customerSegment.contains(couponOffersRequest.getCustomerSegments())) {
			log.error("Invalid customerSegments: ", couponOffersRequest.getCustomerSegments());
			errorMessages.append("customerSegments, ");
			isCouponValid = false;
		}

		// validate ContractIndicator
		if (Optional.ofNullable(couponOffersRequest.getContractIndicator()).isPresent()
				&& !couponOffersRequest.getContractIndicator().isEmpty()) {
			List<String> contIndicatorList = couponOffersRequest.getContractIndicator();
			if (!(contIndicatorList.contains(Constants.CONTRACT) || contIndicatorList.contains(Constants.NONCONTRACT)
					|| contIndicatorList.contains(Constants.EDSP_STRING)
					|| contIndicatorList.contains(Constants.TAZ_STRING)
					|| contIndicatorList.contains(Constants.TAZBYOD_STRING)
					|| contIndicatorList.contains(Constants.TAZCONTRACT_STRING)
					|| contIndicatorList.contains(Constants.GENRE)
					|| contIndicatorList.contains(Constants.ROAD_RUNNER))) {

				log.error("Invalid ContractIndicator: ", couponOffersRequest.getContractIndicator());
				errorMessages.append("contractIndicator, ");
				isCouponValid = false;
			}
		}

		// validate serviceEndDate based on reconnectCustomer flag in request
		if (Optional.ofNullable(couponOffersRequest.getReconnectCustomer()).isPresent()) {
			if (couponOffersRequest.getReconnectCustomer()) {
				if (couponOffersRequest.getServiceEndDate() == null
						|| !couponCartValidationHelper.isValidFormat(couponOffersRequest)) {
					log.error("Invalid ServiceEndDate: ", couponOffersRequest.getServiceEndDate());
					errorMessages.append("serviceEndDate, ");
					isCouponValid = false;
				}
			}
		}
		if (!isCouponValid) {
			errorMessages.deleteCharAt(errorMessages.length()-2);
			errorMessage = errorMessages.toString();
			couponCartValidationHelper.processErrorResponse(errorCode, errorMessage, coupons, coupon, couponCode, null);
		}
		log.debug("Validation of request - END");
		return isCouponValid;
	}

	public String getCouponType(JsonNode couponDetails) {

		Campaign campaignDetails = null;

		if (couponDetails != null && couponDetails.findValue("campaignCode") != null) {
			campaignDetails = couponCartValidationHelper.getCTCampaignDetails(couponDetails);
			log.debug("Campaign details : " + campaignDetails);
		}

		// Fetch couponType from campaignDetails
		if (campaignDetails != null && campaignDetails.getVariants() != null
				&& campaignDetails.getVariants().get(0) != null
				&& campaignDetails.getVariants().get(0).getAttributes() != null
				&& campaignDetails.getVariants().get(0).getAttributes().getCouponType() != null) {

			return campaignDetails.getVariants().get(0).getAttributes().getCouponType();
		}
		return null;
	}

	public Boolean validateCoupon(CouponOffersRequest couponOffersRequest, Coupon coupon,
									 String couponCode, List<ValidateCoupon> coupons, String couponType, Boolean isCouponValid
			,CTOfferResponse cartContextOfferResponse, CTOfferResponse offerResponse) {

		Integer maxApplications = 0;
		String benefitCode = null;
		List<Benefit> benefits = null;
		List<String> offerIds = null;
		List<String> offerCodes = new ArrayList<>();
		String errorCode = null;
		String errorMessage = null;
		Boolean isCartContextValid = false;
		List<String> customerTypes = null;
		List<String> offersForRequestedCoupons = null;


		maxApplications = coupon.getMaxApplications();
		log.info("cTCouponResponse maxApplications.....{}", maxApplications);

		// get couponType
		log.debug("Customer Type : " + couponType);

		if (offerResponse != null && CollectionUtils.isNotEmpty(offerResponse.getOffers())) {
			cpopUCCClientHelper.setInlineProducts(offerResponse);
			log.debug("offerResponse list of offers.....{}", offerResponse.getOffers().size());
			offersForRequestedCoupons = offerResponse.getOffers().stream().filter(Objects::nonNull).map(CTOffer::getCode).collect(Collectors.toList());
		}

		if (!Objects.nonNull(offerResponse)
				&& (!offerResponse.getCoupons().contains(coupon))) {
			log.error("Scenerio - coupon code invalid");
			errorCode = "COUPON_INVALID_ERROR";
			errorMessage = Constants.COUPON_NOT_EXISTS;
			coupons = couponCartValidationHelper.processErrorResponse(errorCode,
					errorMessage, coupons, coupon, couponCode, null);
			isCouponValid = false;
			return isCouponValid;
		}

		if (couponOffersRequest.getCartContext() != null
				&& couponOffersRequest.getCartContext().getCartOffers() != null
				&& !couponOffersRequest.getCartContext().getCartOffers().isEmpty()
				&& offerResponse.getOffers() != null
				&& !offerResponse.getOffers().isEmpty()
				&& cartContextOfferResponse != null
				&& cartContextOfferResponse.getOffers() != null) {

			log.debug("CartContext validations.");
			List<String> offerResponseProductCode = new ArrayList<>();
			for (CTOffer ctOffer : offerResponse.getOffers()) {
				if (ctOffer != null && ctOffer.getAttributes() != null
						&& ctOffer.getAttributes().getOfferProductTypes() != null
						&& ctOffer.getAttributes().getOfferProductTypes()
						.contains(Constants.CAMPAIGN)
						&& ctOffer.getAttributes().getAssociatedProducts() != null
						&& ctOffer.getAttributes().getAssociatedProducts().get(0) != null
						&& ctOffer.getAttributes().getAssociatedProducts().get(0)
						.getQualifyingProducts() != null) {
					List<ProductWrapper> qualifyingProducts = ctOffer.getAttributes().getAssociatedProducts().get(0).getQualifyingProducts();
					if (Objects.nonNull(qualifyingProducts) && !qualifyingProducts.isEmpty()) {
						qualifyingProducts.stream().filter(Objects::nonNull).forEach(qualifyingProduct -> {
							List<Product> products = qualifyingProduct.getProducts();
							if (Objects.nonNull(products) && !products.isEmpty()) {
								products.stream().filter(Objects::nonNull).forEach(product -> {
									if (Objects.nonNull(product.getKey())) {
										offerResponseProductCode.add(product.getKey());
									}
								});
							}
						});
					}
				}
			}

			List<String> cartOfferResponseProductCode = new ArrayList<>();
			for (CTOffer ctCartOffers : cartContextOfferResponse.getOffers()) {
				if (ctCartOffers != null && ctCartOffers.getAttributes() != null
						&& ctCartOffers.getAttributes().getAssociatedProducts() != null
						&& ctCartOffers.getAttributes().getAssociatedProducts()
						.get(0) != null
						&& ctCartOffers.getAttributes().getAssociatedProducts().get(0)
						.getBundleProducts() != null
						&& ctCartOffers.getAttributes().getAssociatedProducts().get(0)
						.getBundleProducts().get(0) != null
						&& ctCartOffers.getAttributes().getAssociatedProducts().get(0)
						.getBundleProducts().get(0).getProducts() != null) {

					for (Product obj : ctCartOffers.getAttributes().getAssociatedProducts()
							.get(0).getBundleProducts().get(0).getProducts()) {
						if (obj != null && obj.getKey() != null) {
							cartOfferResponseProductCode.add(obj.getKey());
						}
					}
				}
			}
			if (!offerResponseProductCode.isEmpty()
					&& !cartOfferResponseProductCode.isEmpty()) {
				for (String qualifyingProd : cartOfferResponseProductCode) {
					if (offerResponseProductCode.contains(qualifyingProd)) {
						isCartContextValid = true;
						break;
					}
				}
			} else if (offerResponseProductCode.isEmpty()) {
				isCartContextValid = true;
			}
			if (!isCartContextValid) {
				log.error("Cart context does not match products from CT.");
				isCouponValid = false;
			}
		}

		// coupon expire check attributes
		Instant instant = Instant.now();
		Date currentDate = Date.from(instant);
		Instant instant1 = Instant.parse(coupon.getValidFrom());
		Date startDate = Date.from(instant1);
		Instant instant2 = Instant.parse(coupon.getValidUntil());
		Date endDate = Date.from(instant2);
		log.info("Coupon expiry check: currentDate={}, startDate={}, endDate={}", currentDate,
				startDate, endDate);

		if (Optional.ofNullable(maxApplications).isPresent()
				&& Optional.ofNullable(coupon.getCustom()).isPresent()
				&& Optional.ofNullable(coupon.getCustom().getFields()).isPresent()
				&& Optional.ofNullable(coupon.getCustom().getFields().getCouponStatus())
				.isPresent()
				&& ((maxApplications == 0 || maxApplications == null)
				&& (coupon.getCustom().getFields().getCouponStatus()
				.equalsIgnoreCase(Constants.QUOTA_REACHED_COUPON)
				|| coupon.getCustom().getFields().getCouponStatus()
				.equalsIgnoreCase(Constants.REDEEMED_COUPON)))) {

			log.error("Scenerio - coupon reached quota. maxApplications: ", maxApplications);
			log.error("Scenerio - coupon reached quota. coupon status: ",
					coupon.getCustom().getFields().getCouponStatus());

			errorCode = "COUPON_REDEEMED_ERROR";
			errorMessage = Constants.COUPON_ALREADY_REDEEMED;
			coupons = couponCartValidationHelper.processErrorResponse(errorCode, errorMessage,
					coupons, coupon, couponCode, null);
			isCouponValid = false;

		} else if (startDate.after(currentDate)) {
			log.error("Scenerio - coupon InActive. coupon status: ",
					coupon.getCustom().getFields().getCouponStatus());

			errorCode = "COUPON_INACTIVE_ERROR";
			errorMessage = Constants.COUPON_INACTIVE;
			coupons = couponCartValidationHelper.processErrorResponse(errorCode, errorMessage,
					coupons, coupon, couponCode, offersForRequestedCoupons);
			isCouponValid = false;

		} else if (Optional.ofNullable(coupon.getCustom()).isPresent()
				&& Optional.ofNullable(coupon.getCustom().getFields()).isPresent()
				&& Optional.ofNullable(coupon.getCustom().getFields().getCouponStatus())
				.isPresent()
				&& (!((currentDate.after(startDate) && currentDate.before(endDate))
				|| currentDate.equals(startDate) || currentDate.equals(endDate))
				|| coupon.getCustom().getFields().getCouponStatus()
				.equalsIgnoreCase(Constants.EXPIRED_COUPON))) {

			log.error("Scenerio - coupon Expired. coupon status: ",
					coupon.getCustom().getFields().getCouponStatus());

			errorCode = "COUPON_EXPIRED_ERROR";
			errorMessage = Constants.COUPON_EXPIRED;
			coupons = couponCartValidationHelper.processErrorResponse(errorCode, errorMessage,
					coupons, coupon, couponCode, offersForRequestedCoupons);
			isCouponValid = false;

		} else {
			log.info("Scenerio - Request validation...");
			// get offer, products and coupon details and then prepare response
			if (Objects.nonNull(coupon)) {
				if (Objects.nonNull(offerResponse) && isCouponValid) {

					// Check if reconnectCustomer. If it is then get BAN/accountNumber and check
					// if
					// coupon associated with the customer or not
					if (couponOffersRequest.getReconnectCustomer() != null
							&& couponOffersRequest.getReconnectCustomer() && couponType != null
							&& (couponType.equals("multi-use1")
							|| couponType.equals("singleUse"))) {
						isCouponValid = couponCartValidationHelper.isReconnectCustomerValid(
								couponOffersRequest, couponType, coupons, couponCode,
								coupon, isCouponValid);
					}

					if (isCouponValid) {
						if (Optional.ofNullable(couponOffersRequest.getOfferActionType())
								.isPresent()
								&& !couponOffersRequest.getOfferActionType().isEmpty()
								&& (couponOffersRequest.getOfferActionType()
								.contains(Constants.ACQUISITION_ACTION_TYPE)
								|| couponOffersRequest.getOfferActionType().contains(Constants.UPSELL)
								|| couponOffersRequest.getOfferActionType().contains(Constants.CLOSING_ACTION_TYPE))) {

							// Fetch customerTypes from offerResponse.attributes
							if (offerResponse != null && offerResponse.getOffers() != null
									&& !offerResponse.getOffers().isEmpty()) {

								for (CTOffer ctOffer : offerResponse.getOffers()) {
									if (ctOffer != null && ctOffer.getAttributes() != null
											&& Optional.ofNullable(ctOffer.getAttributes().getCustomerTypes()).isPresent()
											&& ctOffer.getAttributes()
											.getOfferProductTypes() != null
											&& ctOffer.getAttributes().getOfferProductTypes()
											.contains(Constants.CAMPAIGN)
											|| ctOffer.getAttributes().getOfferProductTypes()
											.contains(Constants.REWARD)) {
										customerTypes = ctOffer.getAttributes().getCustomerTypes();
									}
								}
							}
							log.info("Customer type: {}", customerTypes.toString());
							if (couponOffersRequest.getReconnectCustomer() != null
									&& couponOffersRequest.getServiceEndDate() != null
									&& couponOffersRequest.getReconnectCustomer()
									&& !couponCartValidationHelper.isDateWithinEligibilityWindow(
									couponOffersRequest.getServiceEndDate())) {

								if (CollectionUtils.isEmpty(customerTypes) || CollectionUtils.containsAny(customerTypes.stream().map(String::toLowerCase).collect(Collectors.toList()),
										Arrays.asList(Constants.RECONNECT_RETURN.toLowerCase(), Constants.UPSELL.toLowerCase()))) {

									coupons.add(couponCartValidationHelper
											.processValidateCouponResponse(offerResponse,
													couponOffersRequest, coupon));

								} else {
									log.error("Account is not eligible for the coupon.");
									coupons = couponCartValidationHelper.processErrorResponse(
											"COUPON_ACCOUNT_INELGIBLE_ERROR",
											Constants.COUPON_ACCOUNT_INELIGIBLE, coupons,
											coupon, couponCode, null);
									isCouponValid = false;
								}
							} else if (couponOffersRequest.getReconnectCustomer() != null
									&& couponOffersRequest.getServiceEndDate() != null
									&& couponOffersRequest.getReconnectCustomer()
									&& couponCartValidationHelper.isDateWithinEligibilityWindow(
									couponOffersRequest.getServiceEndDate())) {

								if (CollectionUtils.isEmpty(customerTypes) || CollectionUtils.containsAny(customerTypes.stream().map(String::toLowerCase).collect(Collectors.toList()),
										Arrays.asList(Constants.RECONNECT.toLowerCase(), Constants.UPSELL.toLowerCase()))) {

									coupons.add(couponCartValidationHelper
											.processValidateCouponResponse(offerResponse,
													couponOffersRequest, coupon));
								} else {
									log.error("Account is not eligible for the coupon.");
									coupons = couponCartValidationHelper.processErrorResponse(
											"COUPON_ACCOUNT_INELGIBLE_ERROR",
											Constants.COUPON_ACCOUNT_INELIGIBLE, coupons,
											coupon, couponCode, null);
									isCouponValid = false;
								}
							} else if (couponOffersRequest.getReconnectCustomer() == null
									|| !couponOffersRequest.getReconnectCustomer()) {
								if (CollectionUtils.isEmpty(customerTypes) || CollectionUtils.containsAny(customerTypes.stream().map(String::toLowerCase).collect(Collectors.toList()),
										Arrays.asList(Constants.ACQUISITION.toLowerCase(), Constants.UPSELL.toLowerCase()))) {
									coupons.add(couponCartValidationHelper
											.processValidateCouponResponse(offerResponse,
													couponOffersRequest, coupon));

								} else {
									log.error(
											"Account is not eligible for the coupon. customerTypes{}",
											customerTypes.toString());
									coupons = couponCartValidationHelper.processErrorResponse(
											"COUPON_INELGIBLE_ERROR",
											"Customer is not eligible for the given Coupon",
											coupons, coupon, couponCode, null);
									isCouponValid = false;
								}
							}
						} else {
							coupons.add(
									couponCartValidationHelper.processValidateCouponResponse(
											offerResponse, couponOffersRequest, coupon));
						}
					}
				} else if (!isCartContextValid) {
					log.error(
							"Scenerio - Coupon is not valid for the cart items provided in the request.");
					errorCode = "COUPON_INELGIBLE_ERROR";
					errorMessage = Constants.COUPON_INELGIBLE_ERROR;
					coupons = couponCartValidationHelper.processErrorResponse(errorCode,
							errorMessage, coupons, coupon, couponCode, offersForRequestedCoupons);
					isCouponValid = false;
				} else {
					errorCode = "COUPON_INVALID_ERROR";
					errorMessage = Constants.COUPON_NOT_EXISTS;
					coupons = couponCartValidationHelper.processErrorResponse(errorCode,
							errorMessage, coupons, coupon, couponCode, null);
				}
			}
		}
		return null;
	}
}