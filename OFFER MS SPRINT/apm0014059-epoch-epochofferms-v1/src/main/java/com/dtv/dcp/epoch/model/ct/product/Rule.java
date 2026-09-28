package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private List<String> salesChannel;
	private List<String> businessSegment;
	private List<String> customerSegment;
	private List<String> userType;
	private List<String> flowActionType;
	private boolean actionIndicator;
	public List<String> getSalesChannel() {
		return salesChannel;
	}
	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}
	public List<String> getBusinessSegment() {
		return businessSegment;
	}
	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}
	public List<String> getCustomerSegment() {
		return customerSegment;
	}
	public void setCustomerSegment(List<String> customerSegment) {
		this.customerSegment = customerSegment;
	}
	public List<String> getUserType() {
		return userType;
	}
	public void setUserType(List<String> userType) {
		this.userType = userType;
	}
	public List<String> getFlowActionType() {
		return flowActionType;
	}
	public void setFlowActionType(List<String> flowActionType) {
		this.flowActionType = flowActionType;
	}
	public boolean isActionIndicator() {
		return actionIndicator;
	}
	public void setActionIndicator(boolean actionIndicator) {
		this.actionIndicator = actionIndicator;
	}
}
