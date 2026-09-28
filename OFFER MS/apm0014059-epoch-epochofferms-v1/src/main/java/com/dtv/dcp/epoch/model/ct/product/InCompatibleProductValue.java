package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class InCompatibleProductValue implements Serializable {
	

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The typeId. */
	private String id;
	
	/** The id. */
	private String typeId;
	
	
	private String key;
	
	private String label;
	

	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}


	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @return the typeId
	 */
	public String getTypeId() {
		return typeId;
	}


	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GenericNameValueBase [typeId=" + typeId + ", id=" + id + "]";
	}
	
	


}
