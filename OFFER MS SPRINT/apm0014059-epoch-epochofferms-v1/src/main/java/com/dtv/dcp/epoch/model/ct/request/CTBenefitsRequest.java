package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.request.Account;

public class CTBenefitsRequest implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private List <String> benefitCodes;

    private List <String> benefitIds;
    private List <String> applicableProductCodes;
    private Account account;

    private String userType;
    private List <String> salesChannel;

    public List<String> getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(List<String> salesChannel) {
        this.salesChannel = salesChannel;
    }

    private boolean ioOffer;

    /** The pagination. */
    private Pagination pagination;

    private String state;
    
    private List<String> agreementIds;

    public List<String> getAgreementIds() {
		return agreementIds;
	}

	public void setAgreementIds(List<String> agreementIds) {
		this.agreementIds = agreementIds;
	}

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<String> getBenefitCodes() {
        return benefitCodes;
    }

    public void setBenefitCodes(List<String> benefitCodes) {
        this.benefitCodes = benefitCodes;
    }

    public List<String> getBenefitIds() {
        return benefitIds;
    }

    public void setBenefitIds(List<String> benefitIds) {
        this.benefitIds = benefitIds;
    }

    public List<String> getApplicableProductCodes() {
        return applicableProductCodes;
    }

    public void setApplicableProductCodes(List<String> applicableProductCodes) {
        this.applicableProductCodes = applicableProductCodes;
    }

    public boolean isIoOffer() {
        return ioOffer;
    }

    public void setIoOffer(boolean ioOffer) {
        this.ioOffer = ioOffer;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("CTBenefitRequest [benefitIds=");
        builder.append(benefitIds);
        builder.append(", benefitCodes=");
        builder.append(benefitCodes);
        builder.append(", salesChannel=");
        builder.append(salesChannel);
        builder.append(", account=");
        builder.append(account);
        builder.append(", userType=");
        builder.append(userType);
        builder.append(", state=");
        builder.append(state);
        builder.append(", agreementIds=");
        builder.append(agreementIds);
        builder.append("]");
        return builder.toString();
    }
}
