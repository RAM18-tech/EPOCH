package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RibbonTextByKey implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String agentRibbonText, agentRibbonTextForServices, onlineRibbonText, onlineRibbonTextForServices;

	public String getAgentRibbonText() {
		return agentRibbonText;
	}

	public void setAgentRibbonText(String agentRibbonText) {
		this.agentRibbonText = agentRibbonText;
	}

	public String getAgentRibbonTextForServices() {
		return agentRibbonTextForServices;
	}

	public void setAgentRibbonTextForServices(String agentRibbonTextForServices) {
		this.agentRibbonTextForServices = agentRibbonTextForServices;
	}

	public String getOnlineRibbonText() {
		return onlineRibbonText;
	}

	public void setOnlineRibbonText(String onlineRibbonText) {
		this.onlineRibbonText = onlineRibbonText;
	}

	public String getOnlineRibbonTextForServices() {
		return onlineRibbonTextForServices;
	}

	public void setOnlineRibbonTextForServices(String onlineRibbonTextForServices) {
		this.onlineRibbonTextForServices = onlineRibbonTextForServices;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}