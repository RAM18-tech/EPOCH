package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

public class LineItemTypeAttribute implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Character productType;
	private String productCode;
	private String priceCode;
	private String type;
	private String rebateId;
	private String adjustmentType;
	private String reasonCode;
	private Double amount;
	private String equipmentOwnership;
	private String equipmentType;
    private Integer quantity;
    private String offerId;
    private String description;
    private Boolean primaryOfferFlag;
	private Integer id;
	private String name;
	private String offerDescription;
	private String optionDescription;
	private List<String> availablePaymentMethods;
	private List<String> availableShippingMethods;
	private String vipLevel;
	
	public String getVipLevel() {
		return vipLevel;
	}
	public void setVipLevel(String vipLevel) {
		this.vipLevel = vipLevel;
	}
	
	public Character getProductType() {
		return productType;
	}
	public void setProductType(Character productType) {
		this.productType = productType;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getPriceCode() {
		return priceCode;
	}
	public void setPriceCode(String priceCode) {
		this.priceCode = priceCode;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRebateId() {
		return rebateId;
	}
	public void setRebateId(String rebateId) {
		this.rebateId = rebateId;
	}
	public String getAdjustmentType() {
		return adjustmentType;
	}
	public void setAdjustmentType(String adjustmentType) {
		this.adjustmentType = adjustmentType;
	}
	public String getReasonCode() {
		return reasonCode;
	}
	public void setReasonCode(String reasonCode) {
		this.reasonCode = reasonCode;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getEquipmentOwnership() {
		return equipmentOwnership;
	}
	public void setEquipmentOwnership(String equipmentOwnership) {
		this.equipmentOwnership = equipmentOwnership;
	}
	public String getEquipmentType() {
		return equipmentType;
	}
	public void setEquipmentType(String equipmentType) {
		this.equipmentType = equipmentType;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Boolean getPrimaryOfferFlag() {
		return primaryOfferFlag;
	}
	public void setPrimaryOfferFlag(Boolean primaryOfferFlag) {
		this.primaryOfferFlag = primaryOfferFlag;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOfferDescription() {
		return offerDescription;
	}
	public void setOfferDescription(String offerDescription) {
		this.offerDescription = offerDescription;
	}
	public String getOptionDescription() {
		return optionDescription;
	}
	public void setOptionDescription(String optionDescription) {
		this.optionDescription = optionDescription;
	}
	public List<String> getAvailablePaymentMethods() {
		return availablePaymentMethods;
	}
	public void setAvailablePaymentMethods(List<String> availablePaymentMethods) {
		this.availablePaymentMethods = availablePaymentMethods;
	}
	public List<String> getAvailableShippingMethods() {
		return availableShippingMethods;
	}
	public void setAvailableShippingMethods(List<String> availableShippingMethods) {
		this.availableShippingMethods = availableShippingMethods;
	}
	
	@Override
	public String toString() {
		return "LineItemTypeAttribute [productType=" + productType + ", productCode=" + productCode + ", priceCode="
				+ priceCode + ", type=" + type + ", rebateId=" + rebateId + ", adjustmentType=" + adjustmentType
				+ ", reasonCode=" + reasonCode + ", amount=" + amount + ", equipmentOwnership=" + equipmentOwnership
				+ ", equipmentType=" + equipmentType + ", quantity=" + quantity + ", offerId=" + offerId
				+ ", description=" + description + ", primaryOfferFlag=" + primaryOfferFlag + ", id=" + id + ", name="
				+ name + ", offerDescription=" + offerDescription + ", optionDescription=" + optionDescription
				+ ", availableShippingMethods=" + availableShippingMethods + ", availablePaymentMethods="
				+ availablePaymentMethods + "]";
	}
	
}