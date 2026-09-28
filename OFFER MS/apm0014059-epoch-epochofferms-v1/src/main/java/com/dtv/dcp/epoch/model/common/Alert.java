package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class Alert.
 */
@ApiModel(value = "Alert")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Alert implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The alertCode. */
	@ApiModelProperty(value = "Alert Code")
	@JsonProperty("alertCode")
	private String alertCode;
	
	/** The alertDescription. */
	@ApiModelProperty(value = "Alert Description")
	@JsonProperty("alertDescription")
	private String alertDescription;

	/**
	 * Gets the alert code.
	 *
	 * @return the alert code
	 */
	public String getAlertCode() {
		return alertCode;
	}

	/**
	 * Sets the alert code.
	 *
	 * @param alertCode the new alert code
	 */
	public void setAlertCode(String alertCode) {
		this.alertCode = alertCode;
	}

	/**
	 * Gets the alert description.
	 *
	 * @return the alert description
	 */
	public String getAlertDescription() {
		return alertDescription;
	}

	/**
	 * Sets the alert description.
	 *
	 * @param alertDescription the new alert description
	 */
	public void setAlertDescription(String alertDescription) {
		this.alertDescription = alertDescription;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Alert [alertCode=" + alertCode + ", alertDescription=" + alertDescription + "]";
	}

	
}
