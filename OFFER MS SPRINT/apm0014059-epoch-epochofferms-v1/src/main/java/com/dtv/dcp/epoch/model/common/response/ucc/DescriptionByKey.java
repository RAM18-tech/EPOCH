package com.dtv.dcp.epoch.model.common.response.ucc;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescriptionByKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String onlineShortDescription;
	private String onlineLongDescription;
	private String onlineShortDescForServices;
	private String onlineLongDescForServices;
	private String opusShortDescription;
	private String opusLongDescription;
	private String opusShortDescForServices;
	private String opusLongDescForServices;

	public String getOnlineShortDescription() {
		return onlineShortDescription;
	}

	public void setOnlineShortDescription(String onlineShortDescription) {
		this.onlineShortDescription = onlineShortDescription;
	}

	public String getOnlineLongDescription() {
		return onlineLongDescription;
	}

	public void setOnlineLongDescription(String onlineLongDescription) {
		this.onlineLongDescription = onlineLongDescription;
	}

	public String getOnlineShortDescForServices() {
		return onlineShortDescForServices;
	}

	public void setOnlineShortDescForServices(String onlineShortDescForServices) {
		this.onlineShortDescForServices = onlineShortDescForServices;
	}

	public String getOnlineLongDescForServices() {
		return onlineLongDescForServices;
	}

	public void setOnlineLongDescForServices(String onlineLongDescForServices) {
		this.onlineLongDescForServices = onlineLongDescForServices;
	}

	public String getOpusShortDescription() {
		return opusShortDescription;
	}

	public void setOpusShortDescription(String opusShortDescription) {
		this.opusShortDescription = opusShortDescription;
	}

	public String getOpusLongDescription() {
		return opusLongDescription;
	}

	public void setOpusLongDescription(String opusLongDescription) {
		this.opusLongDescription = opusLongDescription;
	}

	public String getOpusShortDescForServices() {
		return opusShortDescForServices;
	}

	public void setOpusShortDescForServices(String opusShortDescForServices) {
		this.opusShortDescForServices = opusShortDescForServices;
	}

	public String getOpusLongDescForServices() {
		return opusLongDescForServices;
	}

	public void setOpusLongDescForServices(String opusLongDescForServices) {
		this.opusLongDescForServices = opusLongDescForServices;
	}

	@Override
	public String toString() {
		return "DescriptionByKey [onlineShortDescription=" + onlineShortDescription + ", onlineLongDescription="
				+ onlineLongDescription + ", onlineShortDescForServices=" + onlineShortDescForServices
				+ ", onlineLongDescForServices=" + onlineLongDescForServices + ", opusShortDescription="
				+ opusShortDescription + ", opusLongDescription=" + opusLongDescription + ", opusShortDescForServices="
				+ opusShortDescForServices + ", opusLongDescForServices=" + opusLongDescForServices + "]";
	}

}