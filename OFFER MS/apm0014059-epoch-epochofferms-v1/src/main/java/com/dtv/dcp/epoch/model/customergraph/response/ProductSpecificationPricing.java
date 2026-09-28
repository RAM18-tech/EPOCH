package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSpecificationPricing implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ChildPricingSchema childPricingSchema;

	public ChildPricingSchema getChildPricingSchema() {
		return childPricingSchema;
	}

	public void setChildPricingSchema(ChildPricingSchema childPricingSchema) {
		this.childPricingSchema = childPricingSchema;
	}
}