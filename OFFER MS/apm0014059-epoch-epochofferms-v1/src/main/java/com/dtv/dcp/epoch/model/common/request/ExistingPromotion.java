package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExistingPromotion implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String agreementId;
	
	private String agreementPriceCode;
	
	private String offerId;
	
	private String status;
	
	private String agreementStartDate;
	
	private String agreementEndDate;
	
	private String benefitCode;
	
	private String startDate;
	
	private String endDate;
	
	private List<Product> products;
	
	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getOfferId() {
		return offerId;
	}

	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	public String getBenefitCode() {
		return benefitCode;
	}

	public void setBenefitCode(String benefitCode) {
		this.benefitCode = benefitCode;
	}

	public String getAgreementId() {
		return agreementId;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}

	public String getAgreementPriceCode() {
		return agreementPriceCode;
	}

	public void setAgreementPriceCode(String agreementPriceCode) {
		this.agreementPriceCode = agreementPriceCode;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAgreementStartDate() {
		return agreementStartDate;
	}

	public void setAgreementStartDate(String agreementStartDate) {
		this.agreementStartDate = agreementStartDate;
	}

	public String getAgreementEndDate() {
		return agreementEndDate;
	}

	public void setAgreementEndDate(String agreementEndDate) {
		this.agreementEndDate = agreementEndDate;
	}

	public static class Product{
		
		private String billingProductCode;
		private String pricePlanCode;
		private Double recurringCreditAmount;
		
		public Double getRecurringCreditAmount() {
			return recurringCreditAmount;
		}

		public void setRecurringCreditAmount(Double recurringCreditAmount) {
			this.recurringCreditAmount = recurringCreditAmount;
		}

		public String getPricePlanCode() {
			return pricePlanCode;
		}

		public void setPricePlanCode(String pricePlanCode) {
			this.pricePlanCode = pricePlanCode;
		}

		public String getBillingProductCode() {
			return billingProductCode;
		}


		public void setBillingProductCode(String billingProductCode) {
			this.billingProductCode = billingProductCode;
		}
		
		
	}

	@Override
	public String toString() {
		return "ExistingPromotion [agreementId=" + agreementId + ", agreementPriceCode=" + agreementPriceCode
				+ ", offerId=" + offerId + ", status=" + status + ", agreementStartDate=" + agreementStartDate
				+ ", agreementEndDate=" + agreementEndDate 
				+ ", benefitCode=" + benefitCode + ", startDate=" + startDate +", endDate=" + endDate
				+ ", products=" + products + "]";
	}

}
