package com.dtv.dcp.epoch.model.unifiedaccount;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * The class CurrentPromotions
 * @author vd7621
 */

public class IPTVInfo  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The  compCode. */
	@JsonProperty("compCode")
	private String compCode;

	/** The  status. */
	@JsonProperty("status")
	private String status;

	/** The  state. */
	@JsonProperty("state")
	private String state;

	/** The  basePackageName. */
	@JsonProperty("basePackageName")
	private String basePackageName;

	/** The  numberOfSetTopBox. */
	@JsonProperty("numberOfSetTopBox")
	private String numberOfSetTopBox;
	
	/** The  programingChannel. */
	@JsonProperty("programingChannels")
	private List<String> programingChannel;

	/** The  receiverInfoList. */
	@JsonProperty("receiverInfoList")
	private ReceiverInfoList receiverInfoList;

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
	 * @return the basePackageName
	 */
	public String getBasePackageName() {
		return basePackageName;
	}

	/**
	 * @param basePackageName the basePackageName to set
	 */
	public void setBasePackageName(String basePackageName) {
		this.basePackageName = basePackageName;
	}

	/**
	 * @return the numberOfSetTopBox
	 */
	public String getNumberOfSetTopBox() {
		return numberOfSetTopBox;
	}

	/**
	 * @param numberOfSetTopBox the numberOfSetTopBox to set
	 */
	public void setNumberOfSetTopBox(String numberOfSetTopBox) {
		this.numberOfSetTopBox = numberOfSetTopBox;
	}

	/**
	 * @return the programingChannel
	 */
	public List<String> getProgramingChannel() {
		return programingChannel;
	}

	/**
	 * @param programingChannel the programingChannel to set
	 */
	public void setProgramingChannel(List<String> programingChannel) {
		this.programingChannel = programingChannel;
	}

	/**
	 * @return the receiverInfoList
	 */
	public ReceiverInfoList getReceiverInfoList() {
		return receiverInfoList;
	}

	/**
	 * @param receiverInfoList the receiverInfoList to set
	 */
	public void setReceiverInfoList(ReceiverInfoList receiverInfoList) {
		this.receiverInfoList = receiverInfoList;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "IPTVInfo [compCode=" + compCode + ", status=" + status + ", state=" + state + ", basePackageName="
				+ basePackageName + ", numberOfSetTopBox=" + numberOfSetTopBox + ", programingChannel="
				+ programingChannel + ", receiverInfoList=" + receiverInfoList
				+ "]";
	}

	
}
