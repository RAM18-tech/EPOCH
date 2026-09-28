package com.dtv.dcp.epoch.processor.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.dtv.dcp.epoch.model.common.LineItem;
import com.dtv.dcp.epoch.model.common.Location;
import com.dtv.dcp.epoch.model.common.request.OfferValidationRequest;
import com.dtv.dcp.epoch.model.ct.request.CTBenefit;
import com.dtv.dcp.epoch.model.ct.request.CTCartContext;
import com.dtv.dcp.epoch.model.ct.request.CTCartItem;
import com.dtv.dcp.epoch.model.ct.request.CTCartProduct;
import com.dtv.dcp.epoch.model.ct.request.CTContext;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.dtv.dcp.epoch.model.ct.request.CTLosgs;
import com.dtv.dcp.epoch.model.ct.request.CTProductInfo;
import com.dtv.dcp.epoch.model.ct.request.CTShoppingCartRequest;
import com.dtv.dcp.epoch.util.FeatureManagerHelper;

/**
 * The Class CPOPRewardCouponOfferHelper.
 *
 * @author dr000y
 */
@Component
public class CPOPShoppingCartHelper {

	@Autowired
	private FeatureManagerHelper featureManagerHelper;

	/**
	 * @param offerValidationRequest
	 * @param ctShoppingCartRequest
	 * @return
	 */
	public CTShoppingCartRequest constructCTDTVSatelliteRequest(OfferValidationRequest offerValidationRequest,
			CTShoppingCartRequest ctShoppingCartRequest) {
		CTCartItem ctDtvSatelliteCart = new CTCartItem();
		List<CTLosgs> cpopLosgs = new ArrayList<>();

		CTContext ctx = new CTContext();
		ctx.setSalesChannel(
				offerValidationRequest.getContext() != null ? offerValidationRequest.getContext().getChannel() : null);
		ctx.setValidateOnlyReward(true);
		getGeoLocationInfo(offerValidationRequest, ctx);
		ctShoppingCartRequest.setContext(ctx);
		ctDtvSatelliteCart.setCustomerType(offerValidationRequest.getDtvSatelliteCart().getBusinessType());
		ctDtvSatelliteCart.setLobType(offerValidationRequest.getDtvSatelliteCart().getLobType());
		if (Optional.ofNullable(offerValidationRequest.getDtvSatelliteCart().getLosgs()).isPresent()) {
			offerValidationRequest.getDtvSatelliteCart().getLosgs().forEach((losgId, losgMap) -> {
				CTLosgs losg = new CTLosgs();
				losg.setId(losgId);
				losg.setLosgType(losgMap.getLosgType());
				ArrayList<LineItem> lineItemList = new ArrayList<>();
				losgMap.getLineItems().forEach((lineItemId, lineItemMap) -> {
					LineItem lineItem = new LineItem();
					lineItem.setId(lineItemId);
					lineItem.setItemType(lineItemMap.getItemType());
					lineItem.setBillingCode(lineItemMap.getBillingCode());
					lineItem.setProductType(lineItemMap.getProductType());
					lineItem.setProductId(Optional.ofNullable(lineItemMap.getProductId()).orElse(null));
					lineItem.setOfferId(lineItemMap.getOfferId());
					lineItem.setPromotionReferences(lineItemMap.getPromotionReferences());
					lineItemList.add(lineItem);
				});
				losg.setLineItems(lineItemList);
				cpopLosgs.add(losg);
			});
		}
		ctDtvSatelliteCart.setLosgs(cpopLosgs);
		if (Optional.ofNullable(offerValidationRequest.getDtvSatelliteCart().getPromotions()).isPresent()) {
			List<com.dtv.dcp.epoch.model.ct.request.CTBenefit> ctBenefits = new ArrayList<>();
			(offerValidationRequest.getDtvSatelliteCart().getPromotions()).forEach(promotion -> {
				com.dtv.dcp.epoch.model.ct.request.CTBenefit promo = new com.dtv.dcp.epoch.model.ct.request.CTBenefit();
				promo.setId(promotion.getId());
				promo.setPromotionType(promotion.getPromotionType());
				promo.setPromotionBillingCode(promotion.getPromotionBillingCode());
				promo.setOfferId(promotion.getOfferId());
				ctBenefits.add(promo);
			});
			ctDtvSatelliteCart.setBenefits(ctBenefits);
		}
		ctShoppingCartRequest.setDtvSatelliteCart(ctDtvSatelliteCart);
		return ctShoppingCartRequest;
	}

	/**
	 * @param offerValidationRequest
	 * @param ctShoppingCartRequest
	 * @return
	 */
	public CTShoppingCartRequest constructCTDTVNowRequest(OfferValidationRequest offerValidationRequest,
			CTShoppingCartRequest ctShoppingCartRequest) {
		CTCartItem ctDtvNowCart = new CTCartItem();
		List<CTLosgs> cpopLosgs = new ArrayList<>();

		CTContext ctx = new CTContext();
		ctx.setSalesChannel(
				offerValidationRequest.getContext() != null ? offerValidationRequest.getContext().getChannel() : null);
		ctx.setValidateOnlyReward(true);
		getGeoLocationInfo(offerValidationRequest, ctx);
		ctShoppingCartRequest.setContext(ctx);
		ctDtvNowCart.setCustomerType(offerValidationRequest.getDtvnowCart().getBusinessType());
		ctDtvNowCart.setLobType(offerValidationRequest.getDtvnowCart().getLobType());
		if (Optional.ofNullable(offerValidationRequest.getDtvnowCart().getLosgs()).isPresent()) {
			offerValidationRequest.getDtvnowCart().getLosgs().forEach((losgId, losgMap) -> {
				CTLosgs losg = new CTLosgs();
				losg.setId(losgId);
				losg.setLosgType(losgMap.getLosgType());
				ArrayList<LineItem> lineItemList = new ArrayList<>();
				losgMap.getLineItems().forEach((lineItemId, lineItemMap) -> {
					LineItem lineItem = new LineItem();
					lineItem.setId(lineItemId);
					lineItem.setItemType(lineItemMap.getItemType());
					lineItem.setBillingCode(lineItemMap.getBillingCode());
					lineItem.setProductType(lineItemMap.getProductType());
					lineItem.setProductId(Optional.ofNullable(lineItemMap.getProductId()).orElse(null));
					lineItem.setOfferId(lineItemMap.getOfferId());
					lineItem.setPromotionReferences(lineItemMap.getPromotionReferences());
					lineItemList.add(lineItem);
				});
				losg.setLineItems(lineItemList);
				cpopLosgs.add(losg);
			});
		}
		ctDtvNowCart.setLosgs(cpopLosgs);
		if (Optional.ofNullable(offerValidationRequest.getDtvnowCart().getPromotions()).isPresent()) {
			List<com.dtv.dcp.epoch.model.ct.request.CTBenefit> ctBenefits = new ArrayList<>();
			(offerValidationRequest.getDtvnowCart().getPromotions()).forEach(promotion -> {
				com.dtv.dcp.epoch.model.ct.request.CTBenefit promo = new com.dtv.dcp.epoch.model.ct.request.CTBenefit();
				promo.setId(promotion.getId());
				promo.setPromotionType(promotion.getPromotionType());
				promo.setPromotionBillingCode(promotion.getPromotionBillingCode());
				promo.setOfferId(promotion.getOfferId());
				ctBenefits.add(promo);
			});
			ctDtvNowCart.setBenefits(ctBenefits);
		}
		ctShoppingCartRequest.setDtvnowCart(ctDtvNowCart);
		return ctShoppingCartRequest;
	}

	/**
	 * @param offerValidationRequest
	 * @param ctShoppingCartRequest
	 * @return
	 */
	public CTShoppingCartRequest constructCTIptvRequest(OfferValidationRequest offerValidationRequest,
			CTShoppingCartRequest ctShoppingCartRequest) {
		CTCartItem ctIptvCart = new CTCartItem();
		List<CTLosgs> cpopLosgs = new ArrayList<>();

		CTContext ctx = new CTContext();
		ctx.setSalesChannel(
				offerValidationRequest.getContext() != null ? offerValidationRequest.getContext().getChannel() : null);
		ctx.setValidateOnlyReward(true);
		getGeoLocationInfo(offerValidationRequest, ctx);
		ctShoppingCartRequest.setContext(ctx);
		ctIptvCart.setCustomerType(offerValidationRequest.getIptvCart().getBusinessType());
		ctIptvCart.setLobType(offerValidationRequest.getIptvCart().getLobType());
		if (Optional.ofNullable(offerValidationRequest.getIptvCart().getLosgs()).isPresent()) {
			offerValidationRequest.getIptvCart().getLosgs().forEach((losgId, losgMap) -> {
				CTLosgs losg = new CTLosgs();
				losg.setId(losgId);
				losg.setLosgType(losgMap.getLosgType());
				ArrayList<LineItem> lineItemList = new ArrayList<>();
				losgMap.getLineItems().forEach((lineItemId, lineItemMap) -> {
					LineItem lineItem = new LineItem();
					lineItem.setId(lineItemId);
					lineItem.setItemType(lineItemMap.getItemType());
					lineItem.setBillingCode(lineItemMap.getBillingCode());
					lineItem.setProductType(lineItemMap.getProductType());
					lineItem.setProductId(Optional.ofNullable(lineItemMap.getProductId()).orElse(null));
					lineItem.setOfferId(lineItemMap.getOfferId());
					lineItem.setPromotionReferences(lineItemMap.getPromotionReferences());
					lineItemList.add(lineItem);
				});
				losg.setLineItems(lineItemList);
				cpopLosgs.add(losg);
			});
		}
		ctIptvCart.setLosgs(cpopLosgs);
		if (Optional.ofNullable(offerValidationRequest.getIptvCart().getPromotions()).isPresent()) {
			List<com.dtv.dcp.epoch.model.ct.request.CTBenefit> ctBenefits = new ArrayList<>();
			(offerValidationRequest.getIptvCart().getPromotions()).forEach(promotion -> {
				com.dtv.dcp.epoch.model.ct.request.CTBenefit promo = new com.dtv.dcp.epoch.model.ct.request.CTBenefit();
				promo.setId(promotion.getId());
				promo.setPromotionType(promotion.getPromotionType());
				promo.setPromotionBillingCode(promotion.getPromotionBillingCode());
				promo.setOfferId(promotion.getOfferId());
				ctBenefits.add(promo);
			});
			ctIptvCart.setBenefits(ctBenefits);
		}
		ctShoppingCartRequest.setIptvCart(ctIptvCart);
		return ctShoppingCartRequest;
	}

	/**
	 * @param offerValidationRequest
	 * @param ctx
	 */
	private void getGeoLocationInfo(OfferValidationRequest offerValidationRequest, CTContext ctx) {
		if (Optional.ofNullable(offerValidationRequest.getContext().getLocation()).isPresent()) {
			ctx.setZipCode(offerValidationRequest.getContext().getLocation().getZipCode());
			ctx.setDma(offerValidationRequest.getContext().getLocation().getDma());
			ctx.setCity(offerValidationRequest.getContext().getLocation().getCity());
			ctx.setState(offerValidationRequest.getContext().getLocation().getState());
			ctx.setCounty(offerValidationRequest.getContext().getLocation().getCounty());
			ctx.setRegion(offerValidationRequest.getContext().getLocation().getRegion());
		}
	}

	/**
	 * @param offerValidationRequest
	 * @param ctShoppingCartRequest
	 * @return
	 */
	public CTShoppingCartRequest constructCTBroadbandRequest(OfferValidationRequest offerValidationRequest,
			CTShoppingCartRequest ctShoppingCartRequest) {
		CTCartItem ctBroadbandCart = new CTCartItem();
		List<CTLosgs> cpopLosgs = new ArrayList<>();

		CTContext ctx = new CTContext();
		ctx.setSalesChannel(
				offerValidationRequest.getContext() != null ? offerValidationRequest.getContext().getChannel() : null);
		ctx.setValidateOnlyReward(true);
		getGeoLocationInfo(offerValidationRequest, ctx);
		ctShoppingCartRequest.setContext(ctx);
		ctBroadbandCart.setCustomerType(offerValidationRequest.getBroadbandCart().getBusinessType());
		ctBroadbandCart.setLobType(offerValidationRequest.getBroadbandCart().getLobType());
		if (Optional.ofNullable(offerValidationRequest.getBroadbandCart().getLosgs()).isPresent()) {
			offerValidationRequest.getBroadbandCart().getLosgs().forEach((losgId, losgMap) -> {
				CTLosgs losg = new CTLosgs();
				losg.setId(losgId);
				losg.setLosgType(losgMap.getLosgType());
				ArrayList<LineItem> lineItemList = new ArrayList<>();
				losgMap.getLineItems().forEach((lineItemId, lineItemMap) -> {
					LineItem lineItem = new LineItem();
					lineItem.setId(lineItemId);
					lineItem.setItemType(lineItemMap.getItemType());
					lineItem.setBillingCode(lineItemMap.getBillingCode());
					lineItem.setProductType(lineItemMap.getProductType());
					lineItem.setProductId(Optional.ofNullable(lineItemMap.getProductId()).orElse(null));
					lineItem.setOfferId(lineItemMap.getOfferId());
					lineItem.setPromotionReferences(lineItemMap.getPromotionReferences());
					lineItemList.add(lineItem);
				});
				losg.setLineItems(lineItemList);
				cpopLosgs.add(losg);
			});
		}
		ctBroadbandCart.setLosgs(cpopLosgs);
		if (Optional.ofNullable(offerValidationRequest.getBroadbandCart().getPromotions()).isPresent()) {
			List<com.dtv.dcp.epoch.model.ct.request.CTBenefit> ctBenefits = new ArrayList<>();
			(offerValidationRequest.getBroadbandCart().getPromotions()).forEach(promotion -> {
				com.dtv.dcp.epoch.model.ct.request.CTBenefit promo = new com.dtv.dcp.epoch.model.ct.request.CTBenefit();
				promo.setId(promotion.getId());
				promo.setPromotionType(promotion.getPromotionType());
				promo.setPromotionBillingCode(promotion.getPromotionBillingCode());
				promo.setOfferId(promotion.getOfferId());
				ctBenefits.add(promo);
			});
			ctBroadbandCart.setBenefits(ctBenefits);
		}
		ctShoppingCartRequest.setBroadbandCart(ctBroadbandCart);
		return ctShoppingCartRequest;
	}

	public CTShoppingCartRequest constructCTCoolOffPeriodRequest(OfferValidationRequest offerValidationRequest,
			CTShoppingCartRequest ctShoppingCartRequest) {
		// Constructing CartContext
		CTCartContext cartContext = new CTCartContext();
		if (offerValidationRequest.getCartContext() != null) {
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
			ctShoppingCartRequest.setCartContext(cartContext);
		}
		if (offerValidationRequest.getCustomerContext()!=null) {
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
			ctShoppingCartRequest.setCustomerContext(ctCustomerContext);
		}
		return ctShoppingCartRequest;
	}

}
