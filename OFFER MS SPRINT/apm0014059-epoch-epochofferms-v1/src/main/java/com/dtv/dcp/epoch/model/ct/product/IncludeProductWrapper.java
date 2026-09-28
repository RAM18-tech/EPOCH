package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class IncludeProductWrapper implements Serializable   {

	/**
	 * The Class Attributes
	 */
	private static final long serialVersionUID = 1L;

	private List<IncludedProduct> products;

    private List<IncludedProduct> billingConflictingProducts;
	
	private Constraints constraints;

	public Constraints getConstraints() {
		return constraints;
	}

	public void setConstraints(Constraints constraints) {
		this.constraints = constraints;
	}

	public List<IncludedProduct> getProducts() {
		return products;
	}

	public void setProducts(List<IncludedProduct> products) {
		this.products = products;
	}

    public List<IncludedProduct> getBillingConflictingProducts() {
        return billingConflictingProducts;
    }

    public void setBillingConflictingProducts(List<IncludedProduct> billingConflictingProducts) {
        this.billingConflictingProducts = billingConflictingProducts;
    }
}
