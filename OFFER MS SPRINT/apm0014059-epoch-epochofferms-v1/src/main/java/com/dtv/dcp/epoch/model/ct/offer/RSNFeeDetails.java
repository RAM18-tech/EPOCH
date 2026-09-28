package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RSNFeeDetails implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String sku;
	private double amount;
	private boolean visible;
	private String displayName;
	private String feeType;
	private String offerId;
	
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public boolean isVisible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFeeType() {
		return feeType;
	}
	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FeeDetails [sku=");
		builder.append(sku);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", visible=");
		builder.append(visible);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", feeType=");
		builder.append(feeType);
		builder.append("]");
		return builder.toString();
	}
}
