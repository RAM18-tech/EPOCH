package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class ServiceAddress implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String state;
	private String zipCode;
	private String streetAddress;
	private String city;
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	@Override
	public String toString() {
		return "ServiceAddress [state=" + state + ", zipCode=" + zipCode + ", streetAddress=" + streetAddress
				+ ", city=" + city + "]";
	}
	
}
