package com.dtv.dcp.epoch.model.ct.product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dtv.dcp.epoch.model.common.DisplayTypeByKey;
import com.dtv.dcp.epoch.model.common.EligibilityContext;
import com.dtv.dcp.epoch.model.common.MinMaxQuantity;
import com.dtv.dcp.epoch.model.common.SubCategoryByKey;
import com.dtv.dcp.epoch.model.common.UIAttributes;
import com.dtv.dcp.epoch.model.ct.generic.GenericByKey;
import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericNameValueBase;
import com.dtv.dcp.epoch.model.ct.generic.GenericTypeIdBase;
import com.dtv.dcp.epoch.model.ct.offer.ChannelLineupAlertMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.DelayProvisioningMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.DisclosureMessagesByKey;
import com.dtv.dcp.epoch.model.ct.offer.MessageEntry;
import com.dtv.dcp.epoch.model.ct.offer.RibbonTextsByKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Class Attributes.
 */
/**
 * @author vc402t
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Attributes implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The displayName. */
	private GenericLocaleBase displayName;
	
	private Constraints constraints;
	private String key;
	private String name;

	public Constraints getConstraints() {
		return constraints;
	}

	public void setConstraints(Constraints constraints) {
		this.constraints = constraints;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/** The displayRanking. */
	private Integer displayRanking;

	public Integer getDisplayRanking() {
		return displayRanking;
	}

	public void setDisplayRanking(Integer displayRanking) {
		this.displayRanking = displayRanking;
	}
	
	/** The isHDAccessRequired. */
	private Boolean isHDAccessRequired;


	public Boolean getIsHDAccessRequired() {
		return isHDAccessRequired;
	}

	public void setIsHDAccessRequired(Boolean isHDAccessRequired) {
		this.isHDAccessRequired = isHDAccessRequired;
	}
	
    private List<List<MessageEntry>> globalMessagesByKey;
    
    private List<String> delayProvisioningReasons;
    private Boolean delayProvisioning;
    
    private String delayProvisioningReason;

	/** The displayNamesByKey. */
	private GenericByKey displayNamesByKey;

	/** The descriptionsByKey. */
	private ProductDescriptionsByKey descriptionsByKey;

	private RibbonTextsByKey ribbonTextsByKey;
	private DelayProvisioningMessagesByKey delayProvisioningMessagesByKey;
	public RibbonTextsByKey getRibbonTextsByKey() {
		return ribbonTextsByKey;
	}

	public void setRibbonTextsByKey(RibbonTextsByKey ribbonTextsByKey) {
		this.ribbonTextsByKey = ribbonTextsByKey;
	}

	/** The billingProductCode. */
	private String billingProductCode;

	/** The billingCode. */
	private String billingCode;

	/** The billingProductId. */
	private String billingProductId;

	/** The startDate. */
	private String startDate;

	/** The endDate. */
	private String endDate;

	/** The businessSegment. */
	private List<String> businessSegment;

	/** The endDate. */
	private String provisioningCode;

	/** The congratsMessage. */
	private String congratsMessage;

	/** The opusDisclosureMessage. */
	private String opusDisclosureMessage;

	/** The myAttDisclosureMessage. */
	private String myAttDisclosureMessage;

	/** The salesChannel. */
	private List<String> salesChannel;

	/** The ranking. */
	private int ranking;

	/** The attProject. */
	private String attProject;

	/** The productFamily. */
	private String productFamily;

	/** The packageGroup. */
	private String packageGroup;

	/** The fufillmentAttribute1. */
	private String fufillmentAttribute1;

	/** The fufillmentAttribute2. */
	private String fufillmentAttribute2;

	/** The removalRule. */
	private RemovalRule removalRule;

	/** The serviceType. */
	private String serviceType;

	/** The preReqProducts. */
	private List<String> preReqProducts;

	/** The upgradablePlans. */
	private List<GenericTypeIdBase> upgradablePlans;

	/** The upgradablePlans. */
	private List<GenericTypeIdBase> downgradablePlans;

	/** The isRSNCapable. */
	private Boolean isRSNCapable;

	/** The marketingDescLink. */
	private String marketingDescLink;

	/** The numberOfChannels. */
	private String numberOfChannels;

	/** The englishChannelList. */
	private List<String> englishChannelList;

	/** The spanishChannelList. */
	private List<String> spanishChannelList;

	/** The featuredChannels. */
	private List<String> featuredChannels;

	/** The addOnType. */
	private String addOnType;

	/** The planSubType. */
	private String planSubType;

	/** The capacityLimit. */
	private int capacityLimit;

	/** The dataRententionPeriod. */
	private String dataRententionPeriod;

	/** The videoBasePackageRequired. */
	private Boolean videoBasePackageRequired;

	/** The skipAdsIndicator. */
	private Boolean skipAdsIndicator;

	/** The displayType. */
	private String displayType;

	@JsonProperty("isMDUBridePackage")
    private boolean mDUBridePackage;

	private int addonCount;

	private int freeDeviceCount;
	/** The language. */
	private String language;

	/** The serviceFlag. */
	private Boolean serviceFlag;

	/** The concurrentStreamCount. */
	private int concurrentStreamCount;

	/** The benefitReferences. */
	private List<GenericTypeIdBase> benefitReferences;

	/** The preOrder. */
	private Boolean preOrder;

	/** The thirdPartyOTT. */
	private Boolean thirdPartyOTT;

	/** The secondaryBillingProductId. */
	private String secondaryBillingProductId;
    /** The vcDependentPromoCode. */
    private String vcDependentPromoCode;
	/** The dependentPromoCode. */
	private String dependentPromoCode;

	/** The dependentBillerPromoId. */
	private String dependentBillerPromoId;

	/** The dependentPromoStartDate. */
	private String dependentPromoStartDate;

	/** The dependentPromoName . */
	private String dependentPromoName;

	/** The dependentPromoDescription. */
	private String dependentPromoDescription;

	/** The dependentPromoDisplayName. */
	private String dependentPromoDisplayName;

	/** The dependentPromoIsFreeTrail . */
	private Boolean dependentPromoIsFreeTrail;

	/** The productStatus. */
	private String productStatus;

	/** The discountSku. */
	private String discountSku;

	/** The numberOfSpanishChannels. */
	private String numberOfSpanishChannels;

	/** The equipmentSku. */
	private String equipmentSku;

	/** The compatibleProducts. */
	private List<ProductWrapper> compatibleProducts;

	private String accessoryCategory;

	private List<VideoDeviceLimit> videoDeviceLimit;

	private List<VideoDevicePolicy> videoDevicePolicy;

	private String downgradeableRule;

	private List<Rule> restrictionRule;

	private List<Rule> callToOrderRule;

	private List<Rule> upgradeableRule;

	private List<CompatibleVideoDeviceModel> compatibleVideoDeviceModels;

	private Installment installment;

	/** The compatibleMobilityProducts. */
	private List<ProductWrapper> compatibleMobilityProducts;

	private List<IncludeProductWrapper> includedProducts;

    private List<IncludeProductWrapper> equivalentContentProducts;

	private List<IncludeProductWrapper> includedMobilityProducts;

	/** The conflictPromoList. */
	private List<String> conflictPromoList;

	/** The contractApplicable. */
	private String contractApplicable;

	/** The contractIndicator. */
	private List<String> contractIndicator;

	/** The maxQuantity. */
	private String maxQuantity;

	/** The minQuantity */
	private String minQuantity;

	/** The isContracted. */
	private List<MinMaxQuantity> minMaxQuantity;

	/** The isContracted. */
	private List<InstallmentInfo> installmentList;

	/** The subCategory. */
	private String subCategory;

	/** The customer segments. */
	private List<String> customerSegments;

	/** The is third party. */
	private Boolean isThirdParty;

	//private Boolean isInternationalVideoSubscriptionRequired;
	private Boolean isInternationalAddonRequired;

	private Boolean hasLocalChannels;

	private Boolean isPickService;

	private Boolean isRequiredAddonForInternationalVideoPlan;

	private Boolean isAutoRenewable;

	private String selfInstallAvailability;

	private Boolean isAsurionPlan;

	private GenericLocaleBase termsAndConditions;

	private List<Object> specifications;

	private String feeType;

	private List<String> deliverymethod;

	private String accessoryType;

	private String deviceType;

	private String deviceCategory;

	private String addonType;

	private String planType;

	private boolean isWirelessCapable;

	private String purchaseType;

	private int maxNumberOfInstallments;
	private int maxNumberOfPreSeasonInstallments;
	private int maxNumberOfFullSeasonInstallments;
	private int maxNumberOfMidSeasonInstallments;
	private int maxNumberOfPostSeasonInstallments;
	private String seasonStartDate;
	private String seasonEndDate;
	private String valueRanking;
	private String complianceRanking;
	private List<String> channelLineup;
	private int maxOrderLimit;
	private int maxAccountLimit;
	private GenericLocaleBase modelNumbers;
	private String deliveryMethod;

	private String productGroup;
	private String isFulfillmentOnlyProduct;

	// Campaign Attributes

	private String campaignId;
	private String campaignCode;
	private String campaignName;
	private String campaignDesc;
	private String couponType;
	private String campaignCategory;
	private String creationDate;
	private String campaignStartDate;
	private String campaignEndDate;
	private String autoRegister;
	private Boolean studioSponsorByATT;
	private String lockDuration;
	private Integer numberOfCoupons;
	private String campaignMessaging;
	// private List<PurchaseContext> campaignEligibility;
	private List<EligibilityContext> campaignEligibility;

	// wireless attributes
	private String deviceSubType;
	private String deviceSubCategory;
	private String manufacturer;
	private String model;
	private String imeiType;
	private String sku;
	private String atgCommitmentTermId;
	private String termCode;
	private String commitmentTermType;
	private List<GenericNameValueBase> flags;
	private String redemptionLimit;

	private DeliveryMethod deliveryMethodByChannel;

	private List<DeliveryMethodByChannel> deliveryMethodByChannels;
	private ActionRule removalRuleByChannel;
	/**
	 * The conflictingProducts.
	 */
	private List<GenericTypeIdBase> conflictingProducts;

	/** The compatibleEmployeeProducts. */
	private List<ProductWrapper> compatibleEmployeeProducts;

	private String atgProductID;

	private Integer maxNoOfLines;

	private String skuStatus;

	private String saleStatus;

	private String tradeInEligibiltyReason;

	private String hylaCode;

	private List<String> deviceEdModels;

	private Boolean migratedProduct;
	
	private Boolean isChangeable;
	
	private String omsProductCode;
	
	private String orderingMethod;
	
	private String enablerPricePlanCode;
	
	private String tokens;
	
	private String chargeCode;
	
	private Boolean isWorldDirect;
	
	private Integer maxSTBsWired;
	
	private Integer maxDVRs;
	
	private Integer maxSTBsWIFI;
	
	private Integer maxSTBsWithServer;
	
	private Boolean isETFRequired;

	private Boolean isRemovable;
	
	private Integer heroOfferRanking;
	
	private List<GenericNameValueBase> billingParams;
	
	private List<GrandFatherInfo> grandFatherInfo;

	private List<String> benefitsOnCustomersAccount;
	
	private String remoteReplacementType;
	
	private List<ProductKey> productKey;

	private String orderValidFromDate;
	
	private String orderValidToDate;
	
	private String receiverLineType;
	
    private String pick5Mask;
    
    private String make;
    private String deviceName;
    private String catalogProductName;
    
    private Double dollarAmount;
    
    private String productAction;
    
    private String enablerATGProductId;
    private String enablerATGSkuId;
    
    private String recurrenceType;
    
    private String regionName;
    private String regionDesc;
    private String segmentKey;
    private String segmentStatus;
    private String segmentType;
    private String segmentTypeCode;
    private String segmentTypeDesc;
    private Integer segmentLevel;
    private String eligibleAccountTypeList;
    private String eligibleAccountStatus;
    private String eligibleDMA;
    private String eligibleZip;
    private String customerRisk;
    private String salesSystem;
    private String equipmentOption;
    private String marketingSourceCode;
    private String dealerId;
    private List<SegmentOffer> segmentOffers;
    private List<String> salesSubChannel;
    private Integer complianceRank;
    private String billingProductGroup;
    private Boolean includedWithService;
    
    public Boolean getIncludedWithService() {
		return includedWithService;
	}

	public void setIncludedWithService(Boolean includedWithService) {
		this.includedWithService = includedWithService;
	}

	public Integer getComplianceRank() {
        return complianceRank;
    }

    public void setComplianceRank(Integer complianceRank) {
        this.complianceRank = complianceRank;
    }

    public String getBillingProductGroup() {
        return billingProductGroup;
    }

    public void setBillingProductGroup(String billingProductGroup) {
        this.billingProductGroup = billingProductGroup;
    }
    
    public List<String> getSalesSubChannel() {
		return salesSubChannel;
	}

	public void setSalesSubChannel(List<String> salesSubChannel) {
		this.salesSubChannel = salesSubChannel;
	}
    
    private List<String> accessibility;
    
    private Boolean privacyNoticeAgreementRequired;
    private DisclosureMessagesByKey disclosureMessagesByKey;
    private ChannelLineupAlertMessagesByKey channelLineupAlertMessagesByKey;
    private List<String> qualifyingBasePackages;
    
    private String groupCategory;
    private String deviceDisplayCategory;
    private List<ProductWrapper> compatibleDevices;
    
    private Boolean isWaivedForNonFulfillmentDealer;
    
    private Boolean isVisible;
    
    private Boolean requiresBroadband;
    private Boolean requiresBroadbandWaiver;
    private String broadbandWaiver;
    
    private String basePackageIdentifier;
    private String lineItemType;
    private String billingSystem;
    
    private String compatibleBasePackageRangeEnglish;
    private String compatibleBasePackageRangeSpanish;
    private String compatibleBasePackageRangeInternational;
    
    private Boolean strikeThroughPrice;
    
    private String contentChannelId;
    
    private List<String> waiveOffMarketingSourceCode;
    
    private String rewardValue;
    
    private String accessoryLineItemType;
    
    private List<String> waiveOffSalesSubChannel;

	private UIAttributes UIAttributes;

	private String category;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	private List<String> mandatoryProductCategory;

	public List<String> getMandatoryProductCategory() {
		return mandatoryProductCategory;
	}

	public void setMandatoryProductCategory(List<String> mandatoryProductCategory) {
		this.mandatoryProductCategory = mandatoryProductCategory;
	}

	public String getPartnerProductId() {
		return partnerProductId;
	}

	public void setPartnerProductId(String partnerProductId) {
		this.partnerProductId = partnerProductId;
	}

	@JsonProperty("partnerProductId")
	private String partnerProductId;

	public List<String> getWaiveOffSalesSubChannel() {
		return waiveOffSalesSubChannel;
	}

	public void setWaiveOffSalesSubChannel(List<String> waiveOffSalesSubChannel) {
		this.waiveOffSalesSubChannel = waiveOffSalesSubChannel;
	}
    
	public String getAccessoryLineItemType() {
		return accessoryLineItemType;
	}

	public void setAccessoryLineItemType(String accessoryLineItemType) {
		this.accessoryLineItemType = accessoryLineItemType;
	}

	public String getRewardValue() {
		return rewardValue;
	}

	public void setRewardValue(String rewardValue) {
		this.rewardValue = rewardValue;
	}

	public List<String> getWaiveOffMarketingSourceCode() {
		return waiveOffMarketingSourceCode;
	}

	public void setWaiveOffMarketingSourceCode(List<String> waiveOffMarketingSourceCode) {
		this.waiveOffMarketingSourceCode = waiveOffMarketingSourceCode;
	}
    
	public String getContentChannelId() {
		return contentChannelId;
	}

	public void setContentChannelId(String contentChannelId) {
		this.contentChannelId = contentChannelId;
	}

	public Boolean getStrikeThroughPrice() {
		return strikeThroughPrice;
	}

	public void setStrikeThroughPrice(Boolean strikeThroughPrice) {
		this.strikeThroughPrice = strikeThroughPrice;
	}

	public String getCompatibleBasePackageRangeEnglish() {
		return compatibleBasePackageRangeEnglish;
	}

	public void setCompatibleBasePackageRangeEnglish(String compatibleBasePackageRangeEnglish) {
		this.compatibleBasePackageRangeEnglish = compatibleBasePackageRangeEnglish;
	}

	public String getCompatibleBasePackageRangeSpanish() {
		return compatibleBasePackageRangeSpanish;
	}

	public void setCompatibleBasePackageRangeSpanish(String compatibleBasePackageRangeSpanish) {
		this.compatibleBasePackageRangeSpanish = compatibleBasePackageRangeSpanish;
	}

	public String getCompatibleBasePackageRangeInternational() {
		return compatibleBasePackageRangeInternational;
	}

	public void setCompatibleBasePackageRangeInternational(String compatibleBasePackageRangeInternational) {
		this.compatibleBasePackageRangeInternational = compatibleBasePackageRangeInternational;
	}

	public String getLineItemType() {
		return lineItemType;
	}

	public void setLineItemType(String lineItemType) {
		this.lineItemType = lineItemType;
	}

	public String getBillingSystem() {
		return billingSystem;
	}

	public void setBillingSystem(String billingSystem) {
		this.billingSystem = billingSystem;
	}

	public String getBasePackageIdentifier() {
		return basePackageIdentifier;
	}

	public void setBasePackageIdentifier(String basePackageIdentifier) {
		this.basePackageIdentifier = basePackageIdentifier;
	}
    
	public Boolean getRequiresBroadband() {
		return requiresBroadband;
	}

	public void setRequiresBroadband(Boolean requiresBroadband) {
		this.requiresBroadband = requiresBroadband;
	}

	public Boolean getRequiresBroadbandWaiver() {
		return requiresBroadbandWaiver;
	}

	public void setRequiresBroadbandWaiver(Boolean requiresBroadbandWaiver) {
		this.requiresBroadbandWaiver = requiresBroadbandWaiver;
	}

	public String getBroadbandWaiver() {
		return broadbandWaiver;
	}

	public void setBroadbandWaiver(String broadbandWaiver) {
		this.broadbandWaiver = broadbandWaiver;
	}

	public Boolean getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(Boolean isVisible) {
		this.isVisible = isVisible;
	}

	public Boolean getIsWaivedForNonFulfillmentDealer() {
		return isWaivedForNonFulfillmentDealer;
	}

	public void setIsWaivedForNonFulfillmentDealer(Boolean isWaivedForNonFulfillmentDealer) {
		this.isWaivedForNonFulfillmentDealer = isWaivedForNonFulfillmentDealer;
	}

	/**
	 * @return the channelLineupAlertMessagesByKey
	 */
	public ChannelLineupAlertMessagesByKey getChannelLineupAlertMessagesByKey() {
		return channelLineupAlertMessagesByKey;
	}

	/**
	 * @param channelLineupAlertMessagesByKey the channelLineupAlertMessagesByKey to set
	 */
	public void setChannelLineupAlertMessagesByKey(ChannelLineupAlertMessagesByKey channelLineupAlertMessagesByKey) {
		this.channelLineupAlertMessagesByKey = channelLineupAlertMessagesByKey;
	}

	/**
	 * @return the qualifyingBasePackages
	 */
	public List<String> getQualifyingBasePackages() {
		return qualifyingBasePackages;
	}

	/**
	 * @param qualifyingBasePackages the qualifyingBasePackages to set
	 */
	public void setQualifyingBasePackages(List<String> qualifyingBasePackages) {
		this.qualifyingBasePackages = qualifyingBasePackages;
	}

	/**
	 * @return the deviceDisplayCategory
	 */
	public String getDeviceDisplayCategory() {
		return deviceDisplayCategory;
	}

	/**
	 * @param deviceDisplayCategory the deviceDisplayCategory to set
	 */
	public void setDeviceDisplayCategory(String deviceDisplayCategory) {
		this.deviceDisplayCategory = deviceDisplayCategory;
	}

	/**
	 * @return the compatibleDevices
	 */
	public List<ProductWrapper> getCompatibleDevices() {
		return compatibleDevices;
	}

	/**
	 * @param compatibleDevices the compatibleDevices to set
	 */
	public void setCompatibleDevices(List<ProductWrapper> compatibleDevices) {
		this.compatibleDevices = compatibleDevices;
	}

	public boolean ismDUBridePackage() {
		return mDUBridePackage;
	}

	public void setmDUBridePackage(boolean mDUBridePackage) {
		this.mDUBridePackage = mDUBridePackage;
	}

	public int getFreeDeviceCount() {
		return freeDeviceCount;
	}

	public void setFreeDeviceCount(int freeDeviceCount) {
		this.freeDeviceCount = freeDeviceCount;
	}

	public int getAddonCount() {
		return addonCount;
	}

	public void setAddonCount(int addonCount) {
		this.addonCount = addonCount;
	}

	/**
	 * @return the groupCategory
	 */
	public String getGroupCategory() {
		return groupCategory;
	}

	/**
	 * @param groupCategory the groupCategory to set
	 */
	public void setGroupCategory(String groupCategory) {
		this.groupCategory = groupCategory;
	}

	public DisclosureMessagesByKey getDisclosureMessagesByKey() 
	{
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(DisclosureMessagesByKey disclosureMessagesByKey) 
	{
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public Boolean getPrivacyNoticeAgreementRequired() {
		return privacyNoticeAgreementRequired;
	}

	public void setPrivacyNoticeAgreementRequired(Boolean privacyNoticeAgreementRequired) {
		this.privacyNoticeAgreementRequired = privacyNoticeAgreementRequired;
	}

	/**
	 * @return the accessibility
	 */
	public List<String> getAccessibility() {
		return accessibility;
	}

	/**
	 * @param accessibility the accessibility to set
	 */
	public void setAccessibility(List<String> accessibility) {
		this.accessibility = accessibility;
	}

	/**
	 * @return the equipmentOption
	 */
	public String getEquipmentOption() {
		return equipmentOption;
	}

	/**
	 * @param equipmentOption the equipmentOption to set
	 */
	public void setEquipmentOption(String equipmentOption) {
		this.equipmentOption = equipmentOption;
	}

	/**
	 * @return the dealerId
	 */
	public String getDealerId() {
		return dealerId;
	}

	/**
	 * @param dealerId the dealerId to set
	 */
	public void setDealerId(String dealerId) {
		this.dealerId = dealerId;
	}

	/**
	 * @return the regionName
	 */
	public String getRegionName() {
		return regionName;
	}

	/**
	 * @param regionName the regionName to set
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * @return the regionDesc
	 */
	public String getRegionDesc() {
		return regionDesc;
	}

	/**
	 * @param regionDesc the regionDesc to set
	 */
	public void setRegionDesc(String regionDesc) {
		this.regionDesc = regionDesc;
	}

	/**
	 * @return the segmentKey
	 */
	public String getSegmentKey() {
		return segmentKey;
	}

	/**
	 * @param segmentKey the segmentKey to set
	 */
	public void setSegmentKey(String segmentKey) {
		this.segmentKey = segmentKey;
	}

	/**
	 * @return the segmentStatus
	 */
	public String getSegmentStatus() {
		return segmentStatus;
	}

	/**
	 * @param segmentStatus the segmentStatus to set
	 */
	public void setSegmentStatus(String segmentStatus) {
		this.segmentStatus = segmentStatus;
	}

	/**
	 * @return the segmentType
	 */
	public String getSegmentType() {
		return segmentType;
	}

	/**
	 * @param segmentType the segmentType to set
	 */
	public void setSegmentType(String segmentType) {
		this.segmentType = segmentType;
	}

	/**
	 * @return the segmentTypeCode
	 */
	public String getSegmentTypeCode() {
		return segmentTypeCode;
	}

	/**
	 * @param segmentTypeCode the segmentTypeCode to set
	 */
	public void setSegmentTypeCode(String segmentTypeCode) {
		this.segmentTypeCode = segmentTypeCode;
	}

	/**
	 * @return the segmentTypeDesc
	 */
	public String getSegmentTypeDesc() {
		return segmentTypeDesc;
	}

	/**
	 * @param segmentTypeDesc the segmentTypeDesc to set
	 */
	public void setSegmentTypeDesc(String segmentTypeDesc) {
		this.segmentTypeDesc = segmentTypeDesc;
	}

	/**
	 * @return the segmentLevel
	 */
	public Integer getSegmentLevel() {
		return segmentLevel;
	}

	/**
	 * @param segmentLevel the segmentLevel to set
	 */
	public void setSegmentLevel(Integer segmentLevel) {
		this.segmentLevel = segmentLevel;
	}

	/**
	 * @return the eligibleAccountTypeList
	 */
	public String getEligibleAccountTypeList() {
		return eligibleAccountTypeList;
	}

	/**
	 * @param eligibleAccountTypeList the eligibleAccountTypeList to set
	 */
	public void setEligibleAccountTypeList(String eligibleAccountTypeList) {
		this.eligibleAccountTypeList = eligibleAccountTypeList;
	}

	/**
	 * @return the eligibleAccountStatus
	 */
	public String getEligibleAccountStatus() {
		return eligibleAccountStatus;
	}

	/**
	 * @param eligibleAccountStatus the eligibleAccountStatus to set
	 */
	public void setEligibleAccountStatus(String eligibleAccountStatus) {
		this.eligibleAccountStatus = eligibleAccountStatus;
	}

	/**
	 * @return the eligibleDMA
	 */
	public String getEligibleDMA() {
		return eligibleDMA;
	}

	/**
	 * @param eligibleDMA the eligibleDMA to set
	 */
	public void setEligibleDMA(String eligibleDMA) {
		this.eligibleDMA = eligibleDMA;
	}

	/**
	 * @return the eligibleZip
	 */
	public String getEligibleZip() {
		return eligibleZip;
	}

	/**
	 * @param eligibleZip the eligibleZip to set
	 */
	public void setEligibleZip(String eligibleZip) {
		this.eligibleZip = eligibleZip;
	}

	/**
	 * @return the customerRisk
	 */
	public String getCustomerRisk() {
		return customerRisk;
	}

	/**
	 * @param customerRisk the customerRisk to set
	 */
	public void setCustomerRisk(String customerRisk) {
		this.customerRisk = customerRisk;
	}

	/**
	 * @return the salesSystem
	 */
	public String getSalesSystem() {
		return salesSystem;
	}

	/**
	 * @param salesSystem the salesSystem to set
	 */
	public void setSalesSystem(String salesSystem) {
		this.salesSystem = salesSystem;
	}

	/**
	 * @return the marketingSourceCode
	 */
	public String getMarketingSourceCode() {
		return marketingSourceCode;
	}

	/**
	 * @param marketingSourceCode the marketingSourceCode to set
	 */
	public void setMarketingSourceCode(String marketingSourceCode) {
		this.marketingSourceCode = marketingSourceCode;
	}

	/**
	 * @return the segmentOffers
	 */
	public List<SegmentOffer> getSegmentOffers() {
		return segmentOffers;
	}

	/**
	 * @param segmentOffers the segmentOffers to set
	 */
	public void setSegmentOffers(List<SegmentOffer> segmentOffers) {
		this.segmentOffers = segmentOffers;
	}
    
    public String getRecurrenceType() {
		return recurrenceType;
	}

	public void setRecurrenceType(String recurrenceType) {
		this.recurrenceType = recurrenceType;
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

	public String getProductAction() {
		return productAction;
	}

	public void setProductAction(String productAction) {
		this.productAction = productAction;
	}

	public Double getDollarAmount() {
		return dollarAmount;
	}

	public void setDollarAmount(Double dollarAmount) {
		this.dollarAmount = dollarAmount;
	}

    private AutoRenewMessages autoRenewMessages;
    
	public String getPick5Mask() {
		return pick5Mask;
	}

	public void setPick5Mask(String pick5Mask) {
		this.pick5Mask = pick5Mask;
	}

	public String getOrderValidFromDate() {
		return orderValidFromDate;
	}

	public void setOrderValidFromDate(String orderValidFromDate) {
		this.orderValidFromDate = orderValidFromDate;
	}

	public String getOrderValidToDate() {
		return orderValidToDate;
	}

	public void setOrderValidToDate(String orderValidToDate) {
		this.orderValidToDate = orderValidToDate;
	}

	public String getReceiverLineType() {
		return receiverLineType;
	}

	public void setReceiverLineType(String receiverLineType) {
		this.receiverLineType = receiverLineType;
	}

	public String getRemoteReplacementType() {
		return remoteReplacementType;
	}

	public void setRemoteReplacementType(String remoteReplacementType) {
		this.remoteReplacementType = remoteReplacementType;
	}

	public List<ProductKey> getProductKey() {
		return productKey;
	}

	public void setProductKey(List<ProductKey> productKey) {
		this.productKey = productKey;
	}
	
	public List<GrandFatherInfo> getGrandFatherInfo() {
		return grandFatherInfo;
	}

	public void setGrandFatherInfo(List<GrandFatherInfo> grandFatherInfo) {
		this.grandFatherInfo = grandFatherInfo;
	}

	
	public Integer getMaxSTBsWithServer() {
		return maxSTBsWithServer;
	}

	public void setMaxSTBsWithServer(Integer maxSTBsWithServer) {
		this.maxSTBsWithServer = maxSTBsWithServer;
	}

	public Boolean getIsETFRequired() {
		return isETFRequired;
	}

	public void setIsETFRequired(Boolean isETFRequired) {
		this.isETFRequired = isETFRequired;
	}

	public Boolean getIsRemovable() {
		return isRemovable;
	}

	public void setIsRemovable(Boolean isRemovable) {
		this.isRemovable = isRemovable;
	}

	public Integer getHeroOfferRanking() {
		return heroOfferRanking;
	}

	public void setHeroOfferRanking(Integer heroOfferRanking) {
		this.heroOfferRanking = heroOfferRanking;
	}

	public List<GenericNameValueBase> getBillingParams() {
		return billingParams;
	}

	public void setBillingParams(List<GenericNameValueBase> billingParams) {
		this.billingParams = billingParams;
	}

	public Integer getMaxSTBsWired() {
		return maxSTBsWired;
	}

	public void setMaxSTBsWired(Integer maxSTBsWired) {
		this.maxSTBsWired = maxSTBsWired;
	}

	public Integer getMaxDVRs() {
		return maxDVRs;
	}

	public void setMaxDVRs(Integer maxDVRs) {
		this.maxDVRs = maxDVRs;
	}

	public Integer getMaxSTBsWIFI() {
		return maxSTBsWIFI;
	}

	public void setMaxSTBsWIFI(Integer maxSTBsWIFI) {
		this.maxSTBsWIFI = maxSTBsWIFI;
	}

	public String getOmsProductCode() {
		return omsProductCode;
	}

	public void setOmsProductCode(String omsProductCode) {
		this.omsProductCode = omsProductCode;
	}

	public String getOrderingMethod() {
		return orderingMethod;
	}

	public void setOrderingMethod(String orderingMethod) {
		this.orderingMethod = orderingMethod;
	}

	public String getEnablerPricePlanCode() {
		return enablerPricePlanCode;
	}

	public void setEnablerPricePlanCode(String enablerPricePlanCode) {
		this.enablerPricePlanCode = enablerPricePlanCode;
	}

	public String getTokens() {
		return tokens;
	}

	public void setTokens(String tokens) {
		this.tokens = tokens;
	}

	public String getChargeCode() {
		return chargeCode;
	}

	public void setChargeCode(String chargeCode) {
		this.chargeCode = chargeCode;
	}

	public Boolean getIsWorldDirect() {
		return isWorldDirect;
	}

	public void setIsWorldDirect(Boolean isWorldDirect) {
		this.isWorldDirect = isWorldDirect;
	}

	public Boolean getIsChangeable() {
		return isChangeable;
	}

	public void setIsChangeable(Boolean isChangeable) {
		this.isChangeable = isChangeable;
	}

	/**
	 * @return the benefitsOnCustomersAccount
	 */
	public List<String> getBenefitsOnCustomersAccount() {
		return benefitsOnCustomersAccount;
	}

	/**
	 * @param benefitsOnCustomersAccount the benefitsOnCustomersAccount to set
	 */
	public void setBenefitsOnCustomersAccount(List<String> benefitsOnCustomersAccount) {
		this.benefitsOnCustomersAccount = benefitsOnCustomersAccount;
	}

	/**
	 * @return the migratedProduct
	 */
	public Boolean getMigratedProduct() {
		return migratedProduct;
	}

	/**
	 * @param migratedProduct
	 *            the migratedProduct to set
	 */
	public void setMigratedProduct(Boolean migratedProduct) {
		this.migratedProduct = migratedProduct;
	}

	private String url;

	private String contentImages;

	private String productImages;

	private String productHighlights;

	private GenericLocaleBase description;
	
	private List<List<IncompatibleProducts>> incompatibleProducts;
	
	private String doNotAutoRenewBillingProductCode;
	
	private String doNotAutoRenewBillingReferenceId;
	
	private String nonRefundDate;
	
	private String deviceState;

	private String warrantyDuration;
	
	public String getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(String deviceState) {
		this.deviceState = deviceState;
	}

	public String getWarrantyDuration() {
		return warrantyDuration;
	}

	public void setWarrantyDuration(String warrantyDuration) {
		this.warrantyDuration = warrantyDuration;
	}
	
	public String getContentImages() {
		return contentImages;
	}

	public void setContentImages(String contentImages) {
		this.contentImages = contentImages;
	}

	public String getProductImages() {
		return productImages;
	}

	public void setProductImages(String productImages) {
		this.productImages = productImages;
	}

	public List<List<IncompatibleProducts>> getIncompatibleProducts() {
		return incompatibleProducts;
	}

	public void setIncompatibleProducts(List<List<IncompatibleProducts>> incompatibleProducts) {
		this.incompatibleProducts = incompatibleProducts;
	}

	public String getProductHighlights() {
		return productHighlights;
	}

	public void setProductHighlights(String productHighlights) {
		this.productHighlights = productHighlights;
	}

	public GenericLocaleBase getDescription() {
		return description;
	}

	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<DeliveryMethodByChannel> getDeliveryMethodByChannels() {
		return deliveryMethodByChannels;
	}

	public void setDeliveryMethodByChannels(List<DeliveryMethodByChannel> deliveryMethodByChannels) {
		this.deliveryMethodByChannels = deliveryMethodByChannels;
	}

	public Integer getMaxNoOfLines() {
		return maxNoOfLines;
	}

	public void setMaxNoOfLines(Integer maxNoOfLines) {
		this.maxNoOfLines = maxNoOfLines;
	}

	/** The includedEmployeeProducts. */
	private List<IncludeProductWrapper> includedEmployeeProducts;

	/**
	 * @return the includedEmployeeProducts
	 */
	public List<IncludeProductWrapper> getIncludedEmployeeProducts() {
		return includedEmployeeProducts;
	}

	/**
	 * @param includedEmployeeProducts
	 *            the includedEmployeeProducts to set
	 */
	public void setIncludedEmployeeProducts(List<IncludeProductWrapper> includedEmployeeProducts) {
		this.includedEmployeeProducts = includedEmployeeProducts;
	}

	/**
	 * @return the conflictingProducts
	 */
	public List<GenericTypeIdBase> getConflictingProducts() {
		return conflictingProducts;
	}

	/**
	 * @param conflictingProducts
	 *            the conflictingProducts to set
	 */
	public void setConflictingProducts(List<GenericTypeIdBase> conflictingProducts) {
		this.conflictingProducts = conflictingProducts;
	}

	public String getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}

	public String getCampaignCode() {
		return campaignCode;
	}

	public void setCampaignCode(String campaignCode) {
		this.campaignCode = campaignCode;
	}

	public String getCampaignName() {
		return campaignName;
	}

	public void setCampaignName(String campaignName) {
		this.campaignName = campaignName;
	}

	public String getCampaignDesc() {
		return campaignDesc;
	}

	public void setCampaignDesc(String campaignDesc) {
		this.campaignDesc = campaignDesc;
	}

	public String getCouponType() {
		return couponType;
	}

	public void setCouponType(String couponType) {
		this.couponType = couponType;
	}

	public String getCampaignCategory() {
		return campaignCategory;
	}

	public void setCampaignCategory(String campaignCategory) {
		this.campaignCategory = campaignCategory;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getCampaignStartDate() {
		return campaignStartDate;
	}

	public void setCampaignStartDate(String campaignStartDate) {
		this.campaignStartDate = campaignStartDate;
	}

	public String getCampaignEndDate() {
		return campaignEndDate;
	}

	public void setCampaignEndDate(String campaignEndDate) {
		this.campaignEndDate = campaignEndDate;
	}

	public String getAutoRegister() {
		return autoRegister;
	}

	public void setAutoRegister(String autoRegister) {
		this.autoRegister = autoRegister;
	}

	public Boolean getStudioSponsorByATT() {
		return studioSponsorByATT;
	}

	public void setStudioSponsorByATT(Boolean studioSponsorByATT) {
		this.studioSponsorByATT = studioSponsorByATT;
	}

	public String getLockDuration() {
		return lockDuration;
	}

	public void setLockDuration(String lockDuration) {
		this.lockDuration = lockDuration;
	}

	public Integer getNumberOfCoupons() {
		return numberOfCoupons;
	}

	public void setNumberOfCoupons(Integer numberOfCoupons) {
		this.numberOfCoupons = numberOfCoupons;
	}

	public String getCampaignMessaging() {
		return campaignMessaging;
	}

	public void setCampaignMessaging(String campaignMessaging) {
		this.campaignMessaging = campaignMessaging;
	}

	public String getValueRanking() {
		return valueRanking;
	}

	public void setValueRanking(String valueRanking) {
		this.valueRanking = valueRanking;
	}

	public String getAccessoryCategory() {
		return accessoryCategory;
	}

	public void setAccessoryCategory(String accessoryCategory) {
		this.accessoryCategory = accessoryCategory;
	}

	public List<VideoDeviceLimit> getVideoDeviceLimit() {
		return videoDeviceLimit;
	}

	public void setVideoDeviceLimit(List<VideoDeviceLimit> videoDeviceLimit) {
		this.videoDeviceLimit = videoDeviceLimit;
	}

	public List<VideoDevicePolicy> getVideoDevicePolicy() {
		return videoDevicePolicy;
	}

	public void setVideoDevicePolicy(List<VideoDevicePolicy> videoDevicePolicy) {
		this.videoDevicePolicy = videoDevicePolicy;
	}

	public String getDowngradeableRule() {
		return downgradeableRule;
	}

	public void setDowngradeableRule(String downgradeableRule) {
		this.downgradeableRule = downgradeableRule;
	}

	public List<Rule> getRestrictionRule() {
		return restrictionRule;
	}

	public void setRestrictionRule(List<Rule> restrictionRule) {
		this.restrictionRule = restrictionRule;
	}

	public List<Rule> getCallToOrderRule() {
		return callToOrderRule;
	}

	public void setCallToOrderRule(List<Rule> callToOrderRule) {
		this.callToOrderRule = callToOrderRule;
	}

	public List<Rule> getUpgradeableRule() {
		return upgradeableRule;
	}

	public void setUpgradeableRule(List<Rule> upgradeableRule) {
		this.upgradeableRule = upgradeableRule;
	}

	public List<CompatibleVideoDeviceModel> getCompatibleVideoDeviceModels() {
		return compatibleVideoDeviceModels;
	}

	public void setCompatibleVideoDeviceModels(List<CompatibleVideoDeviceModel> compatibleVideoDeviceModels) {
		this.compatibleVideoDeviceModels = compatibleVideoDeviceModels;
	}

	public Installment getInstallment() {
		return installment;
	}

	public void setInstallment(Installment installment) {
		this.installment = installment;
	}

	public String getAccessoryType() {
		return accessoryType;
	}

	public void setAccessoryType(String accessoryType) {
		this.accessoryType = accessoryType;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getDeviceCategory() {
		return deviceCategory;
	}

	public void setDeviceCategory(String deviceCategory) {
		this.deviceCategory = deviceCategory;
	}

	public String getAddonType() {
		return addonType;
	}

	public void setAddonType(String addonType) {
		this.addonType = addonType;
	}

	public String getPlanType() {
		return planType;
	}

	public void setPlanType(String planType) {
		this.planType = planType;
	}

//	public boolean isWirelessCapable() {
	public boolean getIsWirelessCapable() {
		return isWirelessCapable;
	}

	//public void setWirelessCapable(boolean isWirelessCapable) {
	public void setIsWirelessCapable(boolean isWirelessCapable) {
		this.isWirelessCapable = isWirelessCapable;
	}

	public String getPurchaseType() {
		return purchaseType;
	}

	public void setPurchaseType(String purchaseType) {
		this.purchaseType = purchaseType;
	}

	public int getMaxNumberOfInstallments() {
		return maxNumberOfInstallments;
	}

	public void setMaxNumberOfInstallments(int maxNumberOfInstallments) {
		this.maxNumberOfInstallments = maxNumberOfInstallments;
	}

	public int getMaxNumberOfPreSeasonInstallments() {
		return maxNumberOfPreSeasonInstallments;
	}

	public void setMaxNumberOfPreSeasonInstallments(int maxNumberOfPreSeasonInstallments) {
		this.maxNumberOfPreSeasonInstallments = maxNumberOfPreSeasonInstallments;
	}

	public int getMaxNumberOfFullSeasonInstallments() {
		return maxNumberOfFullSeasonInstallments;
	}

	public void setMaxNumberOfFullSeasonInstallments(int maxNumberOfFullSeasonInstallments) {
		this.maxNumberOfFullSeasonInstallments = maxNumberOfFullSeasonInstallments;
	}

	public int getMaxNumberOfMidSeasonInstallments() {
		return maxNumberOfMidSeasonInstallments;
	}

	public void setMaxNumberOfMidSeasonInstallments(int maxNumberOfMidSeasonInstallments) {
		this.maxNumberOfMidSeasonInstallments = maxNumberOfMidSeasonInstallments;
	}

	public int getMaxNumberOfPostSeasonInstallments() {
		return maxNumberOfPostSeasonInstallments;
	}

	public void setMaxNumberOfPostSeasonInstallments(int maxNumberOfPostSeasonInstallments) {
		this.maxNumberOfPostSeasonInstallments = maxNumberOfPostSeasonInstallments;
	}

	public String getSeasonStartDate() {
		return seasonStartDate;
	}

	public void setSeasonStartDate(String seasonStartDate) {
		this.seasonStartDate = seasonStartDate;
	}

	public String getSeasonEndDate() {
		return seasonEndDate;
	}

	public void setSeasonEndDate(String seasonEndDate) {
		this.seasonEndDate = seasonEndDate;
	}

	public String getComplianceRanking() {
		return complianceRanking;
	}

	public void setComplianceRanking(String complianceRanking) {
		this.complianceRanking = complianceRanking;
	}

	public List<String> getChannelLineup() {
		return channelLineup;
	}

	public void setChannelLineup(List<String> channelLineup) {
		this.channelLineup = channelLineup;
	}

	public int getMaxOrderLimit() {
		return maxOrderLimit;
	}

	public void setMaxOrderLimit(int maxOrderLimit) {
		this.maxOrderLimit = maxOrderLimit;
	}

	public int getMaxAccountLimit() {
		return maxAccountLimit;
	}

	public void setMaxAccountLimit(int maxAccountLimit) {
		this.maxAccountLimit = maxAccountLimit;
	}

	public GenericLocaleBase getModelNumbers() {
		return modelNumbers;
	}

	public void setModelNumbers(GenericLocaleBase modelNumbers) {
		this.modelNumbers = modelNumbers;
	}

	public String getDeliveryMethod() {
		return deliveryMethod;
	}

	public void setDeliveryMethod(String deliveryMethod) {
		this.deliveryMethod = deliveryMethod;
	}

	public List<String> getDeliverymethod() {
		return deliverymethod;
	}

	public void setDeliverymethod(List<String> deliverymethod) {
		this.deliverymethod = deliverymethod;
	}

	public String getBillingCode() {
		return billingCode;
	}

	public void setBillingCode(String billingCode) {
		this.billingCode = billingCode;
	}

	public String getFeeType() {
		return feeType;
	}

	public void setFeeType(String feeType) {
		this.feeType = feeType;
	}

	public List<IncludeProductWrapper> getIncludedProducts() {
		return includedProducts;
	}

	public void setIncludedProducts(List<IncludeProductWrapper> includedProducts) {
		this.includedProducts = includedProducts;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public List<String> getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	/*public Boolean getIsInternationalVideoSubscriptionRequired() {
		return isInternationalVideoSubscriptionRequired;
	}

	public void setIsInternationalVideoSubscriptionRequired(Boolean isInternationalVideoSubscriptionRequired) {
		this.isInternationalVideoSubscriptionRequired = isInternationalVideoSubscriptionRequired;
	}*/
	
	public Boolean getIsInternationalAddonRequired() {
		return isInternationalAddonRequired;
	}

	public void setIsInternationalAddonRequired(Boolean isInternationalAddonRequired) {
		this.isInternationalAddonRequired = isInternationalAddonRequired;
	}

	public Boolean getHasLocalChannels() {
		return hasLocalChannels;
	}

	public void setHasLocalChannels(Boolean hasLocalChannels) {
		this.hasLocalChannels = hasLocalChannels;
	}

	public Boolean getIsPickService() {
		return isPickService;
	}

	public void setIsPickService(Boolean isPickService) {
		this.isPickService = isPickService;
	}

	public Boolean getIsRequiredAddonForInternationalVideoPlan() {
		return isRequiredAddonForInternationalVideoPlan;
	}

	public void setIsRequiredAddonForInternationalVideoPlan(Boolean isRequiredAddonForInternationalVideoPlan) {
		this.isRequiredAddonForInternationalVideoPlan = isRequiredAddonForInternationalVideoPlan;
	}

	public Boolean getIsAutoRenewable() {
		return isAutoRenewable;
	}

	public void setIsAutoRenewable(Boolean isAutoRenewable) {
		this.isAutoRenewable = isAutoRenewable;
	}

	public String getSelfInstallAvailability() {
		return selfInstallAvailability;
	}

	public void setSelfInstallAvailability(String selfInstallAvailability) {
		this.selfInstallAvailability = selfInstallAvailability;
	}

	public Boolean getIsAsurionPlan() {
		return isAsurionPlan;
	}

	public void setIsAsurionPlan(Boolean isAsurionPlan) {
		this.isAsurionPlan = isAsurionPlan;
	}

	public Boolean getVideoBasePackageRequired() {
		return videoBasePackageRequired;
	}

	public void setVideoBasePackageRequired(Boolean videoBasePackageRequired) {
		this.videoBasePackageRequired = videoBasePackageRequired;
	}

	public GenericLocaleBase getTermsAndConditions() {
		return termsAndConditions;
	}

	public void setTermsAndConditions(GenericLocaleBase termsAndConditions) {
		this.termsAndConditions = termsAndConditions;
	}

	public List<Object> getSpecifications() {
		return specifications;
	}

	public void setSpecifications(List<Object> specifications) {
		this.specifications = specifications;
	}

	public Boolean getIsThirdParty() {
		return isThirdParty;
	}

	public void setIsThirdParty(Boolean isThirdParty) {
		this.isThirdParty = isThirdParty;
	}

	/**
	 * Gets the customer segments.
	 *
	 * @return the customer segments
	 */
	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	/**
	 * Sets the customer segments.
	 *
	 * @param customerSegments
	 *            the new customer segments
	 */
	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	/**
	 * Gets the conflict promo list.
	 *
	 * @return the conflictPromoList
	 */
	public List<String> getConflictPromoList() {
		return conflictPromoList;
	}

	/**
	 * Sets the conflict promo list.
	 *
	 * @param conflictPromoList
	 *            the conflictPromoList to set
	 */
	public void setConflictPromoList(List<String> conflictPromoList) {
		this.conflictPromoList = conflictPromoList;
	}

	/**
	 * Gets the compatible mobility products.
	 *
	 * @return the compatibleMobilityProducts
	 */
	public List<ProductWrapper> getCompatibleMobilityProducts() {
		return compatibleMobilityProducts;
	}

	/**
	 * Sets the compatible mobility products.
	 *
	 * @param compatibleMobilityProducts
	 *            the compatibleMobilityProducts to set
	 */
	public void setCompatibleMobilityProducts(List<ProductWrapper> compatibleMobilityProducts) {
		this.compatibleMobilityProducts = compatibleMobilityProducts;
	}

	/**
	 * Gets the equipment sku.
	 *
	 * @return the equipmentSku
	 */
	public String getEquipmentSku() {
		return equipmentSku;
	}

	/**
	 * Sets the equipment sku.
	 *
	 * @param equipmentSku
	 *            the equipmentSku to set
	 */
	public void setEquipmentSku(String equipmentSku) {
		this.equipmentSku = equipmentSku;
	}

	/**
	 * Gets the number of spanish channels.
	 *
	 * @return the numberOfSpanishChannels
	 */
	public String getNumberOfSpanishChannels() {
		return numberOfSpanishChannels;
	}

	/**
	 * Sets the number of spanish channels.
	 *
	 * @param numberOfSpanishChannels
	 *            the numberOfSpanishChannels to set
	 */
	public void setNumberOfSpanishChannels(String numberOfSpanishChannels) {
		this.numberOfSpanishChannels = numberOfSpanishChannels;
	}

	/**
	 * Gets the discount sku.
	 *
	 * @return the discountSku
	 */
	public String getDiscountSku() {
		return discountSku;
	}

	/**
	 * Sets the discount sku.
	 *
	 * @param discountSku
	 *            the discountSku to set
	 */
	public void setDiscountSku(String discountSku) {
		this.discountSku = discountSku;
	}

	/**
	 * Gets the product status.
	 *
	 * @return the productStatus
	 */
	public String getProductStatus() {
		return productStatus;
	}

	/**
	 * Sets the product status.
	 *
	 * @param productStatus
	 *            the productStatus to set
	 */
	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}

	/**
	 * Gets the compatible products.
	 *
	 * @return the compatibleProducts
	 */
	public List<ProductWrapper> getCompatibleProducts() {
		return compatibleProducts;
	}

	/**
	 * Sets the compatible products.
	 *
	 * @param compatibleProducts
	 *            the compatibleProducts to set
	 */
	public void setCompatibleProducts(List<ProductWrapper> compatibleProducts) {
		this.compatibleProducts = compatibleProducts;
	}

	/**
	 * Gets the dependent promo name.
	 *
	 * @return the dependentPromoName
	 */
	public String getDependentPromoName() {
		return dependentPromoName;
	}

	/**
	 * Sets the dependent promo name.
	 *
	 * @param dependentPromoName
	 *            the dependentPromoName to set
	 */
	public void setDependentPromoName(String dependentPromoName) {
		this.dependentPromoName = dependentPromoName;
	}

	/**
	 * Gets the dependent promo description.
	 *
	 * @return the dependentPromoDescription
	 */
	public String getDependentPromoDescription() {
		return dependentPromoDescription;
	}

	/**
	 * Sets the dependent promo description.
	 *
	 * @param dependentPromoDescription
	 *            the dependentPromoDescription to set
	 */
	public void setDependentPromoDescription(String dependentPromoDescription) {
		this.dependentPromoDescription = dependentPromoDescription;
	}

	/**
	 * Gets the dependent promo display name.
	 *
	 * @return the dependentPromoDisplayName
	 */
	public String getDependentPromoDisplayName() {
		return dependentPromoDisplayName;
	}

	/**
	 * Sets the dependent promo display name.
	 *
	 * @param dependentPromoDisplayName
	 *            the dependentPromoDisplayName to set
	 */
	public void setDependentPromoDisplayName(String dependentPromoDisplayName) {
		this.dependentPromoDisplayName = dependentPromoDisplayName;
	}

	/**
	 * Gets the dependent promo code.
	 *
	 * @return the dependentPromoCode
	 */
	public String getDependentPromoCode() {
		return dependentPromoCode;
	}

	/**
	 * Sets the dependent promo code.
	 *
	 * @param dependentPromoCode
	 *            the dependentPromoCode to set
	 */
	public void setDependentPromoCode(String dependentPromoCode) {
		this.dependentPromoCode = dependentPromoCode;
	}

	/**
	 * Gets the dependent biller promo id.
	 *
	 * @return the dependentBillerPromoId
	 */
	public String getDependentBillerPromoId() {
		return dependentBillerPromoId;
	}

	/**
	 * Sets the dependent biller promo id.
	 *
	 * @param dependentBillerPromoId
	 *            the dependentBillerPromoId to set
	 */
	public void setDependentBillerPromoId(String dependentBillerPromoId) {
		this.dependentBillerPromoId = dependentBillerPromoId;
	}

	/**
	 * Gets the dependent promo start date.
	 *
	 * @return the dependentPromoStartDate
	 */
	public String getDependentPromoStartDate() {
		return dependentPromoStartDate;
	}

	/**
	 * Sets the dependent promo start date.
	 *
	 * @param dependentPromoStartDate
	 *            the dependentPromoStartDate to set
	 */
	public void setDependentPromoStartDate(String dependentPromoStartDate) {
		this.dependentPromoStartDate = dependentPromoStartDate;
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
	 * @param displayName
	 *            the displayName to set
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
	 * @param displayNamesByKey
	 *            the displayNamesByKey to set
	 */
	public void setDisplayNamesByKey(GenericByKey displayNamesByKey) {
		this.displayNamesByKey = displayNamesByKey;
	}

	/**
	 * Gets the descriptions by key.
	 *
	 * @return the descriptionsByKey
	 */
	public ProductDescriptionsByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}

	/**
	 * Sets the descriptions by key.
	 *
	 * @param descriptionsByKey
	 *            the descriptionsByKey to set
	 */
	public void setDescriptionsByKey(ProductDescriptionsByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}

	/**
	 * Gets the billing product code.
	 *
	 * @return the billingProductCode
	 */
	public String getBillingProductCode() {
		return billingProductCode;
	}

	/**
	 * Sets the billing product code.
	 *
	 * @param billingProductCode
	 *            the billingProductCode to set
	 */
	public void setBillingProductCode(String billingProductCode) {
		this.billingProductCode = billingProductCode;
	}

	/**
	 * Gets the billing product id.
	 *
	 * @return the billingProductId
	 */
	public String getBillingProductId() {
		return billingProductId;
	}

	/**
	 * Sets the billing product id.
	 *
	 * @param billingProductId
	 *            the billingProductId to set
	 */
	public void setBillingProductId(String billingProductId) {
		this.billingProductId = billingProductId;
	}

	/**
	 * Gets the start date.
	 *
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate
	 *            the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the end date.
	 *
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * Sets the end date.
	 *
	 * @param endDate
	 *            the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * @return the provisioningCode
	 */
	public String getProvisioningCode() {
		return provisioningCode;
	}

	/**
	 * Sets the provisioning code.
	 *
	 * @param provisioningCode
	 *            the provisioningCode to set
	 */
	public void setProvisioningCode(String provisioningCode) {
		this.provisioningCode = provisioningCode;
	}

	/**
	 * Gets the congrats message.
	 *
	 * @return the congratsMessage
	 */
	public String getCongratsMessage() {
		return congratsMessage;
	}

	/**
	 * Sets the congrats message.
	 *
	 * @param congratsMessage
	 *            the congratsMessage to set
	 */
	public void setCongratsMessage(String congratsMessage) {
		this.congratsMessage = congratsMessage;
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
	 * @param opusDisclosureMessage
	 *            the opusDisclosureMessage to set
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
	 * @param myAttDisclosureMessage
	 *            the myAttDisclosureMessage to set
	 */
	public void setMyAttDisclosureMessage(String myAttDisclosureMessage) {
		this.myAttDisclosureMessage = myAttDisclosureMessage;
	}

	/**
	 * @return the ranking
	 */
	public int getRanking() {
		return ranking;
	}

	/**
	 * Sets the ranking.
	 *
	 * @param ranking
	 *            the ranking to set
	 */
	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	/**
	 * Gets the att project.
	 *
	 * @return the attProject
	 */
	public String getAttProject() {
		return attProject;
	}

	/**
	 * Sets the att project.
	 *
	 * @param attProject
	 *            the attProject to set
	 */
	public void setAttProject(String attProject) {
		this.attProject = attProject;
	}

	/**
	 * Gets the product family.
	 *
	 * @return the productFamily
	 */
	public String getProductFamily() {
		return productFamily;
	}

	/**
	 * Sets the product family.
	 *
	 * @param productFamily
	 *            the productFamily to set
	 */
	public void setProductFamily(String productFamily) {
		this.productFamily = productFamily;
	}

	/**
	 * Gets the package group.
	 *
	 * @return the packageGroup
	 */
	public String getPackageGroup() {
		return packageGroup;
	}

	/**
	 * Sets the package group.
	 *
	 * @param packageGroup
	 *            the packageGroup to set
	 */
	public void setPackageGroup(String packageGroup) {
		this.packageGroup = packageGroup;
	}

	/**
	 * Gets the fufillment attribute 1.
	 *
	 * @return the fufillmentAttribute1
	 */
	public String getFufillmentAttribute1() {
		return fufillmentAttribute1;
	}

	/**
	 * Sets the fufillment attribute 1.
	 *
	 * @param fufillmentAttribute1
	 *            the fufillmentAttribute1 to set
	 */
	public void setFufillmentAttribute1(String fufillmentAttribute1) {
		this.fufillmentAttribute1 = fufillmentAttribute1;
	}

	/**
	 * Gets the fufillment attribute 2.
	 *
	 * @return the fufillmentAttribute2
	 */
	public String getFufillmentAttribute2() {
		return fufillmentAttribute2;
	}

	/**
	 * Sets the fufillment attribute 2.
	 *
	 * @param fufillmentAttribute2
	 *            the fufillmentAttribute2 to set
	 */
	public void setFufillmentAttribute2(String fufillmentAttribute2) {
		this.fufillmentAttribute2 = fufillmentAttribute2;
	}

	/**
	 * Gets the removal rule.
	 *
	 * @return the removalRule
	 */
	public RemovalRule getRemovalRule() {
		return removalRule;
	}

	/**
	 * Sets the removal rule.
	 *
	 * @param removalRule
	 *            the removalRule to set
	 */
	public void setRemovalRule(RemovalRule removalRule) {
		this.removalRule = removalRule;
	}

	/**
	 * Gets the service type.
	 *
	 * @return the serviceType
	 */
	public String getServiceType() {
		return serviceType;
	}

	/**
	 * Sets the service type.
	 *
	 * @param serviceType
	 *            the serviceType to set
	 */
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	
	/**
	 * Gets the pre req products.
	 *
	 * @return the preReqProducts
	 */
	public List<String> getPreReqProducts() {
		return preReqProducts;
	}

	/**
	 * Sets the pre req products.
	 *
	 * @param preReqProducts
	 *            the preReqProducts to set
	 */
	public void setPreReqProducts(List<String> preReqProducts) {
		this.preReqProducts = preReqProducts;
	}

	/**
	 * Gets the upgradable plans.
	 *
	 * @return the upgradablePlans
	 */
	public List<GenericTypeIdBase> getUpgradablePlans() {
		return upgradablePlans;
	}

	/**
	 * Sets the upgradable plans.
	 *
	 * @param upgradablePlans
	 *            the upgradablePlans to set
	 */
	public void setUpgradablePlans(List<GenericTypeIdBase> upgradablePlans) {
		this.upgradablePlans = upgradablePlans;
	}

	/**
	 * Gets the downgradable plans.
	 *
	 * @return the downgradablePlans
	 */
	public List<GenericTypeIdBase> getDowngradablePlans() {
		return downgradablePlans;
	}

	/**
	 * Sets the downgradable plans.
	 *
	 * @param downgradablePlans
	 *            the downgradablePlans to set
	 */
	public void setDowngradablePlans(List<GenericTypeIdBase> downgradablePlans) {
		this.downgradablePlans = downgradablePlans;
	}

	public Boolean getIsRSNCapable() {
		return isRSNCapable;
	}

	public void setIsRSNCapable(Boolean isRSNCapable) {
		this.isRSNCapable = isRSNCapable;
	}

	/**
	 * Gets the marketing desc link.
	 *
	 * @return the marketingDescLink
	 */
	public String getMarketingDescLink() {
		return marketingDescLink;
	}

	/**
	 * Sets the marketing desc link.
	 *
	 * @param marketingDescLink
	 *            the marketingDescLink to set
	 */
	public void setMarketingDescLink(String marketingDescLink) {
		this.marketingDescLink = marketingDescLink;
	}

	/**
	 * Gets the number of channels.
	 *
	 * @return the numberOfChannels
	 */
	public String getNumberOfChannels() {
		return numberOfChannels;
	}

	/**
	 * Sets the number of channels.
	 *
	 * @param numberOfChannels
	 *            the numberOfChannels to set
	 */
	public void setNumberOfChannels(String numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	/**
	 * Gets the english channel list.
	 *
	 * @return the englishChannelList
	 */
	public List<String> getEnglishChannelList() {
		return englishChannelList;
	}

	/**
	 * Sets the english channel list.
	 *
	 * @param englishChannelList
	 *            the englishChannelList to set
	 */
	public void setEnglishChannelList(List<String> englishChannelList) {
		this.englishChannelList = englishChannelList;
	}

	/**
	 * Gets the spanish channel list.
	 *
	 * @return the spanishChannelList
	 */
	public List<String> getSpanishChannelList() {
		return spanishChannelList;
	}

	/**
	 * Sets the spanish channel list.
	 *
	 * @param spanishChannelList
	 *            the spanishChannelList to set
	 */
	public void setSpanishChannelList(List<String> spanishChannelList) {
		this.spanishChannelList = spanishChannelList;
	}

	/**
	 * Gets the featured channels.
	 *
	 * @return the featuredChannels
	 */
	public List<String> getFeaturedChannels() {
		return featuredChannels;
	}

	/**
	 * Sets the featured channels.
	 *
	 * @param featuredChannels
	 *            the featuredChannels to set
	 */
	public void setFeaturedChannels(List<String> featuredChannels) {
		this.featuredChannels = featuredChannels;
	}

	/**
	 * Gets the adds the on type.
	 *
	 * @return the addOnType
	 */
	public String getAddOnType() {
		return addOnType;
	}

	/**
	 * Sets the adds the on type.
	 *
	 * @param addOnType
	 *            the addOnType to set
	 */
	public void setAddOnType(String addOnType) {
		this.addOnType = addOnType;
	}

	/**
	 * Gets the plan sub type.
	 *
	 * @return the planSubType
	 */
	public String getPlanSubType() {
		return planSubType;
	}

	/**
	 * Sets the plan sub type.
	 *
	 * @param planSubType
	 *            the planSubType to set
	 */
	public void setPlanSubType(String planSubType) {
		this.planSubType = planSubType;
	}

	/**
	 * Gets the capacity limit.
	 *
	 * @return the capacityLimit
	 */
	public int getCapacityLimit() {
		return capacityLimit;
	}

	/**
	 * Sets the capacity limit.
	 *
	 * @param capacityLimit
	 *            the capacityLimit to set
	 */
	public void setCapacityLimit(int capacityLimit) {
		this.capacityLimit = capacityLimit;
	}

	/**
	 * Gets the data rentention period.
	 *
	 * @return the dataRententionPeriod
	 */
	public String getDataRententionPeriod() {
		return dataRententionPeriod;
	}

	/**
	 * Sets the data rentention period.
	 *
	 * @param dataRententionPeriod
	 *            the dataRententionPeriod to set
	 */
	public void setDataRententionPeriod(String dataRententionPeriod) {
		this.dataRententionPeriod = dataRententionPeriod;
	}

	public Boolean getDependentPromoIsFreeTrail() {
		return dependentPromoIsFreeTrail;
	}

	public void setDependentPromoIsFreeTrail(Boolean dependentPromoIsFreeTrail) {
		this.dependentPromoIsFreeTrail = dependentPromoIsFreeTrail;
	}

    public String getVcDependentPromoCode() {
        return vcDependentPromoCode;
    }

    public void setVcDependentPromoCode(String vcDependentPromoCode) {
        this.vcDependentPromoCode = vcDependentPromoCode;
    }

    public Boolean getSkipAdsIndicator() {
		return skipAdsIndicator;
	}

	public void setSkipAdsIndicator(Boolean skipAdsIndicator) {
		this.skipAdsIndicator = skipAdsIndicator;
	}

	/**
	 * Gets the display type.
	 *
	 * @return the displayType
	 */
	public String getDisplayType() {
		return displayType;
	}

	/**
	 * Sets the display type.
	 *
	 * @param displayType
	 *            the displayType to set
	 */
	public void setDisplayType(String displayType) {
		this.displayType = displayType;
	}

	/**
	 * Gets the language.
	 *
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the language.
	 *
	 * @param language
	 *            the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	public Boolean getServiceFlag() {
		return serviceFlag;
	}

	public void setServiceFlag(Boolean serviceFlag) {
		this.serviceFlag = serviceFlag;
	}

	/**
	 * Gets the concurrent stream count.
	 *
	 * @return the concurrentStreamCount
	 */
	public int getConcurrentStreamCount() {
		return concurrentStreamCount;
	}

	/**
	 * Sets the concurrent stream count.
	 *
	 * @param concurrentStreamCount
	 *            the concurrentStreamCount to set
	 */
	public void setConcurrentStreamCount(int concurrentStreamCount) {
		this.concurrentStreamCount = concurrentStreamCount;
	}

	/**
	 * Gets the benefit references.
	 *
	 * @return the benefitReferences
	 */
	public List<GenericTypeIdBase> getBenefitReferences() {
		return benefitReferences;
	}

	/**
	 * Sets the benefit references.
	 *
	 * @param benefitReferences
	 *            the benefitReferences to set
	 */
	public void setBenefitReferences(List<GenericTypeIdBase> benefitReferences) {
		this.benefitReferences = benefitReferences;
	}

	public Boolean getPreOrder() {
		return preOrder;
	}

	public void setPreOrder(Boolean preOrder) {
		this.preOrder = preOrder;
	}

	public Boolean getThirdPartyOTT() {
		return thirdPartyOTT;
	}

	public void setThirdPartyOTT(Boolean thirdPartyOTT) {
		this.thirdPartyOTT = thirdPartyOTT;
	}

	/**
	 * Gets the secondary billing product id.
	 *
	 * @return the secondaryBillingProductId
	 */
	public String getSecondaryBillingProductId() {
		return secondaryBillingProductId;
	}

	/**
	 * Sets the secondary billing product id.
	 *
	 * @param secondaryBillingProductId
	 *            the secondaryBillingProductId to set
	 */
	public void setSecondaryBillingProductId(String secondaryBillingProductId) {
		this.secondaryBillingProductId = secondaryBillingProductId;
	}

	/**
	 * Gets the contract applicable.
	 *
	 * @return the contractApplicable
	 */
	public String getContractApplicable() {
		return contractApplicable;
	}

	/**
	 * Sets the contract applicable.
	 *
	 * @param contractApplicable
	 *            the new contractApplicable
	 */
	public void setContractApplicable(String contractApplicable) {
		String newContractApplicable = null;
		if (contractApplicable != null && contractApplicable.equalsIgnoreCase("non-contract")) {
			newContractApplicable = "noncontract";
		} else {
			newContractApplicable = contractApplicable;
		}
		this.contractApplicable = newContractApplicable;
	}

	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	/**
	 * Gets the max quantity.
	 *
	 * @return the maxQuantity
	 */
	public String getMaxQuantity() {
		return maxQuantity;
	}

	/**
	 * Sets the max quantity.
	 *
	 * @param maxQuantity
	 *            the maxQuantity to set
	 */
	public void setMaxQuantity(String maxQuantity) {
		this.maxQuantity = maxQuantity;
	}

	public String getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(String minQuantity) {
		this.minQuantity = minQuantity;
	}

	/**
	 * Gets the sub category.
	 *
	 * @return the subCategory
	 */
	public String getSubCategory() {
		return subCategory;
	}

	/**
	 * Sets the sub category.
	 *
	 * @param subCategory
	 *            the subCategory to set
	 */
	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	/**
	 * Gets the installment list.
	 *
	 * @return the installment list
	 */
	public List<InstallmentInfo> getInstallmentList() {
		return installmentList;
	}

	/**
	 * Sets the installment list.
	 *
	 * @param installmentList
	 *            the new installment list
	 */
	public void setInstallmentList(List<InstallmentInfo> installmentList) {
		this.installmentList = installmentList;
	}

	/**
	 * 
	 * @return
	 */
	public String getProductGroup() {
		return productGroup;
	}

	/**
	 * 
	 * @param productGroup
	 */
	public void setProductGroup(String productGroup) {
		this.productGroup = productGroup;
	}

	public String getIsFulfillmentOnlyProduct() {
		return isFulfillmentOnlyProduct;
	}

	public void setIsFulfillmentOnlyProduct(String isFulfillmentOnlyProduct) {
		this.isFulfillmentOnlyProduct = isFulfillmentOnlyProduct;
	}

	/**
	 * The Enum ContractApplicable.
	 */
	public enum ContractApplicable {

		/** The contract. */
		CONTRACT("CONTRACT"),
		/** The noncontract. */
		NONCONTRACT("NON-CONTRACT"),
		/** The all. */
		ALL("ALL"),

		/** The contract. */
		contract("contract"),
		/** The noncontract. */
		noncontract("non-contract"),
		/** The all. */
		all("all");

		/** The value. */
		private final String value;

		/** The Constant CONSTANTS. */
		private static final Map<Attributes.ContractApplicable, String> contractApplicableMap = new HashMap<Attributes.ContractApplicable, String>();

		static {

			contractApplicableMap.put(ContractApplicable.CONTRACT, "Contract");
			contractApplicableMap.put(ContractApplicable.NONCONTRACT, "Retail");
			contractApplicableMap.put(ContractApplicable.ALL, "All");
			contractApplicableMap.put(ContractApplicable.contract, "Contract");
			contractApplicableMap.put(ContractApplicable.noncontract, "Retail");
			contractApplicableMap.put(ContractApplicable.all, "All");

		}

		/**
		 * Instantiates a new contact phone type.
		 *
		 * @param value
		 *            the value
		 */
		private ContractApplicable(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return this.value;
		}

		/**
		 * Value.
		 *
		 * @return the string
		 */
		// @JsonValue
		public String value() {
			return this.value;
		}

		/**
		 * From value.
		 *
		 * @return the additional contact phone. contact phone type
		 */
		// @JsonCreator
		public String getCPCvalue() {
			String cpcValue = contractApplicableMap.get(this);
			if (cpcValue == null) {
				throw new IllegalArgumentException(cpcValue);
			} else {
				return cpcValue;
			}
		}
	}

	public List<IncludeProductWrapper> getIncludedMobilityProducts() {
		return includedMobilityProducts;
	}

	public void setIncludedMobilityProducts(List<IncludeProductWrapper> includedMobilityProducts) {
		this.includedMobilityProducts = includedMobilityProducts;
	}

	public List<EligibilityContext> getCampaignEligibility() {
		return campaignEligibility;
	}

	public void setCampaignEligibility(List<EligibilityContext> campaignEligibility) {
		this.campaignEligibility = campaignEligibility;
	}

	/**
	 * @return the compatibleEmployeeProducts
	 */
	public List<ProductWrapper> getCompatibleEmployeeProducts() {
		return compatibleEmployeeProducts;
	}

	/**
	 * @param compatibleEmployeeProducts
	 *            the compatibleEmployeeProducts to set
	 */
	public void setCompatibleEmployeeProducts(List<ProductWrapper> compatibleEmployeeProducts) {
		this.compatibleEmployeeProducts = compatibleEmployeeProducts;
	}

	public String getDeviceSubType() {
		return deviceSubType;
	}

	public void setDeviceSubType(String deviceSubType) {
		this.deviceSubType = deviceSubType;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getCatalogProductName() {
		return catalogProductName;
	}

	public void setCatalogProductName(String catalogProductName) {
		this.catalogProductName = catalogProductName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getImeiType() {
		return imeiType;
	}

	public void setImeiType(String imeiType) {
		this.imeiType = imeiType;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getAtgCommitmentTermId() {
		return atgCommitmentTermId;
	}

	public void setAtgCommitmentTermId(String atgCommitmentTermId) {
		this.atgCommitmentTermId = atgCommitmentTermId;
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getCommitmentTermType() {
		return commitmentTermType;
	}

	public void setCommitmentTermType(String commitmentTermType) {
		this.commitmentTermType = commitmentTermType;
	}

	public List<GenericNameValueBase> getFlags() {
		return flags;
	}

	public void setFlags(List<GenericNameValueBase> flags) {
		this.flags = flags;
	}

	public String getRedemptionLimit() {
		return redemptionLimit;
	}

	public void setRedemptionLimit(String redemptionLimit) {
		this.redemptionLimit = redemptionLimit;
	}

	public String getDeviceSubCategory() {
		return deviceSubCategory;
	}

	public void setDeviceSubCategory(String deviceSubCategory) {
		this.deviceSubCategory = deviceSubCategory;
	}

	public DeliveryMethod getDeliveryMethodByChannel() {
		return deliveryMethodByChannel;
	}

	public void setDeliveryMethodByChannel(DeliveryMethod deliveryMethodByChannel) {
		this.deliveryMethodByChannel = deliveryMethodByChannel;
	}

	public ActionRule getRemovalRuleByChannel() {
		return removalRuleByChannel;
	}

	public void setRemovalRuleByChannel(ActionRule removalRuleByChannel) {
		this.removalRuleByChannel = removalRuleByChannel;
	}

	public String getAtgProductID() {
		return atgProductID;
	}

	public void setAtgProductID(String atgProductID) {
		this.atgProductID = atgProductID;
	}

	public String getSkuStatus() {
		return skuStatus;
	}

	public void setSkuStatus(String skuStatus) {
		this.skuStatus = skuStatus;
	}

	public String getSaleStatus() {
		return saleStatus;
	}

	public void setSaleStatus(String saleStatus) {
		this.saleStatus = saleStatus;
	}

	public List<MinMaxQuantity> getMinMaxQuantity() {
		return minMaxQuantity;
	}

	public void setMinMaxQuantity(List<MinMaxQuantity> minMaxQuantity) {
		this.minMaxQuantity = minMaxQuantity;
	}
	
	private Boolean isAvailable;
	
	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public AutoRenewMessages getAutoRenewMessages() {
		return autoRenewMessages;
	}

	public void setAutoRenewMessages(AutoRenewMessages autoRenewMessages) {
		this.autoRenewMessages = autoRenewMessages;
	}

    public List<IncludeProductWrapper> getEquivalentContentProducts() {
        return equivalentContentProducts;
    }

    public void setEquivalentContentProducts(List<IncludeProductWrapper> equivalentContentProducts) {
        this.equivalentContentProducts = equivalentContentProducts;
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Attributes [displayName=");
		builder.append(displayName);
		builder.append(", displayNamesByKey=");
		builder.append(displayNamesByKey);
		builder.append(", descriptionsByKey=");
		builder.append(descriptionsByKey);
		builder.append(", rewardValue=");
		builder.append(rewardValue);
		builder.append(", billingProductCode=");
		builder.append(billingProductCode);
		builder.append(", billingCode=");
		builder.append(billingCode);
		builder.append(", billingProductId=");
		builder.append(billingProductId);
		builder.append(", startDate=");
		builder.append(startDate);
		builder.append(", endDate=");
		builder.append(endDate);
		builder.append(", businessSegment=");
		builder.append(businessSegment);
		builder.append(", provisioningCode=");
		builder.append(provisioningCode);
		builder.append(", congratsMessage=");
		builder.append(congratsMessage);
		builder.append(", opusDisclosureMessage=");
		builder.append(opusDisclosureMessage);
		builder.append(", myAttDisclosureMessage=");
		builder.append(myAttDisclosureMessage);
		builder.append(", salesChannel=");
		builder.append(salesChannel);
		builder.append(", isAvailable=");
		builder.append(isAvailable);
		builder.append(", ranking=");
		builder.append(ranking);
		builder.append(", attProject=");
		builder.append(attProject);
		builder.append(", productFamily=");
		builder.append(productFamily);
		builder.append(", packageGroup=");
		builder.append(packageGroup);
		builder.append(", fufillmentAttribute1=");
		builder.append(fufillmentAttribute1);
		builder.append(", fufillmentAttribute2=");
		builder.append(fufillmentAttribute2);
		builder.append(", removalRule=");
		builder.append(removalRule);
		builder.append(", serviceType=");
		builder.append(serviceType);
		builder.append(", preReqProducts=");
		builder.append(preReqProducts);
		builder.append(", upgradablePlans=");
		builder.append(upgradablePlans);
		builder.append(", downgradablePlans=");
		builder.append(downgradablePlans);
		builder.append(", isRSNCapable=");
		builder.append(isRSNCapable);
		builder.append(", marketingDescLink=");
		builder.append(marketingDescLink);
		builder.append(", numberOfChannels=");
		builder.append(numberOfChannels);
		builder.append(", isHDAccessRequired=");
		builder.append(isHDAccessRequired);		
		builder.append(", englishChannelList=");
		builder.append(englishChannelList);
		builder.append(", spanishChannelList=");
		builder.append(spanishChannelList);
		builder.append(", featuredChannels=");
		builder.append(featuredChannels);
		builder.append(", addOnType=");
		builder.append(addOnType);
		builder.append(", planSubType=");
		builder.append(planSubType);
		builder.append(", capacityLimit=");
		builder.append(capacityLimit);
		builder.append(", dataRententionPeriod=");
		builder.append(dataRententionPeriod);
		builder.append(", videoBasePackageRequired=");
		builder.append(videoBasePackageRequired);
		builder.append(", skipAdsIndicator=");
		builder.append(skipAdsIndicator);
		builder.append(", displayType=");
		builder.append(displayType);
		builder.append(", language=");
		builder.append(language);
		builder.append(", serviceFlag=");
		builder.append(serviceFlag);
		builder.append(", concurrentStreamCount=");
		builder.append(concurrentStreamCount);
		builder.append(", benefitReferences=");
		builder.append(benefitReferences);
		builder.append(", preOrder=");
		builder.append(preOrder);
		builder.append(", thirdPartyOTT=");
		builder.append(thirdPartyOTT);
		builder.append(", secondaryBillingProductId=");
		builder.append(secondaryBillingProductId);
		builder.append(", dependentPromoCode=");
		builder.append(dependentPromoCode);
		builder.append(", dependentBillerPromoId=");
		builder.append(dependentBillerPromoId);
		builder.append(", dependentPromoStartDate=");
		builder.append(dependentPromoStartDate);
		builder.append(", dependentPromoName=");
		builder.append(dependentPromoName);
		builder.append(", dependentPromoDescription=");
		builder.append(dependentPromoDescription);
		builder.append(", dependentPromoDisplayName=");
		builder.append(dependentPromoDisplayName);
		builder.append(", dependentPromoIsFreeTrail=");
		builder.append(dependentPromoIsFreeTrail);
		builder.append(", productStatus=");
		builder.append(productStatus);
		builder.append(", discountSku=");
		builder.append(discountSku);
		builder.append(", numberOfSpanishChannels=");
		builder.append(numberOfSpanishChannels);
		builder.append(", equipmentSku=");
		builder.append(equipmentSku);
		builder.append(", compatibleProducts=");
		builder.append(compatibleProducts);
		builder.append(", accessoryCategory=");
		builder.append(accessoryCategory);
		builder.append(", videoDeviceLimit=");
		builder.append(videoDeviceLimit);
		builder.append(", videoDevicePolicy=");
		builder.append(videoDevicePolicy);
		builder.append(", downgradeableRule=");
		builder.append(downgradeableRule);
		builder.append(", restrictionRule=");
		builder.append(restrictionRule);
		builder.append(", callToOrderRule=");
		builder.append(callToOrderRule);
		builder.append(", upgradeableRule=");
		builder.append(upgradeableRule);
		builder.append(", compatibleVideoDeviceModels=");
		builder.append(compatibleVideoDeviceModels);
		builder.append(", installment=");
		builder.append(installment);
		builder.append(", compatibleMobilityProducts=");
		builder.append(compatibleMobilityProducts);
		builder.append(", includedProducts=");
		builder.append(includedProducts);
		builder.append(", includedMobilityProducts=");
		builder.append(includedMobilityProducts);
		builder.append(", conflictPromoList=");
		builder.append(conflictPromoList);
		builder.append(", contractApplicable=");
		builder.append(contractApplicable);
		builder.append(", contractIndicator=");
		builder.append(contractIndicator);
		builder.append(", maxQuantity=");
		builder.append(maxQuantity);
		builder.append(", minQuantity=");
		builder.append(minQuantity);
		builder.append(", minMaxQuantity=");
		builder.append(minMaxQuantity);
		builder.append(", installmentList=");
		builder.append(installmentList);
		builder.append(", subCategory=");
		builder.append(subCategory);
		builder.append(", customerSegments=");
		builder.append(customerSegments);
		builder.append(", isThirdParty=");
		builder.append(isThirdParty);
/*		builder.append(", isInternationalVideoSubscriptionRequired=");
		builder.append(isInternationalVideoSubscriptionRequired);*/		
		builder.append(", isInternationalAddonRequired=");
		builder.append(isInternationalAddonRequired);
		builder.append(", hasLocalChannels=");
		builder.append(hasLocalChannels);
		builder.append(", isPickService=");
		builder.append(isPickService);
		builder.append(", isRequiredAddonForInternationalVideoPlan=");
		builder.append(isRequiredAddonForInternationalVideoPlan);
		builder.append(", isAutoRenewable=");
		builder.append(isAutoRenewable);
		builder.append(", selfInstallAvailability=");
		builder.append(selfInstallAvailability);
		builder.append(", isAsurionPlan=");
		builder.append(isAsurionPlan);
		builder.append(", termsAndConditions=");
		builder.append(termsAndConditions);
		builder.append(", specifications=");
		builder.append(specifications);
		builder.append(", feeType=");
		builder.append(feeType);
		builder.append(", deliverymethod=");
		builder.append(deliverymethod);
		builder.append(", accessoryType=");
		builder.append(accessoryType);
		builder.append(", deviceType=");
		builder.append(deviceType);
		builder.append(", deviceCategory=");
		builder.append(deviceCategory);
		builder.append(", addonType=");
		builder.append(addonType);
		builder.append(", planType=");
		builder.append(planType);
		builder.append(", isWirelessCapable=");
		builder.append(isWirelessCapable);
		builder.append(", purchaseType=");
		builder.append(purchaseType);
		builder.append(", maxNumberOfInstallments=");
		builder.append(maxNumberOfInstallments);
		builder.append(", maxNumberOfPreSeasonInstallments=");
		builder.append(maxNumberOfPreSeasonInstallments);
		builder.append(", maxNumberOfFullSeasonInstallments=");
		builder.append(maxNumberOfFullSeasonInstallments);
		builder.append(", maxNumberOfMidSeasonInstallments=");
		builder.append(maxNumberOfMidSeasonInstallments);
		builder.append(", maxNumberOfPostSeasonInstallments=");
		builder.append(maxNumberOfPostSeasonInstallments);
		builder.append(", seasonStartDate=");
		builder.append(seasonStartDate);
		builder.append(", seasonEndDate=");
		builder.append(seasonEndDate);
		builder.append(", valueRanking=");
		builder.append(valueRanking);
		builder.append(", complianceRanking=");
		builder.append(complianceRanking);
		builder.append(", channelLineup=");
		builder.append(channelLineup);
		builder.append(", maxOrderLimit=");
		builder.append(maxOrderLimit);
		builder.append(", maxAccountLimit=");
		builder.append(maxAccountLimit);
		builder.append(", modelNumbers=");
		builder.append(modelNumbers);
		builder.append(", deliveryMethod=");
		builder.append(deliveryMethod);
		builder.append(", productGroup=");
		builder.append(productGroup);
		builder.append(", isFulfillmentOnlyProduct=");
		builder.append(isFulfillmentOnlyProduct);
		builder.append(", campaignId=");
		builder.append(campaignId);
		builder.append(", campaignCode=");
		builder.append(campaignCode);
		builder.append(", campaignName=");
		builder.append(campaignName);
		builder.append(", campaignDesc=");
		builder.append(campaignDesc);
		builder.append(", couponType=");
		builder.append(couponType);
		builder.append(", campaignCategory=");
		builder.append(campaignCategory);
		builder.append(", creationDate=");
		builder.append(creationDate);
		builder.append(", campaignStartDate=");
		builder.append(campaignStartDate);
		builder.append(", campaignEndDate=");
		builder.append(campaignEndDate);
		builder.append(", autoRegister=");
		builder.append(autoRegister);
		builder.append(", studioSponsorByATT=");
		builder.append(studioSponsorByATT);
		builder.append(", lockDuration=");
		builder.append(lockDuration);
		builder.append(", numberOfCoupons=");
		builder.append(numberOfCoupons);
		builder.append(", campaignMessaging=");
		builder.append(campaignMessaging);
		builder.append(", campaignEligibility=");
		builder.append(campaignEligibility);
		builder.append(", deviceSubType=");
		builder.append(deviceSubType);
		builder.append(", deviceSubCategory=");
		builder.append(deviceSubCategory);
		builder.append(", manufacturer=");
		builder.append(manufacturer);
		builder.append(", model=");
		builder.append(model);
		builder.append(", imeiType=");
		builder.append(imeiType);
		builder.append(", sku=");
		builder.append(sku);
		builder.append(", atgCommitmentTermId=");
		builder.append(atgCommitmentTermId);
		builder.append(", termCode=");
		builder.append(termCode);
		builder.append(", commitmentTermType=");
		builder.append(commitmentTermType);
		builder.append(", flags=");
		builder.append(flags);
		builder.append(", redemptionLimit=");
		builder.append(redemptionLimit);
		builder.append(", deliveryMethodByChannel=");
		builder.append(deliveryMethodByChannel);
		builder.append(", removalRuleByChannel=");
		builder.append(removalRuleByChannel);
		builder.append(", conflictingProducts=");
		builder.append(conflictingProducts);
		builder.append(", compatibleEmployeeProducts=");
		builder.append(compatibleEmployeeProducts);
		builder.append(", atgProductID=");
		builder.append(atgProductID);
		builder.append(", maxNoOfLines=");
		builder.append(maxNoOfLines);
		builder.append(", skuStatus=");
		builder.append(skuStatus);
		builder.append(", saleStatus=");
		builder.append(saleStatus);
		builder.append(", includedEmployeeProducts=");
		builder.append(includedEmployeeProducts);
		builder.append(", doNotAutoRenewBillingProductCode=");
		builder.append(doNotAutoRenewBillingProductCode);
		builder.append(", doNotAutoRenewBillingReferenceId=");
		builder.append(doNotAutoRenewBillingReferenceId);
		builder.append(", nonRefundDate=");
		builder.append(nonRefundDate);
		builder.append(", deviceState=");
		builder.append(deviceState);
		builder.append(", warrantyDuration=");
		builder.append(warrantyDuration);
		builder.append(", constraints=");
		builder.append(constraints);
		builder.append(", key=");
		builder.append(key);
		builder.append(", name=");
		builder.append(name);
		builder.append(", isChangeable=");
		builder.append(isChangeable);
		builder.append(", omsProductCode=");
		builder.append(omsProductCode);
		builder.append(", orderingMethod=");
		builder.append(orderingMethod);
		builder.append(", enablerPricePlanCode=");
		builder.append(enablerPricePlanCode);
		builder.append(", tokens=");
		builder.append(tokens);
		builder.append(", chargeCode=");
		builder.append(chargeCode);
		builder.append("isWorldDirect=");
		builder.append(isWorldDirect);
		builder.append(", maxSTBsWired=");
		builder.append(maxSTBsWired);
		builder.append(", maxDVRs=");
		builder.append(maxDVRs);
		builder.append(", maxSTBsWIFI=");
		builder.append(maxSTBsWIFI);
		builder.append("downgradeableRule=");
		builder.append(downgradeableRule);
		builder.append(", isETFRequired=");
		builder.append(isETFRequired);
		builder.append(", isRemovable=");
		builder.append(isRemovable);
		builder.append("heroOfferRanking=");
		builder.append(heroOfferRanking);
		builder.append(", billingParams=");
		builder.append(billingParams);
		builder.append(", remoteReplacementType=");
		builder.append(remoteReplacementType);
		builder.append(", productKey=");
		builder.append(productKey);
		builder.append(", orderValidFromDate=");
		builder.append(orderValidFromDate);
		builder.append(", orderValidToDate=");
		builder.append(orderValidToDate);
		builder.append(", receiverLineType=");
		builder.append(receiverLineType);
		builder.append(", autoRenewMessages=");
		builder.append(autoRenewMessages);
		builder.append(", enablerATGProductId=");
		builder.append(enablerATGProductId);
		builder.append(", enablerATGSkuId=");
		builder.append(enablerATGSkuId);
		builder.append(", regionName=");
		builder.append(regionName);
		builder.append(", regionDesc=");
		builder.append(regionDesc);
		builder.append(", segmentKey=");
		builder.append(segmentKey);
		builder.append(", segmentStatus=");
		builder.append(segmentStatus);
		builder.append(", segmentType=");
		builder.append(segmentType);
		builder.append(", segmentTypeCode=");
		builder.append(segmentTypeCode);
		builder.append(", segmentTypeDesc=");
		builder.append(segmentTypeDesc);
		builder.append(", segmentLevel=");
		builder.append(segmentLevel);
		builder.append(", eligibleAccountTypeList=");
		builder.append(eligibleAccountTypeList);
		builder.append(", eligibleAccountStatus=");
		builder.append(eligibleAccountStatus);
		builder.append(", eligibleDMA=");
		builder.append(eligibleDMA);
		builder.append(", eligibleZip=");
		builder.append(eligibleZip);
		builder.append(", customerRisk=");
		builder.append(customerRisk);
		builder.append(", salesSystem=");
		builder.append(salesSystem);
		builder.append(", equipmentOption=");
		builder.append(equipmentOption);
		builder.append(", marketingSourceCode=");
		builder.append(marketingSourceCode);
		builder.append(", dealerId=");
		builder.append(dealerId);
		builder.append(", segmentOffers=");
		builder.append(segmentOffers);
		builder.append(", accessibility=");
		builder.append(accessibility);
		builder.append(", groupCategory=");
		builder.append(groupCategory);
		builder.append(", deviceDisplayCategory=");
		builder.append(deviceDisplayCategory);
		builder.append(", compatibleDevices=");
		builder.append(compatibleDevices);
		builder.append(", isVisible=");
		builder.append(isVisible);
		builder.append("]");
		return builder.toString();
	}

	public String getTradeInEligibiltyReason() {
		return tradeInEligibiltyReason;
	}

	public void setTradeInEligibiltyReason(String tradeInEligibiltyReason) {
		this.tradeInEligibiltyReason = tradeInEligibiltyReason;
	}

	public String getHylaCode() {
		return hylaCode;
	}

	public void setHylaCode(String hylaCode) {
		this.hylaCode = hylaCode;
	}

	public List<String> getDeviceEdModels() {
		return deviceEdModels;
	}

	public void setDeviceEdModels(List<String> deviceEdModels) {
		this.deviceEdModels = deviceEdModels;
	}
	
	public String getDoNotAutoRenewBillingProductCode() {
		return doNotAutoRenewBillingProductCode;
	}

	public void setDoNotAutoRenewBillingProductCode(String doNotAutoRenewBillingProductCode) {
		this.doNotAutoRenewBillingProductCode = doNotAutoRenewBillingProductCode;
	}

	public String getDoNotAutoRenewBillingReferenceId() {
		return doNotAutoRenewBillingReferenceId;
	}

	public void setDoNotAutoRenewBillingReferenceId(String doNotAutoRenewBillingReferenceId) {
		this.doNotAutoRenewBillingReferenceId = doNotAutoRenewBillingReferenceId;
	}

	public String getNonRefundDate() {
		return nonRefundDate;
	}

	public void setNonRefundDate(String nonRefundDate) {
		this.nonRefundDate = nonRefundDate;
	}

	private List<SubCategoryByKey> subCategoryByKey;

	public List<SubCategoryByKey> getSubCategoryByKey() {
		return subCategoryByKey;
	}

	public void setSubCategoryByKey(List<SubCategoryByKey> subCategoryByKey) {
		this.subCategoryByKey = subCategoryByKey;
	}
	
	private List<DisplayTypeByKey> displayTypeByKey;
	
	public List<DisplayTypeByKey> getDisplayTypeByKey() {
		return displayTypeByKey;
	}

	public void setDisplayTypeByKey(List<DisplayTypeByKey> displayTypeByKey) {
		this.displayTypeByKey = displayTypeByKey;
	}
	
	private String bpType;
	 
	public String getBpType() {
		return bpType;
	}
 
	public void setBpType(String bpType) {
		this.bpType = bpType;
	}
	@JsonProperty("UIAttributes")
	public UIAttributes getUIAttributes() {
		return UIAttributes;
	}

	public void setUIAttributes(UIAttributes UIAttributes) {
		this.UIAttributes = UIAttributes;
	}

	public List<List<MessageEntry>> getGlobalMessagesByKey() {
		return globalMessagesByKey;
	}

	public void setGlobalMessagesByKey(List<List<MessageEntry>> globalMessagesByKey) {
		this.globalMessagesByKey = globalMessagesByKey;
	}

	public DelayProvisioningMessagesByKey getDelayProvisioningMessagesByKey() {
		return delayProvisioningMessagesByKey;
	}

	public void setDelayProvisioningMessagesByKey(DelayProvisioningMessagesByKey delayProvisioningMessagesByKey) {
		this.delayProvisioningMessagesByKey = delayProvisioningMessagesByKey;
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

	public String getDelayProvisioningReason() {
		return delayProvisioningReason;
	}

	public void setDelayProvisioningReason(String delayProvisioningReason) {
		this.delayProvisioningReason = delayProvisioningReason;
	}
	private List<IncludeProductWrapper> conflictingProductsReference;

	public List<IncludeProductWrapper> getConflictingProductsReference() {
		return conflictingProductsReference;
	}

	public void setConflictingProductsReference(List<IncludeProductWrapper> conflictingProductsReference) {
		this.conflictingProductsReference = conflictingProductsReference;
	}
}