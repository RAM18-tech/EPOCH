package com.dtv.dcp.epoch.model.ct.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CTErrorResponse implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("code")
	private String code;
	
	@JsonProperty("error")
	private String error;
	
	@JsonProperty("message")
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
