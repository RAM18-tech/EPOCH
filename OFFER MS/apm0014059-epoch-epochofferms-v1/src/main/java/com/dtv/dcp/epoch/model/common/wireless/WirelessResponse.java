package com.dtv.dcp.epoch.model.common.wireless;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class WirelessResponse
 *
 */
public class WirelessResponse {
	
	@JsonProperty(value="content", required=false)
	private List<Content> content;
	
	@JsonProperty(value="error", required=false)
	private Error error;
	
	
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public List<Content> getContent() {
		return content;
	}

	/**
	 * Sets the offers.
	 *
	 * @param offers the new offers
	 */
	public void setContent(List<Content> content) {
		this.content = content;
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WirelessResponse [contents=" + content + "]";
	}

}
