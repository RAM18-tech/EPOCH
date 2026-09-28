package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingOffer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String baseOfferId;
	private String promType;
	private String promSaleType;
	private String promRank;
	private String promotionExpireDate;
	private String statusCode;
	private String statusValue;
	private String promotionReason;
	private String integratedOfferOriginator;
	private ProductSpecificationPricing productSpecificationPricing;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBaseOfferId() {
		return baseOfferId;
	}

	public void setBaseOfferId(String baseOfferId) {
		this.baseOfferId = baseOfferId;
	}

	public String getPromType() {
		return promType;
	}

	public void setPromType(String promType) {
		this.promType = promType;
	}

	public String getPromSaleType() {
		return promSaleType;
	}

	public void setPromSaleType(String promSaleType) {
		this.promSaleType = promSaleType;
	}

	public String getPromRank() {
		return promRank;
	}

	public void setPromRank(String promRank) {
		this.promRank = promRank;
	}

	public String getPromotionExpireDate() {
		return promotionExpireDate;
	}

	public void setPromotionExpireDate(String promotionExpireDate) {
		this.promotionExpireDate = promotionExpireDate;
	}

	public String getStatusValue() {
		return statusValue;
	}

	public void setStatusValue(String statusValue) {
		this.statusValue = statusValue;
	}

	public String getPromotionReason() {
		return promotionReason;
	}

	public void setPromotionReason(String promotionReason) {
		this.promotionReason = promotionReason;
	}

	public String getIntegratedOfferOriginator() {
		return integratedOfferOriginator;
	}

	public void setIntegratedOfferOriginator(String integratedOfferOriginator) {
		this.integratedOfferOriginator = integratedOfferOriginator;
	}

	public ProductSpecificationPricing getProductSpecificationPricing() {
		return productSpecificationPricing;
	}

	public void setProductSpecificationPricing(ProductSpecificationPricing productSpecificationPricing) {
		this.productSpecificationPricing = productSpecificationPricing;
	}
}