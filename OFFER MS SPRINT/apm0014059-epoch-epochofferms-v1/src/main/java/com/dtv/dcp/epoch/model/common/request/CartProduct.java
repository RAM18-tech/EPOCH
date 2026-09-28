package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Benefit;
import com.dtv.dcp.epoch.model.ct.request.PriceProtection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartProduct implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    @JsonProperty("products")
    private List<ProductInfo> products;
    @JsonProperty("accountNumber")
    private String accountNumber;

    // IPTV/DTVS migration Attr
    private String sunsetDate;
    private boolean isActive;

    // OTT Specific Attributes
    private Boolean retentionPromotionIndicator;
    private String segmentDescription;
    private String coolOffPeriod;
    private Boolean freeTrialEligible;
    private Boolean isDefaultPromo;
    private String accountType;
    private String nextBillingDate;
    private String parentSubscriptionDate;
	private String propertyOwnerActNumber;
	private Boolean isProjectedBillDate;
	private String customerSubType;

	public String getCustomerSubType() {
		return customerSubType;
	}
	private Boolean pendingSwimlaneSwitch;

	public Boolean getPendingSwimlaneSwitch() {
		return pendingSwimlaneSwitch;
	}

	public void setPendingSwimlaneSwitch(Boolean pendingSwimlaneSwitch) {
		this.pendingSwimlaneSwitch = pendingSwimlaneSwitch;
	}

	public void setCustomerSubType(String customerSubType) {
		this.customerSubType = customerSubType;
	}
	
    public Boolean getIsProjectedBillDate() {
		return isProjectedBillDate;
	}

	public void setIsProjectedBillDate(Boolean isProjectedBillDate) {
		this.isProjectedBillDate = isProjectedBillDate;
	}
	public String getPropertyOwnerActNumber() {
		return propertyOwnerActNumber;
	}

	public void setPropertyOwnerActNumber(String propertyOwnerActNumber) {
		this.propertyOwnerActNumber = propertyOwnerActNumber;
	}
    
    public String getParentSubscriptionDate() 
    {
		return parentSubscriptionDate;
	}

	public void setParentSubscriptionDate(String parentSubscriptionDate) 
	{
		this.parentSubscriptionDate = parentSubscriptionDate;
	}

	/**
     * The replacementDevices.
     */
	/*
	 * @JsonProperty("replacementDevices") private List<String> replacementDevices;
	 */
    
    @JsonProperty("replacementDevices")
    private List<?> replacementDevices;
    
	public List<?> getReplacementDevices() {
		return replacementDevices;
	}

	public void setReplacementDevices(List<?> replacementDevices) {
		this.replacementDevices = replacementDevices;
	}

    /**
     * The Price protection.
     */
    private PriceProtection priceProtection;
    @JsonProperty("productFamily")
    private String productFamily;
    @JsonProperty("isContracted")
    private Boolean isContracted;
    @JsonProperty("benefits")
    private List<Benefit> benefits;
    @JsonProperty("accountStatus")
    private String accountStatus;
    
    private String sdgSocCode;
    
    private String tradeInEligibiltyReason;
    
    private String accountInFocus;
    
    private String groupIDInFocus;
    
    private String ctnInFocus;
    
	@JsonProperty("businessSegment")
	private String businessSegment;
	
	@JsonProperty("employeeSegment")
	private String employeeSegment;
	
	@JsonProperty("nftvFlag")
	private String nftvFlag;
	
	@JsonProperty("ftvCount")
	private String ftvCount;
	
	private String policy;
	
	private String dtvSwimlane;
	
	private String serviceOrderDate;
	
	private String remoteReplacementType;
	
	private String accountCreationDate;
	
	private String iapPartnerAccountType;
	
	private String dtvAccountType;
	
    @JsonProperty("existingPromotions")
	private List<ExistingPromotion> existingPromotions;
    
    private Integer dynamicUpgrade;
    
    private String partnerName;
    
    private ServiceAddress serviceAddress;
    
    private String customerValue;
    private Boolean acceptPaperlessBillEnrollment;
    private Boolean acceptAutoBillPayEnrollment;
    private String activationDate;
    private String commissionStartDate;
    
   	public String getDtvSwimlane() {
		return dtvSwimlane;
	}

	public void setDtvSwimlane(String dtvSwimlane) {
		this.dtvSwimlane = dtvSwimlane;
	}

   	/**
	 * @return the customerValue
	 */
	public String getCustomerValue() {
		return customerValue;
	}

	/**
	 * @param customerValue the customerValue to set
	 */
	public void setCustomerValue(String customerValue) {
		this.customerValue = customerValue;
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
	public String getActivationDate() {
		return activationDate;
	}

	/**
	 * @param activationDate the activationDate to set
	 */
	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	/**
	 * @return the commissionStartDate
	 */
	public String getCommissionStartDate() {
		return commissionStartDate;
	}

	/**
	 * @param commissionStartDate the commissionStartDate to set
	 */
	public void setCommissionStartDate(String commissionStartDate) {
		this.commissionStartDate = commissionStartDate;
	}
    
   	public ServiceAddress getServiceAddress() {
   		return serviceAddress;
   	}

   	public void setServiceAddress(ServiceAddress serviceAddress) {
   		this.serviceAddress = serviceAddress;
   	}
    
	public String getPartnerName() {
		return partnerName;
	}

	public void setPartnerName(String partnerName) {
		this.partnerName = partnerName;
	}

	public Integer getDynamicUpgrade() {
		return dynamicUpgrade;
	}

	public void setDynamicUpgrade(Integer dynamicUpgrade) {
		this.dynamicUpgrade = dynamicUpgrade;
	}
	
    public List<ExistingPromotion> getExistingPromotions() {
		return existingPromotions;
	}

	public void setExistingPromotions(List<ExistingPromotion> existingPromotions) {
		this.existingPromotions = existingPromotions;
	}
	
    public String getDtvAccountType() {
		return dtvAccountType;
	}

	public void setDtvAccountType(String dtvAccountType) {
		this.dtvAccountType = dtvAccountType;
	}
	
    public String getAccountCreationDate() {
		return accountCreationDate;
	}

	public void setAccountCreationDate(String accountCreationDate) {
		this.accountCreationDate = accountCreationDate;
	}
	
    public String getRemoteReplacementType() {
		return remoteReplacementType;
	}

	public void setRemoteReplacementType(String remoteReplacementType) {
		this.remoteReplacementType = remoteReplacementType;
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}

	public String getServiceOrderDate() {
		return serviceOrderDate;
	}

	public void setServiceOrderDate(String serviceOrderDate) {
		this.serviceOrderDate = serviceOrderDate;
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

	public String getSdgSocCode() {
		return sdgSocCode;
	}

	public void setSdgSocCode(String sdgSocCode) {
		this.sdgSocCode = sdgSocCode;
	}

	public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(String nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
    
	/*
	 * public List<String> getReplacementDevices() { return replacementDevices; }
	 * 
	 * public void setReplacementDevices(List<String> replacementDevices) {
	 * this.replacementDevices = replacementDevices; }
	 */


    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Boolean getIsActive() {
        return isActive();
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsDefaultPromo() {
        return isDefaultPromo;
    }

    public void setIsDefaultPromo(Boolean isDefaultPromo) {
        this.isDefaultPromo = isDefaultPromo;
    }

    public List<ProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<ProductInfo> products) {
        this.products = products;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSunsetDate() {
        return sunsetDate;
    }

    public void setSunsetDate(String sunsetDate) {
        this.sunsetDate = sunsetDate;
    }

    public Boolean getRetentionPromotionIndicator() {
        return retentionPromotionIndicator;
    }

    public void setRetentionPromotionIndicator(Boolean retentionPromotionIndicator) {
        this.retentionPromotionIndicator = retentionPromotionIndicator;
    }

    public String getSegmentDescription() {
        return segmentDescription;
    }

    public void setSegmentDescription(String segmentDescription) {
        this.segmentDescription = segmentDescription;
    }

    public String getCoolOffPeriod() {
        return coolOffPeriod;
    }

    public void setCoolOffPeriod(String coolOffPeriod) {
        this.coolOffPeriod = coolOffPeriod;
    }

    public Boolean getFreeTrialEligible() {
        return freeTrialEligible;
    }

    public void setFreeTrialEligible(Boolean freeTrialEligible) {
        this.freeTrialEligible = freeTrialEligible;
    }

    public Boolean getDefaultPromo() {
        return getIsDefaultPromo();
    }

    public void setDefaultPromo(Boolean defaultPromo) {
        isDefaultPromo = defaultPromo;
    }

    public String getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	public Boolean getIsContracted() {
		return isContracted;
	}

	public void setIsContracted(Boolean isContracted) {
		this.isContracted = isContracted;
	}

	public List<Benefit> getBenefits() {
		return benefits;
	}

	public void setBenefits(List<Benefit> benefits) {
		this.benefits = benefits;
	}

	public String getAccountStatus() {
		return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
		this.accountStatus = accountStatus;
	}

	@Override
    public String toString() {
        return "CartProduct{" +
                "products:" + products +
                ", accountNumber:'" + accountNumber + '\'' +
                ", sunsetDate:'" + sunsetDate + '\'' +
                ", isActive:" + isActive +
                ", retentionPromotionIndicator:" + retentionPromotionIndicator +
                ", segmentDescription:'" + segmentDescription + '\'' +
                ", coolOffPeriod:'" + coolOffPeriod + '\'' +
                ", freeTrialEligible:" + freeTrialEligible +
                ", isDefaultPromo:" + isDefaultPromo +
                ", accountType:'" + accountType + '\'' +
                ", nextBillingDate:'" + nextBillingDate + '\'' +
                ", productFamily:'" + productFamily + '\'' +
                ", isContracted:'" + isContracted + '\'' +
                ", benefits:'" + benefits + '\'' +
                ", accountStatus:'" + accountStatus + '\'' +
                ", remoteReplacementType:'" + remoteReplacementType + '\'' +
                ", dynamicUpgrade:'" + dynamicUpgrade + '\'' +
                ", customerValue:'" + customerValue + '\'' +
                ", acceptPaperlessBillEnrollment:'" + acceptPaperlessBillEnrollment + '\'' +
                ", acceptAutoBillPayEnrollment:'" + acceptAutoBillPayEnrollment + '\'' +
                ", activationDate:'" + activationDate + '\'' +
                ", commissionStartDate:'" + commissionStartDate + '\'' +
                '}';
    }

	public String getTradeInEligibiltyReason() {
		return tradeInEligibiltyReason;
	}

	public void setTradeInEligibiltyReason(String tradeInEligibiltyReason) {
		this.tradeInEligibiltyReason = tradeInEligibiltyReason;
	}

	public String getAccountInFocus() {
		return accountInFocus;
	}

	public void setAccountInFocus(String accountInFocus) {
		this.accountInFocus = accountInFocus;
	}

	public String getGroupIDInFocus() {
		return groupIDInFocus;
	}

	public void setGroupIDInFocus(String groupIDInFocus) {
		this.groupIDInFocus = groupIDInFocus;
	}

	public String getCtnInFocus() {
		return ctnInFocus;
	}

	public void setCtnInFocus(String ctnInFocus) {
		this.ctnInFocus = ctnInFocus;
	}

	/**
	 * @return the priceProtection
	 */
	public PriceProtection getPriceProtection() {
		return priceProtection;
	}

	/**
	 * @param priceProtection the priceProtection to set
	 */
	public void setPriceProtection(PriceProtection priceProtection) {
		this.priceProtection = priceProtection;
	}

	public String getIapPartnerAccountType() {
		return iapPartnerAccountType;
	}

	public void setIapPartnerAccountType(String iapPartnerAccountType) {
		this.iapPartnerAccountType = iapPartnerAccountType;
	}

	private String subscriptionStatus;

	private String tenureType;
	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public void setSubscriptionStatus(String subscriptionStatus) {
		this.subscriptionStatus = subscriptionStatus;
	}

	public String getTenureType() {
		return tenureType;
	}

	public void setTenureType(String tenureType) {
		this.tenureType = tenureType;
	}
}
