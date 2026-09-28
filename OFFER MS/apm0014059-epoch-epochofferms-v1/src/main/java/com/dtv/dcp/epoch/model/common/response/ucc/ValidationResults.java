package com.dtv.dcp.epoch.model.common.response.ucc;

import java.io.Serializable;

public class ValidationResults implements Serializable {

	private static final long serialVersionUID = 1L;

	private String status;
	private String errorCode;
	private String errorMessage;

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

	@Override
	public String toString() {
		return "ValidationResults [status=" + status + ", errorCode=" + errorCode + ", errorMessage=" + errorMessage
				+ "]";
	}

}