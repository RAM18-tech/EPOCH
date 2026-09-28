package com.dtv.dcp.epoch.model.message;

import java.io.Serializable;

public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String messageStatus;
	private String provider;
	private MessageDetail messageDetail;
	
	public String getMessageStatus() {
		return messageStatus;
	}
	public void setMessageStatus(String messageStatus) {
		this.messageStatus = messageStatus;
	}
	public String getProvider() {
		return provider;
	}
	public void setProvider(String provider) {
		this.provider = provider;
	}
	public MessageDetail getMessageDetail() {
		return messageDetail;
	}
	public void setMessageDetail(MessageDetail messageDetail) {
		this.messageDetail = messageDetail;
	}
	
	@Override
	public String toString() {
		return "Message [messageStatus=" + messageStatus + ", provider=" + provider + ", messageDetail=" + messageDetail
				+ "]";
	}

}
