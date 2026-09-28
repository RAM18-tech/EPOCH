package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Content.
 * 
 * @author dr000y
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTShoppingCartRequest implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("context")
	private CTContext context;
	
	@JsonProperty("cartContext")
	private CTCartContext cartContext;
	
	@JsonProperty("customerContext")
	private CTCustomerContext customerContext;

	@JsonProperty("dtvSatelliteCart")
	private CTCartItem dtvSatelliteCart;

	@JsonProperty("dtvnowCart")
	private CTCartItem dtvnowCart;
	
	@JsonProperty("broadbandCart")
	private CTCartItem broadbandCart;
	
	@JsonProperty("iptvCart")
	private CTCartItem iptvCart;

	/**
	 * @return the context
	 */
	public CTContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(CTContext context) {
		this.context = context;
	}

	/**
	 * @return the cartContext
	 */
	public CTCartContext getCartContext() {
		return cartContext;
	}

	/**
	 * @param cartContext the cartContext to set
	 */
	public void setCartContext(CTCartContext cartContext) {
		this.cartContext = cartContext;
	}

	/**
	 * @return the customerContext
	 */
	public CTCustomerContext getCustomerContext() {
		return customerContext;
	}

	/**
	 * @param customerContext the customerContext to set
	 */
	public void setCustomerContext(CTCustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	/**
	 * @return the dtvSatelliteCart
	 */
	public CTCartItem getDtvSatelliteCart() {
		return dtvSatelliteCart;
	}

	/**
	 * @param dtvSatelliteCart
	 *            the dtvSatelliteCart to set
	 */
	public void setDtvSatelliteCart(CTCartItem dtvSatelliteCart) {
		this.dtvSatelliteCart = dtvSatelliteCart;
	}

	/**
	 * @return the dtvnowCart
	 */
	public CTCartItem getDtvnowCart() {
		return dtvnowCart;
	}

	/**
	 * @param dtvnowCart
	 *            the dtvnowCart to set
	 */
	public void setDtvnowCart(CTCartItem dtvnowCart) {
		this.dtvnowCart = dtvnowCart;
	}

	/**
	 * @return the broadbandCart
	 */
	public CTCartItem getBroadbandCart() {
		return broadbandCart;
	}

	/**
	 * @param broadbandCart the broadbandCart to set
	 */
	public void setBroadbandCart(CTCartItem broadbandCart) {
		this.broadbandCart = broadbandCart;
	}

	/**
	 * @return the iptvCart
	 */
	public CTCartItem getIptvCart() {
		return iptvCart;
	}

	/**
	 * @param iptvCart the iptvCart to set
	 */
	public void setIptvCart(CTCartItem iptvCart) {
		this.iptvCart = iptvCart;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CTShoppingCartRequest: {" + (context != null ? "context=" + context + ", " : "")
				+ (cartContext != null ? "cartContext=" + cartContext + ", " : "")
				+ (customerContext != null ? "customerContext=" + customerContext + ", " : "")
				+ (dtvSatelliteCart != null ? "dtvSatelliteCart=" + dtvSatelliteCart + ", " : "")
				+ (dtvnowCart != null ? "dtvnowCart=" + dtvnowCart + ", " : "")
				+ (broadbandCart != null ? "broadbandCart=" + broadbandCart + ", " : "")
				+ (iptvCart != null ? "iptvCart=" + iptvCart : "") + "}";
	}

	

}
