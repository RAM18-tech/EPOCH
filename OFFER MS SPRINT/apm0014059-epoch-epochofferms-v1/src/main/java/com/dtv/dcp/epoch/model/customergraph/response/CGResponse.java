package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGResponse implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String accountId;
    private String status;
    private CGAccountInfo accountInfo;
    private CGContactInfo contactInfo;
    private CGBillingInfo billingInfo;
    private CGServiceInfo[] serviceInfo;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CGAccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(CGAccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public CGContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(CGContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public CGBillingInfo getBillingInfo() {
        return billingInfo;
    }

    public void setBillingInfo(CGBillingInfo billingInfo) {
        this.billingInfo = billingInfo;
    }

    public CGServiceInfo[] getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(CGServiceInfo[] serviceInfo) {
        this.serviceInfo = serviceInfo;
    }
}