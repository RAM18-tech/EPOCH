package com.dtv.dcp.epoch.model.common.validatecoupons;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.CompatibleProductNameValue;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.AssociatedProduct;
import com.dtv.dcp.epoch.model.ct.offer.OfferPrice;
import com.dtv.dcp.epoch.model.ct.offer.RSNFeeDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CouponOfferAttributes implements Serializable {


    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The offerProductType.
     */
    private String offerProductType;
    
    /** The offer product types. */
    private List<String> offerProductTypes;

    /**
     * The offer product family.
     */
    private String offerProductFamily;
    
    /** The offer product families. */
    private List<String> offerProductFamilies;

    /**
     * The offer status.
     */
    private String offerStatus;

    /**
     * The cpc offer id.
     */
    private String cpcOfferId;

    /**
     * The predicate rule.
     */
    private String predicateRule;

    /**
     * The offerProductSubtype.
     */
    private String offerProductSubtype;

    /**
     * The offerType.
     */
    private String offerType;

    /**
     * The offerActionType.
     */
    private String offerActionType;
    
    /** The offer action types. */
    private List<String> offerActionTypes;
    
    /** The offer action types. */
    @JsonInclude(Include.NON_EMPTY)
    private List<String> filterOffersWhenEligible;

    /**
     * The environment.
     */
    private String environment;

    /**
     * The offerCategory.
     */
    private GenericTypeIdBase offerCategory;

    /**
     * The rank.
     */
    private String rank;

    /**
     * The displayName.
     */
    private GenericLocaleBase displayName;

    /**
     * The displayNamesByKey.
     */
    private GenericByKey displayNamesByKey;

    /**
     * The descriptionsByKey.
     */
    private GenericByKey descriptionsByKey;

    /** The disclaimer. */
    // private String disclaimer;

    /**
     * The termsAndConditions.
     */
    private String termsAndConditions;

    /**
     * The opusDisclosureMessage.
     */
    private String opusDisclosureMessage;

    /**
     * The myAttDisclosureMessage.
     */
    private String myAttDisclosureMessage;

    /**
     * The billingId.
     */
    private String billingId;

    /**
     * The billingCode.
     */
    private String billingCode;

    /**
     * The eligibleSOCs.
     */
    private List<String> eligibleSOCs;

    /**
     * The disqualifyingSOCs.
     */
    private List<String> disqualifyingSOCs;

    /**
     * The migratedOffer.
     */
    private boolean migratedOffer;

    /**
     * The provisioningCodes.
     */
    private List<String> provisioningCodes;

    /**
     * The associatedProducts.
     */
    private List<AssociatedProduct> associatedProducts;

    /**
     * The associatedProducts.
     */
    private List<Benefit> benefits;

    /**
     * The eligibility.
     */
    private Eligibility eligibility;

    /**
     * The stackableOffers.
     */
    private List<GenericTypeIdBase> stackableOffers;

    /**
     * The stackableOffersCategories.
     */
    private List<GenericTypeIdBase> stackableOffersCategories;

    /**
     * The conflictingOffers.
     */
    private List<GenericTypeIdBase> conflictingOffers;

    /**
     * The conflictingOffersCategories.
     */
    private List<GenericTypeIdBase> conflictingOffersCategories;

    /**
     * The iptvCustomerType.
     */
    private String iptvCustomerType;

    /**
     * The offerPromos.
     */
    private List<List<GenericNameValueBase>> offerPromos;

    private List<String> retIoApplicableProducts;

    /**
     * The isVirtual.
     */
    @JsonProperty("isVirtual")
    private boolean virtualOffer;

    /**
     * The leadOffer.
     */
    private boolean leadOffer;

    /** The offerPrice. */
    private OfferPrice offerPrice;

    /** The contractIndicator. */
    private String contractIndicator;

    /** The externalConflictOfferIds. */
    private List<String> externalConflictOfferIds;

    /** The externalConflictOfferCategories. */
    private List<String> externalConflictOfferCategories;

    /**
     * The compatibleProducts.
     */
    @JsonProperty("compatibleProductsList")
    private List<CompatibleProductNameValue> compatibleProducts;

    /** The Flash Sale offer. */
    @JsonProperty("flashSaleOffer")
    private boolean flashSaleOffer;


    /** The mobility conflicting products. */
    private List<String> mobilityConflictingProducts;

    /** The residential conflicting products. */
    private List<String> residentialConflictingProducts;

    /** The employee conflicting products. */
    private List<String> employeeConflictingProducts;
    
	/** The offer intent. */
	private String offerIntent;
	
	/** The offer intents. */
	private List<String> offerIntents;

	/** The migration service type. */
	private List<String> migrationServiceType;

	/** The migration service offer type. */
	private String migrationServiceOfferType;

	private String offerClassificationType;
	
	/** The offerPreselectDesignation. */
	private String offerPreselectDesignation;
	
	/** The benefitCodesToSuppressOffer. */
	private List<String> benefitCodesToSuppressOffer;
	
	private String paymentType;
	
	private String portIn;
	
	private List<String> fanCategories;
	
	private List<String> customerSubtypes;
	
	private List<String> qualifyingSku;
	
	private List<String> excludedCustomerSubTypes;
	
	private List<String> enrollmentType;
	
	private String upsellOfferATGid;
	
	@JsonProperty("isDiscountRolledUp")
	private Boolean isDiscountRolledUp;
	
	@JsonProperty("isOfferTrayEnabled")
	private Boolean isOfferTrayEnabled;
	
	private Boolean visible;
	
	@JsonProperty("useForPriceCalculation")
	private Boolean useForPriceCalculation;
	
    public Boolean getUseForPriceCalculation() {
		return useForPriceCalculation;
	}

	public void setUseForPriceCalculation(Boolean useForPriceCalculation) {
		this.useForPriceCalculation = useForPriceCalculation;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the benefitCodesToSuppressOffer
	 */
	public List<String> getBenefitCodesToSuppressOffer() {
		return benefitCodesToSuppressOffer;
	}

	/**
	 * @param benefitCodesToSuppressOffer the benefitCodesToSuppressOffer to set
	 */
	public void setBenefitCodesToSuppressOffer(List<String> benefitCodesToSuppressOffer) {
		this.benefitCodesToSuppressOffer = benefitCodesToSuppressOffer;
	}

	/**
	 * @return the offerPreselectDesignation
	 */
	public String getOfferPreselectDesignation() {
		return offerPreselectDesignation;
	}

	/**
	 * @param offerPreselectDesignation the offerPreselectDesignation to set
	 */
	public void setOfferPreselectDesignation(String offerPreselectDesignation) {
		this.offerPreselectDesignation = offerPreselectDesignation;
	}

	public String getOfferClassificationType() {
		return offerClassificationType;
	}

	public void setOfferClassificationType(String offerClassificationType) {
		this.offerClassificationType = offerClassificationType;
	}


	/** The rsn fee details. */
	@JsonProperty("rsnFeeDetails")
	private RSNFeeDetails rsnFeeDetails;

    public List<String> getRetIoApplicableProducts() {
        return retIoApplicableProducts;
    }

    public void setRetIoApplicableProducts(List<String> retIoApplicableProducts) {
        this.retIoApplicableProducts = retIoApplicableProducts;
    }

    /**
	 * Gets the rsn fee details.
	 *
	 * @return the rsn fee details
	 */
	public RSNFeeDetails getRsnFeeDetails() {
		return rsnFeeDetails;
	}

	/**
	 * Sets the rsn fee details.
	 *
	 * @param rsnFeeDetails the new rsn fee details
	 */
	public void setRsnFeeDetails(RSNFeeDetails rsnFeeDetails) {
		this.rsnFeeDetails = rsnFeeDetails;

	}

	/**
     * Gets the offer intent.
     *
     * @return the offer intent
     */
    public String getOfferIntent() {
		return offerIntent;
	}

	/**
	 * Sets the offer intent.
	 *
	 * @param offerIntent the new offer intent
	 */
	public void setOfferIntent(String offerIntent) {
		this.offerIntent = offerIntent;
	}

	
	public List<String> getOfferIntents() {
		return offerIntents;
	}

	public void setOfferIntents(List<String> offerIntents) {
		this.offerIntents = offerIntents;
	}

	/**
	 * Gets the migration service type.
	 *
	 * @return the migration service type
	 */
	public List<String> getMigrationServiceType() {
		return migrationServiceType;
	}

	/**
	 * Sets the migration service type.
	 *
	 * @param migrationServiceType the new migration service type
	 */
	public void setMigrationServiceType(List<String> migrationServiceType) {
		this.migrationServiceType = migrationServiceType;
	}

	/**
	 * Gets the migration service offer type.
	 *
	 * @return the migration service offer type
	 */
	public String getMigrationServiceOfferType() {
		return migrationServiceOfferType;
	}

	/**
	 * Sets the migration service offer type.
	 *
	 * @param migrationServiceOfferType the new migration service offer type
	 */
	public void setMigrationServiceOfferType(String migrationServiceOfferType) {
		this.migrationServiceOfferType = migrationServiceOfferType;
	}

	/**
	 * Gets the mobility conflicting products.
	 *
	 * @return the mobility conflicting products
	 */
	public List<String> getMobilityConflictingProducts() {
        return mobilityConflictingProducts;
    }

    /**
     * Sets the mobility conflicting products.
     *
     * @param mobilityConflictingProducts the new mobility conflicting products
     */
    public void setMobilityConflictingProducts(List<String> mobilityConflictingProducts) {
        this.mobilityConflictingProducts = mobilityConflictingProducts;
    }

    /**
     * Gets the residential conflicting products.
     *
     * @return the residential conflicting products
     */
    public List<String> getResidentialConflictingProducts() {
        return residentialConflictingProducts;
    }

    /**
     * Sets the residential conflicting products.
     *
     * @param residentialConflictingProducts the new residential conflicting products
     */
    public void setResidentialConflictingProducts(List<String> residentialConflictingProducts) {
        this.residentialConflictingProducts = residentialConflictingProducts;
    }

    /**
     * Gets the employee conflicting products.
     *
     * @return the employee conflicting products
     */
    public List<String> getEmployeeConflictingProducts() {
        return employeeConflictingProducts;
    }

    /**
     * Sets the employee conflicting products.
     *
     * @param employeeConflictingProducts the new employee conflicting products
     */
    public void setEmployeeConflictingProducts(List<String> employeeConflictingProducts) {
        this.employeeConflictingProducts = employeeConflictingProducts;
    }

    /**
     * Checks if is flash sale offer.
     *
     * @return true, if is flash sale offer
     */
    public boolean isFlashSaleOffer() {
        return flashSaleOffer;
    }

    /**
     * Sets the flash sale offer.
     *
     * @param flashSaleOffer the new flash sale offer
     */
    public void setFlashSaleOffer(boolean flashSaleOffer) {
        this.flashSaleOffer = flashSaleOffer;
    }

    /**
     * Gets the compatible products.
     *
     * @return the compatibleProducts
     */
    public List<CompatibleProductNameValue> getCompatibleProducts() {
        return compatibleProducts;
    }

    /**
     * Sets the compatible products.
     *
     * @param compatibleProducts the compatibleProducts to set
     */
    public void setCompatibleProducts(List<CompatibleProductNameValue> compatibleProducts) {
        this.compatibleProducts = compatibleProducts;
    }
    /**
     * Checks if is virtual offer.
     *
     * @return the virtualOffer
     */
    public boolean isVirtualOffer() {
        return virtualOffer;
    }

    /**
     * Sets the virtual offer.
     *
     * @param virtualOffer the virtualOffer to set
     */
    public void setVirtualOffer(boolean virtualOffer) {
        this.virtualOffer = virtualOffer;
    }

    /**
     * Gets the offer promos.
     *
     * @return the offerPromos
     */
    public List<List<GenericNameValueBase>> getOfferPromos() {
        return offerPromos;
    }

    /**
     * Sets the offer promos.
     *
     * @param offerPromos the offerPromos to set
     */
    public void setOfferPromos(List<List<GenericNameValueBase>> offerPromos) {
        this.offerPromos = offerPromos;
    }

    /**
     * Gets the iptv customer type.
     *
     * @return the iptvCustomerType
     */
    public String getIptvCustomerType() {
        return iptvCustomerType;
    }

    /**
     * Sets the iptv customer type.
     *
     * @param iptvCustomerType the iptvCustomerType to set
     */
    public void setIptvCustomerType(String iptvCustomerType) {
        this.iptvCustomerType = iptvCustomerType;
    }

    /**
     * Gets the offer product type.
     *
     * @return the offerProductType
     */
   

    /**
     * Gets the offer product subtype.
     *
     * @return the offerProductSubtype
     */
    public String getOfferProductSubtype() {
        return offerProductSubtype;
    }

    /**
     * Sets the offer product subtype.
     *
     * @param offerProductSubtype the offerProductSubtype to set
     */
    public void setOfferProductSubtype(String offerProductSubtype) {
        this.offerProductSubtype = offerProductSubtype;
    }

    /**
     * Gets the offer type.
     *
     * @return the offerType
     */
    public String getOfferType() {
        return offerType;
    }

    /**
     * Sets the offer type.
     *
     * @param offerType the offerType to set
     */
    public void setOfferType(String offerType) {
        this.offerType = offerType;
    }

    /**
     * Gets the offer action type.
     *
     * @return the offerActionType
     */
    public String getOfferActionType() {
        return offerActionType;
    }

    /**
     * Sets the offer action type.
     *
     * @param offerActionType the offerActionType to set
     */
    public void setOfferActionType(String offerActionType) {
        this.offerActionType = offerActionType;
    }

    /**
     * Gets the environment.
     *
     * @return the environment
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * Sets the environment.
     *
     * @param environment the environment to set
     */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * Gets the offer category.
     *
     * @return the offerCategory
     */
    public GenericTypeIdBase getOfferCategory() {
        return offerCategory;
    }

    /**
     * Sets the offer category.
     *
     * @param offerCategory the offerCategory to set
     */
    public void setOfferCategory(GenericTypeIdBase offerCategory) {
        this.offerCategory = offerCategory;
    }

    /**
     * Gets the rank.
     *
     * @return the rank
     */
    public String getRank() {
        return rank;
    }

    /**
     * Sets the rank.
     *
     * @param rank the rank to set
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * Gets the display name.
     *
     * @return the displayName
     */
    public GenericLocaleBase getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     *
     * @param displayName the displayName to set
     */
    public void setDisplayName(GenericLocaleBase displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the display names by key.
     *
     * @return the displayNamesByKey
     */
    public GenericByKey getDisplayNamesByKey() {
        return displayNamesByKey;
    }

    /**
     * Sets the display names by key.
     *
     * @param displayNamesByKey the displayNamesByKey to set
     */
    public void setDisplayNamesByKey(GenericByKey displayNamesByKey) {
        this.displayNamesByKey = displayNamesByKey;
    }

    /**
     * Gets the descriptions by key.
     *
     * @return the descriptionsByKey
     */
    public GenericByKey getDescriptionsByKey() {
        return descriptionsByKey;
    }

    /**
     * Sets the descriptions by key.
     *
     * @param descriptionsByKey the descriptionsByKey to set
     */
    public void setDescriptionsByKey(GenericByKey descriptionsByKey) {
        this.descriptionsByKey = descriptionsByKey;
    }

    /**
     * Gets the terms and conditions.
     *
     * @return the disclaimer
     */
    /*
     * public String getDisclaimer() { return disclaimer; }
     *
     *//**
     * @param disclaimer the disclaimer to set
     *//*
     * public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer;
     * }
     */

    /**
     * @return the termsAndConditions
     */
    public String getTermsAndConditions() {
        return termsAndConditions;
    }

    /**
     * Sets the terms and conditions.
     *
     * @param string the termsAndConditions to set
     */
    public void setTermsAndConditions(String string) {
        this.termsAndConditions = string;
    }

    /**
     * Gets the opus disclosure message.
     *
     * @return the opusDisclosureMessage
     */
    public String getOpusDisclosureMessage() {
        return opusDisclosureMessage;
    }

    /**
     * Sets the opus disclosure message.
     *
     * @param opusDisclosureMessage the opusDisclosureMessage to set
     */
    public void setOpusDisclosureMessage(String opusDisclosureMessage) {
        this.opusDisclosureMessage = opusDisclosureMessage;
    }

    /**
     * Gets the my att disclosure message.
     *
     * @return the myAttDisclosureMessage
     */
    public String getMyAttDisclosureMessage() {
        return myAttDisclosureMessage;
    }

    /**
     * Sets the my att disclosure message.
     *
     * @param myAttDisclosureMessage the myAttDisclosureMessage to set
     */
    public void setMyAttDisclosureMessage(String myAttDisclosureMessage) {
        this.myAttDisclosureMessage = myAttDisclosureMessage;
    }

    /**
     * Gets the billing id.
     *
     * @return the billingId
     */
    public String getBillingId() {
        return billingId;
    }

    /**
     * Sets the billing id.
     *
     * @param billingId the billingId to set
     */
    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    /**
     * Gets the billing code.
     *
     * @return the billingCode
     */
    public String getBillingCode() {
        return billingCode;
    }

    /**
     * Sets the billing code.
     *
     * @param billingCode the billingCode to set
     */
    public void setBillingCode(String billingCode) {
        this.billingCode = billingCode;
    }

    /**
     * Gets the eligible SO cs.
     *
     * @return the eligibleSOCs
     */
    public List<String> getEligibleSOCs() {
        return eligibleSOCs;
    }

    /**
     * Sets the eligible SO cs.
     *
     * @param eligibleSOCs the eligibleSOCs to set
     */
    public void setEligibleSOCs(List<String> eligibleSOCs) {
        this.eligibleSOCs = eligibleSOCs;
    }

    /**
     * Gets the disqualifying SO cs.
     *
     * @return the disqualifyingSOCs
     */
    public List<String> getDisqualifyingSOCs() {
        return disqualifyingSOCs;
    }

    /**
     * Sets the disqualifying SO cs.
     *
     * @param disqualifyingSOCs the disqualifyingSOCs to set
     */
    public void setDisqualifyingSOCs(List<String> disqualifyingSOCs) {
        this.disqualifyingSOCs = disqualifyingSOCs;
    }

    /**
     * Checks if is migrated offer.
     *
     * @return the migratedOffer
     */
    public boolean isMigratedOffer() {
        return migratedOffer;
    }

    /**
     * Sets the migrated offer.
     *
     * @param migratedOffer the migratedOffer to set
     */
    public void setMigratedOffer(boolean migratedOffer) {
        this.migratedOffer = migratedOffer;
    }

    /**
     * Gets the provisioning codes.
     *
     * @return the provisioningCodes
     */
    public List<String> getProvisioningCodes() {
        return provisioningCodes;
    }

    /**
     * Sets the provisioning codes.
     *
     * @param provisioningCodes the provisioningCodes to set
     */
    public void setProvisioningCodes(List<String> provisioningCodes) {
        this.provisioningCodes = provisioningCodes;
    }

    /**
     * Gets the associated products.
     *
     * @return the associatedProducts
     */
    public List<AssociatedProduct> getAssociatedProducts() {
        return associatedProducts;
    }

    /**
     * Sets the associated products.
     *
     * @param associatedProducts the associatedProducts to set
     */
    public void setAssociatedProducts(List<AssociatedProduct> associatedProducts) {
        this.associatedProducts = associatedProducts;
    }

    /**
     * Gets the benefits.
     *
     * @return the benefits
     */
    public List<Benefit> getBenefits() {
        return benefits;
    }

    /**
     * Sets the benefits.
     *
     * @param benefits the benefits to set
     */
    public void setBenefits(List<Benefit> benefits) {
        this.benefits = benefits;
    }

    /**
     * Gets the eligibility.
     *
     * @return the eligibility
     */
    public Eligibility getEligibility() {
        return eligibility;
    }

    /**
     * Sets the eligibility.
     *
     * @param eligibility the eligibility to set
     */
    public void setEligibility(Eligibility eligibility) {
        this.eligibility = eligibility;
    }

    /**
     * Gets the stackable offers.
     *
     * @return the stackableOffers
     */
    public List<GenericTypeIdBase> getStackableOffers() {
        return stackableOffers;
    }

    /**
     * Sets the stackable offers.
     *
     * @param stackableOffers the stackableOffers to set
     */
    public void setStackableOffers(List<GenericTypeIdBase> stackableOffers) {
        this.stackableOffers = stackableOffers;
    }

    /**
     * Gets the stackable offers categories.
     *
     * @return the stackableOffersCategories
     */
    public List<GenericTypeIdBase> getStackableOffersCategories() {
        return stackableOffersCategories;
    }

    /**
     * Sets the stackable offers categories.
     *
     * @param stackableOffersCategories the stackableOffersCategories to set
     */
    public void setStackableOffersCategories(List<GenericTypeIdBase> stackableOffersCategories) {
        this.stackableOffersCategories = stackableOffersCategories;
    }

    /**
     * Gets the conflicting offers.
     *
     * @return the conflictingOffers
     */
    public List<GenericTypeIdBase> getConflictingOffers() {
        return conflictingOffers;
    }

    /**
     * Sets the conflicting offers.
     *
     * @param conflictingOffers the conflictingOffers to set
     */
    public void setConflictingOffers(List<GenericTypeIdBase> conflictingOffers) {
        this.conflictingOffers = conflictingOffers;
    }

    /**
     * Gets the conflicting offers categories.
     *
     * @return the conflictingOffersCategories
     */
    public List<GenericTypeIdBase> getConflictingOffersCategories() {
        return conflictingOffersCategories;
    }

    /**
     * Sets the conflicting offers categories.
     *
     * @param conflictingOffersCategories the conflictingOffersCategories to set
     */
    public void setConflictingOffersCategories(List<GenericTypeIdBase> conflictingOffersCategories) {
        this.conflictingOffersCategories = conflictingOffersCategories;
    }

    /**
     * Gets the offer product family.
     *
     * @return the offer product family
     */
   

    /**
     * Gets the offer status.
     *
     * @return the offer status
     */
    public String getOfferStatus() {
        return offerStatus;
    }

    /**
     * Sets the offer status.
     *
     * @param offerStatus the new offer status
     */
    public void setOfferStatus(String offerStatus) {
        this.offerStatus = offerStatus;
    }

    /**
     * Gets the cpc offer id.
     *
     * @return the cpc offer id
     */
    public String getCpcOfferId() {
        return cpcOfferId;
    }

    /**
     * Sets the cpc offer id.
     *
     * @param cpcOfferId the new cpc offer id
     */
    public void setCpcOfferId(String cpcOfferId) {
        this.cpcOfferId = cpcOfferId;
    }

    /**
     * Gets the predicate rule.
     *
     * @return the predicate rule
     */
    public String getPredicateRule() {
        return predicateRule;
    }

    /**
     * Sets the predicate rule.
     *
     * @param predicateRule the new predicate rule
     */
    public void setPredicateRule(String predicateRule) {
        this.predicateRule = predicateRule;
    }

    /**
     * Checks if is lead offer.
     *
     * @return true, if is lead offer
     */
    public boolean isLeadOffer() {
        return leadOffer;
    }

    /**
     * Sets the lead offer.
     *
     * @param leadOffer the new lead offer
     */
    public void setLeadOffer(boolean leadOffer) {
        this.leadOffer = leadOffer;
    }


    /**
     * Gets the offer price.
     *
     * @return the offer price
     */
    public OfferPrice getOfferPrice() {
        return offerPrice;
    }

    /**
     * Sets the offer price.
     *
     * @param offerPrice the new offer price
     */
    public void setOfferPrice(OfferPrice offerPrice) {
        this.offerPrice = offerPrice;
    }

    /**
     * Gets the contract indicator.
     *
     * @return the contract indicator
     */
    public String getContractIndicator() {
        return contractIndicator;
    }

    /**
     * Sets the contract indicator.
     *
     * @param contractIndicator the new contract indicator
     */
    public void setContractIndicator(String contractIndicator) {
        this.contractIndicator = contractIndicator;
    }

    /**
     * Gets the external conflict offer ids.
     *
     * @return the external conflict offer ids
     */
    public List<String> getExternalConflictOfferIds() {
        return externalConflictOfferIds;
    }

    /**
     * Sets the external conflict offer ids.
     *
     * @param externalConflictOfferIds the new external conflict offer ids
     */
    public void setExternalConflictOfferIds(List<String> externalConflictOfferIds) {
        this.externalConflictOfferIds = externalConflictOfferIds;
    }

    /**
     * Gets the external conflict offer categories.
     *
     * @return the external conflict offer categories
     */
    public List<String> getExternalConflictOfferCategories() {
        return externalConflictOfferCategories;
    }

    /**
     * Sets the external conflict offer categories.
     *
     * @param externalConflictOfferCategories the new external conflict offer categories
     */
    public void setExternalConflictOfferCategories(List<String> externalConflictOfferCategories) {
        this.externalConflictOfferCategories = externalConflictOfferCategories;
    }


	/**
	 * Gets the offer product type.
	 *
	 * @return the offer product type
	 */

	public String getOfferProductType() {
		return offerProductType;
	}


	/**
	 * Sets the offer product type.
	 *
	 * @param offerProductType the new offer product type
	 */

	public void setOfferProductType(String offerProductType) {
		this.offerProductType = offerProductType;
	}


	/**
	 * Gets the offer product types.
	 *
	 * @return the offer product types
	 */

	public List<String> getOfferProductTypes() {
		return offerProductTypes;
	}


	/**
	 * Sets the offer product types.
	 *
	 * @param offerProductTypes the new offer product types
	 */

	public void setOfferProductTypes(List<String> offerProductTypes) {
		this.offerProductTypes = offerProductTypes;
	}


	/**
	 * Gets the offer product family.
	 *
	 * @return the offer product family
	 */

	public String getOfferProductFamily() {
		return offerProductFamily;
	}


	/**
	 * Sets the offer product family.
	 *
	 * @param offerProductFamily the new offer product family
	 */

	public void setOfferProductFamily(String offerProductFamily) {
		this.offerProductFamily = offerProductFamily;
	}


	/**
	 * Gets the offer product families.
	 *
	 * @return the offer product families
	 */

	public List<String> getOfferProductFamilies() {
		return offerProductFamilies;
	}


	/**
	 * Sets the offer product families.
	 *
	 * @param offerProductFamilies the new offer product families
	 */

	public void setOfferProductFamilies(List<String> offerProductFamilies) {
		this.offerProductFamilies = offerProductFamilies;
	}


	/**
	 * Gets the offer action types.
	 *
	 * @return the offer action types
	 */

	public List<String> getOfferActionTypes() {
		return offerActionTypes;
	}


	/**
	 * Sets the offer action types.
	 *
	 * @param offerActionTypes the new offer action types
	 */

	public void setOfferActionTypes(List<String> offerActionTypes) {
		this.offerActionTypes = offerActionTypes;
	}

	public List<String> getFilterOffersWhenEligible() {
		return filterOffersWhenEligible;
	}

	public void setFilterOffersWhenEligible(List<String> filterOffersWhenEligible) {
		this.filterOffersWhenEligible = filterOffersWhenEligible;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPortIn() {
		return portIn;
	}

	public void setPortIn(String portIn) {
		this.portIn = portIn;
	}

	public List<String> getFanCategories() {
		return fanCategories;
	}

	public void setFanCategories(List<String> fanCategories) {
		this.fanCategories = fanCategories;
	}

	public List<String> getCustomerSubtypes() {
		return customerSubtypes;
	}

	public void setCustomerSubtypes(List<String> customerSubtypes) {
		this.customerSubtypes = customerSubtypes;
	}

	public List<String> getQualifyingSku() {
		return qualifyingSku;
	}

	public void setQualifyingSku(List<String> qualifyingSku) {
		this.qualifyingSku = qualifyingSku;
	}

	public List<String> getExcludedCustomerSubTypes() {
		return excludedCustomerSubTypes;
	}

	public void setExcludedCustomerSubTypes(List<String> excludedCustomerSubTypes) {
		this.excludedCustomerSubTypes = excludedCustomerSubTypes;
	}

	public List<String> getEnrollmentType() {
		return enrollmentType;
	}

	public void setEnrollmentType(List<String> enrollmentType) {
		this.enrollmentType = enrollmentType;
	}

	public String getUpsellOfferATGid() {
		return upsellOfferATGid;
	}

	public void setUpsellOfferATGid(String upsellOfferATGid) {
		this.upsellOfferATGid = upsellOfferATGid;
	}

	public Boolean isDiscountRolledUp() {
		return isDiscountRolledUp;
	}

	public void setDiscountRolledUp(Boolean isDiscountRolledUp) {
		this.isDiscountRolledUp = isDiscountRolledUp;
	}

	public Boolean isOfferTrayEnabled() {
		return isOfferTrayEnabled;
	}

	public void setOfferTrayEnabled(Boolean isOfferTrayEnabled) {
		this.isOfferTrayEnabled = isOfferTrayEnabled;
	}
	private List<String> eligibilityFan;

	public List<String> getEligibilityFan() {
	return eligibilityFan;
	}

	public void setEligibilityFan(List<String> eligibilityFan) {
	this.eligibilityFan = eligibilityFan;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferAttributes [offerProductType=");
		builder.append(offerProductType);
		builder.append(", offerProductTypes=");
		builder.append(offerProductTypes);
		builder.append(", offerProductFamily=");
		builder.append(offerProductFamily);
		builder.append(", offerProductFamilies=");
		builder.append(offerProductFamilies);
		builder.append(", offerStatus=");
		builder.append(offerStatus);
		builder.append(", cpcOfferId=");
		builder.append(cpcOfferId);
		builder.append(", predicateRule=");
		builder.append(predicateRule);
		builder.append(", offerProductSubtype=");
		builder.append(offerProductSubtype);
		builder.append(", offerType=");
		builder.append(offerType);
		builder.append(", offerActionType=");
		builder.append(offerActionType);
		builder.append(", offerActionTypes=");
		builder.append(offerActionTypes);
		builder.append(", filterOffersWhenEligible=");
		builder.append(filterOffersWhenEligible);
		builder.append(", environment=");
		builder.append(environment);
		builder.append(", offerCategory=");
		builder.append(offerCategory);
		builder.append(", rank=");
		builder.append(rank);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", displayNamesByKey=");
		builder.append(displayNamesByKey);
		builder.append(", descriptionsByKey=");
		builder.append(descriptionsByKey);
		builder.append(", termsAndConditions=");
		builder.append(termsAndConditions);
		builder.append(", opusDisclosureMessage=");
		builder.append(opusDisclosureMessage);
		builder.append(", myAttDisclosureMessage=");
		builder.append(myAttDisclosureMessage);
		builder.append(", billingId=");
		builder.append(billingId);
		builder.append(", billingCode=");
		builder.append(billingCode);
		builder.append(", eligibleSOCs=");
		builder.append(eligibleSOCs);
		builder.append(", disqualifyingSOCs=");
		builder.append(disqualifyingSOCs);
		builder.append(", migratedOffer=");
		builder.append(migratedOffer);
		builder.append(", provisioningCodes=");
		builder.append(provisioningCodes);
		builder.append(", associatedProducts=");
		builder.append(associatedProducts);
		builder.append(", benefits=");
		builder.append(benefits);
		builder.append(", eligibility=");
		builder.append(eligibility);
		builder.append(", stackableOffers=");
		builder.append(stackableOffers);
		builder.append(", stackableOffersCategories=");
		builder.append(stackableOffersCategories);
		builder.append(", conflictingOffers=");
		builder.append(conflictingOffers);
		builder.append(", conflictingOffersCategories=");
		builder.append(conflictingOffersCategories);
		builder.append(", iptvCustomerType=");
		builder.append(iptvCustomerType);
		builder.append(", offerPromos=");
		builder.append(offerPromos);
		builder.append(", retIoApplicableProducts=");
		builder.append(retIoApplicableProducts);
		builder.append(", virtualOffer=");
		builder.append(virtualOffer);
		builder.append(", leadOffer=");
		builder.append(leadOffer);
		builder.append(", offerPrice=");
		builder.append(offerPrice);
		builder.append(", contractIndicator=");
		builder.append(contractIndicator);
		builder.append(", externalConflictOfferIds=");
		builder.append(externalConflictOfferIds);
		builder.append(", externalConflictOfferCategories=");
		builder.append(externalConflictOfferCategories);
		builder.append(", compatibleProducts=");
		builder.append(compatibleProducts);
		builder.append(", flashSaleOffer=");
		builder.append(flashSaleOffer);
		builder.append(", mobilityConflictingProducts=");
		builder.append(mobilityConflictingProducts);
		builder.append(", residentialConflictingProducts=");
		builder.append(residentialConflictingProducts);
		builder.append(", employeeConflictingProducts=");
		builder.append(employeeConflictingProducts);
		builder.append(", offerIntent=");
		builder.append(offerIntent);
		builder.append(", offerIntents=");
		builder.append(offerIntents);
		builder.append(", migrationServiceType=");
		builder.append(migrationServiceType);
		builder.append(", migrationServiceOfferType=");
		builder.append(migrationServiceOfferType);
		builder.append(", offerClassificationType=");
		builder.append(offerClassificationType);
		builder.append(", offerPreselectDesignation=");
		builder.append(offerPreselectDesignation);
		builder.append(", benefitCodesToSuppressOffer=");
		builder.append(benefitCodesToSuppressOffer);
		builder.append(", paymentType=");
		builder.append(paymentType);
		builder.append(", portIn=");
		builder.append(portIn);
		builder.append(", fanCategories=");
		builder.append(fanCategories);
		builder.append(", customerSubtypes=");
		builder.append(customerSubtypes);
		builder.append(", qualifyingSku=");
		builder.append(qualifyingSku);
		builder.append(", excludedCustomerSubTypes=");
		builder.append(excludedCustomerSubTypes);
		builder.append(", enrollmentType=");
		builder.append(enrollmentType);
		builder.append(", upsellOfferATGid=");
		builder.append(upsellOfferATGid);
		builder.append(", isDiscountRolledUp=");
		builder.append(isDiscountRolledUp);
		builder.append(", isOfferTrayEnabled=");
		builder.append(isOfferTrayEnabled);
		builder.append(", visible=");
		builder.append(visible);
		builder.append(", rsnFeeDetails=");
		builder.append(rsnFeeDetails);
		builder.append("]");
		return builder.toString();
	}
	
}
