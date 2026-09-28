package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This class is a model beans for the CT CartItem
 *
 */

/**
 * Created by dr000y on 08/02/2019.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTCartItem implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant businessType. */
	@JsonProperty("customerType")
	private String customerType;

	/** The Constant lobType. */
	@JsonProperty("lobType")
	private String lobType;
	
	@JsonProperty("businessSegment")
	private String businessSegment;
	
	@JsonProperty("customerSegement")
	private String customerSegement;
	
	@JsonProperty("offerActionType")
	private String offerActionType;
	
	@JsonProperty("productFamily")
	private String productFamily;

	/** The losg. */
	@JsonProperty("losgs")
	private List<CTLosgs> losgs;

	@JsonProperty("benefits")
	private List<CTBenefit> benefits;

	/**
	 * @return the customerType
	 */
	public String getCustomerType() {
		return customerType;
	}

	/**
	 * @param customerType
	 *            the customerType to set
	 */
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	/**
	 * @return the lobType
	 */
	public String getLobType() {
		return lobType;
	}

	/**
	 * @param lobType
	 *            the lobType to set
	 */
	public void setLobType(String lobType) {
		this.lobType = lobType;
	}

	/**
	 * @return the losgs
	 */
	public List<CTLosgs> getLosgs() {
		return losgs;
	}

	/**
	 * @param losgs
	 *            the losgs to set
	 */
	public void setLosgs(List<CTLosgs> losgs) {
		this.losgs = losgs;
	}

	/**
	 * @return the benefits
	 */
	public List<CTBenefit> getBenefits() {
		return benefits;
	}

	/**
	 * @param benefits
	 *            the benefits to set
	 */
	public void setBenefits(List<CTBenefit> benefits) {
		this.benefits = benefits;
	}	
	
	/**
	 * @return the businessSegment
	 */
	public String getBusinessSegment() {
		return businessSegment;
	}

	/**
	 * @param businessSegment the businessSegment to set
	 */
	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}

	/**
	 * @return the customerSegement
	 */
	public String getCustomerSegement() {
		return customerSegement;
	}

	/**
	 * @param customerSegement the customerSegement to set
	 */
	public void setCustomerSegement(String customerSegement) {
		this.customerSegement = customerSegement;
	}

	/**
	 * @return the offerActionType
	 */
	public String getOfferActionType() {
		return offerActionType;
	}

	/**
	 * @param offerActionType the offerActionType to set
	 */
	public void setOfferActionType(String offerActionType) {
		this.offerActionType = offerActionType;
	}

	/**
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * @param productFamily the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "lobDetails: {" + (customerType != null ? "customerType=" + customerType + ", " : "")
				+ (lobType != null ? "lobType=" + lobType + ", " : "")
				+ (businessSegment != null ? "businessSegment=" + businessSegment + ", " : "")
				+ (customerSegement != null ? "customerSegement=" + customerSegement + ", " : "")
				+ (offerActionType != null ? "offerActionType=" + offerActionType + ", " : "")
				+ (productFamily != null ? "productFamily=" + productFamily + ", " : "")
				+ (losgs != null ? "losgs=" + losgs + ", " : "") + (benefits != null ? "benefits=" + benefits : "")
				+ "}";
	}

	

}
