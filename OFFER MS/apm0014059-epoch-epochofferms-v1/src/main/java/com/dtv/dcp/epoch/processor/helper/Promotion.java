package com.dtv.dcp.epoch.processor.helper;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class Promotion implements Serializable {

    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The String promoId
     */
    @JsonProperty("promoId")
    @ApiModelProperty(value = "Promotion id from cassandra Json")
    private String promoId;
    /**
     * The String startDate
     */
    @JsonProperty("startDate")
    @ApiModelProperty(value = "Promotion startDate from cassandra Json")
    private String startDate;
    /**
     * The String endDate
     */
    @JsonProperty("endDate")
    @ApiModelProperty(value = "Promotion endDate from cassandra Json")
    private String endDate;

    /**
     * @return String
     */
    public String getPromoId() {
        return promoId;
    }

    /**
     * @param promoId
     */
    public void setPromoId(String promoId) {
        this.promoId = promoId;
    }

    /**
     * @return String
     */
    public String getStartDate() {
        return startDate;
    }

    /**
     * @param startDate
     */
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    /**
     * @return String
     */
    public String getEndDate() {
        return endDate;
    }

    /**
     * @param endDate
     */
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((promoId == null) ? 0 : promoId.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Promotion other = (Promotion) obj;
        if (promoId == null) {
            if (other.promoId != null)
                return false;
        } else if (!promoId.equals(other.promoId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Promotion [promoId=");
        builder.append(promoId);
        builder.append(", startDate=");
        builder.append(startDate);
        builder.append(", endDate=");
        builder.append(endDate);
        builder.append("]");
        return builder.toString();
    }

}