package com.dtv.dcp.epoch.representation;

import java.io.Serializable;

/**
 * REST Representation  for a Links segment in other representations(Collection, Error, Resource)
 * 
 */
public class CollectionLinks extends Links implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String prev;
	
	private String next;
	
	private String first;
	
	private String last;

	/**
	 * This is public constructor which takes a String as input. 
	 * This will be the head link
	 * 
	 * @param self - String input
	 */
	public CollectionLinks(String self) {
		super(self);
	}

	public String getPrev() {
		return prev;
	}

	public void setPrev(String prev) {
		this.prev = prev;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}			
	
}
