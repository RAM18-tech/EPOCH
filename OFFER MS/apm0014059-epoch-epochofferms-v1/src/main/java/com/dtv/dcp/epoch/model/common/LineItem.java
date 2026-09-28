package com.dtv.dcp.epoch.model.common;


import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.request.CTBenefit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *This class is a model beans for wireless lobs - lineitems  of cart
 *
 */

/**
 * Created by nk3077 on 05/08/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineItem implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant id. */
    @JsonProperty("id")
    private String id;


    /** The Constant billingCode. */
    @JsonProperty("billingCode")
    private String billingCode;

    /** The Constant productType. */
    @JsonProperty("productType")
    private String productType;
    
    
    /** The Constant offerId. */
    @JsonProperty("offerId")
    private String offerId;
    
    /** The Constant productId. */
    @JsonProperty("productId")
    private String productId;
    
    /** The Constant productSKU. */
    @JsonProperty("productSKU")
    private String productSKU;	
    
    
    /** The Constant itemType. */
    @JsonProperty("itemType")
    private String itemType;
    
    @JsonProperty("promotionReferences")
    private List<String> promotionReferences;
    
    @JsonProperty("productGroupReferences")
    private List<String> productGroupReferences;
    
    @JsonProperty("offerCodes")
    private List<String> offerCodes;
    
    @JsonProperty("billingProductCode")
    private String billingProductCode;
    
    @JsonProperty("benefits")
    private List<Benefit> benefits;
    
    private List<CTBenefit> ctBenefits;
    

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}


	/**
	 * @return the billingCode
	 */
	public String getBillingCode() {
		return billingCode;
	}


	/**
	 * @param billingCode the billingCode to set
	 */
	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}


	/**
	 * @return the productType
	 */
	public String getProductType() {
		return productType;
	}


	/**
	 * @param productType the productType to set
	 */
	public void setProductType(String productType) {
		this.productType = productType;
	}


	/**
	 * @return the offerId
	 */
	public String getOfferId() {
		return offerId;
	}


	/**
	 * @param offerId the offerId to set
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}


	/**
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}


	/**
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}


	/**
	 * @return the productSKU
	 */
	public String getProductSKU() {
		return productSKU;
	}


	/**
	 * @param productSKU the productSKU to set
	 */
	public void setProductSKU(String productSKU) {
		this.productSKU = productSKU;
	}


	/**
	 * @return the itemType
	 */
	public String getItemType() {
		return itemType;
	}


	/**
	 * @param itemType the itemType to set
	 */
	public void setItemType(String itemType) {
		this.itemType = itemType;
	}


	/**
	 * @return the promotionReferences
	 */
	public List<String> getPromotionReferences() {
		return promotionReferences;
	}


	/**
	 * @param promotionReferences the promotionReferences to set
	 */
	public void setPromotionReferences(List<String> promotionReferences) {
		this.promotionReferences = promotionReferences;
	}

	/**
	 * @return the productGroupReferences
	 */
	public List<String> getProductGroupReferences() {
		return productGroupReferences;
	}


	/**
	 * @param productGroupReferences the productGroupReferences to set
	 */
	public void setProductGroupReferences(List<String> productGroupReferences) {
		this.productGroupReferences = productGroupReferences;
	}


	/**
	 * @return the offerCodes
	 */
	public List<String> getOfferCodes() {
		return offerCodes;
	}


	/**
	 * @param offerCodes the offerCodes to set
	 */
	public void setOfferCodes(List<String> offerCodes) {
		this.offerCodes = offerCodes;
	}


	/**
	 * @return the billingProductCode
	 */
	public String getBillingProductCode() {
		return billingProductCode;
	}


	/**
	 * @param billingProductCode the billingProductCode to set
	 */
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}


	/**
	 * @return the benefits
	 */
	public List<Benefit> getBenefits() {
		return benefits;
	}


	/**
	 * @return the ctBenefits
	 */
	public List<CTBenefit> getCtBenefits() {
		return ctBenefits;
	}


	/**
	 * @param ctBenefits the ctBenefits to set
	 */
	public void setCtBenefits(List<CTBenefit> ctBenefits) {
		this.ctBenefits = ctBenefits;
	}


	/**
	 * @param benefits the benefits to set
	 */
	public void setBenefits(List<Benefit> benefits) {
		this.benefits = benefits;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LineItem [" + (id != null ? "id=" + id + ", " : "")
				+ (billingCode != null ? "billingCode=" + billingCode + ", " : "")
				+ (productType != null ? "productType=" + productType + ", " : "")
				+ (offerId != null ? "offerId=" + offerId + ", " : "")
				+ (productId != null ? "productId=" + productId + ", " : "")
				+ (productSKU != null ? "productSKU=" + productSKU + ", " : "")
				+ (itemType != null ? "itemType=" + itemType + ", " : "")
				+ (promotionReferences != null ? "promotionReferences=" + promotionReferences + ", " : "")
				+ (productGroupReferences != null ? "productGroupReferences=" + productGroupReferences + ", " : "")
				+ (offerCodes != null ? "offerCodes=" + offerCodes + ", " : "")
				+ (billingProductCode != null ? "billingProductCode=" + billingProductCode + ", " : "")
				+ (benefits != null ? "benefits=" + benefits + ", " : "")
				+ (ctBenefits != null ? "ctBenefits=" + ctBenefits : "") + "]";
	}


	
	
	
}
