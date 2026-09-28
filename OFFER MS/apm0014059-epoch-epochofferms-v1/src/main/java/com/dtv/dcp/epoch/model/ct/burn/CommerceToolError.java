package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommerceToolError implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String errorId;
	private String message;

	public CommerceToolError(String errorId, String message) {
		this.errorId = errorId;
		this.message = message;
	}

	public CommerceToolError() {
		// Intentionally Left Blank For Jackson
	}

	@Override
	public String toString() {
		return "CommerceToolError{" + "errorId='" + errorId + '\'' + ", message='" + message + '\'' + '}';
	}

	public String getErrorId() {
		return errorId;
	}

	public void setErrorId(String errorId) {
		this.errorId = errorId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
