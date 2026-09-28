package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.benefit.CustomData;
import com.dtv.dcp.epoch.model.ct.benefit.RelationshipData;
import com.dtv.dcp.epoch.model.ct.product.BillingPrice;
import com.dtv.dcp.epoch.model.ct.product.LineItemDetail;
import com.dtv.dcp.epoch.model.ct.product.LineItemIdentifier;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class STMSLineItem implements Serializable{

	private static final long serialVersionUID = 1L;
	private String orderType;
	private String lineItemType;
	private String action;
	private Boolean isProvisionable;
	private Boolean isVisible;
	private Boolean addedbyRuleFlag;
	private CustomData customData;
	private RelationshipData relationshipList;
	@JsonIgnore
	private String standardName;
	private LineItemIdentifier lineItemIdentifier;
	private BillingPrice price;

	private LineItemDetail lineItemDetail;
	
	@JsonIgnore
	private List<ProductWrapper> applicableProducts;
	
	@JsonIgnore
	private List<String> benefitCodes;
	
	@JsonIgnore
	private String cartSection;
	
	@JsonIgnore
	private String deviceType;
	
	@JsonIgnore
	private String prodId;
	
	@JsonIgnore
	private Boolean isWirelessCapable;
	
	@JsonIgnore
	private String offerCode;
	
	@JsonIgnore
	private String chargeType;
	
	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public String getOfferCode() {
		return offerCode;
	}

	public void setOfferCode(String offerCode) {
		this.offerCode = offerCode;
	}

	public Boolean getIsWirelessCapable() {
		return isWirelessCapable;
	}

	public void setIsWirelessCapable(Boolean isWirelessCapable) {
		this.isWirelessCapable = isWirelessCapable;
	}

	public String getProdId() {
		return prodId;
	}

	public void setProdId(String prodId) {
		this.prodId = prodId;
	}
	
	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	
	public String getCartSection() {
		return cartSection;
	}

	public void setCartSection(String cartSection) {
		this.cartSection = cartSection;
	}
	
	public List<String> getBenefitCodes() {
		return benefitCodes;
	}

	public void setBenefitCodes(List<String> benefitCodes) {
		this.benefitCodes = benefitCodes;
	}

	public String getStandardName() {
		return standardName;
	}

	public void setStandardName(String standardName) {
		this.standardName = standardName;
	}

	public CustomData getCustomData() {
		return customData;
	}

	public void setCustomData(CustomData customData) {
		this.customData = customData;
	}

	public RelationshipData getRelationshipList() {
		return relationshipList;
	}

	public void setRelationshipList(RelationshipData relationshipList) {
		this.relationshipList = relationshipList;
	}

	public BillingPrice getPrice() {
		return price;
	}

	public void setPrice(BillingPrice price) {
		this.price = price;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getLineItemType() {
		return lineItemType;
	}

	public void setLineItemType(String lineItemType) {
		this.lineItemType = lineItemType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Boolean getIsProvisionable() {
		return isProvisionable;
	}

	public void setIsProvisionable(Boolean isProvisionable) {
		this.isProvisionable = isProvisionable;
	}

	public Boolean getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}
	
	public Boolean getAddedbyRuleFlag() {
		return addedbyRuleFlag;
	}

	public void setAddedbyRuleFlag(Boolean addedbyRuleFlag) {
		this.addedbyRuleFlag = addedbyRuleFlag;
	}

	public LineItemIdentifier getLineItemIdentifier() {
		return lineItemIdentifier;
	}

	public void setLineItemIdentifier(LineItemIdentifier lineItemIdentifier) {
		this.lineItemIdentifier = lineItemIdentifier;
	}

	public LineItemDetail getLineItemDetail() {
		return lineItemDetail;
	}

	public void setLineItemDetail(LineItemDetail lineItemDetail) {
		this.lineItemDetail = lineItemDetail;
	}
	
	public List<ProductWrapper> getApplicableProducts() {
		return applicableProducts;
	}

	public void setApplicableProducts(List<ProductWrapper> applicableProducts) {
		this.applicableProducts = applicableProducts;
	}

	@Override
	public String toString() {
		return "STMSLineItem [orderType=" + orderType + ", lineItemType=" + lineItemType + ", action=" + action
				+ ", isProvisionable=" + isProvisionable + ", isVisible=" + isVisible + ", customData=" + customData
				+ ", relationshipList=" + relationshipList + ", lineItemIdentifier=" + lineItemIdentifier + ", price="
				+ price + ", lineItemDetail=" + lineItemDetail + ", applicableProducts=" + applicableProducts + "]";
	}
	
}
