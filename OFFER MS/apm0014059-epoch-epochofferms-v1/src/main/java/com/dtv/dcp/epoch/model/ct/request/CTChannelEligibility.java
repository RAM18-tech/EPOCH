package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTChannelEligibility  implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private String salesChannel;
	private String salesSubChannel;
	private String locationId;
	private String locationTypeId;
	private String dealerCode;
	private String directIntegrationPartnerName;
	private String dealerId;
	private String masterDealerId;

	private String specialPage;

	public String getSpecialPage() { return specialPage; }

	public void setSpecialPage(String specialPage) { this.specialPage = specialPage; }
	
	public String getMasterDealerId() {
		return masterDealerId;
	}

	public void setMasterDealerId(String masterDealerId) {
		this.masterDealerId = masterDealerId;
	}

	public String getDealerId() {
		return dealerId;
	}

	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	/**
	 * @return the salesChannel
	 */
	public String getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel the salesChannel to set
	 */
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	/**
	 * @return the salesSubChannel
	 */
	public String getSalesSubChannel() {
		return salesSubChannel;
	}

	/**
	 * @param salesSubChannel the salesSubChannel to set
	 */
	public void setSalesSubChannel(String salesSubChannel) {
		this.salesSubChannel = salesSubChannel;
	}

	/**
	 * @return the locationId
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * @param locationId the locationId to set
	 */
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	/**
	 * @return the locationTypeId
	 */
	public String getLocationTypeId() {
		return locationTypeId;
	}

	/**
	 * @param locationTypeId the locationTypeId to set
	 */
	public void setLocationTypeId(String locationTypeId) {
		this.locationTypeId = locationTypeId;
	}

	/**
	 * @return the dealerCode
	 */
	public String getDealerCode() {
		return dealerCode;
	}

	/**
	 * @param dealerCode the dealerCode to set
	 */
	public void setDealerCode(String dealerCode) {
		this.dealerCode = dealerCode;
	}

	/**
	 * @return the directIntegrationPartnerName
	 */
	public String getDirectIntegrationPartnerName() {
		return directIntegrationPartnerName;
	}

	/**
	 * @param directIntegrationPartnerName the directIntegrationPartnerName to set
	 */
	public void setDirectIntegrationPartnerName(String directIntegrationPartnerName) {
		this.directIntegrationPartnerName = directIntegrationPartnerName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CTChannelEligibility [salesChannel=");
		builder.append(salesChannel);
		builder.append(", salesSubChannel=");
		builder.append(salesSubChannel);
		builder.append(", locationId=");
		builder.append(locationId);
		builder.append(", locationTypeId=");
		builder.append(locationTypeId);
		builder.append(", dealerCode=");
		builder.append(dealerCode);
		builder.append(", directIntegrationPartnerName=");
		builder.append(directIntegrationPartnerName);
		builder.append(", dealerId=");
		builder.append(dealerId);
		builder.append(", specialPage=");
		builder.append(specialPage);
		builder.append("]");
		return builder.toString();
	}
	
	
}
