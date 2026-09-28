package com.dtv.dcp.epoch.model.common.response;

import com.dtv.dcp.epoch.model.common.request.Campaign;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CampaignProducts implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Campaign> products;

    private int limit;

    /** The offset. */
    private int offset;

    /** The count. */
    private int count;

    /** The total. */
    private int total;

    public List<Campaign> getProducts() {
        return products;
    }

    public void setProducts(List<Campaign> products) {
        this.products = products;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}