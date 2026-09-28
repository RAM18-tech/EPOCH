package com.dtv.dcp.epoch.model.ct.benefit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This class represents the display names of a benefit in the application.
 * It includes both short and long display names.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BenefitDisplayNamesByKey implements Serializable{
    private static final long serialVersionUID = 1L;

    @JsonProperty("shortDisplayName")
    private String shortDisplayName;
    @JsonProperty("longDisplayName")
    private String longDisplayName;

    /**
     * Returns the short display name of the benefit.
     *
     * @return a String representing the short display name
     */
    public String getShortDisplayName() {
        return shortDisplayName;
    }

    /**
     * Sets the short display name of the benefit.
     *
     * @param shortDisplayName a String representing the short display name
     */
    public void setShortDisplayName(String shortDisplayName) {
        this.shortDisplayName = shortDisplayName;
    }

    /**
     * Returns the long display name of the benefit.
     *
     * @return a String representing the long display name
     */
    public String getLongDisplayName() {
        return longDisplayName;
    }

    /**
     * Sets the long display name of the benefit.
     *
     * @param longDisplayName a String representing the long display name
     */
    public void setLongDisplayName(String longDisplayName) {
        this.longDisplayName = longDisplayName;
    }
}
