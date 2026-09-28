package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.exception.ServiceError;
import com.dtv.dcp.epoch.model.ct.request.CTCartContext;
import com.dtv.dcp.epoch.model.ct.request.CTCartItem;
import com.dtv.dcp.epoch.model.ct.request.CTCustomerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTShoppingCartResponse implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<Messages> messages;
	
	private CTCartContext cartContext;
	
	private CTCustomerContext customerContext;

	private CTCartItem dtvnowCart;
	
	private ServiceError error;

	/**
	 * @return the messages
	 */
	public List<Messages> getMessages() {
		return messages;
	}

	/**
	 * @param messages
	 *            the messages to set
	 */
	public void setMessages(List<Messages> messages) {
		this.messages = messages;
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
	 * @return the error
	 */
	public ServiceError getError() {
		return error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(ServiceError error) {
		this.error = error;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CpopRewardResponse [messages=");
		builder.append(messages);
		builder.append(", cartContext=");
		builder.append(cartContext);
		builder.append(", customerContext=");
		builder.append(customerContext);
		builder.append(", error=");
		builder.append(error);
		builder.append(", dtvnowCart=");
		builder.append(dtvnowCart);
		builder.append("]");
		return builder.toString();
	}

}
