/**
 * 
 */
package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class OfferItem.
 *
 * @author sm907k
 */
public class OfferItem  implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;	
	
	/** The offer id. */
	@JsonProperty("offerId")
	private String offerId;
	
	/** The standAlone offer id. */
	@JsonProperty("offerIds")
	private List<String> offerIds;
	
	/** The offer id. */
	@JsonProperty("standAloneOfferIds")
	private List<String> standAloneOfferIds;
	
	/** The product id. */
	@JsonProperty("productId")
	private String productId;
	
	/** The sku id. */
	@JsonProperty("skuId")
	private String skuId;
	
	
	/** The base offer. */
	@JsonProperty("baseOfferId")	
	private String baseOfferId;

	/** The associated offers. */
	@JsonProperty("associatedOffers")
	private List<String> associatedOffers;

	/** The promo Type. */
	@JsonProperty("promoType")
	private String promoType;
	
	/** The add ons. */
	@JsonProperty("addonOfferIds")
	private List<String> addonOfferIds;
	
	/** The contract. */
	@JsonProperty("contract")
	private Boolean contract;
	
	/**
	 * Gets the contract.
	 *
	 * @return the contract
	 */
	public Boolean getContract() {
		return contract;
	}

	/**
	 * Sets the contract.
	 *
	 * @param contract the new contract
	 */
	public void setContract(Boolean contract) {
		this.contract = contract;
	}

	/**
	 * Gets the promoType .
	 *
	 * @return the promoType
	 */
	public String getPromoType() {
		return promoType;
	}

	/**
	 * Sets the promoType.
	 *
	 * @param promoType the promoType to set
	 */
	public void setPromoType(String promoType) {
		this.promoType = promoType;
	}
	

	/**
	 * Gets the offer id.
	 *
	 * @return the offerId
	 */
	public String getOfferId() {
		return offerId;
	}

	/**
	 * Sets the offer id.
	 *
	 * @param offerId the offerId to set
	 */
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}

	/**
	 * Gets the product id.
	 *
	 * @return the productId
	 */
	public String getProductId() {
		return productId;
	}

	/**
	 * Sets the product id.
	 *
	 * @param productId the productId to set
	 */
	public void setProductId(String productId) {
		this.productId = productId;
	}

	/**
	 * Gets the sku id.
	 *
	 * @return the skuId
	 */
	public String getSkuId() {
		return skuId;
	}

	/**
	 * Sets the sku id.
	 *
	 * @param skuId the skuId to set
	 */
	public void setSkuId(String skuId) {
		this.skuId = skuId;
	}

	/**
	 * Gets the associated offers.
	 *
	 * @return the associatedOffers
	 */
	public List<String> getAssociatedOffers() {
		return associatedOffers;
	}

	/**
	 * Sets the associated offers.
	 *
	 * @param associatedOffers the associatedOffers to set
	 */
	public void setAssociatedOffers(List<String> associatedOffers) {
		this.associatedOffers = associatedOffers;
	}

	/**
	 * Gets the base offer id.
	 *
	 * @return the baseOfferId
	 */
	public String getBaseOfferId() {
		return baseOfferId;
	}

	/**
	 * Sets the base offer id.
	 *
	 * @param baseOfferId the baseOfferId to set
	 */
	public void setBaseOfferId(String baseOfferId) {
		this.baseOfferId = baseOfferId;
	}

	/**
	 * Gets the addon offer ids.
	 *
	 * @return the addonOfferIds
	 */
	public List<String> getAddonOfferIds() {
		return addonOfferIds;
	}

	/**
	 * Sets the addon offer ids.
	 *
	 * @param addonOfferIds the addonOfferIds to set
	 */
	public void setAddonOfferIds(List<String> addonOfferIds) {
		this.addonOfferIds = addonOfferIds;
	}

	/**
	 * Gets the offer ids.
	 *
	 * @return the offerIds
	 */
	public List<String> getOfferIds() {
		return offerIds;
	}

	/**
	 * Sets the offer ids.
	 *
	 * @param offerIds the offerIds to set
	 */
	public void setOfferIds(List<String> offerIds) {
		this.offerIds = offerIds;
	}
	
	/**
	 * Gets the stand alone offer ids.
	 *
	 * @return the stand alone offer ids
	 */
	public List<String> getStandAloneOfferIds() {
		return standAloneOfferIds;
	}

	/**
	 * Sets the stand alone offer ids.
	 *
	 * @param standAloneOfferIds the new stand alone offer ids
	 */
	public void setStandAloneOfferIds(List<String> standAloneOfferIds) {
		this.standAloneOfferIds = standAloneOfferIds;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferItem [offerId=");
		builder.append(offerId);
		builder.append(", offerIds=");
		builder.append(offerIds);
		builder.append(", standAloneOfferIds=");
		builder.append(standAloneOfferIds);
		builder.append(", productId=");
		builder.append(productId);
		builder.append(", skuId=");
		builder.append(skuId);
		builder.append(", baseOfferId=");
		builder.append(baseOfferId);
		builder.append(", associatedOffers=");
		builder.append(associatedOffers);
		builder.append(", promoType=");
		builder.append(promoType);
		builder.append(", addonOfferIds=");
		builder.append(addonOfferIds);
		builder.append(", contract=");
		builder.append(contract);
		builder.append("]");
		return builder.toString();
	}
	
}
