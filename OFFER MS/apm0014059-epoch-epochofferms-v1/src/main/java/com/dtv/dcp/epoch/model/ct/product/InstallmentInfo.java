package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.offer.DisclosureMessagesByKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
/**
 * The Class EquipmentInfo.
 */
@ApiModel(value = "InstallmentInfo")
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown=true)
public class InstallmentInfo implements Serializable{

	/** The serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The String installmentDescription */
	private String installmentDescription;

	/** The String numberOfMonthlyInstallments */
	private String numberOfMonthlyInstallments;

	/** The String installmentPlanVersion */
	private String installmentPlanVersion;

	/** The String installmentStartDate */
	private String installmentStartDate;

	/** The String installmentEndDate */
	private String installmentEndDate;

	/** The String installmentID */
	private String installmentId;
	
	/** The installmentProductName. */
	private String installmentProductName;
	
	/** The installmentProductName. */
	private List<String> contractApplicable;
	
	private List<InCompatibleProductValue> creditRiskEligibility;
	
	/** The installmentProvider. */
	private String installmentProvider;
	private DisclosureMessagesByKey disclosureMessagesByKey;
	
	public DisclosureMessagesByKey getDisclosureMessagesByKey()
	{
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) 
	{
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	/**
	 * @return the installmentDescription
	 */
	public String getInstallmentDescription() {
		return installmentDescription;
	}



	/**
	 * @param installmentDescription the installmentDescription to set
	 */
	public void setInstallmentDescription(String installmentDescription) {
		this.installmentDescription = installmentDescription;
	}



	/**
	 * @return the numberOfMonthlyInstallments
	 */
	public String getNumberOfMonthlyInstallments() {
		return numberOfMonthlyInstallments;
	}



	/**
	 * @param numberOfMonthlyInstallments the numberOfMonthlyInstallments to set
	 */
	public void setNumberOfMonthlyInstallments(String numberOfMonthlyInstallments) {
		this.numberOfMonthlyInstallments = numberOfMonthlyInstallments;
	}



	/**
	 * @return the installmentPlanVersion
	 */
	public String getInstallmentPlanVersion() {
		return installmentPlanVersion;
	}



	/**
	 * @param installmentPlanVersion the installmentPlanVersion to set
	 */
	public void setInstallmentPlanVersion(String installmentPlanVersion) {
		this.installmentPlanVersion = installmentPlanVersion;
	}



	/**
	 * @return the installmentStartDate
	 */
	public String getInstallmentStartDate() {
		return installmentStartDate;
	}



	/**
	 * @param installmentStartDate the installmentStartDate to set
	 */
	public void setInstallmentStartDate(String installmentStartDate) {
		this.installmentStartDate = installmentStartDate;
	}



	/**
	 * @return the installmentEndDate
	 */
	public String getInstallmentEndDate() {
		return installmentEndDate;
	}



	/**
	 * @param installmentEndDate the installmentEndDate to set
	 */
	public void setInstallmentEndDate(String installmentEndDate) {
		this.installmentEndDate = installmentEndDate;
	}

	/**
	 * @return the installmentId
	 */
	public String getInstallmentId() {
		return installmentId;
	}

	/**
	 * @param installmentId the installmentId to set
	 */
	public void setInstallmentId(String installmentId) {
		this.installmentId = installmentId;
	}

	public String getInstallmentProductName() {
		return installmentProductName;
	}

	public void setInstallmentProductName(String installmentProductName) {
		this.installmentProductName = installmentProductName;
	}

	public List<String> getContractApplicable() {
		return contractApplicable;
	}

	public void setContractApplicable(List<String> contractApplicable) {
		this.contractApplicable = contractApplicable;
	}

	public List<InCompatibleProductValue> getCreditRiskEligibility() {
		return creditRiskEligibility;
	}

	public void setCreditRiskEligibility(List<InCompatibleProductValue> creditRiskEligibility) {
		this.creditRiskEligibility = creditRiskEligibility;
	}

	public String getInstallmentProvider() {
		return installmentProvider;
	}

	public void setInstallmentProvider(String installmentProvider) {
		this.installmentProvider = installmentProvider;
	}



	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstallmentInfo [installmentDescription=");
		builder.append(installmentDescription);
		builder.append(", numberOfMonthlyInstallments=");
		builder.append(numberOfMonthlyInstallments);
		builder.append(", installmentPlanVersion=");
		builder.append(installmentPlanVersion);
		builder.append(", installmentStartDate=");
		builder.append(installmentStartDate);
		builder.append(", installmentEndDate=");
		builder.append(installmentEndDate);
		builder.append(", installmentId=");
		builder.append(installmentId);
		builder.append(", installmentProductName=");
		builder.append(installmentProductName);
		builder.append(", contractApplicable=");
		builder.append(contractApplicable);
		builder.append(", installmentProvider=");
		builder.append(installmentProvider);
		builder.append("]");
		return builder.toString();
	}
}
