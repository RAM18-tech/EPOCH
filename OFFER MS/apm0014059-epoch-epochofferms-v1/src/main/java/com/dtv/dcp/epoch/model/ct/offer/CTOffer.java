package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Additonals;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CTOffer implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The id. */
	private String id;

	/** The code. */
	private String code;

	/** The status. */
	private String status;

	/** The name. */
	private GenericLocaleBase name;

	/** The description. */
	private GenericLocaleBase description;

	/** The startDate. */
	private String startDate;

	/** The endDate. */
	private String endDate;

	/** The attributes. */
	private OfferAttributes attributes;

	/** The duplicatedCTOfferIds. */
	private List<String> duplicatedCTOfferIds;

	/** The additonals. */
	@JsonProperty("additionals")
	private Additonals additionals;

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
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the name
	 */
	public GenericLocaleBase getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(GenericLocaleBase name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public GenericLocaleBase getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}

	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the attributes
	 */
	public OfferAttributes getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(OfferAttributes attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the duplicatedCTOfferIds
	 */
	public List<String> getDuplicatedCTOfferIds() {
		return duplicatedCTOfferIds;
	}

	/**
	 * @param duplicatedCTOfferIds the duplicatedCTOfferIds to set
	 */
	public void setDuplicatedCTOfferIds(List<String> duplicatedCTOfferIds) {
		this.duplicatedCTOfferIds = duplicatedCTOfferIds;
	}

	/**
	 * @return the additionals
	 */
	public Additonals getAdditionals() {
		return additionals;
	}

	/**
	 * @param additionals the additionals to set
	 */
	public void setAdditionals(Additonals additionals) {
		this.additionals = additionals;
	}

}
