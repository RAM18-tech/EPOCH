package com.dtv.dcp.epoch.model.common.response;

import java.io.Serializable;

public class CpopIXPResponse implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private boolean cpopWirelessBackup;
	
	private boolean cpopWirelessDCTesting;

	public boolean isCpopWirelessBackup() {
		return cpopWirelessBackup;
	}

	public void setCpopWirelessBackup(boolean cpopWirelessBackup) {
		this.cpopWirelessBackup = cpopWirelessBackup;
	}

	public boolean isCpopWirelessDCTesting() {
		return cpopWirelessDCTesting;
	}

	public void setCpopWirelessDCTesting(boolean cpopWirelessDCTesting) {
		this.cpopWirelessDCTesting = cpopWirelessDCTesting;
	}

}
