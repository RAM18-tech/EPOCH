package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class VideoDeviceLimit implements Serializable{
	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String salesChannel;
	private int maxDVRs;
	private int maxSTBsWired;
	private int maxSTBsWIFI;
	private int maxSTBsWithServer;
	public String getSalesChannel() {
		return salesChannel;
	}
	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}
	public int getMaxDVRs() {
		return maxDVRs;
	}
	public void setMaxDVRs(int maxDVRs) {
		this.maxDVRs = maxDVRs;
	}
	public int getMaxSTBsWired() {
		return maxSTBsWired;
	}
	public void setMaxSTBsWired(int maxSTBsWired) {
		this.maxSTBsWired = maxSTBsWired;
	}
	public int getMaxSTBsWIFI() {
		return maxSTBsWIFI;
	}
	public void setMaxSTBsWIFI(int maxSTBsWIFI) {
		this.maxSTBsWIFI = maxSTBsWIFI;
	}
	public int getMaxSTBsWithServer() {
		return maxSTBsWithServer;
	}
	public void setMaxSTBsWithServer(int maxSTBsWithServer) {
		this.maxSTBsWithServer = maxSTBsWithServer;
	}
}
