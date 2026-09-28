package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class Code implements Serializable{

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String billingCode;
	private String billingProductCode;
	private String billingReferenceId;
	private String pricePlanCode;
	private String pickCode;	
	public String getBillingReferenceId() {
		return billingReferenceId;
	}
	public void setBillingReferenceId(String billingReferenceId) {
		this.billingReferenceId = billingReferenceId;
	}
	public String getBillingCode() {
		return billingCode;
	}
	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}
	public String getBillingProductCode() {
		return billingProductCode;
	}
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}
	/**
	 * @return the pricePlanCode
	 */
	public String getPricePlanCode() {
		return pricePlanCode;
	}
	/**
	 * @param pricePlanCode the pricePlanCode to set
	 */
	public void setPricePlanCode(String pricePlanCode) {
		this.pricePlanCode = pricePlanCode;
	}
	/**
	 * @return the pickCode
	 */
	public String getPickCode() {
		return pickCode;
	}
	/**
	 * @param pickCode the pickCode to set
	 */
	public void setPickCode(String pickCode) {
		this.pickCode = pickCode;
	}
}
