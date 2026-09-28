package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericComputedRules;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductWrapper implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The constraints. */
	private GenericComputedRules computedRules;
	
	/** The constraints. */
	private Constraints constraints;
	
	/** The products. */
	private List<Product> products;

	/**
	 * @return the computedRules
	 */
	public GenericComputedRules getComputedRules() {
		return computedRules;
	}

	/**
	 * @param computedRules the computedRules to set
	 */
	public void setComputedRules(GenericComputedRules computedRules) {
		this.computedRules = computedRules;
	}

	/**
	 * @return the constraints
	 */
	public Constraints getConstraints() {
		return constraints;
	}

	/**
	 * @param constraints the constraints to set
	 */
	public void setConstraints(Constraints constraints) {
		this.constraints = constraints;
	}

	/**
	 * @return the products
	 */
	public List<Product> getProducts() {
		return products;
	}

	/**
	 * @param products the products to set
	 */
	public void setProducts(List<Product> products) {
		this.products = products;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProductWrapper [computedRules=");
		builder.append(computedRules);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append(", products=");
		builder.append(products);
		builder.append("]");
		return builder.toString();
	}

	
}
