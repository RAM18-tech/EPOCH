package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Content.
 * 
 * @author nk3077
 */

public class Promotion implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("promotionCode")
	private String promotionCode;

	@JsonProperty("billingProductId")
	private String billingProductId;

	@JsonProperty("id")
	private String id;

	@JsonProperty("promotionId")
	private String promotionId;

	@JsonProperty("promotionName")
	private String promotionName;

	@JsonProperty("promotionType")
	private String promotionType;

	@JsonProperty("offerId")
	private String offerId;

	// added as part of reward cart structure
	@JsonProperty("promotionBillingCode")
	private String promotionBillingCode;

	private String action;

    private String promotionCategory;

    public String getPromotionCategory() {
        return promotionCategory;
    }

    public void setPromotionCategory(String promotionCategory) {
        this.promotionCategory = promotionCategory;
    }

    public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the promotionCode
	 */
	public String getPromotionCode() {
		return promotionCode;
	}

	/**
	 * @param promotionCode
	 *            the promotionCode to set
	 */
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	/**
	 * @return the billingProductId
	 */
	public String getBillingProductId() {
		return billingProductId;
	}

	/**
	 * @param billingProductId
	 *            the billingProductId to set
	 */
	public void setBillingProductId(String billingProductId) {
		this.billingProductId = billingProductId;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the promotionId
	 */
	public String getPromotionId() {
		return promotionId;
	}

	/**
	 * @param promotionId
	 *            the promotionId to set
	 */
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}

	/**
	 * @return the promotionName
	 */
	public String getPromotionName() {
		return promotionName;
	}

	/**
	 * @param promotionName
	 *            the promotionName to set
	 */
	public void setPromotionName(String promotionName) {
		this.promotionName = promotionName;
	}

	/**
	 * @return the promotionType
	 */
	public String getPromotionType() {
		return promotionType;
	}

	/**
	 * @param promotionType
	 *            the promotionType to set
	 */
	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}

	/**
	 * @return the offerId
	 */
	public String getOfferId() {
		return offerId;
	}

	/**
	 * @param offerId
	 *            the offerId to set
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	/**
	 * @return the promotionBillingCode
	 */
	public String getPromotionBillingCode() {
		return promotionBillingCode;
	}

	/**
	 * @param promotionBillingCode
	 *            the promotionBillingCode to set
	 */
	public void setPromotionBillingCode(String promotionBillingCode) {
		this.promotionBillingCode = promotionBillingCode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Promotion [promotionCode=");
		builder.append(promotionCode);
		builder.append(", billingProductId=");
		builder.append(billingProductId);
		builder.append(", id=");
		builder.append(id);
		builder.append(", promotionId=");
		builder.append(promotionId);
		builder.append(", promotionName=");
		builder.append(promotionName);
		builder.append(", promotionType=");
		builder.append(promotionType);
		builder.append(", offerId=");
		builder.append(offerId);
		builder.append(", promotionBillingCode=");
		builder.append(promotionBillingCode);
		builder.append("]");
		return builder.toString();
	}

}
