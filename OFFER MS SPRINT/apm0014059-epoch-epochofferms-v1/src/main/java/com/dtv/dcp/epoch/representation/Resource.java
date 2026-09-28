package com.dtv.dcp.epoch.representation;
/**
 * REST Representation  wrapper for a single Resource
 * 
 * @param <T> the entity to represent as REST Resource
 */
public class Resource<T> {
	
	/**
	 * Optional Hypermedia link of the resource.
	 */
	private Links links;

	private T content;

	/**
	 * Public constructor which provides for Response content
	 * 
	 * @param content
	 */
	public Resource(T content) {
		super();
		setContent(content);
	}

	public T getContent() {
		return content;
	}

	@SuppressWarnings("unused")
	private void setContent(T content) {
		this.content = content;
	}

	public Links getLinks() {
		return links;
	}
	
	public void setLinks(Links links) {
		this.links = links;
	}	
	
}
