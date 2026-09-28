package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingPrice implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private Double amount;
	
	private Double baseAmount;
	
	private String currencyType;
	
	private String installmentEligibility;
	
	private Integer numberOfInstallment;
	
	private Double msrp;
	
	private String priceType;
	
	private Double total;
	
	// STMS BOM attributes
	private Double totalAmount;
	private Double subTotalAmount;
	private Double taxAmount;
	private String frequencyOfCharge;
	private Double shippingAmount;
	private RecurringPriceValue recurringPriceList;
	
	@JsonIgnore
	private Boolean isRoadrunner;
	
	public Boolean getIsRoadrunner() {
		return isRoadrunner;
	}
	public void setIsRoadrunner(Boolean isRoadrunner) {
		this.isRoadrunner = isRoadrunner;
	}
    
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public Double getBaseAmount() {
		return baseAmount;
	}
	public void setBaseAmount(Double baseAmount) {
		this.baseAmount = baseAmount;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	public String getInstallmentEligibility() {
		return installmentEligibility;
	}
	public void setInstallmentEligibility(String installmentEligibility) {
		this.installmentEligibility = installmentEligibility;
	}
	public Integer getNumberOfInstallment() {
		return numberOfInstallment;
	}
	public void setNumberOfInstallment(Integer numberOfInstallment) {
		this.numberOfInstallment = numberOfInstallment;
	}
	public Double getMsrp() {
		return msrp;
	}
	public void setMsrp(Double msrp) {
		this.msrp = msrp;
	}
	public String getPriceType() {
		return priceType;
	}
	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		this.total = total;
	}
	public Double getTotalAmount() {
		return totalAmount;
	}
	public void setTotalAmount(Double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public Double getSubTotalAmount() {
		return subTotalAmount;
	}
	public void setSubTotalAmount(Double subTotalAmount) {
		this.subTotalAmount = subTotalAmount;
	}
	public Double getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(Double taxAmount) {
		this.taxAmount = taxAmount;
	}
	public String getFrequencyOfCharge() {
		return frequencyOfCharge;
	}
	public void setFrequencyOfCharge(String frequencyOfCharge) {
		this.frequencyOfCharge = frequencyOfCharge;
	}
	public Double getShippingAmount() {
		return shippingAmount;
	}
	public void setShippingAmount(Double shippingAmount) {
		this.shippingAmount = shippingAmount;
	}
	public RecurringPriceValue getRecurringPriceList() {
		return recurringPriceList;
	}
	public void setRecurringPriceList(RecurringPriceValue recurringPriceList) {
		this.recurringPriceList = recurringPriceList;
	}
	@Override
	public String toString() {
		return "BillingPrice [amount=" + amount + ", baseAmount=" + baseAmount + ", currencyType=" + currencyType
				+ ", installmentEligibility=" + installmentEligibility + ", numberOfInstallment=" + numberOfInstallment
				+ ", msrp=" + msrp + ", priceType=" + priceType  + ", total=" + total
				+ ", totalAmount=" + totalAmount + ", subTotalAmount=" + subTotalAmount + ", taxAmount=" + taxAmount
				+ ", frequencyOfCharge=" + frequencyOfCharge + ", shippingAmount=" + shippingAmount
				+ ", recurringPriceList=" + recurringPriceList + "]";
	}
	
}