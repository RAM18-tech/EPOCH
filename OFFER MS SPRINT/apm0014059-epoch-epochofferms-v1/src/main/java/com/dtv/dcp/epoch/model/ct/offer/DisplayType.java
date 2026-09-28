package com.dtv.dcp.epoch.model.ct.offer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayType implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String displayTypeKey;
    private String displayTypeValue;

    public String getDisplayTypeKey() {
        return displayTypeKey;
    }

    public void setDisplayTypeKey(String displayTypeKey) {
        this.displayTypeKey = displayTypeKey;
    }

    public String getDisplayTypeValue() {
        return displayTypeValue;
    }

    public void setDisplayTypeValue(String displayTypeValue) {
        this.displayTypeValue = displayTypeValue;
    }
}