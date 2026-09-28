package com.dtv.dcp.epoch.model.ct.benefit;

import com.dtv.dcp.epoch.model.ct.generic.GenericLocaleBase;
import com.dtv.dcp.epoch.model.ct.product.Product;
import com.dtv.dcp.epoch.model.ct.product.ProductWrapper;
import com.dtv.dcp.epoch.model.ct.product.Value;
import com.dtv.dcp.epoch.model.ct.response.ParentOffers;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Benefit implements Serializable {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The benefitCategories.
	 */
	private String benefitCategory;

	/**
	 * The billingBenefitId.
	 */
	private String billingBenefitId;

	/**
	 * CT Benefit Id.
	 */
	private String id;

	private List<ParentOffers> parentOffers;

	private String code;

	/**
	 * The billingBenefitCode.
	 */
	private String billingBenefitCode;

	/**
	 * The benefitName.
	 */
	private GenericLocaleBase benefitName;

	/**
	 * The benefitDisplayName.
	 */
	private GenericLocaleBase benefitDisplayName;

	/**
	 * The benefitRank.
	 */
	private int benefitRank;

	/**
	 * The description.
	 */
	private GenericLocaleBase description;

	/**
	 * The opusDisclosureMessage.
	 */
	private String opusDisclosureMessage;

	/**
	 * The myAttDisclosureMessage.
	 */
	private String myAttDisclosureMessage;

	/**
	 * The opusDisclosureForServices.
	 */
	private String opusDisclosureForServices;

	/**
	 * The myATTDisclosureForServices.
	 */
	private String myATTDisclosureForServices;

	private String disclosure;
	private String shortDisclosure;
	public String getShortDisclosure() {
		return shortDisclosure;
	}
	public void setShortDisclosure(String shortDisclosure) {
		this.shortDisclosure = shortDisclosure;
	}

	private BenefitDisplayNamesByKey displayNamesByKey;

	private BenefitDescriptionByKey descriptionsByKey;

	private Integer maxOccurrence_demo;

	private Integer maxOccurrence_deca;
	
	private Integer maxOccurrence_showroom;

	private Integer maxOccurrence_bcomp;
	
	private Integer maxOccurrence_courtesy;

	private BenefitDisclosureByKey disclosureMessagesByKey;
	
	private Compliance compliance;

	/**
	 * @return the compliance
	 */
	public Compliance getCompliance() {
		return compliance;
	}

	/**
	 * @param compliance the compliance to set
	 */
	public void setCompliance(Compliance compliance) {
		this.compliance = compliance;
	}

	public BenefitDisclosureByKey getDisclosureMessagesByKey() {
		return disclosureMessagesByKey;
	}

	public void setDisclosureMessagesByKey(BenefitDisclosureByKey disclosureMessagesByKey) {
		this.disclosureMessagesByKey = disclosureMessagesByKey;
	}

	public String getDisclosure() {
		return disclosure;
	}

	public void setDisclosure(String disclosure) {
		this.disclosure = disclosure;
	}

	public void setDisclosureMessagesByKey() {
		if (opusDisclosureForServices != null || myATTDisclosureForServices != null || myAttDisclosureMessage != null
				|| opusDisclosureMessage != null || disclosure != null) {
			BenefitDisclosureByKey disclosureMessagesByKey = null;
			if (getDisclosureMessagesByKey() != null) {
				disclosureMessagesByKey = getDisclosureMessagesByKey();
			} else {
				disclosureMessagesByKey = new BenefitDisclosureByKey();
			}
			disclosureMessagesByKey.setOpusDisclosureForServices(opusDisclosureForServices);
			disclosureMessagesByKey.setMyATTDisclosureForServices(myATTDisclosureForServices);
			disclosureMessagesByKey.setMyAttDisclosureMessage(myAttDisclosureMessage);
			disclosureMessagesByKey.setOpusDisclosureMessage(opusDisclosureMessage);
			this.disclosureMessagesByKey = disclosureMessagesByKey;
		}
	}

	public String getOpusDisclosureForServices() {
		return opusDisclosureForServices;
	}

	public String getMyATTDisclosureForServices() {
		return myATTDisclosureForServices;
	}

	public void setOpusDisclosureForServices(String opusDisclosureForServices) {
		this.opusDisclosureForServices = opusDisclosureForServices;

	}

	public void setMyATTDisclosureForServices(String myATTDisclosureForServices) {
		this.myATTDisclosureForServices = myATTDisclosureForServices;
	}

	/**
	 * The startDate.
	 */
	private String startDate;

	/**
	 * The retIoApplicableProducts.
	 */
	private boolean retIoApplicableProducts;

	/**
	 * The endDate.
	 */
	private String endDate;

	/**
	 * The contractIndicator.
	 */
	private String contractIndicator;

	/**
	 * The contractVersion.
	 */
	private String contractVersion;

	/**
	 * The selfBenefit.
	 */
	private boolean selfBenefit;

	/**
	 * The duration.
	 */
	private int duration;

	/**
	 * The period.
	 */
	private String period;

	/**
	 * The duration.
	 */
	private boolean billAfterPeriod;

	/**
	 * The applicableProducts.
	 */
	private List<ProductWrapper> applicableProducts;

	/**
	 * The maxOccurrence.
	 */
	private int maxOccurrence;

	/**
	 * The benefitType.
	 */
	private String benefitType;

	/**
	 * The value.
	 */
	private Value value;

	/**
	 * The customerClassification.
	 */
	private String customerClassification;

	/**
	 * The benefitTypeOrder.
	 */
	private String benefitTypeOrder;

	/**
	 * The secondaryBenefitCode.
	 */
	private String secondaryBenefitCode;

	/**
	 * The freeTrialPromo.
	 */
	private boolean freeTrialPromo;

	/**
	 * The isDependentPromo.
	 */
	@JsonProperty("isDependentPromo")
	private boolean dependentPromo;

	/**
	 * The ioOffer.
	 */
	private boolean ioOffer;

	/**
	 * The freeTrialDuration.
	 */
	private int freeTrialDuration;

	/**
	 * The agentOffer.
	 */
	private boolean agentOffer;

	/**
	 * The descriptionforServices.
	 */
	private String descriptionforServices;

	/**
	 * The contractType.
	 */
	private String contractType;

	/**
	 * The promoSourceSku.
	 */
	private String promoSourceSku;

	/**
	 * The isMigrated.
	 */
	@JsonProperty(value = "isMigrated")
	private boolean isMigrated;

	/**
	 * The contractTerm.
	 */
	private String contractTerm;

	/**
	 * The minQuantity.
	 */
	private int minQuantity;

	/**
	 * The extPromoType.
	 */
	private String extPromoType;

	/**
	 * The promoPriority.
	 */
	private int promoPriority;

	/**
	 * The segmentDescription.
	 */
	private String segmentDescription;

	/**
	 * The salesChannel.
	 */
	private String salesChannel;

	/**
	 * The salesChannel.
	 */
	private String benefitLevel;

	@JsonProperty(value = "isQuotaBased")
	private boolean isQuotaBased;

	@JsonProperty(value = "callToAction")
	private String callToAction;
	
	private Integer availableQuota;

	/**
	 * The customerSegments.
	 */
	private List<String> customerSegments;

	/**
	 * The longDescription.
	 */
	private String longDescription;

	/**
	 * The longDescriptionforServices.
	 */
	private String longDescriptionforServices;

	/**
	 * The shortDescriptionforServices.
	 */
	private String shortDescriptionforServices;

	/**
	 * The shortDescriptionforOpus.
	 */
	private String shortDescriptionforOpus;

	/**
	 * The shortDescription.
	 */
	private String shortDescription;

	/**
	 * The displayNameforServices.
	 */
	private String displayNameforServices;

	/**
	 * The promoSourceSkus.
	 */
	private List<ProductWrapper> promoSourceSkus;

	/**
	 * The notApplibaleForOffer.
	 */
	private boolean notApplibaleForOffer;

	/**
	 * The benefitType.
	 */
	private String originalBenefitType;

	/**
	 * The validityEndDate.
	 */
	private String validityEndDate;

	/**
	 * The restrictedPlans.
	 */
	private List<Product> restrictedPlans;

	private String atgPromoReferenceId;

	private String benefitChargeType;

	/**
	 * The enableServiceDisconnect.
	 */
	private boolean enableServiceDisconnect;

	/**
	 * The enableServiceDisconnect.
	 */
	private boolean isCouponPromo;

	private boolean rtpIndicator;

	/** The period. */

	private boolean portin;

	@JsonProperty(value = "isActive")
	private boolean isActive;

	private String extBenefitCode;

	private List<String> stackableDiscounts;

	private List<String> swimlaneContractIndicators;

	private String benefitSaleType;

	private List<String> segment;

	private List<String> businessSegment;

	private List<String> salesChannels;

	private List<String> benefitActionType;

	private String gfVersion;

	private String enablerPromotionCode;

	private String locationType;

	private String location;

	private List<String> benefitClassificationType;

	private String rebateID;

	private String billingReasonCode;

	private String omsProductId;

	private Integer numberOfCredits;

	private String recurringBillingReasonCode;

	private String agreementID;

	private String agreementPriceCode;

	private List<ProductWrapper> secondaryApplicableProducts;

	private Boolean strikeThroughOffer;
	
	private List<String> enrollmentType;
	
	private String transactionType;
	
	private Integer rebatePriority;
	
	private String billingSystem;
	
	private List<String> dealerCode;
	
	private List<String> eligibleIAPPartners;
	
	public String getBillingSystem() {
		return billingSystem;
	}

	public void setBillingSystem(String billingSystem) {
		this.billingSystem = billingSystem;
	}
	
	public List<String> getDealerCode() {
		return dealerCode;
	}

	public void setDealerCode(List<String> dealerCode) {
		this.dealerCode = dealerCode;
	}

	/**
	 * @return the rebatePriority
	 */
	public Integer getRebatePriority() {
		return rebatePriority;
	}

	/**
	 * @param rebatePriority the rebatePriority to set
	 */
	public void setRebatePriority(Integer rebatePriority) {
		this.rebatePriority = rebatePriority;
	}
	
	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public List<String> getEnrollmentType() {
		return enrollmentType;
	}

	public void setEnrollmentType(List<String> enrollmentType) {
		this.enrollmentType = enrollmentType;
	}

	public Boolean getStrikeThroughOffer() {
		return strikeThroughOffer;
	}

	public void setStrikeThroughOffer(Boolean strikeThroughOffer) {
		this.strikeThroughOffer = strikeThroughOffer;
	}

	public String getAgreementID() {
		return agreementID;
	}

	public void setAgreementID(String agreementID) {
		this.agreementID = agreementID;
	}

	public String getAgreementPriceCode() {
		return agreementPriceCode;
	}

	public void setAgreementPriceCode(String agreementPriceCode) {
		this.agreementPriceCode = agreementPriceCode;
	}

	public List<ProductWrapper> getSecondaryApplicableProducts() {
		return secondaryApplicableProducts;
	}

	public void setSecondaryApplicableProducts(List<ProductWrapper> secondaryApplicableProducts) {
		this.secondaryApplicableProducts = secondaryApplicableProducts;
	}

	public String getRecurringBillingReasonCode() {
		return recurringBillingReasonCode;
	}

	public void setRecurringBillingReasonCode(String recurringBillingReasonCode) {
		this.recurringBillingReasonCode = recurringBillingReasonCode;
	}

	public Integer getNumberOfCredits() {
		return numberOfCredits;
	}

	public void setNumberOfCredits(Integer numberOfCredits) {
		this.numberOfCredits = numberOfCredits;
	}

	public String getOmsProductId() {
		return omsProductId;
	}

	public void setOmsProductId(String omsProductId) {
		this.omsProductId = omsProductId;
	}

	public String getBillingReasonCode() {
		return billingReasonCode;
	}

	public void setBillingReasonCode(String billingReasonCode) {
		this.billingReasonCode = billingReasonCode;
	}

	public String getRebateID() {
		return rebateID;
	}

	public void setRebateID(String rebateID) {
		this.rebateID = rebateID;
	}

	public String getBenefitSaleType() {
		return benefitSaleType;
	}

	public void setBenefitSaleType(String benefitSaleType) {
		this.benefitSaleType = benefitSaleType;
	}

	public List<String> getSegment() {
		return segment;
	}

	public void setSegment(List<String> segment) {
		this.segment = segment;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public List<String> getSalesChannels() {
		return salesChannels;
	}

	public void setSalesChannels(List<String> salesChannels) {
		this.salesChannels = salesChannels;
	}

	public List<String> getBenefitActionType() {
		return benefitActionType;
	}

	public void setBenefitActionType(List<String> benefitActionType) {
		this.benefitActionType = benefitActionType;
	}

	public String getGfVersion() {
		return gfVersion;
	}

	public void setGfVersion(String gfVersion) {
		this.gfVersion = gfVersion;
	}

	public String getEnablerPromotionCode() {
		return enablerPromotionCode;
	}

	public void setEnablerPromotionCode(String enablerPromotionCode) {
		this.enablerPromotionCode = enablerPromotionCode;
	}

	public String getLocationType() {
		return locationType;
	}

	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the swimlaneContractIndicators
	 */
	public List<String> getSwimlaneContractIndicators() {
		return swimlaneContractIndicators;
	}

	/**
	 * @param swimlaneContractIndicators the swimlaneContractIndicators to set
	 */
	public void setSwimlaneContractIndicators(List<String> swimlaneContractIndicators) {
		this.swimlaneContractIndicators = swimlaneContractIndicators;
	}

	/**
	 * @return the portin
	 */
	public boolean getPortin() {
		return portin;
	}

	/**
	 * @param portin the portin to set
	 */
	public void setPortin(boolean portin) {
		this.portin = portin;
	}

	public Integer getMaxOccurrence_demo() {
		return maxOccurrence_demo;
	}

	public void setMaxOccurrence_demo(Integer maxOccurrence_demo) {
		this.maxOccurrence_demo = maxOccurrence_demo;
	}

	public Integer getMaxOccurrence_deca() {
		return maxOccurrence_deca;
	}

	public void setMaxOccurrence_deca(Integer maxOccurrence_deca) {
		this.maxOccurrence_deca = maxOccurrence_deca;
	}

	public Integer getMaxOccurrence_bcomp() {
		return maxOccurrence_bcomp;
	}

	public void setMaxOccurrence_bcomp(Integer maxOccurrence_bcomp) {
		this.maxOccurrence_bcomp = maxOccurrence_bcomp;
	}

	public Integer getMaxOccurrence_courtesy() {
		return maxOccurrence_courtesy;
	}

	public void setMaxOccurrence_courtesy(Integer maxOccurrence_courtesy) {
		this.maxOccurrence_courtesy = maxOccurrence_courtesy;
	}
	
	public Integer getMaxOccurrence_showroom() {
		return maxOccurrence_showroom;
	}

	public void setMaxOccurrence_showroom(Integer maxOccurrence_showroom) {
		this.maxOccurrence_showroom = maxOccurrence_showroom;
	}

	public String getBenefitCategory() {
		return benefitCategory;
	}

	public void setBenefitCategory(String benefitCategory) {
		this.benefitCategory = benefitCategory;
	}

	public String getBillingBenefitId() {
		return billingBenefitId;
	}

	public void setBillingBenefitId(String billingBenefitId) {
		this.billingBenefitId = billingBenefitId;
	}

	/**
	 * @return the rtpIndicator
	 */
	public boolean isRtpIndicator() {
		return rtpIndicator;
	}

	/**
	 * @param rtpIndicator the rtpIndicator to set
	 */
	public void setRtpIndicator(boolean rtpIndicator) {
		this.rtpIndicator = rtpIndicator;
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

	public String getBillingBenefitCode() {
		return billingBenefitCode;
	}

	public void setBillingBenefitCode(String billingBenefitCode) {
		this.billingBenefitCode = billingBenefitCode;
	}

	public GenericLocaleBase getBenefitName() {
		return benefitName;
	}

	public void setBenefitName(GenericLocaleBase benefitName) {
		this.benefitName = benefitName;
	}

	public GenericLocaleBase getBenefitDisplayName() {
		return benefitDisplayName;
	}

	public void setBenefitDisplayName(GenericLocaleBase benefitDisplayName) {
		this.benefitDisplayName = benefitDisplayName;
	}

	public int getBenefitRank() {
		return benefitRank;
	}

	public void setBenefitRank(int benefitRank) {
		this.benefitRank = benefitRank;
	}

	public GenericLocaleBase getDescription() {
		return description;
	}

	public void setDescription(GenericLocaleBase description) {
		this.description = description;
	}

	public String getOpusDisclosureMessage() {
		return opusDisclosureMessage;
	}

	public void setOpusDisclosureMessage(String opusDisclosureMessage) {
		this.opusDisclosureMessage = opusDisclosureMessage;
	}

	public String getMyAttDisclosureMessage() {
		return myAttDisclosureMessage;
	}

	public void setMyAttDisclosureMessage(String myAttDisclosureMessage) {
		this.myAttDisclosureMessage = myAttDisclosureMessage;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public boolean isRetIoApplicableProducts() {
		return retIoApplicableProducts;
	}

	public void setRetIoApplicableProducts(boolean retIoApplicableProducts) {
		this.retIoApplicableProducts = retIoApplicableProducts;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(String contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	public String getContractVersion() {
		return contractVersion;
	}

	public void setContractVersion(String contractVersion) {
		this.contractVersion = contractVersion;
	}

	public boolean isSelfBenefit() {
		return selfBenefit;
	}

	public void setSelfBenefit(boolean selfBenefit) {
		this.selfBenefit = selfBenefit;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public boolean isBillAfterPeriod() {
		return billAfterPeriod;
	}

	public void setBillAfterPeriod(boolean billAfterPeriod) {
		this.billAfterPeriod = billAfterPeriod;
	}

	public List<ProductWrapper> getApplicableProducts() {
		return applicableProducts;
	}

	public void setApplicableProducts(List<ProductWrapper> applicableProducts) {
		this.applicableProducts = applicableProducts;
	}

	public int getMaxOccurrence() {
		return maxOccurrence;
	}

	public void setMaxOccurrence(int maxOccurrence) {
		this.maxOccurrence = maxOccurrence;
	}

	public String getBenefitType() {
		return benefitType;
	}

	public void setBenefitType(String benefitType) {
		this.benefitType = benefitType;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public String getCustomerClassification() {
		return customerClassification;
	}

	public void setCustomerClassification(String customerClassification) {
		this.customerClassification = customerClassification;
	}

	public String getBenefitTypeOrder() {
		return benefitTypeOrder;
	}

	public void setBenefitTypeOrder(String benefitTypeOrder) {
		this.benefitTypeOrder = benefitTypeOrder;
	}

	public String getSecondaryBenefitCode() {
		return secondaryBenefitCode;
	}

	public void setSecondaryBenefitCode(String secondaryBenefitCode) {
		this.secondaryBenefitCode = secondaryBenefitCode;
	}

	public boolean isFreeTrialPromo() {
		return freeTrialPromo;
	}

	public void setFreeTrialPromo(boolean freeTrialPromo) {
		this.freeTrialPromo = freeTrialPromo;
	}

	public boolean isDependentPromo() {
		return dependentPromo;
	}

	public void setDependentPromo(boolean dependentPromo) {
		this.dependentPromo = dependentPromo;
	}

	public boolean isIoOffer() {
		return ioOffer;
	}

	public void setIoOffer(boolean ioOffer) {
		this.ioOffer = ioOffer;
	}

	public int getFreeTrialDuration() {
		return freeTrialDuration;
	}

	public void setFreeTrialDuration(int freeTrialDuration) {
		this.freeTrialDuration = freeTrialDuration;
	}

	public boolean isAgentOffer() {
		return agentOffer;
	}

	public void setAgentOffer(boolean agentOffer) {
		this.agentOffer = agentOffer;
	}

	public String getDescriptionforServices() {
		return descriptionforServices;
	}

	public void setDescriptionforServices(String descriptionforServices) {
		this.descriptionforServices = descriptionforServices;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public String getPromoSourceSku() {
		return promoSourceSku;
	}

	public void setPromoSourceSku(String promoSourceSku) {
		this.promoSourceSku = promoSourceSku;
	}

	public boolean isMigrated() {
		return isMigrated;
	}

	public void setMigrated(boolean migrated) {
		isMigrated = migrated;
	}

	public String getContractTerm() {
		return contractTerm;
	}

	public void setContractTerm(String contractTerm) {
		this.contractTerm = contractTerm;
	}

	public int getMinQuantity() {
		return minQuantity;
	}

	public void setMinQuantity(int minQuantity) {
		this.minQuantity = minQuantity;
	}

	public String getExtPromoType() {
		return extPromoType;
	}

	public void setExtPromoType(String extPromoType) {
		this.extPromoType = extPromoType;
	}

	public int getPromoPriority() {
		return promoPriority;
	}

	public void setPromoPriority(int promoPriority) {
		this.promoPriority = promoPriority;
	}

	public String getSegmentDescription() {
		return segmentDescription;
	}

	public void setSegmentDescription(String segmentDescription) {
		this.segmentDescription = segmentDescription;
	}

	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public String getBenefitLevel() {
		return benefitLevel;
	}

	public void setBenefitLevel(String benefitLevel) {
		this.benefitLevel = benefitLevel;
	}

	public boolean isQuotaBased() {
		return isQuotaBased;
	}

	public void setQuotaBased(boolean quotaBased) {
		isQuotaBased = quotaBased;
	}

	public String getCallToAction() {
		return callToAction;
	}

	public void setCallToAction(String callToAction) {
		this.callToAction = callToAction;
	}

	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getLongDescriptionforServices() {
		return longDescriptionforServices;
	}

	public void setLongDescriptionforServices(String longDescriptionforServices) {
		this.longDescriptionforServices = longDescriptionforServices;
	}

	public String getShortDescriptionforServices() {
		return shortDescriptionforServices;
	}

	public void setShortDescriptionforServices(String shortDescriptionforServices) {
		this.shortDescriptionforServices = shortDescriptionforServices;
	}

	public String getShortDescriptionforOpus() {
		return shortDescriptionforOpus;
	}

	public void setShortDescriptionforOpus(String shortDescriptionforOpus) {
		this.shortDescriptionforOpus = shortDescriptionforOpus;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getDisplayNameforServices() {
		return displayNameforServices;
	}

	public void setDisplayNameforServices(String displayNameforServices) {
		this.displayNameforServices = displayNameforServices;
	}

	public List<ProductWrapper> getPromoSourceSkus() {
		return promoSourceSkus;
	}

	public void setPromoSourceSkus(List<ProductWrapper> promoSourceSkus) {
		this.promoSourceSkus = promoSourceSkus;
	}

	public boolean isNotApplibaleForOffer() {
		return notApplibaleForOffer;
	}

	public void setNotApplibaleForOffer(boolean notApplibaleForOffer) {
		this.notApplibaleForOffer = notApplibaleForOffer;
	}

	public String getOriginalBenefitType() {
		return originalBenefitType;
	}

	public void setOriginalBenefitType(String originalBenefitType) {
		this.originalBenefitType = originalBenefitType;
	}

	public String getValidityEndDate() {
		return validityEndDate;
	}

	public void setValidityEndDate(String validityEndDate) {
		this.validityEndDate = validityEndDate;
	}

	public List<Product> getRestrictedPlans() {
		return restrictedPlans;
	}

	public void setRestrictedPlans(List<Product> restrictedPlans) {
		this.restrictedPlans = restrictedPlans;
	}

	public boolean isEnableServiceDisconnect() {
		return enableServiceDisconnect;
	}

	public void setEnableServiceDisconnect(boolean enableServiceDisconnect) {
		this.enableServiceDisconnect = enableServiceDisconnect;
	}

	public List<String> getBenefitClassificationType() {
		return benefitClassificationType;
	}

	public void setBenefitClassificationType(List<String> benefitClassificationType) {
		this.benefitClassificationType = benefitClassificationType;
	}

	public BenefitDisplayNamesByKey getDisplayNamesByKey() {
		return displayNamesByKey;
	}

	public void setDisplayNamesByKey(BenefitDisplayNamesByKey displayNamesByKey) {
		this.displayNamesByKey = displayNamesByKey;
	}

	public BenefitDescriptionByKey getDescriptionsByKey() {
		return descriptionsByKey;
	}

	public void setDescriptionsByKey(BenefitDescriptionByKey descriptionsByKey) {
		this.descriptionsByKey = descriptionsByKey;
	}

	@Override
	public String toString() {
		return "Benefit{" + "benefitCategory='" + benefitCategory + '\'' + ", billingBenefitId='" + billingBenefitId
				+ '\'' + ", id='" + id + '\'' + ", code='" + code + '\'' + ", billingBenefitCode='" + billingBenefitCode
				+ '\'' + ", benefitName=" + benefitName + ", benefitDisplayName=" + benefitDisplayName
				+ ", benefitRank=" + benefitRank + ", description=" + description + ", opusDislcosureMessage='"
				+ opusDisclosureMessage + '\'' + ", myAttDisclosureMessage='" + myAttDisclosureMessage + '\''
				+ ", startDate='" + startDate + '\'' + ", retIoApplicableProducts=" + retIoApplicableProducts
				+ ", endDate='" + endDate + '\'' + ", contractIndicator='" + contractIndicator + '\''
				+ ", contractVersion='" + contractVersion + '\'' + ", selfBenefit=" + selfBenefit + ", duration="
				+ duration + ", period='" + period + '\'' + ", billAfterPeriod=" + billAfterPeriod
				+ ", applicableProducts=" + applicableProducts + ", maxOccurrence=" + maxOccurrence + ", benefitType='"
				+ benefitType + '\'' + ", value=" + value + ", customerClassification='" + customerClassification + '\''
				+ ", benefitTypeOrder='" + benefitTypeOrder + '\'' + ", secondaryBenefitCode='" + secondaryBenefitCode
				+ '\'' + ", freeTrialPromo=" + freeTrialPromo + ", dependentPromo=" + dependentPromo + ", ioOffer="
				+ ioOffer + ", freeTrialDuration=" + freeTrialDuration + ", agentOffer=" + agentOffer
				+ ", descriptionforServices='" + descriptionforServices + '\'' + ", contractType='" + contractType
				+ '\'' + ", promoSourceSku='" + promoSourceSku + '\'' + ", isMigrated=" + isMigrated
				+ ", contractTerm='" + contractTerm + '\'' + ", minQuantity=" + minQuantity + ", extPromoType='"
				+ extPromoType + '\'' + ", promoPriority=" + promoPriority + ", segmentDescription='"
				+ segmentDescription + '\'' + ", salesChannel='" + salesChannel + '\'' + ", benefitLevel='"
				+ benefitLevel + '\'' + ", atgPromoReferenceId='" + atgPromoReferenceId + '\'' + ", benefitChargeType='"
				+ benefitChargeType + '\'' + ", isQuotaBased=" + isQuotaBased + ", callToAction='" + callToAction + '\''
				+ ", customerSegments=" + customerSegments + ", longDescription='" + longDescription + '\''
				+ ", longDescriptionforServices='" + longDescriptionforServices + '\''
				+ ", shortDescriptionforServices='" + shortDescriptionforServices + '\'' + ", shortDescriptionforOpus='"
				+ shortDescriptionforOpus + '\'' + ", shortDescription='" + shortDescription + '\''
				+ ", displayNameforServices='" + displayNameforServices + '\'' + ", promoSourceSkus=" + promoSourceSkus
				+ ", notApplibaleForOffer=" + notApplibaleForOffer + ", originalBenefitType='" + originalBenefitType
				+ '\'' + ", validityEndDate='" + validityEndDate + '\'' + ", restrictedPlans=" + restrictedPlans
				+ ", enableServiceDisconnect=" + enableServiceDisconnect + ", rtpIndicator=" + rtpIndicator
				+ ", benefitSaleType=" + benefitSaleType + ", segment=" + segment + ", businessSegment="
				+ businessSegment + ", salesChannels=" + salesChannels + ", benefitActionType=" + benefitActionType
				+ ", gfVersion=" + gfVersion + ", enablerPromotionCode=" + enablerPromotionCode + ", locationType="
				+ locationType + ", location=" + location + ", benefitClassificationType=" + benefitClassificationType
				+ ", agreementID=" + agreementID + ", parentOffers=" + parentOffers + ", agreementPriceCode="
				+ agreementPriceCode + ", strikeThroughOffer=" + strikeThroughOffer
				+ ", displayNamesByKey=" + displayNamesByKey + ", descriptionsByKey=" + descriptionsByKey +'}';
	}

	public String getAtgPromoReferenceId() {
		return atgPromoReferenceId;
	}

	public void setAtgPromoReferenceId(String atgPromoReferenceId) {
		this.atgPromoReferenceId = atgPromoReferenceId;
	}

	public String getBenefitChargeType() {
		return benefitChargeType;
	}

	public void setBenefitChargeType(String benefitChargeType) {
		this.benefitChargeType = benefitChargeType;
	}

	public boolean isCouponPromo() {
		return isCouponPromo;
	}

	public void setCouponPromo(boolean isCouponPromo) {
		this.isCouponPromo = isCouponPromo;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getExtBenefitCode() {
		return extBenefitCode;
	}

	public void setExtBenefitCode(String extBenefitCode) {
		this.extBenefitCode = extBenefitCode;
	}

	public List<String> getStackableDiscounts() {
		return stackableDiscounts;
	}

	public void setStackableDiscounts(List<String> stackableDiscounts) {
		this.stackableDiscounts = stackableDiscounts;
	}

	public List<ParentOffers> getParentOffers() {
		return parentOffers;
	}

	public void setParentOffers(List<ParentOffers> parentOffers) {
		this.parentOffers = parentOffers;
	}

	public Integer getAvailableQuota() {
		return availableQuota;
	}

	public void setAvailableQuota(Integer availableQuota) {
		this.availableQuota = availableQuota;
	}
	public List<String> getEligibleIAPPartners() {
		return eligibleIAPPartners;
	}
	public void setEligibleIAPPartners(List<String> eligibleIAPPartners) {
		this.eligibleIAPPartners = eligibleIAPPartners;
	}
}