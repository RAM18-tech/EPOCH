package com.dtv.dcp.epoch.model.eligibility;

import java.util.List;
import java.util.Map;


public class EligibilityErrorMessage {

	
    private List<Map<String,List<String>>> eligibilityErrorMessage;

    public List<Map<String,List<String>>> getEligibilityErrorMessage() {
        return eligibilityErrorMessage;
    }

    public void setEligibilityErrorMessage(List<Map<String,List<String>>> eligibilityErrorMessage) {
        this.eligibilityErrorMessage = eligibilityErrorMessage;
    }
}


