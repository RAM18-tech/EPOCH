package com.dtv.dcp.epoch.model.cache;

import java.io.Serializable;
import java.util.List;

/**
 * The Class CacheResponse.
 */
public class CacheResponse implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The cache. */
	private List<CacheBean> cache;
	
	/** The messgae. */
	private String message;

	/**
	 * Gets the cache.
	 *
	 * @return the cache
	 */
	public List<CacheBean> getCache() {
		return cache;
	}

	/**
	 * Sets the cache.
	 *
	 * @param cache the new cache
	 */
	public void setCache(List<CacheBean> cache) {
		this.cache = cache;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheResponse [cache=");
		builder.append(cache);
		builder.append(", message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}
}