package com.dtv.dcp.epoch.model.ct.offer;


import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelLineupAlertMessagesByKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private String opusAlertMessageForServices;
	private String opusAlertMessage;
	private String onlineAlertMessageForServices;
	private String onlineAlertMessage;
	/**
	 * @return the opusAlertMessageForServices
	 */
	public String getOpusAlertMessageForServices() {
		return opusAlertMessageForServices;
	}
	/**
	 * @param opusAlertMessageForServices the opusAlertMessageForServices to set
	 */
	public void setOpusAlertMessageForServices(String opusAlertMessageForServices) {
		this.opusAlertMessageForServices = opusAlertMessageForServices;
	}
	/**
	 * @return the opusAlertMessage
	 */
	public String getOpusAlertMessage() {
		return opusAlertMessage;
	}
	/**
	 * @param opusAlertMessage the opusAlertMessage to set
	 */
	public void setOpusAlertMessage(String opusAlertMessage) {
		this.opusAlertMessage = opusAlertMessage;
	}
	/**
	 * @return the onlineAlertMessageForServices
	 */
	public String getOnlineAlertMessageForServices() {
		return onlineAlertMessageForServices;
	}
	/**
	 * @param onlineAlertMessageForServices the onlineAlertMessageForServices to set
	 */
	public void setOnlineAlertMessageForServices(String onlineAlertMessageForServices) {
		this.onlineAlertMessageForServices = onlineAlertMessageForServices;
	}
	/**
	 * @return the onlineAlertMessage
	 */
	public String getOnlineAlertMessage() {
		return onlineAlertMessage;
	}
	/**
	 * @param onlineAlertMessage the onlineAlertMessage to set
	 */
	public void setOnlineAlertMessage(String onlineAlertMessage) {
		this.onlineAlertMessage = onlineAlertMessage;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ChannelLineupAlertMessagesByKey [opusAlertMessageForServices=" + opusAlertMessageForServices
				+ ", opusAlertMessage=" + opusAlertMessage + ", onlineAlertMessageForServices="
				+ onlineAlertMessageForServices + ", onlineAlertMessage=" + onlineAlertMessage + "]";
	}
	
	

}