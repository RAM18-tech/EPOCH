package com.dtv.dcp.epoch.model.common.response.ucc;

import com.dtv.dcp.epoch.model.ct.offer.AdditionalEligibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Offer implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String code;
	private  String action;
	private String validationMessage;
	private String offerProductType;
	private DisclosureMessagesByKey disclosureMessagesByKey;
	private DescriptionByKey descriptionsByKey;
	private DisplayNamesByKey displayNamesBykey;
	private String offerPreselectDesignation;
	private Boolean specialoffer;
	private AssociatedProduct associatedProducts;
	private List<Benefit> benefits;

	private List<AdditionalEligibility> additionalEligibility;
	private Boolean delayProvisioning;

	public Boolean getDelayProvisioning() {
		return delayProvisioning;
	}

	public void setDelayProvisioning(Boolean delayProvisioning) {
		this.delayProvisioning = delayProvisioning;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getValidationMessage() {
		return validationMessage;
	}

	public void setValidationMessage(String validationMessage) {
		this.validationMessage = validationMessage;
	}

	public Boolean getSpecialoffer() {
		return specialoffer;
	}

	public void setSpecialoffer(Boolean specialoffer) {
		this.specialoffer = specialoffer;
	}

	public List<AdditionalEligibility> getAdditionalEligibility() {
		return additionalEligibility;
	}

	public void setAdditionalEligibility(List<AdditionalEligibility> additionalEligibility) {
		this.additionalEligibility = additionalEligibility;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getOfferProductType() {
		return offerProductType;
	}

	public void setOfferProductType(String offerProductType) {
		this.offerProductType = offerProductType;
	}

	public DisclosureMessagesByKey getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public DescriptionByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}

	public void setDescriptionsByKey(DescriptionByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}

	public DisplayNamesByKey getDisplayNamesBykey() {
		return displayNamesBykey;
	}

	public void setDisplayNamesBykey(DisplayNamesByKey displayNamesBykey) {
		this.displayNamesBykey = displayNamesBykey;
	}

	public String getOfferPreselectDesignation() {
		return offerPreselectDesignation;
	}

	public void setOfferPreselectDesignation(String offerPreselectDesignation) {
		this.offerPreselectDesignation = offerPreselectDesignation;
	}
	public AssociatedProduct getAssociatedProducts() {
		return associatedProducts;
	}

	public void setAssociatedProducts(AssociatedProduct associatedProducts) {
		this.associatedProducts = associatedProducts;
	}

	public List<Benefit> getBenefits() {
		return benefits;
	}

	public void setBenefits(List<Benefit> benefits) {
		this.benefits = benefits;
	}
}