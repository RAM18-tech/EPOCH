package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProductDescriptionsByKey implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	
	/** The short-opus. */
	@JsonProperty("short-opus")
	private String shortDescOpus;

	/** The attdotcom. */
	@JsonProperty("short-myatt")
	private String shortDescMyatt;
	
	/** The long-services. */
	@JsonProperty("long-services")
	private String longSescServices;
	
	/** The short-service. */
	@JsonProperty("short-services")
	private String shortDescService;

	/** The short-service. */
	@JsonProperty("long-myatt")
	private String longDescription;
	
	/** The description-cpc. */
	@JsonProperty("description-cpc")
	private String cpcDescription;
	
	/** The short-partner. */
	@JsonProperty("short-partner")
	private String shortPartner;
	
	@JsonProperty("oem-long-description-rokutv")
	private String oemLongDescriptionRokuTv;
	
	@JsonProperty("oem-long-description-firetv")
	private String oemLongDescriptionFireTv;
	
	@JsonProperty("oem-short-description-rokutv")
	private String oemShortDescriptionRokuTv;
	
	@JsonProperty("oem-short-description-firetv")
	private String oemShortDescriptionFireTv;

	@JsonProperty("shortDesc")
	private String shortDesc;
	@JsonProperty("longDesc")
	private String longDesc;

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

	public String getShortPartner() {
		return shortPartner;
	}

	public void setShortPartner(String shortPartner) {
		this.shortPartner = shortPartner;
	}

	public String getLongPartner() {
		return longPartner;
	}

	public void setLongPartner(String longPartner) {
		this.longPartner = longPartner;
	}

	/** The long-partner. */
	@JsonProperty("long-partner")
	private String longPartner;

	/**
	 * @return the cpcDescription
	 */
	public String getCpcDescription() {
		return cpcDescription;
	}

	/**
	 * @param cpcDescription the cpcDescription to set
	 */
	public void setCpcDescription(String cpcDescription) {
		this.cpcDescription = cpcDescription;
	}

	/**
	 * @return the longDescription
	 */
	public String getLongDescription() {
		return longDescription;
	}

	/**
	 * @param longDescription the longDescription to set
	 */
	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	/**
	 * @return the shortDescOpus
	 */
	public String getShortDescOpus() {
		return shortDescOpus;
	}

	/**
	 * @param shortDescOpus the shortDescOpus to set
	 */
	public void setShortDescOpus(String shortDescOpus) {
		this.shortDescOpus = shortDescOpus;
	}

	/**
	 * @return the shortDescMyatt
	 */
	public String getShortDescMyatt() {
		return shortDescMyatt;
	}

	/**
	 * @param shortDescMyatt the shortDescMyatt to set
	 */
	public void setShortDescMyatt(String shortDescMyatt) {
		this.shortDescMyatt = shortDescMyatt;
	}

	/**
	 * @return the longSescServices
	 */
	public String getLongSescServices() {
		return longSescServices;
	}

	/**
	 * @param longSescServices the longSescServices to set
	 */
	public void setLongSescServices(String longSescServices) {
		this.longSescServices = longSescServices;
	}

	/**
	 * @return the shortDescService
	 */
	public String getShortDescService() {
		return shortDescService;
	}

	/**
	 * @param shortDescService the shortDescService to set
	 */
	public void setShortDescService(String shortDescService) {
		this.shortDescService = shortDescService;
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

	@JsonIgnore
	public boolean isEmpty() {
		return StringUtils.isBlank(longDesc) && StringUtils.isBlank(shortDesc);
	}

}
