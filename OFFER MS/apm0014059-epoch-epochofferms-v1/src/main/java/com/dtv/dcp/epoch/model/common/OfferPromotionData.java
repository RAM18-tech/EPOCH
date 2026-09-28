package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;

public class OfferPromotionData implements Serializable {

	/**
	 * The serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	private String promoId;
	
	private String startDate;

	private String endDate;

	/**
	 * @return String
	 */
	public String getPromoId() {
		return promoId;
	}

	/**
	 * @param promoId
	 */
	public void setPromoId(String promoId) {
		this.promoId = promoId;
	}

	/**
	 * @return String
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return String
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
}