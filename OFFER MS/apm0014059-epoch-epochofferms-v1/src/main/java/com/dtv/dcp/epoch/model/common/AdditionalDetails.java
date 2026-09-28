package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalDetails implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String additionalInfoName;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private List<Params> params;

	public String getAdditionalInfoName() {
		return additionalInfoName;
	}

	public void setAdditionalInfoName(String additionalInfoName) {
		this.additionalInfoName = additionalInfoName;
	}

	public List<Params> getParams() {
		return params;
	}

	public void setParams(List<Params> params) {
		this.params = params;
	}

}
