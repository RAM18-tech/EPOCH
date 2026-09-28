package com.dtv.dcp.epoch.model.common.response.ucc;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.response.CartItems;
import com.dtv.dcp.epoch.model.message.MessageDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Content implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ValidateCoupon> coupons;

	private  List<CartItems> cartItems;

	private ValidationResults validationResults;
	private List<MessageDetail> messages;

	public List<MessageDetail> getMessages() {
		return messages;
	}

	public void setMessages(List<MessageDetail> messages) {
		this.messages = messages;
	}

	public ValidationResults getValidationResults() {
		return validationResults;
	}

	public void setValidationResults(ValidationResults validationResults) {
		this.validationResults = validationResults;
	}

	public List<CartItems> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItems> cartItems) {
		this.cartItems = cartItems;
	}

	public List<ValidateCoupon> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<ValidateCoupon> coupons) {
		this.coupons = coupons;
	}

	@Override
	public String toString() {
		return "Content [coupons=" + coupons + "]";
	}
}