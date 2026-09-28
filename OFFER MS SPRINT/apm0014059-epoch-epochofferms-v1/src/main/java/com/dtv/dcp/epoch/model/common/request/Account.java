package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class Account implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    private String accountType;
    private boolean contractIndicator;
    private String contractVersion;
    private String zipCode;

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public boolean isContractIndicator() {
        return contractIndicator;
    }

    public void setContractIndicator(boolean contractIndicator) {
        this.contractIndicator = contractIndicator;
    }

    public String getContractVersion() {
        return contractVersion;
    }

    public void setContractVersion(String contractVersion) {
        this.contractVersion = contractVersion;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Account [accountType=");
        builder.append(accountType);
        builder.append(", contractIndicator=");
        builder.append(contractIndicator);
        builder.append(", contractVersion=");
        builder.append(contractVersion);
        builder.append(", zipCode=");
        builder.append(zipCode);
        builder.append("]");
        return builder.toString();
    }
}
