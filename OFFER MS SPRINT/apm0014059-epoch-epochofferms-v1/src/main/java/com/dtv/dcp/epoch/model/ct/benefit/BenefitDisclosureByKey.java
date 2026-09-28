package com.dtv.dcp.epoch.model.ct.benefit;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BenefitDisclosureByKey implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** opusDisclosureMessage */
	@JsonProperty("opusDisclosure")
	private String opusDisclosureMessage;

	/** opusDisclosureForServices */
	@JsonProperty("opusDisclosureForServices")
	private String opusDisclosureForServices;
	
	/** myAttDisclosureMessage */
	@JsonProperty("onlineDisclosure")
	private String myAttDisclosureMessage;
	
	/** myATTDisclosureForServices */
	@JsonProperty("onlineDisclosureForServices")
	private String myATTDisclosureForServices;
	@JsonProperty("disclosure")
	private String disclosure;
	@JsonProperty("shortDisclosure")
	private String shortDisclosure;

	public String getShortDisclosure() {
		return shortDisclosure;
	}
	public void setShortDisclosure(String shortDisclosure) {
		this.shortDisclosure = shortDisclosure;
	}

	public String getDisclosure() {
		return disclosure;
	}

	public void setDisclosure(String disclosure) {
		this.disclosure = disclosure;
	}

	public String getOpusDisclosureMessage() {
		return opusDisclosureMessage;
	}

	public void setOpusDisclosureMessage(String opusDisclosureMessage) {
		this.opusDisclosureMessage = opusDisclosureMessage;
	}

	public String getOpusDisclosureForServices() {
		return opusDisclosureForServices;
	}

	public void setOpusDisclosureForServices(String opusDisclosureForServices) {
		this.opusDisclosureForServices = opusDisclosureForServices;
	}

	public String getMyAttDisclosureMessage() {
		return myAttDisclosureMessage;
	}

	public void setMyAttDisclosureMessage(String myAttDisclosureMessage) {
		this.myAttDisclosureMessage = myAttDisclosureMessage;
	}

	public String getMyATTDisclosureForServices() {
		return myATTDisclosureForServices;
	}

	public void setMyATTDisclosureForServices(String myATTDisclosureForServices) {
		this.myATTDisclosureForServices = myATTDisclosureForServices;
	}

	
}