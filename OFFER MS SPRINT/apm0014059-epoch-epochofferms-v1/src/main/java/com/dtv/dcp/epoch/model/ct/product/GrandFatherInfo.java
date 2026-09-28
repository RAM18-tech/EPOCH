package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class GrandFatherInfo implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	private String salesEffectiveDate;
	
	private String salesExpirationDate;
	
	private Integer version;
	
	/**
	 * @return the salesEffectiveDate
	 */
	public String getSalesEffectiveDate() {
		return salesEffectiveDate;
	}
	/**
	 * @param salesEffectiveDate the salesEffectiveDate to set
	 */
	public void setSalesEffectiveDate(String salesEffectiveDate) {
		this.salesEffectiveDate = salesEffectiveDate;
	}
	/**
	 * @return the salesExpirationDate
	 */
	public String getSalesExpirationDate() {
		return salesExpirationDate;
	}
	/**
	 * @param salesExpirationDate the salesExpirationDate to set
	 */
	public void setSalesExpirationDate(String salesExpirationDate) {
		this.salesExpirationDate = salesExpirationDate;
	}
	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
	@Override
	public String toString() {
		return "GrandFatherInfo [salesEffectiveDate=" + salesEffectiveDate + ", salesExpirationDate="
				+ salesExpirationDate + ", version=" + version + "]";
	}
}
