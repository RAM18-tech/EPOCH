package com.dtv.ct.commercetool.model;

import java.util.List;

public class DealerCodeUpdates {

    List<String> addDealerCodes;

    List<String> removeDealerCodes;

    public List<String> getRemoveDealerCodes() {
        return removeDealerCodes;
    }

    public void setRemoveDealerCodes(List<String> removeDealerCodes) {
        this.removeDealerCodes = removeDealerCodes;
    }

    String attributeName;

    public List<String> getAddDealerCodes() {
        return addDealerCodes;
    }

    public void setAddDealerCodes(List<String> addDealerCodes) {
        this.addDealerCodes = addDealerCodes;
    }

    String offerId;

    boolean publish;

    public void setPublish(boolean publish) {
        this.publish = publish;
    }

    public boolean isPublish() {
        return publish;
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



}

