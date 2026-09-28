package com.dtv.dcp.epoch.model.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Promotion implements Serializable {
	private String billingBenefitCode;
	private String startDate;
	private String endDate;
	private String agreementId;
	private String agreementStartDate;
	private String agreementEndDate;

	public String getBillingBenefitCode() {
		return billingBenefitCode;
	}

	public void setBillingBenefitCode(String billingBenefitCode) {
		this.billingBenefitCode = billingBenefitCode;
	}

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

	public String getAgreementId() {
		return agreementId;
	}

	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
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
}