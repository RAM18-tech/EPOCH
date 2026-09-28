package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Product implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String code;
	private String productType;
	private String billingProductCode;
	private String numberOfChannels;
	private String displayType;
	private DisclosureMessagesByKey disclosureMessagesByKey;
	private DescriptionByKey descriptionsByKey;
	private DisplayNamesByKey displayNamesByKey;
	private Double price;
	private String rewardValue;

	public String getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(String rewardValue) {
		this.rewardValue = rewardValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getBillingProductCode() {
		return billingProductCode;
	}

	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	public String getNumberOfChannels() {
		return numberOfChannels;
	}

	public void setNumberOfChannels(String numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	public String getDisplayType() {
		return displayType;
	}

	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	public DisclosureMessagesByKey getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public DescriptionByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}

	public void setDescriptionsByKey(DescriptionByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}

	public DisplayNamesByKey getDisplayNamesByKey() {
		return displayNamesByKey;
	}

	public void setDisplayNamesByKey(DisplayNamesByKey displayNamesByKey) {
		this.displayNamesByKey = displayNamesByKey;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Product [id=" + id + ", code=" + code + ", productType=" + productType + ", billingProductCode="
				+ billingProductCode + ", rewardValue=" + rewardValue + ", numberOfChannels=" + numberOfChannels
				+ ", displayType=" + displayType + ", disclosureMessagesByKey=" + disclosureMessagesByKey
				+ ", descriptionsByKey=" + descriptionsByKey + ", displayNamesByKey=" + displayNamesByKey + ", price="
				+ price + "]";
	}

}