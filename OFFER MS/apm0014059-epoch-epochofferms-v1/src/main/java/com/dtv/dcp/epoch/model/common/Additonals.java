package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.customergraph.EligibleWirelessAccount;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class Additonals.
 */
@ApiModel(value = "Additonals")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Additonals implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The alerts. */
	@ApiModelProperty(value = "List of Alerts")
	@JsonProperty("alerts")
	private List<Alert> alerts;
	
	/** The eligibleWirelessAccount. */
	@ApiModelProperty(value = "Eligible Wireless Account")
	@JsonProperty("eligibleWirelessAccount")
	private EligibleWirelessAccount eligibleWirelessAccount;
	
	
	/** The bundleContext. */
	@JsonProperty("bundleContext")
	private List<String> bundleContext;
	
	/**
	 * Gets the eligible wireless account.
	 *
	 * @return the eligible wireless account
	 */
	public EligibleWirelessAccount getEligibleWirelessAccount() {
	return eligibleWirelessAccount;
	}

	/**
	 * Sets the eligible wireless account.
	 *
	 * @param eligibleWirelessAccount the new eligible wireless account
	 */
	public void setEligibleWirelessAccount(EligibleWirelessAccount eligibleWirelessAccount) {
	this.eligibleWirelessAccount = eligibleWirelessAccount;
	}

	/**
	 * Gets the alerts.
	 *
	 * @return the alerts
	 */
	public List<Alert> getAlerts() {
		return alerts;
	}

	/**
	 * Sets the alerts.
	 *
	 * @param alerts the new alerts
	 */
	public void setAlerts(List<Alert> alerts) {
		this.alerts = alerts;
	}

	

	public List<String> getBundleContext() {
		return bundleContext;
	}

	public void setBundleContext(List<String> bundleContext) {
		this.bundleContext = bundleContext;
	}


}
