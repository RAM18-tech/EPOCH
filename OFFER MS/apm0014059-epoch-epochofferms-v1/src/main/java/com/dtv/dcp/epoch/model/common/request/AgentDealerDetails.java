package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AgentDealerDetails implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private String agentId;
	private String dealerId1;
	private String dealerId2;

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}

	public String getDealerId1() {
		return dealerId1;
	}

	public void setDealerId1(String dealerId1) {
		this.dealerId1 = dealerId1;
	}

	public String getDealerId2() {
		return dealerId2;
	}

	public void setDealerId2(String dealerId2) {
		this.dealerId2 = dealerId2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(agentId, dealerId1, dealerId2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentDealerDetails other = (AgentDealerDetails) obj;
		return Objects.equals(agentId, other.agentId) && Objects.equals(dealerId1, other.dealerId1)
				&& Objects.equals(dealerId2, other.dealerId2);
	}

	@Override
	public String toString() {
		return "AgentDealerDetails [agentId=" + agentId + ", dealerId1=" + dealerId1 + ", dealerId2=" + dealerId2 + "]";
	}

}