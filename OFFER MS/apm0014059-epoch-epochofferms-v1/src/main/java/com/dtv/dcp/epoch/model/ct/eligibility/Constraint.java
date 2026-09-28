package com.dtv.dcp.epoch.model.ct.eligibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Constraint implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The accountType. */
	private String accountType;

	/** The customerSegment */
	private List<String> customerSegments;

	private String customerSegment;

	/** The minimumPurchaseAmount. */
	private BigDecimal minimumPurchaseAmount;

	/** The customerType. */
	@JsonProperty("existingCustomerType")
	private List<String> customerType;

	/** The salesChannel. */
	private List<String> salesChannel;

	/** The zip. */
	private List<String> zip;

	private List<String> heartValue;

	private List<String> businessSegment;
	
	private String eligibleAccountStatus;
	private String creditSchedule;
    private Boolean acceptPaperlessBillEnrollment;
    private Boolean acceptAutoBillPayEnrollment;
    private Integer activationDate;
    private Integer commissionStartDate;
    
    @JsonProperty("MSC")
    private List<String> msc;
    
    private String eligibleAccountTypes;
    
    private List<String> dma;
    
    public List<String> getDma() {
		return dma;
	}

	public void setDma(List<String> dma) {
		this.dma = dma;
	}
    
    /**
	 * @return the eligibleAccountTypes
	 */
	public String getEligibleAccountTypes() {
		return eligibleAccountTypes;
	}

	/**
	 * @param eligibleAccountTypes the eligibleAccountTypes to set
	 */
	public void setEligibleAccountTypes(String eligibleAccountTypes) {
		this.eligibleAccountTypes = eligibleAccountTypes;
	}

	public List<String> getMsc() {
		return msc;
	}

	public void setMsc(List<String> msc) {
		this.msc = msc;
	}

	/**
	 * @return the eligibleAccountStatus
	 */
	public String getEligibleAccountStatus() {
		return eligibleAccountStatus;
	}

	/**
	 * @param eligibleAccountStatus the eligibleAccountStatus to set
	 */
	public void setEligibleAccountStatus(String eligibleAccountStatus) {
		this.eligibleAccountStatus = eligibleAccountStatus;
	}

	/**
	 * @return the creditSchedule
	 */
	public String getCreditSchedule() {
		return creditSchedule;
	}

	/**
	 * @param creditSchedule the creditSchedule to set
	 */
	public void setCreditSchedule(String creditSchedule) {
		this.creditSchedule = creditSchedule;
	}

	/**
	 * @return the acceptPaperlessBillEnrollment
	 */
	public Boolean getAcceptPaperlessBillEnrollment() {
		return acceptPaperlessBillEnrollment;
	}

	/**
	 * @param acceptPaperlessBillEnrollment the acceptPaperlessBillEnrollment to set
	 */
	public void setAcceptPaperlessBillEnrollment(Boolean acceptPaperlessBillEnrollment) {
		this.acceptPaperlessBillEnrollment = acceptPaperlessBillEnrollment;
	}

	/**
	 * @return the acceptAutoBillPayEnrollment
	 */
	public Boolean getAcceptAutoBillPayEnrollment() {
		return acceptAutoBillPayEnrollment;
	}

	/**
	 * @param acceptAutoBillPayEnrollment the acceptAutoBillPayEnrollment to set
	 */
	public void setAcceptAutoBillPayEnrollment(Boolean acceptAutoBillPayEnrollment) {
		this.acceptAutoBillPayEnrollment = acceptAutoBillPayEnrollment;
	}

	/**
	 * @return the activationDate
	 */
	public Integer getActivationDate() {
		return activationDate;
	}

	/**
	 * @param activationDate the activationDate to set
	 */
	public void setActivationDate(Integer activationDate) {
		this.activationDate = activationDate;
	}

	/**
	 * @return the commissionStartDate
	 */
	public Integer getCommissionStartDate() {
		return commissionStartDate;
	}

	/**
	 * @param commissionStartDate the commissionStartDate to set
	 */
	public void setCommissionStartDate(Integer commissionStartDate) {
		this.commissionStartDate = commissionStartDate;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public List<String> getHeartValue() {
		return heartValue;
	}

	public void setHeartValue(List<String> heartValue) {
		this.heartValue = heartValue;
	}

	/**
	 * @return the accountType
	 */
	public String getAccountType() {
		return accountType;
	}

	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	/**
	 * @return the minimumPurchaseAmount
	 */
	public BigDecimal getMinimumPurchaseAmount() {
		return minimumPurchaseAmount;
	}

	/**
	 * @param minimumPurchaseAmount the minimumPurchaseAmount to set
	 */
	public void setMinimumPurchaseAmount(BigDecimal minimumPurchaseAmount) {
		this.minimumPurchaseAmount = minimumPurchaseAmount;
	}

	/**
	 * @return the customerType
	 */
	public List<String> getCustomerType() {
		return customerType;
	}

	/**
	 * @param customerType the customerType to set
	 */
	public void setCustomerType(List<String> customerType) {
		this.customerType = customerType;
	}

	/**
	 * @return the salesChannel
	 */
	public List<String> getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel the salesChannel to set
	 */
	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	/**
	 * @return the zip
	 */
	public List<String> getZip() {
		return zip;
	}

	/**
	 * @param zip the zip to set
	 */
	public void setZip(List<String> zip) {
		this.zip = zip;
	}

	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	public String getCustomerSegment() {
		return customerSegment;
	}

	public void setCustomerSegment(String customerSegment) {
		this.customerSegment = customerSegment;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Constraint [accountType=");
		builder.append(accountType);
		builder.append(", customerSegment=");
		builder.append(customerSegment);
		builder.append(", minimumPurchaseAmount=");
		builder.append(minimumPurchaseAmount);
		builder.append(", customerType=");
		builder.append(customerType);
		builder.append(", salesChannel=");
		builder.append(salesChannel);
		builder.append(", zip=");
		builder.append(zip);
		builder.append(", heartValue=");
		builder.append(heartValue);
		builder.append(", eligibleAccountStatus=");
		builder.append(eligibleAccountStatus);
		builder.append(", creditSchedule=");
		builder.append(creditSchedule);
		builder.append(", acceptPaperlessBillEnrollment=");
		builder.append(acceptPaperlessBillEnrollment);
		builder.append(", acceptAutoBillPayEnrollment=");
		builder.append(acceptAutoBillPayEnrollment);
		builder.append(", activationDate=");
		builder.append(activationDate);
		builder.append(", commissionStartDate=");
		builder.append(commissionStartDate);
		builder.append(", eligibleAccountTypes=");
		builder.append(eligibleAccountTypes);
		builder.append("]");
		return builder.toString();
	}

}
