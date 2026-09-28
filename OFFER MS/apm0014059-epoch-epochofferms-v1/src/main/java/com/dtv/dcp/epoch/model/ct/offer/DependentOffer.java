package com.dtv.dcp.epoch.model.ct.offer;

import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DependentOffer {

    private String offerProductType;
    private String minCount;
    private String maxCount;
    private List<String> offerCode;
    private List<String> productCode;

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

    public String getOfferProductType() {
        return offerProductType;
    }

    public void setOfferProductType(String offerProductType) {
        this.offerProductType = offerProductType;
    }

    public List<String> getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(List<String> offerCode) {
        this.offerCode = offerCode;
    }

    public List<String> getProductCode() {
        return productCode;
    }

    public void setProductCode(List<String> productCode) {
        this.productCode = productCode;
    }
}