/**
 * 
 */
package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.dtv.dcp.epoch.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author pradiptaa.paul
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Benefit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * The billingBenefitId.
	 */
	@JsonProperty(Constants.BILLING_BENEFIT_ID)
	private String billingBenefitId;
	
	/**
	 * The billingBenefitCode.
	 */
	@JsonProperty(Constants.BILLING_BENEFIT_CODE)
	private String billingBenefitCode;
	
	/**
	 * The promotionType.
	 */
	@JsonProperty(Constants.PROMOTION_TYPE)
	private String promotionType;
	
	/**
	 * The startDate.
	 */
	@JsonProperty("offerCode")
	private String offerCode;
	
	/**
	 * The startDate.
	 */
	@JsonProperty(Constants.STARTDATE)
	private String startDate;
	
	/**
	 * The endDate.
	 */
	@JsonProperty(Constants.ENDDATE)
	private String endDate;
	
	@JsonProperty("portin")
	private boolean portin;
	/**
	 * @return the portin
	 */
	public boolean getPortin() {
		return portin;
	}

	/**
	 * @param portin the portin to set
	 */
	public void setPortin(boolean portin) {
		this.portin = portin;
	}

	public String getBillingBenefitId() {
		return billingBenefitId;
	}

	public void setBillingBenefitId(String billingBenefitId) {
		this.billingBenefitId = billingBenefitId;
	}

	public String getBillingBenefitCode() {
		return billingBenefitCode;
	}

	public void setBillingBenefitCode(String billingBenefitCode) {
		this.billingBenefitCode = billingBenefitCode;
	}

	public String getPromotionType() {
		return promotionType;
	}

	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Benefit {" + (billingBenefitId != null ? "billingBenefitId=" + billingBenefitId + ", " : "")
				+ (billingBenefitCode != null ? "billingBenefitCode=" + billingBenefitCode + ", " : "")
				+ (promotionType != null ? "promotionType=" + promotionType + ", " : "")
				+ (offerCode != null ? "offerCode=" + offerCode + ", " : "")
				+ (startDate != null ? "startDate=" + startDate + ", " : "")
				+ (endDate != null ? "endDate=" + endDate : "") + "}";
	}

}
