package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

public class CTCouponsRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6936553115730420584L;
	
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
