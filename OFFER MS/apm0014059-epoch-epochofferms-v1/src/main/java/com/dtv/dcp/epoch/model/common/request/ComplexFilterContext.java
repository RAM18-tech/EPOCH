package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexFilterContext implements Serializable {
	
	 /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
	
	@JsonProperty(Constants.WIRELESS_PRODUCT_FAMILY)
    private List<ComplexContext> wireless;

	public List<ComplexContext> getWireless() {
		return wireless;
	}

	public void setWireless(List<ComplexContext> wireless) {
		this.wireless = wireless;
	}

}
