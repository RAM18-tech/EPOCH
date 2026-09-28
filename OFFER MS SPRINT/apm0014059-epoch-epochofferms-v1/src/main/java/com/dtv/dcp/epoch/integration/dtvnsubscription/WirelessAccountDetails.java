
package com.dtv.dcp.epoch.integration.dtvnsubscription;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * The Class SubscriptionRequest.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WirelessAccountDetails {
	
	/** The wireless. */
	@JsonProperty("accountType")
	private String accountType;
	
	/** The account number. */
	@JsonProperty("accountNumber")
	private String accountNumber;

	/** The subscriber number. */
	@JsonProperty("subscriberNumber")
	private String subscriberNumber;

	/**
	 * Gets the account type.
	 *
	 * @return the account type
	 */
	public String getAccountType() {
		return accountType;
	}

	/**
	 * Sets the account type.
	 *
	 * @param accountType the new account type
	 */
	public void setAccountType(String accountType) {
		this.accountType = accountType;
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

	/*
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SubscriptionRequest [accountType=");
		builder.append(accountType);
		builder.append(", accountNumber=");
		builder.append(accountNumber);
		builder.append(", subscriberNumber=");
		builder.append(subscriberNumber);
		builder.append("]");
		return builder.toString();
	}
	

	
}
