package com.dtv.ct.commercetool.model;

import java.util.List;

public class StoreIdUpdates {

    List<String> storeIds;

    List<String> removeIds;

    String attributeName;

    String offerId;

    boolean publish;

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublish() {
        return publish;
    }


    public List<String> getRemoveIds() {
        return removeIds;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getOfferId() {
        return offerId;
    }


    public List<String> getStoreIds() {
        return storeIds;
    }

    public void setStoreIds(List<String> storeIds) {
        this.storeIds = storeIds;
    }
}

