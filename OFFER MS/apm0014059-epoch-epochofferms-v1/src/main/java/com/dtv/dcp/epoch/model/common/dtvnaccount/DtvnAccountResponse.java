package com.dtv.dcp.epoch.model.common.dtvnaccount;

/**
 * The Class DtvnAccountResponse.
 */
public class DtvnAccountResponse {

	/** The status. */
	private String status;

	/** The account response. */
	private AccountResponse accountResponse;

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status
	 *            the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Gets the account response.
	 *
	 * @return the account response
	 */
	public AccountResponse getAccountResponse() {
		return accountResponse;
	}

	/**
	 * Sets the account response.
	 *
	 * @param accountResponse
	 *            the new account response
	 */
	public void setAccountResponse(AccountResponse accountResponse) {
		this.accountResponse = accountResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DtvnAccountResponse [status=");
		builder.append(status);
		builder.append(", accountResponse=");
		builder.append(accountResponse);
		builder.append("]");
		return builder.toString();
	}

}
