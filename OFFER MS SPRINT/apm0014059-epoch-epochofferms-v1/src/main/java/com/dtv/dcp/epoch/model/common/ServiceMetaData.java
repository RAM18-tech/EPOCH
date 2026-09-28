package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class ServiceMetaData.
 */
public class ServiceMetaData implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The title. */
	@JsonProperty("title")
	private String title;

	/** The description. */
	@JsonProperty("description")
	private String description;

	/** The version. */
	@JsonProperty("version")
	private String version;

	/** The timestamp. */
	@JsonProperty("timestamp")
	private String timestamp;

	/** The response time. */
	@JsonProperty("processingTime")
	private long processingTime;

	/**
	 * Instantiates a new service meta data.
	 */
	public ServiceMetaData() {
		super();
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String title() {
		return title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title
	 *            the new title
	 */
	public void title(String title) {
		this.title = title;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String description() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description
	 *            the new description
	 */
	public void description(String description) {
		this.description = description;
	}

	/**
	 * Gets the version.
	 *
	 * @return the version
	 */
	public String version() {
		return version;
	}

	/**
	 * Sets the version.
	 *
	 * @param version
	 *            the new version
	 */
	public void version(String version) {
		this.version = version;
	}

	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public String timestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp.
	 *
	 * @param timestamp
	 *            the new timestamp
	 */
	public void timestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Gets the response time.
	 *
	 * @return the response time
	 */
	public long processingTime() {
		return processingTime;
	}

	/**
	 * Sets the processing time.
	 *
	 * @param processingTime
	 *            the new processing time
	 */
	public void processingTime(long processingTime) {
		this.processingTime = processingTime;
	}

	/**
	 * toString() implementation (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ServiceMetaInfo [title=" + title + ", description=" + description + ", version=" + version
				+ ", timestamp=" + timestamp + ", processingTime=" + processingTime + "]";
	}
}
