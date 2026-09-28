package com.dtv.dcp.epoch.model.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PreviousCustomerAddress implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String state;
	private String dma;
	private String county;
	private String city;
	private String region;
	private String zipCode;
	private String zipCodePlus4;
	private List<String> customerCallIntent;
	/** The fipsCode. */
	private String fipsCode;
	
	/**
	 * @return the fipsCode
	 */
	public String getFipsCode() {
		return fipsCode;
	}

	public void setFipsCode(String fipsCode) {
		this.fipsCode = fipsCode;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDma() {
		return dma;
	}

	public void setDma(String dma) {
		this.dma = dma;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getZipCodePlus4() {
		return zipCodePlus4;
	}

	public void setZipCodePlus4(String zipCodePlus4) {
		this.zipCodePlus4 = zipCodePlus4;
	}

	public List<String> getCustomerCallIntent() {
		return customerCallIntent;
	}

	public void setCustomerCallIntent(List<String> customerCallIntent) {
		this.customerCallIntent = customerCallIntent;
	}

	@Override
	public int hashCode() {
		return Objects.hash(city, county, customerCallIntent, dma, region, state, zipCode, zipCodePlus4);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PreviousCustomerAddress other = (PreviousCustomerAddress) obj;
		return Objects.equals(city, other.city) && Objects.equals(county, other.county)
				&& Objects.equals(customerCallIntent, other.customerCallIntent) && Objects.equals(dma, other.dma)
				&& Objects.equals(region, other.region) && Objects.equals(state, other.state)
				&& Objects.equals(zipCode, other.zipCode) && Objects.equals(zipCodePlus4, other.zipCodePlus4);
	}

	@Override
	public String toString() {
		return "PreviousCustomerAddress [state=" + state + ", dma=" + dma + ", county=" + county + ", city=" + city
				+ ", region=" + region + ", zipCode=" + zipCode + ", zipCodePlus4=" + zipCodePlus4
				+ ", customerCallIntent=" + customerCallIntent + "]";
	}
}