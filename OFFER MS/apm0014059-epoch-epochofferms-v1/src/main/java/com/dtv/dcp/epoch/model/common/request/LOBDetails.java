package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LOBDetails implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
    private String businessSegment;
    
    private String customerSegement;
    
    private String offerActionType;
    
    private String productFamily;
   
    private String lobType;
   
    private List<Losgs> losgs;
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
	/**
	 * @return the lobType
	 */
	public String getLobType() {
		return lobType;
	}
	/**
	 * @param lobType the lobType to set
	 */
	public void setLobType(String lobType) {
		this.lobType = lobType;
	}
	
	/**
	 * @return the losgs
	 */
	public List<Losgs> getLosgs() {
		return losgs;
	}
	/**
	 * @param losgs the losgs to set
	 */
	public void setLosgs(List<Losgs> losgs) {
		this.losgs = losgs;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "lobDetails: {" + (businessSegment != null ? "businessSegment=" + businessSegment + ", " : "")
				+ (customerSegement != null ? "customerSegement=" + customerSegement + ", " : "")
				+ (offerActionType != null ? "offerActionType=" + offerActionType + ", " : "")
				+ (productFamily != null ? "productFamily=" + productFamily + ", " : "")
				+ (lobType != null ? "lobType=" + lobType + ", " : "") + (losgs != null ? "losgs=" + losgs : "") + "}";
	}  
    
}
