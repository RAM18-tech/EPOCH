package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

public class ChannelEligibilityByKey implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> salesChannel;
    private List<String> channel;
    private List<String> subChannel;
    private List<String> siteId;
    private List<String> dealerCode;
    private List<String> dealerIds;
    private List<String> masterDealerId;
    private List<String> locationIds;
    private List<String> locationTypeIds;
    private Boolean isFulfillmentDealer;

    public List<String> getSalesChannel() {
        return salesChannel;
    }

    public void setSalesChannel(List<String> salesChannel) {
        this.salesChannel = salesChannel;
    }

    public List<String> getChannel() {
        return channel;
    }

    public void setChannel(List<String> channel) {
        this.channel = channel;
    }

    public List<String> getSiteId() {
        return siteId;
    }

    public void setSiteId(List<String> siteId) {
        this.siteId = siteId;
    }

    public List<String> getDealerIds() {
        return dealerIds;
    }

    public void setDealerIds(List<String> dealerIds) {
        this.dealerIds = dealerIds;
    }

    public List<String> getMasterDealerId() {
        return masterDealerId;
    }

    public void setMasterDealerId(List<String> masterDealerId) {
        this.masterDealerId = masterDealerId;
    }

    public List<String> getDealerCode() {
        return dealerCode;
    }

    public void setDealerCode(List<String> dealerCode) {
        this.dealerCode = dealerCode;
    }

    public List<String> getSubChannel() {
        return subChannel;
    }

    public void setSubChannel(List<String> subChannel) {
        this.subChannel = subChannel;
    }

    public List<String> getLocationIds() {
        return locationIds;
    }

    public void setLocationIds(List<String> locationIds) {
        this.locationIds = locationIds;
    }

    public List<String> getLocationTypeIds() {
        return locationTypeIds;
    }

    public void setLocationTypeIds(List<String> locationTypeIds) {
        this.locationTypeIds = locationTypeIds;
    }

    public Boolean getFulfillmentDealer() {
        return isFulfillmentDealer;
    }

    public void setFulfillmentDealer(Boolean fulfillmentDealer) {
        isFulfillmentDealer = fulfillmentDealer;
    }
}
