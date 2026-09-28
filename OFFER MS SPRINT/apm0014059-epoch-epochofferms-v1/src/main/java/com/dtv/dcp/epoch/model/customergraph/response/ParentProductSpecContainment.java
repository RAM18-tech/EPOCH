package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ParentProductSpecContainment implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ContainedProduct> containedProducts;

	public List<ContainedProduct> getContainedProducts() {
		return containedProducts;
	}

	public void setContainedProducts(List<ContainedProduct> containedProducts) {
		this.containedProducts = containedProducts;
	}
}