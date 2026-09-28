package com.dtv.dcp.epoch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dtv.dcp.epoch.common.Constants;
import com.dtv.dcp.epoch.exception.ClientException;
import com.dtv.dcp.epoch.model.common.CartItems;
import com.dtv.dcp.epoch.model.common.Promotion;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.common.response.OfferValidationResponse;
import com.dtv.dcp.epoch.processor.bundle.BundleOfferProcessor;
import com.dtv.dcp.epoch.processor.helper.CPOPAdditionalOfferHelper;



/**
 * The Class OfferValidationServiceImpl.
 * 
 * @author nk3077
 */
@Service
public class OfferValidationServiceImpl implements OfferValidationService{
	
	/**  The cart OfferValidationProcessor. */
	@Autowired
	private BundleOfferProcessor BundleOfferProcessor;
	
	@Autowired
	private com.dtv.dcp.epoch.processor.bundle.BundleShoppingCartProcessor BundleShoppingCartProcessor;
	
	@Autowired
	CPOPAdditionalOfferHelper dtvNowCPOPAdditionalOfferHelper;
	
	/** Feature Toggle
	 * @Autowired
	 * private FeatureManagerHelper featureManagerHelper; 
	 */
	
	/**
	 * @see com.att.idp.catalog.service.OfferValidationService#offerValidation(com.att.idp.catalog.model.cart.common.OfferValidationRequest, com.att.idp.catalog.model.common.EligibilitiesInputBean, java.lang.String)
	 */
	@Override
	public OfferValidationResponse offerValidation(OfferValidationRequest offerValidationRequest, String sessionId)
			throws ClientException {
		OfferValidationResponse offerValidationResponse = new OfferValidationResponse();
		if (isRewardOfferExist(offerValidationRequest)) {
			return BundleOfferProcessor.offerValidation(offerValidationRequest);
		}
		if(isCoolOffPeriodEnabled(offerValidationRequest)){
			return BundleShoppingCartProcessor.validateShoppingCart(offerValidationRequest, offerValidationResponse);
		}
		return offerValidationResponse;
	}
				
	/**
	 * This method check if request object has Reward offer or not.
	 * 
	 * @param offerValidationRequest
	 * @return
	 */
	private boolean isRewardOfferExist(OfferValidationRequest offerValidationRequest) {
		boolean isRewardExist = false;
		boolean isRewardCapableEnable = false;
		CartItems dtvNowCart = null;
		CartItems dtvSatelliteCart = null;
		CartItems broadbandCart = null;
		CartItems iptvCart = null;
		List<Promotion> promoList = null;
		String channel = null;
		if (Optional.ofNullable(offerValidationRequest.getContext()).isPresent()
				&& Optional.ofNullable(offerValidationRequest.getContext().getChannel()).isPresent()) {
			channel = offerValidationRequest.getContext().getChannel();
		}
		isRewardCapableEnable = dtvNowCPOPAdditionalOfferHelper.isRewardCapabilityEnabled(channel);
		if (isRewardCapableEnable && ((Optional.ofNullable(offerValidationRequest.getDtvnowCart()).isPresent())
				|| (Optional.ofNullable(offerValidationRequest.getDtvSatelliteCart()).isPresent())
				|| (Optional.ofNullable(offerValidationRequest.getBroadbandCart()).isPresent())
				|| (Optional.ofNullable(offerValidationRequest.getIptvCart()).isPresent()))) {

			dtvNowCart = offerValidationRequest.getDtvnowCart();
			dtvSatelliteCart = offerValidationRequest.getDtvSatelliteCart();
			broadbandCart = offerValidationRequest.getBroadbandCart();
			iptvCart = offerValidationRequest.getIptvCart();
			promoList = new ArrayList<>();
			if (Optional.ofNullable(dtvNowCart).isPresent()) {
				promoList.addAll(dtvNowCart.getPromotions());
			}
			if (Optional.ofNullable(dtvSatelliteCart).isPresent()) {
				promoList.addAll(dtvSatelliteCart.getPromotions());
			}
			if (Optional.ofNullable(broadbandCart).isPresent()) {
				promoList.addAll(broadbandCart.getPromotions());
			}
			if (Optional.ofNullable(iptvCart).isPresent()) {
				promoList.addAll(iptvCart.getPromotions());
			}
			for (Promotion promo : promoList) {
				String promotionID = promo.getId();
				String promotionType = promo.getPromotionType();
				if (promotionID != null && Constants.REWARD.equalsIgnoreCase(promotionType)) {
					isRewardExist = true;
				}
			}
		}

		return isRewardExist;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isCoolOffPeriodEnabled(OfferValidationRequest offerValidationRequest) {	
		boolean flag = false;
		if(offerValidationRequest.getCartContext()!= null && offerValidationRequest.getCustomerContext()!=null){
			flag = true;
		}
		return flag;
	}
}