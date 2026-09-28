package com.dtv.dcp.epoch.model.ct.request;

import java.util.List;

public class CTDeviceDetailsRequest {
	
	private List<String> make;
	
	private List<String> model;
	
	private List<String> deviceType;

	

	public List<String> getMake() {
		return make;
	}

	public void setMake(List<String> make) {
		this.make = make;
	}

	public List<String> getModel() {
		return model;
	}

	public void setModel(List<String> model) {
		this.model = model;
	}

	public List<String> getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(List<String> deviceType) {
		this.deviceType = deviceType;
	}

	@Override
	public String toString() {
		return "CTDeviceDetailsRequest [make=" + make + ", model=" + model + ", deviceType=" + deviceType + "]";
	}


	
}
