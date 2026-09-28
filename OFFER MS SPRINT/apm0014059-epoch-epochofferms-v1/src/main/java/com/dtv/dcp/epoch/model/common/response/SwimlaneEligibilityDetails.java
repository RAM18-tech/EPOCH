package com.dtv.dcp.epoch.model.common.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SwimlaneEligibilityDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean swimlaneSwitchEligible;
    private List<String> slsEligibleSubscriptionType;
    private String slsEligibleSubscriberType;
    private String slsIneligibleReasonMsg;
    private String slsIneligibleReasonCode;

    public boolean isSwimlaneSwitchEligible() {
        return swimlaneSwitchEligible;
    }

    public void setSwimlaneSwitchEligible(boolean swimlaneSwitchEligible) {
        this.swimlaneSwitchEligible = swimlaneSwitchEligible;
    }

    public List<String> getSlsEligibleSubscriptionType() {
        return slsEligibleSubscriptionType;
    }

    public void setSlsEligibleSubscriptionType(List<String> slsEligibleSubscriptionType) {
        this.slsEligibleSubscriptionType = slsEligibleSubscriptionType;
    }

    public String getSlsEligibleSubscriberType() {
        return slsEligibleSubscriberType;
    }

    public void setSlsEligibleSubscriberType(String slsEligibleSubscriberType) {
        this.slsEligibleSubscriberType = slsEligibleSubscriberType;
    }

    public String getSlsIneligibleReasonMsg() {
        return slsIneligibleReasonMsg;
    }

    public void setSlsIneligibleReasonMsg(String slsIneligibleReasonMsg) {
        this.slsIneligibleReasonMsg = slsIneligibleReasonMsg;
    }

    public String getSlsIneligibleReasonCode() {
        return slsIneligibleReasonCode;
    }

    public void setSlsIneligibleReasonCode(String slsIneligibleReasonCode) {
        this.slsIneligibleReasonCode = slsIneligibleReasonCode;
    }

}
