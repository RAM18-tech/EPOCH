package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *This class model class is for response structure for Local Channel Eligibility
 *
 */

/**
 * Created by sn611j.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckEligibilityLocalResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean hasLocalChannels;
	private Boolean isLCCTrialMarketDMA;
	
	public Boolean isHasLocalChannels() {
		return hasLocalChannels;
	}

	public void setHasLocalChannels(Boolean hasLocalChannels) {
		this.hasLocalChannels = hasLocalChannels;
	}
	
	public Boolean isIsLCCTrialMarketDMA() {
		return isLCCTrialMarketDMA;
	}
	
	public void setIsLCCTrialMarketDMA(Boolean isLCCTrialMarketDMA) {
		this.isLCCTrialMarketDMA = isLCCTrialMarketDMA;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CheckEligibilityLocalResponse [hasLocalChannels=");
		builder.append(hasLocalChannels);
		builder.append("]");
		builder.append("CheckEligibilityLocalResponse [isLCCTrialMarketDMA=");
		builder.append(isLCCTrialMarketDMA);
		builder.append("]");
		return builder.toString();
	}	

}