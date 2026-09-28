package com.dtv.ct.commercetool.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FieldContainer {

    Boolean recurrenceIndicator;
    String contractIndicator;
    String period;
    @JsonProperty("isGrandfathered")
    Boolean isGrandfathered;
    String proRateFrequency;
    String billingReferenceId;
    List<String> treatmentCode;
    List<String> creditRisk;
    List<String> priceTier;
    List<String> serviceSubscriptionType;
    List<String> conflictingPriceTier;
    List<String> attributePricingCriteria;

    public List<String> getAttributePricingCriteria() {
        return attributePricingCriteria;
    }

    public void setAttributePricingCriteria(List<String> attributePricingCriteria) {
        this.attributePricingCriteria = attributePricingCriteria;
    }

    public List<String> getConflictingPriceTier() {
        return conflictingPriceTier;
    }

    public void setConflictingPriceTier(List<String> conflictingPriceTier) {
        this.conflictingPriceTier = conflictingPriceTier;
    }

    public void setServiceSubscriptionType(List<String> serviceSubscriptionType) {
        this.serviceSubscriptionType = serviceSubscriptionType;
    }

    public List<String> getTreatmentCode() {
        return treatmentCode;
    }

    public void setTreatmentCode(List<String> treatmentCode) {
        this.treatmentCode = treatmentCode;
    }

    public List<String> getCreditRisk() {
        return creditRisk;
    }

    public void setCreditRisk(List<String> creditRisk) {
        this.creditRisk = creditRisk;
    }

    public Boolean getRecurrenceIndicator() {
        return recurrenceIndicator;
    }

    public void setRecurrenceIndicator(Boolean recurrenceIndicator) {
        this.recurrenceIndicator = recurrenceIndicator;
    }

    public String getContractIndicator() {
        return contractIndicator;
    }

    public void setContractIndicator(String contractIndicator) {
        this.contractIndicator = contractIndicator;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Boolean getGrandfathered() {
        return isGrandfathered;
    }

    public void setGrandfathered(Boolean grandfathered) {
        isGrandfathered = grandfathered;
    }

    public String getProRateFrequency() {
        return proRateFrequency;
    }

    public void setProRateFrequency(String proRateFrequency) {
        this.proRateFrequency = proRateFrequency;
    }

    public String getBillingReferenceId() {
        return billingReferenceId;
    }

    public void setBillingReferenceId(String billingReferenceId) {
        this.billingReferenceId = billingReferenceId;
    }

    public List<String> getPriceTier() {
        return priceTier;
    }

    public void setPriceTier(List<String> priceTier) {
        this.priceTier = priceTier;
    }

    public List<String> getServiceSubscriptionType() {
        return serviceSubscriptionType;
    }
}
