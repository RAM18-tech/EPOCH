/**
 * 
 */
package com.dtv.dcp.epoch.model.common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * The Class CartModeItem.
 *
 * @author sb455x
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItems implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The base offer. */
	@ApiModelProperty(value = "Base Package")
	@JsonProperty("basePackage")
	private OfferItem basePackage;

	/** The standAlone offers. */
	@ApiModelProperty(value = "StandAlone Package")
	@JsonProperty("standAlonePackage")
	private OfferItem standAloneOffers;

	/** The equipment. */
	@ApiModelProperty(value = "Equipment")
	@JsonProperty("equipment")
	private OfferItem equipment;

	/** The add ons. */
	@ApiModelProperty(value = "AddOns")
	@JsonProperty("addOns")
	private OfferItem addOns;

	/** The promotion offers. */
	@JsonProperty("promotionOffers")
	@ApiModelProperty(value = "Promotion Offers")
	private List<OfferItem> promotionOffers;

	/** The promotional offers. */
	@ApiModelProperty(value = "Promotional Offers References")
	@JsonProperty("promotionalOffersReferences")
	private List<String> promotionalOffersReferences;

	/** The Constant businessType. */
	@JsonProperty("businessType")
	private String businessType;

	/** The Constant lobType. */
	@JsonProperty("lobType")
	private String lobType;

	@JsonProperty("losgs")
	private Map<String, Losg> losgs;

	@JsonProperty("location")
	private Location location;
	
	@JsonProperty("promotions")
	private List<Promotion> promotions;

	/**
	 * @return the basePackage
	 */
	public OfferItem getBasePackage() {
		return basePackage;
	}

	/**
	 * @param basePackage
	 *            the basePackage to set
	 */
	public void setBasePackage(OfferItem basePackage) {
		this.basePackage = basePackage;
	}

	/**
	 * @return the standAloneOffers
	 */
	public OfferItem getStandAloneOffers() {
		return standAloneOffers;
	}

	/**
	 * @param standAloneOffers
	 *            the standAloneOffers to set
	 */
	public void setStandAloneOffers(OfferItem standAloneOffers) {
		this.standAloneOffers = standAloneOffers;
	}

	/**
	 * @return the equipment
	 */
	public OfferItem getEquipment() {
		return equipment;
	}

	/**
	 * @param equipment
	 *            the equipment to set
	 */
	public void setEquipment(OfferItem equipment) {
		this.equipment = equipment;
	}

	/**
	 * @return the addOns
	 */
	public OfferItem getAddOns() {
		return addOns;
	}

	/**
	 * @param addOns
	 *            the addOns to set
	 */
	public void setAddOns(OfferItem addOns) {
		this.addOns = addOns;
	}

	/**
	 * @return the promotionOffers
	 */
	public List<OfferItem> getPromotionOffers() {
		return promotionOffers;
	}

	/**
	 * @param promotionOffers
	 *            the promotionOffers to set
	 */
	public void setPromotionOffers(List<OfferItem> promotionOffers) {
		this.promotionOffers = promotionOffers;
	}

	/**
	 * @return the promotionalOffersReferences
	 */
	public List<String> getPromotionalOffersReferences() {
		return promotionalOffersReferences;
	}

	/**
	 * @param promotionalOffersReferences
	 *            the promotionalOffersReferences to set
	 */
	public void setPromotionalOffersReferences(List<String> promotionalOffersReferences) {
		this.promotionalOffersReferences = promotionalOffersReferences;
	}

	/**
	 * @return the businessType
	 */
	public String getBusinessType() {
		return businessType;
	}

	/**
	 * @param businessType
	 *            the businessType to set
	 */
	public void setBusinessType(String businessType) {
		this.businessType = businessType;
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
	public Map<String, Losg> getLosgs() {
		return losgs;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * @param losgs
	 *            the losgs to set
	 */
	public void setLosgs(Map<String, Losg> losgs) {
		this.losgs = losgs;
	}

	/**
	 * @return the promotions
	 */
	public List<Promotion> getPromotions() {
		return promotions;
	}

	/**
	 * @param promotions the promotions to set
	 */
	public void setPromotions(List<Promotion> promotions) {
		this.promotions = promotions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CartModeDTVItem [basePackage=");
		builder.append(basePackage);
		builder.append(standAloneOffers);
		builder.append(", equipment=");
		builder.append(equipment);
		builder.append(", addOns=");
		builder.append(addOns);
		builder.append(", promotionOffers=");
		builder.append(promotionOffers);
		builder.append(", promotionalOffersReferences=");
		builder.append(promotionalOffersReferences);
		builder.append(" businessType=");
		builder.append(businessType);
		builder.append(", lobType=");
		builder.append(lobType);
		builder.append(", losgs=");
		builder.append(losgs);
		builder.append(", location=");
		builder.append(location);
		builder.append(", promotions=");
		builder.append(promotions);
		builder.append("]");
		return builder.toString();
	}
}
