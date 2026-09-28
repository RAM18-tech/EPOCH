package com.dtv.dcp.epoch.model.ct.offer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PartnerProductIdByKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("partnerProductID")
    private String partnerProductID;

    public String getPartnerProductID() {
        return partnerProductID;
    }

    public void setPartnerProductID(String partnerProductID) {
        this.partnerProductID = partnerProductID;
    }

    @Override
    public String toString() {
        return "PartnerProductIdByKey{" +
                "partnerProductID='" + partnerProductID + '\'' +
                '}';
    }
}
