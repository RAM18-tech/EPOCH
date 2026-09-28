package com.dtv.dcp.epoch.model.ct.offer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalEligibility implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String eligibleProductType;
    private String minCount;
    private String maxCount;
    private String eligibilityType;
    private List<String> offerIds;

    private List<String> productIds;

    private List<String> priorityOffers;

    public List<String> getPriorityOffers() {
        return priorityOffers;
    }

    public void setPriorityOffers(List<String> priorityOffers) {
        this.priorityOffers = priorityOffers;
    }

    public String getMinCount() {
        return minCount;
    }

    public void setMinCount(String minCount) {
        this.minCount = minCount;
    }

    public String getMaxCount() {
        return maxCount;
    }

    public void setMaxCount(String maxCount) {
        this.maxCount = maxCount;
    }

    public String getEligibilityType() {
        return eligibilityType;
    }

    public void setEligibilityType(String eligibilityType) {
        this.eligibilityType = eligibilityType;
    }

    public List<String> getOfferIds() {
        return offerIds;
    }

    public void setOfferIds(List<String> offerIds) {
        this.offerIds = offerIds;
    }

    public String getEligibleProductType() {
        return eligibleProductType;
    }

    public void setEligibleProductType(String eligibleProductType) {
        this.eligibleProductType = eligibleProductType;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }
}