package com.dtv.dcp.epoch.model.common.request;

import com.dtv.dcp.epoch.model.common.CartProduct;
import com.dtv.dcp.epoch.model.common.CartPromotion;
import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartContexts {

	List<CartOffer> cartOffers;
	List<CartProduct> cartProducts;
	List<CartPromotion> cartPromotions;

	public List<CartOffer> getCartOffers() {
		return cartOffers;
	}

	public void setCartOffers(List<CartOffer> cartOffers) {
		this.cartOffers = cartOffers;
	}

	public List<CartProduct> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(List<CartProduct> cartProducts) {
		this.cartProducts = cartProducts;
	}

	public List<CartPromotion> getCartPromotions() {
		return cartPromotions;
	}

	public void setCartPromotions(List<CartPromotion> cartPromotions) {
		this.cartPromotions = cartPromotions;
	}
}