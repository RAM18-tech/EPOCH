package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGAccountInfo implements Serializable  {
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private  String accountType;
    private  String accountStatus;
    private  boolean verificationStatus;
    private  String spAccountId;
    private  String accountCreationDate;
    private  String cpCustomerId;
    private  boolean isIOIndicator;
    private  boolean isPremiumCustomer;
    private  String isEligibleForFreeTrial;

    /**
	 * @return the isEligibleForFreeTrial
	 */
	public String getIsEligibleForFreeTrial() {
		
		return isEligibleForFreeTrial;
	}

	/**
	 * @param isEligibleForFreeTrial the isEligibleForFreeTrial to set
	 */
	public void setIsEligibleForFreeTrial(String isEligibleForFreeTrial) {
		this.isEligibleForFreeTrial = isEligibleForFreeTrial;
	}

	public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public boolean isVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(boolean verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getSpAccountId() {
        return spAccountId;
    }

    public void setSpAccountId(String spAccountId) {
        this.spAccountId = spAccountId;
    }

    public String getAccountCreationDate() {
        return accountCreationDate;
    }

    public void setAccountCreationDate(String accountCreationDate) {
        this.accountCreationDate = accountCreationDate;
    }

    public String getCpCustomerId() {
        return cpCustomerId;
    }

    public void setCpCustomerId(String cpCustomerId) {
        this.cpCustomerId = cpCustomerId;
    }

    public boolean isIOIndicator() {
        return isIOIndicator;
    }

    public void setIOIndicator(boolean IOIndicator) {
        isIOIndicator = IOIndicator;
    }

    public boolean isIsPremiumCustomer() {
        return isPremiumCustomer;
    }

    public void setPremiumCustomer(boolean premiumCustomer) {
        isPremiumCustomer = premiumCustomer;
    }
}
