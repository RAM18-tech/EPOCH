package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.request.AdditionalAccountDetails;
import com.dtv.dcp.epoch.model.common.request.AgentDealerDetails;
import com.dtv.dcp.epoch.model.common.request.ComplexFilterContext;
import com.dtv.dcp.epoch.model.common.request.CustomerEligibility;
import com.dtv.dcp.epoch.model.common.request.OfferRequest;
import com.dtv.dcp.epoch.model.common.request.OnlinePartnerDetails;
import com.dtv.dcp.epoch.model.common.request.PartnerDealerDetails;
import com.dtv.dcp.epoch.model.common.request.ProductInfo;
import com.dtv.dcp.epoch.model.ct.offer.ChannelEligibilityByKey;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CTOfferRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String flow;

	private List<String> benefitsCodesToSuppressOffer;

	private List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer;

	private List<String> accountTypes;

	private List<String> businessSegment;

	private List<String> businessSegments;

	private List<String> addOnType;

	private List<String> billingBenefitIds;

	private List<String> billingBenefitCodes;

	private List<String> billingProductCodes;

	private CTCartContext cartContext;

	private List<String> compatibleProducts;

	private List<String> contractIndicator;

	private List<CTCustomerContext> customerContext;

	private ComplexFilterContext complexfilterContext;

	private CustomerEligibility customerEligibility;

	private List<CTCustomerIntent> customerIntent;

	private List<String> customerSegments;

	@JsonProperty("existingCustomerTypes")
	private List<String> customerTypes;

	private Exclusions exclusions;

	private List<String> includeAttributes;

	private List<String> includedProducts;

	private String minimumPurchaseAmount;

	private List<String> offerActionType;

	private List<String> offerIds;

	private List<String> offerCodes;

	private List<String> offerProductFamily;

	private List<String> offerProductType;

	private List<String> offerStatus;

	private List<String> offerType;

	private Pagination pagination;

	private List<String> planSubType;

	private List<String> productCategories;

	private List<String> qualifyingProducts;

	private List<String> salesChannel;

	private String state;

	private List<String> offerIntent;

	private List<String> bundleProducts;

	private List<String> offerClassificationType;

	private List<String> migrationServiceType;

	private List<String> migrationCustomerCohort;

	private String creditRisk;
	
	private String treatmentCode;

	private String additionalOfferType;

	private boolean expandProductRefs;

	private boolean migrationIndicator;

	private String opusChannel;

	private String opusSubChannel;

	private String opusStoreId;

	private String iapPartnerAccountType;

	@JsonProperty("isOfferTrayEnabled")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isOfferTrayEnabled;

	/** The expiredOffers. */
	@JsonProperty("isExpiredOffers")
	private boolean expiredOffers;

	private List<String> retentionOfferUser;

	private List<String> paymentType;

	private List<String> customerSubType;

	private List<Integer> fan;

	private List<String> fanCategories;

	private List<String> couponCodes;

	private List<String> couponStatus;

	private List<String> productsOnAccountToSuppressOffer;

	private List<String> partnerType;

	private List<String> customerType;

	private Boolean hasLocalChannels;

	private PriceProtection priceProtection;

	private String nextBillingDate;

	private Boolean oldDateFormat;

	private List<ProductInfo> cartProducts;

	private List<String> cartOffers;

	// Added for the US# 158528 (Longhorn Long Term)
	private AgentDealerDetails agentDealerDetails;
	private PartnerDealerDetails partnerDealerDetails;

	private OnlinePartnerDetails onlinePartnerDetails;
	
	private boolean reconnectOfferFlag;
	
	private Boolean isMigrationRequired;
	
	private CTChannelEligibility channelEligibility;
	
	private Boolean adeRequest;

	private List<String> marketingSourceCode;

	private OfferRequest offerRequest;

	private List<String> flowIntents;

	private List<String> serviceSubscriptionType;

	public List<String> getFlowIntents() {
		return flowIntents;
	}

	public void setFlowIntents(List<String> flowIntents) {
		this.flowIntents = flowIntents;
	}

	public List<String> getServiceSubscriptionType() {
		return serviceSubscriptionType;
	}

	public void setServiceSubscriptionType(List<String> serviceSubscriptionType) {
		this.serviceSubscriptionType = serviceSubscriptionType;
	}

	public List<String> getMarketingSourceCode() {
		return marketingSourceCode;
	}

	public void setMarketingSourceCode(List<String> marketingSourceCode) {
		this.marketingSourceCode = marketingSourceCode;
	}
	
	public Boolean getAdeRequest() {
		return adeRequest;
	}

	public void setAdeRequest(Boolean adeRequest) {
		this.adeRequest = adeRequest;
	}

	/**
	 * @return the channelEligibility
	 */
	public CTChannelEligibility getChannelEligibility() {
		return channelEligibility;
	}

	/**
	 * @param channelEligibility the channelEligibility to set
	 */
	public void setChannelEligibility(CTChannelEligibility channelEligibility) {
		this.channelEligibility = channelEligibility;
	}
    private List<ChannelEligibilityByKey> channelEligibilityByKey;

    public List<ChannelEligibilityByKey> getChannelEligibilityByKey() {
        return channelEligibilityByKey;
    }

    public void setChannelEligibilityByKey(List<ChannelEligibilityByKey> channelEligibilityByKey) {
        this.channelEligibilityByKey = channelEligibilityByKey;
    }

    public Boolean getIsMigrationRequired() {
		return isMigrationRequired;
	}

	public void setIsMigrationRequired(Boolean isMigrationRequired) {
		this.isMigrationRequired = isMigrationRequired;
	}

	private AdditionalAccountDetails additionalAccountDetails;
	
	public AdditionalAccountDetails getAdditionalAccountDetails() {
		return additionalAccountDetails;
	}

	public void setAdditionalAccountDetails(AdditionalAccountDetails additionalAccountDetails) {
		this.additionalAccountDetails = additionalAccountDetails;
	}

	public OnlinePartnerDetails getOnlinePartnerDetails()
	{
		return onlinePartnerDetails;
	}

	public void setOnlinePartnerDetails(OnlinePartnerDetails onlinePartnerDetails)
	{
		this.onlinePartnerDetails = onlinePartnerDetails;
	}

	public PartnerDealerDetails getPartnerDealerDetails() {
		return partnerDealerDetails;
	}

	public void setPartnerDealerDetails(PartnerDealerDetails partnerDealerDetails) {
		this.partnerDealerDetails = partnerDealerDetails;
	}
	
	public AgentDealerDetails getAgentDealerDetails() {
		return agentDealerDetails;
	}

	public void setAgentDealerDetails(AgentDealerDetails agentDealerDetails) {
		this.agentDealerDetails = agentDealerDetails;
	}
	public Boolean getHasLocalChannels() {
		return hasLocalChannels;
	}

	public void setHasLocalChannels(Boolean hasLocalChannels) {
		this.hasLocalChannels = hasLocalChannels;
	}

	/**
	 * @return the customerType
	 */
	public List<String> getCustomerType() {
		return customerType;
	}

	/**
	 * @param customerType
	 *            the customerType to set
	 */
	public void setCustomerType(List<String> customerType) {
		this.customerType = customerType;
	}

	public List<String> getPartnerType() {
		return partnerType;
	}

	public void setPartnerType(List<String> partnerType) {
		this.partnerType = partnerType;
	}

	/**
	 * @return the productsOnAccountToSuppressOffer
	 */
	public List<String> getProductsOnAccountToSuppressOffer() {
		return productsOnAccountToSuppressOffer;
	}

	/**
	 * @param productsOnAccountToSuppressOffer
	 *            the productsOnAccountToSuppressOffer to set
	 */
	public void setProductsOnAccountToSuppressOffer(List<String> productsOnAccountToSuppressOffer) {
		this.productsOnAccountToSuppressOffer = productsOnAccountToSuppressOffer;
	}

	public List<String> getCouponStatus() {
		return couponStatus;
	}

	public void setCouponStatus(List<String> couponStatus) {
		this.couponStatus = couponStatus;
	}

	public List<String> getCouponCodes() {
		return couponCodes;
	}

	public void setCouponCodes(List<String> couponCodes) {
		this.couponCodes = couponCodes;
	}

	public boolean isExpiredOffers() {
		return expiredOffers;
	}

	public void setExpiredOffers(boolean expiredOffers) {
		this.expiredOffers = expiredOffers;
	}

	public List<String> getOfferClassificationType() {
		return offerClassificationType;
	}

	public void setOfferClassificationType(List<String> offerClassificationType) {
		this.offerClassificationType = offerClassificationType;
	}



	public List<String> getOfferIntent() {
		return offerIntent;
	}

	public void setOfferIntent(List<String> offerIntent) {
		this.offerIntent = offerIntent;
	}

	public List<String> getAccountTypes() {
		return accountTypes;
	}

	public void setAccountTypes(List<String> accountTypes) {
		this.accountTypes = accountTypes;
	}

	public List<String> getAddOnType() {
		return addOnType;
	}

	public void setAddOnType(List<String> addOnType) {
		this.addOnType = addOnType;
	}

	public List<String> getBillingBenefitIds() {
		return billingBenefitIds;
	}

	public void setBillingBenefitIds(List<String> billingBenefitIds) {
		this.billingBenefitIds = billingBenefitIds;
	}

	public List<String> getBillingBenefitCodes() {
		return billingBenefitCodes;
	}

	public void setBillingBenefitCodes(List<String> billingBenefitCodes) {
		this.billingBenefitCodes = billingBenefitCodes;
	}

	public List<String> getBillingProductCodes() {
		return billingProductCodes;
	}

	public void setBillingProductCodes(List<String> billingProductCodes) {
		this.billingProductCodes = billingProductCodes;
	}

	public CTCartContext getCartContext() {
		return cartContext;
	}

	public List<String> getBusinessSegments() {
		return businessSegments;
	}

	public void setBusinessSegments(List<String> businessSegments) {
		this.businessSegments = businessSegments;
	}

	public void setCartContext(CTCartContext cartContext) {
		this.cartContext = cartContext;
	}

	public List<String> getCompatibleProducts() {
		return compatibleProducts;
	}

	public void setCompatibleProducts(List<String> compatibleProducts) {
		this.compatibleProducts = compatibleProducts;
	}

	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	public List<CTCustomerContext> getCustomerContext() {
		return customerContext;
	}

	public void setCustomerContext(List<CTCustomerContext> customerContext) {
		this.customerContext = customerContext;
	}

	public CustomerEligibility getCustomerEligibility() {
		return customerEligibility;
	}

	public void setCustomerEligibility(CustomerEligibility customerEligibility) {
		this.customerEligibility = customerEligibility;
	}

	public List<CTCustomerIntent> getCustomerIntent() {
		return customerIntent;
	}

	public void setCustomerIntent(List<CTCustomerIntent> customerIntent) {
		this.customerIntent = customerIntent;
	}

	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	public List<String> getCustomerTypes() {
		return customerTypes;
	}

	public void setCustomerTypes(List<String> customerTypes) {
		this.customerTypes = customerTypes;
	}

	public Exclusions getExclusions() {
		return exclusions;
	}

	public void setExclusions(Exclusions exclusions) {
		this.exclusions = exclusions;
	}

	public List<String> getIncludeAttributes() {
		return includeAttributes;
	}

	public void setIncludeAttributes(List<String> includeAttributes) {
		this.includeAttributes = includeAttributes;
	}

	public List<String> getIncludedProducts() {
		return includedProducts;
	}

	public void setIncludedProducts(List<String> includedProducts) {
		this.includedProducts = includedProducts;
	}

	public String getMinimumPurchaseAmount() {
		return minimumPurchaseAmount;
	}

	public void setMinimumPurchaseAmount(String minimumPurchaseAmount) {
		this.minimumPurchaseAmount = minimumPurchaseAmount;
	}

	public List<String> getOfferActionType() {
		return offerActionType;
	}

	public void setOfferActionType(List<String> offerActionType) {
		this.offerActionType = offerActionType;
	}

	public List<String> getOfferIds() {
		return offerIds;
	}

	public void setOfferIds(List<String> offerIds) {
		this.offerIds = offerIds;
	}

	public List<String> getOfferProductFamily() {
		return offerProductFamily;
	}

	public void setOfferProductFamily(List<String> offerProductFamily) {
		this.offerProductFamily = offerProductFamily;
	}

	public List<String> getOfferProductType() {
		return offerProductType;
	}

	public void setOfferProductType(List<String> offerProductType) {
		this.offerProductType = offerProductType;
	}

	public List<String> getOfferStatus() {
		return offerStatus;
	}

	public void setOfferStatus(List<String> offerStatus) {
		this.offerStatus = offerStatus;
	}

	public List<String> getOfferType() {
		return offerType;
	}

	public void setOfferType(List<String> offerType) {
		this.offerType = offerType;
	}

	public Pagination getPagination() {
		return pagination;
	}

	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}

	public List<String> getPlanSubType() {
		return planSubType;
	}

	public void setPlanSubType(List<String> planSubType) {
		this.planSubType = planSubType;
	}

	public List<String> getProductCategories() {
		return productCategories;
	}

	public void setProductCategories(List<String> productCategories) {
		this.productCategories = productCategories;
	}

	public List<String> getQualifyingProducts() {
		return qualifyingProducts;
	}

	public void setQualifyingProducts(List<String> qualifyingProducts) {
		this.qualifyingProducts = qualifyingProducts;
	}

	public List<String> getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<String> getOfferCodes() {
		return offerCodes;
	}

	public void setOfferCodes(List<String> offerCodes) {
		this.offerCodes = offerCodes;
	}

	public List<String> getBundleProducts() {
		return bundleProducts;
	}

	public void setBundleProducts(List<String> bundleProducts) {
		this.bundleProducts = bundleProducts;
	}

	public List<String> getMigrationServiceType() {
		return migrationServiceType;
	}

	public void setMigrationServiceType(List<String> migrationServiceType) {
		this.migrationServiceType = migrationServiceType;
	}

	public List<String> getMigrationCustomerCohort() {
		return migrationCustomerCohort;
	}

	public void setMigrationCustomerCohort(List<String> migrationCustomerCohort) {
		this.migrationCustomerCohort = migrationCustomerCohort;
	}

	public List<String> getBenefitsCodesToSuppressOffer() {
		return benefitsCodesToSuppressOffer;
	}

	public void setBenefitsCodesToSuppressOffer(List<String> benefitsCodesToSuppressOffer) {
		this.benefitsCodesToSuppressOffer = benefitsCodesToSuppressOffer;
	}

	/**
	 * @return the benefitCodesToSuppressTheOffer
	 */
	public List<BenefitCodesToSuppressTheOffer> getBenefitCodesToSuppressTheOffer() {
		return benefitCodesToSuppressTheOffer;
	}

	/**
	 * @param benefitCodesToSuppressTheOffer
	 *            the benefitCodesToSuppressTheOffer to set
	 */
	public void setBenefitCodesToSuppressTheOffer(List<BenefitCodesToSuppressTheOffer> benefitCodesToSuppressTheOffer) {
		this.benefitCodesToSuppressTheOffer = benefitCodesToSuppressTheOffer;
	}

	public String getCreditRisk() {
		return creditRisk;
	}

	public void setCreditRisk(String creditRisk) {
		this.creditRisk = creditRisk;
	}

	public boolean isExpandProductRefs() {
		return expandProductRefs;
	}

	public void setExpandProductRefs(boolean expandProductRefs) {
		this.expandProductRefs = expandProductRefs;
	}

	public String getAdditionalOfferType() {
		return additionalOfferType;
	}

	public void setAdditionalOfferType(String additionalOfferType) {
		this.additionalOfferType = additionalOfferType;
	}

	public List<String> getRetentionOfferUser() {
		return retentionOfferUser;
	}

	public void setRetentionOfferUser(List<String> retentionOfferUser) {
		this.retentionOfferUser = retentionOfferUser;
	}

	public PriceProtection getPriceProtection() {
		return priceProtection;
	}

	public void setPriceProtection(PriceProtection priceProtection) {
		this.priceProtection = priceProtection;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CTOfferRequest [benefitsCodesToSuppressOffer=");
		builder.append(benefitsCodesToSuppressOffer);
		builder.append(", benefitCodesToSuppressTheOffer=");
		builder.append(benefitCodesToSuppressTheOffer);
		builder.append(", accountTypes=");
		builder.append(accountTypes);
		builder.append(", businessSegment=");
		builder.append(businessSegment);
		builder.append(", businessSegments=");
		builder.append(businessSegments);
		builder.append(", addOnType=");
		builder.append(addOnType);
		builder.append(", billingBenefitIds=");
		builder.append(billingBenefitIds);
		builder.append(", billingBenefitCodes=");
		builder.append(billingBenefitCodes);
		builder.append(", billingProductCodes=");
		builder.append(billingProductCodes);
		builder.append(", cartContext=");
		builder.append(cartContext);
		builder.append(", compatibleProducts=");
		builder.append(compatibleProducts);
		builder.append(", contractIndicator=");
		builder.append(contractIndicator);
		builder.append(", customerContext=");
		builder.append(customerContext);
		builder.append(", complexfilterContext=");
		builder.append(complexfilterContext);
		builder.append(", customerEligibility=");
		builder.append(customerEligibility);
		builder.append(", customerIntent=");
		builder.append(customerIntent);
		builder.append(", customerSegments=");
		builder.append(customerSegments);
		builder.append(", customerTypes=");
		builder.append(customerTypes);
		builder.append(", exclusions=");
		builder.append(exclusions);
		builder.append(", includeAttributes=");
		builder.append(includeAttributes);
		builder.append(", includedProducts=");
		builder.append(includedProducts);
		builder.append(", minimumPurchaseAmount=");
		builder.append(minimumPurchaseAmount);
		builder.append(", offerActionType=");
		builder.append(offerActionType);
		builder.append(", offerIds=");
		builder.append(offerIds);
		builder.append(", offerCodes=");
		builder.append(offerCodes);
		builder.append(", offerProductFamily=");
		builder.append(offerProductFamily);
		builder.append(", offerProductType=");
		builder.append(offerProductType);
		builder.append(", offerStatus=");
		builder.append(offerStatus);
		builder.append(", offerType=");
		builder.append(offerType);
		builder.append(", pagination=");
		builder.append(pagination);
		builder.append(", planSubType=");
		builder.append(planSubType);
		builder.append(", productCategories=");
		builder.append(productCategories);
		builder.append(", qualifyingProducts=");
		builder.append(qualifyingProducts);
		builder.append(", salesChannel=");
		builder.append(salesChannel);
		builder.append(", state=");
		builder.append(state);
		builder.append(", offerIntent=");
		builder.append(offerIntent);
		builder.append(", bundleProducts=");
		builder.append(bundleProducts);
		builder.append(", offerClassificationType=");
		builder.append(offerClassificationType);
		builder.append(", migrationServiceType=");
		builder.append(migrationServiceType);
		builder.append(", migrationCustomerCohort=");
		builder.append(migrationCustomerCohort);
		builder.append(", creditRisk=");
		builder.append(creditRisk);
		builder.append(", additionalOfferType=");
		builder.append(additionalOfferType);
		builder.append(", expandProductRefs=");
		builder.append(expandProductRefs);
		builder.append(", migrationIndicator=");
		builder.append(migrationIndicator);
		builder.append(", isOfferTrayEnabled=");
		builder.append(isOfferTrayEnabled);
		builder.append(", expiredOffers=");
		builder.append(expiredOffers);
		builder.append(", retentionOfferUser=");
		builder.append(retentionOfferUser);
		builder.append(", paymentType=");
		builder.append(paymentType);
		builder.append(", customerSubType=");
		builder.append(customerSubType);
		builder.append(", fan=");
		builder.append(fan);
		builder.append(", fanCategories=");
		builder.append(fanCategories);
		builder.append(", couponCodes=");
		builder.append(couponCodes);
		builder.append(", couponStatus=");
		builder.append(couponStatus);
		builder.append(", productsOnAccountToSuppressOffer=");
		builder.append(productsOnAccountToSuppressOffer);
		builder.append(", partnerType=");
		builder.append(partnerType);
		builder.append(", customerType=");
		builder.append(customerType);
		builder.append(", hasLocalChannels=");
		builder.append(hasLocalChannels);
		builder.append(", priceProtection=");
		builder.append(priceProtection);
		builder.append(", oldDateFormat=");
		builder.append(oldDateFormat);
		builder.append(", opusChannel=");
		builder.append(opusChannel);
		builder.append(", opusSubChannel=");
		builder.append(opusSubChannel);
		builder.append(", opusStoreId=");
		builder.append(opusStoreId);
		builder.append(",agentDealerDetails=");
		builder.append(agentDealerDetails);
		builder.append(",partnerDealerDetails=");
		builder.append(partnerDealerDetails);
		builder.append(",reconnectOfferFlag=");
		builder.append(reconnectOfferFlag);
		builder.append(",marketingSourceCode=");
		builder.append(marketingSourceCode);
		builder.append(", decisioningFlow=");
		builder.append(decisioningFlow);
		builder.append("]");
		return builder.toString();
	}

	public ComplexFilterContext getComplexfilterContext() {
		return complexfilterContext;
	}

	public void setComplexfilterContext(ComplexFilterContext complexfilterContext) {
		this.complexfilterContext = complexfilterContext;
	}

	public List<String> getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(List<String> paymentType) {
		this.paymentType = paymentType;
	}

	/**
	 * @return the nextBillingDate
	 */
	public String getNextBillingDate() {
		return nextBillingDate;
	}

	/**
	 * @param nextBillingDate
	 *            the nextBillingDate to set
	 */
	public void setNextBillingDate(String nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public List<String> getCustomerSubType() {
		return customerSubType;
	}

	public void setCustomerSubType(List<String> customerSubType) {
		this.customerSubType = customerSubType;
	}

	public List<Integer> getFan() {
		return fan;
	}

	public void setFan(List<Integer> fan) {
		this.fan = fan;
	}

	public List<String> getFanCategories() {
		return fanCategories;
	}

	public void setFanCategories(List<String> fanCategories) {
		this.fanCategories = fanCategories;
	}

	public Boolean isOfferTrayEnabled() {
		return isOfferTrayEnabled;
	}

	public void setOfferTrayEnabled(Boolean isOfferTrayEnabled) {
		this.isOfferTrayEnabled = isOfferTrayEnabled;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public boolean isMigrationIndicator() {
		return migrationIndicator;
	}

	public void setMigrationIndicator(boolean migrationIndicator) {
		this.migrationIndicator = migrationIndicator;
	}

	public Boolean getOldDateFormat() {
		return oldDateFormat;
	}

	public void setOldDateFormat(Boolean oldDateFormat) {
		this.oldDateFormat = oldDateFormat;
	}

	public List<ProductInfo> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(List<ProductInfo> cartProducts) {
		this.cartProducts = cartProducts;
	}

	public List<String> getCartOffers() {
		return cartOffers;
	}

	public void setCartOffers(List<String> cartOffers) {
		this.cartOffers = cartOffers;
	}

	public String getOpusChannel() {
		return opusChannel;
	}

	public void setOpusChannel(String opusChannel) {
		this.opusChannel = opusChannel;
	}

	public String getOpusSubChannel() {
		return opusSubChannel;
	}

	public void setOpusSubChannel(String opusSubChannel) {
		this.opusSubChannel = opusSubChannel;
	}

	public String getOpusStoreId() {
		return opusStoreId;
	}

	public void setOpusStoreId(String opusStoreId) {
		this.opusStoreId = opusStoreId;
	}

	public String getIapPartnerAccountType() {
		return iapPartnerAccountType;
	}

	public void setIapPartnerAccountType(String iapPartnerAccountType) {
		this.iapPartnerAccountType = iapPartnerAccountType;
	}

	public boolean isReconnectOfferFlag() {
		return reconnectOfferFlag;
	}

	public void setReconnectOfferFlag(boolean reconnectOfferFlag) {
		this.reconnectOfferFlag = reconnectOfferFlag;
	}

	public String getTreatmentCode() {
		return treatmentCode;
	}

	public void setTreatmentCode(String treatmentCode) {
		this.treatmentCode = treatmentCode;
	}
	private boolean decisioningFlow;

	public boolean isDecisioningFlow() {
		return decisioningFlow;
	}

	public void setDecisioningFlow(boolean decisioningFlow) {
		this.decisioningFlow = decisioningFlow;
	}

	public OfferRequest getOfferRequest() {
		return offerRequest;
	}

	public void setOfferRequest(OfferRequest offerRequest) {
		this.offerRequest = offerRequest;
	}

	public String getFlow() {
		return flow;
	}
	public void setFlow(String flow) {
		this.flow = flow;
	}
	private boolean reconnectCustomer;
	public boolean isReconnectCustomer() {
		return reconnectCustomer;
	}
	public void setReconnectCustomer(boolean reconnectCustomer) {
		this.reconnectCustomer = reconnectCustomer;
	}
	private String serviceEndDate;
	public String getServiceEndDate() {
		return serviceEndDate;
	}
	public void setServiceEndDate(String serviceEndDate) {
		this.serviceEndDate = serviceEndDate;
	}
	private String existingAccountSubscriberType;
	public String getExistingAccountSubscriberType() {
		return existingAccountSubscriberType;
	}
	public void setExistingAccountSubscriberType(String existingAccountSubscriberType) {
		this.existingAccountSubscriberType = existingAccountSubscriberType;
	}
}
