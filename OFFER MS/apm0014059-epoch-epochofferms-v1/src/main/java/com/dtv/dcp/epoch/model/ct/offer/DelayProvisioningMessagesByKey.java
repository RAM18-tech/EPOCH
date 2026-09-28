package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DelayProvisioningMessagesByKey implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
	private String shortMessage;
	private String longMessage;
	public String getShortMessage() {
		return shortMessage;
	}
	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}
	public String getLongMessage() {
		return longMessage;
	}
	public void setLongMessage(String longMessage) {
		this.longMessage = longMessage;
	}

	
}