package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class UnifiedProductDetailsRequest
 * @author vd7621
 */
public class UnifiedProductDetailsRequest implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The unifiedBans. */
	@JsonProperty(value="unifiedBans")
	private List<String> unifiedBans;

	/** The productStatuses. */
	@JsonProperty(value="productStatuses")
	private List<String> productStatuses;
	
	/** The productStates. */
	@JsonProperty(value="productStates")
	private List<String> productStates;

	/**
	 * @return the unifiedBans
	 */
	public List<String> getUnifiedBans() {
		return unifiedBans;
	}

	/**
	 * @param unifiedBans the unifiedBans to set
	 */
	public void setUnifiedBans(List<String> unifiedBans) {
		this.unifiedBans = unifiedBans;
	}

	/**
	 * @return the productStatuses
	 */
	public List<String> getProductStatuses() {
		return productStatuses;
	}

	/**
	 * @param productStatuses the productStatuses to set
	 */
	public void setProductStatuses(List<String> productStatuses) {
		this.productStatuses = productStatuses;
	}

	/**
	 * @return the productStates
	 */
	public List<String> getProductStates() {
		return productStates;
	}

	/**
	 * @param productStates the productStates to set
	 */
	public void setProductStates(List<String> productStates) {
		this.productStates = productStates;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UnifiedProductDetailsRequest [unifiedBans=" + unifiedBans + ", productStatuses=" + productStatuses
				+ ", productStates=" + productStates + "]";
	}
	
}
