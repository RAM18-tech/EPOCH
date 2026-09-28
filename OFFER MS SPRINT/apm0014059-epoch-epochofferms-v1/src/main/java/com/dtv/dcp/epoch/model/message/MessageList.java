package com.dtv.dcp.epoch.model.message;

import java.io.Serializable;
import java.util.List;

public class MessageList implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer count;
	private List<Message> message;
	
	public Integer getCount() {
		return count;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
	public List<Message> getMessage() {
		return message;
	}
	public void setMessage(List<Message> message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "MessageList [count=" + count + ", message=" + message + "]";
	}
	
	
}
