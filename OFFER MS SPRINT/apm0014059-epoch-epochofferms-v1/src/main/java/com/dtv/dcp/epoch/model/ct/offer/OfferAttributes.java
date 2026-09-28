package com.dtv.dcp.epoch.model.ct.offer;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.OfferMinMaxQuantity;
import com.dtv.dcp.epoch.model.ct.benefit.Benefit;
import com.dtv.dcp.epoch.model.ct.eligibility.Eligibility;
import com.dtv.dcp.epoch.model.ct.generic.CompatibleProductNameValue;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.product.IncludedProduct;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

// TODO: Auto-generated Javadoc
/**
 * The Class OfferAttributes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferAttributes implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    
    private String omsOfferID;
    
    /** The offer action types. */
    private List<String> offerActionTypes;
    
    /** The offer action types. */
    @JsonInclude(Include.NON_EMPTY)
    private List<String> filterOffersWhenEligible;
    
    //private List<List<BenefitCodesToSuppressTheOffer>> benefitCodesToSuppressTheOffer;
    private JsonNode benefitCodesToSuppressTheOffer;
    
    private List<String> benefitCodes;
    
    private List<String> ineligibleAccountStatus;

    private Integer coolOffPeriod;
 

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
     private GenericLocaleBase disclaimer;

    /**
     * The termsAndConditions.
     */
    private GenericLocaleBase termsAndConditions;

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
    
    private List<List<MessageEntry>> globalMessagesByKey;
    
    private List<String> delayProvisioningReasons;
    private Boolean delayProvisioning;

    /**
     * The stackableOffers.
     */
    private List<GenericTypeIdBase> stackableOffers;

    /**
     * The stackableOfferCodes.
     */
    private List<String> stackableOfferCodes;

    /**
     * The stackableOffersCategories.
     */
    private List<GenericTypeIdBase> stackableOffersCategories;
    
    private List<String> qualifyingProductsPlanSubTypes;

    /**
     * The conflictingOffers.
     */
    private List<GenericTypeIdBase> conflictingOffers;
	/**
	 * The allowConflictingOffers.
	 */
	private List<GenericTypeIdBase> allowConflictingOffers;
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
    
    /** The contractPrice. */
    private OfferPrice contractPrice;
    
    /** The specialPromoPrice. */
    private OfferPrice specialPromoPrice;
    
    /** The discountAmount. */
    private OfferPrice discountAmount;

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
    
    private List<Product> omsFreeStbPolicy;
    
    private List<MetaData> metadata;

    private List<BundleProductAction> bundleProductAction;
    
    /** The opusStoreIds */
    private List<String> opusStoreIds;
	private List<String> opusChannels;
	private List<String> opusSubChannels;
	private List<String> opusGlobalSubChannels;
	private List<String> opusGlobalChannels;
    
    private List<Integer> dynamicUpgradeValues;
    
    private String nonStackableCodes;
    
    private Boolean targetedOffer;
    
    private RibbonTextByKey ribbonTextByKey;

	private Integer maxAddonCount;
    
    private Integer minAddonCount;
    
    private List<String> treatmentCode;
    
	private List<String> partnerDealerCode1;
	
	private List<String> partnerDealerCode2;

	private List<String> marketingSrcCode;
	
	private List<String> directIntegrationPartnerName;
	
	private List<String> dealerCode;
	
	private List<String> dealerIds;
	
	private List<String> masterDealerId;
	
	private List<String> salesChannel;
	
	private List<String> salesSubChannel;
	private List<String> salesSiteId;
	private List<String> agentId;
	private List<String> agentRole;

	public List<String> getSalesSiteId() {
		return salesSiteId;
	}

	public void setSalesSiteId(List<String> salesSiteId) {
		this.salesSiteId = salesSiteId;
	}

	private String locationId;
	
	private String locationTypeId;
	
	private Boolean forOrderMod;
    
	private List<String> ineligibleSalesSubChannel;
	
	private String commitmentDuration;
	
	private List<String> eligibleCommitmentDuration;

    private Boolean leadDeviceOffer;
	private String priceTier;
	public String getPriceTier() {
		return priceTier;
	}
	public void setPriceTier(String priceTier) {
		this.priceTier = priceTier;
	}
    
	public List<String> getIneligibleSalesSubChannel() {
		return ineligibleSalesSubChannel;
	}

	public void setIneligibleSalesSubChannel(List<String> ineligibleSalesSubChannel) {
		this.ineligibleSalesSubChannel = ineligibleSalesSubChannel;
	}

	public String getCommitmentDuration() {
		return commitmentDuration;
	}

	public void setCommitmentDuration(String commitmentDuration) {
		this.commitmentDuration = commitmentDuration;
	}

	public List<String> getEligibleCommitmentDuration() {
		return eligibleCommitmentDuration;
	}

	public void setEligibleCommitmentDuration(List<String> eligibleCommitmentDuration) {
		this.eligibleCommitmentDuration = eligibleCommitmentDuration;
	}
    
	public Boolean getLeadDeviceOffer() {
		return leadDeviceOffer;
	}

	public void setLeadDeviceOffer(Boolean leadDeviceOffer) {
		this.leadDeviceOffer = leadDeviceOffer;
	}

	public Boolean getForOrderMod() {
		return forOrderMod;
	}

	public void setForOrderMod(Boolean forOrderMod) {
		this.forOrderMod = forOrderMod;
	}
	
	public String getLocationTypeId() {
		return locationTypeId;
	}

	public void setLocationTypeId(String locationTypeId) {
		this.locationTypeId = locationTypeId;
	}

	public String getLocationId() {
		return locationId;
	}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	public List<String> getMasterDealerId() {
		return masterDealerId;
	}

	public void setMasterDealerId(List<String> masterDealerId) {
		this.masterDealerId = masterDealerId;
	}

	public List<String> getSalesSubChannel() {
		return salesSubChannel;
	}

	public void setSalesSubChannel(List<String> salesSubChannel) {
		this.salesSubChannel = salesSubChannel;
	}

	public List<String> getDealerIds() {
		return dealerIds;
	}

	public void setDealerIds(List<String> dealerIds) {
		this.dealerIds = dealerIds;
	}

	public List<String> getDealerCode() {
		return dealerCode;
	}

	public void setDealerCode(List<String> dealerCode) {
		this.dealerCode = dealerCode;
	}

	public List<String> getOpusGlobalSubChannels() {
		return opusGlobalSubChannels;
	}

	public void setOpusGlobalSubChannels(List<String> opusGlobalSubChannels) {
		this.opusGlobalSubChannels = opusGlobalSubChannels;
	}

	public List<String> getOpusGlobalChannels() {
		return opusGlobalChannels;
	}

	public void setOpusGlobalChannels(List<String> opusGlobalChannels) {
		this.opusGlobalChannels = opusGlobalChannels;
	}

	public List<String> getMarketingSrcCode() {
		return marketingSrcCode;
	}

	public void setMarketingSrcCode(List<String> marketingSrcCode) {
		this.marketingSrcCode = marketingSrcCode;
	}

    public List<String> getTreatmentCode() {
		return treatmentCode;
	}

	public void setTreatmentCode(List<String> treatmentCode) {
		this.treatmentCode = treatmentCode;
	}
    
    @JsonProperty("isBulkOffer")
    private boolean bulkOffer;

	public boolean isBulkOffer() {
		return bulkOffer;
	}

	public void setBulkOffer(boolean bulkOffer) {
		this.bulkOffer = bulkOffer;
	}
    
    private List<OfferChoiceGroup> offerChoiceGroup;
    private List<ChannelEligibilityByKey> channelEligibilityByKey;

    public List<ChannelEligibilityByKey> getChannelEligibilityByKey() {
        return channelEligibilityByKey;
    }

    public void setChannelEligibilityByKey(List<ChannelEligibilityByKey> channelEligibilityByKey) {
        this.channelEligibilityByKey = channelEligibilityByKey;
    }

    private List<OfferMinMaxQuantity> offerMinMaxQuantity;
    
    private List<GenericTypeIdBase> associatedOffers;
    
    private String rtpRtcIndicator;
    
    private String requiredProducts;
    
    private String c3OfferId;
	
	private String enablerSalesOfferId;
	
	private String replacementDeviceType;
	
	private Boolean eligibleForServedMarket;
	
	private Boolean strikeThroughOffer;
	
	private String alertMessage;
	
	private List<String> enhancedSportsPackCodes;
	
	private Boolean isLCCBasePackage;
	
	private String minCustomerEligibilityTenureInDays;

	private List<AdditionalEligibility> additionalEligibility;

	private List<DependentOffer> dependentOffers;

	public List<DependentOffer> getDependentOffers() {
		return dependentOffers;
	}

	public void setDependentOffers(List<DependentOffer> dependentOffers) {
		this.dependentOffers = dependentOffers;
	}

	public List<AdditionalEligibility> getAdditionalEligibility() {
		return additionalEligibility;
	}

	private List<DisplayType> displayTypeByKey;

	public List<DisplayType> getDisplayTypeByKey() {
		return displayTypeByKey;
	}

	public void setDisplayTypeByKey(List<DisplayType> displayTypeByKey) {
		this.displayTypeByKey = displayTypeByKey;
	}

	public void setAdditionalEligibility(List<AdditionalEligibility> additionalEligibility) {
		this.additionalEligibility = additionalEligibility;
	}

	public String getMinCustomerEligibilityTenureInDays() {
	    return minCustomerEligibilityTenureInDays;
	}

	public void setMinCustomerEligibilityTenureInDays(String minCustomerEligibilityTenureInDays) {
	    this.minCustomerEligibilityTenureInDays = minCustomerEligibilityTenureInDays;
	}

	public Boolean getIsLCCBasePackage() {
	    return isLCCBasePackage;
	}

	public void setIsLCCBasePackage(Boolean isLCCBasePackage) {
	    this.isLCCBasePackage = isLCCBasePackage;
	}
	
	public List<String> getEnhancedSportsPackCodes() {
		return enhancedSportsPackCodes;
	}

	public void setEnhancedSportsPackCodes(List<String> enhancedSportsPackCodes) {
		this.enhancedSportsPackCodes = enhancedSportsPackCodes;
	}

	public Boolean getStrikeThroughOffer() {
		return strikeThroughOffer;
	}

	public void setStrikeThroughOffer(Boolean strikeThroughOffer) {
		this.strikeThroughOffer = strikeThroughOffer;
	}

	public String getAlertMessage() {
		return alertMessage;
	}

	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}
	
	public Boolean getEligibleForServedMarket() {
		return eligibleForServedMarket;
	}

	public void setEligibleForServedMarket(Boolean eligibleForServedMarket) {
		this.eligibleForServedMarket = eligibleForServedMarket;
	}
	
    public String getReplacementDeviceType() {
		return replacementDeviceType;
	}

	public void setReplacementDeviceType(String replacementDeviceType) {
		this.replacementDeviceType = replacementDeviceType;
	}
	
	@JsonProperty("isADEEligible")
	private Boolean adeEligible;
    
    public String getC3OfferId() {
		return c3OfferId;
	}

	public void setC3OfferId(String c3OfferId) {
		this.c3OfferId = c3OfferId;
	}

	public String getEnablerSalesOfferId() {
		return enablerSalesOfferId;
	}

	public void setEnablerSalesOfferId(String enablerSalesOfferId) {
		this.enablerSalesOfferId = enablerSalesOfferId;
	}

	public Boolean getAdeEligible() {
		return adeEligible;
	}

	public void setAdeEligible(Boolean adeEligible) {
		this.adeEligible = adeEligible;}

	/**
	 * @return the requiredProducts
	 */
	public String getRequiredProducts() {
		return requiredProducts;
	}

	/**
	 * @param requiredProducts the requiredProducts to set
	 */
	public void setRequiredProducts(String requiredProducts) {
		this.requiredProducts = requiredProducts;
	}

	/**
	 * @return the discountAmount
	 */
	public OfferPrice getDiscountAmount() {
		return discountAmount;
	}

	/**
	 * @param discountAmount the discountAmount to set
	 */
	public void setDiscountAmount(OfferPrice discountAmount) {
		this.discountAmount = discountAmount;
	}

	/**
	 * @return the associatedOffers
	 */
	public List<GenericTypeIdBase> getAssociatedOffers() {
		return associatedOffers;
	}

	/**
	 * @param associatedOffers the associatedOffers to set
	 */
	public void setAssociatedOffers(List<GenericTypeIdBase> associatedOffers) {
		this.associatedOffers = associatedOffers;
	}

	private List<OnlinePartnerDetails> onlinePartnerDetails;

	private List<String> opusDealerID1;
	private List<String> opusDealerID2;

	public List<OnlinePartnerDetails> getOnlinePartnerDetails() {
		return onlinePartnerDetails;
	}

	public List<String> getOpusDealerID1() {
		return opusDealerID1;
	}

	public void setOpusDealerID1(List<String> opusDealerID1) {
		this.opusDealerID1 = opusDealerID1;
	}

	public List<String> getOpusDealerID2() {
		return opusDealerID2;
	}

	public void setOpusDealerID2(List<String> opusDealerID2) {
		this.opusDealerID2 = opusDealerID2;
	}

	public void setOnlinePartnerDetails(List<OnlinePartnerDetails> onlinePartnerDetails) {
		this.onlinePartnerDetails = onlinePartnerDetails;
	}

	public List<String> getOpusChannels() {
		return opusChannels;
	}

	public void setOpusChannels(List<String> opusChannels) {
		this.opusChannels = opusChannels;
	}

	public List<String> getOpusSubChannels() {
		return opusSubChannels;
	}

	public void setOpusSubChannels(List<String> opusSubChannels) {
		this.opusSubChannels = opusSubChannels;
	}

	public List<OfferChoiceGroup> getOfferChoiceGroup() {
   		return offerChoiceGroup;
   	}

   	public void setOfferChoiceGroup(List<OfferChoiceGroup> offerChoiceGroup) {
   		this.offerChoiceGroup = offerChoiceGroup;
   	}
    
	public RibbonTextByKey getRibbonTextByKey() {
		return ribbonTextByKey;
	}

	public List<OfferMinMaxQuantity> getOfferMinMaxQuantity() {
		return offerMinMaxQuantity;
	}

	public void setOfferMinMaxQuantity(List<OfferMinMaxQuantity> offerMinMaxQuantity) {
		this.offerMinMaxQuantity = offerMinMaxQuantity;
	}

	public void setRibbonTextByKey(RibbonTextByKey ribbonTextByKey) {
		this.ribbonTextByKey = ribbonTextByKey;
	}

	/**
	 * @return the targetedOffer
	 */
	public Boolean getTargetedOffer() {
		return targetedOffer;
	}


	/**
	 * @param targetedOffer the targetedOffer to set
	 */
	public void setTargetedOffer(Boolean targetedOffer) {
		this.targetedOffer = targetedOffer;
	}

	/**
	 * @return the nonStackableCodes
	 */
	public String getNonStackableCodes() {
		return nonStackableCodes;
	}

	/**
	 * @param nonStackableCodes the nonStackableCodes to set
	 */
	public void setNonStackableCodes(String nonStackableCodes) {
		this.nonStackableCodes = nonStackableCodes;
	}

	public List<Integer> getDynamicUpgradeValues() {
		return dynamicUpgradeValues;
	}

	public void setDynamicUpgradeValues(List<Integer> dynamicUpgradeValues) {
		this.dynamicUpgradeValues = dynamicUpgradeValues;
	}

    public List<String> getOpusStoreIds() {
		return opusStoreIds;
	}

	public void setOpusStoreIds(List<String> opusStoreIds) {
		this.opusStoreIds = opusStoreIds;
	}

	public List<BundleProductAction> getBundleProductAction() {
		return bundleProductAction;
	}

	public void setBundleProductAction(List<BundleProductAction> bundleProductAction) {
		this.bundleProductAction = bundleProductAction;
	}


	public List<Product> getOmsFreeStbPolicy() {
		return omsFreeStbPolicy;
	}

	public void setOmsFreeStbPolicy(List<Product> omsFreeStbPolicy) {
		this.omsFreeStbPolicy = omsFreeStbPolicy;
	}
	

	public List<MetaData> getMetadata() {
		return metadata;
	}

	public void setMetadata(List<MetaData> metadata) {
		this.metadata = metadata;
	}


	/** The mobility conflicting products. */
    private List<String> mobilityConflictingProducts;

    /** The residential conflicting products. */
    private List<String> residentialConflictingProducts;

    /** The employee conflicting products. */
    private List<String> employeeConflictingProducts;
    
	
	/** The offer intents. */
	private List<String> offerIntents;

	private List<String> flowIntents;

	private List<String> serviceSubscriptionType;

    /** SVOD Only Upsell - SLS */
    private List<String> activeSubscriptionType;

	/** The migration service type. */
	private List<String> migrationServiceType;

	/** The migration service offer type. */
	private String migrationServiceOfferType;

	private String offerClassificationType;
	
	private String replacementIndicator;	
	
	/** The offerPreselectDesignation. */
	private String offerPreselectDesignation;
	
	/** The benefitCodesToSuppressOffer. */
	private List<String> benefitCodesToSuppressOffer;
	
	/** The disallowReplacementForPromotion . */
	private List<String> disallowReplacementForPromotion;
	
	/** The reconnectSubscriberType . */
	private List<String> reconnectSubscriberType;
	
	public List<String> getReconnectSubscriberType() {
		return reconnectSubscriberType;
	}

	public void setReconnectSubscriberType(List<String> reconnectSubscriberType) {
		this.reconnectSubscriberType = reconnectSubscriberType;
	}

	public List<String> getDisallowReplacementForPromotion() {
		return disallowReplacementForPromotion;
	}

	public void setDisallowReplacementForPromotion(List<String> disallowReplacementForPromotion) {
		this.disallowReplacementForPromotion = disallowReplacementForPromotion;
	}

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
	
    /** The mandatoryOfferGroups. */
    @JsonProperty("mandatoryOfferGroups")
    private List<String> mandatoryOfferGroups;
    
    private String minimumCustomerTenureInMonths;
    
    //Feature 176015 changes - 2nd Chance Premium Acquisition Offers
    private String maxCustomerEligibilityTenureInDays;
    
    public String getMaxCustomerEligibilityTenureInDays() 
    {
		return maxCustomerEligibilityTenureInDays;
	}

	public void setMaxCustomerEligibilityTenureInDays(String maxCustomerEligibilityTenureInDays) 
	{
		this.maxCustomerEligibilityTenureInDays = maxCustomerEligibilityTenureInDays;
	}
	private Boolean skipStackabilityGroupCheck;
	public Boolean isSkipStackabilityGroupCheck() {
		return skipStackabilityGroupCheck;
	}

	public void setSkipStackabilityGroupCheck(Boolean skipStackabilityGroupCheck) {
		this.skipStackabilityGroupCheck = skipStackabilityGroupCheck;
	}
	public String getMinimumCustomerTenureInMonths() 
    {
		return minimumCustomerTenureInMonths;
	}

	public void setMinimumCustomerTenureInMonths(String minimumCustomerTenureInMonths) 
	{
		this.minimumCustomerTenureInMonths = minimumCustomerTenureInMonths;
	}

	public List<String> getMandatoryOfferGroups() {
		return mandatoryOfferGroups;
	}

	public void setMandatoryOfferGroups(List<String> mandatoryOfferGroups) {
		this.mandatoryOfferGroups = mandatoryOfferGroups;
	}

	public boolean isRecommendedOfferOPUS() {
		return recommendedOfferOPUS;
	}

	public void setRecommendedOfferOPUS(boolean recommendedOfferOPUS) {
		this.recommendedOfferOPUS = recommendedOfferOPUS;
	}

	/**
     * The qualifyingProductCheckNOTRequired.
     */
    private Boolean qualifyingProductCheckNOTRequired;
    
    public Boolean isQualifyingProductCheckNOTRequired() {
		return qualifyingProductCheckNOTRequired;
	}

	public void setQualifyingProductCheckNOTRequired(Boolean qualifyingProductCheckNOTRequired) {
		this.qualifyingProductCheckNOTRequired = qualifyingProductCheckNOTRequired;
	}

	/**
     * The recommendedOfferOPUS.
     */
    private boolean recommendedOfferOPUS;
	
	private List<String> productsOnAccountToSuppressOffer;
	
	private String similarOfferID;
	
	private List<String> retentionOfferSubType;

    private String recommendedEDSP;
    
    private List<String> customerTypes;
	
	@JsonProperty("isSpecialOffer")
    private boolean specialOffer;
	
	private List<IncludedProduct> includedOffers;
	
	public List<IncludedProduct> getIncludedOffers() {
		return includedOffers;
	}

	public void setIncludedOffers(List<IncludedProduct> includedOffers) {
		this.includedOffers = includedOffers;
	}

	public boolean isSpecialOffer() {
		return specialOffer;
	}

	public void setSpecialOffer(boolean specialOffer) {
		this.specialOffer = specialOffer;
	}
	
	private List<String> excludedPartners;
	
	private List<String> ineligiblePartners;
	
	private List<Product>  qualifyingProductIds;
	
	private List<String> qualifyingProductsBillingProductCode;
	
	private List<Product>  bundleProductIds;
    
    private String videoOfferCategory;
    
    private String stackabilityGroup;
    
    private String chargeType;
    
    private Integer minSelected;
    
    private Integer maxSelected;
    
    private String associatedFee;
    
    private String allowedAction;
    
    private Integer receiverOrderLimit;
    
    private Integer receiverAccountLimit;
    
    private String billingSystem;
    
    private String installmentBiller;
    
    private String enablerATGBundleOfferId;
    
    private String enablerATGProductId;
    
	private String enablerATGSkuId;
    
	private Boolean isSelected;
	
	private Integer qtySelected;
	
	private Boolean displayBasePrice;
	
	private List<String> ineligibleStoreIds;
    
    @JsonProperty("eligibilityCreditRisk")
    private List<String> creditRisk;
    
	public List<String> getCreditRisk() {
		return creditRisk;
	}

	public void setCreditRisk(List<String> creditRisk) {
		this.creditRisk = creditRisk;
	}
	
	 public List<String> getIneligibleStoreIds() {
			return ineligibleStoreIds;
		}
	
    public Boolean getDisplayBasePrice() {
		return displayBasePrice;
	}

	public void setDisplayBasePrice(Boolean displayBasePrice) {
		this.displayBasePrice = displayBasePrice;
	}

	/**
	 * @return the qtySelected
	 */
	public Integer getQtySelected() {
		return qtySelected;
	}

	/**
	 * @param qtySelected the qtySelected to set
	 */
	public void setQtySelected(Integer qtySelected) {
		this.qtySelected = qtySelected;
	}

	/**
	 * @return the isSelected
	 */
	public Boolean getIsSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected the isSelected to set
	 */
	public void setIsSelected(Boolean isSelected) {
		this.isSelected = isSelected;
	}

    public List<Product> getBundleProductIds() {
		return bundleProductIds;
	}

	public void setBundleProductIds(List<Product> bundleProductIds) {
		this.bundleProductIds = bundleProductIds;
	}

	public List<String> getQualifyingProductsBillingProductCode() {
		return qualifyingProductsBillingProductCode;
	}

	public void setQualifyingProductsBillingProductCode(List<String> qualifyingProductsBillingProductCode) {
		this.qualifyingProductsBillingProductCode = qualifyingProductsBillingProductCode;
	}

	public List<Product> getQualifyingProductIds() {
		return qualifyingProductIds;
	}

	public void setQualifyingProductIds(List<Product> qualifyingProductIds) {
		this.qualifyingProductIds = qualifyingProductIds;
	}

	public String getEnablerATGProductId() {
		return enablerATGProductId;
	}

	public void setEnablerATGProductId(String enablerATGProductId) {
		this.enablerATGProductId = enablerATGProductId;
	}

	public String getEnablerATGSkuId() {
		return enablerATGSkuId;
	}

	public void setEnablerATGSkuId(String enablerATGSkuId) {
		this.enablerATGSkuId = enablerATGSkuId;
	}
        
	public String getEnablerATGBundleOfferId() {
		return enablerATGBundleOfferId;
	}

	public void setEnablerATGBundleOfferId(String enablerATGBundleOfferId) {
		this.enablerATGBundleOfferId = enablerATGBundleOfferId;
	}

	public String getBillingSystem() {
		return billingSystem;
	}

	public void setBillingSystem(String billingSystem) {
		this.billingSystem = billingSystem;
	}
    
	public Integer getReceiverOrderLimit() {
		return receiverOrderLimit;
	}

	public void setReceiverOrderLimit(Integer receiverOrderLimit) {
		this.receiverOrderLimit = receiverOrderLimit;
	}

	public Integer getReceiverAccountLimit() {
		return receiverAccountLimit;
	}

	public void setReceiverAccountLimit(Integer receiverAccountLimit) {
		this.receiverAccountLimit = receiverAccountLimit;
	}

	public String getAllowedAction() {
		return allowedAction;
	}

	public void setAllowedAction(String allowedAction) {
		this.allowedAction = allowedAction;
	}
    

	/**
	 * @return the chargeType
	 */
	public String getChargeType() {
		return chargeType;
	}

	/**
	 * @param chargeType the chargeType to set
	 */
	public void setChargeType(String chargeType) {
		this.chargeType = chargeType;
	}

	/**
	 * @return the minSelected
	 */
	public Integer getMinSelected() {
		return minSelected;
	}

	/**
	 * @param minSelected the minSelected to set
	 */
	public void setMinSelected(Integer minSelected) {
		this.minSelected = minSelected;
	}

	/**
	 * @return the maxSelected
	 */
	public Integer getMaxSelected() {
		return maxSelected;
	}

	/**
	 * @param maxSelected the maxSelected to set
	 */
	public void setMaxSelected(Integer maxSelected) {
		this.maxSelected = maxSelected;
	}

	/**
	 * @return the associatedFee
	 */
	public String getAssociatedFee() {
		return associatedFee;
	}

	/**
	 * @param associatedFee the associatedFee to set
	 */
	public void setAssociatedFee(String associatedFee) {
		this.associatedFee = associatedFee;
	}

	/**
	 * @return the videoOfferCategories
	 */
	public String getVideoOfferCategory() {
		return videoOfferCategory;
	}

	/**
	 * @param videoOfferCategory
	 */
	public void setVideoOfferCategory(String videoOfferCategory) {
		this.videoOfferCategory = videoOfferCategory;
	}

	/**
	 * @return the stackabilityGroup
	 */
	public String getStackabilityGroup() {
		return stackabilityGroup;
	}
	
	/**
	 * @param stackabilityGroup the stackabilityGroup to set
	 */
	public void setStackabilityGroup(String stackabilityGroup) {
		this.stackabilityGroup = stackabilityGroup;
	} 

	/**
	 * @return the customerType
	 */
	public List<String> getCustomerTypes() {
		return customerTypes;
	}

	/**
	 * @param customerTypes the customerType to set
	 */
	public void setCustomerTypes(List<String> customerTypes) {
		this.customerTypes = customerTypes;
		setCustomerType();
	}

	public String getRecommendedEDSP() {
		return recommendedEDSP;
	}

	public void setRecommendedEDSP(String recommendedEDSP) {
		this.recommendedEDSP = recommendedEDSP;
	}
	
	private List<GenericTypeIdBase> swimlaneSwitchEligiblePack;

    /**
	 * @return the swimlaneSwitchEligiblePack
	 */
	public List<GenericTypeIdBase> getSwimlaneSwitchEligiblePack() {
		return swimlaneSwitchEligiblePack;
	}

	/**
	 * @param swimlaneSwitchEligiblePack the swimlaneSwitchEligiblePack to set
	 */
	public void setSwimlaneSwitchEligiblePack(List<GenericTypeIdBase> swimlaneSwitchEligiblePack) {
		this.swimlaneSwitchEligiblePack = swimlaneSwitchEligiblePack;
	}
	
	private List<GenericTypeIdBase> makeInEligibleIfSKUsPresent;
	
	public List<GenericTypeIdBase> getMakeInEligibleIfSKUsPresent() {
		return makeInEligibleIfSKUsPresent;
	}

	public void setMakeInEligibleIfSKUsPresent(List<GenericTypeIdBase> makeInEligibleIfSKUsPresent) {
		this.makeInEligibleIfSKUsPresent = makeInEligibleIfSKUsPresent;
	}
	
	private boolean recommendedOnline;

	/**
	 * @return the recommendedOnline
	 */
	public boolean isRecommendedOnline() {
		return recommendedOnline;
	}

	/**
	 * @param recommendedOnline the recommendedOnline to set
	 */
	public void setRecommendedOnline(boolean recommendedOnline) {
		this.recommendedOnline = recommendedOnline;
	}
	
	private List<GenericTypeIdBase> reconnectEligibleOffer;
	
	/**
	 * @return the reconnectEligibleOffer
	 */
	public List<GenericTypeIdBase> getReconnectEligibleOffer() {
		return reconnectEligibleOffer;
	}

	/**
	 * @param reconnectEligibleOffer the reconnectEligibleOffer to set
	 */
	public void setReconnectEligibleOffer(List<GenericTypeIdBase> reconnectEligibleOffer) {
		this.reconnectEligibleOffer = reconnectEligibleOffer;
	}

	public List<String> getRetentionOfferSubType() {
		return retentionOfferSubType;
	}

	public void setRetentionOfferSubType(List<String> retentionOfferSubType) {
		this.retentionOfferSubType = retentionOfferSubType;
	}
	
	public String getSimilarOfferID() {
		return similarOfferID;
	}

	public void setSimilarOfferID(String similarOfferID) {
		this.similarOfferID = similarOfferID;
	}

	/**
	 * @return the productsOnAccountToSuppressOffer
	 */
	public List<String> getProductsOnAccountToSuppressOffer() {
		return productsOnAccountToSuppressOffer;
	}

	/**
	 * @param productsOnAccountToSuppressOffer the productsOnAccountToSuppressOffer to set
	 */
	public void setProductsOnAccountToSuppressOffer(List<String> productsOnAccountToSuppressOffer) {
		this.productsOnAccountToSuppressOffer = productsOnAccountToSuppressOffer;
	}

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

	public String getReplacementIndicator() {
		return replacementIndicator;
	}

	public void setReplacementIndicator(String replacementIndicator) {
		this.replacementIndicator = replacementIndicator;
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

    public String getOmsOfferID() {
		return omsOfferID;
	}

	public void setOmsOfferID(String omsOfferID) {
		this.omsOfferID = omsOfferID;
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
    
      public GenericLocaleBase getDisclaimer() { return disclaimer; }
     
     /*
     * @param disclaimer the disclaimer to set
     */
     public void setDisclaimer(GenericLocaleBase disclaimer) { 
    	 this.disclaimer = disclaimer;
     }
     
    /**
     * @return the termsAndConditions
     */
    public GenericLocaleBase getTermsAndConditions() {
        return termsAndConditions;
    }

    /**
     * Sets the terms and conditions.
     *
     * @param termsAndConditions the termsAndConditions to set
     */
    public void setTermsAndConditions(GenericLocaleBase termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
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
        for (Benefit benefit : benefits)
        {
        	benefit.setDisclosureMessagesByKey();
        }
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
     * Gets the allow Conflicting Offers
     *
     * @return the allowConflictingOffers
     */
    public List<GenericTypeIdBase> getAllowConflictingOffers() {
        return allowConflictingOffers;
    }

    /**
     * Sets the allowConflictingOffers.
     *
     * @param allowConflictingOffers the allowConflictingOffers to set
     */
    public void setAllowConflictingOffers(List<GenericTypeIdBase> allowConflictingOffers) {
        this.allowConflictingOffers = allowConflictingOffers;
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
	 * @return the contractPrice
	 */
	public OfferPrice getContractPrice() {
		return contractPrice;
	}

	/**
	 * @param contractPrice the contractPrice to set
	 */
	public void setContractPrice(OfferPrice contractPrice) {
		this.contractPrice = contractPrice;
	}

	/**
	 * @return the specialPromoPrice
	 */
	public OfferPrice getSpecialPromoPrice() {
		return specialPromoPrice;
	}

	/**
	 * @param specialPromoPrice the specialPromoPrice to set
	 */
	public void setSpecialPromoPrice(OfferPrice specialPromoPrice) {
		this.specialPromoPrice = specialPromoPrice;
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

	public Integer getMaxAddonCount() {
		return maxAddonCount;
	}

	public void setMaxAddonCount(Integer maxAddonCount) {
		this.maxAddonCount = maxAddonCount;
	}

	public Integer getMinAddonCount() {
		return minAddonCount;
	}

	public void setMinAddonCount(Integer minAddonCount) {
		this.minAddonCount = minAddonCount;
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

    public List<String> getStackableOfferCodes() {
        return stackableOfferCodes;
    }

    public void setStackableOfferCodes(List<String> stackableOfferCodes) {
        this.stackableOfferCodes = stackableOfferCodes;
    }

	public String getInstallmentBiller() {
		return installmentBiller;
	}

	public void setInstallmentBiller(String installmentBiller) {
		this.installmentBiller = installmentBiller;
	}
	
	public String getRtpRtcIndicator() {
		return rtpRtcIndicator;
	}

	public void setRtpRtcIndicator(String rtpRtcIndicator) {
		this.rtpRtcIndicator = rtpRtcIndicator;
	}
	
	public void setIneligibleStoreIds(List<String> ineligibleStoreIds) {
		this.ineligibleStoreIds = ineligibleStoreIds;
	}

	private String banLookupTable;

	public String getBanLookupTable() {	return banLookupTable; }

	public void setBanLookupTable(String banLookupTable) { this.banLookupTable = banLookupTable; }

	private String specialPage;

	public String getSpecialPage() { return specialPage; }

	public void setSpecialPage(String specialPage) { this.specialPage = specialPage; }

	public List<String> getAgentId() {
		return agentId;
	}

	public void setAgentId(List<String> agentId) {
		this.agentId = agentId;
	}

	public List<String> getAgentRole() {
		return agentRole;
	}

	public void setAgentRole(List<String> agentRole) {
		this.agentRole = agentRole;
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
		builder.append(", omsOfferID=");
		builder.append(omsOfferID);
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
		builder.append(", disclaimer=");
		builder.append(disclaimer);
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
		builder.append(", stackableOfferCodes=");
		builder.append(stackableOfferCodes);
		builder.append(", stackableOffersCategories=");
		builder.append(stackableOffersCategories);
		builder.append(", conflictingOffers=");
		builder.append(conflictingOffers);
        builder.append(", allowConflictingOffers=");
        builder.append(allowConflictingOffers);
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
		builder.append(", contractPrice=");
		builder.append(contractPrice);
		builder.append(", specialPromoPrice=");
		builder.append(specialPromoPrice);
		builder.append(", discountAmount=");
		builder.append(discountAmount);
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
		builder.append(", rtpRtcIndicator=");
		builder.append(rtpRtcIndicator);
		builder.append(", mobilityConflictingProducts=");
		builder.append(mobilityConflictingProducts);
		builder.append(", residentialConflictingProducts=");
		builder.append(residentialConflictingProducts);
		builder.append(", employeeConflictingProducts=");
		builder.append(employeeConflictingProducts);
		builder.append(", offerIntents=");
		builder.append(offerIntents);
		builder.append(", migrationServiceType=");
		builder.append(migrationServiceType);
		builder.append(", migrationServiceOfferType=");
		builder.append(migrationServiceOfferType);
		builder.append(", offerClassificationType=");
		builder.append(offerClassificationType);
		builder.append(", replacementIndicator=");
		builder.append(replacementIndicator);
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
		builder.append(", useForPriceCalculation=");
		builder.append(useForPriceCalculation);
		builder.append(", productsOnAccountToSuppressOffer=");
		builder.append(productsOnAccountToSuppressOffer);
		builder.append(", similarOfferID=");
		builder.append(similarOfferID);
		builder.append(", retentionOfferSubType=");
		builder.append(retentionOfferSubType);
		builder.append(", rsnFeeDetails=");
		builder.append(rsnFeeDetails);
		builder.append(", eligibilityFan=");
		builder.append(eligibilityFan);
		builder.append(", recommendedEDSP=");
		builder.append(recommendedEDSP);
		builder.append(", swimlaneSwitchEligiblePack=");
		builder.append(swimlaneSwitchEligiblePack);
		builder.append(", recommendedOnline=");
		builder.append(recommendedOnline);
		builder.append(", customerType=");
		builder.append(customerTypes);
		builder.append(", videoOfferCategory=");
		builder.append(videoOfferCategory);
		builder.append(", stackabilityGroup=");
		builder.append(stackabilityGroup);
		builder.append(", omsFreeStbPolicy=");
		builder.append(omsFreeStbPolicy);
		builder.append(", chargeType=");
		builder.append(chargeType);
		builder.append(", minSelected=");
		builder.append(minSelected);
		builder.append(", maxSelected=");
		builder.append(maxSelected);
		builder.append(", associatedFee=");
		builder.append(associatedFee);
		builder.append(", allowedAction=");
		builder.append(allowedAction);
		builder.append(", receiverOrderLimit=");
		builder.append(receiverOrderLimit);
		builder.append(", receiverAccountLimit=");
		builder.append(receiverAccountLimit);
		builder.append(", excludedPartners=");
		builder.append(excludedPartners);
		builder.append(", ineligiblePartners=");
		builder.append(ineligiblePartners);
		builder.append(", billingSystem=");
		builder.append(billingSystem);
		builder.append(", enablerATGBundleOfferId=");
		builder.append(enablerATGBundleOfferId);
		builder.append(", enablerATGSkuId=");
		builder.append(enablerATGSkuId);
		builder.append(", enablerATGProductId=");
		builder.append(enablerATGProductId);
		builder.append(", dynamicUpgradeValues=");
		builder.append(dynamicUpgradeValues);
		builder.append(", nonStackableCodes=");
		builder.append(nonStackableCodes);
		builder.append(", targetedOffer=");
		builder.append(targetedOffer);
		builder.append(", associatedOffers=");
		builder.append(associatedOffers);
		builder.append(", requiredProducts=");
		builder.append(requiredProducts);
		builder.append(", c3OfferId=");
		builder.append(c3OfferId);
		builder.append(", enablerSalesOfferId=");
		builder.append(enablerSalesOfferId);
		builder.append(", adeEligible=");
		builder.append(adeEligible);
		builder.append(", isSelected=");
		builder.append(isSelected);
		builder.append(", qtySelected=");
		builder.append(qtySelected);
		builder.append(", disallowReplacementForPromotion=");
		builder.append(disallowReplacementForPromotion);
		builder.append(", treatmentCode=");
		builder.append(treatmentCode);
		builder.append(", displayBasePrice=");
		builder.append(displayBasePrice);
		builder.append(", partnerDealerCode1=");
		builder.append(partnerDealerCode1);
		builder.append(", partnerDealerCode2=");
		builder.append(partnerDealerCode2);
		builder.append(", ineligibleStoreIds=");
		builder.append(ineligibleStoreIds);
		builder.append(", marketingSrcCode=");
		builder.append(marketingSrcCode);
		builder.append(", supportedCohorts=");
		builder.append(supportedCohorts);
		builder.append(", banLookupTable=");
		builder.append(banLookupTable);
		builder.append(", specialPage=");
		builder.append(specialPage);
		builder.append(", offerPricewithLocals=");
		builder.append(offerPricewithLocals);
		builder.append(", planPricewithLocals=");
		builder.append(planPricewithLocals);
		builder.append("]");
		return builder.toString();
	}

	public List<String> getExcludedPartners() {
		return excludedPartners;
	}

	public void setExcludedPartners(List<String> excludedPartners) {
		this.excludedPartners = excludedPartners;
	}

	public List<String> getIneligiblePartners() {
		return ineligiblePartners;
	}

	public void setIneligiblePartners(List<String> ineligiblePartners) {
		this.ineligiblePartners = ineligiblePartners;
	}

	public List<String> getPartnerDealerCode2() {
		return partnerDealerCode2;
	}

	public void setPartnerDealerCode2(List<String> partnerDealerCode2) {
		this.partnerDealerCode2 = partnerDealerCode2;
	}

	public List<String> getPartnerDealerCode1() {
		return partnerDealerCode1;
	}

	public void setPartnerDealerCode1(List<String> partnerDealerCode1) {
		this.partnerDealerCode1 = partnerDealerCode1;
	}

	public JsonNode getBenefitCodesToSuppressTheOffer() {
		return benefitCodesToSuppressTheOffer;
	}

	public void setBenefitCodesToSuppressTheOffer(JsonNode benefitCodesToSuppressTheOffer) {
		this.benefitCodesToSuppressTheOffer = benefitCodesToSuppressTheOffer;
	}
  
    
	public void setBenefitCodes(List<String> benefitCodes) {
		this.benefitCodes = benefitCodes;
	}

	public List<String> getIneligibleAccountStatus() {
		return ineligibleAccountStatus;
	}

	public void setIneligibleAccountStatus(List<String> ineligibleAccountStatus) {
		this.ineligibleAccountStatus = ineligibleAccountStatus;
	}

	public void setCoolOffPeriod(Integer coolOffPeriod) {
		this.coolOffPeriod = coolOffPeriod;
	}

	public List<String> getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	

	public List<String> getQualifyingProductsPlanSubTypes() {
		return qualifyingProductsPlanSubTypes;
	}

	public void setQualifyingProductsPlanSubTypes(List<String> qualifyingProductsPlanSubTypes) {
		this.qualifyingProductsPlanSubTypes = qualifyingProductsPlanSubTypes;
	}

	public Integer getCoolOffPeriod() {
		if(null != benefitCodesToSuppressTheOffer) {
		for(JsonNode x : benefitCodesToSuppressTheOffer) {
			for(JsonNode y : x) {	
				String name = null != y.get("name")?y.get("name").asText():"";
				if(name.equalsIgnoreCase("coolOffPeriod")) {
					Integer value = y.get("value").intValue();
					coolOffPeriod = value;
				}
			}			
		}
		}
		return coolOffPeriod;
	}
	
	public List<String> getBenefitCodes() {
		if(null != benefitCodesToSuppressTheOffer) {
		for(JsonNode x : benefitCodesToSuppressTheOffer) {
			for(JsonNode y : x) {
				String name = null != y.get("name")?y.get("name").asText():"";
				if(name.equalsIgnoreCase("benefitCodes")) {
					List<String> values = MAPPER.convertValue(y.get("value"), List.class);
					benefitCodes = values;
				}
			}			
		}
		}
		return benefitCodes;
	}

	public List<String> getDirectIntegrationPartnerName() {
		return directIntegrationPartnerName;
	}

	public void setDirectIntegrationPartnerName(List<String> directIntegrationPartnerName) {
		this.directIntegrationPartnerName = directIntegrationPartnerName;
	}

	public List<String> getSupportedCohorts() {
		return supportedCohorts;
	}

	public void setSupportedCohorts(List<String> supportedCohorts) {
		this.supportedCohorts = supportedCohorts;
	}

	private List<String> supportedCohorts;

	private String customerType;

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public void setCustomerType() {
        if(customerTypes != null && !customerTypes.isEmpty()){
            this.customerType = customerTypes.get(0);
        }
	}
	@JsonProperty("descriptionByKey")
	private List<OfferLevel3DAttribute> descriptionByKey;


	@JsonProperty("disclosureMessagesByKey")
	private List<OfferLevel3DAttribute> disclosureMessagesByKey;

	@JsonProperty("displayNameByKey")
	private List<OfferLevel3DAttribute> displayNameByKey;

	private List<OfferLevel3DAttribute> ribbonTextsByKey;

	public List<OfferLevel3DAttribute> getRibbonTextsByKey() {
		return ribbonTextsByKey;
	}

	public void setRibbonTextsByKey(List<OfferLevel3DAttribute> ribbonTextsByKey) {
		this.ribbonTextsByKey = ribbonTextsByKey;
	}
	public List<OfferLevel3DAttribute> getDescriptionByKey() {
		return descriptionByKey;
	}

	public void setDescriptionByKey(List<OfferLevel3DAttribute> descriptionByKey) {
		this.descriptionByKey = descriptionByKey;
	}

	public List<OfferLevel3DAttribute> getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(List<OfferLevel3DAttribute> disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public List<OfferLevel3DAttribute> getDisplayNameByKey() {
		return displayNameByKey;
	}

	public void setDisplayNameByKey(List<OfferLevel3DAttribute> displayNameByKey) {
		this.displayNameByKey = displayNameByKey;
	}
	
	private OfferPrice offerPricewithLocals;
	
	 
	public OfferPrice getOfferPricewithLocals() {
		return offerPricewithLocals;
	}
 
	public void setOfferPricewithLocals(OfferPrice offerPricewithLocals) {
		this.offerPricewithLocals = offerPricewithLocals;
	}
	
	private OfferPrice planPricewithLocals;
 
	public OfferPrice getPlanPricewithLocals() {
		return planPricewithLocals;
	}
 
	public void setPlanPricewithLocals(OfferPrice planPricewithLocals) {
		this.planPricewithLocals = planPricewithLocals;
	}

	private List<String> eligibleIAPPartners;

	public List<String> getEligibleIAPPartners() {
		return eligibleIAPPartners;
	}

	public void setEligibleIAPPartners(List<String> eligibleIAPPartners) {
		this.eligibleIAPPartners = eligibleIAPPartners;
	}


	public List<String> getServiceSubscriptionType() {
		return serviceSubscriptionType;
	}

	public void setServiceSubscriptionType(List<String> serviceSubscriptionType) {
		this.serviceSubscriptionType = serviceSubscriptionType;
	}

	public List<String> getFlowIntents() {
		return flowIntents;
	}

	public void setFlowIntents(List<String> flowIntents) {
		this.flowIntents = flowIntents;
	}

    /** SVOD Only Upsell - SLS */
    public List<String> getActiveSubscriptionType() {
        return activeSubscriptionType;
    }

    public void setActiveSubscriptionType(List<String> activeSubscriptionType) {
        this.activeSubscriptionType = activeSubscriptionType;
    }

	private Boolean skipProductsOnAccountToSuppress;

	public Boolean getSkipProductsOnAccountToSuppress() {
		return skipProductsOnAccountToSuppress;
	}

	public void setSkipProductsOnAccountToSuppress(Boolean skipProductsOnAccountToSuppress) {
		this.skipProductsOnAccountToSuppress = skipProductsOnAccountToSuppress;
	}

	public List<List<MessageEntry>> getGlobalMessagesByKey() {
		return globalMessagesByKey;
	}
	public void setGlobalMessagesByKey(List<List<MessageEntry>> globalMessagesByKey) {
		this.globalMessagesByKey = globalMessagesByKey;
	}
	public Boolean isDelayProvisioning() {
		return delayProvisioning;
	}
	public void setDelayProvisioning(Boolean delayProvisioning) {
		this.delayProvisioning = delayProvisioning;
	}
	public List<String> getDelayProvisioningReasons() {
		return delayProvisioningReasons;
	}
	public void setDelayProvisioningReasons(List<String> delayProvisioningReasons) {
		this.delayProvisioningReasons = delayProvisioningReasons;
	}

}
