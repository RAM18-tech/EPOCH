package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author dr000y
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidFor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String startDateTime;

	private String endDateTime;

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getStartDateTime() {
		return this.startDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public String getEndDateTime() {
		return this.endDateTime;
	}

	@Override
	public String toString() {
		return "ValidFor [startDateTime=" + startDateTime + ", endDateTime=" + endDateTime + "]";
	}

}
