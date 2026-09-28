package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint implements Serializable {
	
	private String productFamily;

	private List<ComplianceProduct> complianceProducts;

	/**
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * @param productFamily the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	/**
	 * @return the complianceProducts
	 */
	public List<ComplianceProduct> getComplianceProducts() {
		return complianceProducts;
	}

	/**
	 * @param complianceProducts the complianceProducts to set
	 */
	public void setComplianceProducts(List<ComplianceProduct> complianceProducts) {
		this.complianceProducts = complianceProducts;
	}
	
	
}
