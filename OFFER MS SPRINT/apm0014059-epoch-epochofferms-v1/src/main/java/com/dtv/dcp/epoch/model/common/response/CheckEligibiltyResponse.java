/**
 * 
 */
package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author nf2008
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckEligibiltyResponse implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private PauseEligibilityDetails pauseEligibilityDetails;

	/**
	 * @return the pauseEligibilityDetails
	 */
	public PauseEligibilityDetails getPauseEligibilityDetails() {
		return pauseEligibilityDetails;
	}

	/**
	 * @param pauseEligibilityDetails
	 *            the pauseEligibilityDetails to set
	 */
	public void setPauseEligibilityDetails(PauseEligibilityDetails pauseEligibilityDetails) {
		this.pauseEligibilityDetails = pauseEligibilityDetails;
	}

	private SwimlaneEligibilityDetails swimlaneEligibilityDetails;

	public SwimlaneEligibilityDetails getSwimlaneEligibilityDetails() {
		return swimlaneEligibilityDetails;
	}

	public void setSwimlaneEligibilityDetails(SwimlaneEligibilityDetails swimlaneEligibilityDetails) {
		this.swimlaneEligibilityDetails = swimlaneEligibilityDetails;
	}
}
