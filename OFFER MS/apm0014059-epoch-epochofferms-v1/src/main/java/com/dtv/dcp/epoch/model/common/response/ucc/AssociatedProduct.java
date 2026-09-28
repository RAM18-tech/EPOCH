package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssociatedProduct implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Product> qualifyingProducts;
	private List<Product> bundleProducts;

	public List<Product> getQualifyingProducts() {
		return qualifyingProducts;
	}

	public void setQualifyingProducts(List<Product> qualifyingProducts) {
		this.qualifyingProducts = qualifyingProducts;
	}

	public List<Product> getBundleProducts() {
		return bundleProducts;
	}

	public void setBundleProducts(List<Product> bundleProducts) {
		this.bundleProducts = bundleProducts;
	}

	@Override
	public String toString() {
		return "AssociatedProduct [qualifyingProducts=" + qualifyingProducts + ", bundleProducts=" + bundleProducts
				+ "]";
	}

}