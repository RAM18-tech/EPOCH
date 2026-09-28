package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class RemovalRule implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The channel. */
	private List<String> channel;
	
	
	/** The accountType. */
	private List<String> accountType;


	/**
	 * @return the channel
	 */
	public List<String> getChannel() {
		return channel;
	}


	/**
	 * @param channel the channel to set
	 */
	public void setChannel(List<String> channel) {
		this.channel = channel;
	}


	/**
	 * @return the accountType
	 */
	public List<String> getAccountType() {
		return accountType;
	}


	/**
	 * @param accountType the accountType to set
	 */
	public void setAccountType(List<String> accountType) {
		this.accountType = accountType;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RemovalRule [channel=");
		builder.append(channel);
		builder.append(", accountType=");
		builder.append(accountType);
		builder.append("]");
		return builder.toString();
	}
	
	
}
