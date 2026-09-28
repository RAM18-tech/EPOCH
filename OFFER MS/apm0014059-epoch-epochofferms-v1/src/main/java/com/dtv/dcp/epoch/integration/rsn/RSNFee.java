package com.dtv.dcp.epoch.integration.rsn;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class RSNFee.
 *
 * @author ks5810
 */
public class RSNFee implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	/** The name. */
	private String name;
	/** The sku. */
	private String sku;
	/** The feeAmount. */
	private Double amount;
	/** The visible. */
	private Boolean visible;
	
	/**  The String displayName. */
	private String displayName;
	
	/**  The String description. */
	private String description;
	
	/** The List<Promotion> Object. */
	private List<Promotion> promotions;
	
	/** The String type. */
	private String type;
	
	/** The long description. */
	private String longDescription;
	
	/** The String longDescriptionforServices. */
	private String longDescriptionforServices;
	
	/** The String displayNameforServices. */
	private String displayNameforServices;
	
	/** The String shortDescriptionforServices. */
	private String shortDescriptionforServices;
	
	/** The String descriptionforServices. */
	private String descriptionforServices;
	
	
	/** The String longDescriptionforServices. */
	@JsonProperty("longDescriptionforSales")
	private String longDescriptionforSales;

	/** The String displayNameforServices. */
	@JsonProperty("displayNameforSales")
	private String displayNameforSales;

	/** The String shortDescriptionforServices. */
	@JsonProperty("shortDescriptionforSales")
	private List<ShortDescription> shortDescriptionforSales;

	/** The String descriptionforServices. */
	@JsonProperty("descriptionforSales")
	private String descriptionforSales;
	
	
	/**
	 * Gets the long description.
	 *
	 * @return the long description
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * Sets the long description.
	 *
	 * @param longDescription the new long description
	 */
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	/**
	 * Gets the long descriptionfor services.
	 *
	 * @return the long descriptionfor services
	 */
	public String getLongDescriptionforServices() {
		return longDescriptionforServices;
	}

	/**
	 * Sets the long descriptionfor services.
	 *
	 * @param longDescriptionforServices the new long descriptionfor services
	 */
	public void setLongDescriptionforServices(String longDescriptionforServices) {
		this.longDescriptionforServices = longDescriptionforServices;
	}

	/**
	 * Gets the display namefor services.
	 *
	 * @return the display namefor services
	 */
	public String getDisplayNameforServices() {
		return displayNameforServices;
	}

	/**
	 * Sets the display namefor services.
	 *
	 * @param displayNameforServices the new display namefor services
	 */
	public void setDisplayNameforServices(String displayNameforServices) {
		this.displayNameforServices = displayNameforServices;
	}

	/**
	 * Gets the short descriptionfor services.
	 *
	 * @return the short descriptionfor services
	 */
	public String getShortDescriptionforServices() {
		return shortDescriptionforServices;
	}

	/**
	 * Sets the short descriptionfor services.
	 *
	 * @param shortDescriptionforServices the new short descriptionfor services
	 */
	public void setShortDescriptionforServices(String shortDescriptionforServices) {
		this.shortDescriptionforServices = shortDescriptionforServices;
	}

	/**
	 * Gets the descriptionfor services.
	 *
	 * @return the descriptionfor services
	 */
	public String getDescriptionforServices() {
		return descriptionforServices;
	}

	/**
	 * Sets the descriptionfor services.
	 *
	 * @param descriptionforServices the new descriptionfor services
	 */
	public void setDescriptionforServices(String descriptionforServices) {
		this.descriptionforServices = descriptionforServices;
	}

	/**
	 * Gets the long descriptionfor sales.
	 *
	 * @return the long descriptionfor sales
	 */
	public String getLongDescriptionforSales() {
		return longDescriptionforSales;
	}

	/**
	 * Sets the long descriptionfor sales.
	 *
	 * @param longDescriptionforSales the new long descriptionfor sales
	 */
	public void setLongDescriptionforSales(String longDescriptionforSales) {
		this.longDescriptionforSales = longDescriptionforSales;
	}

	/**
	 * Gets the display namefor sales.
	 *
	 * @return the display namefor sales
	 */
	public String getDisplayNameforSales() {
		return displayNameforSales;
	}

	/**
	 * Sets the display namefor sales.
	 *
	 * @param displayNameforSales the new display namefor sales
	 */
	public void setDisplayNameforSales(String displayNameforSales) {
		this.displayNameforSales = displayNameforSales;
	}

	/**
	 * Gets the short descriptionfor sales.
	 *
	 * @return the short descriptionfor sales
	 */
	public List<ShortDescription> getShortDescriptionforSales() {
		return shortDescriptionforSales;
	}

	/**
	 * Sets the short descriptionfor sales.
	 *
	 * @param shortDescriptionforSales the new short descriptionfor sales
	 */
	public void setShortDescriptionforSales(List<ShortDescription> shortDescriptionforSales) {
		this.shortDescriptionforSales = shortDescriptionforSales;
	}

	/**
	 * Gets the descriptionfor sales.
	 *
	 * @return the descriptionfor sales
	 */
	public String getDescriptionforSales() {
		return descriptionforSales;
	}

	/**
	 * Sets the descriptionfor sales.
	 *
	 * @param descriptionforSales the new descriptionfor sales
	 */
	public void setDescriptionforSales(String descriptionforSales) {
		this.descriptionforSales = descriptionforSales;
	}

	/**
	 * Gets the type.
	 *
	 * @return String
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the promotions.
	 *
	 * @return List<Promotion>
	 */
	public List<Promotion> getPromotions() {
		return promotions;
	}

	/**
	 * Sets the promotions.
	 *
	 * @param promotions the new promotions
	 */
	public void setPromotions(List<Promotion> promotions) {
		this.promotions = promotions;
	}

	/**
	 * Gets the display name.
	 *
	 * @return String
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Gets the description.
	 *
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the sku.
	 *
	 * @return the sku
	 */
	public String getSku() {
		return sku;
	}
	
	/**
	 * Sets the sku.
	 *
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}
	
	/**
	 * Gets the amount.
	 *
	 * @return the feeAmount
	 */
	public Double getAmount() {
		return amount;
	}
	
	/**
	 * Sets the amount.
	 *
	 * @param feeAmount the feeAmount to set
	 */
	public void setAmount(Double feeAmount) {
		this.amount = feeAmount;
	}
	
	/**
	 * Gets the visible.
	 *
	 * @return the visible
	 */
	public Boolean getVisible() {
		return visible;
	}
	
	/**
	 * Sets the visible.
	 *
	 * @param visible the visible to set
	 */
	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RSNFee [name=");
		builder.append(name);
		builder.append(", sku=");
		builder.append(sku);
		builder.append(", amount=");
		builder.append(amount);
		builder.append(", visible=");
		builder.append(visible);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", description=");
		builder.append(description);
		builder.append(", promotions=");
		builder.append(promotions);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

	

}
