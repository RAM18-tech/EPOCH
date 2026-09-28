package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountInfo implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("isContracted")
	private boolean isContracted;

	/**
	 * @return the isContracted
	 */
	public boolean isContracted() {
		return isContracted;
	}

	/**
	 * @param isContracted the isContracted to set
	 */
	public void setContracted(boolean isContracted) {
		this.isContracted = isContracted;
	}

		
}
