package com.dtv.dcp.epoch.model.ct.generic;

import java.io.Serializable;

import com.dtv.dcp.epoch.model.ct.product.ProductDescriptionsByKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenericByKey extends ProductDescriptionsByKey implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	private String online;
	
	/** The key. */
	private String opus;

	/** The attdotcom. */
	@JsonProperty("att.com")
	private String attdotcom;
	
	/** The directvnowdotcom. */
	private String directvnowdotcom;
	
	/** The bill. */
	private String bill;
	
	/** The confirmation. */
	private String confirmation;
	
	/** The cart. */
	private String cart;
	
	/** The services. */
	private String services;
	
	/** The cpcDisplayName. */
	@JsonProperty("name-cpc")
	private String cpcDisplayName;
	
	private String onlineService;

	/** The long description opus. */
	@JsonProperty("long-myatt")
	private String longDescription;	

	@JsonProperty("oem-short-description")
	private String oemShortDescription;
	

	@JsonProperty("oem-long-description")
	private String oemLongDescription;
	
	@JsonProperty("oem-long-description-rokutv")
	private String oemLongDescriptionRokuTv;
	
	@JsonProperty("oem-long-description-firetv")
	private String oemLongDescriptionFireTv;
	
	@JsonProperty("oem-short-description-rokutv")
	private String oemShortDescriptionRokuTv;
	
	@JsonProperty("oem-short-description-firetv")
	private String oemShortDescriptionFireTv;
	
	@JsonProperty("name-partner")
	private String namePartner;

	@JsonProperty("shortDesc")
	private String shortDesc;
	@JsonProperty("longDesc")
	private String longDesc;

	@JsonProperty("shortDisplayName")
	private String shortDisplayName;
	@JsonProperty("longDisplayName")
	private String longDisplayName;

	public String getShortDisplayName() {
		return shortDisplayName;
	}

	public void setShortDisplayName(String shortDisplayName) {
		this.shortDisplayName = shortDisplayName;
	}

	public String getLongDisplayName() {
		return longDisplayName;
	}

	public void setLongDisplayName(String longDisplayName) {
		this.longDisplayName = longDisplayName;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public void setShortDesc(String shortDesc) {
		this.shortDesc = shortDesc;
	}

	public String getLongDesc() {
		return longDesc;
	}

	public void setLongDesc(String longDesc) {
		this.longDesc = longDesc;
	}

	public String getNamePartner() {
		return namePartner;
	}

	public void setNamePartner(String namePartner) {
		this.namePartner = namePartner;
	}
	
	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getOnlineService() {
		return onlineService;
	}

	public void setOnlineService(String onlineService) {
		this.onlineService = onlineService;
	}

	/**
	 * @return the cpcDisplayName
	 */
	public String getCpcDisplayName() {
		return cpcDisplayName;
	}

	/**
	 * @param cpcDisplayName the cpcDisplayName to set
	 */
	public void setCpcDisplayName(String cpcDisplayName) {
		this.cpcDisplayName = cpcDisplayName;
	}

	/**
	 * @return the opus
	 */
	public String getOpus() {
		return opus;
	}

	/**
	 * @param opus the opus to set
	 */
	public void setOpus(String opus) {
		this.opus = opus;
	}

	/**
	 * @return the attdotcom
	 */
	public String getAttdotcom() {
		return attdotcom;
	}

	/**
	 * @param attdotcom the attdotcom to set
	 */
	public void setAttdotcom(String attdotcom) {
		this.attdotcom = attdotcom;
	}

	/**
	 * @return the directvnowdotcom
	 */
	public String getDirectvnowdotcom() {
		return directvnowdotcom;
	}

	/**
	 * @param directvnowdotcom the directvnowdotcom to set
	 */
	public void setDirectvnowdotcom(String directvnowdotcom) {
		this.directvnowdotcom = directvnowdotcom;
	}

	/**
	 * @return the bill
	 */
	public String getBill() {
		return bill;
	}

	/**
	 * @param bill the bill to set
	 */
	public void setBill(String bill) {
		this.bill = bill;
	}

	/**
	 * @return the confirmation
	 */
	public String getConfirmation() {
		return confirmation;
	}

	/**
	 * @param confirmation the confirmation to set
	 */
	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}

	/**
	 * @return the cart
	 */
	public String getCart() {
		return cart;
	}

	/**
	 * @param cart the cart to set
	 */
	public void setCart(String cart) {
		this.cart = cart;
	}

	/**
	 * @return the services
	 */
	public String getServices() {
		return services;
	}

	/**
	 * @param services the services to set
	 */
	public void setServices(String services) {
		this.services = services;
	}

	public String getOemShortDescription() {
		return oemShortDescription;
	}

	public void setOemShortDescription(String oemShortDescription) {
		this.oemShortDescription = oemShortDescription;
	}

	public String getOemLongDescription() {
		return oemLongDescription;
	}

	public void setOemLongDescription(String oemLongDescription) {
		this.oemLongDescription = oemLongDescription;
	}
	
	public String getOemLongDescriptionRokuTv() {
		return oemLongDescriptionRokuTv;
	}

	public void setOemLongDescriptionRokuTv(String oemLongDescriptionRokuTv) {
		this.oemLongDescriptionRokuTv = oemLongDescriptionRokuTv;
	}

	public String getOemLongDescriptionFireTv() {
		return oemLongDescriptionFireTv;
	}

	public void setOemLongDescriptionFireTv(String oemLongDescriptionFireTv) {
		this.oemLongDescriptionFireTv = oemLongDescriptionFireTv;
	}

	public String getOemShortDescriptionRokuTv() {
		return oemShortDescriptionRokuTv;
	}

	public void setOemShortDescriptionRokuTv(String oemShortDescriptionRokuTv) {
		this.oemShortDescriptionRokuTv = oemShortDescriptionRokuTv;
	}

	public String getOemShortDescriptionFireTv() {
		return oemShortDescriptionFireTv;
	}

	public void setOemShortDescriptionFireTv(String oemShortDescriptionFireTv) {
		this.oemShortDescriptionFireTv = oemShortDescriptionFireTv;
	}

	public String getOnline() {
		return online;
	}

	public void setOnline(String online) {
		this.online = online;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GenericByKey [opus=");
		builder.append(opus);
		builder.append(", attdotcom=");
		builder.append(attdotcom);
		builder.append(", directvnowdotcom=");
		builder.append(directvnowdotcom);
		builder.append(", bill=");
		builder.append(bill);
		builder.append(", confirmation=");
		builder.append(confirmation);
		builder.append(", cart=");
		builder.append(cart);
		builder.append(", services=");
		builder.append(services);
		builder.append(", cpcDisplayName=");
		builder.append(cpcDisplayName);
		builder.append(", online=");
		builder.append(online);
		builder.append(", onlineService=");
		builder.append(onlineService);
		builder.append(", longDescription=");
		builder.append(longDescription);
		builder.append("]");
		return builder.toString();
	}

	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isBlank(shortDisplayName) && StringUtils.isBlank(longDisplayName);
	}

}
