package com.dtv.dcp.epoch.processor.ott.services.model;

import java.io.Serializable;


public class CtProductInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private String basePrice;
	
	private String status;
	
	private String contractPrice;
	
	private String edspPrice;
	
	private String packageType;
	
	

	public String getPackageType() {
		return packageType;
	}

	public void setPackageType(String packageType) {
		this.packageType = packageType;
	}

	public String getContractPrice() {
		return contractPrice;
	}

	public void setContractPrice(String contractPrice) {
		this.contractPrice = contractPrice;
	}

	
	public String getEdspPrice() {
		return edspPrice;
	}

	public void setEdspPrice(String edspPrice) {
		this.edspPrice = edspPrice;
	}

	public String getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(String basePrice) {
		this.basePrice = basePrice;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
		