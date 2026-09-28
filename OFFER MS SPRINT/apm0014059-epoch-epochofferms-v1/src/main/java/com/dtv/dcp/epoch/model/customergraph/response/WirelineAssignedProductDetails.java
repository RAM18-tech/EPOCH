package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WirelineAssignedProductDetails implements Serializable{
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private List<ComponentDetail> componentDetails;

	public List<ComponentDetail> getComponentDetails() {
		return componentDetails;
	}

	public void setComponentDetails(List<ComponentDetail> componentDetails) {
		this.componentDetails = componentDetails;
	}
}