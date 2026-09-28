package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReplacementDevices implements Serializable {

	private static final long serialVersionUID = 1L;

	private String productCode;
	private String warrantyStartDate;
	private String warrantyEndDate;

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getWarrantyStartDate() {
		return warrantyStartDate;
	}

	public void setWarrantyStartDate(String warrantyStartDate) {
		this.warrantyStartDate = warrantyStartDate;
	}

	public String getWarrantyEndDate() {
		return warrantyEndDate;
	}

	public void setWarrantyEndDate(String warrantyEndDate) {
		this.warrantyEndDate = warrantyEndDate;
	}

}