package com.dtv.dcp.epoch.model.ct.benefit;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This class represents the descriptions of a benefit in the application.
 * It includes both short and long descriptions.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BenefitDescriptionByKey implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("shortDesc")
    private String shortDesc;

    @JsonProperty("longDesc")
    private String longDesc;

    /**
     * Returns the short description of the benefit.
     *
     * @return a String representing the short description
     */
    public String getShortDesc() {
        return shortDesc;
    }

    /**
     * Sets the short description of the benefit.
     *
     * @param shortDesc a String representing the short description
     */
    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    /**
     * Returns the long description of the benefit.
     *
     * @return a String representing the long description
     */
    public String getLongDesc() {
        return longDesc;
    }

    /**
     * Sets the long description of the benefit.
     *
     * @param longDesc a String representing the long description
     */
    public void setLongDesc(String longDesc) {
        this.longDesc = longDesc;
    }
}