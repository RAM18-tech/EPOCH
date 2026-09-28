package com.dtv.dcp.epoch.model.customergraph;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class Alert.
 */
@ApiModel(value = "EligibleWirelessAccount")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EligibleWirelessAccount implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The accountNumber. */
	@ApiModelProperty(value = "Account Number")
	@JsonProperty("accountNumber")
	private String accountNumber;
	
	/** The subscriberNumber. */
	@ApiModelProperty(value = "Subscriber Number")
	@JsonProperty("subscriberNumber")
	private String subscriberNumber;
	
	/** The groupId. */
	@ApiModelProperty(value = "Group Id")
	@JsonProperty("groupId")
	private String groupId;

	/**  The eligibleSocList. */
	@JsonProperty("customerSOCList")
	private List<String> customerSOCList;
	
	@JsonProperty("accountSocList")
	private Map<String, Map<String, List<String>>> accountSocList;
	
	/**
	 * Gets the customer SOC list.
	 *
	 * @return the customer SOC list
	 */
	public List<String> getCustomerSOCList() {
		return customerSOCList;
	}

	/**
	 * Sets the customer SOC list.
	 *
	 * @param customerSOCList the new customer SOC list
	 */
	public void setCustomerSOCList(List<String> customerSOCList) {
		this.customerSOCList = customerSOCList;
	}

	/**
	 * Gets the account number.
	 *
	 * @return the account number
	 */
	public String getAccountNumber() {
	return accountNumber;
	}

	/**
	 * Sets the account number.
	 *
	 * @param accountNumber the new account number
	 */
	public void setAccountNumber(String accountNumber) {
	this.accountNumber = accountNumber;
	}

	/**
	 * Gets the subscriber number.
	 *
	 * @return the subscriber number
	 */
	public String getSubscriberNumber() {
	return subscriberNumber;
	}

	/**
	 * Sets the subscriber number.
	 *
	 * @param subscriberNumber the new subscriber number
	 */
	public void setSubscriberNumber(String subscriberNumber) {
	this.subscriberNumber = subscriberNumber;
	}

	/**
	 * Gets the group id.
	 *
	 * @return the group id
	 */
	public String getGroupId() {
	return groupId;
	}

	/**
	 * Sets the group id.
	 *
	 * @param groupId the new group id
	 */
	public void setGroupId(String groupId) {
	this.groupId = groupId;
	}

	@Override
	public String toString() {
		return "EligibleWirelessAccount [accountNumber=" + accountNumber + ", subscriberNumber=" + subscriberNumber
				+ ", groupId=" + groupId + ", customerSOCList=" + customerSOCList + ", accountSocList=" + accountSocList
				+ "]";
	}

	public Map<String, Map<String, List<String>>> getAccountSocList() {
		return accountSocList;
	}

	public void setAccountSocList(Map<String, Map<String, List<String>>> accountSocList) {
		this.accountSocList = accountSocList;
	}

}
