package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The class ReceiverInfoList
 * @author vd7621
 */
public class ReceiverInfoList  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The wifiDvrList. */
	@JsonProperty("wifiDvrList")
	private List<String> wifiDvrList;

	/** The regularDvrList. */
	@JsonProperty("regularDvrList")
	private List<RegularDvrList> regularDvrList;

	/** The xboxList. */
	@JsonProperty("xboxList")
	private List<String> xboxList;

	/** The wifiStbList. *//*
	@JsonProperty("wifiStbList")
	private List<String> wifiStbList;

	*//** The stbList. *//*
	@JsonProperty("stbList")
	private List<String> stbList;*/

	/**
	 * @return the wifiDvrList
	 */
	public List<String> getWifiDvrList() {
		return wifiDvrList;
	}

	/**
	 * @param wifiDvrList the wifiDvrList to set
	 */
	public void setWifiDvrList(List<String> wifiDvrList) {
		this.wifiDvrList = wifiDvrList;
	}

	/**
	 * @return the regularDvrList
	 */
	public List<RegularDvrList> getRegularDvrList() {
		return regularDvrList;
	}

	/**
	 * @param regularDvrList the regularDvrList to set
	 */
	public void setRegularDvrList(List<RegularDvrList> regularDvrList) {
		this.regularDvrList = regularDvrList;
	}

	/**
	 * @return the xboxList
	 */
	public List<String> getXboxList() {
		return xboxList;
	}

	/**
	 * @param xboxList the xboxList to set
	 */
	public void setXboxList(List<String> xboxList) {
		this.xboxList = xboxList;
	}

/*	*//**
	 * @return the wifiStbList
	 *//*
	public List<String> getWifiStbList() {
		return wifiStbList;
	}

	*//**
	 * @param wifiStbList the wifiStbList to set
	 *//*
	public void setWifiStbList(List<String> wifiStbList) {
		this.wifiStbList = wifiStbList;
	}

	*//**
	 * @return the stbList
	 *//*
	public List<String> getStbList() {
		return stbList;
	}

	*//**
	 * @param stbList the stbList to set
	 *//*
	public void setStbList(List<String> stbList) {
		this.stbList = stbList;
	}*/

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ReceiverInfoList [wifiDvrList=" + wifiDvrList + ", regularDvrList=" + regularDvrList 
				+ ", xboxList=" + xboxList 
				//+ ", wifiStbList=" + wifiStbList + ", stbList=" + stbList 
				+ "]";
	}
}
