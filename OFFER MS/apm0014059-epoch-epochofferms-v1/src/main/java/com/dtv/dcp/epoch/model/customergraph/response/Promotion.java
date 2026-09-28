package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 
 * @author ps1246
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Promotion implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String promotionBillingId;
	private String promotionBillingCode;
	private String promotionName;
	private String promotionDescription;
	private String promotionAmount;
	private String promotionType;
	private String promotionStartDate;
	private String promotionEndDate;
	private String promotionStatus;
	private String promotionStatusValue;
	private String promotionDuration;
	private String promotionReason;
	private int promoRank;
	private String lineOfBusiness;
	private boolean ioPromo;
	private String startDate;
	private String endDate;
	
	/**
	 * @return the lineOfBusiness
	 */
	public String getLineOfBusiness() {
		return lineOfBusiness;
	}
	/**
	 * @param lineOfBusiness the lineOfBusiness to set
	 */
	public void setLineOfBusiness(String lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}
	/**
	 * @return the promoRank
	 */
	public int getPromoRank() {
		return promoRank;
	}
	/**
	 * @param promoRank the promoRank to set
	 */
	public void setPromoRank(int promoRank) {
		this.promoRank = promoRank;
	}
	/**
	 * @return the promotionBillingID
	 */
	public String getPromotionBillingID() {
		return promotionBillingId;
	}
	/**
	 * @param promotionBillingID the promotionBillingID to set
	 */
	public void setPromotionBillingID(String promotionBillingId) {
		this.promotionBillingId = promotionBillingId;
	}
	/**
	 * @return the promotionBillingCode
	 */
	public String getPromotionBillingCode() {
		return promotionBillingCode;
	}
	/**
	 * @param promotionBillingCode the promotionBillingCode to set
	 */
	public void setPromotionBillingCode(String promotionBillingCode) {
		this.promotionBillingCode = promotionBillingCode;
	}
	/**
	 * @return the promotionName
	 */
	public String getPromotionName() {
		return promotionName;
	}
	/**
	 * @param promotionName the promotionName to set
	 */
	public void setPromotionName(String promotionName) {
		this.promotionName = promotionName;
	}
	public String getPromotionDescription() {
		return promotionDescription;
	}
	public void setPromotionDescription(String promotionDescription) {
		this.promotionDescription = promotionDescription;
	}
	/**
	 * @return the promotionAmount
	 */
	public String getPromotionAmount() {
		return promotionAmount;
	}
	/**
	 * @param promotionAmount the promotionAmount to set
	 */
	public void setPromotionAmount(String promotionAmount) {
		this.promotionAmount = promotionAmount;
	}
	/**
	 * @return the promotionType
	 */
	public String getPromotionType() {
		return promotionType;
	}
	/**
	 * @param promotionType the promotionType to set
	 */
	public void setPromotionType(String promotionType) {
		this.promotionType = promotionType;
	}
	/**
	 * @return the promotionStartDate
	 */
	public String getPromotionStartDate() {
		return promotionStartDate;
	}
	/**
	 * @param promotionStartDate the promotionStartDate to set
	 */
	public void setPromotionStartDate(String promotionStartDate) {
		this.promotionStartDate = promotionStartDate;
	}
	/**
	 * @return the promotionEndDate
	 */
	public String getPromotionEndDate() {
		return promotionEndDate;
	}
	/**
	 * @param promotionEndDate the promotionEndDate to set
	 */
	public void setPromotionEndDate(String promotionEndDate) {
		this.promotionEndDate = promotionEndDate;
	}
	/**
	 * @return the promotionStatus
	 */
	public String getPromotionStatus() {
		return promotionStatus;
	}
	/**
	 * @param promotionStatus the promotionStatus to set
	 */
	public void setPromotionStatus(String promotionStatus) {
		this.promotionStatus = promotionStatus;
	}
	/**
	 * @return the promotionStatusValue
	 */
	public String getPromotionStatusValue() {
		return promotionStatusValue;
	}
	/**
	 * @param promotionStatusValue the promotionStatusValue to set
	 */
	public void setPromotionStatusValue(String promotionStatusValue) {
		this.promotionStatusValue = promotionStatusValue;
	}
	/**
	 * @return the promotionDuration
	 */
	public String getPromotionDuration() {
		return promotionDuration;
	}
	/**
	 * @param promotionDuration the promotionDuration to set
	 */
	public void setPromotionDuration(String promotionDuration) {
		this.promotionDuration = promotionDuration;
	}
	/**
	 * @return the promotionReason
	 */
	public String getPromotionReason() {
		return promotionReason;
	}
	/**
	 * @param promotionReason the promotionReason to set
	 */
	public void setPromotionReason(String promotionReason) {
		this.promotionReason = promotionReason;
	}
	public boolean isIoPromo() {
		return ioPromo;
	}
	public void setIoPromo(boolean ioPromo) {
		this.ioPromo = ioPromo;
	}
	/**
	 * @return the promotionBillingId
	 */
	public String getPromotionBillingId() {
		return promotionBillingId;
	}
	/**
	 * @param promotionBillingId the promotionBillingId to set
	 */
	public void setPromotionBillingId(String promotionBillingId) {
		this.promotionBillingId = promotionBillingId;
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
	
}
