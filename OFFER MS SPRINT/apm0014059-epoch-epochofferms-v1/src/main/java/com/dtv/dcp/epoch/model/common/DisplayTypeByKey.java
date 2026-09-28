/**
 * 
 */
package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayTypeByKey implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
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
