package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by nk3077 on 07/30/2019.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerEligibility implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The state. */
	private List<String> state;
	
	/** The dma. */
	private List<String> dma;
	
	/** The county. */
	private List<String> county;
	
	/** The city. */
	private List<String> city;
	
	/** The zipCode. */
	private List<String> zipCode;
	
	/** The zipCode-4. */
	@JsonProperty("zipCode+4")
	private List<String> zipCodePlusFour;
	
	/** The customerCallIntent. */
	private List<String> customerCallIntent;
	
	/** The custClassification. */
	private List<String> custClassification;

	/** region */
	private List<String> region;

	private List<String> fipsCode;

	@JsonProperty("isBYODFlow")
	private boolean isBYODFlow;

	public boolean isBYODFlow() {
		return isBYODFlow;
	}

	/**
	 * @return the fipsCode
	 */
	public List<String> getFipsCode() {
		return fipsCode;
	}

	/**
	 * @param fipsCode the fipsCode to set
	 */
	public void setFipsCode(List<String> fipsCode) {
		this.fipsCode = fipsCode;
	}

	/**
	 * @return the state
	 */
	public List<String> getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(List<String> state) {
		this.state = state;
	}

	/**
	 * @return the dma
	 */
	public List<String> getDma() {
		return dma;
	}

	/**
	 * @param dma the dma to set
	 */
	public void setDma(List<String> dma) {
		this.dma = dma;
	}

	/**
	 * @return the county
	 */
	public List<String> getCounty() {
		return county;
	}

	/**
	 * @param county the county to set
	 */
	public void setCounty(List<String> county) {
		this.county = county;
	}

	/**
	 * @return the city
	 */
	public List<String> getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(List<String> city) {
		this.city = city;
	}

	/**
	 * @return the zipCode
	 */
	public List<String> getZipCode() {
		return zipCode;
	}

	/**
	 * @param zipCode the zipCode to set
	 */
	public void setZipCode(List<String> zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * @return the zipCodePlusFour
	 */
	public List<String> getZipCodePlusFour() {
		return zipCodePlusFour;
	}

	/**
	 * @param zipCodePlusFour the zipCodePlusFour to set
	 */
	public void setZipCodePlusFour(List<String> zipCodePlusFour) {
		this.zipCodePlusFour = zipCodePlusFour;
	}

	/**
	 * @return the customerCallIntent
	 */
	public List<String> getCustomerCallIntent() {
		return customerCallIntent;
	}

	/**
	 * @param customerCallIntent the customerCallIntent to set
	 */
	public void setCustomerCallIntent(List<String> customerCallIntent) {
		this.customerCallIntent = customerCallIntent;
	}

	/**
	 * @return the custClassification
	 */
	public List<String> getCustClassification() {
		return custClassification;
	}

	/**
	 * @param custClassification the custClassification to set
	 */
	public void setCustClassification(List<String> custClassification) {
		this.custClassification = custClassification;
	}

	public List<String> getRegion() {
		return region;
	}

	public void setRegion(List<String> region) {
		this.region = region;
	}

	@Override
	public String toString() {
		return "CustomerEligibility{" +
				"state=" + state +
				", dma=" + dma +
				", county=" + county +
				", city=" + city +
				", zipCode=" + zipCode +
				", zipCodePlusFour=" + zipCodePlusFour +
				", customerCallIntent=" + customerCallIntent +
				", custClassification=" + custClassification +
				", region=" + region +
				'}';
	}
}
