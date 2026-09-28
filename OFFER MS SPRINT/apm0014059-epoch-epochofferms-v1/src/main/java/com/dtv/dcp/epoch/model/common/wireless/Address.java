package com.dtv.dcp.epoch.model.common.wireless;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Address
 *
 */
public class Address {
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("addressLine1")
	private String addressLine1;
	
	@JsonProperty("addressLine2")
	private String addressLine2;
	
	@JsonProperty("city")
	private String city;
	
	@JsonProperty("zipCode")
	private String zipCode;
	
	@JsonProperty("zipCodeExtention")
	private String zipCodeExtention;
	
	@JsonProperty("country")
	private String country;
	
	@JsonProperty("state")
	private String state;
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAddressLine1() {
		return addressLine1;
	}

	public void setAddressLine1(String addressLine1) {
		this.addressLine1 = addressLine1;
	}

	public String getAddressLine2() {
		return addressLine2;
	}

	public void setAddressLine2(String addressLine2) {
		this.addressLine2 = addressLine2;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getZipCodeExtention() {
		return zipCodeExtention;
	}

	public void setZipCodeExtention(String zipCodeExtention) {
		this.zipCodeExtention = zipCodeExtention;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
	

	
	

}
