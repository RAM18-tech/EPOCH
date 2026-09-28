package com.dtv.dcp.epoch.model.cache;

import java.io.Serializable;
import java.util.Map;

public class CacheEntry implements Serializable{
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The key. */
	private Object key;
	
	/** The value. */
	private Object value;
	
	private Map<Object, Object> dataMap;

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Map<Object, Object> getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map<Object, Object> dataMap) {
		this.dataMap = dataMap;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheEntry [key=");
		builder.append(key);
		builder.append(", value=");
		builder.append(value);
		builder.append(", dataMap=");
		builder.append(dataMap);
		builder.append("]");
		return builder.toString();
	}

}