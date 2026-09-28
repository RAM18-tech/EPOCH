package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *This class model class is for Local Channel Eligibility request.
 *
 */

/**
 * Created by sn611j
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CheckLocalChannelEligibilityRequest implements Serializable { 
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/**
     * The customerEligibility.
     */
    private CustomerEligibility customerEligibility;

	public CustomerEligibility getCustomerEligibility() {
		return customerEligibility;
	}

	public void setCustomerEligibility(CustomerEligibility customerEligibility) {
		this.customerEligibility = customerEligibility;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "checkLocalChannelEligibilityRequest {" + (customerEligibility != null ? "customerEligibility=" + customerEligibility + ", " : "")+ "}";
	}
	

}