package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DisclosureMessagesByKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String opusDisclosure;
	private String opusDisclosureForServices;
	private String onlineDisclosure;
	private String onlineDisclosureForServices;

	public String getOpusDisclosure() {
		return opusDisclosure;
	}

	public void setOpusDisclosure(String opusDisclosure) {
		this.opusDisclosure = opusDisclosure;
	}

	public String getOpusDisclosureForServices() {
		return opusDisclosureForServices;
	}

	public void setOpusDisclosureForServices(String opusDisclosureForServices) {
		this.opusDisclosureForServices = opusDisclosureForServices;
	}

	public String getOnlineDisclosure() {
		return onlineDisclosure;
	}

	public void setOnlineDisclosure(String onlineDisclosure) {
		this.onlineDisclosure = onlineDisclosure;
	}

	public String getOnlineDisclosureForServices() {
		return onlineDisclosureForServices;
	}

	public void setOnlineDisclosureForServices(String onlineDisclosureForServices) {
		this.onlineDisclosureForServices = onlineDisclosureForServices;
	}

	@Override
	public String toString() {
		return "DisclosureMessagesByKey [opusDisclosure=" + opusDisclosure + ", opusDisclosureForServices="
				+ opusDisclosureForServices + ", onlineDisclosure=" + onlineDisclosure
				+ ", onlineDisclosureForServices=" + onlineDisclosureForServices + "]";
	}

}