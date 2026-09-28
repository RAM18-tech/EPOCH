package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class Installment implements Serializable{
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String installmentCode;
	private String numberOfMonthlyInstallments;
	private String installmentName;
	private String description;
	private String startDate;
	private String endDate;
	private String planVersion;
	public String getInstallmentCode() {
		return installmentCode;
	}
	public void setInstallmentCode(String installmentCode) {
		this.installmentCode = installmentCode;
	}
	public String getNumberOfMonthlyInstallments() {
		return numberOfMonthlyInstallments;
	}
	public void setNumberOfMonthlyInstallments(String numberOfMonthlyInstallments) {
		this.numberOfMonthlyInstallments = numberOfMonthlyInstallments;
	}
	public String getInstallmentName() {
		return installmentName;
	}
	public void setInstallmentName(String installmentName) {
		this.installmentName = installmentName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
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
	public String getPlanVersion() {
		return planVersion;
	}
	public void setPlanVersion(String planVersion) {
		this.planVersion = planVersion;
	}
}
