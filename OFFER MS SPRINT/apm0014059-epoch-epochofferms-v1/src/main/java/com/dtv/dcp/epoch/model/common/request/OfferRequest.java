package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.dtv.dcp.epoch.model.ct.request.CartOffer;
import com.dtv.dcp.epoch.model.ct.request.Pagination;
import com.dtv.dcp.epoch.model.ct.response.STMSBOMLineItem;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferRequest implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * The accountTypes.
     */
    private List<String> accountTypes;
    /**
     * The addOnType.
     */
    private List<String> addOnType;
    /**
     * The benefitCodes.
     */
    private List<String> benefitCodes;
    /**
     * The billingProductCodes.
     */
    private List<String> billingProductCodes;
    /**
     * The bundleProductCodes.
     */
    private List<String> bundleProductCodes;
    /**
     * The bundleProductIds.
     */
    private List<String> bundleProductIds;
    
    /**
     * The bundleProductIds.
     */
    @JsonIgnore
    private List<String> benefitsCodesToSuppressOffer;
    
    private List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived;
    /**
     * The businessSegment.
     */
    private List<String> businessSegment;
    
    /**
     * The customerSegments.
     */
    private List<String> customerSegments;
    
    /**
     * The businessSegments.
     */
    private List<String> businessSegments;
    /**
     * The cartContext.
     */
    private CartContext cartContext;

    private List<CartOffer> cartOffers;
    /**
     * The channelEligibility.
     */
    private ChannelEligibility channelEligibility;
    /**
     * The contractIndicator.
     */
    private List<String> contractIndicator;
    private String customerSubscriptionType;
    private CustomerContext customerContext;
    /**
     * The customerEligibility.
     */
    private CustomerEligibility customerEligibility;
    private List<CustomerIntent> customerIntent;

    private String mode;
    /**
     * The heartValue.
     */
    private String heartValue;
    /**
     * The offerActionType.
     */
    private List<String> offerActionType;
    /**
     * The offerCodes.
     */
    private List<String> offerCodes;
    /**
     * The offerIds.
     */
    private List<String> offerIds;
    /**
     * The offerProductFamily.
     */
    private List<String> offerProductFamily;
    /**
     * The serverDate.
     */
    private String serverDate;
    /**
     * The offerProductTypes.
     */
    @JsonProperty("offerProductTypes")
    private List<String> offerProductType;
    /**
     * The offerStatus.
     */
    private List<String> offerStatus;
    /**
     * The offerType.
     */
    private List<String> offerTypes;
    private Pagination pagination;
    /**
     * The planSubType.
     */
    private List<String> planSubType;
    private List<String> productCodes;
    /**
     * The qualifyingProductCodes.
     */
    private List<String> qualifyingProductCodes;
    /**
     * The qualifyingProductCodes.
     */
    private List<String> qualifyingProducts;
    /**
     * The salesChannel.
     */
    private List<String> salesChannel;
    
    /**
     * The offerClassificationType.
     */
    private List<String> offerClassificationType;
    
    /**
     * The migrationServiceType.
     */
    private List<String> migrationServiceType;

    /**
     *
     */
    private String creditRisk;
    
  
	/** The expiredOffers. */
	@JsonProperty("isExpiredOffers")
	private boolean expiredOffers;   
 
	private ComplexFilterContext complexfilterContext;
	
	private String paymentType;
	
	private List<String> customerSubTypes;
	
	private String fan;
	
	private List<String> fanCategories;
	
	@JsonProperty("isOfferTrayEnabled")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Boolean isOfferTrayEnabled;
	
	private List<String> couponStatus;
	
	private List<String> productsOnAccountToSuppressOffer;
	
	private boolean migrationIndicator;
	
	private boolean reconnectCustomer;
	
	private String serviceEndDate;
	
	private String existingAccountSubscriberType;
	
	private String billingSystem;
	
	private Boolean isCartModeOn;
	
	private Boolean validateCart;
	
	private Boolean isMigrationRequired;
	
	private List<String> marketingSourceCode;
	
	private List<STMSBOMLineItem> orderContext;
	
	private String treatmentCode;
	
	private PartnerDealerDetails partnerDealerDetails;
	
	private String contentChannelId;

    private String originalContractIndicator;

    private String iapSalesChannelIsPresent;

    public String getIapSalesChannelIsPresent() {
        return iapSalesChannelIsPresent;
    }

    public void setIapSalesChannelIsPresent(String iapSalesChannelIsPresent) {
        this.iapSalesChannelIsPresent = iapSalesChannelIsPresent;
    }

    public String getOriginalContractIndicator() {
        return originalContractIndicator;
    }

    public void setOriginalContractIndicator(String originalContractIndicator) {
        this.originalContractIndicator = originalContractIndicator;
    }

    public String getCustomerSubscriptionType() {
        return customerSubscriptionType;
    }

    public void setCustomerSubscriptionType(String customerSubscriptionType) {
        this.customerSubscriptionType = customerSubscriptionType;
    }

    public List<CartOffer> getCartOffers() {
        return cartOffers;
    }

    public void setCartOffers(List<CartOffer> cartOffers) {
        this.cartOffers = cartOffers;
    }

    public String getContentChannelId() {
		return contentChannelId;
	}

	public void setContentChannelId(String contentChannelId) {
		this.contentChannelId = contentChannelId;
	}

	public String getTreatmentCode() {
		return treatmentCode;
	}

	public void setTreatmentCode(String treatmentCode) {
		this.treatmentCode = treatmentCode;
	}

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
	 * @return the orderContext
	 */
	public List<STMSBOMLineItem> getOrderContext() {
		return orderContext;
	}

	/**
	 * @param orderContext the orderContext to set
	 */
	public void setOrderContext(List<STMSBOMLineItem> orderContext) {
		this.orderContext = orderContext;
	}
	
	public List<String> getMarketingSourceCode() {
		return marketingSourceCode;
	}

	public void setMarketingSourceCode(List<String> marketingSourceCode) {
		this.marketingSourceCode = marketingSourceCode;
	}

	public Boolean getIsMigrationRequired() {
		return isMigrationRequired;
	}

	public void setIsMigrationRequired(Boolean isMigrationRequired) {
		this.isMigrationRequired = isMigrationRequired;
	}

	public Boolean getValidateCart() {
		return validateCart;
	}

	public void setValidateCart(Boolean validateCart) {
		this.validateCart = validateCart;
	}

	public Boolean getIsCartModeOn() {
		return isCartModeOn;
	}

	public void setIsCartModeOn(Boolean isCartModeOn) {
		this.isCartModeOn = isCartModeOn;
	}

	public AgentDealerDetails getAgentDealerDetails() {
		return agentDealerDetails;
	}

	public void setAgentDealerDetails(AgentDealerDetails agentDealerDetails) {
		this.agentDealerDetails = agentDealerDetails;
	}

	// Added for the US# 158528 (Longhorn Long Term)
	private AgentDealerDetails agentDealerDetails;

	private OnlinePartnerDetails onlinePartnerDetails;

	public OnlinePartnerDetails getOnlinePartnerDetails()
	{
		return onlinePartnerDetails;
	}

	public void setOnlinePartnerDetails(OnlinePartnerDetails onlinePartnerDetails)
	{
		this.onlinePartnerDetails = onlinePartnerDetails;
	}

	public String getBillingSystem() {
		return billingSystem;
	}

	public void setBillingSystem(String billingSystem) {
		this.billingSystem = billingSystem;
	}

	public String getExistingAccountSubscriberType() {
		return existingAccountSubscriberType;
	}

	public void setExistingAccountSubscriberType(String existingAccountSubscriberType) {
		this.existingAccountSubscriberType = existingAccountSubscriberType;
	}

	private Boolean hasLocalChannels;
	
    private Boolean hasOTTLocalChannels = false;

	private boolean decisioningFlow;


	public boolean isDecisioningFlow() {
		return decisioningFlow;
	}

	public void setDecisioningFlow(boolean decisioningFlow) {
		this.decisioningFlow = decisioningFlow;
	}


    public Boolean getHasLocalChannels() {
		return hasLocalChannels;
	}

	public void setHasLocalChannels(Boolean hasLocalChannels) {
		this.hasLocalChannels = hasLocalChannels;
	}

	/**
	 * @return the reconnectCustomer
	 */
	public boolean isReconnectCustomer() {
		return reconnectCustomer;
	}

	/**
	 * @param reconnectCustomer the reconnectCustomer to set
	 */
	public void setReconnectCustomer(boolean reconnectCustomer) {
		this.reconnectCustomer = reconnectCustomer;
	}

	/**
	 * @return the serviceEndDate
	 */
	public String getServiceEndDate() {
		return serviceEndDate;
	}

	/**
	 * @param serviceEndDate the serviceEndDate to set
	 */
	public void setServiceEndDate(String serviceEndDate) {
		this.serviceEndDate = serviceEndDate;
	}

	public boolean isMigrationIndicator() {
		return migrationIndicator;
	}

	public void setMigrationIndicator(boolean migrationIndicator) {
		this.migrationIndicator = migrationIndicator;
	}

    private Boolean swimlaneSwitchEligible;
	
	
	public Boolean getSwimlaneSwitchEligible() {
		return swimlaneSwitchEligible;
	}

	public void setSwimlaneSwitchEligible(Boolean swimlaneSwitchEligible) {
		this.swimlaneSwitchEligible = swimlaneSwitchEligible;
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

	public List<String> getCouponStatus() {
		return couponStatus;
	}

	public void setCouponStatus(List<String> couponStatus) {
		this.couponStatus = couponStatus;
	}

	public boolean isExpiredOffers() {
		return expiredOffers;
	}

	public void setExpiredOffers(boolean expiredOffers) {
		this.expiredOffers = expiredOffers;
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

    public List<String> getBenefitCodes() {
        return benefitCodes;
    }

    public void setBenefitCodes(List<String> benefitCodes) {
        this.benefitCodes = benefitCodes;
    }

    public List<String> getBillingProductCodes() {
        return billingProductCodes;
    }

    public void setBillingProductCodes(List<String> billingProductCodes) {
        this.billingProductCodes = billingProductCodes;
    }

    public List<String> getBundleProductCodes() {
        return bundleProductCodes;
    }

    public void setBundleProductCodes(List<String> bundleProductCodes) {
        this.bundleProductCodes = bundleProductCodes;
    }

    public List<String> getBundleProductIds() {
        return bundleProductIds;
    }

    public void setBundleProductIds(List<String> bundleProductIds) {
        this.bundleProductIds = bundleProductIds;
    }

    public List<String> getBusinessSegment() {
        return businessSegment;
    }

    public void setBusinessSegment(List<String> businessSegment) {
        this.businessSegment = businessSegment;
    }

    public CartContext getCartContext() {
        return cartContext;
    }

    public void setCartContext(CartContext cartContext) {
        this.cartContext = cartContext;
    }

    public ChannelEligibility getChannelEligibility() {
        return channelEligibility;
    }

    public void setChannelEligibility(ChannelEligibility channelEligibility) {
        this.channelEligibility = channelEligibility;
    }

    public List<String> getContractIndicator() {
        return contractIndicator;
    }

    public void setContractIndicator(List<String> contractIndicator) {
        this.contractIndicator = contractIndicator;
    }

    public CustomerContext getCustomerContext() {
        return customerContext;
    }

    public void setCustomerContext(CustomerContext customerContext) {
        this.customerContext = customerContext;
    }

    public CustomerEligibility getCustomerEligibility() {
        return customerEligibility;
    }

    public void setCustomerEligibility(CustomerEligibility customerEligibility) {
        this.customerEligibility = customerEligibility;
    }

    public List<CustomerIntent> getCustomerIntent() {
        return customerIntent;
    }

    public void setCustomerIntent(List<CustomerIntent> customerIntent) {
        this.customerIntent = customerIntent;
    }


    public String getHeartValue() {
        return heartValue;
    }

    public void setHeartValue(String heartValue) {
        this.heartValue = heartValue;
    }

    public List<String> getOfferActionType() {
        return offerActionType;
    }

    public void setOfferActionType(List<String> offerActionType) {
        this.offerActionType = offerActionType;
    }

    public List<String> getOfferCodes() {
        return offerCodes;
    }

    public void setOfferCodes(List<String> offerCodes) {
        this.offerCodes = offerCodes;
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

    private List<String> slsEligibleSubscriptionTypes;
    
    
    public List<String> getSlsEligibleSubscriptionTypes() {
		return slsEligibleSubscriptionTypes;
	}

	public void setSlsEligibleSubscriptionTypes(List<String> slsEligibleSubscriptionTypes) {
		this.slsEligibleSubscriptionTypes = slsEligibleSubscriptionTypes;
	}

	public void setOfferProductFamily(List<String> offerProductFamily) {
        this.offerProductFamily = offerProductFamily;
    }

    public List<String> getOfferProductType() {
    	if(offerProductType==null) {
    		offerProductType = new ArrayList<>();
    	}
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

    public List<String> getOfferTypes() {
        return offerTypes;
    }

    public void setOfferTypes(List<String> offerTypes) {
        this.offerTypes = offerTypes;
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

    public List<String> getProductCodes() {
        return productCodes;
    }

    public void setProductCodes(List<String> productCodes) {
        this.productCodes = productCodes;
    }

    public List<String> getQualifyingProductCodes() {
        return qualifyingProductCodes;
    }

    public void setQualifyingProductCodes(List<String> qualifyingProductCodes) {
        this.qualifyingProductCodes = qualifyingProductCodes;
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
    
    
    public List<String> getOfferClassificationType() {
		return offerClassificationType;
	}

	public void setOfferClassificationType(List<String> offerClassificationType) {
		this.offerClassificationType = offerClassificationType;
	}

	public List<String> getMigrationServiceType() {
		return migrationServiceType;
	}

	public void setMigrationServiceType(List<String> migrationServiceType) {
		this.migrationServiceType = migrationServiceType;
	}
	
	public List<String> getBenefitsCodesToSuppressOffer() {
		return benefitsCodesToSuppressOffer;
	}

	public void setBenefitsCodesToSuppressOffer(List<String> benefitsCodesToSuppressOffer) {
		this.benefitsCodesToSuppressOffer = benefitsCodesToSuppressOffer;
	}

    /**
	 * @return the benefitsCustomerHasReceived
	 */
	public List<BenefitsCustomerHasReceived> getBenefitsCustomerHasReceived() {
		return benefitsCustomerHasReceived;
	}

	/**
	 * @param benefitsCustomerHasReceived the benefitsCustomerHasReceived to set
	 */
	public void setBenefitsCustomerHasReceived(List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived) {
		this.benefitsCustomerHasReceived = benefitsCustomerHasReceived;
	}

	public String getCreditRisk() {
        return creditRisk;
    }

    public void setCreditRisk(String creditRisk) {
        this.creditRisk = creditRisk;
    }
    



	public ComplexFilterContext getComplexfilterContext() {
		return complexfilterContext;
	}

	public void setComplexfilterContext(ComplexFilterContext complexfilterContext) {
		this.complexfilterContext = complexfilterContext;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public List<String> getCustomerSubTypes() {
		return customerSubTypes;
	}

	public void setCustomerSubTypes(List<String> customerSubTypes) {
		this.customerSubTypes = customerSubTypes;
	}

	public String getFan() {
		return fan;
	}

	public void setFan(String fan) {
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

	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	public List<String> getBusinessSegments() {
		return businessSegments;
	}

	public void setBusinessSegments(List<String> businessSegments) {
		this.businessSegments = businessSegments;
	}
	
	public PartnerDealerDetails getPartnerDealerDetails() {
		return partnerDealerDetails;
	}

	public void setPartnerDealerDetails(PartnerDealerDetails partnerDealerDetails) {
		this.partnerDealerDetails = partnerDealerDetails;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OfferRequest [accountTypes=");
		builder.append(accountTypes);
		builder.append(", addOnType=");
		builder.append(addOnType);
		builder.append(", benefitCodes=");
		builder.append(benefitCodes);
		builder.append(", billingProductCodes=");
		builder.append(billingProductCodes);
		builder.append(", bundleProductCodes=");
		builder.append(bundleProductCodes);
		builder.append(", bundleProductIds=");
		builder.append(bundleProductIds);
		builder.append(", benefitsCodesToSuppressOffer=");
		builder.append(benefitsCodesToSuppressOffer);
		builder.append(", benefitsCustomerHasReceived=");
		builder.append(benefitsCustomerHasReceived);
		builder.append(", businessSegment=");
		builder.append(businessSegment);
		builder.append(", customerSegments=");
		builder.append(customerSegments);
		builder.append(", businessSegments=");
		builder.append(businessSegments);
		builder.append(", cartContext=");
		builder.append(cartContext);
		builder.append(", channelEligibility=");
		builder.append(channelEligibility);
		builder.append(", contractIndicator=");
		builder.append(contractIndicator);
		builder.append(", customerContext=");
		builder.append(customerContext);
		builder.append(", customerEligibility=");
		builder.append(customerEligibility);
		builder.append(", customerIntent=");
		builder.append(customerIntent);
		builder.append(", heartValue=");
		builder.append(heartValue);
		builder.append(", offerActionType=");
		builder.append(offerActionType);
		builder.append(", offerCodes=");
		builder.append(offerCodes);
		builder.append(", offerIds=");
		builder.append(offerIds);
		builder.append(", offerProductFamily=");
		builder.append(offerProductFamily);
		builder.append(", offerProductType=");
		builder.append(offerProductType);
		builder.append(", offerStatus=");
		builder.append(offerStatus);
		builder.append(", offerTypes=");
		builder.append(offerTypes);
		builder.append(", pagination=");
		builder.append(pagination);
		builder.append(", planSubType=");
		builder.append(planSubType);
		builder.append(", productCodes=");
		builder.append(productCodes);
		builder.append(", qualifyingProductCodes=");
		builder.append(qualifyingProductCodes);
		builder.append(", qualifyingProducts=");
		builder.append(qualifyingProducts);
		builder.append(", salesChannel=");
		builder.append(salesChannel);
		builder.append(", offerClassificationType=");
		builder.append(offerClassificationType);
		builder.append(", migrationServiceType=");
		builder.append(migrationServiceType);
		builder.append(", creditRisk=");
		builder.append(creditRisk);
		builder.append(", expiredOffers=");
		builder.append(expiredOffers);
		builder.append(", complexfilterContext=");
		builder.append(complexfilterContext);
		builder.append(", paymentType=");
		builder.append(paymentType);
		builder.append(", customerSubTypes=");
		builder.append(customerSubTypes);
		builder.append(", fan=");
		builder.append(fan);
		builder.append(", fanCategories=");
		builder.append(fanCategories);
		builder.append(", isOfferTrayEnabled=");
		builder.append(isOfferTrayEnabled);
		builder.append(", couponStatus=");
		builder.append(couponStatus);
		builder.append(", productsOnAccountToSuppressOffer=");
		builder.append(productsOnAccountToSuppressOffer);
		builder.append(", migrationIndicator=");
		builder.append(migrationIndicator);
		builder.append(", swimlaneSwitchEligible=");
		builder.append(swimlaneSwitchEligible);
		builder.append(", reconnectCustomer=");
		builder.append(reconnectCustomer);
		builder.append(", serviceEndDate=");
		builder.append(serviceEndDate);
		builder.append(", hasLocalChannels=");
		builder.append(hasLocalChannels);
		builder.append(", agentDealerDetails=");
		builder.append(agentDealerDetails);
		builder.append(", isCartModeOn=");
		builder.append(isCartModeOn);
		builder.append(", orderContext=");
		builder.append(orderContext);
		builder.append(", treatmentCode=");
		builder.append(treatmentCode);
		builder.append(", serverDate=");
		builder.append(serverDate);
		builder.append(", partnerDealerDetails=");
		builder.append(partnerDealerDetails);
		builder.append("]");
		return builder.toString();
	}

	public String getServerDate() {
		return serverDate;
	}

	public void setServerDate(String serverDate) {
		this.serverDate = serverDate;
	}
	
	private String customerSubType;	

	public String getCustomerSubType() {
		return customerSubType;
	}

	public void setCustomerSubType(String customerSubType) {
		this.customerSubType = customerSubType;
	}	
	
	private Boolean adeOffersLite;

	public Boolean getAdeOffersLite() {
		return adeOffersLite;
	}

	public void setAdeOffersLite(Boolean adeOffersLite) {
		this.adeOffersLite = adeOffersLite;
	}
	
    public Boolean getHasOTTLocalChannels() {
        return hasOTTLocalChannels;
    }

    public void setHasOTTLocalChannels(Boolean hasOTTLocalChannels) {
        this.hasOTTLocalChannels = hasOTTLocalChannels;
    }

}