package com.dtv.dcp.epoch.model.ct.generic;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericLocaleBase implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The en. */
	private String en;


	/**
	 * @return the en
	 */
	public String getEn() {
		return en;
	}


	/**
	 * @param en the en to set
	 */
	public void setEn(String en) {
		this.en = en;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Name [en=");
		builder.append(en);
		builder.append("]");
		return builder.toString();
	}
	
	
}
