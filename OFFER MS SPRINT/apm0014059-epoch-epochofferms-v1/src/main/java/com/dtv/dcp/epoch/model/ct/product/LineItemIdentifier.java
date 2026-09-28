package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class LineItemIdentifier implements Serializable {

	private static final long serialVersionUID = 1L;
	private String productId;
	private Integer lineItemId;
	private String omsProductID;
	private String productName;
	private String productDescription ;
	private Integer originatorLineItemId;
	private String disconnectReason;
	
	public Integer getOriginatorLineItemId() {
		return originatorLineItemId;
	}
	public void setOriginatorLineItemId(Integer originatorLineItemId) {
		this.originatorLineItemId = originatorLineItemId;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public Integer getLineItemId() {
		return lineItemId;
	}
	public void setLineItemId(Integer lineItemId) {
		this.lineItemId = lineItemId;
	}
	public String getOmsProductID() {
		return omsProductID;
	}
	public void setOmsProductID(String omsProductID) {
		this.omsProductID = omsProductID;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public String getDisconnectReason() {
		return disconnectReason;
	}
	public void setDisconnectReason(String disconnectReason) {
		this.disconnectReason = disconnectReason;
	}
	
	@Override
	public String toString() {
		return "LineItemIdentifier [productId=" + productId + ", lineItemId=" + lineItemId + ", omsProductID="
				+ omsProductID + ", productName=" + productName + ", productDescription=" + productDescription + "]";
	}
	
}
