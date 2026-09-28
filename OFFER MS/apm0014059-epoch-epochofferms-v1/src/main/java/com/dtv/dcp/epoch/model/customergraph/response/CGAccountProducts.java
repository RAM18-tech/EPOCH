package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGAccountProducts implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Product> products;

	public List<Product> getProducts() {
		products = Optional.ofNullable(products).orElse(new ArrayList<>());
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
}
