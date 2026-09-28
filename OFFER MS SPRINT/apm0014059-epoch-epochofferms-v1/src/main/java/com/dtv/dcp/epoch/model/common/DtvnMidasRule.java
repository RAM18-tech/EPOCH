package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The Class DtvnMidasRuleInfo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DtvnMidasRule implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The rule code. */
	private String ruleCode;
	
	/** The rule id. */
	private String ruleId;
	
	/** The sales channel. */
	private String salesChannel;
	
	/** The existing group. */
	private String existingGroup;
	
	/** The available group. */
	private String availableGroup;
	
	/** The lead group. */
	private Long leadGroup;
	
	/** The agent type. */
	private String agentType;
	
	/** The catalog product name. */
	private String catalogProductName;
	
	/** The contracted package. */
	private String contractedPackage;
	
	/** The Contract Intent. */
	private String contractIntent;
	
	/**
	 * Gets the contracted package.
	 *
	 * @return the contracted package
	 */
	public String getContractedPackage() {
		return contractedPackage;
	}

	/**
	 * Sets the contracted package.
	 *
	 * @param contractedPackage the new contracted package
	 */
	public void setContractedPackage(String contractedPackage) {
		this.contractedPackage = contractedPackage;
	}

	/**
	 * Gets the rule code.
	 *
	 * @return the rule code
	 */
	public String getRuleCode() {
		return ruleCode;
	}
	
	/**
	 * Sets the rule code.
	 *
	 * @param ruleCode the new rule code
	 */
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}
	
	/**
	 * Gets the rule id.
	 *
	 * @return the rule id
	 */
	public String getRuleId() {
		return ruleId;
	}
	
	/**
	 * Sets the rule id.
	 *
	 * @param ruleId the new rule id
	 */
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	
	/**
	 * Gets the sales channel.
	 *
	 * @return the sales channel
	 */
	public String getSalesChannel() {
		return salesChannel;
	}
	
	/**
	 * Sets the sales channel.
	 *
	 * @param salesChannel the new sales channel
	 */
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}
	
	/**
	 * Gets the existing group.
	 *
	 * @return the existing group
	 */
	public String getExistingGroup() {
		return existingGroup;
	}
	
	/**
	 * Sets the existing group.
	 *
	 * @param existingGroup the new existing group
	 */
	public void setExistingGroup(String existingGroup) {
		this.existingGroup = existingGroup;
	}
	
	/**
	 * Gets the available group.
	 *
	 * @return the available group
	 */
	public String getAvailableGroup() {
		return availableGroup;
	}
	
	/**
	 * Sets the available group.
	 *
	 * @param availableGroup the new available group
	 */
	public void setAvailableGroup(String availableGroup) {
		this.availableGroup = availableGroup;
	}
	
	
	/**
	 * Gets the lead group.
	 *
	 * @return the lead group
	 */
	public Long getLeadGroup() {
		return leadGroup;
	}

	/**
	 * Sets the lead group.
	 *
	 * @param leadGroup the new lead group
	 */
	public void setLeadGroup(Long leadGroup) {
		this.leadGroup = leadGroup;
	}

	/**
	 * Gets the agent type.
	 *
	 * @return the agent type
	 */
	public String getAgentType() {
		return agentType;
	}
	
	/**
	 * Sets the agent type.
	 *
	 * @param agentType the new agent type
	 */
	public void setAgentType(String agentType) {
		this.agentType = agentType;
	}
	
	/**
	 * Gets the catalog product name.
	 *
	 * @return the catalog product name
	 */
	public String getCatalogProductName() {
		return catalogProductName;
	}
	
	/**
	 * Sets the catalog product name.
	 *
	 * @param catalogProductName the new catalog product name
	 */
	public void setCatalogProductName(String catalogProductName) {
		this.catalogProductName = catalogProductName;
	}
	

	/**
	 * @return the ContractIntent
	 */
	public String getContractIntent() {
		return contractIntent;
	}

	/**
	 * @param ContractIntent the ContractIntent to set
	 */
	public void setContractIntent(String contractIntent) {
		this.contractIntent = contractIntent;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DtvnMidasRuleInfo [ruleCode=");
		builder.append(ruleCode);
		builder.append(", ruleId=");
		builder.append(ruleId);
		builder.append(", salesChannel=");
		builder.append(salesChannel);
		builder.append(", existingGroup=");
		builder.append(existingGroup);
		builder.append(", availableGroup=");
		builder.append(availableGroup);
		builder.append(", leadGroup=");
		builder.append(leadGroup);
		builder.append(", agentType=");
		builder.append(agentType);
		builder.append(", catalogProductName=");
		builder.append(catalogProductName);
		builder.append(", contractedPackage=");
		builder.append(contractedPackage);
		builder.append(", contractIntent=");
		builder.append(contractIntent);
		builder.append("]");
		return builder.toString();
	}
}
