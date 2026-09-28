package com.dtv.dcp.epoch.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Products implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer pricePlanCode;
	private String billingProductCode;
	private List<Promotion> promotions;

	public Integer getPricePlanCode() {
		return pricePlanCode;
	}

	public void setPricePlanCode(Integer pricePlanCode) {
		this.pricePlanCode = pricePlanCode;
	}

	public String getBillingProductCode() {
		return billingProductCode;
	}

	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	public List<Promotion> getPromotions() {
		return promotions;
	}

	public void setPromotions(List<Promotion> promotions) {
		this.promotions = promotions;
	}
}