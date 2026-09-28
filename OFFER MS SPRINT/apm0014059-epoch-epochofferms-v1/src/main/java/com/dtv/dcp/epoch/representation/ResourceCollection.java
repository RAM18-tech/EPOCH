package com.dtv.dcp.epoch.representation;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Rest Representation for the collection of resources of type T.
 * Collection may optionally contain pagination details. 		
 * 
 * Below is the serialized representation in Json format.
 * {
 * 		"links" : {
 * 			"self" : "",
 * 			"prev" : "",
 * 			"next" : "",
 * 			"first" : "",
 * 			"last" : "" 			
 * 		}
 * 		"collection" : [],
 * 		"pagination" : {
 * 			"pageNumber" : "",
 * 			"pageSize" : "",
 * 			"totalCount" : ""
 * 		}
 * }
 *
 * @param <T> the generic type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings({"squid:S1948"})
public class ResourceCollection<T extends Serializable> implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** Hypermedia links for Resource Collection. */
	private CollectionLinks links;
	
	/** The collection. */
	private List<T> collection;
	
	/** The pagination. */
	private Pagination pagination;

	/**
	 * This is public constructor which takes a list of Collections as input.
	 *
	 * @param collection the collection
	 */
	public ResourceCollection(List<T> collection) {
		super();
		this.collection = collection;
	}

	/**
	 * Gets the links.
	 *
	 * @return the links
	 */
	public CollectionLinks getLinks() {
		return links;
	}

	/**
	 * Sets the links.
	 *
	 * @param links the new links
	 */
	public void setLinks(CollectionLinks links) {
		this.links = links;
	}

	/**
	 * Sets the collection.
	 *
	 * @param collection the new collection
	 */
	//used only for serialization
	@SuppressWarnings("unused")
	public void setCollection(List<T> collection) {
		this.collection = collection;
	}

	/**
	 * Sets the pagination.
	 *
	 * @param pagination the new pagination
	 */
	//used only for serialization
	@SuppressWarnings("unused")
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	/**
	 * Gets the collection.
	 *
	 * @return the collection
	 */
	public List<T> getCollection() {
		return collection;
	}

	/**
	 * Gets the pagination.
	 *
	 * @return the pagination
	 */
	public Pagination getPagination() {
		return pagination;
	}
	


}