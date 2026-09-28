package com.dtv.dcp.epoch.model.ct.generic;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompatibleProductNameValue implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The value. */
	private List<CompatibleProductValue> value;
	
	/** The name. */
	private String name;

	/**
	 * @return the value
	 */
	public List<CompatibleProductValue> getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(List<CompatibleProductValue> value) {
		this.value = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GenericNameValueBase [value=" + value + ", name=" + name + "]";
	}
	
	
}
