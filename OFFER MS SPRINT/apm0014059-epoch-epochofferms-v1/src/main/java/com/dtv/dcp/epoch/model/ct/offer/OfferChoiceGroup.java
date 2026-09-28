package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferChoiceGroup implements Serializable
{
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    private String groupName, groupSelection, groupDesignation, groupSelectionCount;

	private String groupDisplayRank;

	private String groupDisplayName;

	private Boolean groupOffersForDisplay;

    private DisclosureMessagesByKey disclosureMessagesByKey;
    
    private List<String> choiceGroupSalesChannel;
    private List<String> impactedPromoCode, promosOnSelection, promosOnDeselection;

	private List<String> offerCodes;

	public String getGroupName() {
		return groupName;
	}

	public List<String> getChoiceGroupSalesChannel() {
	return choiceGroupSalesChannel;}

	public void setSalesChannel(List<String> choiceGroupSalesChannel) {
	this.choiceGroupSalesChannel = choiceGroupSalesChannel;}

	public List<String> getImpactedPromoCode() {
	return impactedPromoCode;}

	public void setImpactedPromoCode(List<String> impactedPromoCode) {
	this.impactedPromoCode = impactedPromoCode;}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getGroupSelection() {
		return groupSelection;
	}

	public void setGroupSelection(String groupSelection) {
		this.groupSelection = groupSelection;
	}

	public String getGroupDesignation() {
		return groupDesignation;
	}

	public void setGroupDesignation(String groupDesignation) {
		this.groupDesignation = groupDesignation;
	}

	public String getGroupSelectionCount() {
		return groupSelectionCount;
	}

	public void setGroupSelectionCount(String groupSelectionCount) {
		this.groupSelectionCount = groupSelectionCount;
	}

	public DisclosureMessagesByKey getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public List<String> getPromosOnSelection() {
		return promosOnSelection;
	}

	public void setPromosOnSelection(List<String> promosOnSelection) {
		this.promosOnSelection = promosOnSelection;
	}

	public List<String> getPromosOnDeselection() {
		return promosOnDeselection;
	}

	public void setPromosOnDeselection(List<String> promosOnDeselection) {
		this.promosOnDeselection = promosOnDeselection;
	}

	public List<String> getOfferCodes() {
		return offerCodes;
	}

	public void setOfferCodes(List<String> offerCodes) {
		this.offerCodes = offerCodes;
	}

	public String getGroupDisplayRank() {
		return groupDisplayRank;
	}

	public void setGroupDisplayRank(String groupDisplayRank) {
		this.groupDisplayRank = groupDisplayRank;
	}

	public Boolean getGroupOffersForDisplay() {
		return groupOffersForDisplay;
	}

	public void setGroupOffersForDisplay(Boolean groupOffersForDisplay) {
		this.groupOffersForDisplay = groupOffersForDisplay;
	}

	public String getGroupDisplayName() {
		return groupDisplayName;
	}

	public void setGroupDisplayName(String groupDisplayName) {
		this.groupDisplayName = groupDisplayName;
	}
}