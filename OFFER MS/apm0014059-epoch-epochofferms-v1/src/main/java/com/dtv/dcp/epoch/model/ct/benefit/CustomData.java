package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.dtv.dcp.epoch.model.ct.response.TaxLineItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String appliesToSvc;
	private Double governmentFees;
	private Double attFeesAndSurcharges;
	private Integer agreementPriceCode;
	private String offerId;
	@JsonProperty("FTVCount")
	private String ftvCount;
	@JsonProperty("NFTV")
	private String nftv;
	private String offerLanguage;
	private Integer duration;
	@JsonProperty("UseTaxInstallationAmount")
	private Double useTaxInstallationAmount;
	private String productType;
	private String productSubType;
	private String associatedDevice;
	private Double deviceCharge;
	private Double offerPrice;
	@JsonIgnore
	private Map<String, Double> taxMap;
	private List<TaxLineItem> taxLineItems;
	private Double dollarAmount;
	private String addonType;
	private List<String> premiumPickCode;
	private Integer previousLineItemId;
	private String equipmentType;
	private String extPromoType;
	private String installActionRequired;
	
	private Double offerPricewithLocals;
	
	private Double planPricewithLocals;

	public Double getPlanPricewithLocals() {
		return planPricewithLocals;
	}

	public void setPlanPricewithLocals(Double planPricewithLocals) {
		this.planPricewithLocals = planPricewithLocals;
	}

	public Double getOfferPricewithLocals() {
		return offerPricewithLocals;
	}

	public void setOfferPricewithLocals(Double offerPricewithLocals) {
		this.offerPricewithLocals = offerPricewithLocals;
	}
	
	public String getInstallActionRequired() {
		return installActionRequired;
	}

	public void setInstallActionRequired(String installActionRequired) {
		this.installActionRequired = installActionRequired;
	}

	public String getExtPromoType() {
		return extPromoType;
	}

	public void setExtPromoType(String extPromoType) {
		this.extPromoType = extPromoType;
	}

	public String getEquipmentType() {
		return equipmentType;
	}

	public void setEquipmentType(String equipmentType) {
		this.equipmentType = equipmentType;
	}
	
	public Integer getPreviousLineItemId() {
		return previousLineItemId;
	}

	public void setPreviousLineItemId(Integer previousLineItemId) {
		this.previousLineItemId = previousLineItemId;
	}

	public List<String> getPremiumPickCode() {
		return premiumPickCode;
	}
	
	public void setPremiumPickCode(List<String> premiumPickCode) {
		this.premiumPickCode = premiumPickCode;
	}
	
	public String getAddonType() {
		return addonType;
	}

	public void setAddonType(String addonType) {
		this.addonType = addonType;
	}

	public Double getDollarAmount() {
		return dollarAmount;
	}

	public void setDollarAmount(Double dollarAmount) {
		this.dollarAmount = dollarAmount;
	}

	/**
	 * @return the taxMap
	 */
	public Map<String, Double> getTaxMap() {
		return taxMap;
	}

	/**
	 * @param taxMap the taxMap to set
	 */
	public void setTaxMap(Map<String, Double> taxMap) {
		this.taxMap = taxMap;
	}

	/**
	 * @return the taxLineItems
	 */
	public List<TaxLineItem> getTaxLineItems() {
		return taxLineItems;
	}

	/**
	 * @param taxLineItems the taxLineItems to set
	 */
	public void setTaxLineItems(List<TaxLineItem> taxLineItems) {
		this.taxLineItems = taxLineItems;
	}

	public Double getDeviceCharge() {
		return deviceCharge;
	}

	public void setDeviceCharge(Double deviceCharge) {
		this.deviceCharge = deviceCharge;
	}

	public String getAssociatedDevice() {
		return associatedDevice;
	}

	public void setAssociatedDevice(String associatedDevice) {
		this.associatedDevice = associatedDevice;
	}
	
	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getProductSubType() {
		return productSubType;
	}

	public void setProductSubType(String productSubType) {
		this.productSubType = productSubType;
	}
	
	public String getFtvCount() {
		return ftvCount;
	}

	public void setFtvCount(String ftvCount) {
		this.ftvCount = ftvCount;
	}

	public String getNftv() {
		return nftv;
	}

	public void setNftv(String nftv) {
		this.nftv = nftv;
	}
	
	public String getOfferLanguage() {
		return offerLanguage;
	}

	public void setOfferLanguage(String offerLanguage) {
		this.offerLanguage = offerLanguage;
	}

	public Double getGovernmentFees() {
		return governmentFees;
	}

	public void setGovernmentFees(Double governmentFees) {
		this.governmentFees = governmentFees;
	}

	public Double getAttFeesAndSurcharges() {
		return attFeesAndSurcharges;
	}

	public void setAttFeesAndSurcharges(Double attFeesAndSurcharges) {
		this.attFeesAndSurcharges = attFeesAndSurcharges;
	}

	public String getAppliesToSvc() {
		return appliesToSvc;
	}

	public void setAppliesToSvc(String appliesToSvc) {
		this.appliesToSvc = appliesToSvc;
	}

	public Integer getAgreementPriceCode() {
		return agreementPriceCode;
	}

	public void setAgreementPriceCode(Integer agreementPriceCode) {
		this.agreementPriceCode = agreementPriceCode;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	
	public Double getUseTaxInstallationAmount() {
		return useTaxInstallationAmount;
	}

	public void setUseTaxInstallationAmount(Double useTaxInstallationAmount) {
		this.useTaxInstallationAmount = useTaxInstallationAmount;
	}
	
	public Double getOfferPrice() {
		return offerPrice;
	}

	public void setOfferPrice(Double offerPrice) {
		this.offerPrice = offerPrice;
	}
	@JsonProperty("BPTYPE")
	private String bpType;

	public String getBpType() {
		return bpType;
	}

	public void setBpType(String bpType) {
		this.bpType = bpType;
	}

	@Override
	public String toString() {
		return "CustomData [appliesToSvc=" + appliesToSvc + ", governmentFees=" + governmentFees
				+ ", attFeesAndSurcharges=" + attFeesAndSurcharges + ", agreementPriceCode=" + agreementPriceCode
				+ ", offerId=" + offerId + ", ftvCount=" + ftvCount + ", nftv=" + nftv + ", offerLanguage="
				+ offerLanguage + ", duration=" + duration + ", useTaxInstallationAmount=" + useTaxInstallationAmount
				+ ", productType=" + productType + ", productSubType=" + productSubType + ", associatedDevice="
				+ associatedDevice + ", deviceCharge=" + deviceCharge + ", offerPrice=" + offerPrice + ", taxLineItems=" + taxLineItems + ", dollarAmount="+dollarAmount+
				", addonType="+addonType+ ", bpType=" + bpType + "]";
	}

	

}