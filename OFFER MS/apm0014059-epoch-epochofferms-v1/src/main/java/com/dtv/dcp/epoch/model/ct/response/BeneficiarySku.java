package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BeneficiarySku implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String sku;
	private String atgProductID;
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getAtgProductId() {
		return atgProductID;
	}
	public void setAtgProductId(String atgProductId) {
		this.atgProductID = atgProductId;
	}

}
