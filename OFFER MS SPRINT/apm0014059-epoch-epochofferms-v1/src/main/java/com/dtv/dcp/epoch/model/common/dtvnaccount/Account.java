package com.dtv.dcp.epoch.model.common.dtvnaccount;

/**
 * The Class Account.
 */
public class Account {

	/** The customer ID. */
	private String customerID;

	/**
	 * Gets the customer ID.
	 *
	 * @return the customer ID
	 */
	public String getCustomerID() {
	return customerID;
	}

	/**
	 * Sets the customer ID.
	 *
	 * @param customerID the new customer ID
	 */
	public void setCustomerID(String customerID) {
	this.customerID = customerID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Account [customerID=");
		builder.append(customerID);
		builder.append("]");
		return builder.toString();
	}
	
}
