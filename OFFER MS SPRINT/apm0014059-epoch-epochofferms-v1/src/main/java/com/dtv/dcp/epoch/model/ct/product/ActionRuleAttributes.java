package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ActionRuleAttributes implements Serializable{
		
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String salesChannel;

	private String businessSegment;
	
	private String customerSegments;

	private String userType;
	
	private String flowActionType;
	
	private Boolean actionIndicator;

	private String iapPartnerAccountType;
	
	private List<String> ineligibleAccountStatuses;
	
	private List<String> opusStoreIds;
	
	private List<String> opusSubChannels;
	
	private List<String> opusChannels;

	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public String getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}

	public String getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(String customerSegments) {
		this.customerSegments = customerSegments;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getFlowActionType() {
		return flowActionType;
	}

	public void setFlowActionType(String flowActionType) {
		this.flowActionType = flowActionType;
	}

	public Boolean getActionIndicator() {
		return actionIndicator;
	}

	public void setActionIndicator(Boolean actionIndicator) {
		this.actionIndicator = actionIndicator;
	}

	public String getIapPartnerAccountType() {
		return iapPartnerAccountType;
	}

	public void setIapPartnerAccountType(String iapPartnerAccountType) {
		this.iapPartnerAccountType = iapPartnerAccountType;
	}
	
	public List<String> getIneligibleAccountStatuses() {
		return ineligibleAccountStatuses;
	}

	public void setIneligibleAccountStatuses(List<String> ineligibleAccountStatuses) {
		this.ineligibleAccountStatuses = ineligibleAccountStatuses;
	}

	public List<String> getOpusStoreIds() {
		return opusStoreIds;
	}

	public void setOpusStoreIds(List<String> opusStoreIds) {
		this.opusStoreIds = opusStoreIds;
	}

	public List<String> getOpusSubChannels() {
		return opusSubChannels;
	}

	public void setOpusSubChannels(List<String> opusSubChannels) {
		this.opusSubChannels = opusSubChannels;
	}

	public List<String> getOpusChannels() {
		return opusChannels;
	}

	public void setOpusChannels(List<String> opusChannels) {
		this.opusChannels = opusChannels;
	}
	
}
