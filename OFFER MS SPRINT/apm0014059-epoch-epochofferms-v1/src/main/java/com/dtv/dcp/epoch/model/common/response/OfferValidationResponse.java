package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.response.Messages;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This class model class is for response structure for bundle offer
 *
 */

/**
 * Created by nk3077 on 05/08/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferValidationResponse implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	@JsonProperty("messages")
	private List<Messages> messages;
	/**
	 * @return the messages
	 */
	public List<Messages> getMessages() {
		return messages;
	}

	/**
	 * @param messages
	 *            the messages to set
	 */
	public void setMessages(List<Messages> messages) {
		this.messages = messages;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OfferValidationResponse [" + (messages != null ? "messages=" + messages + ", " : "") + "]";
	}

}