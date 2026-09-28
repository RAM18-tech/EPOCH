package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociatedProduct implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The qualifyingProducts. */
	private List<ProductWrapper> qualifyingProducts;
	
	/** The disqualifyingProducts. */
	private List<ProductWrapper> disqualifyingProducts;
	
	/** The includedProducts. */
	private List<ProductWrapper> includedProducts;
	
	/** The bundleProducts. */
	private List<ProductWrapper> bundleProducts;
	
	private List<ProductWrapper> beneficiaryProducts;
	
	private List<String> benefitActionType;
	
	private String predicate;
	
	/** The associatedConditions. */
	private List<ProductWrapper> associatedConditions;

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public List<ProductWrapper> getAssociatedConditions() {
		return associatedConditions;
	}

	public void setAssociatedConditions(List<ProductWrapper> associatedConditions) {
		this.associatedConditions = associatedConditions;
	}

	/**
	 * @return the bundleProducts
	 */
	public List<ProductWrapper> getBundleProducts() {
		return bundleProducts;
	}

	/**
	 * @param bundleProducts the bundleProducts to set
	 */
	public void setBundleProducts(List<ProductWrapper> bundleProducts) {
		this.bundleProducts = bundleProducts;
	}

	/**
	 * @return the qualifyingProducts
	 */
	public List<ProductWrapper> getQualifyingProducts() {
		return qualifyingProducts;
	}

	/**
	 * @param qualifyingProducts the qualifyingProducts to set
	 */
	public void setQualifyingProducts(List<ProductWrapper> qualifyingProducts) {
		this.qualifyingProducts = qualifyingProducts;
	}

	/**
	 * @return the disqualifyingProducts
	 */
	public List<ProductWrapper> getDisqualifyingProducts() {
		return disqualifyingProducts;
	}

	/**
	 * @param disqualifyingProducts the disqualifyingProducts to set
	 */
	public void setDisqualifyingProducts(List<ProductWrapper> disqualifyingProducts) {
		this.disqualifyingProducts = disqualifyingProducts;
	}

	/**
	 * @return the includedProducts
	 */
	public List<ProductWrapper> getIncludedProducts() {
		return includedProducts;
	}

	/**
	 * @param includedProducts the includedProducts to set
	 */
	public void setIncludedProducts(List<ProductWrapper> includedProducts) {
		this.includedProducts = includedProducts;
	}
	
	public List<ProductWrapper> getBeneficiaryProducts() {
		return beneficiaryProducts;
	}

	public void setBeneficiaryProducts(List<ProductWrapper> beneficiaryProducts) {
		this.beneficiaryProducts = beneficiaryProducts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AssociatedProduct [qualifyingProducts=");
		builder.append(qualifyingProducts);
		builder.append(", disqualifyingProducts=");
		builder.append(disqualifyingProducts);
		builder.append(", includedProducts=");
		builder.append(includedProducts);
		builder.append(", bundleProducts=");
		builder.append(bundleProducts);
		builder.append(", beneficiaryProducts=");
		builder.append(beneficiaryProducts);
		builder.append("]");
		return builder.toString();
	}

	public List<String> getBenefitActionType() {
		return benefitActionType;
	}

	public void setBenefitActionType(List<String> benefitActionType) {
		this.benefitActionType = benefitActionType;
	}
	

}