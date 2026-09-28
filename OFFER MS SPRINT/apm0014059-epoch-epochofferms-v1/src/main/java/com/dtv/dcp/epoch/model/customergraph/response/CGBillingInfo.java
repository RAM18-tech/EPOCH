package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CGBillingInfo  implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    private String nextBillingDateTime;
    private String nextBillingAmount;

    public String getNextBillingDateTime() {
        return nextBillingDateTime;
    }

    public void setNextBillingDateTime(String nextBillingDateTime) {
        this.nextBillingDateTime = nextBillingDateTime;
    }

    public String getNextBillingAmount() {
        return nextBillingAmount;
    }

    public void setNextBillingAmount(String nextBillingAmount) {
        this.nextBillingAmount = nextBillingAmount;
    }
}
