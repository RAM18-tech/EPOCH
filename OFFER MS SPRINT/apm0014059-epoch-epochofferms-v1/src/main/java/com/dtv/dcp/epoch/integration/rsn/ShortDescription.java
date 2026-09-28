package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;

public class ShortDescription implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The String Object shortDesc */
	private String shortDesc;
	/** The String Object system */
	private String system;

	/**
	 * @return String
	 */
	public String getShortDesc() {
		return shortDesc;
	}

	/**
	 * @param shortDesc
	 */
	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	/**
	 * @return String
	 */
	public String getSystem() {
		return system;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ShortDescription [shortDesc=");
		builder.append(shortDesc);
		builder.append(", system=");
		builder.append(system);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param system
	 */
	public void setSystem(String system) {
		this.system = system;
	}

}
