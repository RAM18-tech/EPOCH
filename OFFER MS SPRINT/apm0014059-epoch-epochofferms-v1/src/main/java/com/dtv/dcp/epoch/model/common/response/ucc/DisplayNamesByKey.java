package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisplayNamesByKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String onlineDisplayName;
	private String onlineDisplayNameForServices;
	private String opusDisplayName;
	private String opusDisplayNameForServices;

	public String getOnlineDisplayName() {
		return onlineDisplayName;
	}

	public void setOnlineDisplayName(String onlineDisplayName) {
		this.onlineDisplayName = onlineDisplayName;
	}

	public String getOnlineDisplayNameForServices() {
		return onlineDisplayNameForServices;
	}

	public void setOnlineDisplayNameForServices(String onlineDisplayNameForServices) {
		this.onlineDisplayNameForServices = onlineDisplayNameForServices;
	}

	public String getOpusDisplayName() {
		return opusDisplayName;
	}

	public void setOpusDisplayName(String opusDisplayName) {
		this.opusDisplayName = opusDisplayName;
	}

	public String getOpusDisplayNameForServices() {
		return opusDisplayNameForServices;
	}

	public void setOpusDisplayNameForServices(String opusDisplayNameForServices) {
		this.opusDisplayNameForServices = opusDisplayNameForServices;
	}

	@Override
	public String toString() {
		return "DisplayNamesByKey [onlineDisplayName=" + onlineDisplayName + ", onlineDisplayNameForServices="
				+ onlineDisplayNameForServices + ", opusDisplayName=" + opusDisplayName
				+ ", opusDisplayNameForServices=" + opusDisplayNameForServices + "]";
	}

}