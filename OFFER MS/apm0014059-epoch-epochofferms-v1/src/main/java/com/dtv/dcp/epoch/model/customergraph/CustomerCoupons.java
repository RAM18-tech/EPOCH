package com.dtv.dcp.epoch.model.customergraph;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerCoupons implements Serializable {
	
/**
	 * 
	 */
	private static final long serialVersionUID = 5559174207745772345L;
	
	private String couponCode;
	private String status;
	private String statusDescription;
	private String billingSystem;
	private String statusUpdateTime;
	private String usedDate;
	private String redemptionDate;
	private String titlePurchased;
	private String retailPrice;
	private String discountAmount;
	private String netAmount;
	private String tmsProgramId;
	private String lockDuration;
	private String couponType;

	public CustomerCoupons(String couponCode, String status, String statusDescription, String billingSystem,
			String statusUpdateTime, String usedDate, String redemptionDate, String titlePurchased, String retailPrice,
			String discountAmount, String netAmount, String tmsProgramId, String lockDuration, String couponType) {
		super();
		this.couponCode = couponCode;
		this.status = status;
		this.statusDescription = statusDescription;
		this.billingSystem = billingSystem;
		this.statusUpdateTime = statusUpdateTime;
		this.usedDate = usedDate;
		this.redemptionDate = redemptionDate;
		this.titlePurchased = titlePurchased;
		this.retailPrice = retailPrice;
		this.discountAmount = discountAmount;
		this.netAmount = netAmount;
		this.tmsProgramId = tmsProgramId;
		this.lockDuration = lockDuration;
		this.couponType = couponType;
	}
	
	
	public CustomerCoupons() {
        // Intentionally Left Blank For Jackson
    }
	public String getCouponCode() {
		return couponCode;
	}
	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusDescription() {
		return statusDescription;
	}
	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}
	public String getBillingSystem() {
		return billingSystem;
	}
	public void setBillingSystem(String billingSystem) {
		this.billingSystem = billingSystem;
	}
	public String getStatusUpdateTime() {
		return statusUpdateTime;
	}
	public void setStatusUpdateTime(String statusUpdateTime) {
		this.statusUpdateTime = statusUpdateTime;
	}
	public String getUsedDate() {
		return usedDate;
	}
	public void setUsedDate(String usedDate) {
		this.usedDate = usedDate;
	}
	public String getRedemptionDate() {
		return redemptionDate;
	}
	public void setRedemptionDate(String redemptionDate) {
		this.redemptionDate = redemptionDate;
	}
	public String getTitlePurchased() {
		return titlePurchased;
	}
	public void setTitlePurchased(String titlePurchased) {
		this.titlePurchased = titlePurchased;
	}
	public String getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(String retailPrice) {
		this.retailPrice = retailPrice;
	}
	public String getDiscountAmount() {
		return discountAmount;
	}
	public void setDiscountAmount(String discountAmount) {
		this.discountAmount = discountAmount;
	}
	public String getNetAmount() {
		return netAmount;
	}
	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}
	public String getTmsProgramId() {
		return tmsProgramId;
	}

	public void setTmsProgramId(String tmsProgramId) {
		this.tmsProgramId = tmsProgramId;
	}

	public String getLockDuration() {
		return lockDuration;
	}

	public void setLockDuration(String lockDuration) {
		this.lockDuration = lockDuration;
	}
	
	public String getCouponType() {
		return couponType;
	}

	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}
	
	@Override
	public String toString() {
		return "CustomerCoupons [couponCode=" + couponCode + ", status=" + status + ", statusDescription="
				+ statusDescription + ", billingSystem=" + billingSystem + ", statusUpdateTime=" + statusUpdateTime
				+ ", usedDate=" + usedDate + ", redemptionDate=" + redemptionDate + ", titlePurchased=" + titlePurchased
				+ ", retailPrice=" + retailPrice + ", discountAmount=" + discountAmount + ", netAmount=" + netAmount
				+ ", tmsProgramId=" + tmsProgramId + ", lockDuration=" + lockDuration + ", couponType=" + couponType
				+ "]";
	}
	
	
	
	
	

}
