package com.dtv.dcp.epoch.model.customergraph;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Component
public class CustomerGraphPublisherMessage {
	
	private String SOR;
	
	private String SORDataUpdatedTimeStamp;
	
	private String SORNotificationCreationTimestamp;
	
	private String couponCode;
	
	private String customerId;
	
	private String couponType;
	
	private String couponStatus;
	
	private String customerBillingSystem;
	
	private String lockDuration;
	
	private String usedDate;
	
	private String redeemedDate;
	
	private String titlePurchased;
	
	private String retailPrice;
	
	private String discountAmount;
	
	private String netAmount;
	
	private String tmsProgramId;
	
	private String transactionStatus;
	

	@JsonProperty("SOR")
	public String getSOR() {
		return SOR;
	}

	public void setSOR(String SOR) {
		this.SOR = SOR;
	}
	
	@JsonProperty("SORDataUpdatedTimeStamp")
	public String getSORDataUpdatedTimeStamp() {
		return SORDataUpdatedTimeStamp;
	}

	public void setSORDataUpdatedTimeStamp(String SORDataUpdatedTimeStamp) {
		this.SORDataUpdatedTimeStamp = SORDataUpdatedTimeStamp;
	}
	
	@JsonProperty("SORNotificationCreationTimestamp")
	public String getSORNotificationCreationTimestamp() {
		return SORNotificationCreationTimestamp;
	}

	public void setSORNotificationCreationTimestamp(String sORNotificationCreationTimestamp) {
		SORNotificationCreationTimestamp = sORNotificationCreationTimestamp;
	}
	
	@JsonProperty("couponCode")
	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}
	
	@JsonProperty("customerId")
	public String getCustomerID() {
		return customerId;
	}

	public void setCustomerID(String customerID) {
		this.customerId = customerID;
	}
	
	@JsonProperty("couponType")
	public String getCouponType() {
		return couponType;
	}

	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}

	@JsonProperty("couponStatus")
	public String getCouponStatus() {
		return couponStatus;
	}

	public void setCouponStatus(String couponStatus) {
		this.couponStatus = couponStatus;
	}

	@JsonProperty("customerBillingSystem")
	public String getCustomerBillingSystem() {
		return customerBillingSystem;
	}

	public void setCustomerBillingSystem(String customerBillingSystem) {
		this.customerBillingSystem = customerBillingSystem;
	}

	@JsonProperty("lockDuration")
	public String getLockDuration() {
		return lockDuration;
	}

	public void setLockDuration(String lockDuration) {
		this.lockDuration = lockDuration;
	}

	@JsonProperty("usedDate")
	public String getUsedDate() {
		return usedDate;
	}

	public void setUsedDate(String usedDate) {
		this.usedDate = usedDate;
	}
	
	@JsonProperty("redeemedDate")
	public String getRedeemedDate() {
		return redeemedDate;
	}

	public void setRedeemedDate(String redeemedDate) {
		this.redeemedDate = redeemedDate;
	}

	@JsonProperty("titlePurchased")
	public String getTitlePurchased() {
		return titlePurchased;
	}

	public void setTitlePurchased(String titlePurchased) {
		this.titlePurchased = titlePurchased;
	}

	@JsonProperty("retailPrice")
	public String getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(String retailPrice) {
		this.retailPrice = retailPrice;
	}

	@JsonProperty("discountAmount")
	public String getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(String discountAmount) {
		this.discountAmount = discountAmount;
	}

	@JsonProperty("netAmount")
	public String getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}

	@JsonProperty("tmsProgramId")
	public String getTmsProgramID() {
		return tmsProgramId;
	}

	public void setTmsProgramID(String tmsProgramID) {
		this.tmsProgramId = tmsProgramID;
	}
	
	public String getTransactionStatus() {
		return transactionStatus;
	}

	public void setTransactionStatus(String transactionStatus) {
		this.transactionStatus = transactionStatus;
	}
}
