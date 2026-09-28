package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OfferEligibility implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("offerCode")
	private String offerCode;
	
	@JsonProperty("eligible")
	private boolean eligible;

	public OfferEligibility() {
		super();
	}

	public OfferEligibility(String offerCode, boolean eligible) {
		super();
		this.offerCode = offerCode;
		this.eligible = eligible;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	public boolean isEligible() {
		return eligible;
	}

	public void setEligible(boolean eligible) {
		this.eligible = eligible;
	}

	@Override
	public String toString() {
		return "OfferStatus [offerCode=" + offerCode + ", eligible=" + eligible + "]";
	}
}
