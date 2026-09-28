package com.dtv.dcp.epoch.processor.bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ResourceManager;
import com.dtv.dcp.epoch.exception.ServiceException;
import com.dtv.dcp.epoch.integration.CpopClient;
import com.dtv.dcp.epoch.message.ErrorMessages;
import com.dtv.dcp.epoch.model.common.LineItem;
import com.dtv.dcp.epoch.model.common.Location;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.model.ct.request.CTBenefit;
import com.dtv.dcp.epoch.model.ct.request.CTCartContext;
import com.dtv.dcp.epoch.model.ct.request.CTCartItem;
import com.dtv.dcp.epoch.model.ct.request.CTCartProduct;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTLosgs;
import com.dtv.dcp.epoch.model.ct.request.CTProductInfo;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.model.ct.response.CTShoppingCartResponse;
import com.dtv.dcp.epoch.model.ct.response.Messages;
import com.dtv.dcp.epoch.processor.helper.CPOPShoppingCartHelper;
import com.dtv.dcp.epoch.util.JsonService;

/**
 * The Class RewardOfferValidationProcessor.This class validate offer in CT
 * based on cart request
 *
 * @author dr000y
 */
@Component
public class BundleShoppingCartProcessor {

	@Autowired
	private CpopClient cpopClient;

	@Autowired
	private CPOPShoppingCartHelper cpopShoppingCartHelper;

	/**
	 * The log.
	 */
	private static final Logger log = LoggerFactory.getLogger(BundleShoppingCartProcessor.class);

	private static final String REWARD_QUOTA_REACHED_MESSAGE = ResourceManager
			.getMessage(ErrorMessages.CART_VALIDATION_DTVNOW_QUOTA_REACHED);

	private static final String REWARD_QUOTA_REACHED_CODE = ResourceManager
			.getIdentifier(ErrorMessages.CART_VALIDATION_DTVNOW_QUOTA_REACHED);

	private static final String REWARD_EXPIRED_MESSAGE = ResourceManager
			.getMessage(ErrorMessages.CART_VALIDATION_DTVNOW_EXPIRED_REWARD);

	private static final String REWARD_EXPIRED_CODE = ResourceManager
			.getIdentifier(ErrorMessages.CART_VALIDATION_DTVNOW_EXPIRED_REWARD);

	private static final String INELIGIBLE_REWARD_MESSAGE = ResourceManager
			.getMessage(ErrorMessages.CART_VALIDATION_DTVNOW_INELIGIBLE_REWARD);

	private static final String INELIGIBLE_REWARD_CODE = ResourceManager
			.getIdentifier(ErrorMessages.CART_VALIDATION_DTVNOW_INELIGIBLE_REWARD);

	/**
	 * Fetching the reward offers from EPOCH ms
	 *
	 * @param offerValidationRequest
	 * @return
	 */
	public OfferValidationResponse validateShoppingCart(OfferValidationRequest offerValidationRequest,
			OfferValidationResponse offerValidationResponse) {
		CTShoppingCartResponse cTShoppingCartResponse = null;
		try {
			CTShoppingCartRequest cpopRewardRequest = createCTShoppingCartRequest(offerValidationRequest);
			String promotionId = getRewardIDFromRequest(cpopRewardRequest);
			if (cpopRewardRequest.getCartContext() != null && cpopRewardRequest.getCustomerContext() != null) {
				boolean validateShoppingCartFlag = true;
				cTShoppingCartResponse = cpopClient.validateShoppingCart(cpopRewardRequest, validateShoppingCartFlag);

			} else {
				boolean validateShoppingCartFlag = false;
				cTShoppingCartResponse = cpopClient.validateShoppingCart(cpopRewardRequest, validateShoppingCartFlag);
			}
			log.info(JsonService.getJsonFromObject(cTShoppingCartResponse));

			processCTShoppingCartResponse(cTShoppingCartResponse, offerValidationResponse, promotionId,
					offerValidationRequest);

		} catch (Exception ex) {
			log.error(
					"EPOCH Response processing Exception ocuured in DTVNowCPOPBaseOffersProcessor.fetchCPOPBaseOffers() method while processing.",
					ex);
			log.info("EPOCH_BASE_OFFER_DATA_INCORRECT");
			throw new ServiceException(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR)
					.addDetail(ErrorMessages.CATALOGMS_INTERNALSERVER_ERROR_DETAILS001);
		}
		return offerValidationResponse;
	}

	/**
	 * This method construct EPOCH Reward Validation Request.
	 *
	 * @param offerValidationRequest
	 * @return
	 */
	public CTShoppingCartRequest createCTShoppingCartRequest(OfferValidationRequest offerValidationRequest) {
		log.info("createCTShoppingCartRequest method() start ...");
		CTShoppingCartRequest ctShoppingCartRequest = new CTShoppingCartRequest();
		if (offerValidationRequest.getCartContext() != null && offerValidationRequest.getCustomerContext() != null) {
			cpopShoppingCartHelper.constructCTCoolOffPeriodRequest(offerValidationRequest, ctShoppingCartRequest);
		}
		if (Optional.ofNullable(offerValidationRequest.getDtvSatelliteCart()).isPresent()) {
			cpopShoppingCartHelper.constructCTDTVSatelliteRequest(offerValidationRequest, ctShoppingCartRequest);
		}
		if (Optional.ofNullable(offerValidationRequest.getDtvnowCart()).isPresent()) {
			cpopShoppingCartHelper.constructCTDTVNowRequest(offerValidationRequest, ctShoppingCartRequest);
		}
		if (Optional.ofNullable(offerValidationRequest.getIptvCart()).isPresent()) {
			cpopShoppingCartHelper.constructCTIptvRequest(offerValidationRequest, ctShoppingCartRequest);
		}
		if (Optional.ofNullable(offerValidationRequest.getBroadbandCart()).isPresent()) {
			cpopShoppingCartHelper.constructCTBroadbandRequest(offerValidationRequest, ctShoppingCartRequest);
		}
		log.info(JsonService.getJsonFromObject(ctShoppingCartRequest));
		log.info("createCTShoppingCartRequest method() end ...");
		return ctShoppingCartRequest;
	}

	/**
	 * @param ctShoppingCartRequest
	 * @return
	 */
	private String getRewardIDFromRequest(CTShoppingCartRequest ctShoppingCartRequest) {
		String promotionId = null;
		if (ctShoppingCartRequest != null && ctShoppingCartRequest.getDtvSatelliteCart() != null
				&& ctShoppingCartRequest.getDtvSatelliteCart().getBenefits() != null) {
			for (CTBenefit benefit : ctShoppingCartRequest.getDtvSatelliteCart().getBenefits()) {
				if (Constants.REWARD.equalsIgnoreCase(benefit.getPromotionType())) {
					return benefit.getId();
				}
			}
		}
		return promotionId;
	}

	/**
	 * This method construct the offer validation response based on EPOCH
	 * response received.
	 *
	 * @param ctShoppingCartResponse
	 * @param offerValidationResponse
	 */
	private void processCTShoppingCartResponse(CTShoppingCartResponse ctShoppingCartResponse,
			OfferValidationResponse offerValidationResponse, String promotionId,
			OfferValidationRequest offerValidationRequest) {
		List<Messages> messagesList = new ArrayList<>();
		List<Messages> cpopMessages = null;
		if (Optional.ofNullable(ctShoppingCartResponse).isPresent()
				&& Optional.ofNullable(ctShoppingCartResponse.getMessages()).isPresent()
				&& !ctShoppingCartResponse.getMessages().isEmpty()) {
			cpopMessages = ctShoppingCartResponse.getMessages();
			cpopMessages.forEach(message -> {
				if (Constants.SUCCESS.equalsIgnoreCase(message.getStatus())) {
					updateStatus(messagesList, Constants.SUCCESS, Constants.ZERO_SUCCESS,
							Constants.REWARD_VALIDATION_SUCCESS_MESSAGE, Constants.PROMOTION, promotionId);
				}
				Messages msg = new Messages();
				if (message.getMessageCode() != null) {
					msg.setMessageCode(message.getMessageCode());
				}
				if (message.getMessageType() != null) {
					msg.setMessageType(message.getMessageType());
				}
				if (message.getMessageDescription() != null) {
					msg.setMessageDescription(message.getMessageDescription());
				}
				if (message.getOfferCode() != null) {
					msg.setOfferCode(message.getOfferCode());
				}
				if (message.getBillingProductCode() != null) {
					msg.setBillingProductCode(message.getBillingProductCode());
				}
				messagesList.add(msg);
			});
			if (offerValidationRequest.getCartContext() != null & offerValidationRequest.getCustomerContext() != null) {
				ctShoppingCartResponse = constructCTCoolOffPeriodResponse(offerValidationRequest,
						ctShoppingCartResponse);
			}
		} else if (Optional.ofNullable(ctShoppingCartResponse).isPresent()
				&& Optional.ofNullable(ctShoppingCartResponse.getError()).isPresent()
				&& ctShoppingCartResponse.getError().getErrorId() != null) {
			switch (ctShoppingCartResponse.getError().getErrorId()) {
			case Constants.CT_ERROR_CODE_EV8349:
				updateStatus(messagesList, Constants.WARNING, INELIGIBLE_REWARD_CODE, INELIGIBLE_REWARD_MESSAGE,
						Constants.PROMOTION, promotionId);
				break;
			case Constants.CT_ERROR_CODE_EV8350:
				updateStatus(messagesList, Constants.WARNING, REWARD_EXPIRED_CODE, REWARD_EXPIRED_MESSAGE,
						Constants.PROMOTION, promotionId);
				break;
			case Constants.CT_ERROR_CODE_EV8351:
				updateStatus(messagesList, Constants.WARNING, REWARD_QUOTA_REACHED_CODE, REWARD_QUOTA_REACHED_MESSAGE,
						Constants.PROMOTION, promotionId);
				break;
			case Constants.CT_ERROR_CODE_EV8355:
				updateStatus(messagesList, Constants.WARNING, REWARD_QUOTA_REACHED_CODE, REWARD_QUOTA_REACHED_MESSAGE,
						Constants.PROMOTION, promotionId);
				break;
			default:
			}
		}

		offerValidationResponse.setMessages(messagesList);
	}

	/**
	 * @param cpopMessages
	 * @param status
	 * @param errorCode
	 * @param errorMessage
	 * @param type
	 * @param id
	 */
	private void updateStatus(List<Messages> cpopMessages, String status, String errorCode, String errorMessage,
			String type, String id) {
		Messages message = new Messages();
		message.setStatusCode(errorCode);
		message.setStatusDescription(errorMessage);
		message.setStatus(status);
		message.setType(type);
		message.setId(id);
		cpopMessages.add(message);
	}

	public CTShoppingCartResponse constructCTCoolOffPeriodResponse(OfferValidationRequest offerValidationRequest,
			CTShoppingCartResponse shoppingCartResponse) {
		if (offerValidationRequest.getCartContext() != null) {
			// Constructing CartContext
			CTCartContext cartContext = new CTCartContext();
			cartContext.setSalesChannel(offerValidationRequest.getCartContext().getSalesChannel());
			cartContext.setValidateOnlyReward(offerValidationRequest.getCartContext().getValidateOnlyReward());
			cartContext.setValidateAvailableQuota(offerValidationRequest.getCartContext().getValidateAvailableQuota());
			Location location = new Location();
			location.setZipCode(offerValidationRequest.getCartContext().getLocation().getZipCode());
			location.setDma(offerValidationRequest.getCartContext().getLocation().getDma());
			location.setCity(offerValidationRequest.getCartContext().getLocation().getCity());
			location.setState(offerValidationRequest.getCartContext().getLocation().getState());
			location.setCounty(offerValidationRequest.getCartContext().getLocation().getCounty());
			location.setRegion(offerValidationRequest.getCartContext().getLocation().getRegion());
			cartContext.setLocation(location);
			List<CTCartItem> lobDetailsList = new ArrayList<CTCartItem>();
			(offerValidationRequest.getCartContext().getLobDetails()).forEach(lob -> {
				CTCartItem lobDetails = new CTCartItem();
				lobDetails.setBusinessSegment(lob.getBusinessSegment());
				lobDetails.setCustomerSegement(lob.getCustomerSegement());
				lobDetails.setOfferActionType(lob.getOfferActionType());
				lobDetails.setProductFamily(lob.getProductFamily());
				lobDetails.setLobType(lob.getLobType());
				List<CTLosgs> ctLosgsList = new ArrayList<CTLosgs>();
				List<CTBenefit> ctBenefits = new ArrayList<CTBenefit>();
				(lob.getLosgs()).forEach(losgs -> {
					CTLosgs ctlosgs = new CTLosgs();
					List<LineItem> ctLineItems = new ArrayList<LineItem>();
					(losgs.getLineItems()).forEach(lineItem -> {
						LineItem item = new LineItem();
						item.setOfferCodes(lineItem.getOfferCodes());
						item.setBillingProductCode(lineItem.getBillingProductCode());
						item.setProductType(lineItem.getProductType());
						(lineItem.getBenefits()).forEach(benefit -> {
							CTBenefit ctBenefit = new CTBenefit();
							ctBenefit.setBillingBenefitId(benefit.getBillingBenefitId());
							ctBenefit.setBillingBenefitCode(benefit.getBillingBenefitCode());
							ctBenefit.setPromotionType(benefit.getPromotionType());
							ctBenefit.setOfferCode(benefit.getOfferCode());
							ctBenefits.add(ctBenefit);
						});
						item.setCtBenefits(ctBenefits);
						ctLineItems.add(item);
					});
					ctlosgs.setLineItems(ctLineItems);
					ctLosgsList.add(ctlosgs);
				});
				lobDetails.setLosgs(ctLosgsList);
				lobDetailsList.add(lobDetails);
			});
			cartContext.setLobDetails(lobDetailsList);
			shoppingCartResponse.setCartContext(cartContext);
		}
		if (offerValidationRequest.getCustomerContext() != null) {
			// Constructing CustomerContext
			CTCustomerContext ctCustomerContext = new CTCustomerContext();
			ctCustomerContext.setExistingAccounts(offerValidationRequest.getCustomerContext().getExistingAccounts());
			CTCartProduct ctCartProduct = new CTCartProduct();
			ctCartProduct.setAccountNumber(offerValidationRequest.getCustomerContext().getOtt().getAccountNumber());
			ctCartProduct.setProductFamily(offerValidationRequest.getCustomerContext().getOtt().getProductFamily());
			ctCartProduct.setIsContracted(offerValidationRequest.getCustomerContext().getOtt().getIsContracted());
			ctCartProduct.setAccountStatus(offerValidationRequest.getCustomerContext().getOtt().getAccountStatus());
			List<CTProductInfo> products = new ArrayList<CTProductInfo>();
			(offerValidationRequest.getCustomerContext().getOtt().getProducts()).forEach(product -> {
				CTProductInfo ctProduct = new CTProductInfo();
				ctProduct.setBillingProductCode(product.getBillingProductCode());
				ctProduct.setProductType(product.getProductType());
				ctProduct.setProductId(product.getProductId());
				ctProduct.setStreamType(product.getStreamType());
				products.add(ctProduct);
			});
			ctCartProduct.setProducts(products);
			List<CTBenefit> benefits = new ArrayList<CTBenefit>();
			(offerValidationRequest.getCustomerContext().getOtt().getBenefits()).forEach(benefit -> {
				CTBenefit ctBenefit = new CTBenefit();
				ctBenefit.setBillingBenefitId(benefit.getBillingBenefitId());
				ctBenefit.setBillingBenefitCode(benefit.getBillingBenefitCode());
				ctBenefit.setPromotionType(benefit.getPromotionType());
				ctBenefit.setStartDate(benefit.getStartDate());
				ctBenefit.setEndDate(benefit.getEndDate());
				benefits.add(ctBenefit);
			});
			ctCartProduct.setBenefits(benefits);
			ctCustomerContext.setOtt(ctCartProduct);
			shoppingCartResponse.setCustomerContext(ctCustomerContext);
		}
		return shoppingCartResponse;
	}
}
