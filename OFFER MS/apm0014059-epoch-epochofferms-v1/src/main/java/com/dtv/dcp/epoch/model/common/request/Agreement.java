package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class Agreement implements Serializable {
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    private String agreementId;
    private String agreementPriceCode;
    
	public String getAgreementId() {
		return agreementId;
	}
	
	public void setAgreementId(String agreementId) {
		this.agreementId = agreementId;
	}
	
	public String getAgreementPriceCode() {
		return agreementPriceCode;
	}
	
	public void setAgreementPriceCode(String agreementPriceCode) {
		this.agreementPriceCode = agreementPriceCode;
	}
	
	@Override
	public String toString() {
		return "Agreement [agreementId=" + agreementId + ", agreementPriceCode=" + agreementPriceCode + "]";
	}

}
