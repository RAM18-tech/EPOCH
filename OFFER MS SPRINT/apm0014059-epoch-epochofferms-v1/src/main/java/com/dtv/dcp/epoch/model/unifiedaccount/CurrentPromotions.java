package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class CurrentPromotions
 * @author vd7621
 */
public class CurrentPromotions  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The name. */
	@JsonProperty("name")
	private String name;

	/** The description. */
	@JsonProperty("description")
	private String description;

	/** The pricePlanCode. */
	@JsonProperty("pricePlanCode")
	private String pricePlanCode;

	/** The promoCrossPkgInd. */
	@JsonProperty("promoCrossPkgInd")
	private String promoCrossPkgInd;

	/** The effectiveDate. */
	@JsonProperty("effectiveDate")
	private String effectiveDate;

	/** The pricePlanType. */
	@JsonProperty("pricePlanType")
	private String pricePlanType;

	/** The applicableService. */
	@JsonProperty("applicableService")
	private String applicableService;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the pricePlanCode
	 */
	public String getPricePlanCode() {
		return pricePlanCode;
	}

	/**
	 * @param pricePlanCode the pricePlanCode to set
	 */
	public void setPricePlanCode(String pricePlanCode) {
		this.pricePlanCode = pricePlanCode;
	}

	/**
	 * @return the promoCrossPkgInd
	 */
	public String getPromoCrossPkgInd() {
		return promoCrossPkgInd;
	}

	/**
	 * @param promoCrossPkgInd the promoCrossPkgInd to set
	 */
	public void setPromoCrossPkgInd(String promoCrossPkgInd) {
		this.promoCrossPkgInd = promoCrossPkgInd;
	}

	/**
	 * @return the effectiveDate
	 */
	public String getEffectiveDate() {
		return effectiveDate;
	}

	/**
	 * @param effectiveDate the effectiveDate to set
	 */
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	/**
	 * @return the pricePlanType
	 */
	public String getPricePlanType() {
		return pricePlanType;
	}

	/**
	 * @param pricePlanType the pricePlanType to set
	 */
	public void setPricePlanType(String pricePlanType) {
		this.pricePlanType = pricePlanType;
	}

	/**
	 * @return the applicableService
	 */
	public String getApplicableService() {
		return applicableService;
	}

	/**
	 * @param applicableService the applicableService to set
	 */
	public void setApplicableService(String applicableService) {
		this.applicableService = applicableService;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "CurrentPromotions [name=" + name + ", description=" + description + ", pricePlanCode=" + pricePlanCode
				+ ", promoCrossPkgInd=" + promoCrossPkgInd + ", effectiveDate=" + effectiveDate + ", pricePlanType="
				+ pricePlanType + ", applicableService=" + applicableService + "]";
	}

}
