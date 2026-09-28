package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CTBenefit implements Serializable {

	/**
	 * The serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("billingBenefitId")
	private String billingBenefitId;
	
	@JsonProperty("billingBenefitCode")
	private String billingBenefitCode;
	
	@JsonProperty("startDate")
	private String startDate;
	
	@JsonProperty("endDate")
	private String endDate;
	
	@JsonProperty("offerCode")
	private String offerCode;

	@JsonProperty("promotionBillingCode")
	private String promotionBillingCode;

	@JsonProperty("promotionType")
	private String promotionType;

	@JsonProperty("promotionName")
	private String promotionName;

	@JsonProperty("effectiveDate")
	private String effectiveDate;
	
	@JsonProperty("offerId")
	private String offerId;

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
	 * @return the effectiveDate
	 */
	public String getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * @param effectiveDate
	 *            the effectiveDate to set
	 */
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * @return the offerId
	 */
	public String getOfferId() {
		return offerId;
	}

	/**
	 * @param offerId the offerId to set
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	/**
	 * @return the billingBenefitId
	 */
	public String getBillingBenefitId() {
		return billingBenefitId;
	}

	/**
	 * @param billingBenefitId the billingBenefitId to set
	 */
	public void setBillingBenefitId(String billingBenefitId) {
		this.billingBenefitId = billingBenefitId;
	}

	/**
	 * @return the billingBenefitCode
	 */
	public String getBillingBenefitCode() {
		return billingBenefitCode;
	}

	/**
	 * @param billingBenefitCode the billingBenefitCode to set
	 */
	public void setBillingBenefitCode(String billingBenefitCode) {
		this.billingBenefitCode = billingBenefitCode;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the offerCode
	 */
	public String getOfferCode() {
		return offerCode;
	}

	/**
	 * @param offerCode the offerCode to set
	 */
	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CTBenefit [" + (id != null ? "id=" + id + ", " : "")
				+ (billingBenefitId != null ? "billingBenefitId=" + billingBenefitId + ", " : "")
				+ (billingBenefitCode != null ? "billingBenefitCode=" + billingBenefitCode + ", " : "")
				+ (startDate != null ? "startDate=" + startDate + ", " : "")
				+ (endDate != null ? "endDate=" + endDate + ", " : "")
				+ (offerCode != null ? "offerCode=" + offerCode + ", " : "")
				+ (promotionBillingCode != null ? "promotionBillingCode=" + promotionBillingCode + ", " : "")
				+ (promotionType != null ? "promotionType=" + promotionType + ", " : "")
				+ (promotionName != null ? "promotionName=" + promotionName + ", " : "")
				+ (effectiveDate != null ? "effectiveDate=" + effectiveDate + ", " : "")
				+ (offerId != null ? "offerId=" + offerId : "") + "]";
	}

		
}
