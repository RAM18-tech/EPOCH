package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGPromotion implements Serializable {
	
	 /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
	
	private String externalPromoType;
	private String promotionName;
	private String promotionId ;
	private String startDate ;
	private String endDate;
	private String promotionalAmount;
	private String promotionType;
	private String promotionAmount;
	private String isVoucher;
	private String promoRanking;
	private String isDefaultPromo;

	/**
	 * @return the externalPromoType
	 */
	public String getExternalPromoType() {
		return externalPromoType;
	}
	/**
	 * @param externalPromoType the externalPromoType to set
	 */
	public void setExternalPromoType(String externalPromoType) {
		this.externalPromoType = externalPromoType;
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
	/**
	 * @return the promotionId
	 */
	public String getPromotionId() {
		return promotionId;
	}
	/**
	 * @param promotionId the promotionId to set
	 */
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
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
	 * @return the promotionalAmount
	 */
	public String getPromotionalAmount() {
		return promotionalAmount;
	}
	/**
	 * @param promotionalAmount the promotionalAmount to set
	 */
	public void setPromotionalAmount(String promotionalAmount) {
		this.promotionalAmount = promotionalAmount;
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
	 * @return the isVoucher
	 */
	public String getIsVoucher() {
		return isVoucher;
	}
	/**
	 * @param isVoucher the isVoucher to set
	 */
	public void setIsVoucher(String isVoucher) {
		this.isVoucher = isVoucher;
	}
	/**
	 * @return the promoRanking
	 */
	public String getPromoRanking() {
		return promoRanking;
	}
	/**
	 * @param promoRanking the promoRanking to set
	 */
	public void setPromoRanking(String promoRanking) {
		this.promoRanking = promoRanking;
	}
	/**
	 * @return the isDefaultPromo
	 */
	public String getIsDefaultPromo() {
		return isDefaultPromo;
	}
	/**
	 * @param isDefaultPromo the isDefaultPromo to set
	 */
	public void setIsDefaultPromo(String isDefaultPromo) {
		this.isDefaultPromo = isDefaultPromo;
	}

}
