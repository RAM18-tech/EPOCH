package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDetails implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<CGProduct> products;

	public List<CGProduct> getProducts() {
		return products;
	}

	public void setProducts(List<CGProduct> products) {
		this.products = products;
	}
}