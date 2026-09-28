package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisclosureMessagesByKey implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String onlineDisclosure, opusDisclosure, opusDisclosureForServices, onlineDisclosureForServices, disclosure;

	@JsonProperty("opt-in")
	private String optIn;

	@JsonProperty("opt-out")
	private String optOut;

	@JsonProperty("auto-renew")
	private String autoRenew;

	private String selectAllDisclosure;
	private String shortDisclosure;
	public String getShortDisclosure() {
		return shortDisclosure;
	}
	public void setShortDisclosure(String shortDisclosure) {
		this.shortDisclosure = shortDisclosure;
	}

	public String getSelectAllDisclosure() {
		return selectAllDisclosure;
	}
	public void setSelectAllDisclosure(String assistedSalesSalesSelectAllDisclosure) {
		this.selectAllDisclosure = assistedSalesSalesSelectAllDisclosure;
	}

	public String getOnlineDisclosure() {
		return onlineDisclosure;
	}

	public void setOnlineDisclosure(String onlineDisclosure) {
		this.onlineDisclosure = onlineDisclosure;
	}

	public String getOpusDisclosure() {
		return opusDisclosure;
	}

	public void setOpusDisclosure(String opusDisclosure) {
		this.opusDisclosure = opusDisclosure;
	}

	public String getOpusDisclosureForServices() {
		return opusDisclosureForServices;
	}

	public void setOpusDisclosureForServices(String opusDisclosureForServices) {
		this.opusDisclosureForServices = opusDisclosureForServices;
	}

	public String getOnlineDisclosureForServices() {
		return onlineDisclosureForServices;
	}

	public void setOnlineDisclosureForServices(String onlineDisclosureForServices) {
		this.onlineDisclosureForServices = onlineDisclosureForServices;
	}

	public String getDisclosure() {
		return disclosure;
	}

	public void setDisclosure(String disclosure) {
		this.disclosure = disclosure;
	}

	public String getOptIn() {
		return optIn;
	}

	public void setOptIn(String optIn) {
		this.optIn = optIn;
	}

	public String getOptOut() {
		return optOut;
	}

	public void setOptOut(String optOut) {
		this.optOut = optOut;
	}

	public String getAutoRenew() {
		return autoRenew;
	}

	public void setAutoRenew(String autoRenew) {
		this.autoRenew = autoRenew;
	}

	/**
	 * Checks if all disclosure-related fields are blank.
	 *
	 * IMPORTANT: If new fields are added to this class in the future,
	 * make sure to update this method accordingly to include those fields
	 * in the emptiness check.
	 */
	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isBlank(onlineDisclosure)
				&& StringUtils.isBlank(opusDisclosure)
				&& StringUtils.isBlank(opusDisclosureForServices)
				&& StringUtils.isBlank(onlineDisclosureForServices)
				&& StringUtils.isBlank(disclosure)
				&& StringUtils.isBlank(optIn)
				&& StringUtils.isBlank(optOut)
				&& StringUtils.isBlank(autoRenew)
				&& StringUtils.isBlank(selectAllDisclosure)
				&& StringUtils.isBlank(shortDisclosure);
	}

}
