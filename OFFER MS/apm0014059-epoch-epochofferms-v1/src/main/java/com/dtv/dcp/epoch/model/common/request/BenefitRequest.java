package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BenefitRequest implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private List<String> benefitIds;
    private List<String> benefitCodes;
    private Account account;

	private String userType;
    private String state;

    private List<String> salesChannel;

    public List<String> getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(List<String> salesChannel) {
        this.salesChannel = salesChannel;
    }
    private List<String> productFamily;
    private List<Agreement> agreements;

	public List<Agreement> getAgreements() {
		return agreements;
	}

	public void setAgreements(List<Agreement> agreements) {
		this.agreements = agreements;
	}

	public List<String> getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(List<String> productFamily) {
		this.productFamily = productFamily;
	}

	public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<String> getBenefitIds() {
        return benefitIds;
    }

    public void setBenefitIds(List<String> benefitIds) {
        this.benefitIds = benefitIds;
    }

    public List<String> getBenefitCodes() {
        return benefitCodes;
    }

    public void setBenefitCodes(List<String> benefitCodes) {
        this.benefitCodes = benefitCodes;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BenefitRequest [benefitIds=");
        builder.append(benefitIds);
        builder.append(", benefitCodes=");
        builder.append(benefitCodes);
        builder.append(", account=");
        builder.append(account);
        builder.append(", userType=");
        builder.append(userType);
        builder.append(", state=");
        builder.append(state);
        builder.append(", salesChannel=");
        builder.append(salesChannel);
        builder.append(", productFamily=");
        builder.append(productFamily);
        builder.append(", agreements=");
        builder.append(agreements);
        builder.append("]");
        return builder.toString();
    }
}
