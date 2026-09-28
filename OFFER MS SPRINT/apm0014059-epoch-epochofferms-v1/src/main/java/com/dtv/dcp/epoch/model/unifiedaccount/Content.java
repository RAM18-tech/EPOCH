package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class Content
 * @author vd7621
 */

public class Content implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@JsonProperty("serviceInfo")
	private Map<String, UnifiedProductDetailsInfo> serviceInfo;

	@JsonProperty("errors")
	private Map<String, Error> errors;

	/**
	 * @return
	 */
	public Map<String, UnifiedProductDetailsInfo> getServiceInfo() {
		return serviceInfo;
	}

	/**
	 * @param serviceInfo
	 */
	public void setServiceInfo(Map<String, UnifiedProductDetailsInfo> serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	public Map<String, Error> getErrors() {
		return errors;
	}

	public void setErrors(Map<String, Error> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Content [serviceInfo=");
		builder.append(serviceInfo);
		builder.append(", errors=");
		builder.append(errors);
		builder.append("]");
		return builder.toString();
	}

}
