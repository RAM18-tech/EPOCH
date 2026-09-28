package com.dtv.dcp.epoch.model.common.wireless;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Subscriber
 *
 */
public class Subscriber {

	@JsonProperty("subscriberNumber")
	private String subscriberNumber;

	@JsonProperty("subscriberStatus")
	private String subscriberStatus;

	@JsonProperty("primary")
	private boolean primary;

	@JsonProperty("groupType")
	private String groupType;

	@JsonProperty("groupId")
	private String groupId;

	@JsonProperty("additionalOfferings")
	private List<AdditionalOffering> additionalOfferings = null;

	@JsonProperty("billingMarketInfo")
	private BillingMarketSubscriber billingMarket;

	@JsonProperty("ratePlanCode")
	private String ratePlanCode;

	@JsonProperty("deviceDetailsInfo")
	private DeviceDetailsInfo deviceDetailsInfo;

	public String getSubscriberNumber() {
		return subscriberNumber;
	}

	public void setSubscriberNumber(String subscriberNumber) {
		this.subscriberNumber = subscriberNumber;
	}

	public String getSubscriberStatus() {
		return subscriberStatus;
	}

	public void setSubscriberStatus(String subscriberStatus) {
		this.subscriberStatus = subscriberStatus;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public List<AdditionalOffering> getAdditionalOfferings() {
		return additionalOfferings;
	}

	public void setAdditionalOfferings(List<AdditionalOffering> additionalOfferings) {
		this.additionalOfferings = additionalOfferings;
	}


	public BillingMarketSubscriber getBillingMarket() {
		return billingMarket;
	}

	public void setBillingMarket(BillingMarketSubscriber billingMarket) {
		this.billingMarket = billingMarket;
	}

	public String getRatePlanCode() {
		return ratePlanCode;
	}

	public void setRatePlanCode(String ratePlanCode) {
		this.ratePlanCode = ratePlanCode;
	}

	public DeviceDetailsInfo getDeviceDetailsInfo() {
		return deviceDetailsInfo;
	}

	public void setDeviceDetailsInfo(DeviceDetailsInfo deviceDetailsInfo) {
		this.deviceDetailsInfo = deviceDetailsInfo;
	}

}
