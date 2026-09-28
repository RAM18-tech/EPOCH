package com.dtv.dcp.epoch.model.ct.burn;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OfferBurnResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** The content. */
	@JsonProperty(value = "content", required = false)
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
		builder.append("OfferBurnResponse [content=");
		builder.append(content);
		builder.append("]");
		return builder.toString();
	}
}