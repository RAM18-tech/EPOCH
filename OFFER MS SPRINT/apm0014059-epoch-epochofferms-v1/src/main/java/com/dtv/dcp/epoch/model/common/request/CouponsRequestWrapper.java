package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

public class CouponsRequestWrapper implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The logged In Indicator. */
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}