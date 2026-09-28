package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MinMaxQuantity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String maxQuantity;
	
	private String minQuantity;
	
	private List<String>contractApplicable;
	
	public String getMaxQuantity() {
		return maxQuantity;
	}

	public void setMaxQuantity(String maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public String getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(String minQuantity) {
		this.minQuantity = minQuantity;
	}

	public List<String> getContractApplicable() {
		return contractApplicable;
	}

	public void setContractApplicable(List<String> contractApplicable) {
		this.contractApplicable = contractApplicable;
	}
}
