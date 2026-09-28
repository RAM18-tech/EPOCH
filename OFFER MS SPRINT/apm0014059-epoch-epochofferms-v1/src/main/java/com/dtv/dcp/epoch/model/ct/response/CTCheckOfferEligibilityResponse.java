package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CTCheckOfferEligibilityResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("offerStatuses")
	private List<OfferEligibility> offerStatuses=new ArrayList<OfferEligibility>();
	/**
	 * @return the offerStatuses
	 */
	public List<OfferEligibility> getOfferStatuses() {
		return offerStatuses;
	}

	/**
	 * @param offerStatuses
	 *            the offerStatuses to set
	 */
	public void setOfferStatuses(List<OfferEligibility> offerStatuses) {
		this.offerStatuses = offerStatuses;
	}

	@Override
	public String toString() {
		return "CTValidateOfferResponse [offerStatuses=" + offerStatuses + "]";
	}
}
