package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class BoltOnList
 * @author vd7621
 */
public class BoltOnList  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	@JsonProperty("name")
	private String name;

	/** The type. */
	@JsonProperty("type")
	private String type;

	/** The dataAmount. */
	@JsonProperty("dataAmount")
	private String dataAmount;

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

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the dataAmount
	 */
	public String getDataAmount() {
		return dataAmount;
	}

	/**
	 * @param dataAmount the dataAmount to set
	 */
	public void setDataAmount(String dataAmount) {
		this.dataAmount = dataAmount;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BoltOnList [name=" + name + ", type=" + type + ", dataAmount=" + dataAmount + "]";
	}
	
}
