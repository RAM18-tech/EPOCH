package com.dtv.dcp.epoch.representation;

import java.io.Serializable;

/**
 * REST Representation  for a Links segment in other representations(Collection, Error, Resource)
 * 
 */
public class Links implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String self;
	
	/**
	 * This is public constructor which takes a String as input. 
	 * 
	 * @param self - String input
	 */
	public Links(String self) {
		super();
		setSelf(self);
	}

	public String getSelf() {
		return self;
	}

	@SuppressWarnings("unused")
	private void setSelf(String self) {
		this.self = self;
	}	
}
