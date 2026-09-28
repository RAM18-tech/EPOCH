package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nk3077 on 07/30/2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChannelEligibility  implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The opusGroup. */
	private List<String> opusGroup;
	
	/** The opusChannels. */
	private String opusChannel;
	
	/** The opusSubChannels. */
	private String opusSubChannel;
	
	/** The regions. */
	private List<String> regions;
	
	/** The market. */
	private List<String> market;
	
	/** The opusState. */
	private List<String> opusState;
	
	/** The storeIds. */
	private String opusStoreId;
	
	/** The dealerIds. */
	private List<String> dealerIds;
	
	/** The callCenterIds. */
	private List<String> callCenterIds;
	
	/** The kioskIds. */
	private List<String> kioskIds;
	
	/** The repProfile. */
	private List<String> repProfile;
	
	private String salesChannel;
	private String salesSubChannel;
	private String locationId;
	private String locationTypeId;
	private String dealerCode;
	private String directIntegrationPartnerName;
	private Boolean isFulfillmentDealer;
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

	public Boolean getIsFulfillmentDealer() {
		return isFulfillmentDealer;
	}

	public void setIsFulfillmentDealer(Boolean isFulfillmentDealer) {
		this.isFulfillmentDealer = isFulfillmentDealer;
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

	/**
	 * @return the opusGroup
	 */
	public List<String> getOpusGroup() {
		return opusGroup;
	}

	/**
	 * @param opusGroup the opusGroup to set
	 */
	public void setOpusGroup(List<String> opusGroup) {
		this.opusGroup = opusGroup;
	}

	

	/**
	 * @return the regions
	 */
	public List<String> getRegions() {
		return regions;
	}

	/**
	 * @param regions the regions to set
	 */
	public void setRegions(List<String> regions) {
		this.regions = regions;
	}

	/**
	 * @return the market
	 */
	public List<String> getMarket() {
		return market;
	}

	/**
	 * @param market the market to set
	 */
	public void setMarket(List<String> market) {
		this.market = market;
	}

	/**
	 * @return the opusState
	 */
	public List<String> getOpusState() {
		return opusState;
	}

	/**
	 * @param opusState the opusState to set
	 */
	public void setOpusState(List<String> opusState) {
		this.opusState = opusState;
	}


	/**
	 * @return the dealerIds
	 */
	public List<String> getDealerIds() {
		return dealerIds;
	}

	/**
	 * @param dealerIds the dealerIds to set
	 */
	public void setDealerIds(List<String> dealerIds) {
		this.dealerIds = dealerIds;
	}

	/**
	 * @return the callCenterIds
	 */
	public List<String> getCallCenterIds() {
		return callCenterIds;
	}

	/**
	 * @param callCenterIds the callCenterIds to set
	 */
	public void setCallCenterIds(List<String> callCenterIds) {
		this.callCenterIds = callCenterIds;
	}

	/**
	 * @return the kioskIds
	 */
	public List<String> getKioskIds() {
		return kioskIds;
	}

	/**
	 * @param kioskIds the kioskIds to set
	 */
	public void setKioskIds(List<String> kioskIds) {
		this.kioskIds = kioskIds;
	}

	/**
	 * @return the repProfile
	 */
	public List<String> getRepProfile() {
		return repProfile;
	}

	/**
	 * @param repProfile the repProfile to set
	 */
	public void setRepProfile(List<String> repProfile) {
		this.repProfile = repProfile;
	}

	/**
	 * @return the opusChannel
	 */
	public String getOpusChannel() {
		return opusChannel;
	}

	/**
	 * @param opusChannel the opusChannel to set
	 */
	public void setOpusChannel(String opusChannel) {
		this.opusChannel = opusChannel;
	}

	/**
	 * @return the opusSubChannel
	 */
	public String getOpusSubChannel() {
		return opusSubChannel;
	}

	/**
	 * @param opusSubChannel the opusSubChannel to set
	 */
	public void setOpusSubChannel(String opusSubChannel) {
		this.opusSubChannel = opusSubChannel;
	}


	/**
	 * @return the opusStoreId
	 */
	public String getOpusStoreId() {
		return opusStoreId;
	}

	/**
	 * @param opusStoreId the opusStoreId to set
	 */
	public void setOpusStoreId(String opusStoreId) {
		this.opusStoreId = opusStoreId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ChannelEligibility [opusGroup=");
		builder.append(opusGroup);
		builder.append(", opusChannel=");
		builder.append(opusChannel);
		builder.append(", opusSubChannel=");
		builder.append(opusSubChannel);
		builder.append(", regions=");
		builder.append(regions);
		builder.append(", market=");
		builder.append(market);
		builder.append(", opusState=");
		builder.append(opusState);
		builder.append(", opusStoreId=");
		builder.append(opusStoreId);
		builder.append(", dealerIds=");
		builder.append(dealerIds);
		builder.append(", callCenterIds=");
		builder.append(callCenterIds);
		builder.append(", kioskIds=");
		builder.append(kioskIds);
		builder.append(", repProfile=");
		builder.append(repProfile);
		builder.append(", salesChannel=");
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
