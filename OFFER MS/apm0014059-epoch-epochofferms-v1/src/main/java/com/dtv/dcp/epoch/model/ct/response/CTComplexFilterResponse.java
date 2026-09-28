package com.dtv.dcp.epoch.model.ct.response;


import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTComplexFilterResponse implements Serializable  {
	
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("offerQualification")
	private List<CTOfferQualification> offerQualification;

	public List<CTOfferQualification> getOfferQualification() {
		return offerQualification;
	}

	public void setOfferQualification(List<CTOfferQualification> offerQualification) {
		this.offerQualification = offerQualification;
	}

}
