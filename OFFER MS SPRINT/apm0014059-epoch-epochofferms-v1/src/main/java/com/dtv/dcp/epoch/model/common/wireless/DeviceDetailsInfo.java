package com.dtv.dcp.epoch.model.common.wireless;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class DeviceDetailsInfo
 *
 */
public class DeviceDetailsInfo {
	
	@JsonProperty("imeiType")
	private String imeiType;

	public String getImeiType() {
		return imeiType;
	}

	public void setImeiType(String imeiType) {
		this.imeiType = imeiType;
	}
	
	

}
