package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BurnResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String status;
	private String errorCode;
	private String errorMessage;

	public BurnResult(String status, String errorCode, String errorMessage) {
		this.status = status;
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public BurnResult() {
		// Intentionally Left Blank For Jackson
	}

	@Override
	public String toString() {
		return "BurnResult{" + "status='" + status + '\'' + ", errorCode='" + errorCode + '\'' + ", errorMessage='"
				+ errorMessage + '\'' + '}';
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
