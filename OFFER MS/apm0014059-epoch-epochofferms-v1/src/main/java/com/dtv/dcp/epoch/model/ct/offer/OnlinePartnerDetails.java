package com.dtv.dcp.epoch.model.ct.offer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OnlinePartnerDetails implements Serializable {
    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    @JsonProperty("onlinePartnerName")
    private String partnerName;
    private String agentId;
    private List<String> dealerCode1;
    private List<String> dealerCode2;

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public List<String> getDealerCode1() {
        return dealerCode1;
    }

    public void setDealerCode1(List<String> dealerCode1) {
        this.dealerCode1 = dealerCode1;
    }

    public List<String> getDealerCode2() {
        return dealerCode2;
    }

    public void setDealerCode2(List<String> dealerCode2) {
        this.dealerCode2 = dealerCode2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, dealerCode1, dealerCode2, partnerName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OnlinePartnerDetails other = (OnlinePartnerDetails) obj;
        return Objects.equals(agentId, other.agentId) && Objects.equals(dealerCode1, other.dealerCode1)
                && Objects.equals(dealerCode2, other.dealerCode2) && Objects.equals(partnerName, other.partnerName);
    }

    @Override
    public String toString() {
        return "OnlinePartnerDetails{" +
                "partnerName='" + partnerName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", dealerCode1=" + dealerCode1 +
                ", dealerCode2=" + dealerCode2 +
                '}';
    }
}
