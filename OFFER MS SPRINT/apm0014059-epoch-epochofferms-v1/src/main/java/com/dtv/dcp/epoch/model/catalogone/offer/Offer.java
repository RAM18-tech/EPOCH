package com.dtv.dcp.epoch.model.catalogone.offer;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.Benefit;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private String id;
	private String code;
	private GenericLocaleBase name;
	private GenericLocaleBase description;
	private String startDate;
	private String endDate;
	private Boolean isBundle;
	private Boolean mustBeBundled;
	private ValidFor validFor;
	private Boolean isPrimary;
	private Integer rank;
	private String productOfferingType;
	private String productOfferingTypeGroup;
	private List<CatalogPrice> productPrice;
	private List<CatalogPrice> totalPrice;
	private List<CategoryRef> categoryRef;
	private List<CatalogAttribute> attributes;
	private ProductSpecification productSpecification;
	private SelectionOption selectionOptions;
	private List<Offer> bundledProducts;
	private List<Offer> bundledProductGroups;
	private List<Benefit> benefits;
	private String offerClassification;
	private String relationType;
	private String relationRole;
	private String relationSubType;
	private String relatedID;

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
	 * @return the isBundle
	 */
	public Boolean getIsBundle() {
		return isBundle;
	}

	/**
	 * @param isBundle the isBundle to set
	 */
	public void setIsBundle(Boolean isBundle) {
		this.isBundle = isBundle;
	}

	/**
	 * @return the mustBeBundled
	 */
	public Boolean getMustBeBundled() {
		return mustBeBundled;
	}

	/**
	 * @param mustBeBundled the mustBeBundled to set
	 */
	public void setMustBeBundled(Boolean mustBeBundled) {
		this.mustBeBundled = mustBeBundled;
	}

	/**
	 * @return the validFor
	 */
	public ValidFor getValidFor() {
		return validFor;
	}

	/**
	 * @param validFor the validFor to set
	 */
	public void setValidFor(ValidFor validFor) {
		this.validFor = validFor;
	}

	/**
	 * @return the isPrimary
	 */
	public Boolean getIsPrimary() {
		return isPrimary;
	}

	/**
	 * @param isPrimary the isPrimary to set
	 */
	public void setIsPrimary(Boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	/**
	 * @return the rank
	 */
	public Integer getRank() {
		return rank;
	}

	/**
	 * @param rank the rank to set
	 */
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	/**
	 * @return the productOfferingType
	 */
	public String getProductOfferingType() {
		return productOfferingType;
	}

	/**
	 * @param productOfferingType the productOfferingType to set
	 */
	public void setProductOfferingType(String productOfferingType) {
		this.productOfferingType = productOfferingType;
	}

	/**
	 * @return the productOfferingTypeGroup
	 */
	public String getProductOfferingTypeGroup() {
		return productOfferingTypeGroup;
	}

	/**
	 * @param productOfferingTypeGroup the productOfferingTypeGroup to set
	 */
	public void setProductOfferingTypeGroup(String productOfferingTypeGroup) {
		this.productOfferingTypeGroup = productOfferingTypeGroup;
	}

	/**
	 * @return the productPrice
	 */
	public List<CatalogPrice> getProductPrice() {
		return productPrice;
	}

	/**
	 * @param productPrice the productPrice to set
	 */
	public void setProductPrice(List<CatalogPrice> productPrice) {
		this.productPrice = productPrice;
	}

	/**
	 * @return the totalPrice
	 */
	public List<CatalogPrice> getTotalPrice() {
		return totalPrice;
	}

	/**
	 * @param totalPrice the totalPrice to set
	 */
	public void setTotalPrice(List<CatalogPrice> totalPrice) {
		this.totalPrice = totalPrice;
	}

	/**
	 * @return the categoryRef
	 */
	public List<CategoryRef> getCategoryRef() {
		return categoryRef;
	}

	/**
	 * @param categoryRef the categoryRef to set
	 */
	public void setCategoryRef(List<CategoryRef> categoryRef) {
		this.categoryRef = categoryRef;
	}

	/**
	 * @return the attributes
	 */
	public List<CatalogAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<CatalogAttribute> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return the productSpecification
	 */
	public ProductSpecification getProductSpecification() {
		return productSpecification;
	}

	/**
	 * @param productSpecification the productSpecification to set
	 */
	public void setProductSpecification(ProductSpecification productSpecification) {
		this.productSpecification = productSpecification;
	}

	/**
	 * @return the selectionOptions
	 */
	public SelectionOption getSelectionOptions() {
		return selectionOptions;
	}

	/**
	 * @param selectionOptions the selectionOptions to set
	 */
	public void setSelectionOptions(SelectionOption selectionOptions) {
		this.selectionOptions = selectionOptions;
	}

	/**
	 * @return the bundledProducts
	 */
	public List<Offer> getBundledProducts() {
		return bundledProducts;
	}

	/**
	 * @param bundledProducts the bundledProducts to set
	 */
	public void setBundledProducts(List<Offer> bundledProducts) {
		this.bundledProducts = bundledProducts;
	}

	/**
	 * @return the bundledProductGroups
	 */
	public List<Offer> getBundledProductGroups() {
		return bundledProductGroups;
	}

	/**
	 * @param bundledProductGroups the bundledProductGroups to set
	 */
	public void setBundledProductGroups(List<Offer> bundledProductGroups) {
		this.bundledProductGroups = bundledProductGroups;
	}

	/**
	 * @return the benefits
	 */
	public List<Benefit> getBenefits() {
		return benefits;
	}

	/**
	 * @param benefits the benefits to set
	 */
	public void setBenefits(List<Benefit> benefits) {
		this.benefits = benefits;
	}

	/**
	 * @return the offerClassification
	 */
	public String getOfferClassification() {
		return offerClassification;
	}

	/**
	 * @param offerClassification the offerClassification to set
	 */
	public void setOfferClassification(String offerClassification) {
		this.offerClassification = offerClassification;
	}

	/**
	 * @return the relationType
	 */
	public String getRelationType() {
		return relationType;
	}

	/**
	 * @param relationType the relationType to set
	 */
	public void setRelationType(String relationType) {
		this.relationType = relationType;
	}

	/**
	 * @return the relationRole
	 */
	public String getRelationRole() {
		return relationRole;
	}

	/**
	 * @param relationRole the relationRole to set
	 */
	public void setRelationRole(String relationRole) {
		this.relationRole = relationRole;
	}

	/**
	 * @return the relationSubType
	 */
	public String getRelationSubType() {
		return relationSubType;
	}

	/**
	 * @param relationSubType the relationSubType to set
	 */
	public void setRelationSubType(String relationSubType) {
		this.relationSubType = relationSubType;
	}

	/**
	 * @return the relatedID
	 */
	public String getRelatedID() {
		return relatedID;
	}

	/**
	 * @param relatedID the relat edID to set
	 */
	public void setRelatedID(String relatedID) {
		this.relatedID = relatedID;
	}

	@Override
	public String toString() {
		return "Offer [id=" + id + ", code=" + code + ", name=" + name + ", description=" + description + ", startDate="
				+ startDate + ", endDate=" + endDate + ", isBundle=" + isBundle + ", mustBeBundled=" + mustBeBundled
				+ ", validFor=" + validFor + ", isPrimary=" + isPrimary + ", rank=" + rank + ", productOfferingType="
				+ productOfferingType + ", productOfferingTypeGroup=" + productOfferingTypeGroup + ", productPrice="
				+ productPrice + ", totalPrice=" + totalPrice + ", categoryRef=" + categoryRef + ", attributes="
				+ attributes + ", productSpecification=" + productSpecification + ", selectionOptions="
				+ selectionOptions + ", bundledProducts=" + bundledProducts + ", bundledProductGroups="
				+ bundledProductGroups + ", benefits=" + benefits + ", offerClassification=" + offerClassification
				+ ", relationType=" + relationType + ", relationRole=" + relationRole + ", relationSubType="
				+ relationSubType + ", relatedID=" + relatedID + "]";
	}

}
