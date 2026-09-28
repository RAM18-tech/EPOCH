package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class HSIAInfo
 * @author vd7621
 */
public class HSIAInfo  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The compCode */
	@JsonProperty("compCode")
	private String compCode;

	/** The status */
	@JsonProperty("status")
	private String status;

	/** The state */
	@JsonProperty("state")
	private String state;

	/** The speed */
	@JsonProperty("speed")
	private String speed;
	
	/** The premiumTiers */
	@JsonProperty("premiumTiers")
	private String premiumTiers;

	/** The originalInstallationType */
	@JsonProperty("originalInstallationType")
	private String originalInstallationType;

	/** The numberOfWifiNextGenExtenders */
	@JsonProperty("numberOfWifiNextGenExtenders")
	private int numberOfWifiNextGenExtenders;
	
	/** The currentPromotions. */
	@JsonProperty("currentPromotions")
	private List<CurrentPromotions> currentPromotions;
	
	@JsonProperty("boltOnDetails")
	private BoltOnDetails boltOnDetails;

	/**
	 * @return the compCode
	 */
	public String getCompCode() {
		return compCode;
	}

	/**
	 * @param compCode the compCode to set
	 */
	public void setCompCode(String compCode) {
		this.compCode = compCode;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the speed
	 */
	public String getSpeed() {
		return speed;
	}

	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(String speed) {
		this.speed = speed;
	}

	/**
	 * @return the premiumTiers
	 */
	public String getPremiumTiers() {
		return premiumTiers;
	}

	/**
	 * @param premiumTiers the premiumTiers to set
	 */
	public void setPremiumTiers(String premiumTiers) {
		this.premiumTiers = premiumTiers;
	}

	/**
	 * @return the originalInstallationType
	 */
	public String getOriginalInstallationType() {
		return originalInstallationType;
	}

	/**
	 * @param originalInstallationType the originalInstallationType to set
	 */
	public void setOriginalInstallationType(String originalInstallationType) {
		this.originalInstallationType = originalInstallationType;
	}

	/**
	 * @return the numberOfWifiNextGenExtenders
	 */
	public int getNumberOfWifiNextGenExtenders() {
		return numberOfWifiNextGenExtenders;
	}

	/**
	 * @param numberOfWifiNextGenExtenders the numberOfWifiNextGenExtenders to set
	 */
	public void setNumberOfWifiNextGenExtenders(int numberOfWifiNextGenExtenders) {
		this.numberOfWifiNextGenExtenders = numberOfWifiNextGenExtenders;
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

	/**
	 * @return the boltOnDetails
	 */
	public BoltOnDetails getBoltOnDetails() {
		return boltOnDetails;
	}

	/**
	 * @param boltOnDetails the boltOnDetails to set
	 */
	public void setBoltOnDetails(BoltOnDetails boltOnDetails) {
		this.boltOnDetails = boltOnDetails;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HSIAInfo [compCode=" + compCode + ", status=" + status + ", state=" + state + ", speed=" + speed
				+ ", premiumTiers=" + premiumTiers + ", originalInstallationType=" + originalInstallationType
				+ ", numberOfWifiNextGenExtenders=" + numberOfWifiNextGenExtenders + ", currentPromotions="
				+ currentPromotions + ", boltOnDetails=" + boltOnDetails + "]";
	}
	
}
