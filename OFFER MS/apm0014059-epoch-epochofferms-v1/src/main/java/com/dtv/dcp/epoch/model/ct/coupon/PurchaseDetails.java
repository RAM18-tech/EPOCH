package com.dtv.dcp.epoch.model.ct.coupon;

import java.io.Serializable;

public class PurchaseDetails implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** usedDate */
	private String usedDate;
	
	/** titlePurchased */
	private String titlePurchased;
	
	/** retailPrice */
	private String retailPrice;
	
	/** discountAmount */
	private String discountAmount;
	
	/** netAmount */
	private String netAmount;
	
	/** tmsProgramID */
	private String tmsProgramID;
	

	public String getTitlePurchased() {
		return titlePurchased;
	}

	public void setTitlePurchased(String titlePurchased) {
		this.titlePurchased = titlePurchased;
	}

	public String getRetailPrice() {
		return retailPrice;
	}

	public void setRetailPrice(String retailPrice) {
		this.retailPrice = retailPrice;
	}

	public String getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(String discountAmount) {
		this.discountAmount = discountAmount;
	}

	public String getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}

	public String getTmsProgramID() {
		return tmsProgramID;
	}

	public void setTmsProgramID(String tmsProgramID) {
		this.tmsProgramID = tmsProgramID;
	}

	public String getUsedDate() {
		return usedDate;
	}

	public void setUsedDate(String usedDate) {
		this.usedDate = usedDate;
	}

}