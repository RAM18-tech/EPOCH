package com.dtv.dcp.epoch.model.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentChannelDetails implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String channel;
	private String subChannel;
	private String storeId;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSubChannel() {
		return subChannel;
	}

	public void setSubChannel(String subChannel) {
		this.subChannel = subChannel;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(channel, storeId, subChannel);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentChannelDetails other = (AgentChannelDetails) obj;
		return Objects.equals(channel, other.channel) && Objects.equals(storeId, other.storeId)
				&& Objects.equals(subChannel, other.subChannel);
	}

	@Override
	public String toString() {
		return "AgentChannelDetails [channel=" + channel + ", subChannel=" + subChannel + ", storeId=" + storeId + "]";
	}

}