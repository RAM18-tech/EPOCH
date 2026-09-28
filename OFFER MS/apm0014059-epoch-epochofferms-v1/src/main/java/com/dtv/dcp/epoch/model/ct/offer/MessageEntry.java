package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

public class MessageEntry implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private Object value; // Can be MessageType, List<List<MessageDetail>>, or String

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
