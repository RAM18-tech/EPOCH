package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class UnifiedProductDetailsInfo
 * @author vd7621
 */
public class UnifiedProductDetailsInfo implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The IPTVInfo. */
	@JsonProperty("iptv")
	private IPTVInfo iptvInfo;

	/** The HSIAInfo. */
	@JsonProperty("hsia")
	private HSIAInfo hsiaInfo;

	/** The currentPromotions. */
	@JsonProperty("currentPromotions")
	private List<CurrentPromotions> currentPromotions;

	/**
	 * @return the iptvInfo
	 */
	public IPTVInfo getIptvInfo() {
		return iptvInfo;
	}

	/**
	 * @param iptvInfo the iptvInfo to set
	 */
	public void setIptvInfo(IPTVInfo iptvInfo) {
		this.iptvInfo = iptvInfo;
	}

	/**
	 * @return the hsiaInfo
	 */
	public HSIAInfo getHsiaInfo() {
		return hsiaInfo;
	}

	/**
	 * @param hsiaInfo the hsiaInfo to set
	 */
	public void setHsiaInfo(HSIAInfo hsiaInfo) {
		this.hsiaInfo = hsiaInfo;
	}

	/**
	 * @return the currentPromotions
	 */
	public List<CurrentPromotions> getCurrentPromotions() {
		return currentPromotions;
	}

	/**
	 * @param currentPromotions the currentPromotions to set
	 */
	public void setCurrentPromotions(List<CurrentPromotions> currentPromotions) {
		this.currentPromotions = currentPromotions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UnifiedProductDetailsInfo [iptvInfo=" + iptvInfo + ", hsiaInfo=" + hsiaInfo + ", currentPromotions="
				+ currentPromotions + "]";
	}

	
}
