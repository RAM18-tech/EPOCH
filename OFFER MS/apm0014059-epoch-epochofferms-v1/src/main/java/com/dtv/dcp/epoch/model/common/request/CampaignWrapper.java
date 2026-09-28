package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignWrapper implements Serializable {

	private static final long serialVersionUID = 1L;

	private String campaignId;

	private String campaignCode;

	private String campaignName;

	private String campaignDesc;

	private String couponType;

	private String campaignCategory;

	private String creationDate;

	private String campaignStartDate;

	private String campaignEndDate;

	private boolean autoRegister;

	private String studioSponsorByATT;

	private String lockDuration;

	// private String studioSponsorNames;

	// private String campaignEligibility;

	private int numberOfCoupons;

	private int couponLength;

	private String campaignMessaging;

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

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getCampaignStartDate() {
		return campaignStartDate;
	}

	public void setCampaignStartDate(String campaignStartDate) {
		this.campaignStartDate = campaignStartDate;
	}

	public String getCampaignEndDate() {
		return campaignEndDate;
	}

	public void setCampaignEndDate(String campaignEndDate) {
		this.campaignEndDate = campaignEndDate;
	}

	public boolean isAutoRegister() {
		return autoRegister;
	}

	public void setAutoRegister(boolean autoRegister) {
		this.autoRegister = autoRegister;
	}

	public String getStudioSponsorByATT() {
		return studioSponsorByATT;
	}

	public void setStudioSponsorByATT(String studioSponsorByATT) {
		this.studioSponsorByATT = studioSponsorByATT;
	}

	public String getLockDuration() {
		return lockDuration;
	}

	public void setLockDuration(String lockDuration) {
		this.lockDuration = lockDuration;
	}

	/*
	 * public String getStudioSponsorNames() { return studioSponsorNames; }
	 * 
	 * public void setStudioSponsorNames(String studioSponsorNames) {
	 * this.studioSponsorNames = studioSponsorNames; }
	 */
	public int getNumberOfCoupons() {
		return numberOfCoupons;
	}

	public void setNumberOfCoupons(int numberOfCoupons) {
		this.numberOfCoupons = numberOfCoupons;
	}

	public int getCouponLength() {
		return couponLength;
	}

	public void setCouponLength(int couponLength) {
		this.couponLength = couponLength;
	}

	public String getCampaignMessaging() {
		return campaignMessaging;
	}

	public void setCampaignMessaging(String campaignMessaging) {
		this.campaignMessaging = campaignMessaging;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CampaignWrapper [campaignId=");
		builder.append(campaignId);
		builder.append(", campaignCode=");
		builder.append(campaignCode);
		builder.append(", campaignName=");
		builder.append(campaignName);
		builder.append(", campaignDesc=");
		builder.append(campaignDesc);
		builder.append(", couponType=");
		builder.append(couponType);
		builder.append(", campaignCategory=");
		builder.append(campaignCategory);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", campaignStartDate=");
		builder.append(campaignStartDate);
		builder.append(", campaignEndDate=");
		builder.append(campaignEndDate);
		builder.append(", autoRegister=");
		builder.append(autoRegister);
		builder.append(", studioSponsorByATT=");
		builder.append(studioSponsorByATT);
		builder.append(", campaignMessaging=");
		builder.append(campaignMessaging);
		builder.append(", lockDuration=");
		builder.append(lockDuration);
		/*
		 * builder.append(", studioSponsorNames="); builder.append(studioSponsorNames);
		 */
		builder.append(", numberOfCoupons=");
		builder.append(numberOfCoupons);
		builder.append(", couponLength=");
		builder.append(couponLength);
		builder.append("]");
		return builder.toString();
	}

}
