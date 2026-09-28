package com.dtv.ct.commercetool.model;

import java.util.List;

public class BenefitUpdateRequest {


    List<String> offerCodes;
    String benefitCode;

    String attributeName;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getBenefitCode() {
        return benefitCode;
    }


    public void setBenefitCode(String benefitCode) {
        this.benefitCode = benefitCode;
    }

    public List<String> getOfferCodes() {
        return offerCodes;
    }

    public void setOfferCodes(List<String> offerCodes) {
        this.offerCodes = offerCodes;
    }


}
