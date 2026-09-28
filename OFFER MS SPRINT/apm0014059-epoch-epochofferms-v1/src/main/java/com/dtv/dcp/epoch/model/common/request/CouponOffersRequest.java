package com.dtv.dcp.epoch.model.common.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.CustomerContext;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CouponOffersRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<String> offerActionType;
	private String salesChannel;
	private String offerProductFamily;
	private String accountTypes;
	private String businessSegment;
	private String customerSegments;
	private List<String> contractIndicator;
	private Boolean decisioningFlow;
	private Boolean reconnectCustomer;
	private String serviceEndDate;
	private String existingAccountSubscriberType;
	private Boolean swimlaneSwitchEligible;
	private List<String> migrationServiceType;
	private boolean migrationIndicator;
	private String creditRisk;
	private CustomerAddress customerAddress;
	private AgentChannelDetails agentChannelDetails;
	private AgentDealerDetails agentDealerDetails;
	private OnlinePartnerDetails onlinePartnerDetails;
	private CartContexts cartContext;
	private CustomerContext customerContext;
	private List<String> marketingSourceCode;
	private String slsEligibleSubscriptionType;


    private String customerSubscriptionType;

	public String getSlsEligibleSubscriptionType() {
		return slsEligibleSubscriptionType;
	}

	public String getCustomerSubscriptionType() {
		return customerSubscriptionType;
	}

    public void setCustomerSubscriptionType(String customerSubscriptionType) {
        this.customerSubscriptionType = customerSubscriptionType;
    }


    public List<String> getMarketingSourceCode() {
		return marketingSourceCode;
	}

	private List<String> bundleProductIds;


	public ChannelEligibility getChannelEligibility() {
		return channelEligibility;
	}

	public void setChannelEligibility(ChannelEligibility channelEligibility) {
		this.channelEligibility = channelEligibility;
	}

	private ChannelEligibility channelEligibility;

	public String getAccountTypes() {
		return accountTypes;
	}

	public void setAccountTypes(String accountTypes) {
		this.accountTypes = accountTypes;
	}

	private List<String> couponCodes;
	private List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived;

	public List<BenefitsCustomerHasReceived> getBenefitsCustomerHasReceived() {
		return benefitsCustomerHasReceived;
	}

	public void setBenefitsCustomerHasReceived(List<BenefitsCustomerHasReceived> benefitsCustomerHasReceived) {
		this.benefitsCustomerHasReceived = benefitsCustomerHasReceived;
	}

	public List<String> getOfferActionType() {
		return offerActionType;
	}

	public void setOfferActionType(List<String> offerActionType) {
		this.offerActionType = offerActionType;
	}

	public String getSalesChannel() {
		return salesChannel;
	}

	public void setSalesChannel(String salesChannel) {
		this.salesChannel = salesChannel;
	}

	public String getOfferProductFamily() {
		return offerProductFamily;
	}

	public void setOfferProductFamily(String offerProductFamily) {
		this.offerProductFamily = offerProductFamily;
	}

	public String getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(String businessSegment) {
		this.businessSegment = businessSegment;
	}

	public String getCustomerSegments() {
		return customerSegments;
	}

	public void setCustomerSegments(String customerSegments) {
		this.customerSegments = customerSegments;
	}

	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	public Boolean getDecisioningFlow() {
		return decisioningFlow;
	}

	public void setDecisioningFlow(Boolean decisioningFlow) {
		this.decisioningFlow = decisioningFlow;
	}

	public Boolean getReconnectCustomer() {
		return reconnectCustomer;
	}

	public void setReconnectCustomer(Boolean reconnectCustomer) {
		this.reconnectCustomer = reconnectCustomer;
	}

	public String getServiceEndDate() {
		return serviceEndDate;
	}

	public void setServiceEndDate(String serviceEndDate) {
		this.serviceEndDate = serviceEndDate;
	}

	public String getExistingAccountSubscriberType() {
		return existingAccountSubscriberType;
	}

	public void setExistingAccountSubscriberType(String existingAccountSubscriberType) {
		this.existingAccountSubscriberType = existingAccountSubscriberType;
	}

	public Boolean getSwimlaneSwitchEligible() {
		return swimlaneSwitchEligible;
	}

	public void setSwimlaneSwitchEligible(Boolean swimlaneSwitchEligible) {
		this.swimlaneSwitchEligible = swimlaneSwitchEligible;
	}

	public List<String> getMigrationServiceType() {
		return migrationServiceType;
	}

	public void setMigrationServiceType(List<String> migrationServiceType) {
		this.migrationServiceType = migrationServiceType;
	}

	public boolean isMigrationIndicator() {
		return migrationIndicator;
	}

	public void setMigrationIndicator(boolean migrationIndicator) {
		this.migrationIndicator = migrationIndicator;
	}

	public String getCreditRisk() {
		return creditRisk;
	}

	public void setCreditRisk(String creditRisk) {
		this.creditRisk = creditRisk;
	}

	public CustomerAddress getCustomerAddress() {
		return customerAddress;
	}

	public void setCustomerAddress(CustomerAddress customerAddress) {
		this.customerAddress = customerAddress;
	}

	public AgentChannelDetails getAgentChannelDetails() {
		return agentChannelDetails;
	}

	public void setAgentChannelDetails(AgentChannelDetails agentChannelDetails) {
		this.agentChannelDetails = agentChannelDetails;
	}

	public AgentDealerDetails getAgentDealerDetails() {
		return agentDealerDetails;
	}

	public void setAgentDealerDetails(AgentDealerDetails agentDealerDetails) {
		this.agentDealerDetails = agentDealerDetails;
	}

	public OnlinePartnerDetails getOnlinePartnerDetails() {
		return onlinePartnerDetails;
	}

	public void setOnlinePartnerDetails(OnlinePartnerDetails onlinePartnerDetails) {
		this.onlinePartnerDetails = onlinePartnerDetails;
	}

	public CartContexts getCartContext() {
		return cartContext;
	}

	public void setCartContext(CartContexts cartContext) {
		this.cartContext = cartContext;
	}

	public CustomerContext getCustomerContext() {
		return customerContext;
	}

	public void setCustomerContext(CustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	public List<String> getCouponCodes() {
		return couponCodes;
	}

	public void setCouponCodes(List<String> couponCodes) {
		this.couponCodes = couponCodes;
	}

	private PreviousCustomerAddress previousCustomerAddress;
	public PreviousCustomerAddress getPreviousCustomerAddress() {
		return previousCustomerAddress;
	}

	public void setPreviousCustomerAddress(PreviousCustomerAddress previousCustomerAddress) {
		this.previousCustomerAddress = previousCustomerAddress;
	}

	@JsonProperty("offerProductTypes")
	private List<String> offerProductType;

	public List<String> getOfferProductType() {
		return offerProductType;
	}

	public void setOfferProductType(List<String> offerProductType) {
		this.offerProductType = offerProductType;
	}
	public List<String> getBundleProductIds() {
		return bundleProductIds;
	}
	public void setBundleProductIds(List<String> bundleProductIds) {
		this.bundleProductIds = bundleProductIds;
	}
	private String treatmentcode;
	public String getTreatmentcode() {
		return treatmentcode;
	}
	public void setTreatmentcode(String treatmentcode) {
		this.treatmentcode = treatmentcode;
	}
}