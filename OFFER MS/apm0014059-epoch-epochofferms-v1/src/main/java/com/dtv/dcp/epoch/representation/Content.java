package com.dtv.dcp.epoch.representation;


import java.io.Serializable;

import com.dtv.dcp.epoch.model.common.ServiceMetaData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The Class Content.
 *
 * @param <T> the generic type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "meta-info", "content" })
public class Content<T extends Serializable> implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The content data. */
	@JsonProperty("content")
	private T data;

	/** The meta data. */
	@JsonProperty("meta-info")
	private ServiceMetaData metaData;

	/**
	 * Instantiates a new content.
	 */
	private Content() {
	}

	/**
	 * Instantiates a new content.
	 *
	 * @param data
	 *            the data
	 */
	public Content(T data) {
		super();
		this.data = data;
		this.metaData = null;
	}

	/**
	 * Instantiates a new content.
	 *
	 * @param metaData
	 *            the meta data
	 */
	public Content(ServiceMetaData metaData) {
		super();
		this.data = null;
		this.metaData = metaData;
	}

	/**
	 * Instantiates a new content.
	 *
	 * @param data the data
	 * @param metaData the meta data
	 */
	public Content(T data, ServiceMetaData metaData) {
		super();
		this.data = data;
		this.metaData = metaData;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public T getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(T data) {
		this.data = data;
	}

	/**
	 * Gets the meta data.
	 *
	 * @return the meta data
	 */
	public ServiceMetaData metaData() {
		return metaData;
	}

	/**
	 * Sets the meta data.
	 *
	 * @param metaData the new meta data
	 */
	public void metaData(ServiceMetaData metaData) {
		this.metaData = metaData;
	}

	
	/**
	 * toString() implementation.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Content [data=" + data + ", metaData=" + metaData + "]";
	}
}
