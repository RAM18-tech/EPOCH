package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

public class Exclusions implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> offerType;

    public List<String> getOfferType() {
        return offerType;
    }

    public void setOfferType(List<String> offerType) {
        this.offerType = offerType;
    }

    @Override
    public String toString() {
        return "Exclusions{" +
                "offerType=" + offerType +
                '}';
    }
}
