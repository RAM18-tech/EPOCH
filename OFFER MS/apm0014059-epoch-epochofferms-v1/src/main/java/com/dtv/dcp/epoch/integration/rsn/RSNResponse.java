package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;

/**
 * The Class RSNResponse.
 * @author ks5810
 *
 */
public class RSNResponse implements Serializable  {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The message. */
	private String message;
	/** The message. */
	private RSNFee fee;
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	/**
	 * @return the rsnFee
	 */
	public RSNFee getFee() {
		return fee;
	}
	/**
	 * @param rsnFee the rsnFee to set
	 */
	public void setFee(RSNFee rsnFee) {
		this.fee = rsnFee;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RSNResponse [message=" + message + ", rsnFee=" + fee + "]";
	}
	

}