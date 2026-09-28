package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

public class UserType implements Serializable {
   	
		/**
	 * 
	 */
	private static final long serialVersionUID = -8470211681350510377L;

	private String typeId;

	private String id;
		
    public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
