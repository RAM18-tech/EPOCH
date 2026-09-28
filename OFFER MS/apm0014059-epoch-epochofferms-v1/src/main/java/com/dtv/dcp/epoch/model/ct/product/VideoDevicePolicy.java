package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.common.Location;

public class VideoDevicePolicy implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String policy;
	private String customerSegment;
	private String startDate;
	private String endDate;
	private String customerSubType;
	private String description;
	private Location location;
	public String getPolicy() {
		return policy;
	}
	public void setPolicy(String policy) {
		this.policy = policy;
	}
	public String getCustomerSegment() {
		return customerSegment;
	}
	public void setCustomerSegment(String customerSegment) {
		this.customerSegment = customerSegment;
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
	public String getCustomerSubType() {
		return customerSubType;
	}
	public void setCustomerSubType(String customerSubType) {
		this.customerSubType = customerSubType;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
}
