package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class LineItemDetail implements Serializable {

	private static final long serialVersionUID = 1L;

	private LineItemTypeAttribute programming;
	private LineItemTypeAttribute agreement;
	private LineItemTypeAttribute adjustment;
	private LineItemTypeAttribute receiver;
	private LineItemTypeAttribute accessory;
	private LineItemTypeAttribute offer;
	private LineItemTypeAttribute offerDetail;
	private LineItemTypeAttribute fee;
	private LineItemTypeAttribute installation;
	private String virtual;
	
	public String getVirtual() {
		return virtual;
	}
	public void setVirtual(String virtual) {
		this.virtual = virtual;
	}
	
	public LineItemTypeAttribute getInstallation() {
		return installation;
	}
	public void setInstallation(LineItemTypeAttribute installation) {
		this.installation = installation;
	}
	
	public LineItemTypeAttribute getFee() {
		return fee;
	}
	public void setFee(LineItemTypeAttribute fee) {
		this.fee = fee;
	}
	
	public LineItemTypeAttribute getOfferDetail() {
		return offerDetail;
	}
	public void setOfferDetail(LineItemTypeAttribute offerDetail) {
		this.offerDetail = offerDetail;
	}
	public LineItemTypeAttribute getOffer() {
		return offer;
	}
	public void setOffer(LineItemTypeAttribute offer) {
		this.offer = offer;
	}
	public LineItemTypeAttribute getProgramming() {
		return programming;
	}
	public void setProgramming(LineItemTypeAttribute programming) {
		this.programming = programming;
	}
	public LineItemTypeAttribute getAgreement() {
		return agreement;
	}
	public void setAgreement(LineItemTypeAttribute agreement) {
		this.agreement = agreement;
	}
	public LineItemTypeAttribute getAdjustment() {
		return adjustment;
	}
	public void setAdjustment(LineItemTypeAttribute adjustment) {
		this.adjustment = adjustment;
	}
	public LineItemTypeAttribute getReceiver() {
		return receiver;
	}
	public void setReceiver(LineItemTypeAttribute receiver) {
		this.receiver = receiver;
	}
	public LineItemTypeAttribute getAccessory() {
		return accessory;
	}
	public void setAccessory(LineItemTypeAttribute accessory) {
		this.accessory = accessory;
	}
	
	@Override
	public String toString() {
		return "LineItemDetail [programming=" + programming + ", agreement=" + agreement + ", adjustment=" + adjustment
				+ ", receiver=" + receiver + ", accessory=" + accessory + "]";
	}
	
}
