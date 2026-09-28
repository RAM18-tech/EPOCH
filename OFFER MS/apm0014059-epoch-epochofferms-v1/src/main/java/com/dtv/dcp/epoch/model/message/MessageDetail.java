package com.dtv.dcp.epoch.model.message;

import java.io.Serializable;

public class MessageDetail implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String messageCode;
	private String messageText;
	private String messageSeverity;
	private String messageCategory;
	
	public String getMessageCode() {
		return messageCode;
	}
	public void setMessageCode(String messageCode) {
		this.messageCode = messageCode;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String getMessageSeverity() {
		return messageSeverity;
	}
	public void setMessageSeverity(String messageSeverity) {
		this.messageSeverity = messageSeverity;
	}
	public String getMessageCategory() {
		return messageCategory;
	}
	public void setMessageCategory(String messageCategory) {
		this.messageCategory = messageCategory;
	}
	
	@Override
	public String toString() {
		return "MessageDetail [messageCode=" + messageCode + ", messageText=" + messageText + ", messageSeverity="
				+ messageSeverity + ", messageCategory=" + messageCategory + "]";
	}

}
