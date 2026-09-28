package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.product.ProductDescriptionsByKey;

public class CouponProductAttributes implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The displayName. */
	private GenericLocaleBase  displayName;
	
	/** The displayNamesByKey. */
	private GenericByKey displayNamesByKey;
	
	/** The descriptionsByKey. */
	private ProductDescriptionsByKey descriptionsByKey;
	
	/** The billingProductCode. */
	private String billingProductCode ;
	
	/** The billingCode. */
	private String billingCode ;
	
	/** The billingProductId. */
	private String  billingProductId ;
	
	/** The startDate. */
	private String startDate ;
	
	/** The endDate. */
	private String endDate ;
	
	
	/** The productFamily. */
	private String productFamily ;
	
    //Campaign Attributes
    
    private String campaignId;
    private String campaignCode;
    private String campaignName;
    private String campaignDesc;
    private String couponType;
    private String campaignCategory;
    private String createdAt;
    private String campaignStartDate;
    private String campaignEndDate;
    private String autoRegister;
    private Boolean studioSponsorByATT;
    private String lockDuration;
    private Integer numberOfCoupons;
    private String campaignMessaging;
	private String redemptionLimit;

	public String getRedemptionLimit() {
		return redemptionLimit;
	}

	public void setRedemptionLimit(String redemptionLimit) {
		this.redemptionLimit = redemptionLimit;
	}

    private List<EligibilityContext> campaignEligibility;
	private List<EligibilityContext> campaignEligibilityOriginal;
    
	public GenericLocaleBase getDisplayName() {
		return displayName;
	}
	public void setDisplayName(GenericLocaleBase displayName) {
		this.displayName = displayName;
	}
	public GenericByKey getDisplayNamesByKey() {
		return displayNamesByKey;
	}
	public void setDisplayNamesByKey(GenericByKey displayNamesByKey) {
		this.displayNamesByKey = displayNamesByKey;
	}
	public ProductDescriptionsByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}
	public void setDescriptionsByKey(ProductDescriptionsByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}
	public String getBillingProductCode() {
		return billingProductCode;
	}
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}
	public String getBillingCode() {
		return billingCode;
	}
	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}
	public String getBillingProductId() {
		return billingProductId;
	}
	public void setBillingProductId(String billingProductId) {
		this.billingProductId = billingProductId;
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
	public String getProductFamily() {
		return productFamily;
	}
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getCampaignCode() {
		return campaignCode;
	}
	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}
	public String getCampaignName() {
		return campaignName;
	}
	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}
	public String getCampaignDesc() {
		return campaignDesc;
	}
	public void setCampaignDesc(String campaignDesc) {
		this.campaignDesc = campaignDesc;
	}
	public String getCouponType() {
		return couponType;
	}
	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}
	public String getCampaignCategory() {
		return campaignCategory;
	}
	public void setCampaignCategory(String campaignCategory) {
		this.campaignCategory = campaignCategory;
	}
	public String getCampaignStartDate() {
		return campaignStartDate;
	}
	public void setCampaignStartDate(String campaignStartDate) {
		this.campaignStartDate = campaignStartDate;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public String getCampaignEndDate() {
		return campaignEndDate;
	}
	public void setCampaignEndDate(String campaignEndDate) {
		this.campaignEndDate = campaignEndDate;
	}
	public String getAutoRegister() {
		return autoRegister;
	}
	public void setAutoRegister(String autoRegister) {
		this.autoRegister = autoRegister;
	}
	public Boolean getStudioSponsorByATT() {
		return studioSponsorByATT;
	}
	public void setStudioSponsorByATT(Boolean studioSponsorByATT) {
		this.studioSponsorByATT = studioSponsorByATT;
	}
	public String getLockDuration() {
		return lockDuration;
	}
	public void setLockDuration(String lockDuration) {
		this.lockDuration = lockDuration;
	}
	public Integer getNumberOfCoupons() {
		return numberOfCoupons;
	}
	public void setNumberOfCoupons(Integer numberOfCoupons) {
		this.numberOfCoupons = numberOfCoupons;
	}
	public String getCampaignMessaging() {
		return campaignMessaging;
	}
	public void setCampaignMessaging(String campaignMessaging) {
		this.campaignMessaging = campaignMessaging;
	}

	public List<EligibilityContext> getCampaignEligibility() {
		return campaignEligibility;
	}

	public void setCampaignEligibility(List<EligibilityContext> campaignEligibility) {
		this.campaignEligibility = campaignEligibility;
	}

}
