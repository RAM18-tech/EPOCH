package com.dtv.dcp.epoch.model.ct.productType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductType {

	/** The offers. */
	private List<ProductTypeAttribute> attributes;

	public List<ProductTypeAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ProductTypeAttribute> attributes) {
		this.attributes = attributes;
	}
	
}
