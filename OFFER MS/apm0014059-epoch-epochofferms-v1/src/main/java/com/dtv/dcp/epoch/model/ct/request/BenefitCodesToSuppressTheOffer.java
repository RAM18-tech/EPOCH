/**
 * 
 */
package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;

import com.dtv.dcp.epoch.common.Constants;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author pradiptaa.paul
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BenefitCodesToSuppressTheOffer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(Constants.BENEFITCODE)
	private String benefitCode;
	@JsonProperty(Constants.STARTDATE)
	private String startDate;
	@JsonProperty(Constants.ENDDATE)
	private String endDate;
	/**
	 * @return the benefitCode
	 */
	public String getBenefitCode() {
		return benefitCode;
	}
	/**
	 * @param benefitCode the benefitCode to set
	 */
	public void setBenefitCode(String benefitCode) {
		this.benefitCode = benefitCode;
	}
	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "benefitCodesToSuppressTheOffer : { benefitCode=" + benefitCode + ", startDate=" + startDate + ", endDate="
				+ endDate + "}";
	}

}
