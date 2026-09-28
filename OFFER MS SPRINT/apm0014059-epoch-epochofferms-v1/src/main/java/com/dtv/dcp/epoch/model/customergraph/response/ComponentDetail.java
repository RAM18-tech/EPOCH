package com.dtv.dcp.epoch.model.customergraph.response;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComponentDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ParentProductSpecContainment parentProductSpecContainment;
	private List<BillingOffer> billingOffers;
	private List<ComponentDetail> componentDetails;
	private String statusCode;
    private List<CharacteristicDetail> characteristicDetails;

	public List<CharacteristicDetail> getCharacteristicDetails() {
		return characteristicDetails;
	}

	public void setCharacteristicDetails(List<CharacteristicDetail> characteristicDetails) {
		this.characteristicDetails = characteristicDetails;
	}

	public ParentProductSpecContainment getParentProductSpecContainment() {
		return parentProductSpecContainment;
	}

	public void setParentProductSpecContainment(ParentProductSpecContainment parentProductSpecContainment) {
		this.parentProductSpecContainment = parentProductSpecContainment;
	}

	public List<BillingOffer> getBillingOffers() {
		return billingOffers;
	}

	public void setBillingOffers(List<BillingOffer> billingOffers) {
		this.billingOffers = billingOffers;
	}

	public List<ComponentDetail> getComponentDetails() {
		return componentDetails;
	}

	public void setComponentDetails(List<ComponentDetail> componentDetails) {
		this.componentDetails = componentDetails;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
}