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
 * @param <T>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings({"squid:S1948"})
public class Collection<T> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Hypermedia links for Resource Collection
	 */
	private CollectionLinks links;
	
	
	private List<T> collection;
	
	private Pagination pagination;
	
	
	private Collection() {
		super();
	}

	public Collection(List<T> collection) {
		super();
		this.collection = collection;
	}

	public CollectionLinks getLinks() {
		return links;
	}

	public void setLinks(CollectionLinks links) {
		this.links = links;
	}

	//used only for serialization
	@SuppressWarnings("unused")
	private void setCollection(List<T> collection) {
		this.collection = collection;
	}

	//used only for serialization
	@SuppressWarnings("unused")
	private void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public List<T> getCollection() {
		return collection;
	}

	public Pagination getPagination() {
		return pagination;
	}
	
}

@JsonInclude(JsonInclude.Include.NON_NULL)
class Pagination {
	/**
	 * Current pageNumber in the paginated collection.
	 */
	private int pageNumber;
	/**
	 * Number of items in a single page(size) used for pagination.
	 * This is not the actual number of items returned in the response.
	 * 
	 */
	private int pageSize;
	/**
	 * Total number of records, pupulated by the server
	 */
	private long totalCount;
	/**
	 * Constructor
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @param totalCount
	 */
	public Pagination(int pageNumber, int pageSize, long totalCount) {
		super();
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.totalCount = totalCount;
	}

	@SuppressWarnings("unused")
	private void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}
	
	@SuppressWarnings("unused")
	private void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	public long getTotalCount() {
		return totalCount;
	}
	
	@SuppressWarnings("unused")
	private void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}	
}
