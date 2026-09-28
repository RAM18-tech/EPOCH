package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

public class SegmentOffer implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	private String segmentOfferId;
	private String segmentOfferType;
	private String offerRequired;
	private String promotionCode;
	private Integer freeTVCount;

	/**
	 * @return the segmentOfferId
	 */
	public String getSegmentOfferId() {
		return segmentOfferId;
	}

	/**
	 * @param segmentOfferId
	 *            the segmentOfferId to set
	 */
	public void setSegmentOfferId(String segmentOfferId) {
		this.segmentOfferId = segmentOfferId;
	}

	/**
	 * @return the segmentOfferType
	 */
	public String getSegmentOfferType() {
		return segmentOfferType;
	}

	/**
	 * @param segmentOfferType
	 *            the segmentOfferType to set
	 */
	public void setSegmentOfferType(String segmentOfferType) {
		this.segmentOfferType = segmentOfferType;
	}

	/**
	 * @return the offerRequired
	 */
	public String getOfferRequired() {
		return offerRequired;
	}

	/**
	 * @param offerRequired
	 *            the offerRequired to set
	 */
	public void setOfferRequired(String offerRequired) {
		this.offerRequired = offerRequired;
	}

	/**
	 * @return the promotionCode
	 */
	public String getPromotionCode() {
		return promotionCode;
	}

	/**
	 * @param promotionCode
	 *            the promotionCode to set
	 */
	public void setPromotionCode(String promotionCode) {
		this.promotionCode = promotionCode;
	}

	/**
	 * @return the freeTVCount
	 */
	public Integer getFreeTVCount() {
		return freeTVCount;
	}

	/**
	 * @param freeTVCount
	 *            the freeTVCount to set
	 */
	public void setFreeTVCount(Integer freeTVCount) {
		this.freeTVCount = freeTVCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SegmentOffer [segmentOfferId=" + segmentOfferId + ", segmentOfferType=" + segmentOfferType
				+ ", offerRequired=" + offerRequired + ", promotionCode=" + promotionCode + ", freeTVCount="
				+ freeTVCount + "]";
	}

}
