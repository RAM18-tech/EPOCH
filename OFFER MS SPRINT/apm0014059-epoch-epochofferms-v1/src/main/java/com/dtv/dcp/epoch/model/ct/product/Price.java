package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Price implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The value. */
	private Value  value;
	
	/** The id. */
	private String id;
	
	/** The startDate. */
	private String startDate ;
	
	/** The endDate. */
	private String endDate ;
	
	/** The recurrenceIndicator. */
	private boolean recurrenceIndicator ;
	
	/** The isContracted. */
	private boolean isContracted ;
	
	/** The duration. */
	private int duration ;
	
	/** The period. */
	private String period ;
	
	/** The proRateFrequency. */
	private String proRateFrequency ;
	
	/** The isGrandfathered. */
	private boolean isGrandfathered ;
	
	/** The contractIndicator. */
	private String contractIndicator ;
	
	private String billingReferenceId;
	
	private int gfVersion;
	
	private String customerSubType;
	
	private String customerGroup;
	
	private String premiumTier;
	
	private String freeDevicePolicyID;
	
	private String fromRange;
	
	private Value preSeasonValue;
	
	private Value fullSeasonValue;
	
	private Value midSeasonValue;
	
	private Value postSeasonValue;
	
	private String businessSegment;
	
	private String employeeSegment;
	
	private String nftvFlag;
	
	private String ftvCount;
	
	private String chargeType;
	
	private Integer grandfatherVersion;
	
	private String omsFreeSTBPolicyId;
	
	private Integer numberOfPayments;
	
    private String omsProductId;
    
    private String channel;
    
    private List<Tier> tiers;
    
    private List<String> treatmentCode;
    
    private List<String> creditRisk;
    
    private GenericLocaleBase priceDescription;
    
    private String rtpRtcIndicator;

    private Boolean isAutoRenewable;
    
    private Boolean isRemovable;
    
    private Boolean isVisible;
    
    private Double planPricewithLocals;
   
	private List<String> priceTier;
	
	private String subCategory;

	private List<String> serviceSubscriptionType;

	public List<String> getServiceSubscriptionType() {
		return serviceSubscriptionType;
	}
	public void setServiceSubscriptionType(List<String> serviceSubscriptionType) {
		this.serviceSubscriptionType = serviceSubscriptionType;
	}
	
	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}
	
	public List<String> getPriceTier() {
		return priceTier;
	}
	public void setPriceTier(List<String> priceTier) {
		this.priceTier = priceTier;
	}
	
	public Double getPlanPricewithLocals() {
		return planPricewithLocals;
	}

	public void setPlanPricewithLocals(Double planPricewithLocals) {
		this.planPricewithLocals = planPricewithLocals;
	}

	public String getRtpRtcIndicator() {
		return rtpRtcIndicator;
	}

	public void setRtpRtcIndicator(String rtpRtcIndicator) {
		this.rtpRtcIndicator = rtpRtcIndicator;
	}

	public Boolean isIsRemovable() {
		return isRemovable;
	}

	public void setIsRemovable(Boolean isRemovable) {
		this.isRemovable = isRemovable;
	}

	public Boolean isIsAutoRenewable() {
		return isAutoRenewable;
	}

	public void setIsAutoRenewable(Boolean isAutoRenewable) {
		this.isAutoRenewable = isAutoRenewable;
	}
	
	public Boolean isIsVisible() {
		return isVisible;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}
    
	public GenericLocaleBase getPriceDescription() {
		return priceDescription;
	}

	public void setPriceDescription(GenericLocaleBase priceDescription) {
		this.priceDescription = priceDescription;
	}

	/**
	 * @return the tiers
	 */
	public List<Tier> getTiers() {
		return tiers;
	}

	/**
	 * @param tiers the tiers to set
	 */
	public void setTiers(List<Tier> tiers) {
		this.tiers = tiers;
	}
    
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getOmsProductId() {
		return omsProductId;
	}

	public void setOmsProductId(String omsProductId) {
		this.omsProductId = omsProductId;
	}
	
	public String getChargeType() {
		return chargeType;
	}

	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	public Integer getGrandfatherVersion() {
		return grandfatherVersion;
	}

	public void setGrandfatherVersion(Integer grandfatherVersion) {
		this.grandfatherVersion = grandfatherVersion;
	}

	public String getOmsFreeSTBPolicyId() {
		return omsFreeSTBPolicyId;
	}

	public void setOmsFreeSTBPolicyId(String omsFreeSTBPolicyId) {
		this.omsFreeSTBPolicyId = omsFreeSTBPolicyId;
	}

	public String getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}
	
	public String getEmployeeSegment() {
		return employeeSegment;
	}

	public void setEmployeeSegment(String employeeSegment) {
		this.employeeSegment = employeeSegment;
	}

	public String getNftvFlag() {
		return nftvFlag;
	}

	public void setNftvFlag(String nftvFlag) {
		this.nftvFlag = nftvFlag;
	}

	public String getFtvCount() {
		return ftvCount;
	}

	public void setFtvCount(String ftvCount) {
		this.ftvCount = ftvCount;
	}
	
	public Integer getNumberOfPayments() {
		return numberOfPayments;
	}

	public void setNumberOfPayments(Integer numberOfPayments) {
		this.numberOfPayments = numberOfPayments;
	}

	public String getBillingReferenceId() {
		return billingReferenceId;
	}

	public void setBillingReferenceId(String billingReferenceId) {
		this.billingReferenceId = billingReferenceId;
	}

	public int getGfVersion() {
		return gfVersion;
	}

	public void setGfVersion(int gfVersion) {
		this.gfVersion = gfVersion;
	}

	public String getCustomerSubType() {
		return customerSubType;
	}

	public void setCustomerSubType(String customerSubType) {
		this.customerSubType = customerSubType;
	}

	public String getPremiumTier() {
		return premiumTier;
	}

	public void setPremiumTier(String premiumTier) {
		this.premiumTier = premiumTier;
	}

	public String getFreeDevicePolicyID() {
		return freeDevicePolicyID;
	}

	public void setFreeDevicePolicyID(String freeDevicePolicyID) {
		this.freeDevicePolicyID = freeDevicePolicyID;
	}

	public String getFromRange() {
		return fromRange;
	}

	public void setFromRange(String fromRange) {
		this.fromRange = fromRange;
	}

	public Value getPreSeasonValue() {
		return preSeasonValue;
	}

	public void setPreSeasonValue(Value preSeasonValue) {
		this.preSeasonValue = preSeasonValue;
	}

	public Value getFullSeasonValue() {
		return fullSeasonValue;
	}

	public void setFullSeasonValue(Value fullSeasonValue) {
		this.fullSeasonValue = fullSeasonValue;
	}

	public Value getMidSeasonValue() {
		return midSeasonValue;
	}

	public void setMidSeasonValue(Value midSeasonValue) {
		this.midSeasonValue = midSeasonValue;
	}

	public Value getPostSeasonValue() {
		return postSeasonValue;
	}

	public void setPostSeasonValue(Value postSeasonValue) {
		this.postSeasonValue = postSeasonValue;
	}

	/**
	 * @return the contractIndicator
	 */
	public String getContractIndicator() {
		return contractIndicator;
	}

	/**
	 * @param contractIndicator the contractIndicator to set
	 */
	public void setContractIndicator(String contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	/**
	 * @return the value
	 */
	public Value getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Value value) {
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the recurrenceIndicator
	 */
	public boolean isRecurrenceIndicator() {
		return recurrenceIndicator;
	}

	/**
	 * @param recurrenceIndicator the recurrenceIndicator to set
	 */
	public void setRecurrenceIndicator(boolean recurrenceIndicator) {
		this.recurrenceIndicator = recurrenceIndicator;
	}

	/**
	 * @return the isContracted
	 */
	public boolean isContracted() {
		return isContracted;
	}

	/**
	 * @param isContracted the isContracted to set
	 */
	public void setContracted(boolean isContracted) {
		this.isContracted = isContracted;
	}

	/**
	 * @return the duration
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @return the period
	 */
	public String getPeriod() {
		return period;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(String period) {
		this.period = period;
	}

	/**
	 * @return the proRateFrequency
	 */
	public String getProRateFrequency() {
		return proRateFrequency;
	}

	/**
	 * @param proRateFrequency the proRateFrequency to set
	 */
	public void setProRateFrequency(String proRateFrequency) {
		this.proRateFrequency = proRateFrequency;
	}

	/**
	 * @return the isGrandfathered
	 */
	public boolean isGrandfathered() {
		return isGrandfathered;
	}

	/**
	 * @param isGrandfathered the isGrandfathered to set
	 */
	public void setGrandfathered(boolean isGrandfathered) {
		this.isGrandfathered = isGrandfathered;
	}

	public String getCustomerGroup() {
		return customerGroup;
	}

	public void setCustomerGroup(String customerGroup) {
		this.customerGroup = customerGroup;
	}

	public List<String> getTreatmentCode() {
		return treatmentCode;
	}

	public void setTreatmentCode(List<String> treatmentCode) {
		this.treatmentCode = treatmentCode;
	}

	public List<String> getCreditRisk() {
		return creditRisk;
	}

	public void setCreditRisk(List<String> creditRisk) {
		this.creditRisk = creditRisk;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Price [value=");
		builder.append(value);
		builder.append(", id=");
		builder.append(id);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", recurrenceIndicator=");
		builder.append(recurrenceIndicator);
		builder.append(", isContracted=");
		builder.append(isContracted);
		builder.append(", duration=");
		builder.append(duration);
		builder.append(", period=");
		builder.append(period);
		builder.append(", proRateFrequency=");
		builder.append(proRateFrequency);
		builder.append(", isGrandfathered=");
		builder.append(isGrandfathered);
		builder.append(", businessSegment=");
		builder.append(businessSegment);
		builder.append(", employeeSegment=");
		builder.append(employeeSegment);
		builder.append(", nftvFlag=");
		builder.append(nftvFlag);
		builder.append(", ftvCount=");
		builder.append(ftvCount);
		builder.append(", numberOfPayments=");
		builder.append(numberOfPayments);
		builder.append(", chargeType=");
		builder.append(chargeType);
		builder.append(", grandfatherVersion=");
		builder.append(grandfatherVersion);
		builder.append(", omsFreeSTBPolicyId=");
		builder.append(omsFreeSTBPolicyId);
		builder.append(", tiers=");
		builder.append(tiers);
		builder.append("]");
		return builder.toString();
	}

	private List<String> attributePricingCriteria;
	private List<String> conflictingPriceTiers;

	public List<String> getAttributePricingCriteria() {
		return attributePricingCriteria;
	}

	public void setAttributePricingCriteria(List<String> attributePricingCriteria) {
		this.attributePricingCriteria = attributePricingCriteria;
	}

	public List<String> getConflictingPriceTiers() {
		return conflictingPriceTiers;
	}

	public void setConflictingPriceTiers(List<String> conflictingPriceTiers) {
		this.conflictingPriceTiers = conflictingPriceTiers;
	}

}
