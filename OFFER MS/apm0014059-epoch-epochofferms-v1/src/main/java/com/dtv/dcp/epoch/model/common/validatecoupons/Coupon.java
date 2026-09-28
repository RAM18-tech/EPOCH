package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.coupon.PurchaseDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Coupon implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private String id;
	
	/** The code. */
	private String code;
	
	/** The status. */
	private String status;
	
    /** The startDate. */
	private String startDate;
	
	/** The endDate. */
	private String endDate;
	
	/** The createdAt. */
	private String createdAt;
	
	/** The createdBy. */
	private String createdBy;
	
	/** The lastModifiedAt. */
	private String lastModifiedBy;
	
	/** The lastModifiedBy. */
	private String lastModifiedAt;
	
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The campaignCode. */
	private String campaignCode;
	
	/** The maxApplications. */
	private Integer maxApplications;
	
	/** The maxApplicationsPerCustomer. */
	private Integer maxApplicationsPerCustomer;
	
	/** The isActive. */
	private Boolean isActive;
	
	/** The couponStatus. */
	private String couponStatus;
	
	/** The cart discount. */
	private List<String> CartDiscount;
	
	/** The purchaseDetails. */
	private List<PurchaseDetails> purchaseDetails;
	
	/** The purchaseDetailsObj. */
	private PurchaseDetails purchaseDetailsObj;
	
	/** The statusDescription. */
	private String statusDescription;
	
	/** The billingSystem. */
    private String billingSystem;
    
    /** The statusUpdateTime. */
    private String statusUpdateTime;
    
    /** The usedDate. */
    private String usedDate;
    
    /** The redemptionDate. */
    private String redemptionDate;
    
    /** The titlePurchased. */
    private String titlePurchased;
    
    /** The retailPrice. */
    private String retailPrice;
    
    /** The discountAmount. */
    private String discountAmount;
    
    /** The netAmount. */
    private String netAmount;
	
	public List<PurchaseDetails> getPurchaseDetails() {
		return purchaseDetails;
	}
	public void setPurchaseDetails(List<PurchaseDetails> purchaseDetails) {
		this.purchaseDetails = purchaseDetails;
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
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}
	public String getLastModifiedAt() {
		return lastModifiedAt;
	}
	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCampaignCode() {
		return campaignCode;
	}
	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}
	public Integer getMaxApplications() {
		return maxApplications;
	}
	public void setMaxApplications(Integer maxApplications) {
		this.maxApplications = maxApplications;
	}
	public Integer getMaxApplicationsPerCustomer() {
		return maxApplicationsPerCustomer;
	}
	public void setMaxApplicationsPerCustomer(Integer maxApplicationsPerCustomer) {
		this.maxApplicationsPerCustomer = maxApplicationsPerCustomer;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	public String getCouponStatus() {
		return couponStatus;
	}
	public void setCouponStatus(String couponStatus) {
		this.couponStatus = couponStatus;
	}
	public List<String> getCartDiscount() {
		return CartDiscount;
	}
	public void setCartDiscount(List<String> cartDiscount) {
		CartDiscount = cartDiscount;
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
	public PurchaseDetails getPurchaseDetailsObj() {
		return purchaseDetailsObj;
	}
	public void setPurchaseDetailsObj(PurchaseDetails purchaseDetailsObj) {
		this.purchaseDetailsObj = purchaseDetailsObj;
	}

}
