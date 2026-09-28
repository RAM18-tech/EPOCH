package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DTVSatelliteRsnResponse implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String fCode;
	private List<String> shortDesc;
	
	
	public String getfCode() {
		return fCode;
	}
	public void setfCode(String fCode) {
		this.fCode = fCode;
	}
	public List<String> getShortDesc() {
		return shortDesc;
	}
	public void setShortDesc(List<String> shortDesc) {
		this.shortDesc = shortDesc;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FeeDetails [fCode=");
		builder.append(fCode);
		builder.append(", shortDesc=");
		builder.append(shortDesc);
		builder.append("]");
		return builder.toString();
	}
}
