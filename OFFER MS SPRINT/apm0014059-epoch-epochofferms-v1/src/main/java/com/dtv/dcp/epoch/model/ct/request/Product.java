package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

public class Product implements Serializable {

	private static final long serialVersionUID = 1L;
	private String productId;
	private String productCode;
	private String productType;
	private String billingCode;
	private String billingProductCode;
	private String sku;
	private String billingReferenceID;
	private String pricePlanCode;
	private String pickCode;
	private String priceCode;
	private String action;
	private String componentCode;
	private String modelNumber;
	private String manufacturer;
	private String accessCardId;
	private String accessCardStatus;
	private String equipmentOwnership;

	public String getManufacturer() {
		return manufacturer;
	}
	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}
	public String getAccessCardId() {
		return accessCardId;
	}
	public void setAccessCardId(String accessCardId) {
		this.accessCardId = accessCardId;
	}
	public String getAccessCardStatus() {
		return accessCardStatus;
	}
	public void setAccessCardStatus(String accessCardStatus) {
		this.accessCardStatus = accessCardStatus;
	}
	public String getEquipmentOwnership() {
		return equipmentOwnership;
	}
	public void setEquipmentOwnership(String equipmentOwnership) {
		this.equipmentOwnership = equipmentOwnership;
	}
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getPriceCode() {
		return priceCode;
	}
	public void setPriceCode(String priceCode) {
		this.priceCode = priceCode;
	}
	public String getComponentCode() {
		return componentCode;
	}
	public void setComponentCode(String componentCode) {
		this.componentCode = componentCode;
	}
	public String getModelNumber() {
		return modelNumber;
	}
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}
	
	
	public String getBillingReferenceID() {
		return billingReferenceID;
	}
	public void setBillingReferenceID(String billingReferenceID) {
		this.billingReferenceID = billingReferenceID;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
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
	public String getSku() {
		return sku;
	}
	public void setSku(String sku) {
		this.sku = sku;
	}
	public String getPricePlanCode() {
		return pricePlanCode;
	}
	public void setPricePlanCode(String pricePlanCode) {
		this.pricePlanCode = pricePlanCode;
	}
	public String getPickCode() {
		return pickCode;
	}
	public void setPickCode(String pickCode) {
		this.pickCode = pickCode;
	}
	
	@Override
	public String toString() {
		return "Product [productId=" + productId + ", productCode=" + productCode + ", productType=" + productType
				+ ", billingCode=" + billingCode + ", billingProductCode=" + billingProductCode + ", sku=" + sku
				+ ", billingReferenceID=" + billingReferenceID + ", pricePlanCode=" + pricePlanCode + ", pickCode="
				+ pickCode + ", componentCode=" + componentCode + ", modelNumber=" + modelNumber
				+ ", manufacturer=" + manufacturer + ", accessCardId=" + accessCardId
				+ ", accessCardStatus=" + accessCardStatus + ", equipmentOwnership=" + equipmentOwnership + "]";
	}
	
}