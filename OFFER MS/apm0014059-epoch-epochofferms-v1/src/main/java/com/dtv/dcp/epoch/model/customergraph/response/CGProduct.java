package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGProduct implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String productName;
	private String productId;
	private List<String> lineOfBusiness;
	private WirelineAssignedProductDetails wirelineAssignedProductDetails;

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public List<String> getLineOfBusiness() {
		return lineOfBusiness;
	}

	public void setLineOfBusiness(List<String> lineOfBusiness) {
		this.lineOfBusiness = lineOfBusiness;
	}

	public WirelineAssignedProductDetails getWirelineAssignedProductDetails() {
		return wirelineAssignedProductDetails;
	}

	public void setWirelineAssignedProductDetails(WirelineAssignedProductDetails wirelineAssignedProductDetails) {
		this.wirelineAssignedProductDetails = wirelineAssignedProductDetails;
	}
}