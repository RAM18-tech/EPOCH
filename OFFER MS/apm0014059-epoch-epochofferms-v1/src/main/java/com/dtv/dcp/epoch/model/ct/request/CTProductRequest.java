package com.dtv.dcp.epoch.model.ct.request;

import java.io.Serializable;
import java.util.List;

import com.dtv.dcp.epoch.model.common.request.*;
import com.dtv.dcp.epoch.model.ct.product.Code;


public class CTProductRequest implements Serializable {

	private List<ProductInfo> cartProducts;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	private List<Code> codes;

	/** The accountTypes. */
	private List<String> accountTypes;

	/** The customerTypes. */
	private List<String> customerSegments; //si

	private List<String> businessSegment;

	/** The salesChannel. */
	private List<String> salesChannel; //si


	private List<String> productTypes; //si

	private List<String> offerCategories; //si

	private List<String> productFamily; //si

	private List<String> offerStatus; //si

	private List<String> productIds; //si
	/** The offerTypes. */
	private List<String> offerTypes; //si

	/** The offerIds. */
	private List<String> offerIds;//si


	private List<String> productCodes;//si

	private List<String> contractApplicable;//si

	private List<String> benefitCodes;//si

	private List<String> contractIndicator;

	private CTDeviceDetailsRequest devices;

	private List<String> productSalesChannel;//si

	/** The customerEligibility. */
	private CustomerEligibility customerEligibility; //si

	/** The channelEligibility. */
	private ChannelEligibility channelEligibility; //si

	/** The cartContext. */
	private CartContext cartContext; //si



	/** The offerActionType. */
	private List<String> offerActionType; //si



	/** The pagination. */
	private Pagination pagination;

	private String userType;

	private List<String> productStatus;

	private String state;

	private List<CTCustomerContext> customerContext;

	private String creditRiskEligibility;

	private List<String> addOnType;

	private List<String> planSubType;

	private List<String> billingProductCodes;

	private PriceProtection priceProtection;

	private CTProductNextBillingDate nextBillingDate;


	private List<String> enablerPricePlanCode;

	private String iapPartnerAccountType;

	/**
	 * @return the enablerPricePlanCode
	 */
	public List<String> getEnablerPricePlanCode() {
		return enablerPricePlanCode;
	}

	/**
	 * @param enablerPricePlanCode the enablerPricePlanCode to set
	 */
	public void setEnablerPricePlanCode(List<String> enablerPricePlanCode) {
		this.enablerPricePlanCode = enablerPricePlanCode;
	}

	public List<String> getBillingProductCodes() {
		return billingProductCodes;
	}

	public void setBillingProductCodes(List<String> billingProductCodes) {
		this.billingProductCodes = billingProductCodes;
	}


	public List<String> getAddOnType() {
		return addOnType;
	}

	public void setAddOnType(List<String> addOnType) {
		this.addOnType = addOnType;
	}

	public List<String> getPlanSubType() {
		return planSubType;
	}

	public void setPlanSubType(List<String> planSubType) {
		this.planSubType = planSubType;
	}

	public List<CTCustomerContext> getCustomerContext() {
		return customerContext;
	}

	public void setCustomerContext(List<CTCustomerContext> customerContext) {
		this.customerContext = customerContext;
	}

	public List<String> getBusinessSegment() {
		return businessSegment;
	}

	public void setBusinessSegment(List<String> businessSegment) {
		this.businessSegment = businessSegment;
	}

	public List<Code> getCodes() {
		return codes;
	}

	public void setCodes(List<Code> codes) {
		this.codes = codes;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<String> getProductStatus() {
		return productStatus;
	}

	public void setProductStatus(List<String> productStatus) {
		this.productStatus = productStatus;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public List<String> getContractIndicator() {
		return contractIndicator;
	}

	public void setContractIndicator(List<String> contractIndicator) {
		this.contractIndicator = contractIndicator;
	}

	/**
	 * @return the pagination
	 */
	public Pagination getPagination() {
		return pagination;
	}

	/**
	 * @param pagination the pagination to set
	 */
	public void setPagination(Pagination pagination) {
		this.pagination = pagination;
	}



	/**
	 * @return the accountTypes
	 */
	public List<String> getAccountTypes() {
		return accountTypes;
	}

	/**
	 * @param accountTypes the accountTypes to set
	 */
	public void setAccountTypes(List<String> accountTypes) {
		this.accountTypes = accountTypes;
	}



	/**
	 * @return the customerSegments
	 */
	public List<String> getCustomerSegments() {
		return customerSegments;
	}

	/**
	 * @param customerSegments the customerSegments to set
	 */
	public void setCustomerSegments(List<String> customerSegments) {
		this.customerSegments = customerSegments;
	}

	/**
	 * @return the salesChannel
	 */
	public List<String> getSalesChannel() {
		return salesChannel;
	}

	/**
	 * @param salesChannel the salesChannel to set
	 */
	public void setSalesChannel(List<String> salesChannel) {
		this.salesChannel = salesChannel;
	}



	/**
	 * @return the offerTypes
	 */
	public List<String> getOfferTypes() {
		return offerTypes;
	}

	/**
	 * @param offerTypes the offerTypes to set
	 */
	public void setOfferTypes(List<String> offerTypes) {
		this.offerTypes = offerTypes;
	}

	/**
	 * @return the offerIds
	 */
	public List<String> getOfferIds() {
		return offerIds;
	}

	/**
	 * @param offerIds the offerIds to set
	 */
	public void setOfferIds(List<String> offerIds) {
		this.offerIds = offerIds;
	}



	/**
	 * @return the offerActionType
	 */
	public List<String> getOfferActionType() {
		return offerActionType;
	}

	/**
	 * @param offerActionType the offerActionType to set
	 */
	public void setOfferActionType(List<String> offerActionType) {
		this.offerActionType = offerActionType;
	}


	public List<String> getProductTypes() {
		return productTypes;
	}

	public void setProductTypes(List<String> productTypes) {
		this.productTypes = productTypes;
	}

	public List<String> getOfferCategories() {
		return offerCategories;
	}

	public void setOfferCategories(List<String> offerCategories) {
		this.offerCategories = offerCategories;
	}

	public List<String> getProductFamily() {
		return productFamily;
	}

	public void setProductFamily(List<String> productFamily) {
		this.productFamily = productFamily;
	}

	public List<String> getOfferStatus() {
		return offerStatus;
	}

	public void setOfferStatus(List<String> offerStatus) {
		this.offerStatus = offerStatus;
	}

	public List<String> getProductIds() {
		return productIds;
	}

	public void setProductIds(List<String> productIds) {
		this.productIds = productIds;
	}

	public List<String> getProductCodes() {
		return productCodes;
	}

	public void setProductCodes(List<String> productCodes) {
		this.productCodes = productCodes;
	}

	public List<String> getContractApplicable() {
		return contractApplicable;
	}

	public void setContractApplicable(List<String> contractApplicable) {
		this.contractApplicable = contractApplicable;
	}

	public List<String> getBenefitCodes() {
		return benefitCodes;
	}

	public void setBenefitCodes(List<String> benefitCodes) {
		this.benefitCodes = benefitCodes;
	}

	public CustomerEligibility getCustomerEligibility() {
		return customerEligibility;
	}

	public void setCustomerEligibility(CustomerEligibility customerEligibility) {
		this.customerEligibility = customerEligibility;
	}

	public ChannelEligibility getChannelEligibility() {
		return channelEligibility;
	}

	public void setChannelEligibility(ChannelEligibility channelEligibility) {
		this.channelEligibility = channelEligibility;
	}

	public CartContext getCartContext() {
		return cartContext;
	}

	public void setCartContext(CartContext cartContext) {
		this.cartContext = cartContext;
	}

	public String getCreditRiskEligibility() {
		return creditRiskEligibility;
	}

	public void setCreditRiskEligibility(String creditRiskEligibility) {
		this.creditRiskEligibility = creditRiskEligibility;
	}

	public PriceProtection getPriceProtection() {
		return priceProtection;
	}

	public void setPriceProtection(PriceProtection priceProtection) {
		this.priceProtection = priceProtection;
	}


	public CTProductNextBillingDate getNextBillingDate() {
		return nextBillingDate;
	}

	public void setNextBillingDate(CTProductNextBillingDate nextBillingDate) {
		this.nextBillingDate = nextBillingDate;
	}

	public CTDeviceDetailsRequest getDevices() {
		return devices;
	}

	public void setDevices(CTDeviceDetailsRequest devices) {
		this.devices = devices;
	}

	public List<String> getProductSalesChannel() {
		return productSalesChannel;
	}

	public void setProductSalesChannel(List<String> productSalesChannel) {
		this.productSalesChannel = productSalesChannel;
	}

	public String getIapPartnerAccountType() {
		return iapPartnerAccountType;
	}

	public void setIapPartnerAccountType(String iapPartnerAccountType) {
		this.iapPartnerAccountType = iapPartnerAccountType;
	}


	private AdditionalAccountDetails additionalAccountDetails;

	public AdditionalAccountDetails getAdditionalAccountDetails() {
		return additionalAccountDetails;
	}

	public void setAdditionalAccountDetails(AdditionalAccountDetails additionalAccountDetails) {
		this.additionalAccountDetails = additionalAccountDetails;
	}

	@Override
	public String toString() {
		return "CTProductRequest{" +
				"codes=" + codes +
				", accountTypes=" + accountTypes +
				", customerSegments=" + customerSegments +
				", businessSegment=" + businessSegment +
				", salesChannel=" + salesChannel +
				", productTypes=" + productTypes +
				", offerCategories=" + offerCategories +
				", productFamily=" + productFamily +
				", offerStatus=" + offerStatus +
				", productIds=" + productIds +
				", offerTypes=" + offerTypes +
				", offerIds=" + offerIds +
				", productCodes=" + productCodes +
				", contractApplicable=" + contractApplicable +
				", benefitCodes=" + benefitCodes +
				", contractIndicator=" + contractIndicator +
				", customerEligibility=" + customerEligibility +
				", channelEligibility=" + channelEligibility +
				", cartContext=" + cartContext +
				", offerActionType=" + offerActionType +
				", pagination=" + pagination +
				", userType='" + userType + '\'' +
				", productStatus=" + productStatus +
				", state='" + state + '\'' +
				", customerContext=" + customerContext +
				", devices=" + devices +
				", creditRiskEligibility='" + creditRiskEligibility + '\'' +
				", enablerPricePlanCode='" + enablerPricePlanCode + '\'' +
				'}';
	}

	public List<ProductInfo> getCartProducts() {
		return cartProducts;
	}

	public void setCartProducts(List<ProductInfo> cartProducts) {
		this.cartProducts = cartProducts;
	}


}
