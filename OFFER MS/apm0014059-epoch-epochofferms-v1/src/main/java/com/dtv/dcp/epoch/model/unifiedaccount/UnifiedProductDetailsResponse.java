package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class UnifiedProductDetailsResponse
 * @author vd7621
 */
public class UnifiedProductDetailsResponse implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The content. */
	@JsonProperty(value="content", required=false)
	private Content content;

	/**
	 * @return
	 */
	public Content getContent() {
		return content;
	}

	/**
	 * @param content
	 */
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UnifiedServiceResponse [content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}
	
}
