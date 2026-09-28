package com.dtv.dcp.epoch.model.common;


import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Context.
 * @author dr000y
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Context implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    @JsonProperty("channel")
    private String channel;
    
    @JsonProperty("location")
    private Location location;

	/**
	 * @return the channel
	 */
	public String getChannel() {
		return channel;
	}
	/**
	 * @param channel the channel to set
	 */
	public void setChannel(String channel) {
		this.channel = channel;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}
	/**
	 * @param location the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Context [channel=");
		builder.append(channel);
		builder.append(" ,location=");
		builder.append(location);
		builder.append("]");
		return builder.toString();
	}

}
