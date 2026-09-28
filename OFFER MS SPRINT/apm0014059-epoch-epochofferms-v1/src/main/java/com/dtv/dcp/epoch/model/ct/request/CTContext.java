package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class CartContext.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CTContext implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("salesChannel")
	private String salesChannel;

	@JsonProperty("accountInfo")
	private AccountInfo accountInfo;

	@JsonProperty("serviceInfo")
	private ServiceInfo serviceInfo;
	
	@JsonProperty("validateOnlyReward")
	private boolean validateOnlyReward;
	
	@JsonProperty("zipCode")
	private String zipCode;
	
	@JsonProperty("dma")
	private String dma;
	
	@JsonProperty("city")
	private String city;

	@JsonProperty("state")
	private String state;
	
	@JsonProperty("county")
	private String county;

	@JsonProperty("region")
	private String region;

	/**
	 * @return the salesChannel
	 */
	public String getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel
	 *            the salesChannel to set
	 */
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}
	/**
	 * @return the accountInfo
	 */
	public AccountInfo getAccountInfo() {
		return accountInfo;
	}

	/**
	 * @param accountInfo the accountInfo to set
	 */
	public void setAccountInfo(AccountInfo accountInfo) {
		this.accountInfo = accountInfo;
	}

	/**
	 * @return the serviceInfo
	 */
	public ServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	/**
	 * @param serviceInfo the serviceInfo to set
	 */
	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}
	

	/**
	 * @return the validateOnlyReward
	 */
	public boolean isValidateOnlyReward() {
		return validateOnlyReward;
	}

	/**
	 * @param validateOnlyReward the validateOnlyReward to set
	 */
	public void setValidateOnlyReward(boolean validateOnlyReward) {
		this.validateOnlyReward = validateOnlyReward;
	}

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
	 * @param dma the dma to set
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
	 * @param city the city to set
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
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the county
	 */
	public String getCounty() {
		return county;
	}

	/**
	 * @param county the county to set
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
	 * @param region the region to set
	 */
	public void setRegion(String region) {
		this.region = region;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [salesChannel=");
		builder.append(salesChannel);
		builder.append(", validateOnlyReward=");
		builder.append(validateOnlyReward);
		builder.append(", zipCode=");
		builder.append(zipCode);
		builder.append(", dma=");
		builder.append(dma);
		builder.append(", accountInfo=");
		builder.append(accountInfo);
		builder.append(", serviceInfo=");
		builder.append(serviceInfo);
		builder.append("]");
		return builder.toString();
	}

}
