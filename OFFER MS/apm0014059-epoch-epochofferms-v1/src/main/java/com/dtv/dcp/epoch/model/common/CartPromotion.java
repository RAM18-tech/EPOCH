package com.dtv.dcp.epoch.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartPromotion implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String billingBenefitCode;
	private String billingProductCode;
	private String action;

	private  String promotionId;

	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}

	public String getBillingBenefitCode() {
		return billingBenefitCode;
	}

	public void setBillingBenefitCode(String billingBenefitCode) {
		this.billingBenefitCode = billingBenefitCode;
	}

	public String getBillingProductCode() {
		return billingProductCode;
	}

	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}