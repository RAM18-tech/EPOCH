package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

public class GlobalEligibilityAttribute implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	private String requestAttributePath;
	private String responseAttributePath;
	
	private String attributeName;
	private String staticValue;

	public String getRequestAttributePath() {
		return requestAttributePath;
	}

	public void setRequestAttributePath(String requestAttributePath) {
		this.requestAttributePath = requestAttributePath;
	}

	public String getResponseAttributePath() {
		return responseAttributePath;
	}

	public void setResponseAttributePath(String responseAttributePath) {
		this.responseAttributePath = responseAttributePath;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getStaticValue() {
		return staticValue;
	}

	public void setStaticValue(String staticValue) {
		this.staticValue = staticValue;
	}
	
	@Override
	public String toString() {
		return "GlobalEligibilityAttribute [requestAttributePath=" + requestAttributePath + ", responseAttributePath="
				+ responseAttributePath + ", attributeName=" + attributeName + ", staticValue="
				+ staticValue + "]";
	}
	
}
