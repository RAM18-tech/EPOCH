package com.dtv.dcp.epoch.model.ct.coupon;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.CustomCouponBase;
import com.dtv.dcp.epoch.model.common.TextContext;
import com.dtv.dcp.epoch.model.ct.generic.CompatibleProductValue;
import com.dtv.dcp.epoch.model.ct.generic.GenericLastModifiedBy;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Coupon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2090167504909087537L;

	/**
	 * 
	 */
	
	/** The id. */
	private String id;
	
	/** The code. */
	private String code;
	
	/** The status. */
	private String status;
	
    /** The startDate. */
	private String validFrom;
	
	/** The endDate. */
	private String validUntil;
	
	/** The createdAt. */
	private String createdAt;
	
	/** The createdBy. */
	private GenericLastModifiedBy createdBy;
	
	/** The lastModifiedAt. */
	private GenericLastModifiedBy lastModifiedBy;
	
	/** The lastModifiedBy. */
	private String lastModifiedAt;
	
	/** The name. */
	private TextContext name;
	
	/** The description. */
	private TextContext description;
	
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

	private String statusDescription;
    private String billingSystem;
    private String statusUpdateTime;
    private String usedDate;
    private String redemptionDate;
    private String titlePurchased;
    private String retailPrice;
    private String discountAmount;
    private String netAmount;
    
    
	
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

	/** The cartDiscount. */
	private List<CompatibleProductValue> cartDiscounts;
	
	public CustomCouponBase getCustom() {
		return custom;
	}

	public void setCustom(CustomCouponBase custom) {
		this.custom = custom;
	}

	private CustomCouponBase custom;
	
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

	public String getCreatedAt() {
		return createdAt;
	}

	public String getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}

	public String getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(String validUntil) {
		this.validUntil = validUntil;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public GenericLastModifiedBy getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(GenericLastModifiedBy createdBy) {
		this.createdBy = createdBy;
	}

	public GenericLastModifiedBy getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(GenericLastModifiedBy lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(String lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
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

	public TextContext getName() {
		return name;
	}

	public void setName(TextContext name) {
		this.name = name;
	}

	public TextContext getDescription() {
		return description;
	}

	public void setDescription(TextContext description) {
		this.description = description;
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

	public List<CompatibleProductValue> getCartDiscounts() {
		return cartDiscounts;
	}

	public void setCartDiscounts(List<CompatibleProductValue> cartDiscounts) {
		this.cartDiscounts = cartDiscounts;
	}
	
}
