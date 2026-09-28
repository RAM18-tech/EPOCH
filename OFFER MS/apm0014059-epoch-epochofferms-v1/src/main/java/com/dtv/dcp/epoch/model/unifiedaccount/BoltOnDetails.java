package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class BoltOnDetails
 * @author vd7621
 */
public class BoltOnDetails  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The boltOnIndicator. */
	@JsonProperty("boltOnIndicator")
	private boolean boltOnIndicator;

	/** The boltOnList. */
	@JsonProperty("boltOnList")
	private List<BoltOnList> boltOnList;

	/** The availableBoltOnOption. */
	@JsonProperty("availableBoltOnOption")
	private String availableBoltOnOption;

	/** The boltOnUnlimited. */
	@JsonProperty("boltOnUnlimited")
	private boolean boltOnUnlimited;

	/**
	 * @return the boltOnIndicator
	 */
	public boolean isBoltOnIndicator() {
		return boltOnIndicator;
	}

	/**
	 * @param boltOnIndicator the boltOnIndicator to set
	 */
	public void setBoltOnIndicator(boolean boltOnIndicator) {
		this.boltOnIndicator = boltOnIndicator;
	}

	/**
	 * @return the boltOnList
	 */
	public List<BoltOnList> getBoltOnList() {
		return boltOnList;
	}

	/**
	 * @param boltOnList the boltOnList to set
	 */
	public void setBoltOnList(List<BoltOnList> boltOnList) {
		this.boltOnList = boltOnList;
	}

	/**
	 * @return the availableBoltOnOption
	 */
	public String getAvailableBoltOnOption() {
		return availableBoltOnOption;
	}

	/**
	 * @param availableBoltOnOption the availableBoltOnOption to set
	 */
	public void setAvailableBoltOnOption(String availableBoltOnOption) {
		this.availableBoltOnOption = availableBoltOnOption;
	}

	/**
	 * @return the boltOnUnlimited
	 */
	public boolean isBoltOnUnlimited() {
		return boltOnUnlimited;
	}

	/**
	 * @param boltOnUnlimited the boltOnUnlimited to set
	 */
	public void setBoltOnUnlimited(boolean boltOnUnlimited) {
		this.boltOnUnlimited = boltOnUnlimited;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BoltOnDetails [boltOnIndicator=" + boltOnIndicator + ", boltOnList=" + boltOnList
				+ ", availableBoltOnOption=" + availableBoltOnOption + ", boltOnUnlimited=" + boltOnUnlimited + "]";
	}
	
}
