/**
 * 
 */
package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author nf2008
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PauseEligibilityDetails implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("isEligibleForServicePause")
	private Boolean eligibleForServicePause;
	
	@JsonProperty("isEligibleToExtendPause")
	private Boolean eligibleToExtendPause;
	
	private String pausePeriod;
	
	private List<String> pauseIntervals;

	/**
	 * @return the eligibleForServicePause
	 */
	public Boolean getEligibleForServicePause() {
		return eligibleForServicePause;
	}

	/**
	 * @param eligibleForServicePause the eligibleForServicePause to set
	 */
	public void setEligibleForServicePause(Boolean eligibleForServicePause) {
		this.eligibleForServicePause = eligibleForServicePause;
	}

	/**
	 * @return the eligibleToExtendPause
	 */
	public Boolean getEligibleToExtendPause() {
		return eligibleToExtendPause;
	}

	/**
	 * @param eligibleToExtendPause the eligibleToExtendPause to set
	 */
	public void setEligibleToExtendPause(Boolean eligibleToExtendPause) {
		this.eligibleToExtendPause = eligibleToExtendPause;
	}

	/**
	 * @return the pausePeriod
	 */
	public String getPausePeriod() {
		return pausePeriod;
	}

	/**
	 * @param pausePeriod the pausePeriod to set
	 */
	public void setPausePeriod(String pausePeriod) {
		this.pausePeriod = pausePeriod;
	}

	/**
	 * @return the pauseIntervals
	 */
	public List<String> getPauseIntervals() {
		return pauseIntervals;
	}

	/**
	 * @param pauseIntervals the pauseIntervals to set
	 */
	public void setPauseIntervals(List<String> pauseIntervals) {
		this.pauseIntervals = pauseIntervals;
	}
	
	

}
