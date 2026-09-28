package com.dtv.dcp.epoch.model.common.dtvnaccount;

/**
 * The Class AccountResponse.
 */
public class AccountResponse {

	/** The response code. */
	private String responseCode;
	
	/** The message. */
	private String message;
	
	/** The subscribable. */
	private Boolean subscribable;
	
	/** The current account status. */
	private String currentAccountStatus;

	/**
	 * Gets the response code.
	 *
	 * @return the response code
	 */
	public String getResponseCode() {
	return responseCode;
	}

	/**
	 * Sets the response code.
	 *
	 * @param responseCode the new response code
	 */
	public void setResponseCode(String responseCode) {
	this.responseCode = responseCode;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
	return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the new message
	 */
	public void setMessage(String message) {
	this.message = message;
	}

	/**
	 * Gets the subscribable.
	 *
	 * @return the subscribable
	 */
	public Boolean getSubscribable() {
	return subscribable;
	}

	/**
	 * Sets the subscribable.
	 *
	 * @param subscribable the new subscribable
	 */
	public void setSubscribable(Boolean subscribable) {
	this.subscribable = subscribable;
	}

	/**
	 * Gets the current account status.
	 *
	 * @return the current account status
	 */
	public String getCurrentAccountStatus() {
	return currentAccountStatus;
	}

	/**
	 * Sets the current account status.
	 *
	 * @param currentAccountStatus the new current account status
	 */
	public void setCurrentAccountStatus(String currentAccountStatus) {
	this.currentAccountStatus = currentAccountStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AccountResponse [responseCode=");
		builder.append(responseCode);
		builder.append(", message=");
		builder.append(message);
		builder.append(", subscribable=");
		builder.append(subscribable);
		builder.append(", currentAccountStatus=");
		builder.append(currentAccountStatus);
		builder.append("]");
		return builder.toString();
	}
	
}
