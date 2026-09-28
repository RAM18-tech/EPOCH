package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomCouponFields implements Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -4444655955430921914L;

	private String campaignCode;
	
	/** The id. */
	private String couponStatus;

	public String getCampaignCode() {
		return campaignCode;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	public String getCouponStatus() {
		return couponStatus;
	}

	public void setCouponStatus(String couponStatus) {
		this.couponStatus = couponStatus;
	}

	@Override
	public String toString() {
		return "CustomCouponFields [campaignCode=" + campaignCode + ", couponStatus=" + couponStatus + "]";
	}
	
}
