package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

public class CompatibleVideoDeviceModel  implements Serializable{
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String videoDeviceType;
	private List<String> modelNumbers;
	public String getVideoDeviceType() {
		return videoDeviceType;
	}
	public void setVideoDeviceType(String videoDeviceType) {
		this.videoDeviceType = videoDeviceType;
	}
	public List<String> getModelNumbers() {
		return modelNumbers;
	}
	public void setModelNumbers(List<String> modelNumbers) {
		this.modelNumbers = modelNumbers;
	}
}
