package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String zipCode;
	private String dma;
	private String city;
	private String state;
	private String county;
	private String region;

	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * @param zipCode the zipCode to set
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * @return the dma
	 */
	public String getDma() {
		return dma;
	}

	/**
	 * @param dma
	 *            the dma to set
	 */
	public void setDma(String dma) {
		this.dma = dma;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city
	 *            the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the country
	 */
	public String getCounty() {
		return county;
	}

	/**
	 * @param country
	 *            the country to set
	 */
	public void setCounty(String county) {
		this.county = county;
	}

	/**
	 * @return the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @param region
	 *            the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Location [zipCode=");
		builder.append(zipCode);
		builder.append(", dma=");
		builder.append(dma);
		builder.append(", city=");
		builder.append(city);
		builder.append(", state=");
		builder.append(state);
		builder.append(", county=");
		builder.append(county);
		builder.append(", region=");
		builder.append(region);
		builder.append("]");
		return builder.toString();
	}
}
