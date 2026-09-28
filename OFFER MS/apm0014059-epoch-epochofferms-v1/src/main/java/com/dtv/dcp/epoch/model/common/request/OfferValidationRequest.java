package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.common.CartItems;
import com.dtv.dcp.epoch.model.common.Context;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This class model class is for shoppingCart validation request.
 *
 */

/**
 * Created by nk3077 on 05/09/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferValidationRequest implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("context")
	private Context context;
	@JsonProperty("cartContext")
	private CartContext cartContext;
	
	@JsonProperty("customerContext")
	private CustomerContext customerContext;

	/** The Constant dtvnowCart. */
	@JsonProperty("dtvnowCart")
	private CartItems dtvnowCart;

	/** The Constant wirelessCart. */
	@JsonProperty("wirelessCart")
	private CartItems wirelessCart;

	/** The Constant broadbandCart. */
	@JsonProperty("broadbandCart")
	private CartItems broadbandCart;

	/** The Constant broadbandCart. */
	@JsonProperty("dtvSatelliteCart")
	private CartItems dtvSatelliteCart;

	/** The Constant broadbandCart. */
	@JsonProperty("iptvCart")
	private CartItems iptvCart;

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @return the cartContext
	 */
	public CartContext getCartContext() {
		return cartContext;
	}

	/**
	 * @param cartContext the cartContext to set
	 */
	public void setCartContext(CartContext cartContext) {
		this.cartContext = cartContext;
	}

	/**
	 * @return the customerContext
	 */
	public CustomerContext getCustomerContext() {
		return customerContext;
	}

	/**
	 * @param customerContext the customerContext to set
	 */
	public void setCustomerContext(CustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	/**
	 * @return the dtvnowCart
	 */
	public CartItems getDtvnowCart() {
		return dtvnowCart;
	}

	/**
	 * @param dtvnowCart
	 *            the dtvnowCart to set
	 */
	public void setDtvnowCart(CartItems dtvnowCart) {
		this.dtvnowCart = dtvnowCart;
	}

	/**
	 * @return the wirelessCart
	 */
	public CartItems getWirelessCart() {
		return wirelessCart;
	}

	/**
	 * @param wirelessCart
	 *            the wirelessCart to set
	 */
	public void setWirelessCart(CartItems wirelessCart) {
		this.wirelessCart = wirelessCart;
	}

	/**
	 * @return the broadbandCart
	 */
	public CartItems getBroadbandCart() {
		return broadbandCart;
	}

	/**
	 * @param broadbandCart
	 *            the broadbandCart to set
	 */
	public void setBroadbandCart(CartItems broadbandCart) {
		this.broadbandCart = broadbandCart;
	}

	/**
	 * @return the dtvSatelliteCart
	 */
	public CartItems getDtvSatelliteCart() {
		return dtvSatelliteCart;
	}

	/**
	 * @param dtvSatelliteCart
	 *            the dtvSatelliteCart to set
	 */
	public void setDtvSatelliteCart(CartItems dtvSatelliteCart) {
		this.dtvSatelliteCart = dtvSatelliteCart;
	}

	/**
	 * @return the iptvCart
	 */
	public CartItems getIptvCart() {
		return iptvCart;
	}

	/**
	 * @param iptvCart
	 *            the iptvCart to set
	 */
	public void setIptvCart(CartItems iptvCart) {
		this.iptvCart = iptvCart;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OfferValidationRequest {" + (context != null ? "context=" + context + ", " : "")
				+ (cartContext != null ? "cartContext=" + cartContext + ", " : "")
				+ (customerContext != null ? "customerContext=" + customerContext + ", " : "")
				+ (dtvnowCart != null ? "dtvnowCart=" + dtvnowCart + ", " : "")
				+ (wirelessCart != null ? "wirelessCart=" + wirelessCart + ", " : "")
				+ (broadbandCart != null ? "broadbandCart=" + broadbandCart + ", " : "")
				+ (dtvSatelliteCart != null ? "dtvSatelliteCart=" + dtvSatelliteCart + ", " : "")
				+ (iptvCart != null ? "iptvCart=" + iptvCart : "") + "}";
	}

	

}