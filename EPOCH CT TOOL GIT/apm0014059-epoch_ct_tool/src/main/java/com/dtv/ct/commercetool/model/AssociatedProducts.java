package com.dtv.ct.commercetool.model;

import java.util.List;

public class AssociatedProducts {

    private String offerId;
    private List<String> qualifyingProductIds;
    private List<String> bundledProductIds;
    boolean keepExisting = true;
    boolean clearInside;
    private List<String> removeFromQualifyingProductIds;
    private List<String> removeFromBundledProductIds;

    public List<String> getRemoveFromQualifyingProductIds() {
        return removeFromQualifyingProductIds;
    }

    public void setRemoveFromQualifyingProductIds(List<String> removeFromQualifyingProductIds) {
        this.removeFromQualifyingProductIds = removeFromQualifyingProductIds;
    }

    public List<String> getRemoveFromBundledProductIds() {
        return removeFromBundledProductIds;
    }

    public void setRemoveFromBundledProductIds(List<String> removeFromBundledProductIds) {
        this.removeFromBundledProductIds = removeFromBundledProductIds;
    }

    public boolean isClearInside() {
        return clearInside;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public List<String> getQualifyingProductIds() {
        return qualifyingProductIds;
    }

    public void setQualifyingProductIds(List<String> qualifyingProductIds) {
        this.qualifyingProductIds = qualifyingProductIds;
    }

    public List<String> getBundledProductIds() {
        return bundledProductIds;
    }

    public void setBundledProductIds(List<String> bundledProductIds) {
        this.bundledProductIds = bundledProductIds;
    }

    public boolean isKeepExisting() {
        return keepExisting;
    }

    public void setKeepExisting(boolean keepExisting) {
        this.keepExisting = keepExisting;
    }
}
