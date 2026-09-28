package com.dtv.dcp.epoch.model.cache;

import java.io.Serializable;
import java.util.List;

/**
 * The Class CacheEvictionResponse.
 */
public class CacheEvictionResponse implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The status. */
	private List<CacheEvictionStatusBean> status;

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public List<CacheEvictionStatusBean> getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(List<CacheEvictionStatusBean> status) {
		this.status = status;
	}

	/**
	 * toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CacheEvictionResponse []");
		return builder.toString();
	}
}